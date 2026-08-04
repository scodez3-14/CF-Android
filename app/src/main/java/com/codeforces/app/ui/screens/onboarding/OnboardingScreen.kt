package com.codeforces.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codeforces.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var handle by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(viewModel.navigated) {
        viewModel.navigated.collect { onComplete() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CfBackground, CfSurface, CfBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo section
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CodeforcesRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Codeforces",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    ),
                    color = CfTextPrimary
                )
                Text(
                    text = "Competitive Programming Hub",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CfTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your Codeforces handle to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = CfTextSecondary,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = handle,
                onValueChange = {
                    handle = it
                    isError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Codeforces Handle") },
                leadingIcon = {
                    Icon(Icons.Rounded.Person, contentDescription = null)
                },
                placeholder = { Text("e.g. tourist") },
                isError = isError,
                supportingText = if (isError) {
                    { Text("Please enter a valid handle") }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (handle.isNotBlank()) viewModel.saveHandle(handle.trim())
                    else isError = true
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CodeforcesRed,
                    focusedLabelColor = CodeforcesRed,
                    cursorColor = CodeforcesRed
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (handle.isNotBlank()) viewModel.saveHandle(handle.trim())
                    else isError = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CodeforcesRed),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = CfTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Text(
                text = "You can change this later in Settings",
                style = MaterialTheme.typography.bodySmall,
                color = CfTextDisabled
            )
        }
    }
}
