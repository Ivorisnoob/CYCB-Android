package com.cycb.chat.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cycb.chat.data.model.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditProfileScreen(
    user: User,
    onSaveClick: (User) -> Unit,
    onCancelClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val viewModel: com.cycb.chat.viewmodel.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val updateState by viewModel.updateState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadedPictureUrl by viewModel.uploadedProfilePictureUrl.collectAsState()

    var displayName by remember { mutableStateOf(user.displayName) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        when (updateState) {
            is com.cycb.chat.viewmodel.UpdateProfileState.Success -> {
                showSuccessMessage = true
                kotlinx.coroutines.delay(300)

                onSaveClick(user.copy(
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    email = email,
                    profilePicture = uploadedPictureUrl ?: user.profilePicture
                ))

                kotlinx.coroutines.delay(100)
                viewModel.resetUpdateState()
            }
            is com.cycb.chat.viewmodel.UpdateProfileState.Error -> {

                android.util.Log.e("EditProfileScreen", "Update error: ${(updateState as com.cycb.chat.viewmodel.UpdateProfileState.Error).message}")
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    val isModified = displayName != user.displayName ||
            username != user.username ||
            bio != (user.bio ?: "") ||
            email != (user.email ?: "") ||
            profilePictureUri != null

    val isLoading = updateState is com.cycb.chat.viewmodel.UpdateProfileState.Loading
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    TextButton(onClick = onCancelClick) {
                        Text(
                            "Cancel",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (isLoading) {
                        LoadingIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {

                                    if (profilePictureUri != null) {
                                        val result = com.cycb.chat.utils.ImageUploadHelper.uploadProfilePicture(
                                            context = context,
                                            imageUri = profilePictureUri!!
                                        )

                                        result.onSuccess { url ->

                                            viewModel.updateProfile(
                                                displayName = displayName,
                                                username = username,
                                                bio = bio,
                                                email = email,
                                                profilePictureUrl = url
                                            )
                                        }.onFailure {

                                            viewModel.updateProfile(
                                                displayName = displayName,
                                                username = username,
                                                bio = bio,
                                                email = email
                                            )
                                        }
                                    } else {

                                        viewModel.updateProfile(
                                            displayName = displayName,
                                            username = username,
                                            bio = bio,
                                            email = email
                                        )
                                    }
                                }
                            },
                            enabled = isModified && displayName.isNotBlank() && username.isNotBlank()
                        ) {
                            Text(
                                "Save",
                                color = if (isModified && displayName.isNotBlank() && username.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                EditProfilePicture(
                    currentPictureUrl = user.profilePicture ?: "",
                    newPictureUri = profilePictureUri,
                    onPictureSelected = { uri -> profilePictureUri = uri }
                )
            }

            item {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { if (it.length <= 50) displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    supportingText = {
                        Text("${displayName.length}/50")
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        if (it.length <= 20) username = it.lowercase().filter { char ->
                            char.isLetterOrDigit() || char == '_'
                        }
                    },
                    label = { Text("Username") },
                    leadingIcon = { Text("@") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    supportingText = {
                        Text("${username.length}/20")
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 200) bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        Text("${bio.length}/200")
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )
            }

            item {
                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {  },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account")
                }
            }
        }
    }
}

@Composable
fun EditProfilePicture(
    currentPictureUrl: String,
    newPictureUri: Uri?,
    onPictureSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPictureSelected(it) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            AsyncImage(
                model = newPictureUri ?: currentPictureUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .clickable { launcher.launch("image/*") },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Change Picture",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { launcher.launch("image/*") }) {
            Text("Change Profile Picture")
        }
    }
}
