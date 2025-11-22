@file:OptIn(ExperimentalMaterial3Api::class)

package com.cycb.chat.ui.auth

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cycb.chat.ui.components.ChatTextField
import com.cycb.chat.ui.components.PasswordTextField
import com.cycb.chat.ui.components.PrimaryButton
import com.cycb.chat.viewmodel.AuthUiState
import com.cycb.chat.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isDisplayNameValid = displayName.length >= 2
    val isUsernameValid = username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))
    val isPasswordValid = password.length >= 8 &&
        password.any { it.isUpperCase() } &&
        password.any { it.isLowerCase() } &&
        password.any { it.isDigit() }
    val isConfirmPasswordValid = confirmPassword == password && confirmPassword.isNotEmpty()
    val isFormValid = isDisplayNameValid && isUsernameValid && isPasswordValid && isConfirmPasswordValid

    var fieldsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        fieldsVisible = true
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignUpSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isFormValid) {
                                focusManager.clearFocus()
                                viewModel.register(username, password, displayName)
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Icon(
                            Icons.Default.Check,
                            "Create",
                            tint = if (isFormValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                var profileImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

                AnimatedVisibility(
                    visible = fieldsVisible,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        com.cycb.chat.ui.components.CircularImagePicker(
                            imageUri = profileImageUri,
                            onImageSelected = { uri -> profileImageUri = uri },
                            onImageRemoved = { profileImageUri = null },
                            size = 100.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Add Photo (Optional)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = fieldsVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = spring(
                            dampingRatio = 0.65f,
                            stiffness = Spring.StiffnessHigh
                        )
                    ) + fadeIn()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        ChatTextField(
                            value = displayName,
                            onValueChange = { if (it.length <= 50) displayName = it },
                            placeholder = "Display Name",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            isError = displayName.isNotEmpty() && !isDisplayNameValid,
                            errorMessage = if (displayName.isNotEmpty() && !isDisplayNameValid)
                                "Name must be at least 2 characters"
                            else null
                        )

                        ChatTextField(
                            value = username,
                            onValueChange = {
                                val filtered = it.filter { char -> char.isLetterOrDigit() || char == '_' }
                                if (filtered.length <= 20) username = filtered.lowercase()
                            },
                            placeholder = "Username",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            isError = username.isNotEmpty() && !isUsernameValid,
                            errorMessage = if (username.isNotEmpty() && !isUsernameValid)
                                "Username: 3-20 chars, letters, numbers, underscore only"
                            else null
                        )

                        PasswordTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Password",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            isError = password.isNotEmpty() && !isPasswordValid,
                            errorMessage = if (password.isNotEmpty() && !isPasswordValid)
                                "Min 8 chars, 1 uppercase, 1 lowercase, 1 number"
                            else null
                        )

                        PasswordTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Confirm Password",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (isFormValid) {
                                        viewModel.register(username, password, displayName)
                                    }
                                }
                            ),
                            isError = confirmPassword.isNotEmpty() && !isConfirmPasswordValid,
                            errorMessage = if (confirmPassword.isNotEmpty() && !isConfirmPasswordValid)
                                "Passwords do not match"
                            else null
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState is AuthUiState.Error,
                    enter = fadeIn(
                        animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = (uiState as? AuthUiState.Error)?.message ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = fieldsVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = 0.65f,
                            stiffness = Spring.StiffnessHigh
                        )
                    ) + fadeIn()
                ) {
                    PrimaryButton(
                        text = "Create Account",
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.register(username, password, displayName)
                        },
                        isLoading = uiState is AuthUiState.Loading,
                        enabled = isFormValid
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = fieldsVisible,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 400, delayMillis = 200)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onNavigateToLogin
                        ) {
                            Text(
                                text = "Log in",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
