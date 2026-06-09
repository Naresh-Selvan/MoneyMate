package com.moneymate.app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.ui.viewmodel.CollectionPersonState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Collection person card for Collect/Pay tabs.
 * Shows: serial, name, start date + installment, green/red dot, area,
 * amount input field, balance/paid/total, swipe actions.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionPersonCard(
    personState: CollectionPersonState,
    serialNumber: Int,
    onDelete: () -> Unit,
    onEditLoan: () -> Unit,
    onEditCustomer: () -> Unit,
    onSavePayment: (amount: Double) -> Unit,
    onLongPress: () -> Unit,
    onTap: () -> Unit
) {
    val person = personState.person
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showActions by remember { mutableStateOf(false) }
    var paymentInput by remember { mutableStateOf("") }

    // ── Photo / Avatar ──
    val photoBitmap = remember(person.photoUri) {
        if (!person.photoUri.isNullOrBlank()) {
            try { BitmapFactory.decodeFile(person.photoUri) } catch (e: Exception) { null }
        } else null
    }
    val initials = remember(person.name) {
        person.name.trim().split("\\s+".toRegex()).take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
    }

    val dotColor by animateColorAsState(
        targetValue = if (personState.paidToday) Color(0xFF4CAF50) else Color(0xFFE53935),
        animationSpec = tween(300),
        label = "dotColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (showActions) showActions = false else onTap() },
                onLongClick = { showActions = !showActions }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (showActions) 4.dp else 1.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // ── Main card content ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(),
                            contentDescription = "Photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            initials.ifEmpty { "?" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))

                // Serial + Name
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$serialNumber.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            person.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    // Start date + installment
                    Text(
                        "${dateFmt.format(Date(person.dateGiven))}(${personState.installmentNumber})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Installment info
                    if (personState.perInstallmentAmount > 0) {
                        Text(
                            "Install: ${"₹%.0f".format(personState.perInstallmentAmount)} (${personState.installmentNumber}/${personState.totalInstallments})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Area (place)
                    if (!person.place.isNullOrBlank()) {
                        Text(
                            person.place!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Right side: dot + amount input + save ──
                Column(horizontalAlignment = Alignment.End) {
                    // Green/Red dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(Modifier.height(4.dp))

                    // Amount input field
                    OutlinedTextField(
                        value = paymentInput,
                        onValueChange = { paymentInput = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = { Text("₹", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        modifier = Modifier.width(80.dp).height(40.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.labelSmall
                    )

                    // Save icon
                    if (paymentInput.isNotBlank() && (paymentInput.toDoubleOrNull() ?: 0.0) > 0) {
                        IconButton(
                            onClick = {
                                val amt = paymentInput.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    onSavePayment(amt)
                                    paymentInput = ""
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Save,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── Balance/Paid/Total row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Balance: ${"₹%.0f".format(personState.pending)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "Paid: ${"₹%.0f".format(personState.totalPaid)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    "Total: ${"₹%.0f".format(person.amountGiven)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                // Bad Loan chip
                if (personState.isBadLoan) {
                    AssistChip(
                        onClick = {},
                        label = { Text("BAD LOAN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }

            // ── Swipe-down action buttons ──
            AnimatedVisibility(visible = showActions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showActions = false; onDelete() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("DELETE", style = MaterialTheme.typography.labelSmall)
                    }
                    FilledTonalButton(
                        onClick = { showActions = false; onEditLoan() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("EDIT LOAN", style = MaterialTheme.typography.labelSmall)
                    }
                    FilledTonalButton(
                        onClick = { showActions = false; onEditCustomer() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("EDIT CUST", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
        }
    }
}
