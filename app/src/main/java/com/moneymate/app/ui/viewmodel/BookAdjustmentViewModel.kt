package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.repository.BookAdjustmentRepository
import com.moneymate.app.data.local.entity.BookAdjustment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookAdjustmentViewModel @Inject constructor(
    private val repository: BookAdjustmentRepository
) : ViewModel() {
    fun insert(adjustment: BookAdjustment) = viewModelScope.launch { repository.insert(adjustment) }
}
