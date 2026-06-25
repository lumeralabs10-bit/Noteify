package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY lastLogin DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCount(): Int
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_records WHERE :isAdmin = 1 OR userEmail = :email ORDER BY timestamp DESC")
    fun getAllHistory(isAdmin: Boolean, email: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Query("DELETE FROM history_records WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM history_records")
    suspend fun clearAllHistory()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM workspace_notes WHERE :isAdmin = 1 OR userEmail = :email ORDER BY timestamp DESC")
    fun getAllNotes(isAdmin: Boolean, email: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM workspace_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM workspace_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions WHERE :isAdmin = 1 OR userEmail = :email ORDER BY lastUpdated DESC")
    fun getAllSessions(isAdmin: Boolean, email: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM study_groups ORDER BY timestamp DESC")
    fun getAllGroups(): Flow<List<StudyGroupEntity>>

    @Query("SELECT * FROM study_groups WHERE id = :id")
    suspend fun getGroupById(id: Int): StudyGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: StudyGroupEntity): Long

    @Delete
    suspend fun deleteGroup(group: StudyGroupEntity)

    @Query("DELETE FROM study_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Int)

    // Memberships
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userEmail = :userEmail")
    suspend fun deleteMember(groupId: Int, userEmail: String)

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getMembersOfGroup(groupId: Int): Flow<List<GroupMemberEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM group_members WHERE groupId = :groupId AND userEmail = :userEmail)")
    suspend fun isUserMemberOfGroup(groupId: Int, userEmail: String): Boolean

    // Shared Notes
    @Query("SELECT * FROM shared_notes WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getSharedNotesForGroup(groupId: Int): Flow<List<SharedNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedNote(sharedNote: SharedNoteEntity)

    @Query("DELETE FROM shared_notes WHERE id = :sharedNoteId")
    suspend fun deleteSharedNoteById(sharedNoteId: Int)
}

