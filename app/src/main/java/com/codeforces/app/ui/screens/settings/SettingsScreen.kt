package com.codeforces.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentHandle by viewModel.handle.collectAsStateWithLifecycle(initialValue = "")
    var editingHandle by remember { mutableStateOf(false) }
    var tempHandle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.Rounded.ArrowBack, "Back") 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesRed)
                if (editingHandle) {
                    OutlinedTextField(
                        value = tempHandle,
                        onValueChange = { tempHandle = it },
                        label = { Text("Codeforces Handle") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CodeforcesRed)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (tempHandle.isNotBlank()) {
                                    viewModel.saveHandle(tempHandle.trim())
                                    editingHandle = false
                                }
                            }
                        ) { Text("Save") }
                        TextButton(onClick = { editingHandle = false }) { Text("Cancel") }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Handle", style = MaterialTheme.typography.labelMedium, color = CfTextSecondary)
                            Text(currentHandle ?: "Not set", style = MaterialTheme.typography.bodyLarge)
                        }
                        IconButton(onClick = {
                            tempHandle = currentHandle ?: ""
                            editingHandle = true
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = CfTextSecondary)
                        }
                    }
                }
            }

            HorizontalDivider(color = CfDivider)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App Data", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesRed)
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CfSurface, contentColor = VerdictWA),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout & Clear Cache")
                }
            }
        }
    }
}
