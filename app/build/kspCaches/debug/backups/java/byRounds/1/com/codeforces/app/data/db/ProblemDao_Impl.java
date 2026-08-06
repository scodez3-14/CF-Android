package com.codeforces.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProblemDao_Impl implements ProblemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedProblemEntity> __insertionAdapterOfCachedProblemEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public ProblemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedProblemEntity = new EntityInsertionAdapter<CachedProblemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_problems` (`id`,`contestId`,`index`,`name`,`rating`,`tags`,`solvedCount`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedProblemEntity entity) {
        statement.bindString(1, entity.getId());
        if (entity.getContestId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getContestId());
        }
        statement.bindString(3, entity.getIndex());
        statement.bindString(4, entity.getName());
        if (entity.getRating() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getRating());
        }
        final String _tmp = __converters.fromStringList(entity.getTags());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getSolvedCount());
        statement.bindLong(8, entity.getCachedAt());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_problems";
        return _query;
      }
    };
  }

  @Override
  public Object insertProblems(final List<CachedProblemEntity> problems,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedProblemEntity.insert(problems);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CachedProblemEntity>> getAllProblems() {
    final String _sql = "SELECT * FROM cached_problems ORDER BY rating ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_problems"}, new Callable<List<CachedProblemEntity>>() {
      @Override
      @NonNull
      public List<CachedProblemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContestId = CursorUtil.getColumnIndexOrThrow(_cursor, "contestId");
          final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfSolvedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "solvedCount");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<CachedProblemEntity> _result = new ArrayList<CachedProblemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedProblemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final Integer _tmpContestId;
            if (_cursor.isNull(_cursorIndexOfContestId)) {
              _tmpContestId = null;
            } else {
              _tmpContestId = _cursor.getInt(_cursorIndexOfContestId);
            }
            final String _tmpIndex;
            _tmpIndex = _cursor.getString(_cursorIndexOfIndex);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Integer _tmpRating;
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null;
            } else {
              _tmpRating = _cursor.getInt(_cursorIndexOfRating);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp);
            final int _tmpSolvedCount;
            _tmpSolvedCount = _cursor.getInt(_cursorIndexOfSolvedCount);
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new CachedProblemEntity(_tmpId,_tmpContestId,_tmpIndex,_tmpName,_tmpRating,_tmpTags,_tmpSolvedCount,_tmpCachedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllProblemsOnce(
      final Continuation<? super List<CachedProblemEntity>> $completion) {
    final String _sql = "SELECT * FROM cached_problems ORDER BY rating ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedProblemEntity>>() {
      @Override
      @NonNull
      public List<CachedProblemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContestId = CursorUtil.getColumnIndexOrThrow(_cursor, "contestId");
          final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfSolvedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "solvedCount");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<CachedProblemEntity> _result = new ArrayList<CachedProblemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedProblemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final Integer _tmpContestId;
            if (_cursor.isNull(_cursorIndexOfContestId)) {
              _tmpContestId = null;
            } else {
              _tmpContestId = _cursor.getInt(_cursorIndexOfContestId);
            }
            final String _tmpIndex;
            _tmpIndex = _cursor.getString(_cursorIndexOfIndex);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Integer _tmpRating;
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null;
            } else {
              _tmpRating = _cursor.getInt(_cursorIndexOfRating);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp);
            final int _tmpSolvedCount;
            _tmpSolvedCount = _cursor.getInt(_cursorIndexOfSolvedCount);
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new CachedProblemEntity(_tmpId,_tmpContestId,_tmpIndex,_tmpName,_tmpRating,_tmpTags,_tmpSolvedCount,_tmpCachedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getProblemAtOffset(final int offset,
      final Continuation<? super CachedProblemEntity> $completion) {
    final String _sql = "SELECT * FROM cached_problems ORDER BY id LIMIT 1 OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedProblemEntity>() {
      @Override
      @Nullable
      public CachedProblemEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContestId = CursorUtil.getColumnIndexOrThrow(_cursor, "contestId");
          final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfSolvedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "solvedCount");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final CachedProblemEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final Integer _tmpContestId;
            if (_cursor.isNull(_cursorIndexOfContestId)) {
              _tmpContestId = null;
            } else {
              _tmpContestId = _cursor.getInt(_cursorIndexOfContestId);
            }
            final String _tmpIndex;
            _tmpIndex = _cursor.getString(_cursorIndexOfIndex);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Integer _tmpRating;
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null;
            } else {
              _tmpRating = _cursor.getInt(_cursorIndexOfRating);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp);
            final int _tmpSolvedCount;
            _tmpSolvedCount = _cursor.getInt(_cursorIndexOfSolvedCount);
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _result = new CachedProblemEntity(_tmpId,_tmpContestId,_tmpIndex,_tmpName,_tmpRating,_tmpTags,_tmpSolvedCount,_tmpCachedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CachedProblemEntity>> getProblemsByRating(final int minRating,
      final int maxRating) {
    final String _sql = "SELECT * FROM cached_problems WHERE rating BETWEEN ? AND ? ORDER BY rating ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, minRating);
    _argIndex = 2;
    _statement.bindLong(_argIndex, maxRating);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_problems"}, new Callable<List<CachedProblemEntity>>() {
      @Override
      @NonNull
      public List<CachedProblemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContestId = CursorUtil.getColumnIndexOrThrow(_cursor, "contestId");
          final int _cursorIndexOfIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "index");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfSolvedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "solvedCount");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<CachedProblemEntity> _result = new ArrayList<CachedProblemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedProblemEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final Integer _tmpContestId;
            if (_cursor.isNull(_cursorIndexOfContestId)) {
              _tmpContestId = null;
            } else {
              _tmpContestId = _cursor.getInt(_cursorIndexOfContestId);
            }
            final String _tmpIndex;
            _tmpIndex = _cursor.getString(_cursorIndexOfIndex);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Integer _tmpRating;
            if (_cursor.isNull(_cursorIndexOfRating)) {
              _tmpRating = null;
            } else {
              _tmpRating = _cursor.getInt(_cursorIndexOfRating);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.toStringList(_tmp);
            final int _tmpSolvedCount;
            _tmpSolvedCount = _cursor.getInt(_cursorIndexOfSolvedCount);
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new CachedProblemEntity(_tmpId,_tmpContestId,_tmpIndex,_tmpName,_tmpRating,_tmpTags,_tmpSolvedCount,_tmpCachedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM cached_problems";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
