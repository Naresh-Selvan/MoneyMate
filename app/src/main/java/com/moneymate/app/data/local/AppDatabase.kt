package com.moneymate.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.local.entity.*

@Database(
    entities = [
        LoanFile::class,
        Person::class,
        Payment::class,
        EditRequest::class,
        DefaultPerson::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun personDao(): PersonDao
    abstract fun paymentDao(): PaymentDao
    abstract fun editRequestDao(): EditRequestDao
    abstract fun defaultPersonDao(): DefaultPersonDao
}