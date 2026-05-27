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
import com.moneymate.app.data.local.entity.LoanFile;
import java.lang.Class;
import java.lang.Exception;
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
public final class FileDao_Impl implements FileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LoanFile> __insertionAdapterOfLoanFile;

  private final EntityDeletionOrUpdateAdapter<LoanFile> __updateAdapterOfLoanFile;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteFile;

  private final SharedSQLiteStatement __preparedStmtOfRestoreFile;

  private final SharedSQLiteStatement __preparedStmtOfHardDeleteFile;

  private final SharedSQLiteStatement __preparedStmtOfPurgeExpiredFiles;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSortOrder;

  public FileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLoanFile = new EntityInsertionAdapter<LoanFile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `loan_files` (`id`,`name`,`createdAt`,`sortOrder`,`isDeleted`,`deletedAt`,`syncedToFirebase`,`lastUploadedAt`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LoanFile entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getSortOrder());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDeletedAt());
        }
        final int _tmp_1 = entity.getSyncedToFirebase() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        if (entity.getLastUploadedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getLastUploadedAt());
        }
      }
    };
    this.__updateAdapterOfLoanFile = new EntityDeletionOrUpdateAdapter<LoanFile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `loan_files` SET `id` = ?,`name` = ?,`createdAt` = ?,`sortOrder` = ?,`isDeleted` = ?,`deletedAt` = ?,`syncedToFirebase` = ?,`lastUploadedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LoanFile entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getSortOrder());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        if (entity.getDeletedAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDeletedAt());
        }
        final int _tmp_1 = entity.getSyncedToFirebase() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        if (entity.getLastUploadedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getLastUploadedAt());
        }
        statement.bindString(9, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDeleteFile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE loan_files SET isDeleted = 1, deletedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRestoreFile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE loan_files SET isDeleted = 0, deletedAt = null WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfHardDeleteFile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM loan_files WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfPurgeExpiredFiles = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM loan_files WHERE isDeleted = 1 AND deletedAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE loan_files SET syncedToFirebase = ?, lastUploadedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSortOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE loan_files SET sortOrder = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertFile(final LoanFile file, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLoanFile.insert(file);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFile(final LoanFile file, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLoanFile.handle(file);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeleteFile(final String id, final long deletedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeleteFile.acquire();
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
          __preparedStmtOfSoftDeleteFile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object restoreFile(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRestoreFile.acquire();
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
          __preparedStmtOfRestoreFile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object hardDeleteFile(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfHardDeleteFile.acquire();
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
          __preparedStmtOfHardDeleteFile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object purgeExpiredFiles(final long cutoff, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeExpiredFiles.acquire();
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
          __preparedStmtOfPurgeExpiredFiles.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final String id, final boolean synced, final long uploadedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        final int _tmp = synced ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, uploadedAt);
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
          __preparedStmtOfMarkSynced.release(_stmt);
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
  public Flow<List<LoanFile>> getTrashedFiles() {
    final String _sql = "SELECT * FROM loan_files WHERE isDeleted = 1 ORDER BY deletedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"loan_files"}, new Callable<List<LoanFile>>() {
      @Override
      @NonNull
      public List<LoanFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncedToFirebase = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedToFirebase");
          final int _cursorIndexOfLastUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadedAt");
          final List<LoanFile> _result = new ArrayList<LoanFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanFile _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
            final boolean _tmpSyncedToFirebase;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSyncedToFirebase);
            _tmpSyncedToFirebase = _tmp_1 != 0;
            final Long _tmpLastUploadedAt;
            if (_cursor.isNull(_cursorIndexOfLastUploadedAt)) {
              _tmpLastUploadedAt = null;
            } else {
              _tmpLastUploadedAt = _cursor.getLong(_cursorIndexOfLastUploadedAt);
            }
            _item = new LoanFile(_tmpId,_tmpName,_tmpCreatedAt,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncedToFirebase,_tmpLastUploadedAt);
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
  public Object getFileById(final String id, final Continuation<? super LoanFile> $completion) {
    final String _sql = "SELECT * FROM loan_files WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LoanFile>() {
      @Override
      @Nullable
      public LoanFile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncedToFirebase = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedToFirebase");
          final int _cursorIndexOfLastUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadedAt");
          final LoanFile _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
            final boolean _tmpSyncedToFirebase;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSyncedToFirebase);
            _tmpSyncedToFirebase = _tmp_1 != 0;
            final Long _tmpLastUploadedAt;
            if (_cursor.isNull(_cursorIndexOfLastUploadedAt)) {
              _tmpLastUploadedAt = null;
            } else {
              _tmpLastUploadedAt = _cursor.getLong(_cursorIndexOfLastUploadedAt);
            }
            _result = new LoanFile(_tmpId,_tmpName,_tmpCreatedAt,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncedToFirebase,_tmpLastUploadedAt);
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
  public Object getFileByName(final String name, final Continuation<? super LoanFile> $completion) {
    final String _sql = "SELECT * FROM loan_files WHERE LOWER(name) = LOWER(?) LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LoanFile>() {
      @Override
      @Nullable
      public LoanFile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncedToFirebase = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedToFirebase");
          final int _cursorIndexOfLastUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadedAt");
          final LoanFile _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
            final boolean _tmpSyncedToFirebase;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSyncedToFirebase);
            _tmpSyncedToFirebase = _tmp_1 != 0;
            final Long _tmpLastUploadedAt;
            if (_cursor.isNull(_cursorIndexOfLastUploadedAt)) {
              _tmpLastUploadedAt = null;
            } else {
              _tmpLastUploadedAt = _cursor.getLong(_cursorIndexOfLastUploadedAt);
            }
            _result = new LoanFile(_tmpId,_tmpName,_tmpCreatedAt,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncedToFirebase,_tmpLastUploadedAt);
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
  public Flow<List<LoanFile>> getAllFiles() {
    final String _sql = "SELECT * FROM loan_files WHERE isDeleted = 0 ORDER BY sortOrder ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"loan_files"}, new Callable<List<LoanFile>>() {
      @Override
      @NonNull
      public List<LoanFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncedToFirebase = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedToFirebase");
          final int _cursorIndexOfLastUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadedAt");
          final List<LoanFile> _result = new ArrayList<LoanFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanFile _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
            final boolean _tmpSyncedToFirebase;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSyncedToFirebase);
            _tmpSyncedToFirebase = _tmp_1 != 0;
            final Long _tmpLastUploadedAt;
            if (_cursor.isNull(_cursorIndexOfLastUploadedAt)) {
              _tmpLastUploadedAt = null;
            } else {
              _tmpLastUploadedAt = _cursor.getLong(_cursorIndexOfLastUploadedAt);
            }
            _item = new LoanFile(_tmpId,_tmpName,_tmpCreatedAt,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncedToFirebase,_tmpLastUploadedAt);
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
  public Object getAllFilesOnce(final Continuation<? super List<LoanFile>> $completion) {
    final String _sql = "SELECT * FROM loan_files WHERE isDeleted = 0 ORDER BY sortOrder ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LoanFile>>() {
      @Override
      @NonNull
      public List<LoanFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt");
          final int _cursorIndexOfSyncedToFirebase = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedToFirebase");
          final int _cursorIndexOfLastUploadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadedAt");
          final List<LoanFile> _result = new ArrayList<LoanFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanFile _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
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
            final boolean _tmpSyncedToFirebase;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSyncedToFirebase);
            _tmpSyncedToFirebase = _tmp_1 != 0;
            final Long _tmpLastUploadedAt;
            if (_cursor.isNull(_cursorIndexOfLastUploadedAt)) {
              _tmpLastUploadedAt = null;
            } else {
              _tmpLastUploadedAt = _cursor.getLong(_cursorIndexOfLastUploadedAt);
            }
            _item = new LoanFile(_tmpId,_tmpName,_tmpCreatedAt,_tmpSortOrder,_tmpIsDeleted,_tmpDeletedAt,_tmpSyncedToFirebase,_tmpLastUploadedAt);
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
}
