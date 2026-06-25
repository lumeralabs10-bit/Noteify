package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileProcessor
import com.example.data.database.*
import com.example.data.gemini.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

// ==========================================
// 1. AUTH & PREFERENCES VIEWMODEL
// ==========================================
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("noteify_prefs", Context.MODE_PRIVATE)
    private val db = DatabaseProvider.getDatabase(application)
    private val userDao = db.userDao()

    var isLoggedIn by mutableStateOf(prefs.getBoolean("logged_in", false))
        private set
    var userEmail by mutableStateOf(prefs.getString("user_email", "") ?: "")
        private set
    var userName by mutableStateOf(prefs.getString("user_name", "") ?: "")
        private set
    var hasCompletedOnboarding by mutableStateOf(prefs.getBoolean("onboarding_done", false))
        private set

    // Stats
    var studyStreak by mutableStateOf(prefs.getInt("study_streak", 3))
        private set
    var notesGeneratedCount by mutableStateOf(prefs.getInt("notes_generated", 12))
        private set
    var filesProcessedCount by mutableStateOf(prefs.getInt("files_processed", 8))
        private set
    var quizScorePercent by mutableStateOf(prefs.getInt("quiz_score_percent", 85))
        private set

    val allUsers: StateFlow<List<UserEntity>> = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isAdmin: Boolean
        get() = userEmail.trim().lowercase() == "lumeralabs10@gmail.com"

    fun login(email: String, name: String) {
        val normalizedEmail = email.trim().lowercase()
        prefs.edit().apply {
            putBoolean("logged_in", true)
            putString("user_email", normalizedEmail)
            putString("user_name", name)
            apply()
        }
        isLoggedIn = true
        userEmail = normalizedEmail
        userName = name

        // Register/update user record locally
        viewModelScope.launch(Dispatchers.IO) {
            userDao.insertUser(UserEntity(email = normalizedEmail, name = name))
            // Always ensure the admin account is listed as a user as well
            userDao.insertUser(UserEntity(email = "lumeralabs10@gmail.com", name = "Lumera Labs Admin"))
        }
    }

    fun logout() {
        prefs.edit().apply {
            putBoolean("logged_in", false)
            apply()
        }
        isLoggedIn = false
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_done", true).apply()
        hasCompletedOnboarding = true
    }

    fun incrementNotesCount() {
        val count = notesGeneratedCount + 1
        prefs.edit().putInt("notes_generated", count).apply()
        notesGeneratedCount = count
        
        // Boost streak as well
        if (Math.random() > 0.5) {
            val str = studyStreak + 1
            prefs.edit().putInt("study_streak", str).apply()
            studyStreak = str
        }
    }

    fun incrementFilesCount() {
        val count = filesProcessedCount + 1
        prefs.edit().putInt("files_processed", count).apply()
        filesProcessedCount = count
    }

    fun updateQuizScore(score: Int) {
        prefs.edit().putInt("quiz_score_percent", score).apply()
        quizScorePercent = score
    }

    fun deleteUser(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.deleteUserByEmail(email)
        }
    }
}

// ==========================================
// 2. NOTES WORKSPACE VIEWMODEL
// ==========================================
class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val noteDao = db.noteDao()
    private val historyDao = db.historyDao()

    private val queryParams = MutableStateFlow(Pair("", false)) // Pair(email, isAdmin)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allNotes: StateFlow<List<NoteEntity>> = queryParams
        .flatMapLatest { (email, isAdmin) ->
            noteDao.getAllNotes(isAdmin, email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var currentNote by mutableStateOf<NoteEntity?>(null)
    var isGeneratingNotes by mutableStateOf(false)
    var notesGenerationProgress by mutableStateOf("")

    fun updateCurrentUser(email: String, isAdmin: Boolean) {
        queryParams.value = Pair(email.trim().lowercase(), isAdmin)
    }

    fun saveNote(title: String, content: String, folder: String = "General") {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(
                title = title,
                content = content,
                folder = folder,
                userEmail = queryParams.value.first
            )
            noteDao.insertNote(note)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }

    fun toggleBookmark(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.updateNote(note.copy(isBookmarked = !note.isBookmarked))
        }
    }

    fun changeNoteColor(note: NoteEntity, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.updateNote(note.copy(colorHex = colorHex))
        }
    }

    fun mergeNotes(note1: NoteEntity, note2: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val mergedTitle = "Merged: ${note1.title} & ${note2.title}"
            val mergedContent = "# ${note1.title}\n\n${note1.content}\n\n---\n\n# ${note2.title}\n\n${note2.content}"
            noteDao.insertNote(
                NoteEntity(
                    title = mergedTitle,
                    content = mergedContent,
                    folder = note1.folder,
                    userEmail = queryParams.value.first
                )
            )
        }
    }

    fun duplicateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertNote(
                NoteEntity(
                    title = "${note.title} (Copy)",
                    content = note.content,
                    folder = note.folder,
                    userEmail = note.userEmail.ifBlank { queryParams.value.first }
                )
            )
        }
    }

    /**
     * Launch the full file parsing & AI Notes Generation pipeline
     */
    fun generateAiNotesFromFile(
        context: Context, 
        uri: Uri, 
        authVm: AuthViewModel,
        onComplete: (NoteEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        isGeneratingNotes = true
        notesGenerationProgress = "Analyzing & Validating File..."
        
        viewModelScope.launch {
            try {
                // 1. Extract raw content
                notesGenerationProgress = "Extracting Academic Content..."
                val extracted = withContext(Dispatchers.IO) {
                    FileProcessor.extractTextContent(context, uri)
                }

                if (extracted.startsWith("Error extracting")) {
                    isGeneratingNotes = false
                    onError(extracted)
                    return@launch
                }

                // 2. Process with AI Notes generator
                notesGenerationProgress = "Synthesizing and Humanizing with Noteify AI..."
                val meta = FileProcessor.getFileMetadata(context, uri)
                val formattedNotes = GeminiService.generateStructuredNotes(extracted, meta.name)

                // 3. Save to database
                notesGenerationProgress = "Saving generated notes to Workspace..."
                val noteId = withContext(Dispatchers.IO) {
                    val noteEntity = NoteEntity(
                        title = meta.name.substringBeforeLast("."),
                        content = formattedNotes,
                        folder = "Uploads",
                        userEmail = queryParams.value.first
                    )
                    val generatedId = noteDao.insertNote(noteEntity)

                    // Save file and results to local History as well
                    historyDao.insertHistory(
                        HistoryEntity(
                            type = "notes",
                            title = "AI Notes: ${meta.name.substringBeforeLast(".")}",
                            description = "Structured study material synthesized from raw ${meta.extension.uppercase()} upload.",
                            originalFileName = meta.name,
                            content = formattedNotes,
                            fileSize = meta.size,
                            userEmail = queryParams.value.first
                        )
                    )
                    generatedId
                }

                authVm.incrementNotesCount()
                authVm.incrementFilesCount()

                isGeneratingNotes = false
                val newNote = noteDao.getNoteById(noteId.toInt())
                if (newNote != null) {
                    onComplete(newNote)
                } else {
                    onError("Failed to retrieve generated note from database.")
                }

            } catch (e: Exception) {
                Log.e("WorkspaceViewModel", "Notes generation failed", e)
                isGeneratingNotes = false
                onError("Note generation failed: ${e.localizedMessage}")
            }
        }
    }
}

// ==========================================
// 3. CONVERTER VIEWMODEL
// ==========================================
class ConverterViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val historyDao = db.historyDao()

    var state by mutableStateOf("idle") // "idle", "uploading", "processing", "converting", "generating", "ready", "failed"
        private set
    var sourceMetadata by mutableStateOf<FileProcessor.FileMetadata?>(null)
        private set
    var convertedFile by mutableStateOf<File?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var currentUserEmail = ""

    fun updateCurrentUser(email: String) {
        currentUserEmail = email.trim().lowercase()
    }

    fun reset() {
        state = "idle"
        sourceMetadata = null
        convertedFile = null
        errorMessage = null
    }

    fun startConversion(context: Context, inputUri: Uri, targetFormat: String, authVm: AuthViewModel) {
        viewModelScope.launch {
            try {
                errorMessage = null
                val meta = FileProcessor.getFileMetadata(context, inputUri)
                sourceMetadata = meta

                // Step 1: Uploading
                state = "uploading"
                kotlinx.coroutines.delay(600)

                // Step 2: Processing
                state = "processing"
                kotlinx.coroutines.delay(800)

                // Step 3: Converting
                state = "converting"
                val output = withContext(Dispatchers.IO) {
                    FileProcessor.convertFileLocally(context, inputUri, targetFormat)
                }

                // Step 4: Generating Output
                state = "generating"
                kotlinx.coroutines.delay(700)

                convertedFile = output
                state = "ready"

                // Save to History
                withContext(Dispatchers.IO) {
                    historyDao.insertHistory(
                        HistoryEntity(
                            type = "conversion",
                            title = "Converted ${meta.name.substringBeforeLast(".")}.${targetFormat.lowercase()}",
                            description = "Successful 100% local format translation: ${meta.extension.uppercase()} → ${targetFormat.uppercase()}",
                            originalFileName = meta.name,
                            content = "Successfully converted to ${targetFormat.uppercase()}.",
                            fileSize = String.format("%.2f KB", output.length() / 1024f),
                            outputFilePath = output.absolutePath,
                            userEmail = currentUserEmail.ifBlank { authVm.userEmail }
                        )
                    )
                }
                
                authVm.incrementFilesCount()

            } catch (e: Exception) {
                Log.e("ConverterViewModel", "Conversion failed", e)
                errorMessage = e.localizedMessage ?: "Conversion encountered an unexpected error."
                state = "failed"
            }
        }
    }
}

// ==========================================
// 4. LUMERA AI ASSISTANT VIEWMODEL
// ==========================================
class LumeraViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val chatDao = db.chatDao()

    private val queryParams = MutableStateFlow(Pair("", false))

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allSessions: StateFlow<List<ChatSessionEntity>> = queryParams
        .flatMapLatest { (email, isAdmin) ->
            chatDao.getAllSessions(isAdmin, email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var activeSessionId by mutableStateOf<String?>(null)
        private set
    var activeMessages = mutableStateListOf<Pair<String, Boolean>>() // Text to IsUser
    var isAiTyping by mutableStateOf(false)
    var isVoiceMode by mutableStateOf(false)
    var voiceSpeechIndicator by mutableStateOf("Tap mic to speak...")

    init {
        // Initialize with default/new chat session if empty
        viewModelScope.launch {
            allSessions.first().let {
                if (it.isNotEmpty()) {
                    loadSession(it.first())
                } else {
                    createNewSession()
                }
            }
        }
    }

    fun updateCurrentUser(email: String, isAdmin: Boolean) {
        val normalizedEmail = email.trim().lowercase()
        val oldEmail = queryParams.value.first
        queryParams.value = Pair(normalizedEmail, isAdmin)
        
        // If current user switched, reload the sessions
        if (oldEmail != normalizedEmail) {
            viewModelScope.launch {
                allSessions.first().let {
                    if (it.isNotEmpty()) {
                        loadSession(it.first())
                    } else {
                        createNewSession()
                    }
                }
            }
        }
    }

    fun loadSession(session: ChatSessionEntity) {
        activeSessionId = session.id
        activeMessages.clear()
        try {
            val jsonArray = JSONArray(session.messagesJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                activeMessages.add(Pair(obj.getString("text"), obj.getBoolean("isUser")))
            }
        } catch (e: Exception) {
            Log.e("LumeraViewModel", "Failed to deserialize messages", e)
        }
    }

    fun createNewSession(title: String = "New Academic Dialogue") {
        val newId = UUID.randomUUID().toString()
        activeSessionId = newId
        activeMessages.clear()
        
        // Welcome message
        activeMessages.add(Pair("Hello! I am **LUMERA**, developed by Lumera Labs. 🌌 I am here as your personal academic mentor. How can I assist your learning today?", false))
        saveCurrentSession(title)
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteSessionById(sessionId)
            if (activeSessionId == sessionId) {
                activeSessionId = null
                activeMessages.clear()
                createNewSession()
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        activeMessages.add(Pair(text, true))
        saveCurrentSession(text.take(30) + "...")

        isAiTyping = true
        viewModelScope.launch {
            try {
                // Compile conversation history format
                val history = activeMessages.dropLast(1).map { it }
                val aiReply = GeminiService.askLumera(text, history)
                activeMessages.add(Pair(aiReply, false))
                saveCurrentSession(text.take(30) + "...")
            } catch (e: Exception) {
                activeMessages.add(Pair("LUMERA encountered an error: ${e.localizedMessage}. Please try again.", false))
            } finally {
                isAiTyping = false
            }
        }
    }

    fun startVoiceMode() {
        isVoiceMode = true
        voiceSpeechIndicator = "Listening... Speak now."
    }

    fun stopVoiceMode() {
        isVoiceMode = false
    }

    fun processSimulatedVoiceInput() {
        voiceSpeechIndicator = "Thinking..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val prompts = listOf(
                "How can I prepare for competitive exams?",
                "Explain the formulas of notes processing",
                "Can you create a quick biology quiz?"
            )
            val randomPrompt = prompts.random()
            sendMessage(randomPrompt)
            voiceSpeechIndicator = "Speaking: \"Here is what you need to know...\""
            kotlinx.coroutines.delay(2000)
            isVoiceMode = false
        }
    }

    private fun saveCurrentSession(title: String) {
        val sessionId = activeSessionId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val jsonArray = JSONArray()
            for (msg in activeMessages) {
                val obj = JSONObject()
                obj.put("text", msg.first)
                obj.put("isUser", msg.second)
                jsonArray.put(obj)
            }
            chatDao.insertSession(
                ChatSessionEntity(
                    id = sessionId,
                    title = title,
                    messagesJson = jsonArray.toString(),
                    userEmail = queryParams.value.first
                )
            )
        }
    }
}

// ==========================================
// 5. HISTORY VIEWMODEL
// ==========================================
class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val historyDao = db.historyDao()

    private val queryParams = MutableStateFlow(Pair("", false))

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allHistory: StateFlow<List<HistoryEntity>> = queryParams
        .flatMapLatest { (email, isAdmin) ->
            historyDao.getAllHistory(isAdmin, email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCurrentUser(email: String, isAdmin: Boolean) {
        queryParams.value = Pair(email.trim().lowercase(), isAdmin)
    }

    fun deleteHistory(item: HistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.deleteHistoryById(item.id)
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.clearAllHistory()
        }
    }
}

// ==========================================
// 6. COLLABORATIVE STUDY GROUPS VIEWMODEL
// ==========================================
class GroupViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val groupDao = db.groupDao()

    var currentUserEmail by mutableStateOf("")
        private set
    var isAdminUser by mutableStateOf(false)
        private set

    val allGroups: StateFlow<List<StudyGroupEntity>> = groupDao.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCurrentUser(email: String, isAdmin: Boolean) {
        currentUserEmail = email.trim().lowercase()
        isAdminUser = isAdmin
    }

    fun createGroup(name: String, description: String, password: String, onResult: (Boolean) -> Unit) {
        if (name.isBlank() || password.isBlank()) {
            onResult(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newGroupId = groupDao.insertGroup(
                    StudyGroupEntity(
                        name = name,
                        description = description,
                        password = password,
                        creatorEmail = currentUserEmail
                    )
                )
                // Auto-join creator to the group
                groupDao.insertMember(
                    GroupMemberEntity(
                        groupId = newGroupId.toInt(),
                        userEmail = currentUserEmail
                    )
                )
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Failed to create group", e)
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun joinGroup(group: StudyGroupEntity, enteredPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isMember = groupDao.isUserMemberOfGroup(group.id, currentUserEmail)
                if (isMember) {
                    withContext(Dispatchers.Main) {
                        onResult(true, "Already a member of this study group.")
                    }
                    return@launch
                }

                // Check if admin (lumeralabs10@gmail.com) is joining, or if password matches
                val isPasswordCorrect = enteredPassword.trim() == group.password.trim() || isAdminUser
                
                if (isPasswordCorrect) {
                    groupDao.insertMember(
                        GroupMemberEntity(
                            groupId = group.id,
                            userEmail = currentUserEmail
                        )
                    )
                    withContext(Dispatchers.Main) {
                        onResult(true, "Success: Welcome to ${group.name}!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Error: Invalid group password.")
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Failed to join group", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "Error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun leaveGroup(groupId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            groupDao.deleteMember(groupId, currentUserEmail)
        }
    }

    fun deleteGroup(group: StudyGroupEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Only creator or admin can delete group
                if (group.creatorEmail == currentUserEmail || isAdminUser) {
                    groupDao.deleteGroup(group)
                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Failed to delete group", e)
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun shareNoteToGroup(groupId: Int, note: NoteEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                groupDao.insertSharedNote(
                    SharedNoteEntity(
                        groupId = groupId,
                        title = note.title,
                        content = note.content,
                        sharedBy = currentUserEmail
                    )
                )
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Failed to share note", e)
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun deleteSharedNote(sharedNoteId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            groupDao.deleteSharedNoteById(sharedNoteId)
        }
    }

    fun getSharedNotesForGroup(groupId: Int): Flow<List<SharedNoteEntity>> {
        return groupDao.getSharedNotesForGroup(groupId)
    }

    fun getMembersOfGroup(groupId: Int): Flow<List<GroupMemberEntity>> {
        return groupDao.getMembersOfGroup(groupId)
    }

    suspend fun isUserMemberOfGroup(groupId: Int): Boolean {
        if (isAdminUser) return true // Admin is always a group member/can view anything
        return groupDao.isUserMemberOfGroup(groupId, currentUserEmail)
    }
}

/**
 * Compose state observer list helper
 */
fun <T> androidx.compose.runtime.snapshots.SnapshotStateList<T>.clearAndAddAll(list: List<T>) {
    clear()
    addAll(list)
}

fun mutableStateListOf(vararg elements: Pair<String, Boolean>) = 
    androidx.compose.runtime.mutableStateListOf<Pair<String, Boolean>>().apply { addAll(elements) }
