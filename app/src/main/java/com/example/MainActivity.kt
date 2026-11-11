package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.BusEntryViewModel
import com.example.ui.UiEvent
import com.example.ui.screens.EntryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ShareReportScreen
import com.example.ui.theme.BusEntryTheme
import com.example.ui.theme.IosBackground
import com.example.ui.theme.IosDarkBlue

enum class CurrentScreen {
    HOME,
    ENTRY,
    SHARE
}

class MainActivity : ComponentActivity() {

    private val viewModel: BusEntryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BusEntryTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }

                LaunchedEffect(Unit) {
                    viewModel.uiEvents.collect { event ->
                        when (event) {
                            is UiEvent.ShowToast -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            }
                            is UiEvent.ShowError -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                            is UiEvent.EntrySaved -> {
                                // Kept on entry form so user can swiftly log subsequent vehicles
                            }
                        }
                    }
                }

                BackHandler(enabled = currentScreen != CurrentScreen.HOME) {
                    currentScreen = CurrentScreen.HOME
                }

                // Removed bottom navigation taskbar as requested: "Home taskpar dont needed"
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "Ashok Leyland - Ennore",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        letterSpacing = (-0.2).sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Bus Record &bull; Transport Security",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = Color(0xFFD1E4FA)
                                    )
                                }
                            },
                            navigationIcon = {
                                if (currentScreen != CurrentScreen.HOME) {
                                    IconButton(
                                        onClick = { currentScreen = CurrentScreen.HOME },
                                        modifier = Modifier.testTag("topbar_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = IosDarkBlue,
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(IosBackground)
                    ) {
                        when (currentScreen) {
                            CurrentScreen.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToEntry = {
                                        currentScreen = CurrentScreen.ENTRY
                                    },
                                    onNavigateToShare = {
                                        currentScreen = CurrentScreen.SHARE
                                    }
                                )
                            }
                            CurrentScreen.ENTRY -> {
                                EntryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        currentScreen = CurrentScreen.HOME
                                    }
                                )
                            }
                            CurrentScreen.SHARE -> {
                                ShareReportScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        currentScreen = CurrentScreen.HOME
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
