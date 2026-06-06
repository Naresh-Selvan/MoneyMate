package com.moneymate.app.utils

import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────────────
// Data models
// ─────────────────────────────────────────────────────────────────────────────

data class EmiInstallment(
    val installmentNumber: Int,
    val dueDate: Long,           // epoch ms
    val expectedAmount: Double,
    val status: EmiStatus
)

enum class EmiStatus { PAID, PARTIAL, MISSED, UPCOMING, TODAY }

// ─────────────────────────────────────────────────────────────────────────────
// Engine
// ─────────────────────────────────────────────────────────────────────────────

object EmiScheduleEngine {

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Returns a Calendar normalised to midnight (00:00:00.000) in the device's
     * default time-zone for the given epoch millisecond value.
     */
    private fun dayStart(epochMs: Long): Calendar =
        Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    /** True when [a] and [b] fall on the same calendar day. */
    private fun isSameDay(a: Long, b: Long): Boolean {
        val ca = dayStart(a)
        val cb = dayStart(b)
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
                ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Calculates the due date for installment [n] (1-based) according to the
     * person's [Person.loanType].
     */
    private fun dueDateFor(person: Person, n: Int): Long {
        return when (person.loanType.uppercase()) {
            "DAILY" -> person.dateGiven + n * TimeUnit.DAYS.toMillis(1)
            "WEEKLY" -> person.dateGiven + n * TimeUnit.DAYS.toMillis(7)
            else -> { // MONTHLY
                Calendar.getInstance().apply {
                    timeInMillis = person.dateGiven
                    add(Calendar.MONTH, n)
                }.timeInMillis
            }
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generates a full EMI schedule for [person] and maps each installment's
     * payment status against the supplied [payments] list.
     *
     * Matching rule: a payment is matched to an installment when the payment
     * date falls on the same calendar day as the installment's due date.
     * Only non-deleted payments are considered for status evaluation.
     */
    fun generateSchedule(person: Person, payments: List<Payment>): List<EmiInstallment> {
        val todayMs = System.currentTimeMillis()
        val activePayments = payments.filter { !it.isDeleted }

        return (1..person.numberOfInstallments).map { n ->
            val dueDate = dueDateFor(person, n)
            val expected = person.perInstallmentAmount

            // Sum all active payments that fall on the same calendar day as dueDate
            val paidOnDay = activePayments
                .filter { isSameDay(it.date, dueDate) }
                .sumOf { it.amount }

            val status = when {
                paidOnDay >= expected && expected > 0.0 -> EmiStatus.PAID
                paidOnDay > 0.0 -> EmiStatus.PARTIAL
                isSameDay(dueDate, todayMs) -> EmiStatus.TODAY
                dueDate < dayStart(todayMs).timeInMillis -> EmiStatus.MISSED
                else -> EmiStatus.UPCOMING
            }

            EmiInstallment(
                installmentNumber = n,
                dueDate = dueDate,
                expectedAmount = expected,
                status = status
            )
        }
    }

    /**
     * Returns the number of days elapsed since the earliest MISSED installment.
     * Returns 0 if there are no missed installments.
     */
    fun getOverdueDays(person: Person, payments: List<Payment>): Int {
        val schedule = generateSchedule(person, payments)
        val earliestMissedDueDate = schedule
            .filter { it.status == EmiStatus.MISSED }
            .minOfOrNull { it.dueDate }
            ?: return 0

        val diffMs = System.currentTimeMillis() - earliestMissedDueDate
        return TimeUnit.MILLISECONDS.toDays(diffMs).toInt().coerceAtLeast(0)
    }

    /**
     * Returns the outstanding balance for a person:
     *   totalRepayment − Σ(non-deleted payment amounts)
     */
    fun getPendingBalance(person: Person, payments: List<Payment>): Double {
        val totalCollected = payments
            .filter { !it.isDeleted }
            .sumOf { it.amount }
        return (person.totalRepayment - totalCollected).coerceAtLeast(0.0)
    }

    /**
     * Returns the collection efficiency as a percentage across all [persons]:
     *   (totalCollected / totalExpected) × 100
     *
     * Only non-deleted persons and non-deleted payments are considered.
     * Returns 0.0 when [persons] is empty or total expected is zero.
     */
    fun getCollectionEfficiency(persons: List<Person>, allPayments: List<Payment>): Double {
        val activePersons = persons.filter { !it.isDeleted }
        if (activePersons.isEmpty()) return 0.0

        val totalExpected = activePersons.sumOf { it.totalRepayment }
        if (totalExpected == 0.0) return 0.0

        val activePersonIds = activePersons.map { it.id }.toSet()
        val totalCollected = allPayments
            .filter { !it.isDeleted && it.personId in activePersonIds }
            .sumOf { it.amount }

        return (totalCollected / totalExpected) * 100.0
    }
}
