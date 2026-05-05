package com.hbazai.mycarservices.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.padding(horizontal = 32.dp)
        ) {

            // ── Logo ──────────────────────────────────
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(color = PrimaryYellow, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(color = Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(id = R.drawable.icon),
                        contentDescription = "App Logo",
                        modifier           = Modifier.size(75.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── App name ──────────────────────────────
            Text(
                text       = stringResource(R.string.app_name),
                style      = MaterialTheme.typography.headlineMedium,
                color      = PrimaryYellow,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            // ── Tagline ───────────────────────────────
            Text(
                text      = "Track · Maintain · Drive Safe",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // ── Credit ────────────────────────────────
            Text(
                text      = "Made with ❤️ by HBazai",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}