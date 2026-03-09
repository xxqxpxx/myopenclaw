package com.myopenclaw.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAlertBottomSheet(
    ticker: String,
    onDismiss: () -> Unit,
    onCreateAlert: (ticker: String, alertType: String, targetPrice: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var targetPrice by remember { mutableStateOf("") }
    var tickerInput by remember { mutableStateOf(ticker) }
    var alertType by remember { mutableStateOf("above") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Set Price Alert",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Ticker input (editable if not pre-filled)
            OutlinedTextField(
                value = tickerInput,
                onValueChange = { tickerInput = it.uppercase().take(10) },
                label = { Text("Ticker Symbol") },
                placeholder = { Text("e.g. AAPL, BTC, EUR/USD") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Alert type toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = alertType == "above",
                    onClick = { alertType = "above" },
                    label = { Text("Price goes above") },
                    leadingIcon = if (alertType == "above") {
                        { Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BullishGreen.copy(alpha = 0.2f),
                        selectedLabelColor = BullishGreen
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = alertType == "below",
                    onClick = { alertType = "below" },
                    label = { Text("Price goes below") },
                    leadingIcon = if (alertType == "below") {
                        { Icon(Icons.Default.TrendingDown, null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BearishRed.copy(alpha = 0.2f),
                        selectedLabelColor = BearishRed
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Target price input
            OutlinedTextField(
                value = targetPrice,
                onValueChange = { newValue ->
                    // Allow only valid decimal numbers
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        targetPrice = newValue
                    }
                },
                label = { Text("Target Price") },
                leadingIcon = { Text("$", color = Primary, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Create button
            Button(
                onClick = {
                    val price = targetPrice.toDoubleOrNull()
                    if (price != null && price > 0 && tickerInput.isNotBlank()) {
                        onCreateAlert(tickerInput, alertType, price)
                        onDismiss()
                    }
                },
                enabled = targetPrice.toDoubleOrNull()?.let { it > 0 } == true && tickerInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Alert", modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
