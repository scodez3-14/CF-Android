# Requirements Document

## Introduction

This feature optimizes the performance of the Problems List and Problem Detail screens in the Codeforces Android app. The current implementation suffers from two critical performance bottlenecks: (1) the Problems List screen takes 4-5 seconds to load despite having a Room database cache, and (2) the Problem Detail screen takes 4-5 seconds per problem because it scrapes HTML from the web on every view with no caching.

The optimization will implement a cache-first strategy with background refresh for both screens, add debouncing to search inputs, optimize filtering operations, and introduce caching for scraped problem details. The result will be instant loading from cache (< 500ms for problem list, < 1 second for problem details) while maintaining data freshness through background synchronization.

## Glossary

- **Problems_Repository**: The CodeforcesRepository class responsible for fetching and caching problem data
- **Problems_ViewModel**: The ProblemsViewModel class managing UI state for the problems list screen
- **Problem_Detail_ViewModel**: The ProblemDetailViewModel class managing UI state for the problem detail screen
- **Problem_Dao**: Room database DAO interface for problem cache operations
- **Cache_Validator**: Logic that determines if cached data is stale based on timestamp
- **Background_Sync**: Coroutine-based operation that fetches fresh data from network without blocking UI
- **Filter_Engine**: Logic that applies search query, tags, and rating filters to problem lists
- **Search_Debouncer**: Mechanism that delays search execution until user stops typing
- **Problem_Detail_Entity**: Room database entity for cached problem details (HTML, samples, editorial)
- **Problem_Detail_Dao**: Room database DAO interface for problem detail cache operations
- **HTML_Scraper**: Service that extracts problem content from Codeforces web pages
- **Cache_Hit**: When requested data exists in local database and is not stale
- **Cache_Miss**: When requested data does not exist in local database or is stale
- **Stale_Cache**: Cached data older than the configured freshness duration

## Requirements

### Requirement 1: Cache-First Loading for Problems List

**User Story:** As a user, I want the problems list to load instantly from cache, so that I can browse problems without waiting for network requests.

#### Acceptance Criteria

1. WHEN the Problems List screen is opened, THE Problems_Repository SHALL emit cached problems from Problem_Dao within 500 milliseconds
2. WHEN cached problems exist in Problem_Dao, THE Problems_ViewModel SHALL display them immediately before fetching fresh data
3. THE Problems_Repository SHALL determine if cached data is stale by comparing current time with the cachedAt timestamp
4. WHERE cached data is older than 24 hours, THE Problems_Repository SHALL mark it as Stale_Cache
5. WHEN displaying cached problems, THE Problems_ViewModel SHALL trigger Background_Sync to fetch fresh data from the API
6. WHEN Background_Sync completes successfully, THE Problems_Repository SHALL update Problem_Dao and emit the new data
7. THE Problems_ViewModel SHALL update the displayed list when fresh data arrives without disrupting user scrolling position

### Requirement 2: Optimized Search with Debouncing

**User Story:** As a user, I want search to be responsive without triggering on every keystroke, so that the app remains smooth while I type.

#### Acceptance Criteria

1. WHEN the user types in the search field, THE Problems_ViewModel SHALL delay filter execution by 300 milliseconds
2. WHEN the user types additional characters within the delay period, THE Search_Debouncer SHALL reset the timer
3. WHEN the delay period expires without new input, THE Filter_Engine SHALL execute the search filter
4. THE Search_Debouncer SHALL cancel any pending filter execution when the user types a new character
5. WHEN the search query is cleared, THE Filter_Engine SHALL apply filters immediately without debouncing

### Requirement 3: Optimized Filtering Operations

**User Story:** As a user, I want filtering and sorting to be efficient, so that the UI remains responsive when I change filters.

#### Acceptance Criteria

1. THE Filter_Engine SHALL compute filtered results only when filter parameters change (search query, tags, rating range)
2. THE Filter_Engine SHALL reuse the previous filtered results when no parameters have changed
3. WHEN filtering a list of N problems, THE Filter_Engine SHALL complete within O(N) time complexity
4. WHEN sorting filtered results, THE Filter_Engine SHALL use stable sorting to preserve relative order of equal elements
5. THE Filter_Engine SHALL apply filters in the order: search filter, then tag filter, then rating filter to minimize intermediate list sizes
6. THE Problems_ViewModel SHALL prevent concurrent filter operations by canceling in-progress filtering when new parameters arrive

### Requirement 4: Cache-First Loading for Problem Details

**User Story:** As a user, I want problem details to load instantly from cache, so that I can review problems I've seen before without waiting.

#### Acceptance Criteria

1. WHEN a problem detail screen is opened, THE Problem_Detail_ViewModel SHALL query Problem_Detail_Dao for cached content
2. WHEN cached problem detail exists and is less than 7 days old, THE Problem_Detail_ViewModel SHALL display it within 1 second
3. WHEN cached problem detail exists but is older than 7 days, THE Problem_Detail_ViewModel SHALL display it immediately and trigger Background_Sync
4. WHEN no cached problem detail exists, THE Problem_Detail_ViewModel SHALL fetch from the network and display a loading indicator
5. THE Problem_Detail_Entity SHALL store the problem statement HTML, sample tests, time limit, memory limit, and editorial HTML
6. WHEN Background_Sync fetches fresh problem details, THE Problem_Detail_Dao SHALL update the cached content
7. THE Problem_Detail_ViewModel SHALL update the display when fresh content arrives without disrupting the user's scroll position

### Requirement 5: Optimized HTML Scraping for Problem Details

**User Story:** As a developer, I want HTML scraping to be efficient, so that fetching problem details from the network is as fast as possible when cache misses occur.

#### Acceptance Criteria

1. WHEN scraping problem content, THE HTML_Scraper SHALL try URLs in parallel rather than sequentially
2. WHEN multiple URL requests are in flight, THE HTML_Scraper SHALL return the first successful response
3. WHEN all URL requests fail, THE HTML_Scraper SHALL return an error with the most informative failure message
4. WHEN fetching editorial content, THE HTML_Scraper SHALL execute the editorial fetch asynchronously independent of the problem statement fetch
5. WHEN the editorial fetch fails, THE HTML_Scraper SHALL return the problem detail with null editorial content rather than failing the entire operation
6. THE HTML_Scraper SHALL apply a 10-second timeout to each HTTP request to prevent indefinite waiting
7. THE HTML_Scraper SHALL reuse OkHttpClient connection pools to minimize connection overhead

### Requirement 6: Cache Statistics and Management

**User Story:** As a user, I want to see cache status and manage cached data, so that I understand data freshness and can free up storage if needed.

#### Acceptance Criteria

1. THE Problems_Repository SHALL expose the cache timestamp of the problems list
2. THE Problems_ViewModel SHALL display the cache age (e.g., "Updated 2 hours ago") in the UI
3. WHEN the user triggers manual refresh, THE Problems_Repository SHALL invalidate the cache and fetch fresh data from the network
4. THE Problems_Repository SHALL provide a method to clear all cached problems
5. THE Problem_Detail_ViewModel SHALL provide a method to clear all cached problem details
6. WHEN clearing cache, THE Problem_Dao SHALL delete all rows from cached_problems table
7. WHEN clearing cache, THE Problem_Detail_Dao SHALL delete all rows from the problem details table

### Requirement 7: Database Schema for Problem Details

**User Story:** As a developer, I want a Room database entity for problem details, so that scraped content can be persisted locally.

#### Acceptance Criteria

1. THE Problem_Detail_Entity SHALL have a composite primary key consisting of contestId and index
2. THE Problem_Detail_Entity SHALL store statementHtml as a TEXT field
3. THE Problem_Detail_Entity SHALL store samples as a JSON string in a TEXT field
4. THE Problem_Detail_Entity SHALL store editorialHtml as a nullable TEXT field
5. THE Problem_Detail_Entity SHALL store timeLimit and memoryLimit as TEXT fields
6. THE Problem_Detail_Entity SHALL store cachedAt as a LONG timestamp field
7. THE Problem_Detail_Dao SHALL provide a method to query problem details by contestId and index
8. THE Problem_Detail_Dao SHALL provide a method to insert or replace problem details

### Requirement 8: Error Handling and Offline Support

**User Story:** As a user, I want the app to work offline with cached data, so that I can browse problems without an internet connection.

#### Acceptance Criteria

1. WHEN no network connection exists and cached problems are available, THE Problems_Repository SHALL emit cached problems without error
2. WHEN no network connection exists and no cached problems are available, THE Problems_Repository SHALL emit an error indicating offline status
3. WHEN Background_Sync fails due to network error, THE Problems_ViewModel SHALL retain the currently displayed cached data
4. WHEN Background_Sync fails due to network error, THE Problems_ViewModel SHALL display a non-intrusive notification (e.g., snackbar) indicating sync failure
5. WHEN network becomes available after being offline, THE Problems_Repository SHALL automatically retry Background_Sync
6. WHEN cached problem detail exists and network is unavailable, THE Problem_Detail_ViewModel SHALL display the cached content
7. WHEN no cached problem detail exists and network is unavailable, THE Problem_Detail_ViewModel SHALL display an offline error message

### Requirement 9: Performance Metrics and Validation

**User Story:** As a developer, I want to measure actual performance improvements, so that I can validate the optimization meets the goals.

#### Acceptance Criteria

1. WHEN measuring problems list load time, THE Problems_ViewModel SHALL log the duration from screen open to first content display
2. WHEN loading from cache, THE Problems_ViewModel SHALL achieve first content display within 500 milliseconds
3. WHEN measuring problem detail load time, THE Problem_Detail_ViewModel SHALL log the duration from screen open to content display
4. WHEN loading problem details from cache, THE Problem_Detail_ViewModel SHALL achieve content display within 1 second
5. WHEN measuring filter operations, THE Filter_Engine SHALL complete filtering within 100 milliseconds for lists up to 10,000 problems
6. WHEN measuring search debounce, THE Search_Debouncer SHALL prevent filter execution until 300 milliseconds after the last keystroke
7. THE Problems_ViewModel SHALL log cache hit rate (percentage of loads served from cache) for monitoring

### Requirement 10: Migration and Backwards Compatibility

**User Story:** As a developer, I want database migrations to handle the new schema, so that existing users don't lose data when upgrading.

#### Acceptance Criteria

1. WHEN the app updates with the new schema, THE CodeforcesDatabase SHALL execute a migration that creates the problem_details table
2. THE migration SHALL preserve all existing data in cached_problems, cached_users, and cached_contests tables
3. WHEN the migration completes, THE CodeforcesDatabase SHALL have a version number incremented by 1
4. IF the migration fails, THE CodeforcesDatabase SHALL fall back to destructive migration and notify the user
5. THE Problem_Detail_Dao SHALL be added to the CodeforcesDatabase @Database annotation
6. THE Problem_Detail_Entity SHALL be added to the CodeforcesDatabase entities list
