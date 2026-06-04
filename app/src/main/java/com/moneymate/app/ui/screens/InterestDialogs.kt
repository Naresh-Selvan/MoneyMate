package com.moneymate.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.CalculationMode

// ══════════════════════════════════════════════════════════════════════════════
// Interest calculation helpers
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Calculate flat-rate interest.
 * Interest = principal × (rate / 100)
 */
fun calcFlatInterest(principal: Double, rate: Double): Double =
    principal * (rate / 100.0)

/**
 * Calculate duration-based interest.
 * Interest = principal × (rate / 100) × (days / 365)
 */
fun calcDurationInterest(principal: Double, rate: Double, days: Int): Double =
    principal * (rate / 100.0) * (days / 365.0)

/**
 * Return the default installment count for a given loan type string.
 */
fun defaultInstallmentsForType(loanType: String): Int = when (loanType) {
    "DAILY"   -> 100
    "WEEKLY"  -> 20
    else      -> 10   // MONTHLY
}

// ══════════════════════════════════════════════════════════════════════════════
// Part 1 — File creation: second dialog asking for interest rate & mode
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Shown immediately after the user enters the file name and taps "Next".
 * Captures [defaultInterestRate] and [defaultCalculationMode] for the new file.
 *
 * @param onConfirm Called with (rate, mode) when user taps "Create File"
 * @param onDismiss Called when user taps "Skip" or back
 */
@Composable
fun FileInterestSetupDialog(
    onConfirm: (rate: Double, mode: CalculationMode) -> Unit,
    onDismiss: () -> Unit
) {
    var rateText by remember { mutableStateOf("25") }
    var mode     by remember { mutableStateOf(CalculationMode.FLAT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Default Interest Rate") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This rate will apply to all new persons added to this file. " +
                    "Each person can override it individually.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Default Interest Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("%", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)) }
                )
                Text("Calculation mode", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == CalculationMode.FLAT,
                        onClick  = { mode = CalculationMode.FLAT },
                        label    = { Text("Flat Rate") }
                    )
                    FilterChip(
                        selected = mode == CalculationMode.DURATION,
                        onClick  = { mode = CalculationMode.DURATION },
                        label    = { Text("Duration Based") }
                    )
                }
                if (mode == CalculationMode.DURATION) {
                    Text(
                        "Duration-based: Interest = Principal × Rate × (Days/365)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Flat rate: Interest = Principal × Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val rate = rateText.toDoubleOrNull() ?: 25.0
                onConfirm(rate, mode)
            }) { Text("Create File") }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(25.0, CalculationMode.FLAT) }) { Text("Skip (use 25%)") }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// Part 2 — File-level interest settings dialog (from 3-dot menu inside file)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Shown from the 3-dot menu inside FileDetailScreen.
 * Lets the user change the file's default interest rate and mode.
 * Does NOT retroactively update existing persons.
 */
@Composable
fun FileInterestSettingsDialog(
    currentRate: Double,
    currentMode: CalculationMode,
    onConfirm: (rate: Double, mode: CalculationMode) -> Unit,
    onDismiss: () -> Unit
) {
    var rateText by remember(currentRate) { mutableStateOf(currentRate.toBigDecimal().stripTrailingZeros().toPlainString()) }
    var mode     by remember(currentMode) { mutableStateOf(currentMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File Interest Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Changes the default rate for new persons only. Existing persons are not affected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Default Interest Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("%", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)) }
                )
                Text("Calculation mode", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == CalculationMode.FLAT,
                        onClick  = { mode = CalculationMode.FLAT },
                        label    = { Text("Flat Rate") }
                    )
                    FilterChip(
                        selected = mode == CalculationMode.DURATION,
                        onClick  = { mode = CalculationMode.DURATION },
                        label    = { Text("Duration Based") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val rate = rateText.toDoubleOrNull() ?: currentRate
                onConfirm(rate, mode)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// Part 3 — Per-person loan amount + interest input dialog
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Rich loan amount entry dialog shown when adding a new person.
 *
 * Features:
 * - Amount field (principal)
 * - Interest rate field pre-filled with [fileDefaultRate]
 * - Live calculation: interest amount, total repayment, per installment
 * - Loan type selector (Daily/Weekly/Monthly) with auto-suggested installments
 * - Advanced section (collapsed by default): duration-based calculation
 *
 * @param fileDefaultRate   Pre-fill rate from file settings
 * @param fileDefaultMode   Pre-fill calculation mode from file settings
 * @param onConfirm         Called with all captured interest fields
 * @param onDismiss         Cancel callback
 */
data class InterestInputResult(
    val principal: Double,
    val interestRate: Double,
    val interestAmount: Double,
    val totalRepayment: Double,
    val loanType: String,
    val numberOfInstallments: Int,
    val perInstallmentAmount: Double,
    val isDurationBased: Boolean,
    val durationDays: Int?
)

@Composable
fun LoanAmountInterestDialog(
    fileDefaultRate: Double,
    fileDefaultMode: CalculationMode,
    initialAmount: String = "",
    onConfirm: (InterestInputResult) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText    by remember { mutableStateOf(initialAmount) }
    var rateText      by remember { mutableStateOf(
        fileDefaultRate.toBigDecimal().stripTrailingZeros().toPlainString()
    ) }
    var loanType      by remember { mutableStateOf("MONTHLY") }
    var installText   by remember { mutableStateOf(defaultInstallmentsForType("MONTHLY").toString()) }
    var showAdvanced  by remember { mutableStateOf(false) }
    var durationBased by remember { mutableStateOf(fileDefaultMode == CalculationMode.DURATION) }
    var durationText  by remember { mutableStateOf("") }

    // ── Live calculations ────────────────────────────────────────────────────
    val principal     by remember { derivedStateOf { amountText.toDoubleOrNull() ?: 0.0 } }
    val rate          by remember { derivedStateOf { rateText.toDoubleOrNull() ?: 0.0 } }
    val installments  by remember { derivedStateOf { installText.toIntOrNull()?.coerceAtLeast(1) ?: 1 } }
    val durationDays  by remember { derivedStateOf { durationText.toIntOrNull()?.coerceAtLeast(1) } }

    val interestAmount by remember {
        derivedStateOf {
            if (durationBased && durationDays != null)
                calcDurationInterest(principal, rate, durationDays!!)
            else
                calcFlatInterest(principal, rate)
        }
    }
    val totalRepayment     by remember { derivedStateOf { principal + interestAmount } }
    val perInstallment     by remember { derivedStateOf {
        if (installments > 0) totalRepayment / installments else totalRepayment
    } }

    fun formatMoney(v: Double): String {
        if (v == 0.0) return "₹0"
        val s = "%.2f".format(v).trimEnd('0').trimEnd('.')
        return "₹$s"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Loan Amount & Interest") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Inputs ───────────────────────────────────────────────────
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Principal Amount*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)) }
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Interest Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("%", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)) }
                )

                // ── Live summary card ────────────────────────────────────────
                if (principal > 0.0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            InterestRow("Principal",     formatMoney(principal))
                            InterestRow("Interest Rate", "$rate%")
                            InterestRow("Interest",      formatMoney(interestAmount))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            InterestRow("Total Repayment", formatMoney(totalRepayment), bold = true)
                        }
                    }
                }

                // ── Loan type ────────────────────────────────────────────────
                Text("Loan Type", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DAILY", "WEEKLY", "MONTHLY").forEach { t ->
                        FilterChip(
                            selected = loanType == t,
                            onClick  = {
                                loanType = t
                                installText = defaultInstallmentsForType(t).toString()
                            },
                            label = { Text(t.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                // ── Installments + per-installment ───────────────────────────
                OutlinedTextField(
                    value = installText,
                    onValueChange = { installText = it.filter { c -> c.isDigit() } },
                    label = { Text("Number of Installments") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (principal > 0.0 && installments > 0) {
                    Text(
                        "Per Installment: ${formatMoney(perInstallment)} × $installments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // ── Advanced section ─────────────────────────────────────────
                HorizontalDivider()
                TextButton(
                    onClick = { showAdvanced = !showAdvanced },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (showAdvanced) "Advanced ▲" else "Advanced ▼",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(
                    visible = showAdvanced,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked  = durationBased,
                                onCheckedChange = { durationBased = it }
                            )
                            Text("Enable duration-based calculation",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        AnimatedVisibility(visible = durationBased) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = durationText,
                                    onValueChange = { durationText = it.filter { c -> c.isDigit() } },
                                    label = { Text("Duration (days)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                if (principal > 0.0 && durationDays != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            InterestRow("Duration", "${durationDays} days")
                                            InterestRow("Interest (duration-based)", formatMoney(interestAmount))
                                            InterestRow("Total with duration", formatMoney(totalRepayment), bold = true)
                                        }
                                    }
                                }
                                Text(
                                    "Formula: Principal × (Rate/100) × (Days/365)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (!durationBased) {
                            Text(
                                "Simple flat rate: Interest = Principal × Rate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = amountText.toDoubleOrNull() ?: return@TextButton
                    if (p <= 0.0) return@TextButton
                    onConfirm(
                        InterestInputResult(
                            principal            = p,
                            interestRate         = rate,
                            interestAmount       = interestAmount,
                            totalRepayment       = totalRepayment,
                            loanType             = loanType,
                            numberOfInstallments = installments,
                            perInstallmentAmount = perInstallment,
                            isDurationBased      = durationBased,
                            durationDays         = if (durationBased) durationDays else null
                        )
                    )
                },
                enabled = amountText.toDoubleOrNull() != null && amountText.toDoubleOrNull()!! > 0.0
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Small helper row for the live interest summary card. */
@Composable
private fun InterestRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Part 5 — Interest info chip shown on person card (call inside PersonCard)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Compact interest summary shown below the amount on a person card.
 * Only shown when [interestRate] > 0 (i.e. interest was configured).
 *
 * Usage inside PersonCard content column:
 * ```
 * PersonInterestInfo(
 *     interestRate         = person.interestRate,
 *     totalRepayment       = person.totalRepayment,
 *     perInstallmentAmount = person.perInstallmentAmount,
 *     numberOfInstallments = person.numberOfInstallments,
 *     isDurationBased      = person.isDurationBased,
 *     durationDays         = person.durationDays
 * )
 * ```
 */
@Composable
fun PersonInterestInfo(
    interestRate: Double,
    totalRepayment: Double,
    perInstallmentAmount: Double,
    numberOfInstallments: Int,
    isDurationBased: Boolean,
    durationDays: Int?
) {
    if (interestRate <= 0.0) return

    fun fmt(v: Double): String {
        if (v == 0.0) return "₹0"
        val s = "%.2f".format(v).trimEnd('0').trimEnd('.')
        return "₹$s"
    }

    Column(
        modifier = Modifier.padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            "Interest: $interestRate%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (totalRepayment > 0.0) {
            Text(
                "Total: ${fmt(totalRepayment)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (perInstallmentAmount > 0.0 && numberOfInstallments > 0) {
            Text(
                "Installment: ${fmt(perInstallmentAmount)} × $numberOfInstallments",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isDurationBased && durationDays != null && durationDays > 0) {
            Text(
                "Duration: $durationDays days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
