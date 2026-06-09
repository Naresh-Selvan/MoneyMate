package com.moneymate.app.di

import com.moneymate.app.data.local.dao.AppUserDao
import com.moneymate.app.data.local.dao.AuditLogDao
import com.moneymate.app.data.repository.AreaRepository
import com.moneymate.app.data.repository.PersonRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for accessing singleton-scoped repositories from composables
 * that do not have a ViewModel wrapper. Use via [EntryPointAccessors.fromApplication].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun areaRepository(): AreaRepository
    fun personRepository(): PersonRepository
    fun appUserDao(): AppUserDao
    fun auditLogDao(): AuditLogDao
}
