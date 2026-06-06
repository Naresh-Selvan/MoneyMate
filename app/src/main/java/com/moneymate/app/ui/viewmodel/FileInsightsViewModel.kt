package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.repository.PaymentRepository
import com.moneymate.app.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class FileInsightsData(
    val todayGiven: Double = 0.0,
    val todayReceived: Double = 0.0,
    val todayNet: Double = 0.0,
    val weekGiven: Double = 0.0,
    val weekReceived: Double = 0.0,
    val weekNet: Double = 0.0,
    val allTimeGiven: Double = 0.0,
    val allTimeReceived: Double = 0.0,
    val outstanding: Double = 0.0,
    val activeLoanCount: Int = 0,
    val completedLoanCount: Int = 0
)

@HiltViewModel
class FileInsightsViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _insights = MutableStateFlow(FileInsightsData())
    val insights: StateFlow<FileInsightsData> = _insights

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadInsights(fileId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val now = System.currentTimeMillis()
                val cal = Calendar.getInstance()

                // Today's boundaries
                cal.timeInMillis = now
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val todayStart = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                val todayEnd = cal.timeInMillis

                // This week's boundaries (start of week = Monday)
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val weekStart = cal.timeInMillis
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                val weekEnd = cal.timeInMillis

                val allTimeGiven = personRepository.getTotalGivenInFile(fileId)
                val allTimeReceived = paymentRepository.getTotalReceivedInFile(fileId)
                val todayGiven = personRepository.getTotalGivenToday(fileId, todayStart, todayEnd)
                val todayReceived = paymentRepository.getTotalReceivedToday(fileId, todayStart, todayEnd)
                val weekGivenCalc = personRepository.getTotalGivenToday(fileId, weekStart, weekEnd)
                val weekReceived = paymentRepository.getTotalReceivedThisWeek(fileId, weekStart, weekEnd)
                val outstanding = personRepository.getTotalOutstanding(fileId)
                val activeCount = personRepository.getActiveLoanCount(fileId)
                val completedCount = personRepository.getCompletedLoanCount(fileId)

                _insights.value = FileInsightsData(
                    todayGiven = todayGiven,
                    todayReceived = todayReceived,
                    todayNet = todayGiven - todayReceived,
                    weekGiven = weekGivenCalc,
                    weekReceived = weekReceived,
                    weekNet = weekGivenCalc - weekReceived,
                    allTimeGiven = allTimeGiven,
                    allTimeReceived = allTimeReceived,
                    outstanding = outstanding,
                    activeLoanCount = activeCount,
                    completedLoanCount = completedCount
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
