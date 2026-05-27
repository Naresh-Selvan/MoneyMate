package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.entity.DefaultPerson
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.data.repository.DefaultPersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val repo: DefaultPersonRepository
) : ViewModel() {

    val nlrKeys = listOf("NLR 1", "NLR 2", "NLR 3", "NLR 4")

    fun getForNlr(nlrKey: String): StateFlow<List<DefaultPerson>> =
        repo.getForNlr(nlrKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPerson(nlrKey: String, name: String, place: String?, mobile: String?, sortOrder: Int) =
        viewModelScope.launch {
            repo.insert(
                DefaultPerson(
                    nlrKey = nlrKey,
                    name = name.trim(),
                    place = place?.trim()?.ifEmpty { null },
                    mobileNumber = mobile?.trim()?.ifEmpty { null },
                    amountGiven = 0.0,
                    mode = PaymentMode.CASH,
                    sortOrder = sortOrder,
                    recordType = LoanType.LENDING,
                    isSeeded = false
                )
            )
        }

    fun deletePerson(person: DefaultPerson) = viewModelScope.launch { repo.delete(person) }

    fun clearAll(nlrKey: String) = viewModelScope.launch { repo.deleteAllForNlr(nlrKey) }
}