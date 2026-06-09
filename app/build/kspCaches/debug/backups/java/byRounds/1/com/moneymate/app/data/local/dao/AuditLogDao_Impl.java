package com.moneymate.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.moneymate.app.data.local.entity.AuditAction;
import com.moneymate.app.data.local.entity.AuditLog;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class AuditLogDao_Impl implements AuditLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AuditLog> __insertionAdapterOfAuditLog;

  private final SharedSQLiteStatement __preparedStmtOfPruneOlderThan;

  public AuditLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAuditLog = new EntityInsertionAdapter<AuditLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `audit_logs` (`id`,`userId`,`userEmail`,`action`,`targetType`,`targetId`,`targetLabel`,`details`,`timestamp`,`fileId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AuditLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getUserEmail());
        statement.bindString(4, __AuditAction_enumToString(entity.getAction()));
        statement.bindString(5, entity.getTargetType());
        statement.bindString(6, entity.getTargetId());
        statement.bindString(7, entity.getTargetLabel());
        if (entity.getDetails() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDetails());
        }
        statement.bindLong(9, entity.getTimestamp());
        if (entity.getFileId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getFileId());
        }
      }
    };
    this.__preparedStmtOfPruneOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM audit_logs WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AuditLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAuditLog.insert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object pruneOlderThan(final long timestamp, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPruneOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
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
          __preparedStmtOfPruneOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AuditLog>> getAllLogs(final int limit) {
    final String _sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      @NonNull
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfTargetLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLabel");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final AuditAction _tmpAction;
            _tmpAction = __AuditAction_stringToEnum(_cursor.getString(_cursorIndexOfAction));
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final String _tmpTargetLabel;
            _tmpTargetLabel = _cursor.getString(_cursorIndexOfTargetLabel);
            final String _tmpDetails;
            if (_cursor.isNull(_cursorIndexOfDetails)) {
              _tmpDetails = null;
            } else {
              _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFileId;
            if (_cursor.isNull(_cursorIndexOfFileId)) {
              _tmpFileId = null;
            } else {
              _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            }
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpUserEmail,_tmpAction,_tmpTargetType,_tmpTargetId,_tmpTargetLabel,_tmpDetails,_tmpTimestamp,_tmpFileId);
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
  public Flow<List<AuditLog>> getLogsForFile(final String fileId, final int limit) {
    final String _sql = "SELECT * FROM audit_logs WHERE fileId = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      @NonNull
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfTargetLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLabel");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final AuditAction _tmpAction;
            _tmpAction = __AuditAction_stringToEnum(_cursor.getString(_cursorIndexOfAction));
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final String _tmpTargetLabel;
            _tmpTargetLabel = _cursor.getString(_cursorIndexOfTargetLabel);
            final String _tmpDetails;
            if (_cursor.isNull(_cursorIndexOfDetails)) {
              _tmpDetails = null;
            } else {
              _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFileId;
            if (_cursor.isNull(_cursorIndexOfFileId)) {
              _tmpFileId = null;
            } else {
              _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            }
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpUserEmail,_tmpAction,_tmpTargetType,_tmpTargetId,_tmpTargetLabel,_tmpDetails,_tmpTimestamp,_tmpFileId);
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
  public Flow<List<AuditLog>> getLogsForUser(final long userId) {
    final String _sql = "SELECT * FROM audit_logs WHERE userId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      @NonNull
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfTargetLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLabel");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final AuditAction _tmpAction;
            _tmpAction = __AuditAction_stringToEnum(_cursor.getString(_cursorIndexOfAction));
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final String _tmpTargetLabel;
            _tmpTargetLabel = _cursor.getString(_cursorIndexOfTargetLabel);
            final String _tmpDetails;
            if (_cursor.isNull(_cursorIndexOfDetails)) {
              _tmpDetails = null;
            } else {
              _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFileId;
            if (_cursor.isNull(_cursorIndexOfFileId)) {
              _tmpFileId = null;
            } else {
              _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            }
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpUserEmail,_tmpAction,_tmpTargetType,_tmpTargetId,_tmpTargetLabel,_tmpDetails,_tmpTimestamp,_tmpFileId);
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
  public Flow<List<AuditLog>> getLogsForTarget(final String targetType, final String targetId) {
    final String _sql = "SELECT * FROM audit_logs WHERE targetType = ? AND targetId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, targetType);
    _argIndex = 2;
    _statement.bindString(_argIndex, targetId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      @NonNull
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfTargetLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLabel");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final AuditAction _tmpAction;
            _tmpAction = __AuditAction_stringToEnum(_cursor.getString(_cursorIndexOfAction));
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final String _tmpTargetLabel;
            _tmpTargetLabel = _cursor.getString(_cursorIndexOfTargetLabel);
            final String _tmpDetails;
            if (_cursor.isNull(_cursorIndexOfDetails)) {
              _tmpDetails = null;
            } else {
              _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFileId;
            if (_cursor.isNull(_cursorIndexOfFileId)) {
              _tmpFileId = null;
            } else {
              _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            }
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpUserEmail,_tmpAction,_tmpTargetType,_tmpTargetId,_tmpTargetLabel,_tmpDetails,_tmpTimestamp,_tmpFileId);
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
  public Flow<List<AuditLog>> getRecentLogsForUser(final long userId, final int limit) {
    final String _sql = "SELECT * FROM audit_logs WHERE userId = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      @NonNull
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfTargetLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLabel");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final AuditAction _tmpAction;
            _tmpAction = __AuditAction_stringToEnum(_cursor.getString(_cursorIndexOfAction));
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final String _tmpTargetLabel;
            _tmpTargetLabel = _cursor.getString(_cursorIndexOfTargetLabel);
            final String _tmpDetails;
            if (_cursor.isNull(_cursorIndexOfDetails)) {
              _tmpDetails = null;
            } else {
              _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFileId;
            if (_cursor.isNull(_cursorIndexOfFileId)) {
              _tmpFileId = null;
            } else {
              _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            }
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpUserEmail,_tmpAction,_tmpTargetType,_tmpTargetId,_tmpTargetLabel,_tmpDetails,_tmpTimestamp,_tmpFileId);
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
  public Flow<List<AuditLog>> getLogsByActions(final List<String> actions, final int limit) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM audit_logs WHERE action IN (");
    final int _inputSize = actions.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") ORDER BY timestamp DESC LIMIT ");
    _stringBuilder.append("?");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 1 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : actions) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 1 + _inputSize;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      @NonNull
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfTargetLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLabel");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLog _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final AuditAction _tmpAction;
            _tmpAction = __AuditAction_stringToEnum(_cursor.getString(_cursorIndexOfAction));
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final String _tmpTargetLabel;
            _tmpTargetLabel = _cursor.getString(_cursorIndexOfTargetLabel);
            final String _tmpDetails;
            if (_cursor.isNull(_cursorIndexOfDetails)) {
              _tmpDetails = null;
            } else {
              _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFileId;
            if (_cursor.isNull(_cursorIndexOfFileId)) {
              _tmpFileId = null;
            } else {
              _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            }
            _item_1 = new AuditLog(_tmpId,_tmpUserId,_tmpUserEmail,_tmpAction,_tmpTargetType,_tmpTargetId,_tmpTargetLabel,_tmpDetails,_tmpTimestamp,_tmpFileId);
            _result.add(_item_1);
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
  public Flow<List<AuditLog>> getLogsByDateRange(final long from, final long to, final int limit) {
    final String _sql = "SELECT * FROM audit_logs WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      @NonNull
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfTargetType = CursorUtil.getColumnIndexOrThrow(_cursor, "targetType");
          final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
          final int _cursorIndexOfTargetLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLabel");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AuditLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpUserId;
            _tmpUserId = _cursor.getLong(_cursorIndexOfUserId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final AuditAction _tmpAction;
            _tmpAction = __AuditAction_stringToEnum(_cursor.getString(_cursorIndexOfAction));
            final String _tmpTargetType;
            _tmpTargetType = _cursor.getString(_cursorIndexOfTargetType);
            final String _tmpTargetId;
            _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
            final String _tmpTargetLabel;
            _tmpTargetLabel = _cursor.getString(_cursorIndexOfTargetLabel);
            final String _tmpDetails;
            if (_cursor.isNull(_cursorIndexOfDetails)) {
              _tmpDetails = null;
            } else {
              _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpFileId;
            if (_cursor.isNull(_cursorIndexOfFileId)) {
              _tmpFileId = null;
            } else {
              _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            }
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpUserEmail,_tmpAction,_tmpTargetType,_tmpTargetId,_tmpTargetLabel,_tmpDetails,_tmpTimestamp,_tmpFileId);
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
  public Object countPaymentsByUser(final long userId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM audit_logs WHERE userId = ? AND action = 'ADD_PAYMENT'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
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
  public Object countPersonsAddedByUser(final long userId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM audit_logs WHERE userId = ? AND action = 'ADD_PERSON'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
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

  private String __AuditAction_enumToString(@NonNull final AuditAction _value) {
    switch (_value) {
      case ADD_PERSON: return "ADD_PERSON";
      case EDIT_PERSON: return "EDIT_PERSON";
      case DELETE_PERSON: return "DELETE_PERSON";
      case MOVE_PERSON: return "MOVE_PERSON";
      case ADD_PAYMENT: return "ADD_PAYMENT";
      case EDIT_PAYMENT: return "EDIT_PAYMENT";
      case DELETE_PAYMENT: return "DELETE_PAYMENT";
      case ADD_LOAN: return "ADD_LOAN";
      case EDIT_LOAN: return "EDIT_LOAN";
      case CLOSE_LOAN: return "CLOSE_LOAN";
      case ADD_EXPENSE: return "ADD_EXPENSE";
      case EDIT_EXPENSE: return "EDIT_EXPENSE";
      case DELETE_EXPENSE: return "DELETE_EXPENSE";
      case ADD_FILE: return "ADD_FILE";
      case RENAME_FILE: return "RENAME_FILE";
      case DELETE_FILE: return "DELETE_FILE";
      case MOVE_FILE: return "MOVE_FILE";
      case ADD_USER: return "ADD_USER";
      case EDIT_USER: return "EDIT_USER";
      case DEACTIVATE_USER: return "DEACTIVATE_USER";
      case CHANGE_ROLE: return "CHANGE_ROLE";
      case EXPORT_REPORT: return "EXPORT_REPORT";
      case LOGIN: return "LOGIN";
      case LOGOUT: return "LOGOUT";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private AuditAction __AuditAction_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ADD_PERSON": return AuditAction.ADD_PERSON;
      case "EDIT_PERSON": return AuditAction.EDIT_PERSON;
      case "DELETE_PERSON": return AuditAction.DELETE_PERSON;
      case "MOVE_PERSON": return AuditAction.MOVE_PERSON;
      case "ADD_PAYMENT": return AuditAction.ADD_PAYMENT;
      case "EDIT_PAYMENT": return AuditAction.EDIT_PAYMENT;
      case "DELETE_PAYMENT": return AuditAction.DELETE_PAYMENT;
      case "ADD_LOAN": return AuditAction.ADD_LOAN;
      case "EDIT_LOAN": return AuditAction.EDIT_LOAN;
      case "CLOSE_LOAN": return AuditAction.CLOSE_LOAN;
      case "ADD_EXPENSE": return AuditAction.ADD_EXPENSE;
      case "EDIT_EXPENSE": return AuditAction.EDIT_EXPENSE;
      case "DELETE_EXPENSE": return AuditAction.DELETE_EXPENSE;
      case "ADD_FILE": return AuditAction.ADD_FILE;
      case "RENAME_FILE": return AuditAction.RENAME_FILE;
      case "DELETE_FILE": return AuditAction.DELETE_FILE;
      case "MOVE_FILE": return AuditAction.MOVE_FILE;
      case "ADD_USER": return AuditAction.ADD_USER;
      case "EDIT_USER": return AuditAction.EDIT_USER;
      case "DEACTIVATE_USER": return AuditAction.DEACTIVATE_USER;
      case "CHANGE_ROLE": return AuditAction.CHANGE_ROLE;
      case "EXPORT_REPORT": return AuditAction.EXPORT_REPORT;
      case "LOGIN": return AuditAction.LOGIN;
      case "LOGOUT": return AuditAction.LOGOUT;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
