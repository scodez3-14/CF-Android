package com.codeforces.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CodeforcesDatabase_Impl extends CodeforcesDatabase {
  private volatile UserDao _userDao;

  private volatile ProblemDao _problemDao;

  private volatile ContestDao _contestDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_users` (`handle` TEXT NOT NULL, `firstName` TEXT, `lastName` TEXT, `country` TEXT, `city` TEXT, `organization` TEXT, `contribution` INTEGER NOT NULL, `rank` TEXT, `rating` INTEGER NOT NULL, `maxRank` TEXT, `maxRating` INTEGER NOT NULL, `friendOfCount` INTEGER NOT NULL, `avatar` TEXT, `titlePhoto` TEXT, `lastOnlineTimeSeconds` INTEGER NOT NULL, `registrationTimeSeconds` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`handle`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_problems` (`id` TEXT NOT NULL, `contestId` INTEGER, `index` TEXT NOT NULL, `name` TEXT NOT NULL, `rating` INTEGER, `tags` TEXT NOT NULL, `solvedCount` INTEGER NOT NULL, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_contests` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `phase` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `startTimeSeconds` INTEGER, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4cafccbbc65c9cfccc87dc77226a85fd')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `cached_users`");
        db.execSQL("DROP TABLE IF EXISTS `cached_problems`");
        db.execSQL("DROP TABLE IF EXISTS `cached_contests`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCachedUsers = new HashMap<String, TableInfo.Column>(17);
        _columnsCachedUsers.put("handle", new TableInfo.Column("handle", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("firstName", new TableInfo.Column("firstName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("lastName", new TableInfo.Column("lastName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("country", new TableInfo.Column("country", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("city", new TableInfo.Column("city", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("organization", new TableInfo.Column("organization", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("contribution", new TableInfo.Column("contribution", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("rank", new TableInfo.Column("rank", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("rating", new TableInfo.Column("rating", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("maxRank", new TableInfo.Column("maxRank", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("maxRating", new TableInfo.Column("maxRating", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("friendOfCount", new TableInfo.Column("friendOfCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("avatar", new TableInfo.Column("avatar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("titlePhoto", new TableInfo.Column("titlePhoto", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("lastOnlineTimeSeconds", new TableInfo.Column("lastOnlineTimeSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("registrationTimeSeconds", new TableInfo.Column("registrationTimeSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedUsers = new TableInfo("cached_users", _columnsCachedUsers, _foreignKeysCachedUsers, _indicesCachedUsers);
        final TableInfo _existingCachedUsers = TableInfo.read(db, "cached_users");
        if (!_infoCachedUsers.equals(_existingCachedUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_users(com.codeforces.app.data.db.CachedUserEntity).\n"
                  + " Expected:\n" + _infoCachedUsers + "\n"
                  + " Found:\n" + _existingCachedUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedProblems = new HashMap<String, TableInfo.Column>(8);
        _columnsCachedProblems.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedProblems.put("contestId", new TableInfo.Column("contestId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedProblems.put("index", new TableInfo.Column("index", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedProblems.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedProblems.put("rating", new TableInfo.Column("rating", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedProblems.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedProblems.put("solvedCount", new TableInfo.Column("solvedCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedProblems.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedProblems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedProblems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedProblems = new TableInfo("cached_problems", _columnsCachedProblems, _foreignKeysCachedProblems, _indicesCachedProblems);
        final TableInfo _existingCachedProblems = TableInfo.read(db, "cached_problems");
        if (!_infoCachedProblems.equals(_existingCachedProblems)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_problems(com.codeforces.app.data.db.CachedProblemEntity).\n"
                  + " Expected:\n" + _infoCachedProblems + "\n"
                  + " Found:\n" + _existingCachedProblems);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedContests = new HashMap<String, TableInfo.Column>(7);
        _columnsCachedContests.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedContests.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedContests.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedContests.put("phase", new TableInfo.Column("phase", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedContests.put("durationSeconds", new TableInfo.Column("durationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedContests.put("startTimeSeconds", new TableInfo.Column("startTimeSeconds", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedContests.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedContests = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedContests = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedContests = new TableInfo("cached_contests", _columnsCachedContests, _foreignKeysCachedContests, _indicesCachedContests);
        final TableInfo _existingCachedContests = TableInfo.read(db, "cached_contests");
        if (!_infoCachedContests.equals(_existingCachedContests)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_contests(com.codeforces.app.data.db.CachedContestEntity).\n"
                  + " Expected:\n" + _infoCachedContests + "\n"
                  + " Found:\n" + _existingCachedContests);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4cafccbbc65c9cfccc87dc77226a85fd", "876691d56267d56fc0f073419685bc3d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "cached_users","cached_problems","cached_contests");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `cached_users`");
      _db.execSQL("DELETE FROM `cached_problems`");
      _db.execSQL("DELETE FROM `cached_contests`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProblemDao.class, ProblemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ContestDao.class, ContestDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public ProblemDao problemDao() {
    if (_problemDao != null) {
      return _problemDao;
    } else {
      synchronized(this) {
        if(_problemDao == null) {
          _problemDao = new ProblemDao_Impl(this);
        }
        return _problemDao;
      }
    }
  }

  @Override
  public ContestDao contestDao() {
    if (_contestDao != null) {
      return _contestDao;
    } else {
      synchronized(this) {
        if(_contestDao == null) {
          _contestDao = new ContestDao_Impl(this);
        }
        return _contestDao;
      }
    }
  }
}
