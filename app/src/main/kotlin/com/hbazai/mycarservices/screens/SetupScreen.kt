package com.hbazai.mycarservices.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hbazai.mycarservices.ui.theme.OnPrimary
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import com.hbazai.mycarservices.util.AppPreferences

@Composable
fun SetupScreen(onSetupDone: () -> Unit) {
    val context = LocalContext.current

    val currencies    = listOf("€", "$", "£", "﷼", "تومان")
    val distanceUnits = listOf("km", "mile")

    var selectedCurrency by remember { mutableStateOf("€") }
    var selectedDistance by remember { mutableStateOf("km") }

    Box(
        modifier         = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text       = "⚙️",
                fontSize   = 48.sp,
                textAlign  = TextAlign.Center
            )

            Text(
                text       = "App Setup",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = PrimaryYellow,
                textAlign  = TextAlign.Center
            )

            Text(
                text      = "Choose your preferred currency and distance unit. You can change these later in Settings.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // ── Currency ──────────────────────────────
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = "Currency",
                        fontWeight = FontWeight.Bold,
                        color      = PrimaryYellow,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    currencies.forEach { c ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCurrency == c,
                                onClick  = { selectedCurrency = c },
                                colors   = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryYellow
                                )
                            )
                            Text(
                                text     = c,
                                modifier = Modifier.padding(start = 8.dp),
                                color    = MaterialTheme.colorScheme.onSurface,
                                style    = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // ── Distance ──────────────────────────────
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = "Distance Unit",
                        fontWeight = FontWeight.Bold,
                        color      = PrimaryYellow,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    distanceUnits.forEach { d ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDistance == d,
                                onClick  = { selectedDistance = d },
                                colors   = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryYellow
                                )
                            )
                            Text(
                                text     = d,
                                modifier = Modifier.padding(start = 8.dp),
                                color    = MaterialTheme.colorScheme.onSurface,
                                style    = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    AppPreferences.saveCurrency(context, selectedCurrency)
                    AppPreferences.saveDistanceUnit(context, selectedDistance)
                    AppPreferences.markSetupDone(context)
                    onSetupDone()
                },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor   = OnPrimary
                )
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}