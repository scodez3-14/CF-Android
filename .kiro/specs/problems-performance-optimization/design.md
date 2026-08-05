# Design Document: Problems Performance Optimization

## Overview

This design addresses critical performance bottlenecks in the Codeforces Android app's Problems List and Problem Detail screens. The current implementation exhibits two major performance issues:

1. **Problems List**: Takes 4-5 seconds to load despite having Room database caching
2. **Problem Detail**: Takes 4-5 seconds per problem due to uncached HTML scraping

The optimization strategy implements a **cache-first architecture** with background refresh, ensuring instant loads from cache while maintaining data freshness. Key improvements include:

- **Cache-first loading**: Emit cached data immediately (< 500ms for list, < 1s for details), then refresh in background
- **Search debouncing**: 300ms delay prevents filter execution on every keystroke
- **Optimized filtering**: O(N) complexity with result caching and parameter change detection
- **Parallel network fetching**: Try multiple URLs concurrently for problem details
- **Structured caching**: New `ProblemDetailEntity` stores scraped HTML, samples, and editorials
- **Offline support**: Full functionality with cached data when network is unavailable

**Target Performance:**
- Problems list first display: < 500ms (from cache)
- Problem detail first display: < 1s (from cache)
- Search/filter operations: < 100ms for 10,000 problems
- Cache hit rate: > 90% after initial load


## Architecture

### High-Level Architecture

The architecture follows the existing MVVM pattern with enhanced caching and background synchronization:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                       │
│  ┌────────────────────┐            ┌──────────────────────┐    │
│  │ ProblemsViewModel  │            │ ProblemDetailViewModel│    │
│  │ - StateFlow<State> │            │ - StateFlow<State>   │    │
│  │ - Debouncer        │            │ - Cache validator    │    │
│  │ - Filter engine    │            │                      │    │
│  └─────────┬──────────┘            └──────────┬───────────┘    │
└────────────┼─────────────────────────────────┼─────────────────┘
             │                                  │
             │ Flow<Resource<T>>                │
             │                                  │
┌────────────┼─────────────────────────────────┼─────────────────┐
│            ▼        Domain/Repository Layer   ▼                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            CodeforcesRepository                          │  │
│  │  - Cache-first emission strategy                        │  │
│  │  - Background sync coordination                         │  │
│  │  - Staleness detection (24h for problems, 7d for        │  │
│  │    details)                                             │  │
│  └────────────┬─────────────────────────────┬──────────────┘  │
└───────────────┼─────────────────────────────┼─────────────────┘
                │                              │
      ┌─────────┴────────┐          ┌─────────┴────────┐
      │                  │          │                  │
┌─────▼──────────┐ ┌────▼──────────▼────┐ ┌──────────▼─────────┐
│ CodeforcesAPI  │ │ CodeforcesDatabase  │ │  HTMLScraperService│
│ (Retrofit)     │ │ (Room)              │ │  (OkHttp + Jsoup)  │
│                │ │ - ProblemDao        │ │  - Parallel fetching│
│                │ │ - ProblemDetailDao  │ │  - Async editorial  │
└────────────────┘ └─────────────────────┘ └────────────────────┘
```

### Cache-First Flow

**Problems List Loading:**
```
User opens screen
    ↓
ViewModel calls repository.getProblems()
    ↓
Repository emits Loading()
    ↓
Repository queries ProblemDao.getAllProblems()
    ↓
If cache exists → Emit Success(cached data)  [< 500ms]
    ↓
Check if cache is stale (> 24 hours)
    ↓
Launch background coroutine:
    - Fetch from API
    - Update ProblemDao
    - Emit Success(fresh data)
    ↓
UI updates seamlessly
```


**Problem Detail Loading:**
```
User taps problem
    ↓
ViewModel calls load(contestId, index)
    ↓
ViewModel queries ProblemDetailDao.getDetail(contestId, index)
    ↓
Cache hit (< 7 days old)?
    ├─ YES → Display cached content [< 1s]
    │         Launch background refresh
    └─ NO  → Display loading indicator
             Scrape from web (parallel URLs)
             Save to ProblemDetailDao
             Display fresh content
```

### Key Architectural Decisions

**1. Cache-First with Background Refresh (Stale-While-Revalidate)**
- **Decision**: Always emit cached data first if available, then fetch fresh data in background
- **Rationale**: Provides instant perceived performance while maintaining data freshness
- **Trade-off**: Users may briefly see stale data, but this is acceptable for problem metadata which changes infrequently

**2. Different Staleness Windows**
- **Problems List**: 24 hours (metadata changes infrequently)
- **Problem Details**: 7 days (statement/editorial rarely change after contest ends)
- **Rationale**: Balances freshness with network efficiency

**3. Debouncing in ViewModel (Not Repository)**
- **Decision**: Implement search debouncing at the ViewModel layer
- **Rationale**: ViewModels already manage UI state; keeps repository focused on data operations
- **Implementation**: Use `Flow.debounce(300)` on search query changes

**4. Parallel URL Fetching with Coroutine Race**
- **Decision**: Launch multiple HTTP requests concurrently and return first successful response
- **Rationale**: Codeforces problems exist at multiple URLs (/contest/, /problemset/, /gym/); trying them in parallel reduces worst-case latency from 15s (3 × 5s timeout) to 5s
- **Implementation**: `async`/`await` with timeout


## Components and Interfaces

### 1. Enhanced CodeforcesRepository

**New Methods:**
```kotlin
interface ProblemsDataSource {
    // Cache-first with background refresh
    fun getProblemsWithCache(): Flow<Resource<ProblemSetResultDto>>
    
    // Get cache metadata
    suspend fun getProblemsCacheTimestamp(): Long?
    
    // Force refresh (invalidate cache)
    suspend fun refreshProblems(): Flow<Resource<ProblemSetResultDto>>
    
    // Clear problem cache
    suspend fun clearProblemsCache()
    
    // Get problem detail with cache
    suspend fun getProblemDetail(
        contestId: Int, 
        index: String
    ): Flow<Resource<ProblemDetail>>
    
    // Clear problem details cache
    suspend fun clearProblemDetailsCache()
}
```

**Implementation Notes:**
- `getProblemsWithCache()` replaces existing `getProblems()` for cache-first behavior
- Repository checks `cachedAt` timestamp to determine staleness
- Background refresh uses `viewModelScope` or `CoroutineScope(SupervisorJob() + Dispatchers.IO)`

### 2. ProblemDao Enhancements

**New Methods:**
```kotlin
@Dao
interface ProblemDao {
    // Existing methods...
    @Query("SELECT * FROM cached_problems ORDER BY rating ASC")
    fun getAllProblems(): Flow<List<CachedProblemEntity>>
    
    // New: Get cache timestamp
    @Query("SELECT MAX(cachedAt) FROM cached_problems")
    suspend fun getCacheTimestamp(): Long?
    
    // New: Batch insert with timestamp
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblemsWithTimestamp(
        problems: List<CachedProblemEntity>
    )
    
    // Existing clear method
    @Query("DELETE FROM cached_problems")
    suspend fun clearAll()
}
```


### 3. New ProblemDetailDao

```kotlin
@Dao
interface ProblemDetailDao {
    @Query("""
        SELECT * FROM problem_details 
        WHERE contestId = :contestId AND `index` = :index
    """)
    suspend fun getDetail(contestId: Int, index: String): ProblemDetailEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: ProblemDetailEntity)
    
    @Query("DELETE FROM problem_details")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM problem_details")
    suspend fun count(): Int
    
    @Query("DELETE FROM problem_details WHERE cachedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
```

**Usage Pattern:**
```kotlin
// In repository
suspend fun getProblemDetail(contestId: Int, index: String): Flow<Resource<ProblemDetail>> = flow {
    emit(Resource.Loading())
    
    // Try cache first
    val cached = problemDetailDao.getDetail(contestId, index)
    if (cached != null) {
        emit(Resource.Success(cached.toDomain()))
        
        // Check staleness
        val age = System.currentTimeMillis() - cached.cachedAt
        if (age < TimeUnit.DAYS.toMillis(7)) {
            return@flow // Cache is fresh, done
        }
    }
    
    // Fetch from network
    try {
        val scraped = htmlScraper.scrapeProblemDetail(contestId, index)
        val entity = scraped.toEntity()
        problemDetailDao.insertDetail(entity)
        emit(Resource.Success(scraped))
    } catch (e: Exception) {
        if (cached != null) {
            // Already emitted cached data, log error but don't emit Error
        } else {
            emit(Resource.Error(e.message ?: "Failed to fetch"))
        }
    }
}
```


### 4. Enhanced ProblemsViewModel

**Debounced Search Implementation:**
```kotlin
@HiltViewModel
class ProblemsViewModel @Inject constructor(
    private val repo: CodeforcesRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProblemsUiState())
    val state: StateFlow<ProblemsUiState> = _state.asStateFlow()
    
    // Search query as a flow for debouncing
    private val searchQueryFlow = MutableStateFlow("")
    
    init {
        loadProblems()
        setupDebouncedSearch()
    }
    
    private fun setupDebouncedSearch() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300) // 300ms delay
                .distinctUntilChanged()
                .collect { query ->
                    _state.update { it.copy(searchQuery = query) }
                    applyFilters()
                }
        }
    }
    
    fun setSearchQuery(query: String) {
        // Immediate update for empty query (clear search)
        if (query.isEmpty()) {
            _state.update { it.copy(searchQuery = query) }
            applyFilters()
        } else {
            searchQueryFlow.value = query
        }
    }
    
    // Optimized filter engine
    private var lastFilterParams: FilterParams? = null
    private var lastFilteredResult: List<ProblemDto> = emptyList()
    
    private fun applyFilters() {
        val s = _state.value
        val params = FilterParams(s.searchQuery, s.selectedTags, 
                                   s.ratingFilterEnabled, s.minRating, s.maxRating)
        
        // Skip if parameters haven't changed
        if (params == lastFilterParams && lastFilteredResult.isNotEmpty()) {
            return
        }
        
        lastFilterParams = params
        lastFilteredResult = filterProblems(s.problems, params)
        _state.update { it.copy(filteredProblems = lastFilteredResult) }
    }
    
    private fun filterProblems(
        problems: List<ProblemDto>, 
        params: FilterParams
    ): List<ProblemDto> {
        return problems
            .asSequence() // Lazy evaluation
            .filter { p -> matchesSearch(p, params.searchQuery) }
            .filter { p -> matchesTags(p, params.selectedTags) }
            .filter { p -> matchesRating(p, params) }
            .sortedWith(compareByDescending<ProblemDto> { it.contestId ?: 0 }
                .thenBy { it.index })
            .toList()
    }
}

data class FilterParams(
    val searchQuery: String,
    val selectedTags: Set<String>,
    val ratingFilterEnabled: Boolean,
    val minRating: Int,
    val maxRating: Int
)
```


### 5. HTMLScraperService with Parallel Fetching

**Refactored from ProblemDetailViewModel:**
```kotlin
@Singleton
class HTMLScraperService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val userAgent = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36"
    
    suspend fun scrapeProblemDetail(
        contestId: Int,
        index: String
    ): ProblemDetail = coroutineScope {
        // Parallel URL fetching
        val urls = listOf(
            "https://codeforces.com/contest/$contestId/problem/$index?locale=en",
            "https://codeforces.com/problemset/problem/$contestId/$index?locale=en",
            "https://codeforces.com/gym/$contestId/problem/$index?locale=en"
        )
        
        // Launch all requests in parallel
        val deferreds = urls.map { url ->
            async(Dispatchers.IO) {
                withTimeout(10_000) { // 10s per request
                    fetchAndParse(url)
                }
            }
        }
        
        // Return first successful result
        var lastException: Exception? = null
        for (deferred in deferreds) {
            try {
                val result = deferred.await()
                if (result != null) {
                    // Cancel remaining requests
                    deferreds.forEach { if (it != deferred) it.cancel() }
                    return@coroutineScope result
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        
        throw lastException ?: Exception("All URLs failed")
    }
    
    // Async editorial fetch (doesn't block main scraping)
    private suspend fun scrapeEditorialAsync(contestId: Int): Deferred<String?> {
        return coroutineScope {
            async(Dispatchers.IO) {
                try {
                    withTimeout(10_000) {
                        scrapeEditorialContent(contestId)
                    }
                } catch (e: Exception) {
                    null // Editorial is optional
                }
            }
        }
    }
    
    private fun fetchAndParse(url: String): ProblemDetail? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .build()
            
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val html = response.body?.string() ?: return null
            return parseHTML(html, url)
        }
    }
}
```


## Data Models

### Enhanced CachedProblemEntity

**No changes to existing entity** — already has `cachedAt` field:
```kotlin
@Entity(tableName = "cached_problems")
data class CachedProblemEntity(
    @PrimaryKey val id: String, // contestId + index
    val contestId: Int?,
    val index: String,
    val name: String,
    val rating: Int?,
    val tags: List<String>,
    val solvedCount: Int,
    val cachedAt: Long = System.currentTimeMillis() // Already present
)
```

### New ProblemDetailEntity

```kotlin
@Entity(
    tableName = "problem_details",
    primaryKeys = ["contestId", "index"]
)
data class ProblemDetailEntity(
    val contestId: Int,
    val index: String,
    val name: String,
    val rating: Int?,
    @TypeConverters(Converters::class)
    val tags: List<String>,
    val timeLimit: String,
    val memoryLimit: String,
    val statementHtml: String, // Complete .problem-statement div HTML
    val samplesJson: String,   // JSON array of {input, output} objects
    val editorialHtml: String?, // Nullable, may not exist for all problems
    val cachedAt: Long = System.currentTimeMillis()
)

// Domain model (unchanged from current ProblemDetailViewModel)
data class ProblemDetail(
    val name: String,
    val contestId: String,
    val index: String,
    val rating: Int?,
    val tags: List<String>,
    val timeLimit: String,
    val memoryLimit: String,
    val statementHtml: String,
    val samples: List<SampleTest>,
    val editorialHtml: String?
)

data class SampleTest(
    val input: String,
    val output: String
)
```

**Mapping Functions:**
```kotlin
fun ProblemDetailEntity.toDomain(): ProblemDetail {
    val samples = Json.decodeFromString<List<SampleTest>>(samplesJson)
    return ProblemDetail(
        name = name,
        contestId = contestId.toString(),
        index = index,
        rating = rating,
        tags = tags,
        timeLimit = timeLimit,
        memoryLimit = memoryLimit,
        statementHtml = statementHtml,
        samples = samples,
        editorialHtml = editorialHtml
    )
}

fun ProblemDetail.toEntity(): ProblemDetailEntity {
    val samplesJson = Json.encodeToString(samples)
    return ProblemDetailEntity(
        contestId = contestId.toInt(),
        index = index,
        name = name,
        rating = rating,
        tags = tags,
        timeLimit = timeLimit,
        memoryLimit = memoryLimit,
        statementHtml = statementHtml,
        samplesJson = samplesJson,
        editorialHtml = editorialHtml
    )
}
```


### Database Migration

**Migration from Version 1 to Version 2:**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS problem_details (
                contestId INTEGER NOT NULL,
                `index` TEXT NOT NULL,
                name TEXT NOT NULL,
                rating INTEGER,
                tags TEXT NOT NULL,
                timeLimit TEXT NOT NULL,
                memoryLimit TEXT NOT NULL,
                statementHtml TEXT NOT NULL,
                samplesJson TEXT NOT NULL,
                editorialHtml TEXT,
                cachedAt INTEGER NOT NULL,
                PRIMARY KEY(contestId, `index`)
            )
        """.trimIndent())
    }
}

// In AppModule.kt
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): CodeforcesDatabase {
    return Room.databaseBuilder(
        context,
        CodeforcesDatabase::class.java,
        "codeforces_db"
    )
    .addMigrations(MIGRATION_1_2)
    .fallbackToDestructiveMigration() // Safety net for dev builds
    .build()
}
```

**Updated Database Class:**
```kotlin
@Database(
    entities = [
        CachedUserEntity::class, 
        CachedProblemEntity::class, 
        CachedContestEntity::class,
        ProblemDetailEntity::class  // NEW
    ],
    version = 2,  // Incremented
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CodeforcesDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun problemDao(): ProblemDao
    abstract fun contestDao(): ContestDao
    abstract fun problemDetailDao(): ProblemDetailDao  // NEW
}
```

