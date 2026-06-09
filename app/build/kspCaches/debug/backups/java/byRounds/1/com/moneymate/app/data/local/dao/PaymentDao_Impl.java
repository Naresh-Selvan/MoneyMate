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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.moneymate.app.data.local.entity.EditPermissionScope;
import com.moneymate.app.data.local.entity.Payment;
import com.moneymate.app.data.local.entity.PaymentMode;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
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
public final class PaymentDao_Impl implements PaymentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Payment> __insertionAdapterOfPayment;

  private final EntityDeletionOrUpdateAdapter<Payment> __updateAdapterOfPayment;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeletePayment;

  private final SharedSQLiteStatement __preparedStmtOfRestorePayment;

  private final SharedSQLiteStatement __preparedStmtOfHardDeletePayment;

  private final SharedSQLiteStatement __preparedStmtOfPurgeExpiredPayments;

  private final SharedSQLiteStatement __preparedStmtOfMarkAllUploadedForPerson;

  private final SharedSQLiteStatement __preparedStmtOfSetEditPermission;

  public PaymentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPayment = new EntityInsertionAdapter<Payment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `payments` (`id`,`personId`,`amount`,`mode`,`date`,`isDeleted`,`deletedAt`,`isRollover`,`uploadedAt`,`editPermissionGranted`,`editPermissionScope`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Payment entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPersonId());
        statement.bindDouble(3, entity.getAmount());
        statement.bindString(4, __PaymentMode_enumToString(entity.getMode()));
        statement.bindLong(5, entity.getDate());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getDeletedAt());
        }
        final int _tmp_1 = entity.isRollover() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getUploadedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getUploadedAt());
        }
        final int _tmp_2 = entity.getEditPermissionGranted() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        statement.bindString(11, __EditPermissionScope_enumToString(entity.getEditPermissionScope()));
      }
    };
    this.__updateAdapterOfPayment = new EntityDeletionOrUpdateAdapter<Payment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `payments` SET `id` = ?,`personId` = ?,`amount` = ?,`mode` = ?,`date` = ?,`isDeleted` = ?,`deletedAt` = ?,`isRollover` = ?,`uploadedAt` = ?,`editPermissionGranted` = ?,`editPermissionScope` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Payment entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPersonId());
        statement.bindDouble(3, entity.getAmount());
        statement.bindString(4, __PaymentMode_enumToString(entity.getMode()));
        statement.bindLong(5, entity.getDate());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getDeletedAt());
        }
        final int _tmp_1 = entity.isRollover() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getUploadedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getUploadedAt());
        }
        final int _tmp_2 = entity.getEditPermissionGranted() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        statement.bindString(11, __EditPermissionScope_enumToString(entity.getEditPermissionScope()));
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDeletePayment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE payments SET isDeleted = 1, deletedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRestorePayment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE payments SET isDeleted = 0, deletedAt = NULL WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfHardDeletePayment = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM payments WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfPurgeExpiredPayments = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM payments WHERE isDeleted = 1 AND deletedAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAllUploadedForPerson = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE payments SET uploadedAt = ? WHERE personId = ? AND isDeleted = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSetEditPermission = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE payments SET editPermissionGranted = ?, editPermissionScope = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPayment(final Payment payment, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPayment.insert(payment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePayment(final Payment payment, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPayment.handle(payment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeletePayment(final String id, final long deletedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeletePayment.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, deletedAt);
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
          __preparedStmtOfSoftDeletePayment.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object restorePayment(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRestorePayment.acquire();
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
          __preparedStmtOfRestorePayment.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object hardDeletePayment(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfHardDeletePayment.acquire();
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
          __preparedStmtOfHardDeletePayment.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object purgeExpiredPayments(final long cutoff,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeExpiredPayments.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cutoff);
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
          __preparedStmtOfPurgeExpiredPayments.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAllUploadedForPerson(final String personId, final long uploadedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAllUploadedForPerson.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, uploadedAt);
        _argIndex = 2;
        _stmt.bindString(_argIndex, personId);
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
          __preparedStmtOfMarkAllUploadedForPerson.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setEditPermission(final String id, final boolean granted,
      final EditPermissionScope scope, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetEditPermission.acquire();
        int _argIndex = 1;
        final int _tmp = granted ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, __EditPermissionScope_enumToString(scope));
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
          __preparedStmtOfSetEditPermission.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalPaidByPersonIds(final List<String> personIds,
      final Continuation<? super List<PersonTotalPaid>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT personId, SUM(amount) as totalPaid FROM payments WHERE personId IN (");
    final int _inputSize = personIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") AND isDeleted = 0 GROUP BY personId");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : personIds) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PersonTotalPaid>>() {
      @Override
      @NonNull
      public List<PersonTotalPaid> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonId = 0;
          final int _cursorIndexOfTotalPaid = 1;
          final List<PersonTotalPaid> _result = new ArrayList<PersonTotalPaid>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonTotalPaid _item_1;
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpTotalPaid;
            _tmpTotalPaid = _cursor.getDouble(_cursorIndexOfTotalPaid);
            _item_1 = new PersonTotalPaid(_tmpPersonId,_tmpTotalPaid);
            _result.add(_item_1);
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
  public Flow<List<Payment>> getPaymentsForPerson(final String personId) {
    final String _sql = "SELECT * FROM payments WHERE personId = ? AND isDeleted = 0 ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments"}, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Flow<List<Payment>> getPaymentsForPersonSortedByMode(final String personId) {
    final String _sql = "SELECT * FROM payments WHERE personId = ? AND isDeleted = 0 ORDER BY mode ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments"}, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Object getAllPaymentsForPerson(final String personId,
      final Continuation<? super List<Payment>> $completion) {
    final String _sql = "SELECT * FROM payments WHERE personId = ? ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Flow<List<Payment>> getPaymentsForPersonSortedByDate(final String personId) {
    final String _sql = "SELECT * FROM payments WHERE personId = ? AND isDeleted = 0 ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments"}, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Object getPaymentById(final String id, final Continuation<? super Payment> $completion) {
    final String _sql = "SELECT * FROM payments WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Payment>() {
      @Override
      @Nullable
      public Payment call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final Payment _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _result = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Flow<List<Payment>> getDeletedPayments() {
    final String _sql = "SELECT * FROM payments WHERE isDeleted = 1 ORDER BY deletedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments"}, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Object getTotalPaidByPerson(final String personId,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(amount) FROM payments WHERE personId = ? AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getTotalPaidCashByPerson(final String personId,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(amount) FROM payments WHERE personId = ? AND isDeleted = 0 AND mode = 'CASH'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getTotalPaidUpiByPerson(final String personId,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(amount) FROM payments WHERE personId = ? AND isDeleted = 0 AND mode = 'UPI'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getTotalReceivedInFile(final String fileId,
      final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT SUM(p.amount) FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getTotalReceivedCashInFile(final String fileId,
      final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT SUM(p.amount) FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND p.mode = 'CASH' AND pr.isDeleted = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getTotalReceivedUpiInFile(final String fileId,
      final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT SUM(p.amount) FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND p.mode = 'UPI' AND pr.isDeleted = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Flow<List<Payment>> getPaymentsForFile(final String fileId) {
    final String _sql = "\n"
            + "        SELECT p.* FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0 AND pr.isCompleted = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Flow<List<Payment>> getPaymentsForFileIncludingCompleted(final String fileId) {
    final String _sql = "\n"
            + "        SELECT p.* FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Object getTotalReceivedToday(final String fileId, final long startOfDay,
      final long endOfDay, final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(p.amount), 0) FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND p.date >= ? AND p.date < ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endOfDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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
  public Object getTotalReceivedThisWeek(final String fileId, final long weekStart,
      final long weekEnd, final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(p.amount), 0) FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND p.date >= ? AND p.date < ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, weekStart);
    _argIndex = 3;
    _statement.bindLong(_argIndex, weekEnd);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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
  public Object getExpiredDeletedPayments(final long cutoff,
      final Continuation<? super List<Payment>> $completion) {
    final String _sql = "SELECT * FROM payments WHERE isDeleted = 1 AND deletedAt < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cutoff);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Payment>>() {
      @Override
      @NonNull
      public List<Payment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfIsRollover = CursorUtil.getColumnIndexOrThrow(_cursor, "isRollover");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final List<Payment> _result = new ArrayList<Payment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Payment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final boolean _tmpIsRollover;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRollover);
            _tmpIsRollover = _tmp_1 != 0;
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_2 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            _item = new Payment(_tmpId,_tmpPersonId,_tmpAmount,_tmpMode,_tmpDate,_tmpIsDeleted,_tmpDeletedAt,_tmpIsRollover,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope);
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
  public Object getLatestPaymentTimestamp(final String personId,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(date) FROM payments WHERE personId = ? AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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
  public Object getTotalPaidTodayByPersonIds(final List<String> personIds, final long startOfDay,
      final long endOfDay, final Continuation<? super List<PersonTotalPaid>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("\n");
    _stringBuilder.append("        SELECT personId, COALESCE(SUM(amount), 0) as totalPaid");
    _stringBuilder.append("\n");
    _stringBuilder.append("        FROM payments");
    _stringBuilder.append("\n");
    _stringBuilder.append("        WHERE personId IN (");
    final int _inputSize = personIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    _stringBuilder.append("\n");
    _stringBuilder.append("          AND isDeleted = 0");
    _stringBuilder.append("\n");
    _stringBuilder.append("          AND date >= ");
    _stringBuilder.append("?");
    _stringBuilder.append(" AND date < ");
    _stringBuilder.append("?");
    _stringBuilder.append("\n");
    _stringBuilder.append("        GROUP BY personId");
    _stringBuilder.append("\n");
    _stringBuilder.append("    ");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 2 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : personIds) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 1 + _inputSize;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 2 + _inputSize;
    _statement.bindLong(_argIndex, endOfDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PersonTotalPaid>>() {
      @Override
      @NonNull
      public List<PersonTotalPaid> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonId = 0;
          final int _cursorIndexOfTotalPaid = 1;
          final List<PersonTotalPaid> _result = new ArrayList<PersonTotalPaid>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonTotalPaid _item_1;
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final double _tmpTotalPaid;
            _tmpTotalPaid = _cursor.getDouble(_cursorIndexOfTotalPaid);
            _item_1 = new PersonTotalPaid(_tmpPersonId,_tmpTotalPaid);
            _result.add(_item_1);
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
  public Object getTotalCollectionToday(final String fileId, final long startOfDay,
      final long endOfDay, final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(p.amount), 0) FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND p.date >= ? AND p.date < ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endOfDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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
  public Flow<List<PlanEntry>> getPlanReport(final String fileId, final long from, final long to) {
    final String _sql = "\n"
            + "        SELECT pr.name as personName, pr.amountGiven as loanAmount, pr.perInstallmentAmount as installmentAmount,\n"
            + "               (SELECT COUNT(*) FROM payments WHERE personId = pr.id AND isDeleted = 0) as paidCount,\n"
            + "               pr.numberOfInstallments as totalInstallments,\n"
            + "               COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0 AND date >= ? AND date <= ?), 0) as collectedToday,\n"
            + "               (COALESCE(pr.totalRepayment, pr.amountGiven) - COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0)) as balance,\n"
            + "               pr.place\n"
            + "        FROM persons pr\n"
            + "        WHERE pr.fileId = ? AND pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0\n"
            + "        ORDER BY pr.sortOrder ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    _argIndex = 3;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<PlanEntry>>() {
      @Override
      @NonNull
      public List<PlanEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonName = 0;
          final int _cursorIndexOfLoanAmount = 1;
          final int _cursorIndexOfInstallmentAmount = 2;
          final int _cursorIndexOfPaidCount = 3;
          final int _cursorIndexOfTotalInstallments = 4;
          final int _cursorIndexOfCollectedToday = 5;
          final int _cursorIndexOfBalance = 6;
          final int _cursorIndexOfPlace = 7;
          final List<PlanEntry> _result = new ArrayList<PlanEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlanEntry _item;
            final String _tmpPersonName;
            _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            final double _tmpLoanAmount;
            _tmpLoanAmount = _cursor.getDouble(_cursorIndexOfLoanAmount);
            final double _tmpInstallmentAmount;
            _tmpInstallmentAmount = _cursor.getDouble(_cursorIndexOfInstallmentAmount);
            final int _tmpPaidCount;
            _tmpPaidCount = _cursor.getInt(_cursorIndexOfPaidCount);
            final int _tmpTotalInstallments;
            _tmpTotalInstallments = _cursor.getInt(_cursorIndexOfTotalInstallments);
            final double _tmpCollectedToday;
            _tmpCollectedToday = _cursor.getDouble(_cursorIndexOfCollectedToday);
            final double _tmpBalance;
            _tmpBalance = _cursor.getDouble(_cursorIndexOfBalance);
            final String _tmpPlace;
            if (_cursor.isNull(_cursorIndexOfPlace)) {
              _tmpPlace = null;
            } else {
              _tmpPlace = _cursor.getString(_cursorIndexOfPlace);
            }
            _item = new PlanEntry(_tmpPersonName,_tmpLoanAmount,_tmpInstallmentAmount,_tmpPaidCount,_tmpTotalInstallments,_tmpCollectedToday,_tmpBalance,_tmpPlace);
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
  public Flow<List<DailySummaryEntry>> getDailySummary(final String fileId, final long from,
      final long to) {
    final String _sql = "\n"
            + "        SELECT pr.name as personName, pr.perInstallmentAmount as installAmount,\n"
            + "               p.amount as paidAmount, p.mode as paymentMode, pr.place\n"
            + "        FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND p.date >= ? AND p.date < ?\n"
            + "        ORDER BY pr.recordType, pr.sortOrder\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, from);
    _argIndex = 3;
    _statement.bindLong(_argIndex, to);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<DailySummaryEntry>>() {
      @Override
      @NonNull
      public List<DailySummaryEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonName = 0;
          final int _cursorIndexOfInstallAmount = 1;
          final int _cursorIndexOfPaidAmount = 2;
          final int _cursorIndexOfPaymentMode = 3;
          final int _cursorIndexOfPlace = 4;
          final List<DailySummaryEntry> _result = new ArrayList<DailySummaryEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailySummaryEntry _item;
            final String _tmpPersonName;
            _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            final double _tmpInstallAmount;
            _tmpInstallAmount = _cursor.getDouble(_cursorIndexOfInstallAmount);
            final double _tmpPaidAmount;
            _tmpPaidAmount = _cursor.getDouble(_cursorIndexOfPaidAmount);
            final String _tmpPaymentMode;
            _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode);
            final String _tmpPlace;
            if (_cursor.isNull(_cursorIndexOfPlace)) {
              _tmpPlace = null;
            } else {
              _tmpPlace = _cursor.getString(_cursorIndexOfPlace);
            }
            _item = new DailySummaryEntry(_tmpPersonName,_tmpInstallAmount,_tmpPaidAmount,_tmpPaymentMode,_tmpPlace);
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
  public Flow<List<LineSummaryEntry>> getLineSummary(final String fileId, final long from,
      final long to) {
    final String _sql = "\n"
            + "        SELECT p.date as date,\n"
            + "               COALESCE(SUM(p.amount), 0) as totalCollected,\n"
            + "               COALESCE(SUM(CASE WHEN p.mode = 'UPI' THEN p.amount ELSE 0 END), 0) as totalOnline,\n"
            + "               COALESCE(SUM(CASE WHEN p.mode = 'CASH' THEN p.amount ELSE 0 END), 0) as totalCash,\n"
            + "               0.0 as totalExpense,\n"
            + "               0.0 as netBalance\n"
            + "        FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND p.date >= ? AND p.date <= ?\n"
            + "        GROUP BY p.date\n"
            + "        ORDER BY p.date ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, from);
    _argIndex = 3;
    _statement.bindLong(_argIndex, to);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<LineSummaryEntry>>() {
      @Override
      @NonNull
      public List<LineSummaryEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = 0;
          final int _cursorIndexOfTotalCollected = 1;
          final int _cursorIndexOfTotalOnline = 2;
          final int _cursorIndexOfTotalCash = 3;
          final int _cursorIndexOfTotalExpense = 4;
          final int _cursorIndexOfNetBalance = 5;
          final List<LineSummaryEntry> _result = new ArrayList<LineSummaryEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LineSummaryEntry _item;
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final double _tmpTotalCollected;
            _tmpTotalCollected = _cursor.getDouble(_cursorIndexOfTotalCollected);
            final double _tmpTotalOnline;
            _tmpTotalOnline = _cursor.getDouble(_cursorIndexOfTotalOnline);
            final double _tmpTotalCash;
            _tmpTotalCash = _cursor.getDouble(_cursorIndexOfTotalCash);
            final double _tmpTotalExpense;
            _tmpTotalExpense = _cursor.getDouble(_cursorIndexOfTotalExpense);
            final double _tmpNetBalance;
            _tmpNetBalance = _cursor.getDouble(_cursorIndexOfNetBalance);
            _item = new LineSummaryEntry(_tmpDate,_tmpTotalCollected,_tmpTotalOnline,_tmpTotalCash,_tmpTotalExpense,_tmpNetBalance);
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
  public Flow<List<OnlineCollectionEntry>> getOnlineCollections(final String fileId,
      final long from, final long to) {
    final String _sql = "\n"
            + "        SELECT pr.name as personName, p.date as date, p.amount as amount, p.mode as paymentMode\n"
            + "        FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND p.mode = 'UPI' AND p.date >= ? AND p.date <= ?\n"
            + "        ORDER BY p.date DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, from);
    _argIndex = 3;
    _statement.bindLong(_argIndex, to);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<OnlineCollectionEntry>>() {
      @Override
      @NonNull
      public List<OnlineCollectionEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonName = 0;
          final int _cursorIndexOfDate = 1;
          final int _cursorIndexOfAmount = 2;
          final int _cursorIndexOfPaymentMode = 3;
          final List<OnlineCollectionEntry> _result = new ArrayList<OnlineCollectionEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OnlineCollectionEntry _item;
            final String _tmpPersonName;
            _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpPaymentMode;
            _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode);
            _item = new OnlineCollectionEntry(_tmpPersonName,_tmpDate,_tmpAmount,_tmpPaymentMode);
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
  public Flow<List<LoanSummaryEntry>> getLoanSummary(final String fileId) {
    final String _sql = "\n"
            + "        SELECT pr.name as name, pr.amountGiven as loanAmount, pr.interestRate as interest,\n"
            + "               pr.perInstallmentAmount as installAmount, pr.numberOfInstallments as totalInstallments,\n"
            + "               (SELECT COUNT(*) FROM payments WHERE personId = pr.id AND isDeleted = 0) as paidCount,\n"
            + "               (COALESCE(pr.totalRepayment, pr.amountGiven) - COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0)) as balance,\n"
            + "               pr.dateGiven as startDate,\n"
            + "               CASE WHEN pr.numberOfInstallments > 0 AND pr.perInstallmentAmount > 0\n"
            + "                    THEN pr.dateGiven + (pr.numberOfInstallments * CASE WHEN pr.loanType = 'DAILY' THEN 86400000 WHEN pr.loanType = 'WEEKLY' THEN 604800000 ELSE 2592000000 END)\n"
            + "                    ELSE pr.dateGiven END as endDate,\n"
            + "               CASE WHEN (SELECT COALESCE(SUM(amount), 0) FROM payments WHERE personId = pr.id AND isDeleted = 0) >= COALESCE(pr.totalRepayment, pr.amountGiven) AND pr.amountGiven > 0\n"
            + "                    THEN 'Paid' ELSE 'Active' END as status,\n"
            + "               pr.id as personId\n"
            + "        FROM persons pr\n"
            + "        WHERE pr.fileId = ? AND pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0\n"
            + "        ORDER BY pr.sortOrder ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<LoanSummaryEntry>>() {
      @Override
      @NonNull
      public List<LoanSummaryEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfName = 0;
          final int _cursorIndexOfLoanAmount = 1;
          final int _cursorIndexOfInterest = 2;
          final int _cursorIndexOfInstallAmount = 3;
          final int _cursorIndexOfTotalInstallments = 4;
          final int _cursorIndexOfPaidCount = 5;
          final int _cursorIndexOfBalance = 6;
          final int _cursorIndexOfStartDate = 7;
          final int _cursorIndexOfEndDate = 8;
          final int _cursorIndexOfStatus = 9;
          final int _cursorIndexOfPersonId = 10;
          final List<LoanSummaryEntry> _result = new ArrayList<LoanSummaryEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanSummaryEntry _item;
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpLoanAmount;
            _tmpLoanAmount = _cursor.getDouble(_cursorIndexOfLoanAmount);
            final double _tmpInterest;
            _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
            final double _tmpInstallAmount;
            _tmpInstallAmount = _cursor.getDouble(_cursorIndexOfInstallAmount);
            final int _tmpTotalInstallments;
            _tmpTotalInstallments = _cursor.getInt(_cursorIndexOfTotalInstallments);
            final int _tmpPaidCount;
            _tmpPaidCount = _cursor.getInt(_cursorIndexOfPaidCount);
            final double _tmpBalance;
            _tmpBalance = _cursor.getDouble(_cursorIndexOfBalance);
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final long _tmpEndDate;
            _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            _item = new LoanSummaryEntry(_tmpName,_tmpLoanAmount,_tmpInterest,_tmpInstallAmount,_tmpTotalInstallments,_tmpPaidCount,_tmpBalance,_tmpStartDate,_tmpEndDate,_tmpStatus,_tmpPersonId);
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
  public Flow<List<CompletedLoanEntry>> getCompletedLoans(final String fileId, final long from,
      final long to) {
    final String _sql = "\n"
            + "        SELECT pr.name as name, pr.amountGiven as loanAmount,\n"
            + "               COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) as totalCollected,\n"
            + "               pr.completedAt as completionDate,\n"
            + "               (CASE WHEN pr.dateGiven > 0 THEN (pr.completedAt - pr.dateGiven) / 86400000 ELSE 0 END) as durationDays\n"
            + "        FROM persons pr\n"
            + "        WHERE pr.fileId = ? AND pr.isDeleted = 0 AND pr.isCompleted = 1\n"
            + "          AND pr.completedAt >= ? AND pr.completedAt <= ?\n"
            + "        ORDER BY pr.completedAt DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, from);
    _argIndex = 3;
    _statement.bindLong(_argIndex, to);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<CompletedLoanEntry>>() {
      @Override
      @NonNull
      public List<CompletedLoanEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfName = 0;
          final int _cursorIndexOfLoanAmount = 1;
          final int _cursorIndexOfTotalCollected = 2;
          final int _cursorIndexOfCompletionDate = 3;
          final int _cursorIndexOfDurationDays = 4;
          final List<CompletedLoanEntry> _result = new ArrayList<CompletedLoanEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletedLoanEntry _item;
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpLoanAmount;
            _tmpLoanAmount = _cursor.getDouble(_cursorIndexOfLoanAmount);
            final double _tmpTotalCollected;
            _tmpTotalCollected = _cursor.getDouble(_cursorIndexOfTotalCollected);
            final long _tmpCompletionDate;
            _tmpCompletionDate = _cursor.getLong(_cursorIndexOfCompletionDate);
            final long _tmpDurationDays;
            _tmpDurationDays = _cursor.getLong(_cursorIndexOfDurationDays);
            _item = new CompletedLoanEntry(_tmpName,_tmpLoanAmount,_tmpTotalCollected,_tmpCompletionDate,_tmpDurationDays);
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
  public Flow<List<LoanAnalysisEntry>> getLoanAnalysis(final String fileId, final long from,
      final long to) {
    final String _sql = "\n"
            + "        SELECT pr.dateGiven as date,\n"
            + "               COUNT(DISTINCT CASE WHEN pr.isCompleted = 0 THEN pr.id END) as activeLoans,\n"
            + "               COUNT(DISTINCT CASE WHEN pr.isCompleted = 1 THEN pr.id END) as completedLoans,\n"
            + "               COALESCE(SUM(CASE WHEN pr.dateGiven >= ? AND pr.dateGiven <= ? THEN pr.amountGiven ELSE 0 END), 0) as totalDisbursed,\n"
            + "               COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.personId IN (SELECT id FROM persons WHERE fileId = ? AND isDeleted = 0) AND p.date >= ? AND p.date <= ? AND p.isDeleted = 0), 0) as totalCollected\n"
            + "        FROM persons pr\n"
            + "        WHERE pr.fileId = ? AND pr.isDeleted = 0 AND pr.isPendingNewLoan = 0\n"
            + "        GROUP BY pr.dateGiven\n"
            + "        ORDER BY pr.dateGiven ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    _argIndex = 3;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 4;
    _statement.bindLong(_argIndex, from);
    _argIndex = 5;
    _statement.bindLong(_argIndex, to);
    _argIndex = 6;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<LoanAnalysisEntry>>() {
      @Override
      @NonNull
      public List<LoanAnalysisEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = 0;
          final int _cursorIndexOfActiveLoans = 1;
          final int _cursorIndexOfCompletedLoans = 2;
          final int _cursorIndexOfTotalDisbursed = 3;
          final int _cursorIndexOfTotalCollected = 4;
          final List<LoanAnalysisEntry> _result = new ArrayList<LoanAnalysisEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanAnalysisEntry _item;
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final int _tmpActiveLoans;
            _tmpActiveLoans = _cursor.getInt(_cursorIndexOfActiveLoans);
            final int _tmpCompletedLoans;
            _tmpCompletedLoans = _cursor.getInt(_cursorIndexOfCompletedLoans);
            final double _tmpTotalDisbursed;
            _tmpTotalDisbursed = _cursor.getDouble(_cursorIndexOfTotalDisbursed);
            final double _tmpTotalCollected;
            _tmpTotalCollected = _cursor.getDouble(_cursorIndexOfTotalCollected);
            _item = new LoanAnalysisEntry(_tmpDate,_tmpActiveLoans,_tmpCompletedLoans,_tmpTotalDisbursed,_tmpTotalCollected);
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
  public Flow<List<LedgerEntry>> getLedgerEntries(final String personId) {
    final String _sql = "\n"
            + "        SELECT p.date as date, 'PAYMENT' as type, p.amount as amount, p.mode as mode, p.id as paymentId\n"
            + "        FROM payments p\n"
            + "        WHERE p.personId = ? AND p.isDeleted = 0\n"
            + "        ORDER BY p.date ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments"}, new Callable<List<LedgerEntry>>() {
      @Override
      @NonNull
      public List<LedgerEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = 0;
          final int _cursorIndexOfType = 1;
          final int _cursorIndexOfAmount = 2;
          final int _cursorIndexOfMode = 3;
          final int _cursorIndexOfPaymentId = 4;
          final List<LedgerEntry> _result = new ArrayList<LedgerEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LedgerEntry _item;
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpMode;
            _tmpMode = _cursor.getString(_cursorIndexOfMode);
            final String _tmpPaymentId;
            _tmpPaymentId = _cursor.getString(_cursorIndexOfPaymentId);
            _item = new LedgerEntry(_tmpDate,_tmpType,_tmpAmount,_tmpMode,_tmpPaymentId);
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
  public Flow<List<ExcessEntry>> getBookExcessLoss(final String fileId) {
    final String _sql = "\n"
            + "        SELECT pr.name as personName, pr.amountGiven as loanAmount,\n"
            + "               COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) as totalPaid,\n"
            + "               (COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) - COALESCE(pr.totalRepayment, pr.amountGiven)) as excessAmount\n"
            + "        FROM persons pr\n"
            + "        WHERE pr.fileId = ? AND pr.isDeleted = 0 AND pr.isCompleted = 1\n"
            + "          AND (COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) > COALESCE(pr.totalRepayment, pr.amountGiven))\n"
            + "        ORDER BY excessAmount DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"payments",
        "persons"}, new Callable<List<ExcessEntry>>() {
      @Override
      @NonNull
      public List<ExcessEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonName = 0;
          final int _cursorIndexOfLoanAmount = 1;
          final int _cursorIndexOfTotalPaid = 2;
          final int _cursorIndexOfExcessAmount = 3;
          final List<ExcessEntry> _result = new ArrayList<ExcessEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExcessEntry _item;
            final String _tmpPersonName;
            _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            final double _tmpLoanAmount;
            _tmpLoanAmount = _cursor.getDouble(_cursorIndexOfLoanAmount);
            final double _tmpTotalPaid;
            _tmpTotalPaid = _cursor.getDouble(_cursorIndexOfTotalPaid);
            final double _tmpExcessAmount;
            _tmpExcessAmount = _cursor.getDouble(_cursorIndexOfExcessAmount);
            _item = new ExcessEntry(_tmpPersonName,_tmpLoanAmount,_tmpTotalPaid,_tmpExcessAmount);
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
  public Object getSiteTotalCollected(final long from, final long to,
      final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(p.amount), 0) FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND p.date >= ? AND p.date <= ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
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
  public Object getPendingCollectionsTodayAllFiles(final long todayStart, final long todayEnd,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COUNT(*) FROM (\n"
            + "            SELECT pr.id FROM persons pr\n"
            + "            WHERE pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0\n"
            + "              AND pr.id NOT IN (\n"
            + "                  SELECT DISTINCT p.personId FROM payments p\n"
            + "                  WHERE p.isDeleted = 0 AND p.date >= ? AND p.date < ?\n"
            + "              )\n"
            + "        )\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, todayStart);
    _argIndex = 2;
    _statement.bindLong(_argIndex, todayEnd);
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
  public Object getBadLoansAllFiles(final long cutoffDate,
      final Continuation<? super List<BadLoanResult>> $completion) {
    final String _sql = "\n"
            + "        SELECT pr.id as personId, pr.name as personName, pr.fileId as fileId,\n"
            + "               ((? - (SELECT MAX(p.date) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0)) / 86400000) as daysOverdue,\n"
            + "               (COALESCE(pr.totalRepayment, pr.amountGiven) - COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0), 0)) as balance\n"
            + "        FROM persons pr\n"
            + "        WHERE pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0\n"
            + "          AND (SELECT MAX(p.date) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0) IS NOT NULL\n"
            + "          AND ((? - (SELECT MAX(p.date) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0)) / 86400000) >= pr.badLoanDays\n"
            + "        ORDER BY daysOverdue DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cutoffDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, cutoffDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BadLoanResult>>() {
      @Override
      @NonNull
      public List<BadLoanResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonId = 0;
          final int _cursorIndexOfPersonName = 1;
          final int _cursorIndexOfFileId = 2;
          final int _cursorIndexOfDaysOverdue = 3;
          final int _cursorIndexOfBalance = 4;
          final List<BadLoanResult> _result = new ArrayList<BadLoanResult>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BadLoanResult _item;
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final String _tmpPersonName;
            _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            final long _tmpDaysOverdue;
            _tmpDaysOverdue = _cursor.getLong(_cursorIndexOfDaysOverdue);
            final double _tmpBalance;
            _tmpBalance = _cursor.getDouble(_cursorIndexOfBalance);
            _item = new BadLoanResult(_tmpPersonId,_tmpPersonName,_tmpFileId,_tmpDaysOverdue,_tmpBalance);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __PaymentMode_enumToString(@NonNull final PaymentMode _value) {
    switch (_value) {
      case CASH: return "CASH";
      case UPI: return "UPI";
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

  private PaymentMode __PaymentMode_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "CASH": return PaymentMode.CASH;
      case "UPI": return PaymentMode.UPI;
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
