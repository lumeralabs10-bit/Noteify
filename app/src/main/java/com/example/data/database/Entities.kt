package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val lastLogin: Long = System.currentTimeMillis()
)

@Entity(tableName = "history_records")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "notes", "conversion", "chat_summary"
    val title: String,
    val description: String,
    val originalFileName: String?,
    val content: String, // Full markdown notes or summary
    val fileSize: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val outputFilePath: String? = null, // For downloaded/converted files
    val userEmail: String = "" // Partition by user
)

@Entity(tableName = "workspace_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val folder: String = "General", // "Biology", "Physics", "General", etc.
    val isBookmarked: Boolean = false,
    val colorHex: String = "#A78BFA", // default lavender color code
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String = "" // Partition by user
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String, // unique session UUID
    val title: String,
    val messagesJson: String, // serialized list of ChatMessage
    val lastUpdated: Long = System.currentTimeMillis(),
    val userEmail: String = "" // Partition by user
)

@Entity(tableName = "study_groups")
data class StudyGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val password: String, // Password created by the creator, required for classmates to join
    val creatorEmail: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val userEmail: String,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shared_notes")
data class SharedNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val title: String,
    val content: String,
    val sharedBy: String, // email of classmate who shared it
    val timestamp: Long = System.currentTimeMillis()
)

