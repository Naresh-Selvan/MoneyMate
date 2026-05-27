package com.moneymate.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.moneymate.app.data.local.dao.DefaultPersonDao;
import com.moneymate.app.data.local.dao.DefaultPersonDao_Impl;
import com.moneymate.app.data.local.dao.EditRequestDao;
import com.moneymate.app.data.local.dao.EditRequestDao_Impl;
import com.moneymate.app.data.local.dao.FileDao;
import com.moneymate.app.data.local.dao.FileDao_Impl;
import com.moneymate.app.data.local.dao.PaymentDao;
import com.moneymate.app.data.local.dao.PaymentDao_Impl;
import com.moneymate.app.data.local.dao.PersonDao;
import com.moneymate.app.data.local.dao.PersonDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile FileDao _fileDao;

  private volatile PersonDao _personDao;

  private volatile PaymentDao _paymentDao;

  private volatile EditRequestDao _editRequestDao;

  private volatile DefaultPersonDao _defaultPersonDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(7) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `loan_files` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `syncedToFirebase` INTEGER NOT NULL, `lastUploadedAt` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `persons` (`id` TEXT NOT NULL, `fileId` TEXT NOT NULL, `name` TEXT NOT NULL, `place` TEXT, `mobileNumber` TEXT, `amountGiven` REAL NOT NULL, `mode` TEXT NOT NULL, `dateGiven` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `uploadedAt` INTEGER, `editPermissionGranted` INTEGER NOT NULL, `editPermissionScope` TEXT NOT NULL, `recordType` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `completedAt` INTEGER, `linkedNewPersonId` TEXT, `isPendingNewLoan` INTEGER NOT NULL, `previousPersonId` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`fileId`) REFERENCES `loan_files`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_persons_fileId` ON `persons` (`fileId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `payments` (`id` TEXT NOT NULL, `personId` TEXT NOT NULL, `amount` REAL NOT NULL, `mode` TEXT NOT NULL, `date` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `isRollover` INTEGER NOT NULL, `uploadedAt` INTEGER, `editPermissionGranted` INTEGER NOT NULL, `editPermissionScope` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`personId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_personId` ON `payments` (`personId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `edit_requests` (`id` TEXT NOT NULL, `recordId` TEXT NOT NULL, `recordType` TEXT NOT NULL, `requestedAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `resolvedAt` INTEGER, `scope` TEXT NOT NULL, `firestoreRequestId` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `default_persons` (`id` TEXT NOT NULL, `nlrKey` TEXT NOT NULL, `name` TEXT NOT NULL, `place` TEXT, `mobileNumber` TEXT, `amountGiven` REAL NOT NULL, `mode` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `recordType` TEXT NOT NULL, `isSeeded` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '61f6b9a0b3bbba31ff88e666d4935fba')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `loan_files`");
        db.execSQL("DROP TABLE IF EXISTS `persons`");
        db.execSQL("DROP TABLE IF EXISTS `payments`");
        db.execSQL("DROP TABLE IF EXISTS `edit_requests`");
        db.execSQL("DROP TABLE IF EXISTS `default_persons`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsLoanFiles = new HashMap<String, TableInfo.Column>(8);
        _columnsLoanFiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("syncedToFirebase", new TableInfo.Column("syncedToFirebase", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("lastUploadedAt", new TableInfo.Column("lastUploadedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLoanFiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLoanFiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLoanFiles = new TableInfo("loan_files", _columnsLoanFiles, _foreignKeysLoanFiles, _indicesLoanFiles);
        final TableInfo _existingLoanFiles = TableInfo.read(db, "loan_files");
        if (!_infoLoanFiles.equals(_existingLoanFiles)) {
          return new RoomOpenHelper.ValidationResult(false, "loan_files(com.moneymate.app.data.local.entity.LoanFile).\n"
                  + " Expected:\n" + _infoLoanFiles + "\n"
                  + " Found:\n" + _existingLoanFiles);
        }
        final HashMap<String, TableInfo.Column> _columnsPersons = new HashMap<String, TableInfo.Column>(20);
        _columnsPersons.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("place", new TableInfo.Column("place", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("mobileNumber", new TableInfo.Column("mobileNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("amountGiven", new TableInfo.Column("amountGiven", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("dateGiven", new TableInfo.Column("dateGiven", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("uploadedAt", new TableInfo.Column("uploadedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("editPermissionGranted", new TableInfo.Column("editPermissionGranted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("editPermissionScope", new TableInfo.Column("editPermissionScope", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("recordType", new TableInfo.Column("recordType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("linkedNewPersonId", new TableInfo.Column("linkedNewPersonId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("isPendingNewLoan", new TableInfo.Column("isPendingNewLoan", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("previousPersonId", new TableInfo.Column("previousPersonId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPersons = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPersons.add(new TableInfo.ForeignKey("loan_files", "CASCADE", "NO ACTION", Arrays.asList("fileId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPersons = new HashSet<TableInfo.Index>(1);
        _indicesPersons.add(new TableInfo.Index("index_persons_fileId", false, Arrays.asList("fileId"), Arrays.asList("ASC")));
        final TableInfo _infoPersons = new TableInfo("persons", _columnsPersons, _foreignKeysPersons, _indicesPersons);
        final TableInfo _existingPersons = TableInfo.read(db, "persons");
        if (!_infoPersons.equals(_existingPersons)) {
          return new RoomOpenHelper.ValidationResult(false, "persons(com.moneymate.app.data.local.entity.Person).\n"
                  + " Expected:\n" + _infoPersons + "\n"
                  + " Found:\n" + _existingPersons);
        }
        final HashMap<String, TableInfo.Column> _columnsPayments = new HashMap<String, TableInfo.Column>(11);
        _columnsPayments.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("personId", new TableInfo.Column("personId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("isRollover", new TableInfo.Column("isRollover", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("uploadedAt", new TableInfo.Column("uploadedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("editPermissionGranted", new TableInfo.Column("editPermissionGranted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPayments.put("editPermissionScope", new TableInfo.Column("editPermissionScope", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPayments = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPayments.add(new TableInfo.ForeignKey("persons", "CASCADE", "NO ACTION", Arrays.asList("personId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPayments = new HashSet<TableInfo.Index>(1);
        _indicesPayments.add(new TableInfo.Index("index_payments_personId", false, Arrays.asList("personId"), Arrays.asList("ASC")));
        final TableInfo _infoPayments = new TableInfo("payments", _columnsPayments, _foreignKeysPayments, _indicesPayments);
        final TableInfo _existingPayments = TableInfo.read(db, "payments");
        if (!_infoPayments.equals(_existingPayments)) {
          return new RoomOpenHelper.ValidationResult(false, "payments(com.moneymate.app.data.local.entity.Payment).\n"
                  + " Expected:\n" + _infoPayments + "\n"
                  + " Found:\n" + _existingPayments);
        }
        final HashMap<String, TableInfo.Column> _columnsEditRequests = new HashMap<String, TableInfo.Column>(8);
        _columnsEditRequests.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditRequests.put("recordId", new TableInfo.Column("recordId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditRequests.put("recordType", new TableInfo.Column("recordType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditRequests.put("requestedAt", new TableInfo.Column("requestedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditRequests.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditRequests.put("resolvedAt", new TableInfo.Column("resolvedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditRequests.put("scope", new TableInfo.Column("scope", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEditRequests.put("firestoreRequestId", new TableInfo.Column("firestoreRequestId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEditRequests = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEditRequests = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEditRequests = new TableInfo("edit_requests", _columnsEditRequests, _foreignKeysEditRequests, _indicesEditRequests);
        final TableInfo _existingEditRequests = TableInfo.read(db, "edit_requests");
        if (!_infoEditRequests.equals(_existingEditRequests)) {
          return new RoomOpenHelper.ValidationResult(false, "edit_requests(com.moneymate.app.data.local.entity.EditRequest).\n"
                  + " Expected:\n" + _infoEditRequests + "\n"
                  + " Found:\n" + _existingEditRequests);
        }
        final HashMap<String, TableInfo.Column> _columnsDefaultPersons = new HashMap<String, TableInfo.Column>(10);
        _columnsDefaultPersons.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("nlrKey", new TableInfo.Column("nlrKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("place", new TableInfo.Column("place", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("mobileNumber", new TableInfo.Column("mobileNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("amountGiven", new TableInfo.Column("amountGiven", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("recordType", new TableInfo.Column("recordType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDefaultPersons.put("isSeeded", new TableInfo.Column("isSeeded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDefaultPersons = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDefaultPersons = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDefaultPersons = new TableInfo("default_persons", _columnsDefaultPersons, _foreignKeysDefaultPersons, _indicesDefaultPersons);
        final TableInfo _existingDefaultPersons = TableInfo.read(db, "default_persons");
        if (!_infoDefaultPersons.equals(_existingDefaultPersons)) {
          return new RoomOpenHelper.ValidationResult(false, "default_persons(com.moneymate.app.data.local.entity.DefaultPerson).\n"
                  + " Expected:\n" + _infoDefaultPersons + "\n"
                  + " Found:\n" + _existingDefaultPersons);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "61f6b9a0b3bbba31ff88e666d4935fba", "2f835c292a2b8dcd8a16338aeb3bf546");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "loan_files","persons","payments","edit_requests","default_persons");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `loan_files`");
      _db.execSQL("DELETE FROM `persons`");
      _db.execSQL("DELETE FROM `payments`");
      _db.execSQL("DELETE FROM `edit_requests`");
      _db.execSQL("DELETE FROM `default_persons`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(FileDao.class, FileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PersonDao.class, PersonDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PaymentDao.class, PaymentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EditRequestDao.class, EditRequestDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DefaultPersonDao.class, DefaultPersonDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public FileDao fileDao() {
    if (_fileDao != null) {
      return _fileDao;
    } else {
      synchronized(this) {
        if(_fileDao == null) {
          _fileDao = new FileDao_Impl(this);
        }
        return _fileDao;
      }
    }
  }

  @Override
  public PersonDao personDao() {
    if (_personDao != null) {
      return _personDao;
    } else {
      synchronized(this) {
        if(_personDao == null) {
          _personDao = new PersonDao_Impl(this);
        }
        return _personDao;
      }
    }
  }

  @Override
  public PaymentDao paymentDao() {
    if (_paymentDao != null) {
      return _paymentDao;
    } else {
      synchronized(this) {
        if(_paymentDao == null) {
          _paymentDao = new PaymentDao_Impl(this);
        }
        return _paymentDao;
      }
    }
  }

  @Override
  public EditRequestDao editRequestDao() {
    if (_editRequestDao != null) {
      return _editRequestDao;
    } else {
      synchronized(this) {
        if(_editRequestDao == null) {
          _editRequestDao = new EditRequestDao_Impl(this);
        }
        return _editRequestDao;
      }
    }
  }

  @Override
  public DefaultPersonDao defaultPersonDao() {
    if (_defaultPersonDao != null) {
      return _defaultPersonDao;
    } else {
      synchronized(this) {
        if(_defaultPersonDao == null) {
          _defaultPersonDao = new DefaultPersonDao_Impl(this);
        }
        return _defaultPersonDao;
      }
    }
  }
}
