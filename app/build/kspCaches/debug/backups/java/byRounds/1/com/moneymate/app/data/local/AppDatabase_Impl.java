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
import com.moneymate.app.data.local.dao.AppUserDao;
import com.moneymate.app.data.local.dao.AppUserDao_Impl;
import com.moneymate.app.data.local.dao.AreaDao;
import com.moneymate.app.data.local.dao.AreaDao_Impl;
import com.moneymate.app.data.local.dao.AuditLogDao;
import com.moneymate.app.data.local.dao.AuditLogDao_Impl;
import com.moneymate.app.data.local.dao.BookAdjustmentDao;
import com.moneymate.app.data.local.dao.BookAdjustmentDao_Impl;
import com.moneymate.app.data.local.dao.DefaultPersonDao;
import com.moneymate.app.data.local.dao.DefaultPersonDao_Impl;
import com.moneymate.app.data.local.dao.EditRequestDao;
import com.moneymate.app.data.local.dao.EditRequestDao_Impl;
import com.moneymate.app.data.local.dao.ExpenseCategoryDao;
import com.moneymate.app.data.local.dao.ExpenseCategoryDao_Impl;
import com.moneymate.app.data.local.dao.ExpenseDao;
import com.moneymate.app.data.local.dao.ExpenseDao_Impl;
import com.moneymate.app.data.local.dao.FileDao;
import com.moneymate.app.data.local.dao.FileDao_Impl;
import com.moneymate.app.data.local.dao.InvestmentDao;
import com.moneymate.app.data.local.dao.InvestmentDao_Impl;
import com.moneymate.app.data.local.dao.InvestmentTypeDao;
import com.moneymate.app.data.local.dao.InvestmentTypeDao_Impl;
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

  private volatile BookAdjustmentDao _bookAdjustmentDao;

  private volatile ExpenseDao _expenseDao;

  private volatile InvestmentDao _investmentDao;

  private volatile ExpenseCategoryDao _expenseCategoryDao;

  private volatile InvestmentTypeDao _investmentTypeDao;

  private volatile AreaDao _areaDao;

  private volatile AppUserDao _appUserDao;

  private volatile AuditLogDao _auditLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(16) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `loan_files` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `syncedToFirebase` INTEGER NOT NULL, `lastUploadedAt` INTEGER, `defaultInterestRate` REAL NOT NULL, `defaultCalculationMode` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `persons` (`id` TEXT NOT NULL, `fileId` TEXT NOT NULL, `name` TEXT NOT NULL, `place` TEXT, `mobileNumber` TEXT, `amountGiven` REAL NOT NULL, `mode` TEXT NOT NULL, `dateGiven` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `uploadedAt` INTEGER, `editPermissionGranted` INTEGER NOT NULL, `editPermissionScope` TEXT NOT NULL, `recordType` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `completedAt` INTEGER, `linkedNewPersonId` TEXT, `isPendingNewLoan` INTEGER NOT NULL, `previousPersonId` TEXT, `interestRate` REAL NOT NULL, `interestType` TEXT NOT NULL, `interestAmount` REAL NOT NULL, `totalRepayment` REAL NOT NULL, `loanType` TEXT NOT NULL, `numberOfInstallments` INTEGER NOT NULL, `perInstallmentAmount` REAL NOT NULL, `isDurationBased` INTEGER NOT NULL, `durationDays` INTEGER, `photoUri` TEXT, `alternateMobile` TEXT, `address` TEXT, `businessType` TEXT, `maxLoanAmount` REAL, `guarantorPersonId` TEXT, `customerCode` TEXT, `subCode` TEXT, `badLoanDays` INTEGER NOT NULL, `sendSms` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`fileId`) REFERENCES `loan_files`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_persons_fileId` ON `persons` (`fileId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `payments` (`id` TEXT NOT NULL, `personId` TEXT NOT NULL, `amount` REAL NOT NULL, `mode` TEXT NOT NULL, `date` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `isRollover` INTEGER NOT NULL, `uploadedAt` INTEGER, `editPermissionGranted` INTEGER NOT NULL, `editPermissionScope` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`personId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_personId` ON `payments` (`personId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `edit_requests` (`id` TEXT NOT NULL, `recordId` TEXT NOT NULL, `recordType` TEXT NOT NULL, `requestedAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `resolvedAt` INTEGER, `scope` TEXT NOT NULL, `firestoreRequestId` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `default_persons` (`id` TEXT NOT NULL, `nlrKey` TEXT NOT NULL, `name` TEXT NOT NULL, `place` TEXT, `mobileNumber` TEXT, `amountGiven` REAL NOT NULL, `mode` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `recordType` TEXT NOT NULL, `isSeeded` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `book_adjustments` (`id` TEXT NOT NULL, `personId` TEXT NOT NULL, `fileId` TEXT NOT NULL, `discrepancyAmount` REAL NOT NULL, `type` TEXT NOT NULL, `reason` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `approvedByAdmin` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `isOnline` INTEGER NOT NULL, `date` INTEGER NOT NULL, `notes` TEXT, `isDeleted` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`fileId`) REFERENCES `loan_files`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_fileId` ON `expenses` (`fileId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `investments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `isOnline` INTEGER NOT NULL, `date` INTEGER NOT NULL, `notes` TEXT, `isDeleted` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`fileId`) REFERENCES `loan_files`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_investments_fileId` ON `investments` (`fileId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `expense_categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isDefault` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `investment_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isDefault` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `areas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` TEXT NOT NULL, `name` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_areas_fileId` ON `areas` (`fileId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `displayName` TEXT NOT NULL, `role` TEXT NOT NULL, `assignedFileIds` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `createdByUserId` INTEGER NOT NULL, `pinHash` TEXT, `lastLoginAt` INTEGER, `isDeleted` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_app_users_email` ON `app_users` (`email`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `audit_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `userEmail` TEXT NOT NULL, `action` TEXT NOT NULL, `targetType` TEXT NOT NULL, `targetId` TEXT NOT NULL, `targetLabel` TEXT NOT NULL, `details` TEXT, `timestamp` INTEGER NOT NULL, `fileId` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_userId` ON `audit_logs` (`userId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_fileId` ON `audit_logs` (`fileId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_targetType_targetId` ON `audit_logs` (`targetType`, `targetId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_logs_timestamp` ON `audit_logs` (`timestamp`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e6147d729d0620fb3be6812803696041')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `loan_files`");
        db.execSQL("DROP TABLE IF EXISTS `persons`");
        db.execSQL("DROP TABLE IF EXISTS `payments`");
        db.execSQL("DROP TABLE IF EXISTS `edit_requests`");
        db.execSQL("DROP TABLE IF EXISTS `default_persons`");
        db.execSQL("DROP TABLE IF EXISTS `book_adjustments`");
        db.execSQL("DROP TABLE IF EXISTS `expenses`");
        db.execSQL("DROP TABLE IF EXISTS `investments`");
        db.execSQL("DROP TABLE IF EXISTS `expense_categories`");
        db.execSQL("DROP TABLE IF EXISTS `investment_types`");
        db.execSQL("DROP TABLE IF EXISTS `areas`");
        db.execSQL("DROP TABLE IF EXISTS `app_users`");
        db.execSQL("DROP TABLE IF EXISTS `audit_logs`");
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
        final HashMap<String, TableInfo.Column> _columnsLoanFiles = new HashMap<String, TableInfo.Column>(10);
        _columnsLoanFiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("deletedAt", new TableInfo.Column("deletedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("syncedToFirebase", new TableInfo.Column("syncedToFirebase", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("lastUploadedAt", new TableInfo.Column("lastUploadedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("defaultInterestRate", new TableInfo.Column("defaultInterestRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoanFiles.put("defaultCalculationMode", new TableInfo.Column("defaultCalculationMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLoanFiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLoanFiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLoanFiles = new TableInfo("loan_files", _columnsLoanFiles, _foreignKeysLoanFiles, _indicesLoanFiles);
        final TableInfo _existingLoanFiles = TableInfo.read(db, "loan_files");
        if (!_infoLoanFiles.equals(_existingLoanFiles)) {
          return new RoomOpenHelper.ValidationResult(false, "loan_files(com.moneymate.app.data.local.entity.LoanFile).\n"
                  + " Expected:\n" + _infoLoanFiles + "\n"
                  + " Found:\n" + _existingLoanFiles);
        }
        final HashMap<String, TableInfo.Column> _columnsPersons = new HashMap<String, TableInfo.Column>(39);
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
        _columnsPersons.put("interestRate", new TableInfo.Column("interestRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("interestType", new TableInfo.Column("interestType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("interestAmount", new TableInfo.Column("interestAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("totalRepayment", new TableInfo.Column("totalRepayment", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("loanType", new TableInfo.Column("loanType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("numberOfInstallments", new TableInfo.Column("numberOfInstallments", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("perInstallmentAmount", new TableInfo.Column("perInstallmentAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("isDurationBased", new TableInfo.Column("isDurationBased", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("durationDays", new TableInfo.Column("durationDays", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("photoUri", new TableInfo.Column("photoUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("alternateMobile", new TableInfo.Column("alternateMobile", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("address", new TableInfo.Column("address", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("businessType", new TableInfo.Column("businessType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("maxLoanAmount", new TableInfo.Column("maxLoanAmount", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("guarantorPersonId", new TableInfo.Column("guarantorPersonId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("customerCode", new TableInfo.Column("customerCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("subCode", new TableInfo.Column("subCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("badLoanDays", new TableInfo.Column("badLoanDays", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("sendSms", new TableInfo.Column("sendSms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
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
        final HashMap<String, TableInfo.Column> _columnsBookAdjustments = new HashMap<String, TableInfo.Column>(8);
        _columnsBookAdjustments.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookAdjustments.put("personId", new TableInfo.Column("personId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookAdjustments.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookAdjustments.put("discrepancyAmount", new TableInfo.Column("discrepancyAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookAdjustments.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookAdjustments.put("reason", new TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookAdjustments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookAdjustments.put("approvedByAdmin", new TableInfo.Column("approvedByAdmin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBookAdjustments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBookAdjustments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBookAdjustments = new TableInfo("book_adjustments", _columnsBookAdjustments, _foreignKeysBookAdjustments, _indicesBookAdjustments);
        final TableInfo _existingBookAdjustments = TableInfo.read(db, "book_adjustments");
        if (!_infoBookAdjustments.equals(_existingBookAdjustments)) {
          return new RoomOpenHelper.ValidationResult(false, "book_adjustments(com.moneymate.app.data.local.entity.BookAdjustment).\n"
                  + " Expected:\n" + _infoBookAdjustments + "\n"
                  + " Found:\n" + _existingBookAdjustments);
        }
        final HashMap<String, TableInfo.Column> _columnsExpenses = new HashMap<String, TableInfo.Column>(9);
        _columnsExpenses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("isOnline", new TableInfo.Column("isOnline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExpenses = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysExpenses.add(new TableInfo.ForeignKey("loan_files", "CASCADE", "NO ACTION", Arrays.asList("fileId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExpenses = new HashSet<TableInfo.Index>(1);
        _indicesExpenses.add(new TableInfo.Index("index_expenses_fileId", false, Arrays.asList("fileId"), Arrays.asList("ASC")));
        final TableInfo _infoExpenses = new TableInfo("expenses", _columnsExpenses, _foreignKeysExpenses, _indicesExpenses);
        final TableInfo _existingExpenses = TableInfo.read(db, "expenses");
        if (!_infoExpenses.equals(_existingExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "expenses(com.moneymate.app.data.local.entity.Expense).\n"
                  + " Expected:\n" + _infoExpenses + "\n"
                  + " Found:\n" + _existingExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsInvestments = new HashMap<String, TableInfo.Column>(9);
        _columnsInvestments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("isOnline", new TableInfo.Column("isOnline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInvestments = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysInvestments.add(new TableInfo.ForeignKey("loan_files", "CASCADE", "NO ACTION", Arrays.asList("fileId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesInvestments = new HashSet<TableInfo.Index>(1);
        _indicesInvestments.add(new TableInfo.Index("index_investments_fileId", false, Arrays.asList("fileId"), Arrays.asList("ASC")));
        final TableInfo _infoInvestments = new TableInfo("investments", _columnsInvestments, _foreignKeysInvestments, _indicesInvestments);
        final TableInfo _existingInvestments = TableInfo.read(db, "investments");
        if (!_infoInvestments.equals(_existingInvestments)) {
          return new RoomOpenHelper.ValidationResult(false, "investments(com.moneymate.app.data.local.entity.Investment).\n"
                  + " Expected:\n" + _infoInvestments + "\n"
                  + " Found:\n" + _existingInvestments);
        }
        final HashMap<String, TableInfo.Column> _columnsExpenseCategories = new HashMap<String, TableInfo.Column>(3);
        _columnsExpenseCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenseCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenseCategories.put("isDefault", new TableInfo.Column("isDefault", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExpenseCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExpenseCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExpenseCategories = new TableInfo("expense_categories", _columnsExpenseCategories, _foreignKeysExpenseCategories, _indicesExpenseCategories);
        final TableInfo _existingExpenseCategories = TableInfo.read(db, "expense_categories");
        if (!_infoExpenseCategories.equals(_existingExpenseCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "expense_categories(com.moneymate.app.data.local.entity.ExpenseCategory).\n"
                  + " Expected:\n" + _infoExpenseCategories + "\n"
                  + " Found:\n" + _existingExpenseCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsInvestmentTypes = new HashMap<String, TableInfo.Column>(3);
        _columnsInvestmentTypes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestmentTypes.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestmentTypes.put("isDefault", new TableInfo.Column("isDefault", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInvestmentTypes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInvestmentTypes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInvestmentTypes = new TableInfo("investment_types", _columnsInvestmentTypes, _foreignKeysInvestmentTypes, _indicesInvestmentTypes);
        final TableInfo _existingInvestmentTypes = TableInfo.read(db, "investment_types");
        if (!_infoInvestmentTypes.equals(_existingInvestmentTypes)) {
          return new RoomOpenHelper.ValidationResult(false, "investment_types(com.moneymate.app.data.local.entity.InvestmentType).\n"
                  + " Expected:\n" + _infoInvestmentTypes + "\n"
                  + " Found:\n" + _existingInvestmentTypes);
        }
        final HashMap<String, TableInfo.Column> _columnsAreas = new HashMap<String, TableInfo.Column>(5);
        _columnsAreas.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAreas.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAreas.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAreas.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAreas.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAreas = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAreas = new HashSet<TableInfo.Index>(1);
        _indicesAreas.add(new TableInfo.Index("index_areas_fileId", false, Arrays.asList("fileId"), Arrays.asList("ASC")));
        final TableInfo _infoAreas = new TableInfo("areas", _columnsAreas, _foreignKeysAreas, _indicesAreas);
        final TableInfo _existingAreas = TableInfo.read(db, "areas");
        if (!_infoAreas.equals(_existingAreas)) {
          return new RoomOpenHelper.ValidationResult(false, "areas(com.moneymate.app.data.local.entity.Area).\n"
                  + " Expected:\n" + _infoAreas + "\n"
                  + " Found:\n" + _existingAreas);
        }
        final HashMap<String, TableInfo.Column> _columnsAppUsers = new HashMap<String, TableInfo.Column>(11);
        _columnsAppUsers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("assignedFileIds", new TableInfo.Column("assignedFileIds", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("createdByUserId", new TableInfo.Column("createdByUserId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("pinHash", new TableInfo.Column("pinHash", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("lastLoginAt", new TableInfo.Column("lastLoginAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsers.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppUsers = new HashSet<TableInfo.Index>(1);
        _indicesAppUsers.add(new TableInfo.Index("index_app_users_email", true, Arrays.asList("email"), Arrays.asList("ASC")));
        final TableInfo _infoAppUsers = new TableInfo("app_users", _columnsAppUsers, _foreignKeysAppUsers, _indicesAppUsers);
        final TableInfo _existingAppUsers = TableInfo.read(db, "app_users");
        if (!_infoAppUsers.equals(_existingAppUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "app_users(com.moneymate.app.data.local.entity.AppUser).\n"
                  + " Expected:\n" + _infoAppUsers + "\n"
                  + " Found:\n" + _existingAppUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsAuditLogs = new HashMap<String, TableInfo.Column>(10);
        _columnsAuditLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("userId", new TableInfo.Column("userId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("userEmail", new TableInfo.Column("userEmail", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("action", new TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("targetType", new TableInfo.Column("targetType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("targetId", new TableInfo.Column("targetId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("targetLabel", new TableInfo.Column("targetLabel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("details", new TableInfo.Column("details", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAuditLogs.put("fileId", new TableInfo.Column("fileId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAuditLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAuditLogs = new HashSet<TableInfo.Index>(4);
        _indicesAuditLogs.add(new TableInfo.Index("index_audit_logs_userId", false, Arrays.asList("userId"), Arrays.asList("ASC")));
        _indicesAuditLogs.add(new TableInfo.Index("index_audit_logs_fileId", false, Arrays.asList("fileId"), Arrays.asList("ASC")));
        _indicesAuditLogs.add(new TableInfo.Index("index_audit_logs_targetType_targetId", false, Arrays.asList("targetType", "targetId"), Arrays.asList("ASC", "ASC")));
        _indicesAuditLogs.add(new TableInfo.Index("index_audit_logs_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        final TableInfo _infoAuditLogs = new TableInfo("audit_logs", _columnsAuditLogs, _foreignKeysAuditLogs, _indicesAuditLogs);
        final TableInfo _existingAuditLogs = TableInfo.read(db, "audit_logs");
        if (!_infoAuditLogs.equals(_existingAuditLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "audit_logs(com.moneymate.app.data.local.entity.AuditLog).\n"
                  + " Expected:\n" + _infoAuditLogs + "\n"
                  + " Found:\n" + _existingAuditLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "e6147d729d0620fb3be6812803696041", "75ed84c18908cf0e371c4e651c989fcd");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "loan_files","persons","payments","edit_requests","default_persons","book_adjustments","expenses","investments","expense_categories","investment_types","areas","app_users","audit_logs");
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
      _db.execSQL("DELETE FROM `book_adjustments`");
      _db.execSQL("DELETE FROM `expenses`");
      _db.execSQL("DELETE FROM `investments`");
      _db.execSQL("DELETE FROM `expense_categories`");
      _db.execSQL("DELETE FROM `investment_types`");
      _db.execSQL("DELETE FROM `areas`");
      _db.execSQL("DELETE FROM `app_users`");
      _db.execSQL("DELETE FROM `audit_logs`");
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
    _typeConvertersMap.put(BookAdjustmentDao.class, BookAdjustmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExpenseDao.class, ExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InvestmentDao.class, InvestmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExpenseCategoryDao.class, ExpenseCategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InvestmentTypeDao.class, InvestmentTypeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AreaDao.class, AreaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppUserDao.class, AppUserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AuditLogDao.class, AuditLogDao_Impl.getRequiredConverters());
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

  @Override
  public BookAdjustmentDao bookAdjustmentDao() {
    if (_bookAdjustmentDao != null) {
      return _bookAdjustmentDao;
    } else {
      synchronized(this) {
        if(_bookAdjustmentDao == null) {
          _bookAdjustmentDao = new BookAdjustmentDao_Impl(this);
        }
        return _bookAdjustmentDao;
      }
    }
  }

  @Override
  public ExpenseDao expenseDao() {
    if (_expenseDao != null) {
      return _expenseDao;
    } else {
      synchronized(this) {
        if(_expenseDao == null) {
          _expenseDao = new ExpenseDao_Impl(this);
        }
        return _expenseDao;
      }
    }
  }

  @Override
  public InvestmentDao investmentDao() {
    if (_investmentDao != null) {
      return _investmentDao;
    } else {
      synchronized(this) {
        if(_investmentDao == null) {
          _investmentDao = new InvestmentDao_Impl(this);
        }
        return _investmentDao;
      }
    }
  }

  @Override
  public ExpenseCategoryDao expenseCategoryDao() {
    if (_expenseCategoryDao != null) {
      return _expenseCategoryDao;
    } else {
      synchronized(this) {
        if(_expenseCategoryDao == null) {
          _expenseCategoryDao = new ExpenseCategoryDao_Impl(this);
        }
        return _expenseCategoryDao;
      }
    }
  }

  @Override
  public InvestmentTypeDao investmentTypeDao() {
    if (_investmentTypeDao != null) {
      return _investmentTypeDao;
    } else {
      synchronized(this) {
        if(_investmentTypeDao == null) {
          _investmentTypeDao = new InvestmentTypeDao_Impl(this);
        }
        return _investmentTypeDao;
      }
    }
  }

  @Override
  public AreaDao areaDao() {
    if (_areaDao != null) {
      return _areaDao;
    } else {
      synchronized(this) {
        if(_areaDao == null) {
          _areaDao = new AreaDao_Impl(this);
        }
        return _areaDao;
      }
    }
  }

  @Override
  public AppUserDao appUserDao() {
    if (_appUserDao != null) {
      return _appUserDao;
    } else {
      synchronized(this) {
        if(_appUserDao == null) {
          _appUserDao = new AppUserDao_Impl(this);
        }
        return _appUserDao;
      }
    }
  }

  @Override
  public AuditLogDao auditLogDao() {
    if (_auditLogDao != null) {
      return _auditLogDao;
    } else {
      synchronized(this) {
        if(_auditLogDao == null) {
          _auditLogDao = new AuditLogDao_Impl(this);
        }
        return _auditLogDao;
      }
    }
  }
}
