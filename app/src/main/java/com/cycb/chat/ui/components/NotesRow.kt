package com.cycb.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cycb.chat.data.model.Note
import com.cycb.chat.data.model.UserSummary

@Composable
fun NotesRow(
    currentUser: UserSummary?,
    notes: List<Note>,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            val myNote = notes.find { it.userId._id == currentUser?._id }
            AddNoteItem(
                currentUser = currentUser,
                note = myNote,
                onClick = onAddNoteClick
            )
        }

        items(notes.filter { it.userId._id != currentUser?._id }) { note ->
            NoteItem(note = note, onClick = { onNoteClick(note) })
        }
    }
}

@Composable
fun AddNoteItem(
    currentUser: UserSummary?,
    note: Note?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .padding(4.dp)
                .clickable(onClick = onClick)
        ) {

            AsyncImage(
                model = currentUser?.profilePicture
                    ?: "https://ui-avatars.com/api/?name=${currentUser?.username ?: "Me"}",
                contentDescription = "Your Profile",
                modifier = Modifier
                    .size(if (note != null) 44.dp else 56.dp)
                    .clip(CircleShape)
                    .align(if (note != null) Alignment.BottomCenter else Alignment.Center),
                contentScale = ContentScale.Crop
            )

            if (note != null) {

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = 64.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 11.sp
                    )
                }
            } else {

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Note",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = if (note != null) "Your Note" else "Add note",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {

            AsyncImage(
                model = note.userId.profilePicture ?: "https://ui-avatars.com/api/?name=${note.userId.username}",
                contentDescription = note.userId.username,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 64.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = note.userId.displayName ?: note.userId.username,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
