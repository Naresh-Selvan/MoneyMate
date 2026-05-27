package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.DefaultPerson
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultPersonDao {

    @Query("SELECT * FROM default_persons WHERE nlrKey = :nlrKey ORDER BY sortOrder ASC")
    fun getByNlrKey(nlrKey: String): Flow<List<DefaultPerson>>

    @Query("SELECT * FROM default_persons WHERE nlrKey = :nlrKey ORDER BY sortOrder ASC")
    suspend fun getByNlrKeyOnce(nlrKey: String): List<DefaultPerson>

    @Query("SELECT COUNT(*) FROM default_persons WHERE nlrKey = :nlrKey")
    suspend fun countForNlr(nlrKey: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(persons: List<DefaultPerson>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: DefaultPerson)

    @Delete
    suspend fun delete(person: DefaultPerson)

    @Query("DELETE FROM default_persons WHERE nlrKey = :nlrKey")
    suspend fun deleteAllForNlr(nlrKey: String)

    @Query("UPDATE default_persons SET name=:name, place=:place, mobileNumber=:mobile, amountGiven=:amount, mode=:mode, recordType=:recordType WHERE id=:id")
    suspend fun update(id: String, name: String, place: String?, mobile: String?, amount: Double, mode: String, recordType: String)
}