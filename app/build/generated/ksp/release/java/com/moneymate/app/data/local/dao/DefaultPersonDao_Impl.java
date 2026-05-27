package com.moneymate.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.moneymate.app.data.local.entity.DefaultPerson;
import com.moneymate.app.data.local.entity.LoanType;
import com.moneymate.app.data.local.entity.PaymentMode;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
public final class DefaultPersonDao_Impl implements DefaultPersonDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DefaultPerson> __insertionAdapterOfDefaultPerson;

  private final EntityDeletionOrUpdateAdapter<DefaultPerson> __deletionAdapterOfDefaultPerson;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllForNlr;

  private final SharedSQLiteStatement __preparedStmtOfUpdate;

  public DefaultPersonDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDefaultPerson = new EntityInsertionAdapter<DefaultPerson>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `default_persons` (`id`,`nlrKey`,`name`,`place`,`mobileNumber`,`amountGiven`,`mode`,`sortOrder`,`recordType`,`isSeeded`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DefaultPerson entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getNlrKey());
        statement.bindString(3, entity.getName());
        if (entity.getPlace() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPlace());
        }
        if (entity.getMobileNumber() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getMobileNumber());
        }
        statement.bindDouble(6, entity.getAmountGiven());
        statement.bindString(7, __PaymentMode_enumToString(entity.getMode()));
        statement.bindLong(8, entity.getSortOrder());
        statement.bindString(9, __LoanType_enumToString(entity.getRecordType()));
        final int _tmp = entity.isSeeded() ? 1 : 0;
        statement.bindLong(10, _tmp);
      }
    };
    this.__deletionAdapterOfDefaultPerson = new EntityDeletionOrUpdateAdapter<DefaultPerson>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `default_persons` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DefaultPerson entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllForNlr = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM default_persons WHERE nlrKey = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdate = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE default_persons SET name=?, place=?, mobileNumber=?, amountGiven=?, mode=?, recordType=? WHERE id=?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<DefaultPerson> persons,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDefaultPerson.insert(persons);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final DefaultPerson person, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDefaultPerson.insert(person);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final DefaultPerson person, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfDefaultPerson.handle(person);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllForNlr(final String nlrKey, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllForNlr.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, nlrKey);
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
          __preparedStmtOfDeleteAllForNlr.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final String id, final String name, final String place, final String mobile,
      final double amount, final String mode, final String recordType,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdate.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        _argIndex = 2;
        if (place == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, place);
        }
        _argIndex = 3;
        if (mobile == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, mobile);
        }
        _argIndex = 4;
        _stmt.bindDouble(_argIndex, amount);
        _argIndex = 5;
        _stmt.bindString(_argIndex, mode);
        _argIndex = 6;
        _stmt.bindString(_argIndex, recordType);
        _argIndex = 7;
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
          __preparedStmtOfUpdate.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DefaultPerson>> getByNlrKey(final String nlrKey) {
    final String _sql = "SELECT * FROM default_persons WHERE nlrKey = ? ORDER BY sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nlrKey);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"default_persons"}, new Callable<List<DefaultPerson>>() {
      @Override
      @NonNull
      public List<DefaultPerson> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNlrKey = CursorUtil.getColumnIndexOrThrow(_cursor, "nlrKey");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsSeeded = CursorUtil.getColumnIndexOrThrow(_cursor, "isSeeded");
          final List<DefaultPerson> _result = new ArrayList<DefaultPerson>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DefaultPerson _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpNlrKey;
            _tmpNlrKey = _cursor.getString(_cursorIndexOfNlrKey);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPlace;
            if (_cursor.isNull(_cursorIndexOfPlace)) {
              _tmpPlace = null;
            } else {
              _tmpPlace = _cursor.getString(_cursorIndexOfPlace);
            }
            final String _tmpMobileNumber;
            if (_cursor.isNull(_cursorIndexOfMobileNumber)) {
              _tmpMobileNumber = null;
            } else {
              _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            }
            final double _tmpAmountGiven;
            _tmpAmountGiven = _cursor.getDouble(_cursorIndexOfAmountGiven);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsSeeded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSeeded);
            _tmpIsSeeded = _tmp != 0;
            _item = new DefaultPerson(_tmpId,_tmpNlrKey,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpSortOrder,_tmpRecordType,_tmpIsSeeded);
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
  public Object getByNlrKeyOnce(final String nlrKey,
      final Continuation<? super List<DefaultPerson>> $completion) {
    final String _sql = "SELECT * FROM default_persons WHERE nlrKey = ? ORDER BY sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nlrKey);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DefaultPerson>>() {
      @Override
      @NonNull
      public List<DefaultPerson> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNlrKey = CursorUtil.getColumnIndexOrThrow(_cursor, "nlrKey");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsSeeded = CursorUtil.getColumnIndexOrThrow(_cursor, "isSeeded");
          final List<DefaultPerson> _result = new ArrayList<DefaultPerson>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DefaultPerson _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpNlrKey;
            _tmpNlrKey = _cursor.getString(_cursorIndexOfNlrKey);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPlace;
            if (_cursor.isNull(_cursorIndexOfPlace)) {
              _tmpPlace = null;
            } else {
              _tmpPlace = _cursor.getString(_cursorIndexOfPlace);
            }
            final String _tmpMobileNumber;
            if (_cursor.isNull(_cursorIndexOfMobileNumber)) {
              _tmpMobileNumber = null;
            } else {
              _tmpMobileNumber = _cursor.getString(_cursorIndexOfMobileNumber);
            }
            final double _tmpAmountGiven;
            _tmpAmountGiven = _cursor.getDouble(_cursorIndexOfAmountGiven);
            final PaymentMode _tmpMode;
            _tmpMode = __PaymentMode_stringToEnum(_cursor.getString(_cursorIndexOfMode));
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsSeeded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSeeded);
            _tmpIsSeeded = _tmp != 0;
            _item = new DefaultPerson(_tmpId,_tmpNlrKey,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpSortOrder,_tmpRecordType,_tmpIsSeeded);
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
  public Object countForNlr(final String nlrKey, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM default_persons WHERE nlrKey = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nlrKey);
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

  private String __PaymentMode_enumToString(@NonNull final PaymentMode _value) {
    switch (_value) {
      case CASH: return "CASH";
      case UPI: return "UPI";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __LoanType_enumToString(@NonNull final LoanType _value) {
    switch (_value) {
      case LENDING: return "LENDING";
      case BORROWING: return "BORROWING";
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

  private LoanType __LoanType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "LENDING": return LoanType.LENDING;
      case "BORROWING": return LoanType.BORROWING;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
