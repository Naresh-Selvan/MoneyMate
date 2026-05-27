package com.moneymate.app.data.local.dao;

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
import com.moneymate.app.data.local.entity.EditPermissionScope;
import com.moneymate.app.data.local.entity.EditRequest;
import com.moneymate.app.data.local.entity.RecordType;
import com.moneymate.app.data.local.entity.RequestStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
public final class EditRequestDao_Impl implements EditRequestDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EditRequest> __insertionAdapterOfEditRequest;

  private final SharedSQLiteStatement __preparedStmtOfUpdateRequestStatus;

  private final SharedSQLiteStatement __preparedStmtOfSetFirestoreId;

  private final SharedSQLiteStatement __preparedStmtOfSetPermissionScope;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRequest;

  public EditRequestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEditRequest = new EntityInsertionAdapter<EditRequest>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `edit_requests` (`id`,`recordId`,`recordType`,`requestedAt`,`status`,`resolvedAt`,`scope`,`firestoreRequestId`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EditRequest entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getRecordId());
        statement.bindString(3, __RecordType_enumToString(entity.getRecordType()));
        statement.bindLong(4, entity.getRequestedAt());
        statement.bindString(5, __RequestStatus_enumToString(entity.getStatus()));
        if (entity.getResolvedAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getResolvedAt());
        }
        statement.bindString(7, __EditPermissionScope_enumToString(entity.getScope()));
        if (entity.getFirestoreRequestId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getFirestoreRequestId());
        }
      }
    };
    this.__preparedStmtOfUpdateRequestStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE edit_requests SET status = ?, resolvedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetFirestoreId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE edit_requests SET firestoreRequestId = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetPermissionScope = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE edit_requests SET scope = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteRequest = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM edit_requests WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRequest(final EditRequest request,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEditRequest.insert(request);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRequestStatus(final String id, final RequestStatus status,
      final long resolvedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateRequestStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __RequestStatus_enumToString(status));
        _argIndex = 2;
        _stmt.bindLong(_argIndex, resolvedAt);
        _argIndex = 3;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateRequestStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setFirestoreId(final String id, final String firestoreId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetFirestoreId.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, firestoreId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSetFirestoreId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setPermissionScope(final String id, final EditPermissionScope scope,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetPermissionScope.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __EditPermissionScope_enumToString(scope));
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSetPermissionScope.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRequest(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRequest.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteRequest.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EditRequest>> getAllRequests() {
    final String _sql = "SELECT * FROM edit_requests ORDER BY requestedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"edit_requests"}, new Callable<List<EditRequest>>() {
      @Override
      @NonNull
      public List<EditRequest> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfResolvedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "resolvedAt");
          final int _cursorIndexOfScope = CursorUtil.getColumnIndexOrThrow(_cursor, "scope");
          final int _cursorIndexOfFirestoreRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "firestoreRequestId");
          final List<EditRequest> _result = new ArrayList<EditRequest>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EditRequest _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpRecordId;
            _tmpRecordId = _cursor.getString(_cursorIndexOfRecordId);
            final RecordType _tmpRecordType;
            _tmpRecordType = __RecordType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final RequestStatus _tmpStatus;
            _tmpStatus = __RequestStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpResolvedAt;
            if (_cursor.isNull(_cursorIndexOfResolvedAt)) {
              _tmpResolvedAt = null;
            } else {
              _tmpResolvedAt = _cursor.getLong(_cursorIndexOfResolvedAt);
            }
            final EditPermissionScope _tmpScope;
            _tmpScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfScope));
            final String _tmpFirestoreRequestId;
            if (_cursor.isNull(_cursorIndexOfFirestoreRequestId)) {
              _tmpFirestoreRequestId = null;
            } else {
              _tmpFirestoreRequestId = _cursor.getString(_cursorIndexOfFirestoreRequestId);
            }
            _item = new EditRequest(_tmpId,_tmpRecordId,_tmpRecordType,_tmpRequestedAt,_tmpStatus,_tmpResolvedAt,_tmpScope,_tmpFirestoreRequestId);
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
  public Flow<List<EditRequest>> getPendingRequests() {
    final String _sql = "SELECT * FROM edit_requests WHERE status = 'PENDING' ORDER BY requestedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"edit_requests"}, new Callable<List<EditRequest>>() {
      @Override
      @NonNull
      public List<EditRequest> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfResolvedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "resolvedAt");
          final int _cursorIndexOfScope = CursorUtil.getColumnIndexOrThrow(_cursor, "scope");
          final int _cursorIndexOfFirestoreRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "firestoreRequestId");
          final List<EditRequest> _result = new ArrayList<EditRequest>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EditRequest _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpRecordId;
            _tmpRecordId = _cursor.getString(_cursorIndexOfRecordId);
            final RecordType _tmpRecordType;
            _tmpRecordType = __RecordType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final RequestStatus _tmpStatus;
            _tmpStatus = __RequestStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpResolvedAt;
            if (_cursor.isNull(_cursorIndexOfResolvedAt)) {
              _tmpResolvedAt = null;
            } else {
              _tmpResolvedAt = _cursor.getLong(_cursorIndexOfResolvedAt);
            }
            final EditPermissionScope _tmpScope;
            _tmpScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfScope));
            final String _tmpFirestoreRequestId;
            if (_cursor.isNull(_cursorIndexOfFirestoreRequestId)) {
              _tmpFirestoreRequestId = null;
            } else {
              _tmpFirestoreRequestId = _cursor.getString(_cursorIndexOfFirestoreRequestId);
            }
            _item = new EditRequest(_tmpId,_tmpRecordId,_tmpRecordType,_tmpRequestedAt,_tmpStatus,_tmpResolvedAt,_tmpScope,_tmpFirestoreRequestId);
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
  public Object getLatestRequestForRecord(final String recordId, final String recordType,
      final Continuation<? super EditRequest> $completion) {
    final String _sql = "SELECT * FROM edit_requests WHERE recordId = ? AND recordType = ? ORDER BY requestedAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, recordId);
    _argIndex = 2;
    _statement.bindString(_argIndex, recordType);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EditRequest>() {
      @Override
      @Nullable
      public EditRequest call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfResolvedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "resolvedAt");
          final int _cursorIndexOfScope = CursorUtil.getColumnIndexOrThrow(_cursor, "scope");
          final int _cursorIndexOfFirestoreRequestId = CursorUtil.getColumnIndexOrThrow(_cursor, "firestoreRequestId");
          final EditRequest _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpRecordId;
            _tmpRecordId = _cursor.getString(_cursorIndexOfRecordId);
            final RecordType _tmpRecordType;
            _tmpRecordType = __RecordType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final RequestStatus _tmpStatus;
            _tmpStatus = __RequestStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpResolvedAt;
            if (_cursor.isNull(_cursorIndexOfResolvedAt)) {
              _tmpResolvedAt = null;
            } else {
              _tmpResolvedAt = _cursor.getLong(_cursorIndexOfResolvedAt);
            }
            final EditPermissionScope _tmpScope;
            _tmpScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfScope));
            final String _tmpFirestoreRequestId;
            if (_cursor.isNull(_cursorIndexOfFirestoreRequestId)) {
              _tmpFirestoreRequestId = null;
            } else {
              _tmpFirestoreRequestId = _cursor.getString(_cursorIndexOfFirestoreRequestId);
            }
            _result = new EditRequest(_tmpId,_tmpRecordId,_tmpRecordType,_tmpRequestedAt,_tmpStatus,_tmpResolvedAt,_tmpScope,_tmpFirestoreRequestId);
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

  private String __RecordType_enumToString(@NonNull final RecordType _value) {
    switch (_value) {
      case PERSON: return "PERSON";
      case PAYMENT: return "PAYMENT";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __RequestStatus_enumToString(@NonNull final RequestStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case APPROVED: return "APPROVED";
      case DENIED: return "DENIED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __EditPermissionScope_enumToString(@NonNull final EditPermissionScope _value) {
    switch (_value) {
      case NONE: return "NONE";
      case THIS_RECORD: return "THIS_RECORD";
      case ALL_LOCKED: return "ALL_LOCKED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private RecordType __RecordType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PERSON": return RecordType.PERSON;
      case "PAYMENT": return RecordType.PAYMENT;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private RequestStatus __RequestStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return RequestStatus.PENDING;
      case "APPROVED": return RequestStatus.APPROVED;
      case "DENIED": return RequestStatus.DENIED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private EditPermissionScope __EditPermissionScope_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "NONE": return EditPermissionScope.NONE;
      case "THIS_RECORD": return EditPermissionScope.THIS_RECORD;
      case "ALL_LOCKED": return EditPermissionScope.ALL_LOCKED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
