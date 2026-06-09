package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.AreaDao
import com.moneymate.app.data.local.entity.Area
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaRepository @Inject constructor(
    private val areaDao: AreaDao
) {
    fun getAreasByFile(fileId: String): Flow<List<Area>> = areaDao.getAreasByFile(fileId)

    suspend fun getAreasByFileOnce(fileId: String): List<Area> = areaDao.getAreasByFileOnce(fileId)

    fun getAreaNames(fileId: String): Flow<List<String>> = areaDao.getAreaNames(fileId)

    suspend fun getAreaNamesOnce(fileId: String): List<String> = areaDao.getAreaNamesOnce(fileId)

    suspend fun insert(area: Area): Long = areaDao.insert(area)

    suspend fun update(area: Area) = areaDao.update(area)

    suspend fun softDelete(id: Long) = areaDao.softDelete(id)

    suspend fun restore(id: Long) = areaDao.restore(id)

    suspend fun hardDelete(id: Long) = areaDao.hardDelete(id)

    suspend fun updateSortOrder(id: Long, sortOrder: Int) = areaDao.updateSortOrder(id, sortOrder)

    suspend fun getAreaByName(fileId: String, name: String): Area? = areaDao.getAreaByName(fileId, name)
}
