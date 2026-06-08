package com.moneymate.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SlideToCallSheet(
    phoneNumber: String,
    personName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Phone, null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text("Call $personName?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(phoneNumber, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Slide button
            val offsetX = remember { Animatable(0f) }
            val trackWidth = 280.dp
            val thumbSize = 56.dp
            val maxSlide = with(androidx.compose.ui.platform.LocalDensity.current) { (trackWidth - thumbSize).toPx() }
            val coroutineScope = rememberCoroutineScope()
            var triggered by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .width(trackWidth)
                    .height(thumbSize)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    "Slide to call →",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .size(thumbSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {},
                                onDragEnd = {
                                    if (offsetX.value >= maxSlide * 0.85f && !triggered) {
                                        triggered = true
                                        onConfirm()
                                    } else {
                                        coroutineScope.launch { offsetX.animateTo(0f, tween(300)) }
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch { offsetX.animateTo(0f, tween(300)) }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        val newValue = offsetX.value + dragAmount
                                        offsetX.snapTo(newValue.coerceIn(0f, maxSlide))
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Spacer(Modifier.height(8.dp))
        }
    }
}
