package com.hbazai.mycarservices

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hbazai.mycarservices.navigation.NavGraph
import com.hbazai.mycarservices.ui.theme.MyCarServicesTheme
import com.hbazai.mycarservices.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang    = LocaleHelper.getSavedLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Yellow status bar, dark icons on yellow background
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim         = Color.parseColor("#FFD600"),
                darkScrim     = Color.parseColor("#FFD600")
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            MyCarServicesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}