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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedUserEntity> __insertionAdapterOfCachedUserEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedUserEntity = new EntityInsertionAdapter<CachedUserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_users` (`handle`,`firstName`,`lastName`,`country`,`city`,`organization`,`contribution`,`rank`,`rating`,`maxRank`,`maxRating`,`friendOfCount`,`avatar`,`titlePhoto`,`lastOnlineTimeSeconds`,`registrationTimeSeconds`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedUserEntity entity) {
        statement.bindString(1, entity.getHandle());
        if (entity.getFirstName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getFirstName());
        }
        if (entity.getLastName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getLastName());
        }
        if (entity.getCountry() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCountry());
        }
        if (entity.getCity() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCity());
        }
        if (entity.getOrganization() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getOrganization());
        }
        statement.bindLong(7, entity.getContribution());
        if (entity.getRank() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getRank());
        }
        statement.bindLong(9, entity.getRating());
        if (entity.getMaxRank() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getMaxRank());
        }
        statement.bindLong(11, entity.getMaxRating());
        statement.bindLong(12, entity.getFriendOfCount());
        if (entity.getAvatar() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getAvatar());
        }
        if (entity.getTitlePhoto() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getTitlePhoto());
        }
        statement.bindLong(15, entity.getLastOnlineTimeSeconds());
        statement.bindLong(16, entity.getRegistrationTimeSeconds());
        statement.bindLong(17, entity.getCachedAt());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_users";
        return _query;
      }
    };
  }

  @Override
  public Object insertUser(final CachedUserEntity user,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedUserEntity.insert(user);
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
  public Object getUser(final String handle,
      final Continuation<? super CachedUserEntity> $completion) {
    final String _sql = "SELECT * FROM cached_users WHERE handle = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, handle);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedUserEntity>() {
      @Override
      @Nullable
      public CachedUserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "firstName");
          final int _cursorIndexOfLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "lastName");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfCity = CursorUtil.getColumnIndexOrThrow(_cursor, "city");
          final int _cursorIndexOfOrganization = CursorUtil.getColumnIndexOrThrow(_cursor, "organization");
          final int _cursorIndexOfContribution = CursorUtil.getColumnIndexOrThrow(_cursor, "contribution");
          final int _cursorIndexOfRank = CursorUtil.getColumnIndexOrThrow(_cursor, "rank");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfMaxRank = CursorUtil.getColumnIndexOrThrow(_cursor, "maxRank");
          final int _cursorIndexOfMaxRating = CursorUtil.getColumnIndexOrThrow(_cursor, "maxRating");
          final int _cursorIndexOfFriendOfCount = CursorUtil.getColumnIndexOrThrow(_cursor, "friendOfCount");
          final int _cursorIndexOfAvatar = CursorUtil.getColumnIndexOrThrow(_cursor, "avatar");
          final int _cursorIndexOfTitlePhoto = CursorUtil.getColumnIndexOrThrow(_cursor, "titlePhoto");
          final int _cursorIndexOfLastOnlineTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOnlineTimeSeconds");
          final int _cursorIndexOfRegistrationTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "registrationTimeSeconds");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final CachedUserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final String _tmpFirstName;
            if (_cursor.isNull(_cursorIndexOfFirstName)) {
              _tmpFirstName = null;
            } else {
              _tmpFirstName = _cursor.getString(_cursorIndexOfFirstName);
            }
            final String _tmpLastName;
            if (_cursor.isNull(_cursorIndexOfLastName)) {
              _tmpLastName = null;
            } else {
              _tmpLastName = _cursor.getString(_cursorIndexOfLastName);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCity;
            if (_cursor.isNull(_cursorIndexOfCity)) {
              _tmpCity = null;
            } else {
              _tmpCity = _cursor.getString(_cursorIndexOfCity);
            }
            final String _tmpOrganization;
            if (_cursor.isNull(_cursorIndexOfOrganization)) {
              _tmpOrganization = null;
            } else {
              _tmpOrganization = _cursor.getString(_cursorIndexOfOrganization);
            }
            final int _tmpContribution;
            _tmpContribution = _cursor.getInt(_cursorIndexOfContribution);
            final String _tmpRank;
            if (_cursor.isNull(_cursorIndexOfRank)) {
              _tmpRank = null;
            } else {
              _tmpRank = _cursor.getString(_cursorIndexOfRank);
            }
            final int _tmpRating;
            _tmpRating = _cursor.getInt(_cursorIndexOfRating);
            final String _tmpMaxRank;
            if (_cursor.isNull(_cursorIndexOfMaxRank)) {
              _tmpMaxRank = null;
            } else {
              _tmpMaxRank = _cursor.getString(_cursorIndexOfMaxRank);
            }
            final int _tmpMaxRating;
            _tmpMaxRating = _cursor.getInt(_cursorIndexOfMaxRating);
            final int _tmpFriendOfCount;
            _tmpFriendOfCount = _cursor.getInt(_cursorIndexOfFriendOfCount);
            final String _tmpAvatar;
            if (_cursor.isNull(_cursorIndexOfAvatar)) {
              _tmpAvatar = null;
            } else {
              _tmpAvatar = _cursor.getString(_cursorIndexOfAvatar);
            }
            final String _tmpTitlePhoto;
            if (_cursor.isNull(_cursorIndexOfTitlePhoto)) {
              _tmpTitlePhoto = null;
            } else {
              _tmpTitlePhoto = _cursor.getString(_cursorIndexOfTitlePhoto);
            }
            final long _tmpLastOnlineTimeSeconds;
            _tmpLastOnlineTimeSeconds = _cursor.getLong(_cursorIndexOfLastOnlineTimeSeconds);
            final long _tmpRegistrationTimeSeconds;
            _tmpRegistrationTimeSeconds = _cursor.getLong(_cursorIndexOfRegistrationTimeSeconds);
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _result = new CachedUserEntity(_tmpHandle,_tmpFirstName,_tmpLastName,_tmpCountry,_tmpCity,_tmpOrganization,_tmpContribution,_tmpRank,_tmpRating,_tmpMaxRank,_tmpMaxRating,_tmpFriendOfCount,_tmpAvatar,_tmpTitlePhoto,_tmpLastOnlineTimeSeconds,_tmpRegistrationTimeSeconds,_tmpCachedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
