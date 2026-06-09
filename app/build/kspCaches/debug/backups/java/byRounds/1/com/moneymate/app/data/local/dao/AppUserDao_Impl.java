package com.moneymate.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.moneymate.app.data.local.entity.AppUser;
import com.moneymate.app.data.local.entity.UserRole;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
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
public final class AppUserDao_Impl implements AppUserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppUser> __insertionAdapterOfAppUser;

  private final EntityDeletionOrUpdateAdapter<AppUser> __updateAdapterOfAppUser;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  private final SharedSQLiteStatement __preparedStmtOfRestore;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastLogin;

  public AppUserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppUser = new EntityInsertionAdapter<AppUser>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_users` (`id`,`email`,`displayName`,`role`,`assignedFileIds`,`isActive`,`createdAt`,`createdByUserId`,`pinHash`,`lastLoginAt`,`isDeleted`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppUser entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        statement.bindString(3, entity.getDisplayName());
        statement.bindString(4, __UserRole_enumToString(entity.getRole()));
        statement.bindString(5, entity.getAssignedFileIds());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getCreatedByUserId());
        if (entity.getPinHash() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getPinHash());
        }
        if (entity.getLastLoginAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getLastLoginAt());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
      }
    };
    this.__updateAdapterOfAppUser = new EntityDeletionOrUpdateAdapter<AppUser>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `app_users` SET `id` = ?,`email` = ?,`displayName` = ?,`role` = ?,`assignedFileIds` = ?,`isActive` = ?,`createdAt` = ?,`createdByUserId` = ?,`pinHash` = ?,`lastLoginAt` = ?,`isDeleted` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppUser entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getEmail());
        statement.bindString(3, entity.getDisplayName());
        statement.bindString(4, __UserRole_enumToString(entity.getRole()));
        statement.bindString(5, entity.getAssignedFileIds());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getCreatedByUserId());
        if (entity.getPinHash() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getPinHash());
        }
        if (entity.getLastLoginAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getLastLoginAt());
        }
        final int _tmp_1 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_users SET isDeleted = 1, isActive = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRestore = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_users SET isActive = 1, isDeleted = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLastLogin = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_users SET lastLoginAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AppUser user, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAppUser.insertAndReturnId(user);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final AppUser user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAppUser.handle(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDelete(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfSoftDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object restore(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRestore.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfRestore.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastLogin(final long id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastLogin.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateLastLogin.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AppUser>> getAllActiveUsers() {
    final String _sql = "SELECT * FROM app_users WHERE isDeleted = 0 AND isActive = 1 ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_users"}, new Callable<List<AppUser>>() {
      @Override
      @NonNull
      public List<AppUser> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfAssignedFileIds = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedFileIds");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final List<AppUser> _result = new ArrayList<AppUser>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppUser _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final UserRole _tmpRole;
            _tmpRole = __UserRole_stringToEnum(_cursor.getString(_cursorIndexOfRole));
            final String _tmpAssignedFileIds;
            _tmpAssignedFileIds = _cursor.getString(_cursorIndexOfAssignedFileIds);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpCreatedByUserId;
            _tmpCreatedByUserId = _cursor.getLong(_cursorIndexOfCreatedByUserId);
            final String _tmpPinHash;
            if (_cursor.isNull(_cursorIndexOfPinHash)) {
              _tmpPinHash = null;
            } else {
              _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            }
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            _item = new AppUser(_tmpId,_tmpEmail,_tmpDisplayName,_tmpRole,_tmpAssignedFileIds,_tmpIsActive,_tmpCreatedAt,_tmpCreatedByUserId,_tmpPinHash,_tmpLastLoginAt,_tmpIsDeleted);
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
  public Flow<List<AppUser>> getAllInactiveUsers() {
    final String _sql = "SELECT * FROM app_users WHERE (isDeleted = 1 OR isActive = 0) ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_users"}, new Callable<List<AppUser>>() {
      @Override
      @NonNull
      public List<AppUser> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfAssignedFileIds = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedFileIds");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final List<AppUser> _result = new ArrayList<AppUser>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppUser _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final UserRole _tmpRole;
            _tmpRole = __UserRole_stringToEnum(_cursor.getString(_cursorIndexOfRole));
            final String _tmpAssignedFileIds;
            _tmpAssignedFileIds = _cursor.getString(_cursorIndexOfAssignedFileIds);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpCreatedByUserId;
            _tmpCreatedByUserId = _cursor.getLong(_cursorIndexOfCreatedByUserId);
            final String _tmpPinHash;
            if (_cursor.isNull(_cursorIndexOfPinHash)) {
              _tmpPinHash = null;
            } else {
              _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            }
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            _item = new AppUser(_tmpId,_tmpEmail,_tmpDisplayName,_tmpRole,_tmpAssignedFileIds,_tmpIsActive,_tmpCreatedAt,_tmpCreatedByUserId,_tmpPinHash,_tmpLastLoginAt,_tmpIsDeleted);
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
  public Flow<List<AppUser>> getAllUsers() {
    final String _sql = "SELECT * FROM app_users WHERE isDeleted = 0 ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_users"}, new Callable<List<AppUser>>() {
      @Override
      @NonNull
      public List<AppUser> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfAssignedFileIds = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedFileIds");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final List<AppUser> _result = new ArrayList<AppUser>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppUser _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final UserRole _tmpRole;
            _tmpRole = __UserRole_stringToEnum(_cursor.getString(_cursorIndexOfRole));
            final String _tmpAssignedFileIds;
            _tmpAssignedFileIds = _cursor.getString(_cursorIndexOfAssignedFileIds);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpCreatedByUserId;
            _tmpCreatedByUserId = _cursor.getLong(_cursorIndexOfCreatedByUserId);
            final String _tmpPinHash;
            if (_cursor.isNull(_cursorIndexOfPinHash)) {
              _tmpPinHash = null;
            } else {
              _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            }
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            _item = new AppUser(_tmpId,_tmpEmail,_tmpDisplayName,_tmpRole,_tmpAssignedFileIds,_tmpIsActive,_tmpCreatedAt,_tmpCreatedByUserId,_tmpPinHash,_tmpLastLoginAt,_tmpIsDeleted);
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
  public Object getAllActiveUsersOnce(final Continuation<? super List<AppUser>> $completion) {
    final String _sql = "SELECT * FROM app_users WHERE isDeleted = 0 AND isActive = 1 ORDER BY displayName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppUser>>() {
      @Override
      @NonNull
      public List<AppUser> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfAssignedFileIds = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedFileIds");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final List<AppUser> _result = new ArrayList<AppUser>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppUser _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final UserRole _tmpRole;
            _tmpRole = __UserRole_stringToEnum(_cursor.getString(_cursorIndexOfRole));
            final String _tmpAssignedFileIds;
            _tmpAssignedFileIds = _cursor.getString(_cursorIndexOfAssignedFileIds);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpCreatedByUserId;
            _tmpCreatedByUserId = _cursor.getLong(_cursorIndexOfCreatedByUserId);
            final String _tmpPinHash;
            if (_cursor.isNull(_cursorIndexOfPinHash)) {
              _tmpPinHash = null;
            } else {
              _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            }
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            _item = new AppUser(_tmpId,_tmpEmail,_tmpDisplayName,_tmpRole,_tmpAssignedFileIds,_tmpIsActive,_tmpCreatedAt,_tmpCreatedByUserId,_tmpPinHash,_tmpLastLoginAt,_tmpIsDeleted);
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
  public Object getUserByEmail(final String email,
      final Continuation<? super AppUser> $completion) {
    final String _sql = "SELECT * FROM app_users WHERE email = ? AND isDeleted = 0 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, email);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppUser>() {
      @Override
      @Nullable
      public AppUser call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfAssignedFileIds = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedFileIds");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final AppUser _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final UserRole _tmpRole;
            _tmpRole = __UserRole_stringToEnum(_cursor.getString(_cursorIndexOfRole));
            final String _tmpAssignedFileIds;
            _tmpAssignedFileIds = _cursor.getString(_cursorIndexOfAssignedFileIds);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpCreatedByUserId;
            _tmpCreatedByUserId = _cursor.getLong(_cursorIndexOfCreatedByUserId);
            final String _tmpPinHash;
            if (_cursor.isNull(_cursorIndexOfPinHash)) {
              _tmpPinHash = null;
            } else {
              _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            }
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            _result = new AppUser(_tmpId,_tmpEmail,_tmpDisplayName,_tmpRole,_tmpAssignedFileIds,_tmpIsActive,_tmpCreatedAt,_tmpCreatedByUserId,_tmpPinHash,_tmpLastLoginAt,_tmpIsDeleted);
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
  public Object getUserById(final long id, final Continuation<? super AppUser> $completion) {
    final String _sql = "SELECT * FROM app_users WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppUser>() {
      @Override
      @Nullable
      public AppUser call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfAssignedFileIds = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedFileIds");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final AppUser _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final UserRole _tmpRole;
            _tmpRole = __UserRole_stringToEnum(_cursor.getString(_cursorIndexOfRole));
            final String _tmpAssignedFileIds;
            _tmpAssignedFileIds = _cursor.getString(_cursorIndexOfAssignedFileIds);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpCreatedByUserId;
            _tmpCreatedByUserId = _cursor.getLong(_cursorIndexOfCreatedByUserId);
            final String _tmpPinHash;
            if (_cursor.isNull(_cursorIndexOfPinHash)) {
              _tmpPinHash = null;
            } else {
              _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            }
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            _result = new AppUser(_tmpId,_tmpEmail,_tmpDisplayName,_tmpRole,_tmpAssignedFileIds,_tmpIsActive,_tmpCreatedAt,_tmpCreatedByUserId,_tmpPinHash,_tmpLastLoginAt,_tmpIsDeleted);
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
  public Object getActiveUserCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM app_users WHERE isDeleted = 0 AND isActive = 1";
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

  @Override
  public Flow<List<AppUser>> getUsersForFile(final String fileId) {
    final String _sql = "SELECT * FROM app_users WHERE isDeleted = 0 AND isActive = 1 AND (assignedFileIds = '' OR assignedFileIds LIKE '%' || ? || '%')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_users"}, new Callable<List<AppUser>>() {
      @Override
      @NonNull
      public List<AppUser> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfAssignedFileIds = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedFileIds");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfLastLoginAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastLoginAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final List<AppUser> _result = new ArrayList<AppUser>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppUser _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final UserRole _tmpRole;
            _tmpRole = __UserRole_stringToEnum(_cursor.getString(_cursorIndexOfRole));
            final String _tmpAssignedFileIds;
            _tmpAssignedFileIds = _cursor.getString(_cursorIndexOfAssignedFileIds);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpCreatedByUserId;
            _tmpCreatedByUserId = _cursor.getLong(_cursorIndexOfCreatedByUserId);
            final String _tmpPinHash;
            if (_cursor.isNull(_cursorIndexOfPinHash)) {
              _tmpPinHash = null;
            } else {
              _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            }
            final Long _tmpLastLoginAt;
            if (_cursor.isNull(_cursorIndexOfLastLoginAt)) {
              _tmpLastLoginAt = null;
            } else {
              _tmpLastLoginAt = _cursor.getLong(_cursorIndexOfLastLoginAt);
            }
            final boolean _tmpIsDeleted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_1 != 0;
            _item = new AppUser(_tmpId,_tmpEmail,_tmpDisplayName,_tmpRole,_tmpAssignedFileIds,_tmpIsActive,_tmpCreatedAt,_tmpCreatedByUserId,_tmpPinHash,_tmpLastLoginAt,_tmpIsDeleted);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __UserRole_enumToString(@NonNull final UserRole _value) {
    switch (_value) {
      case ADMIN: return "ADMIN";
      case BOSS: return "BOSS";
      case USER: return "USER";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private UserRole __UserRole_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ADMIN": return UserRole.ADMIN;
      case "BOSS": return UserRole.BOSS;
      case "USER": return UserRole.USER;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
