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
import com.moneymate.app.data.local.entity.EditPermissionScope;
import com.moneymate.app.data.local.entity.Payment;
import com.moneymate.app.data.local.entity.PaymentMode;
import java.lang.Class;
import java.lang.Double;
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
  public Flow<List<Payment>> getPaymentsForFileIncludingCompleted(final String fileId) {
    final String _sql = "\n"
            + "        SELECT p.* FROM payments p\n"
            + "        INNER JOIN persons pr ON p.personId = pr.id\n"
            + "        WHERE pr.fileId = ? AND p.isDeleted = 0 AND pr.isDeleted = 0\n"
            + "          AND pr.isPendingNewLoan = 0\n"
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
