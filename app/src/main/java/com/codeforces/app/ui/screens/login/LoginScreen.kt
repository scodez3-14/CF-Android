package com.codeforces.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onBack: () -> Unit,
    onBrowserLogin: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign in", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        },
        containerColor = CfBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(CodeforcesAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Sign in to Codeforces",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = CfTextPrimary
            )
            Text(
                text = "A real account is required to submit solutions.\nYour password is encrypted and stored on-device.",
                style = MaterialTheme.typography.bodyMedium,
                color = CfTextSecondary,
                textAlign = TextAlign.Center
            )

            when (state.isLoggedIn) {
                true -> SignedInContent(
                    handle = state.loggedInHandle,
                    onSignOut = viewModel::logout,
                    onDone = onLoggedIn
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onBrowserLogin,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)
                    ) {
                        Icon(Icons.Rounded.Public, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign in with browser", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        text = "Recommended — Codeforces may block direct logins, so use the built-in browser.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CfTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "or use handle & password",
                        style = MaterialTheme.typography.labelMedium,
                        color = CfTextDisabled,
                        textAlign = TextAlign.Center
                    )
                    SignInForm(
                        state = state,
                        onLogin = viewModel::login
                    )
                }
            }
        }
    }
}

@Composable
private fun SignedInContent(
    handle: String?,
    onSignOut: () -> Unit,
    onDone: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = VerdictOK, modifier = Modifier.size(40.dp))
            Text(
                text = "Signed in as ${handle ?: "Codeforces user"}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CfTextPrimary
            )
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
            TextButton(onClick = onSignOut) {
                Text("Sign out", color = VerdictWA)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignInForm(
    state: LoginUiState,
    onLogin: (String, String, Boolean) -> Unit
) {
    var handle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var remember by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = handle,
            onValueChange = { handle = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Handle or Email") },
            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CodeforcesAccent,
                focusedLabelColor = CodeforcesAccent,
                cursorColor = CodeforcesAccent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onLogin(handle, password, remember)
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CodeforcesAccent,
                focusedLabelColor = CodeforcesAccent,
                cursorColor = CodeforcesAccent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = remember,
                onCheckedChange = { remember = it },
                colors = CheckboxDefaults.colors(checkedColor = CodeforcesAccent)
            )
            Text("Remember me (auto sign-in)", color = CfTextSecondary)
        }

        state.errorMessage?.let {
            Text(it, color = VerdictWA, style = MaterialTheme.typography.bodyMedium)
        }
        state.successMessage?.let {
            Text(it, color = VerdictOK, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                onLogin(handle, password, remember)
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign in", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Text(
            text = "Signing in creates a Codeforces web session that this app uses to submit code on your behalf.",
            style = MaterialTheme.typography.bodySmall,
            color = CfTextDisabled,
            textAlign = TextAlign.Center
        )
    }
}
