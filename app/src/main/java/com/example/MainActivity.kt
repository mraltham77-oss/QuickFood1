package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.QuickFoodMainScreen
import com.example.ui.theme.QuickFoodTheme
import com.example.ui.viewmodel.QuickFoodViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickFoodTheme {
                val viewModel: QuickFoodViewModel = viewModel()
                QuickFoodMainScreen(viewModel = viewModel)
            }
        }
    }
}
