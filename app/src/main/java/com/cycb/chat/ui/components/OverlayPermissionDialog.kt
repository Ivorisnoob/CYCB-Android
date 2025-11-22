package com.cycb.chat.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cycb.chat.utils.OverlayPermissionHelper

@Composable
fun OverlayPermissionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Picture-in-Picture") },
        text = {
            Column {
                Text("To use voice calls while using other apps:")
                Spacer(Modifier.height(8.dp))
                Text("1. Tap 'Open Settings' below")
                Text("2. Enable 'Display over other apps'")
                Text("3. Return to the app")
            }
        },
        confirmButton = {
            Button(onClick = {
                OverlayPermissionHelper.requestOverlayPermission(context)
                onDismiss()
            }) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}
