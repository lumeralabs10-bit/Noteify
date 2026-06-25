package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        HistoryEntity::class,
        NoteEntity::class,
        ChatSessionEntity::class,
        StudyGroupEntity::class,
        GroupMemberEntity::class,
        SharedNoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun historyDao(): HistoryDao
    abstract fun noteDao(): NoteDao
    abstract fun chatDao(): ChatDao
    abstract fun groupDao(): GroupDao
}

