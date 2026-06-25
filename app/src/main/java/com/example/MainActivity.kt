package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.NoteifyApp
import com.example.ui.theme.NoteifyTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ConverterViewModel
import com.example.ui.viewmodel.GroupViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.LumeraViewModel
import com.example.ui.viewmodel.WorkspaceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val authVm: AuthViewModel = viewModel()
                    val workspaceVm: WorkspaceViewModel = viewModel()
                    val converterVm: ConverterViewModel = viewModel()
                    val lumeraVm: LumeraViewModel = viewModel()
                    val historyVm: HistoryViewModel = viewModel()
                    val groupVm: GroupViewModel = viewModel()

                    NoteifyApp(
                        authVm = authVm,
                        workspaceVm = workspaceVm,
                        converterVm = converterVm,
                        lumeraVm = lumeraVm,
                        historyVm = historyVm,
                        groupVm = groupVm
                    )
                }
            }
        }
    }
}
