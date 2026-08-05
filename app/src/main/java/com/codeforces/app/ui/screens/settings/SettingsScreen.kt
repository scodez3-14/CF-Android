package com.codeforces.app.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentHandle by viewModel.handle.collectAsStateWithLifecycle(initialValue = "")
    val reminders by viewModel.remindersEnabled.collectAsStateWithLifecycle(initialValue = false)
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val loggedInHandle by viewModel.loggedInHandle.collectAsStateWithLifecycle(initialValue = null)
    var editingHandle by remember { mutableStateOf(false) }
    var tempHandle by remember { mutableStateOf("") }
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshLogin()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val onRemindersChange: (Boolean) -> Unit = { enabled ->
        viewModel.setRemindersEnabled(enabled)
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesAccent)
                if (editingHandle) {
                    OutlinedTextField(
                        value = tempHandle,
                        onValueChange = { tempHandle = it },
                        label = { Text("Codeforces Handle") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CodeforcesAccent)
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

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Codeforces Sign-in", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesAccent)
                Text(
                    "Required to submit solutions to problems. Your password is encrypted and stored on this device only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CfTextSecondary
                )
                when (isLoggedIn) {
                    true -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Signed in as", style = MaterialTheme.typography.labelMedium, color = CfTextSecondary)
                                Text(loggedInHandle ?: "Codeforces user", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = VerdictOK)
                            }
                            TextButton(onClick = { viewModel.logoutCf() }) {
                                Text("Sign out", color = VerdictWA)
                            }
                        }
                    }
                    false -> {
                        Button(
                            onClick = onLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Login, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sign in to Codeforces")
                        }
                    }
                    null -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Text("Checking sign-in…", style = MaterialTheme.typography.bodyMedium, color = CfTextSecondary)
                        }
                    }
                }
            }

            HorizontalDivider(color = CfDivider)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notifications", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesAccent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Contest reminders", style = MaterialTheme.typography.bodyMedium, color = CfTextPrimary)
                        Text("Notify 30 minutes before upcoming contests", style = MaterialTheme.typography.labelMedium, color = CfTextSecondary)
                    }
                    Switch(
                        checked = reminders,
                        onCheckedChange = onRemindersChange
                    )
                }
            }

            HorizontalDivider(color = CfDivider)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App Data", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesAccent)
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
