package com.example.casioplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.casioplayer.ui.CasioMainScreen
import com.example.casioplayer.ui.CasioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CasioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CasioMainScreen(viewModel = viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkBraveStatus()
    }
}