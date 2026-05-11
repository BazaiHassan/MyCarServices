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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(1.dp))

            // ── Center Content ────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── Logo ──────────────────────────────
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(color = PrimaryYellow, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(70.dp)
                    )
                }

                // ── App Name ─────────────────────────
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // ── Tagline ──────────────────────────
                Text(
                    text = "Track · Maintain · Drive Safe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Footer ──────────────────────────────
            Text(
                text = "Made with ❤️ by HBazai",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}