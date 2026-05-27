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
import com.moneymate.app.data.local.entity.LoanType;
import com.moneymate.app.data.local.entity.PaymentMode;
import com.moneymate.app.data.local.entity.Person;
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
public final class PersonDao_Impl implements PersonDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Person> __insertionAdapterOfPerson;

  private final EntityDeletionOrUpdateAdapter<Person> __updateAdapterOfPerson;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMobileNumber;

  private final SharedSQLiteStatement __preparedStmtOfShiftSortOrdersAfter;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSortOrder;

  private final SharedSQLiteStatement __preparedStmtOfUpdateNameAndPlace;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeletePerson;

  private final SharedSQLiteStatement __preparedStmtOfRestorePerson;

  private final SharedSQLiteStatement __preparedStmtOfHardDeletePerson;

  private final SharedSQLiteStatement __preparedStmtOfPurgeExpiredPersons;

  private final SharedSQLiteStatement __preparedStmtOfPurgeExpiredCompletedPersons;

  private final SharedSQLiteStatement __preparedStmtOfMarkAllUploadedInFile;

  private final SharedSQLiteStatement __preparedStmtOfSetEditPermission;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsCompleted;

  private final SharedSQLiteStatement __preparedStmtOfActivatePendingNewLoan;

  public PersonDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPerson = new EntityInsertionAdapter<Person>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `persons` (`id`,`fileId`,`name`,`place`,`mobileNumber`,`amountGiven`,`mode`,`dateGiven`,`sortOrder`,`isDeleted`,`deletedAt`,`uploadedAt`,`editPermissionGranted`,`editPermissionScope`,`recordType`,`isCompleted`,`completedAt`,`linkedNewPersonId`,`isPendingNewLoan`,`previousPersonId`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Person entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFileId());
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
        statement.bindLong(8, entity.getDateGiven());
        statement.bindLong(9, entity.getSortOrder());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getDeletedAt());
        }
        if (entity.getUploadedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getUploadedAt());
        }
        final int _tmp_1 = entity.getEditPermissionGranted() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        statement.bindString(14, __EditPermissionScope_enumToString(entity.getEditPermissionScope()));
        statement.bindString(15, __LoanType_enumToString(entity.getRecordType()));
        final int _tmp_2 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(16, _tmp_2);
        if (entity.getCompletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getCompletedAt());
        }
        if (entity.getLinkedNewPersonId() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getLinkedNewPersonId());
        }
        final int _tmp_3 = entity.isPendingNewLoan() ? 1 : 0;
        statement.bindLong(19, _tmp_3);
        if (entity.getPreviousPersonId() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getPreviousPersonId());
        }
      }
    };
    this.__updateAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Person>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `persons` SET `id` = ?,`fileId` = ?,`name` = ?,`place` = ?,`mobileNumber` = ?,`amountGiven` = ?,`mode` = ?,`dateGiven` = ?,`sortOrder` = ?,`isDeleted` = ?,`deletedAt` = ?,`uploadedAt` = ?,`editPermissionGranted` = ?,`editPermissionScope` = ?,`recordType` = ?,`isCompleted` = ?,`completedAt` = ?,`linkedNewPersonId` = ?,`isPendingNewLoan` = ?,`previousPersonId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Person entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFileId());
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
        statement.bindLong(8, entity.getDateGiven());
        statement.bindLong(9, entity.getSortOrder());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getDeletedAt());
        }
        if (entity.getUploadedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getUploadedAt());
        }
        final int _tmp_1 = entity.getEditPermissionGranted() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        statement.bindString(14, __EditPermissionScope_enumToString(entity.getEditPermissionScope()));
        statement.bindString(15, __LoanType_enumToString(entity.getRecordType()));
        final int _tmp_2 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(16, _tmp_2);
        if (entity.getCompletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getCompletedAt());
        }
        if (entity.getLinkedNewPersonId() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getLinkedNewPersonId());
        }
        final int _tmp_3 = entity.isPendingNewLoan() ? 1 : 0;
        statement.bindLong(19, _tmp_3);
        if (entity.getPreviousPersonId() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getPreviousPersonId());
        }
        statement.bindString(21, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateMobileNumber = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET mobileNumber = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfShiftSortOrdersAfter = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET sortOrder = sortOrder + 1 WHERE fileId = ? AND sortOrder > ? AND isDeleted = 0";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSortOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET sortOrder = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateNameAndPlace = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET name = ?, place = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDeletePerson = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET isDeleted = 1, deletedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRestorePerson = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET isDeleted = 0, deletedAt = NULL WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfHardDeletePerson = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persons WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfPurgeExpiredPersons = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persons WHERE isDeleted = 1 AND deletedAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfPurgeExpiredCompletedPersons = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persons WHERE isCompleted = 1 AND completedAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAllUploadedInFile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET uploadedAt = ? WHERE fileId = ? AND isDeleted = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSetEditPermission = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET editPermissionGranted = ?, editPermissionScope = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsCompleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET isCompleted = 1, completedAt = ?, linkedNewPersonId = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfActivatePendingNewLoan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE persons SET isPendingNewLoan = 0, amountGiven = ?, dateGiven = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPerson(final Person person, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPerson.insert(person);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePerson(final Person person, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPerson.handle(person);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMobileNumber(final String id, final String mobileNumber,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMobileNumber.acquire();
        int _argIndex = 1;
        if (mobileNumber == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, mobileNumber);
        }
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
          __preparedStmtOfUpdateMobileNumber.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object shiftSortOrdersAfter(final String fileId, final int afterSortOrder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfShiftSortOrdersAfter.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, fileId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, afterSortOrder);
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
          __preparedStmtOfShiftSortOrdersAfter.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSortOrder(final String id, final int sortOrder,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSortOrder.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sortOrder);
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
          __preparedStmtOfUpdateSortOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateNameAndPlace(final String id, final String name, final String place,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateNameAndPlace.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        _argIndex = 2;
        if (place == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, place);
        }
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
          __preparedStmtOfUpdateNameAndPlace.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeletePerson(final String id, final long deletedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeletePerson.acquire();
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
          __preparedStmtOfSoftDeletePerson.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object restorePerson(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRestorePerson.acquire();
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
          __preparedStmtOfRestorePerson.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object hardDeletePerson(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfHardDeletePerson.acquire();
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
          __preparedStmtOfHardDeletePerson.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object purgeExpiredPersons(final long cutoff,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeExpiredPersons.acquire();
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
          __preparedStmtOfPurgeExpiredPersons.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object purgeExpiredCompletedPersons(final long cutoff,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeExpiredCompletedPersons.acquire();
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
          __preparedStmtOfPurgeExpiredCompletedPersons.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAllUploadedInFile(final String fileId, final long uploadedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAllUploadedInFile.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, uploadedAt);
        _argIndex = 2;
        _stmt.bindString(_argIndex, fileId);
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
          __preparedStmtOfMarkAllUploadedInFile.release(_stmt);
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
  public Object markAsCompleted(final String id, final long completedAt,
      final String linkedNewPersonId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsCompleted.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, completedAt);
        _argIndex = 2;
        _stmt.bindString(_argIndex, linkedNewPersonId);
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
          __preparedStmtOfMarkAsCompleted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object activatePendingNewLoan(final String id, final double amount, final long dateGiven,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfActivatePendingNewLoan.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, amount);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dateGiven);
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
          __preparedStmtOfActivatePendingNewLoan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Person>> getPersonsByFile(final String fileId) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND isDeleted = 0 AND isCompleted = 0 AND isPendingNewLoan = 0 ORDER BY sortOrder ASC, dateGiven ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Flow<List<Person>> getPersonsByFileIncludingPending(final String fileId) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND isDeleted = 0 AND isCompleted = 0 ORDER BY sortOrder ASC, dateGiven ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Flow<List<Person>> getPersonsByFileSortedByDate(final String fileId) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND isDeleted = 0 ORDER BY dateGiven ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Flow<List<Person>> getPersonsByFileSortedByMode(final String fileId) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND isDeleted = 0 ORDER BY mode ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Object getPersonById(final String id, final Continuation<? super Person> $completion) {
    final String _sql = "SELECT * FROM persons WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Person>() {
      @Override
      @Nullable
      public Person call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final Person _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _result = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Flow<List<Person>> getCompletedPersonsByFile(final String fileId) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND isCompleted = 1 AND isDeleted = 0 ORDER BY completedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Flow<List<Person>> getPendingNewLoanPersonsByFile(final String fileId) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND isPendingNewLoan = 1 AND isDeleted = 0 ORDER BY sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Object findDuplicateByName(final String fileId, final String name,
      final Continuation<? super List<Person>> $completion) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND LOWER(name) = LOWER(?) AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Object findDuplicateByNameAndPlace(final String fileId, final String name,
      final String place, final Continuation<? super List<Person>> $completion) {
    final String _sql = "SELECT * FROM persons WHERE fileId = ? AND LOWER(name) = LOWER(?) AND LOWER(place) = LOWER(?) AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    _argIndex = 2;
    _statement.bindString(_argIndex, name);
    _argIndex = 3;
    _statement.bindString(_argIndex, place);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Object findAllNamesInFile(final String fileId,
      final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT name FROM persons WHERE fileId = ? AND isDeleted = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fileId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
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
  public Flow<List<Person>> getDeletedPersons() {
    final String _sql = "SELECT * FROM persons WHERE isDeleted = 1 ORDER BY deletedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persons"}, new Callable<List<Person>>() {
      @Override
      @NonNull
      public List<Person> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFileId = CursorUtil.getColumnIndexOrThrow(_cursor, "fileId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPlace = CursorUtil.getColumnIndexOrThrow(_cursor, "place");
          final int _cursorIndexOfMobileNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "mobileNumber");
          final int _cursorIndexOfAmountGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "amountGiven");
          final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
          final int _cursorIndexOfDateGiven = CursorUtil.getColumnIndexOrThrow(_cursor, "dateGiven");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadedAt");
          final int _cursorIndexOfEditPermissionGranted = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionGranted");
          final int _cursorIndexOfEditPermissionScope = CursorUtil.getColumnIndexOrThrow(_cursor, "editPermissionScope");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfLinkedNewPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedNewPersonId");
          final int _cursorIndexOfIsPendingNewLoan = CursorUtil.getColumnIndexOrThrow(_cursor, "isPendingNewLoan");
          final int _cursorIndexOfPreviousPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "previousPersonId");
          final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Person _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFileId;
            _tmpFileId = _cursor.getString(_cursorIndexOfFileId);
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
            final long _tmpDateGiven;
            _tmpDateGiven = _cursor.getLong(_cursorIndexOfDateGiven);
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
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
            final Long _tmpUploadedAt;
            if (_cursor.isNull(_cursorIndexOfUploadedAt)) {
              _tmpUploadedAt = null;
            } else {
              _tmpUploadedAt = _cursor.getLong(_cursorIndexOfUploadedAt);
            }
            final boolean _tmpEditPermissionGranted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfEditPermissionGranted);
            _tmpEditPermissionGranted = _tmp_1 != 0;
            final EditPermissionScope _tmpEditPermissionScope;
            _tmpEditPermissionScope = __EditPermissionScope_stringToEnum(_cursor.getString(_cursorIndexOfEditPermissionScope));
            final LoanType _tmpRecordType;
            _tmpRecordType = __LoanType_stringToEnum(_cursor.getString(_cursorIndexOfRecordType));
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final String _tmpLinkedNewPersonId;
            if (_cursor.isNull(_cursorIndexOfLinkedNewPersonId)) {
              _tmpLinkedNewPersonId = null;
            } else {
              _tmpLinkedNewPersonId = _cursor.getString(_cursorIndexOfLinkedNewPersonId);
            }
            final boolean _tmpIsPendingNewLoan;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsPendingNewLoan);
            _tmpIsPendingNewLoan = _tmp_3 != 0;
            final String _tmpPreviousPersonId;
            if (_cursor.isNull(_cursorIndexOfPreviousPersonId)) {
              _tmpPreviousPersonId = null;
            } else {
              _tmpPreviousPersonId = _cursor.getString(_cursorIndexOfPreviousPersonId);
            }
            _item = new Person(_tmpId,_tmpFileId,_tmpName,_tmpPlace,_tmpMobileNumber,_tmpAmountGiven,_tmpMode,_tmpDateGiven,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpUploadedAt,_tmpEditPermissionGranted,_tmpEditPermissionScope,_tmpRecordType,_tmpIsCompleted,_tmpCompletedAt,_tmpLinkedNewPersonId,_tmpIsPendingNewLoan,_tmpPreviousPersonId);
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
  public Object getTotalGivenInFile(final String fileId,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(amountGiven) FROM persons WHERE fileId = ? AND isDeleted = 0";
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
  public Object getTotalGivenCashInFile(final String fileId,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(amountGiven) FROM persons WHERE fileId = ? AND isDeleted = 0 AND mode = 'CASH'";
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
  public Object getTotalGivenUpiInFile(final String fileId,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(amountGiven) FROM persons WHERE fileId = ? AND isDeleted = 0 AND mode = 'UPI'";
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

  private EditPermissionScope __EditPermissionScope_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "NONE": return EditPermissionScope.NONE;
      case "THIS_RECORD": return EditPermissionScope.THIS_RECORD;
      case "ALL_LOCKED": return EditPermissionScope.ALL_LOCKED;
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
