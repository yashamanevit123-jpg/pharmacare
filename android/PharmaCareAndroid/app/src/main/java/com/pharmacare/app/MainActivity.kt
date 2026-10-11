package com.pharmacare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pharmacare.app.ui.navigation.PharmaCareNavGraph
import com.pharmacare.app.ui.theme.PharmaCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PharmaCareTheme {
                PharmaCareNavGraph()
            }
        }
    }
}
