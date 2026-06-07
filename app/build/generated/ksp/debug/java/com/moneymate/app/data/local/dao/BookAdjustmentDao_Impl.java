package com.moneymate.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.moneymate.app.data.local.entity.AdjustmentType;
import com.moneymate.app.data.local.entity.BookAdjustment;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
public final class BookAdjustmentDao_Impl implements BookAdjustmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BookAdjustment> __insertionAdapterOfBookAdjustment;

  public BookAdjustmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBookAdjustment = new EntityInsertionAdapter<BookAdjustment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `book_adjustments` (`id`,`personId`,`fileId`,`discrepancyAmount`,`type`,`reason`,`createdAt`,`approvedByAdmin`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BookAdjustment entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getPersonId());
        statement.bindString(3, entity.getFileId());
        statement.bindDouble(4, entity.getDiscrepancyAmount());
        statement.bindString(5, __AdjustmentType_enumToString(entity.getType()));
        statement.bindString(6, entity.getReason());
        statement.bindLong(7, entity.getCreatedAt());
        final int _tmp = entity.getApprovedByAdmin() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
  }

  @Override
  public Object insert(final BookAdjustment adjustment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBookAdjustment.insert(adjustment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BookAdjustment>> getByFileId(final String fileId) {
    final String _sql = "SELECT * FROM book_adjustments WHERE fileId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"book_adjustments"}, new Callable<List<BookAdjustment>>() {
      @Override
      @NonNull
      public List<BookAdjustment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfDiscrepancyAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discrepancyAmount");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfApprovedByAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "approvedByAdmin");
          final List<BookAdjustment> _result = new ArrayList<BookAdjustment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookAdjustment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            final double _tmpDiscrepancyAmount;
            _tmpDiscrepancyAmount = _cursor.getDouble(_cursorIndexOfDiscrepancyAmount);
            final AdjustmentType _tmpType;
            _tmpType = __AdjustmentType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpApprovedByAdmin;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfApprovedByAdmin);
            _tmpApprovedByAdmin = _tmp != 0;
            _item = new BookAdjustment(_tmpId,_tmpPersonId,_tmpFileId,_tmpDiscrepancyAmount,_tmpType,_tmpReason,_tmpCreatedAt,_tmpApprovedByAdmin);
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
  public Object getByPersonId(final String personId,
      final Continuation<? super List<BookAdjustment>> $completion) {
    final String _sql = "SELECT * FROM book_adjustments WHERE personId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, personId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BookAdjustment>>() {
      @Override
      @NonNull
      public List<BookAdjustment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfDiscrepancyAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discrepancyAmount");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfApprovedByAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "approvedByAdmin");
          final List<BookAdjustment> _result = new ArrayList<BookAdjustment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookAdjustment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            final double _tmpDiscrepancyAmount;
            _tmpDiscrepancyAmount = _cursor.getDouble(_cursorIndexOfDiscrepancyAmount);
            final AdjustmentType _tmpType;
            _tmpType = __AdjustmentType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpApprovedByAdmin;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfApprovedByAdmin);
            _tmpApprovedByAdmin = _tmp != 0;
            _item = new BookAdjustment(_tmpId,_tmpPersonId,_tmpFileId,_tmpDiscrepancyAmount,_tmpType,_tmpReason,_tmpCreatedAt,_tmpApprovedByAdmin);
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
  public Flow<List<BookAdjustment>> getAll() {
    final String _sql = "SELECT * FROM book_adjustments ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"book_adjustments"}, new Callable<List<BookAdjustment>>() {
      @Override
      @NonNull
      public List<BookAdjustment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "personId");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfDiscrepancyAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discrepancyAmount");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfApprovedByAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "approvedByAdmin");
          final List<BookAdjustment> _result = new ArrayList<BookAdjustment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookAdjustment _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpPersonId;
            _tmpPersonId = _cursor.getString(_cursorIndexOfPersonId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
            final double _tmpDiscrepancyAmount;
            _tmpDiscrepancyAmount = _cursor.getDouble(_cursorIndexOfDiscrepancyAmount);
            final AdjustmentType _tmpType;
            _tmpType = __AdjustmentType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpApprovedByAdmin;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfApprovedByAdmin);
            _tmpApprovedByAdmin = _tmp != 0;
            _item = new BookAdjustment(_tmpId,_tmpPersonId,_tmpFileId,_tmpDiscrepancyAmount,_tmpType,_tmpReason,_tmpCreatedAt,_tmpApprovedByAdmin);
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

  private String __AdjustmentType_enumToString(@NonNull final AdjustmentType _value) {
    switch (_value) {
      case BOOK_PROFIT: return "BOOK_PROFIT";
      case BOOK_LOSS: return "BOOK_LOSS";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private AdjustmentType __AdjustmentType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "BOOK_PROFIT": return AdjustmentType.BOOK_PROFIT;
      case "BOOK_LOSS": return AdjustmentType.BOOK_LOSS;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
