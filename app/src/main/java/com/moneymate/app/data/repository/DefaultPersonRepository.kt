package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.DefaultPersonDao
import com.moneymate.app.data.local.entity.DefaultPerson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPersonRepository @Inject constructor(
    private val dao: DefaultPersonDao
) {
    fun getForNlr(nlrKey: String): Flow<List<DefaultPerson>> = dao.getByNlrKey(nlrKey)

    suspend fun getForNlrOnce(nlrKey: String): List<DefaultPerson> = dao.getByNlrKeyOnce(nlrKey)

    suspend fun countForNlr(nlrKey: String): Int = dao.countForNlr(nlrKey)

    suspend fun insert(person: DefaultPerson) = dao.insert(person)

    suspend fun insertAll(persons: List<DefaultPerson>) = dao.insertAll(persons)

    suspend fun delete(person: DefaultPerson) = dao.delete(person)

    suspend fun deleteAllForNlr(nlrKey: String) = dao.deleteAllForNlr(nlrKey)

    /** Called after upload — replaces the template for [nlrKey] with current file snapshot */
    suspend fun snapshotFromPersons(
        nlrKey: String,
        persons: List<com.moneymate.app.data.local.entity.Person>
    ) {
        dao.deleteAllForNlr(nlrKey)
        val defaults = persons.mapIndexed { idx, p ->
            DefaultPerson(
                nlrKey = nlrKey,
                name = p.name,
                place = p.place,
                mobileNumber = p.mobileNumber,
                amountGiven = p.amountGiven,
                mode = p.mode,
                sortOrder = idx,
                recordType = p.recordType,
                isSeeded = false
            )
        }
        dao.insertAll(defaults)
    }
}