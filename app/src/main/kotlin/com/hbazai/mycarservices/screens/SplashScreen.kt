package com.hbazai.mycarservices.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hbazai.mycarservices.R
import com.hbazai.mycarservices.ui.theme.PrimaryYellow
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Calculate final radius needed to cover screen (hypotenuse)
    val finalRadius = remember(screenWidth, screenHeight) {
        kotlin.math.sqrt(
            (screenWidth.value * screenWidth.value) + 
            (screenHeight.value * screenHeight.value)
        ).dp / 2
    }
    
    var circleSize by remember { mutableStateOf(120.dp) }
    val animatedSize by animateDpAsState(
        targetValue = if (circleSize == 120.dp) 120.dp else finalRadius * 2,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        )
    )
    
    var isExpanding by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanding) finalRadius.value * 2 / 60 else 1f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        )
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (isExpanding) 0f else 1f,
        animationSpec = tween(durationMillis = 300)
    )
    
    LaunchedEffect(Unit) {
        // Initial delay for the logo entrance
        delay(500)
        // Start expansion
        isExpanding = true
        // Wait for expansion animation
        delay(800)
        // Navigate to main app
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Expanding background circle
        Box(
            modifier = Modifier
                .size(animatedSize)
                .background(
                    color = PrimaryYellow,
                    shape = CircleShape
                )
                .align(Alignment.Center)
        )
        
        // Content that fades out during expansion
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = contentAlpha),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo image with circular yellow background
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = PrimaryYellow,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Track · Maintain · Drive Safe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}