@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.FileProcessor
import com.example.data.database.HistoryEntity
import com.example.data.database.NoteEntity
import com.example.data.database.StudyGroupEntity
import com.example.data.database.GroupMemberEntity
import com.example.data.database.SharedNoteEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// CENTRAL NAVIGATION DESTINATIONS
// ==========================================
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val NOTE_PREVIEW = "note_preview"
    const val REVISION_ZONE = "revision_zone"
    const val HELP_SUPPORT = "help_support"
}

// ==========================================
// LUMERA PREMIUM LAVENDER LOGO DRAWING
// ==========================================
@Composable
fun LumeraLogo(modifier: Modifier = Modifier, pulse: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val currentScale = if (pulse) scale else 1.0f

    Box(
        modifier = modifier
            .scale(currentScale)
            .size(72.dp)
            .drawBehind {
                // Drawing outer cosmic stardust rings
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LavenderAccent.copy(alpha = 0.25f), Color.Transparent),
                        center = center,
                        radius = size.width * 0.7f
                    )
                )

                // Crystalline vector matrix circles
                val radius = size.width * 0.3f
                drawCircle(
                    color = LavenderAccent,
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                )

                // Inner core
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(LavenderAccent, LavenderDark),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    radius = radius * 0.6f,
                    center = center
                )

                // Node connectors representing ML
                val centerX = center.x
                val centerY = center.y
                val dotsCount = 5
                for (i in 0 until dotsCount) {
                    val angle = (2 * Math.PI * i) / dotsCount
                    val dotX = centerX + radius * cos(angle).toFloat()
                    val dotY = centerY + radius * sin(angle).toFloat()

                    drawLine(
                        color = LavenderLight.copy(alpha = 0.6f),
                        start = center,
                        end = Offset(dotX, dotY),
                        strokeWidth = 2.5f
                    )

                    drawCircle(
                        color = LavenderLight,
                        radius = 6f,
                        center = Offset(dotX, dotY)
                    )
                }
            }
    )
}

// ==========================================
// MASTER NAV HOST DEFINITION
// ==========================================
@Composable
fun NoteifyApp(
    authVm: AuthViewModel,
    workspaceVm: WorkspaceViewModel,
    converterVm: ConverterViewModel,
    lumeraVm: LumeraViewModel,
    historyVm: HistoryViewModel,
    groupVm: GroupViewModel
) {
    val navController = rememberNavController()

    NoteifyTheme {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(navController, authVm)
            }
            composable(Routes.LOGIN) {
                LoginScreen(navController, authVm)
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(navController, authVm)
            }
            composable(Routes.MAIN) {
                MainScreen(navController, authVm, workspaceVm, converterVm, lumeraVm, historyVm, groupVm)
            }
            composable(Routes.NOTE_PREVIEW) {
                NotePreviewScreen(navController, workspaceVm, authVm)
            }
            composable(Routes.REVISION_ZONE) {
                RevisionZoneScreen(navController, workspaceVm, authVm)
            }
            composable(Routes.HELP_SUPPORT) {
                HelpSupportScreen(navController)
            }
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN (ANIMATED)
// ==========================================
@Composable
fun SplashScreen(navController: NavController, authVm: AuthViewModel) {
    var startAnim by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(1500, easing = EaseInOutCubic),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.8f,
        animationSpec = tween(1500, easing = EaseOutBack),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnim = true
        delay(2600)
        scope.launch {
            if (!authVm.isLoggedIn) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            } else if (!authVm.hasCompletedOnboarding) {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            } else {
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value).scale(scaleAnim.value)
        ) {
            // Elegant glowing AI node logo for Noteify splash
            LumeraLogo(pulse = true)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Noteify",
                color = PureWhite,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Next-Gen AI Revision & Notes Engine",
                color = TextMutedDark,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Footer branding
            Text(
                text = "DEVELOPED BY LUMERA LABS",
                color = LavenderAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp
            )
        }
    }
}

// ==========================================
// 2. SECURE LOGIN SCREEN (GOOGLE + EMAIL)
// ==========================================
@Composable
fun LoginScreen(navController: NavController, authVm: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LumeraLogo(pulse = false)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Noteify by Lumera Labs",
                color = PureWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Access your elite workspace and study analytics",
                color = TextMutedDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Educational Email") },
                placeholder = { Text("e.g. bhuvanaraajsri15@gmail.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LavenderAccent) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderDark,
                    focusedBorderColor = LavenderAccent,
                    focusedLabelColor = LavenderAccent,
                    unfocusedLabelColor = TextMutedDark,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                modifier = Modifier.fillMaxWidth().testTag("username_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Secure Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LavenderAccent) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderDark,
                    focusedBorderColor = LavenderAccent,
                    focusedLabelColor = LavenderAccent,
                    unfocusedLabelColor = TextMutedDark,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign-In Button
            Button(
                onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    } else {
                        val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                        authVm.login(email, name)
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button")
            ) {
                Text("Sign In with Email", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In (Mock Integration)
            OutlinedButton(
                onClick = {
                    authVm.login("bhuvanaraajsri15@gmail.com", "Bhuvanaraaj Sri")
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                    Toast.makeText(context, "Logged in securely via Google Account", Toast.LENGTH_SHORT).show()
                },
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = LavenderAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", color = PureWhite, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Protected by Lumera Labs Secure Guard",
                color = TextMutedDark,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// 3. INTERACTIVE ONBOARDING TUTORIAL
// ==========================================
@Composable
fun OnboardingScreen(navController: NavController, authVm: AuthViewModel) {
    var step by remember { mutableIntStateOf(0) }

    val steps = listOf(
        Triple(
            "Synthesize Notes from Anything",
            "Upload lecture PDFs, PowerPoint presentation slides, Word files, spreadsheets, or images. Noteify extracts core elements instantly and eliminates empty fluff.",
            Icons.Default.EditNote
        ),
        Triple(
            "Local Offline Conversion",
            "Convert files between major extensions (PPTX to PDF, Image to Text OCR) completely offline. Secure and prompt execution without relying on third-party servers.",
            Icons.Default.Cached
        ),
        Triple(
            "Meet LUMERA AI",
            "Your lavender-themed educational companion and voice mentor. Solves doubts, compiles last-minute bullet notes, and generates customized practice tests.",
            Icons.Default.AutoAwesome
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Noteify Onboarding",
                    color = LavenderAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${step + 1} of 3",
                    color = TextMutedDark,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Body
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(LavenderBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = steps[step].third,
                        contentDescription = null,
                        tint = LavenderAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = steps[step].first,
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = steps[step].second,
                    color = TextMutedDark,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Indicator dots
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == step) 16.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (index == step) LavenderAccent else BorderDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next / Skip Button
            Button(
                onClick = {
                    if (step < 2) {
                        step++
                    } else {
                        authVm.completeOnboarding()
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (step < 2) "Next Feature" else "Explore Workspace",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ==========================================
// 4. MAIN APPS WITH TAB CONTAINER (5 TAB SECTIONS)
// ==========================================
@Composable
fun MainScreen(
    parentNavController: NavController,
    authVm: AuthViewModel,
    workspaceVm: WorkspaceViewModel,
    converterVm: ConverterViewModel,
    lumeraVm: LumeraViewModel,
    historyVm: HistoryViewModel,
    groupVm: GroupViewModel
) {
    var selectedTab by remember { mutableIntStateOf(3) } // default to LUMERA (index 3 now)
    var showCreateNotesSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Propagate logged-in user details to other ViewModels to enable partition
    LaunchedEffect(authVm.userEmail, authVm.isAdmin) {
        workspaceVm.updateCurrentUser(authVm.userEmail, authVm.isAdmin)
        historyVm.updateCurrentUser(authVm.userEmail, authVm.isAdmin)
        lumeraVm.updateCurrentUser(authVm.userEmail, authVm.isAdmin)
        converterVm.updateCurrentUser(authVm.userEmail)
        groupVm.updateCurrentUser(authVm.userEmail, authVm.isAdmin)
    }

    // Document Picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            if (!authVm.isAdmin) {
                Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can generate study notes.", Toast.LENGTH_LONG).show()
            } else {
                workspaceVm.generateAiNotesFromFile(
                    context = context,
                    uri = uri,
                    authVm = authVm,
                    onComplete = { note ->
                        workspaceVm.currentNote = note
                        parentNavController.navigate(Routes.NOTE_PREVIEW)
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LavenderAccent),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Noteify",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PureWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { parentNavController.navigate(Routes.HELP_SUPPORT) },
                        modifier = Modifier.testTag("help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help & Support",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = PureWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkBg,
                tonalElevation = 8.dp
            ) {
                val tabs = mutableListOf(
                    Triple("Converter", Icons.Default.SwapHoriz, 0),
                    Triple("Img to Notes", Icons.Default.Image, 1),
                    Triple("Groups", Icons.Default.Groups, 2),
                    Triple("LUMERA", Icons.Default.AutoAwesome, 3),
                    Triple("History", Icons.Default.History, 4),
                    Triple("Profile", Icons.Default.Person, 5)
                )

                if (authVm.isAdmin) {
                    tabs.add(Triple("Admin Panel", Icons.Default.AdminPanelSettings, 6))
                }

                tabs.forEach { (label, icon, index) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) LavenderAccent else TextMutedDark
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = if (isSelected) LavenderAccent else TextMutedDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = LavenderBg
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (!authVm.isAdmin) {
                        Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can create study notes.", Toast.LENGTH_LONG).show()
                    } else {
                        showCreateNotesSheet = true 
                    }
                },
                containerColor = LavenderAccent,
                contentColor = PureBlack,
                shape = CircleShape,
                modifier = Modifier.testTag("create_notes_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create New Notes",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Read-Only Banner notice for regular non-admin users
                if (!authVm.isAdmin) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFF991B1B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF991B1B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔒 Read-Only Guest Mode: Only Admin (lumeralabs10@gmail.com) can modify notes, convert files or delete records.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    // Display corresponding tab
                    when (selectedTab) {
                        0 -> ConverterTab(converterVm, authVm)
                        1 -> ImageToNotesTab(documentPickerLauncher, workspaceVm, parentNavController, authVm)
                        2 -> GroupsTab(groupVm, authVm, workspaceVm)
                        3 -> LumeraTab(lumeraVm)
                        4 -> HistoryTab(historyVm, workspaceVm, parentNavController, authVm)
                        5 -> ProfileTab(authVm, parentNavController)
                        6 -> if (authVm.isAdmin) AdminPanelTab(authVm, workspaceVm, historyVm, lumeraVm) else ProfileTab(authVm, parentNavController)
                    }
                }
            }


            // Note generation loading overlay
            if (workspaceVm.isGeneratingNotes) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PureBlack.copy(alpha = 0.8f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(color = LavenderAccent, strokeWidth = 5.dp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = workspaceVm.notesGenerationProgress,
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Noteify AI is analyzing documents. REDUNDANCIES are scrubbed automatically.",
                            color = TextMutedDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Central FAB Action Selector Drawer Dialog
            if (showCreateNotesSheet) {
                Dialog(onDismissRequest = { showCreateNotesSheet = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Create Study Notes",
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Choose document source to generate notes",
                                color = TextMutedDark,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            val options = listOf(
                                Quad(
                                    "Upload Files",
                                    "Support PPTX, PDF, DOCX, TXT",
                                    Icons.Default.CloudUpload,
                                    {
                                        showCreateNotesSheet = false
                                        documentPickerLauncher.launch("*/*")
                                    }
                                ),
                                Quad(
                                    "Take Lecture Photo",
                                    "Use Camera with prompt text",
                                    Icons.Default.PhotoCamera,
                                    {
                                        showCreateNotesSheet = false
                                        Toast.makeText(context, "Opening secure Android Camera...", Toast.LENGTH_SHORT).show()
                                        // Auto scan simulated
                                        scope.launch {
                                            delay(1000)
                                            workspaceVm.generateAiNotesFromFile(
                                                context, Uri.parse("file:///simulated_camera_snapshot.jpg"), authVm,
                                                { note ->
                                                    workspaceVm.currentNote = note
                                                    parentNavController.navigate(Routes.NOTE_PREVIEW)
                                                },
                                                { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                                            )
                                        }
                                    }
                                ),
                                Quad(
                                    "Scan Documents",
                                    "OCR text segment extraction",
                                    Icons.Default.DocumentScanner,
                                    {
                                        showCreateNotesSheet = false
                                        Toast.makeText(context, "Scanning scanned documents offline...", Toast.LENGTH_SHORT).show()
                                        scope.launch {
                                            delay(1000)
                                            workspaceVm.generateAiNotesFromFile(
                                                context, Uri.parse("file:///simulated_scanner_page.png"), authVm,
                                                { note ->
                                                    workspaceVm.currentNote = note
                                                    parentNavController.navigate(Routes.NOTE_PREVIEW)
                                                },
                                                { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                                            )
                                        }
                                    }
                                ),
                                Quad(
                                    "Import from Cloud",
                                    "Google Drive & iCloud",
                                    Icons.Default.FolderOpen,
                                    {
                                        showCreateNotesSheet = false
                                        documentPickerLauncher.launch("*/*")
                                    }
                                ),
                                Quad(
                                    "Create Notes Manually",
                                    "Enter markdown and titles",
                                    Icons.Default.Edit,
                                    {
                                        showCreateNotesSheet = false
                                        val mNote = NoteEntity(
                                            title = "Untitled Master Note",
                                            content = "# Title of Study Material\n\nWrite your curriculum notes here...",
                                            folder = "Manual"
                                        )
                                        workspaceVm.saveNote(mNote.title, mNote.content, mNote.folder)
                                        Toast.makeText(context, "Created custom blank note in Workspace", Toast.LENGTH_SHORT).show()
                                        selectedTab = 3 // Move to history list
                                    }
                                )
                            )

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(options) { quad ->
                                    val title = quad.first
                                    val desc = quad.second
                                    val icon = quad.third
                                    val onClickAction = quad.fourth
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PureBlack)
                                            .clickable { onClickAction() }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LavenderBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = LavenderAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(desc, color = TextMutedDark, fontSize = 11.sp)
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMutedDark)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(onClick = { showCreateNotesSheet = false }) {
                                Text("Cancel", color = LavenderAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ==========================================
// TAB 1: FILE FORMAT CONVERTER
// ==========================================
@Composable
fun ConverterTab(converterVm: ConverterViewModel, authVm: AuthViewModel) {
    val context = LocalContext.current
    var selectedTargetFormat by remember { mutableStateOf("pdf") }
    val scope = rememberCoroutineScope()

    // File picker specifically for conversion
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            converterVm.startConversion(context, uri, selectedTargetFormat, authVm)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Branding Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LavenderBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Transform, contentDescription = null, tint = LavenderAccent)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Secure Local Converter", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Developed by Lumera Labs — 100% Offline", color = TextMutedDark, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Target Format Selector
        Text(
            text = "Target Export Format",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        val formats = listOf("PDF", "DOCX", "TXT", "CSV")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            formats.forEach { format ->
                val isSelected = selectedTargetFormat.lowercase() == format.lowercase()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) LavenderAccent else DarkCard)
                        .border(1.dp, if (isSelected) LavenderAccent else BorderDark, RoundedCornerShape(12.dp))
                        .clickable { selectedTargetFormat = format.lowercase() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = format,
                        color = if (isSelected) PureBlack else PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Conversion States Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (converterVm.state) {
                    "idle" -> {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ready to convert", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tap below to select PPTX, PDF, DOCX, CSV, XLSX, or Image files. Conversion runs locally without OpenAI key dependencies.",
                            color = TextMutedDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                if (!authVm.isAdmin) {
                                    Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can convert files.", Toast.LENGTH_LONG).show()
                                } else {
                                    pickerLauncher.launch("*/*") 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("upload_file_button")
                        ) {
                            Text("Select File to Convert", fontWeight = FontWeight.Bold)
                        }
                    }

                    "uploading", "processing", "converting", "generating" -> {
                        CircularProgressIndicator(color = LavenderAccent, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(20.dp))
                        val progressLabel = when (converterVm.state) {
                            "uploading" -> "Uploading file..."
                            "processing" -> "Validating structural layouts..."
                            "converting" -> "Running local translation matrix..."
                            else -> "Generating output downloads..."
                        }
                        Text(progressLabel, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Processing ${converterVm.sourceMetadata?.name ?: "document"} (${converterVm.sourceMetadata?.size ?: ""})",
                            color = TextMutedDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    "ready" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Conversion Succeeded!", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "File has been converted locally. Content format integrity is preserved.",
                            color = TextMutedDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Converted File Details Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PureBlack)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = LavenderAccent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        converterVm.convertedFile?.name ?: "Converted Document",
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Size: ${String.format("%.2f KB", (converterVm.convertedFile?.length() ?: 0L) / 1024f)}",
                                        color = TextMutedDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { converterVm.reset() },
                                border = BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Convert Another", color = PureWhite)
                            }

                            Button(
                                onClick = {
                                    val file = converterVm.convertedFile
                                    if (file != null) {
                                        Toast.makeText(context, "Saving converted file to downloads...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "failed" -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Conversion Failed", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            converterVm.errorMessage ?: "Unknown local processing fault.",
                            color = TextMutedDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { converterVm.reset() },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry Conversion", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: IMAGE TO NOTES SCANNER
// ==========================================
@Composable
fun ImageToNotesTab(
    documentPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    workspaceVm: WorkspaceViewModel,
    parentNavController: NavController,
    authVm: AuthViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LavenderBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Image to Revision Notes", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Noteify programmatically extracts printed or handwritten text from screenshots and image documents, synthesizing detailed revision materials instantly.",
            color = TextMutedDark,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { 
                if (!authVm.isAdmin) {
                    Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can extract image text to notes.", Toast.LENGTH_LONG).show()
                } else {
                    documentPickerLauncher.launch("image/*") 
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Handwritten / Image File", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                if (!authVm.isAdmin) {
                    Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can extract image text to notes.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Initializing Noteify live OCR camera...", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        delay(1200)
                        workspaceVm.generateAiNotesFromFile(
                            context, Uri.parse("file:///camera_ocr_scan.png"), authVm,
                            { note ->
                                workspaceVm.currentNote = note
                                parentNavController.navigate(Routes.NOTE_PREVIEW)
                            },
                            { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                        )
                    }
                }
            },
            border = BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = LavenderAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Snap Instant Document OCR", color = PureWhite, fontSize = 15.sp)
        }
    }
}

// ==========================================
// TAB 3: LUMERA AI ASSISTANT (PERSISTENT CHATS)
// ==========================================
@Composable
fun LumeraTab(lumeraVm: LumeraViewModel) {
    val context = LocalContext.current
    var inputMessage by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val chatSessions by lumeraVm.allSessions.collectAsStateWithLifecycle()
    var showSessionsDrawer by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Conversation Space
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(DarkBg)
        ) {
            // Assistant Sub-header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showSessionsDrawer = !showSessionsDrawer }) {
                        Icon(Icons.Default.Menu, contentDescription = "Dialogue History", tint = LavenderAccent)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("LUMERA Assistant", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (lumeraVm.isAiTyping) "Typing notes..." else "Your Lavender Voice Companion",
                            color = LavenderAccent,
                            fontSize = 11.sp
                        )
                    }
                }

                // Voice assistant button
                IconButton(onClick = {
                    lumeraVm.startVoiceMode()
                    lumeraVm.processSimulatedVoiceInput()
                }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Mode",
                        tint = if (lumeraVm.isVoiceMode) LavenderAccent else PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Message scrolling list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(lumeraVm.activeMessages) { msg ->
                    val text = msg.first
                    val isUser = msg.second
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            // Crystalline Mini AI Logo
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(LavenderBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Speech bubbles
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .background(if (isUser) PureWhite else LavenderBg)
                                .border(
                                    1.dp,
                                    if (isUser) PureWhite else LavenderAccent.copy(alpha = 0.3f),
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .padding(14.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = text,
                                color = if (isUser) PureBlack else PureWhite,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Typist pulsing loader
                if (lumeraVm.isAiTyping) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 38.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LavenderAccent)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LavenderAccent.copy(alpha = 0.6f))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LavenderAccent.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

            // Input Send Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCard)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Ask LUMERA to solve or summarize...", color = TextMutedDark, fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputMessage.isNotBlank()) {
                            lumeraVm.sendMessage(inputMessage)
                            inputMessage = ""
                            keyboardController?.hide()
                        }
                    }),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = LavenderAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = PureBlack,
                        unfocusedContainerColor = PureBlack
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            lumeraVm.sendMessage(inputMessage)
                            inputMessage = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LavenderAccent)
                        .size(44.dp)
                        .testTag("send_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = PureBlack)
                }
            }
        }

        // Persistent Chat Session memory Drawer Dialog overlay
        if (showSessionsDrawer) {
            Dialog(onDismissRequest = { showSessionsDrawer = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AI Persistent Chats", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = {
                                lumeraVm.createNewSession()
                                showSessionsDrawer = false
                            }) {
                                Icon(Icons.Default.AddComment, contentDescription = "New Dialogue", tint = LavenderAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(chatSessions) { session ->
                                val isSelected = session.id == lumeraVm.activeSessionId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) LavenderBg else PureBlack)
                                        .clickable {
                                            lumeraVm.loadSession(session)
                                            showSessionsDrawer = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = if (isSelected) LavenderAccent else TextMutedDark)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = session.title,
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { lumeraVm.deleteSession(session.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { showSessionsDrawer = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Close", color = LavenderAccent)
                        }
                    }
                }
            }
        }
    }

    // Voice Dialogue Simulated Modal Overlay
    if (lumeraVm.isVoiceMode) {
        Dialog(onDismissRequest = { lumeraVm.stopVoiceMode() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, LavenderAccent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("LUMERA Voice Mentor", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Developed by Lumera Labs", color = TextMutedDark, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(40.dp))

                    // Pulsating Glowing Lavender rings
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .drawBehind {
                                drawCircle(
                                    color = LavenderAccent.copy(alpha = 0.2f),
                                    radius = size.width * 0.6f
                                )
                                drawCircle(
                                    color = LavenderAccent.copy(alpha = 0.4f),
                                    radius = size.width * 0.45f
                                )
                                drawCircle(
                                    color = LavenderAccent,
                                    radius = size.width * 0.3f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = PureBlack, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = lumeraVm.voiceSpeechIndicator,
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedButton(
                        onClick = { lumeraVm.stopVoiceMode() },
                        border = BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Exit Voice", color = PureWhite)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: CHRONOLOGICAL HISTORY
// ==========================================
@Composable
fun HistoryTab(
    historyVm: HistoryViewModel,
    workspaceVm: WorkspaceViewModel,
    parentNavController: NavController,
    authVm: AuthViewModel
) {
    val items by historyVm.allHistory.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredItems = items.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search academic records...", color = TextMutedDark, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LavenderAccent) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedBorderColor = LavenderAccent,
                unfocusedBorderColor = BorderDark,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No records found", color = PureWhite, fontWeight = FontWeight.Bold)
                    Text("Your processed files, conversions, and AI notes appear here.", color = TextMutedDark, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // If it's AI Notes type, load and open preview!
                                if (item.type == "notes" || item.content.length > 200) {
                                    val note = NoteEntity(
                                        title = item.title.removePrefix("AI Notes: "),
                                        content = item.content,
                                        folder = "History"
                                    )
                                    workspaceVm.currentNote = note
                                    parentNavController.navigate(Routes.NOTE_PREVIEW)
                                } else {
                                    Toast.makeText(context, "File available local path: ${item.outputFilePath ?: "saved Cache"}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (item.type == "notes") LavenderBg else PureBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.type == "notes") Icons.Default.EditNote else Icons.Default.Cached,
                                    contentDescription = null,
                                    tint = if (item.type == "notes") LavenderAccent else Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.description, color = TextMutedDark, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (item.fileSize != null) {
                                        Text("Size: ${item.fileSize}", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    val dateStr = android.text.format.DateFormat.format("MMM dd, hh:mm a", item.timestamp).toString()
                                    Text(dateStr, color = LavenderAccent, fontSize = 10.sp)
                                }
                            }

                            IconButton(onClick = { 
                                if (!authVm.isAdmin) {
                                    Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can delete history records.", Toast.LENGTH_LONG).show()
                                } else {
                                    historyVm.deleteHistory(item) 
                                }
                            }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 5: STUDY PROFILE & DASHBOARD
// ==========================================
@Composable
fun ProfileTab(authVm: AuthViewModel, parentNavController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar centerpiece card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(LavenderBg)
                        .border(1.5.dp, LavenderAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        authVm.userName.take(2).uppercase(),
                        color = LavenderAccent,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(authVm.userName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(authVm.userEmail, color = TextMutedDark, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // Streak pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LavenderBg)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${authVm.studyStreak} Day Revision Streak!", color = LavenderAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats grid
        Text(
            "Academic Performance Dashboard",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "Notes Generated",
                value = "${authVm.notesGeneratedCount}",
                icon = Icons.Default.EditNote,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Files Ingested",
                value = "${authVm.filesProcessedCount}",
                icon = Icons.Default.CloudQueue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Quiz Score",
                value = "${authVm.quizScorePercent}%",
                icon = Icons.Default.Assignment,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Achievements / Badges row
        Text(
            "Acquired Badges & Credentials",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val badges = listOf(
                Pair("Syllabus Crusher", Icons.Default.MilitaryTech),
                Pair("LUMERA Scholar", Icons.Default.WorkspacePremium),
                Pair("Local Titan", Icons.Default.Memory),
                Pair("Streak Mastery", Icons.Default.EmojiEvents)
            )
            items(badges) { badge ->
                val name = badge.first
                val icon = badge.second
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions List
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                val items = listOf(
                    Triple("Feedback & Features", Icons.Default.RateReview, {}),
                    Triple("Privacy Policy", Icons.Default.Security, {}),
                    Triple("Terms of Service", Icons.Default.Description, {}),
                    Triple("Support & FAQs", Icons.Default.ContactSupport, { parentNavController.navigate(Routes.HELP_SUPPORT) }),
                    Triple("Sign Out", Icons.Default.Logout, { authVm.logout(); parentNavController.navigate(Routes.LOGIN) { popUpTo(0) } })
                )

                items.forEachIndexed { index, (label, icon, onClick) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = if (label == "Sign Out") Color.Red else LavenderAccent)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(label, color = if (label == "Sign Out") Color.Red else PureWhite, fontSize = 14.sp)
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text("Noteify Version 1.0.0", color = TextMutedDark, fontSize = 11.sp)
        Text("MADE BY LUMERA LABS", color = LavenderAccent, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = TextMutedDark, fontSize = 9.sp, textAlign = TextAlign.Center)
        }
    }
}

// ==========================================
// 5. NOTES PREVIEW & WORKSPACE EDITOR
// ==========================================
@Composable
fun NotePreviewScreen(navController: NavController, workspaceVm: WorkspaceViewModel, authVm: AuthViewModel) {
    val note = workspaceVm.currentNote ?: return
    var contentText by remember { mutableStateOf(note.content) }
    var noteTitle by remember { mutableStateOf(note.title) }
    var folderText by remember { mutableStateOf(note.folder) }
    var isEditing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Study Workspace", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                actions = {
                    // Export to PDF
                    IconButton(onClick = {
                        PdfExporter.exportNoteToPdf(context, noteTitle, folderText, contentText)
                    }) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export Study Notes as PDF",
                            tint = LavenderAccent
                        )
                    }

                    // Edit Toggle
                    IconButton(onClick = {
                        if (!authVm.isAdmin) {
                            Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can edit or save notes.", Toast.LENGTH_LONG).show()
                        } else {
                            if (isEditing) {
                                workspaceVm.updateNote(note.copy(title = noteTitle, content = contentText, folder = folderText))
                                Toast.makeText(context, "Study Notes Saved Locally", Toast.LENGTH_SHORT).show()
                            }
                            isEditing = !isEditing
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = "Save / Edit",
                            tint = LavenderAccent
                        )
                    }

                    // Duplicate Note Action
                    IconButton(onClick = {
                        if (!authVm.isAdmin) {
                            Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can duplicate notes.", Toast.LENGTH_LONG).show()
                        } else {
                            workspaceVm.duplicateNote(note)
                            Toast.makeText(context, "Note Duplicated Successfully", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.CopyAll, contentDescription = "Duplicate", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // Note Title Section
            if (isEditing) {
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Note Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite, unfocusedTextColor = PureWhite,
                        focusedBorderColor = LavenderAccent, unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = folderText,
                    onValueChange = { folderText = it },
                    label = { Text("Folder / Category") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite, unfocusedTextColor = PureWhite,
                        focusedBorderColor = LavenderAccent, unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(noteTitle, color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(folderText, color = TextMutedDark, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = { 
                        if (!authVm.isAdmin) {
                            Toast.makeText(context, "Action Denied: Only Admin (lumeralabs10@gmail.com) can bookmark notes.", Toast.LENGTH_LONG).show()
                        } else {
                            workspaceVm.toggleBookmark(note) 
                        }
                    }) {
                        Icon(
                            imageVector = if (note.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = LavenderAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Body Area (Workspace view/edit markdown text)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            // Markdown Preview simple renderer
                            Text(
                                text = contentText,
                                color = PureWhite,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Launch Smart Revision Portal Centerpiece Button!
            Button(
                onClick = { navController.navigate(Routes.REVISION_ZONE) },
                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("launch_smart_revision")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Smart Revision Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export options Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExportButton(label = "Markdown", context = context, filename = noteTitle, text = contentText, folder = folderText)
                ExportButton(label = "PDF", context = context, filename = noteTitle, text = contentText, folder = folderText)
                ExportButton(label = "Word Doc", context = context, filename = noteTitle, text = contentText, folder = folderText)
            }
        }
    }
}

@Composable
fun RowScope.ExportButton(label: String, context: Context, filename: String, text: String, folder: String) {
    OutlinedButton(
        onClick = {
            when (label) {
                "PDF" -> {
                    PdfExporter.exportNoteToPdf(context, filename, folder, text)
                }
                "Markdown" -> {
                    try {
                        val sanitized = filename.replace("[^a-zA-Z0-9]".toRegex(), "_")
                        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${sanitized}_notes.md")
                        file.writeText(text)
                        Toast.makeText(context, "Markdown file saved!", Toast.LENGTH_SHORT).show()
                        
                        val authority = "${context.packageName}.fileprovider"
                        val uri = FileProvider.getUriForFile(context, authority, file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/markdown"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, file.name)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Markdown Study Notes"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to export Markdown: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                "Word Doc" -> {
                    try {
                        val sanitized = filename.replace("[^a-zA-Z0-9]".toRegex(), "_")
                        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${sanitized}_notes.doc")
                        file.writeText(text)
                        Toast.makeText(context, "Word Doc file saved!", Toast.LENGTH_SHORT).show()
                        
                        val authority = "${context.packageName}.fileprovider"
                        val uri = FileProvider.getUriForFile(context, authority, file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/msword"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, file.name)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Word Doc Study Notes"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to export Word Doc: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f)
    ) {
        Icon(
            imageVector = if (label == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Download,
            contentDescription = null,
            tint = LavenderAccent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = PureWhite, fontSize = 11.sp)
    }
}

// ==========================================
// 6. SMART REVISION SYSTEM PORTAL
// ==========================================
@Composable
fun RevisionZoneScreen(navController: NavController, workspaceVm: WorkspaceViewModel, authVm: AuthViewModel) {
    val note = workspaceVm.currentNote ?: return
    var selectedRevisionTool by remember { mutableStateOf("sheets") } // "sheets", "flashcards", "mindmap", "quiz"
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Smart Revision Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // Revision Selector Pills
            val tools = listOf(
                Triple("Revision Sheets", "sheets", Icons.Default.Description),
                Triple("Flashcards", "flashcards", Icons.Default.Layers),
                Triple("Mind Map", "mindmap", Icons.Default.Hub),
                Triple("Interactive Quiz", "quiz", Icons.Default.Quiz)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tools) { tool ->
                    val label = tool.first
                    val mode = tool.second
                    val icon = tool.third
                    val isSelected = selectedRevisionTool == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) LavenderAccent else DarkCard)
                            .clickable { selectedRevisionTool = mode }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) PureBlack else LavenderAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, color = if (isSelected) PureBlack else PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tool Interactive Layout Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkCard)
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                when (selectedRevisionTool) {
                    "sheets" -> {
                        Column {
                            Text("One-Page Quick Revision Sheet", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyColumn {
                                item {
                                    Text(
                                        text = "CORE METRICS:\n" +
                                                "- Subject Core: Academic Revision Material of ${note.title}\n" +
                                                "- Syllabic Chapter: 01 Introduction\n\n" +
                                                "KEY BULLETS SUMMARY:\n" +
                                                "1. Material parsing handles layout, redundant noise elimination, and paragraph extraction.\n" +
                                                "2. 100% local PDF compilation utilizes PdfDocument without third-party network APIs.\n" +
                                                "3. Flashcard visual grids support dual-sided rotational perspective mappings.\n\n" +
                                                "FORMULAS & EQUATIONS:\n" +
                                                "- efficiencyFactor = (timeSpent * concentrationLevel) / distractionsLevel\n" +
                                                "- retentionPercent = e^(-decayConstant * daysSinceReading)",
                                        color = PureWhite,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    "flashcards" -> {
                        // Flashing Flip-able 3D visual Flashcards!
                        var isFlipped by remember { mutableStateOf(false) }
                        val flipRotation by animateFloatAsState(
                            targetValue = if (isFlipped) 180f else 0f,
                            animationSpec = tween(600, easing = EaseInOutSine),
                            label = "rotation"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("Visual Spaced Recall Deck", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Tap the flashcard to flip and check answers", color = TextMutedDark, fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(40.dp))

                            Box(
                                modifier = Modifier
                                    .size(width = 280.dp, height = 180.dp)
                                    .graphicsLayer {
                                        rotationY = flipRotation
                                        cameraDistance = 8 * density
                                    }
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isFlipped) LavenderBg else PureBlack)
                                    .border(1.5.dp, LavenderAccent, RoundedCornerShape(16.dp))
                                    .clickable { isFlipped = !isFlipped },
                                contentAlignment = Alignment.Center
                            ) {
                                // Prevent visual mirroring of back text
                                val textScale = if (flipRotation > 90f) -1f else 1f
                                Box(modifier = Modifier.graphicsLayer { scaleX = textScale }) {
                                    if (flipRotation <= 90f) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                            Text("QUESTION", color = LavenderAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                "What programmatic component in Noteify converts screenshots to editable text?",
                                                color = PureWhite,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                            Text("ANSWER CORE", color = LavenderLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                "The Local Image OCR Synthesis Engine developed by Lumera Labs.",
                                                color = PureWhite,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedButton(onClick = { isFlipped = false }, border = BorderStroke(1.dp, BorderDark)) {
                                    Text("Got it wrong", color = PureWhite)
                                }
                                Button(
                                    onClick = {
                                        isFlipped = false
                                        Toast.makeText(context, "Logged inside spaced-repetition memory!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack)
                                ) {
                                    Text("Perfect Recall", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    "mindmap" -> {
                        // Drawing interactive vector nodes Mind Map in Compose Canvas!
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                            Text("Interactive Core Mind Map", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                    .background(PureBlack)
                            ) {
                                val cx = size.width / 2
                                val cy = size.height / 2

                                // Connector lines
                                val angles = listOf(30f, 120f, 210f, 300f)
                                val radius = size.width * 0.28f
                                angles.forEach { angle ->
                                    val rad = Math.toRadians(angle.toDouble())
                                    val endX = cx + radius * cos(rad).toFloat()
                                    val endY = cy + radius * sin(rad).toFloat()

                                    drawLine(
                                        color = LavenderAccent,
                                        start = Offset(cx, cy),
                                        end = Offset(endX, endY),
                                        strokeWidth = 3f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )

                                    drawCircle(
                                        color = LavenderAccent,
                                        radius = 28f,
                                        center = Offset(endX, endY)
                                    )
                                }

                                // Draw center centerpiece node
                                drawCircle(
                                    color = LavenderDark,
                                    radius = 48f,
                                    center = Offset(cx, cy)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Center: ${note.title.take(15)} → Nodes: Definitions, Formulas, summaries, exercises.", color = TextMutedDark, fontSize = 10.sp)
                        }
                    }

                    "quiz" -> {
                        // Interactive MCQ Quiz layout!
                        var currentQuestionIndex by remember { mutableIntStateOf(0) }
                        var selectedOption by remember { mutableStateOf<String?>(null) }
                        var scoreSum by remember { mutableIntStateOf(0) }
                        var showScoreCard by remember { mutableStateOf(false) }

                        val questions = listOf(
                            Triple(
                                "What primary function does Noteify's Local PDF converter provide?",
                                listOf("A) Translates speech", "B) Synthesizes Canvas vectors", "C) Generates local high-fidelity PDFs from text"),
                                "C) Generates local high-fidelity PDFs from text"
                            ),
                            Triple(
                                "Who is the lead development laboratory for Noteify?",
                                listOf("A) OpenSource Org", "B) Lumera Labs", "C) OpenAI Partners"),
                                "B) Lumera Labs"
                            )
                        )

                        if (showScoreCard) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Quiz Finished!", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Your Score: $scoreSum of ${questions.size}", color = LavenderLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        currentQuestionIndex = 0
                                        selectedOption = null
                                        scoreSum = 0
                                        showScoreCard = false
                                        authVm.updateQuizScore(((scoreSum.toFloat() / questions.size.toFloat()) * 100f).toInt())
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Retake Quiz", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            val activeQuestion = questions[currentQuestionIndex]
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "Question ${currentQuestionIndex + 1} of ${questions.size}",
                                    color = LavenderAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(activeQuestion.first, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.height(24.dp))

                                activeQuestion.second.forEach { option ->
                                    val isCurrentSelection = selectedOption == option
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCurrentSelection) LavenderBg else PureBlack)
                                            .border(1.5.dp, if (isCurrentSelection) LavenderAccent else BorderDark, RoundedCornerShape(12.dp))
                                            .clickable { selectedOption = option }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(option, color = PureWhite, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    onClick = {
                                        if (selectedOption != null) {
                                            if (selectedOption == activeQuestion.third) {
                                                scoreSum++
                                            }
                                            if (currentQuestionIndex < questions.size - 1) {
                                                currentQuestionIndex++
                                                selectedOption = null
                                            } else {
                                                showScoreCard = true
                                            }
                                        } else {
                                            Toast.makeText(context, "Please choose an answer", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Submit Option", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. FAQ & HELP SUPPORT COMPOSABLE
// ==========================================
@Composable
fun HelpSupportScreen(navController: NavController) {
    val context = LocalContext.current
    val faqs = listOf(
        Pair("What formats can I upload?", "Noteify fully parses PPT, PPTX, PDF, DOCX, TXT, CSV, XLSX spreadsheets, and handwritten images."),
        Pair("Does Noteify support offline mode?", "Yes! Secure local format conversion (PPTX to PDF, Image to PDF, DOCX formatting) runs entirely locally on-device without third-party API dependencies."),
        Pair("How do I contact developers?", "You can email Lumera Labs team directly at lumeralabs10@gmail.com for prompt custom integration requests."),
        Pair("How does the AI optimize cognitive load?", "Noteify programmatically scans documents, isolating primary headings and equations, discarding empty text fillers automatically.")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Noteify Support Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(LavenderBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MailOutline, contentDescription = null, tint = LavenderAccent)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Direct Support Email", color = PureWhite, fontWeight = FontWeight.Bold)
                    Text("lumeralabs10@gmail.com", color = LavenderAccent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:lumeralabs10@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Noteify Platform Support Request")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email client found. Email us: lumeralabs10@gmail.com", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Draft Support Mail", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Frequently Asked Questions", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(faqs) { faq ->
                    val q = faq.first
                    val a = faq.second
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(q, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(a, color = TextMutedDark, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 6: ADMIN CONTROL PANEL
// ==========================================
@Composable
fun AdminPanelTab(
    authVm: AuthViewModel,
    workspaceVm: WorkspaceViewModel,
    historyVm: HistoryViewModel,
    lumeraVm: LumeraViewModel
) {
    val context = LocalContext.current
    val users by authVm.allUsers.collectAsStateWithLifecycle()
    val allNotes by workspaceVm.allNotes.collectAsStateWithLifecycle()
    val allHistory by historyVm.allHistory.collectAsStateWithLifecycle()

    var selectedUserFilter by remember { mutableStateOf<String?>(null) } // null means "All Users"

    val filteredNotes = if (selectedUserFilter == null) {
        allNotes
    } else {
        allNotes.filter { it.userEmail.trim().lowercase() == selectedUserFilter!!.trim().lowercase() }
    }

    val filteredHistory = if (selectedUserFilter == null) {
        allHistory
    } else {
        allHistory.filter { it.userEmail.trim().lowercase() == selectedUserFilter!!.trim().lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Admin Header
        Card(
            colors = CardDefaults.cardColors(containerColor = LavenderBg),
            border = BorderStroke(1.5.dp, LavenderAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LavenderAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PureBlack)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Lumera Labs Admin System", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Root Administrator Console", color = TextMutedDark, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Authorized Admin Account: ${authVm.userEmail}",
                    color = LavenderAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Total Users",
                value = "${users.size}",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Total Notes",
                value = "${allNotes.size}",
                icon = Icons.Default.EditNote,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Total Files/Convs",
                value = "${allHistory.size}",
                icon = Icons.Default.History,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Registered Users Section
        Text(
            text = "Registered Academic Users",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (users.isEmpty()) {
            Text("No users registered yet.", color = TextMutedDark, fontSize = 12.sp)
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    users.forEachIndexed { index, user ->
                        val isSelected = selectedUserFilter == user.email
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) LavenderBg.copy(alpha = 0.5f) else Color.Transparent)
                                .clickable {
                                    selectedUserFilter = if (isSelected) null else user.email
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(LavenderAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    color = LavenderAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (user.email == "lumeralabs10@gmail.com") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(LavenderAccent)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("ADMIN", color = PureBlack, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(user.email, color = TextMutedDark, fontSize = 11.sp)
                            }
                            // Filter indicator
                            Icon(
                                imageVector = if (isSelected) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                                contentDescription = null,
                                tint = if (isSelected) LavenderAccent else TextMutedDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (index < users.size - 1) {
                            HorizontalDivider(color = BorderDark, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Aggregate Data List
        val filterLabel = if (selectedUserFilter == null) "All Users" else selectedUserFilter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workspace Notes ($filterLabel)",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (selectedUserFilter != null) {
                TextButton(onClick = { selectedUserFilter = null }) {
                    Text("Clear Filter", color = LavenderAccent, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No workspace notes found for this filter.", color = TextMutedDark, fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredNotes.forEach { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                workspaceVm.currentNote = note
                                Toast.makeText(context, "Opening Admin View of: ${note.title}", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(note.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(LavenderBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(note.folder, color = LavenderAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.content.take(120) + if (note.content.length > 120) "..." else "",
                                color = TextMutedDark,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Owner: ${note.userEmail.ifBlank { "Unassigned" }}",
                                    color = LavenderAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val dateStr = android.text.format.DateFormat.format("MMM dd, yyyy", note.timestamp).toString()
                                Text(dateStr, color = TextMutedDark, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Section
        Text(
            text = "User Files & History Records ($filterLabel)",
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No files or history records found for this filter.", color = TextMutedDark, fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredHistory.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LavenderBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.type == "notes") Icons.Default.EditNote else Icons.Default.Cached,
                                    contentDescription = null,
                                    tint = LavenderAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.description, color = TextMutedDark, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("User: ${item.userEmail.ifBlank { "Unassigned" }}", color = LavenderAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    if (item.fileSize != null) {
                                        Text("Size: ${item.fileSize}", color = TextMutedDark, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ==========================================
// TAB 2: COLLABORATIVE STUDY GROUPS
// ==========================================
@Composable
fun GroupsTab(
    groupVm: GroupViewModel,
    authVm: AuthViewModel,
    workspaceVm: WorkspaceViewModel
) {
    val context = LocalContext.current
    val groups by groupVm.allGroups.collectAsStateWithLifecycle(emptyList())
    val allNotes by workspaceVm.allNotes.collectAsStateWithLifecycle(emptyList())

    var selectedGroup by remember { mutableStateOf<StudyGroupEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var groupToJoin by remember { mutableStateOf<StudyGroupEntity?>(null) }
    var joinPasswordText by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var activeReadNote by remember { mutableStateOf<SharedNoteEntity?>(null) }
    var showShareNoteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedGroup != null) {
            GroupDetailScreen(
                group = selectedGroup!!,
                groupVm = groupVm,
                authVm = authVm,
                allMyNotes = allNotes,
                onBack = { selectedGroup = null },
                onReadSharedNote = { activeReadNote = it },
                onOpenShareDialog = { showShareNoteDialog = true }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = LavenderBg),
                    border = BorderStroke(1.5.dp, LavenderAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LavenderAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = PureBlack)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Classmate Study Circles", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Create private groups to share study notes with friends", color = TextMutedDark, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Bar: Create Group Button
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Study Circle", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // List Header
                Text(
                    text = "Active Study Circles",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No study groups found. Be the first to create one!", color = TextMutedDark, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        groups.forEach { group ->
                            GroupListItem(
                                group = group,
                                groupVm = groupVm,
                                authVm = authVm,
                                onEnter = { selectedGroup = group },
                                onJoinClick = {
                                    joinPasswordText = ""
                                    isPasswordVisible = false
                                    groupToJoin = group
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Create Group Dialog
        if (showCreateDialog) {
            CreateGroupDialog(
                groupVm = groupVm,
                onDismiss = { showCreateDialog = false },
                onSuccess = {
                    showCreateDialog = false
                    Toast.makeText(context, "Study group created successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Join Group Dialog (Requires Password)
        if (groupToJoin != null) {
            val group = groupToJoin!!
            AlertDialog(
                onDismissRequest = { groupToJoin = null },
                containerColor = DarkCard,
                tonalElevation = 8.dp,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = LavenderAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Enter Password", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "To access \"${group.name}\", please enter the group security password created by its members.",
                            color = TextMutedDark,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = joinPasswordText,
                            onValueChange = { joinPasswordText = it },
                            label = { Text("Group Password") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = LavenderAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedLabelColor = LavenderAccent,
                                unfocusedLabelColor = TextMutedDark
                            ),
                            visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextMutedDark
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // If user is Admin, display password as help
                        if (authVm.isAdmin) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "👑 Admin Help: Group Password is \"${group.password}\"",
                                color = LavenderAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            groupVm.joinGroup(group, joinPasswordText) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    groupToJoin = null
                                    selectedGroup = group
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack)
                    ) {
                        Text("Verify & Join", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { groupToJoin = null }) {
                        Text("Cancel", color = TextMutedDark)
                    }
                }
            )
        }

        // Shared Note Detail Reading Dialog
        if (activeReadNote != null) {
            val note = activeReadNote!!
            AlertDialog(
                onDismissRequest = { activeReadNote = null },
                containerColor = DarkCard,
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = LavenderAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(note.title, color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Shared by Classmate: ${note.sharedBy}",
                            color = LavenderAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(note.content, color = PureWhite, fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { activeReadNote = null },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Note selector dialog to share
        if (showShareNoteDialog && selectedGroup != null) {
            val group = selectedGroup!!
            AlertDialog(
                onDismissRequest = { showShareNoteDialog = false },
                containerColor = DarkCard,
                title = {
                    Text("Share Study Guide", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        Text(
                            text = "Select a study guide from your personal workspace to share with group classmates:",
                            color = TextMutedDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (allNotes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Your workspace is empty.", color = TextMutedDark, fontSize = 13.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allNotes.forEach { note ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DarkBg),
                                        border = BorderStroke(1.dp, BorderDark),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                groupVm.shareNoteToGroup(group.id, note) { success ->
                                                    if (success) {
                                                        Toast.makeText(context, "Note shared successfully!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Failed to share note.", Toast.LENGTH_SHORT).show()
                                                    }
                                                    showShareNoteDialog = false
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.EditNote, contentDescription = null, tint = LavenderAccent)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(note.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(note.folder, color = TextMutedDark, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showShareNoteDialog = false }) {
                        Text("Cancel", color = TextMutedDark)
                    }
                }
            )
        }
    }
}

@Composable
fun GroupListItem(
    group: StudyGroupEntity,
    groupVm: GroupViewModel,
    authVm: AuthViewModel,
    onEnter: () -> Unit,
    onJoinClick: () -> Unit
) {
    val context = LocalContext.current
    var isMember by remember { mutableStateOf(false) }

    LaunchedEffect(group.id, groupVm.currentUserEmail) {
        isMember = groupVm.isUserMemberOfGroup(group.id)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(group.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                
                // Password status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isMember) LavenderBg else Color(0xFF1E1B4B))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMember) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isMember) LavenderAccent else Color(0xFFA5B4FC),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMember) "Member" else "Private",
                            color = if (isMember) LavenderAccent else Color(0xFFA5B4FC),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(group.description, color = TextMutedDark, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Created by: ${group.creatorEmail}", color = LavenderAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    // If Admin or Member, show password
                    if (authVm.isAdmin || isMember) {
                        Text("🔑 password: ${group.password}", color = LavenderAccent, fontSize = 10.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Delete Group Action (Only Creator or Admin)
                    if (group.creatorEmail == groupVm.currentUserEmail || authVm.isAdmin) {
                        IconButton(
                            onClick = {
                                groupVm.deleteGroup(group) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Study group deleted.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Group", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Action Button: Enter or Join
                    Button(
                        onClick = {
                            if (isMember) onEnter() else onJoinClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMember) LavenderBg else LavenderAccent,
                            contentColor = if (isMember) LavenderAccent else PureBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (isMember) "Enter" else "Join Circle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupDetailScreen(
    group: StudyGroupEntity,
    groupVm: GroupViewModel,
    authVm: AuthViewModel,
    allMyNotes: List<NoteEntity>,
    onBack: () -> Unit,
    onReadSharedNote: (SharedNoteEntity) -> Unit,
    onOpenShareDialog: () -> Unit
) {
    val context = LocalContext.current
    val sharedNotes by groupVm.getSharedNotesForGroup(group.id).collectAsStateWithLifecycle(emptyList())
    val members by groupVm.getMembersOfGroup(group.id).collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Go Back", tint = LavenderAccent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to All Study Circles", color = LavenderAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Group Meta Card
        Card(
            colors = CardDefaults.cardColors(containerColor = LavenderBg),
            border = BorderStroke(1.5.dp, LavenderAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(group.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(group.description, color = TextMutedDark, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Circle Creator: ${group.creatorEmail}", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("🔑 Group Password: ${group.password}", color = LavenderAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (group.creatorEmail != groupVm.currentUserEmail && !authVm.isAdmin) {
                        TextButton(
                            onClick = {
                                groupVm.leaveGroup(group.id)
                                onBack()
                                Toast.makeText(context, "You left this study group.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Leave Circle", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Classmate Members Row
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Joined Classmates (${members.size})", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (members.isEmpty()) {
                    Text("No classmates registered in this circle.", color = TextMutedDark, fontSize = 11.sp)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        members.forEach { member ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BorderDark)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = LavenderAccent, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(member.userEmail, color = PureWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Shared Notes Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shared Study Notes", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            
            Button(
                onClick = onOpenShareDialog,
                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sharedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No study notes shared yet.\nClick \"Share Note\" to post the first one!", color = TextMutedDark, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sharedNotes.forEach { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReadSharedNote(note) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(note.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                
                                // Delete/Remove option for creator, admin, or classmate who shared
                                if (note.sharedBy == groupVm.currentUserEmail || group.creatorEmail == groupVm.currentUserEmail || authVm.isAdmin) {
                                    IconButton(
                                        onClick = { groupVm.deleteSharedNote(note.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove shared note", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.content.take(150) + if (note.content.length > 150) "..." else "",
                                color = TextMutedDark,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Classmate: ${note.sharedBy}", color = LavenderAccent, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                val dateStr = android.text.format.DateFormat.format("MMM dd, yyyy", note.timestamp).toString()
                                Text(dateStr, color = TextMutedDark, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun CreateGroupDialog(
    groupVm: GroupViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var isPassVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = LavenderAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Study Circle", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Start a private academic group with your classmates to securely collaborate.", color = TextMutedDark, fontSize = 12.sp)

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Circle Name (e.g., Physics Group A)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = LavenderAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = LavenderAccent,
                        unfocusedLabelColor = TextMutedDark
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Short Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = LavenderAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = LavenderAccent,
                        unfocusedLabelColor = TextMutedDark
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("Group Password (created by member)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = LavenderAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = LavenderAccent,
                        unfocusedLabelColor = TextMutedDark
                    ),
                    visualTransformation = if (isPassVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPassVisible = !isPassVisible }) {
                            Icon(
                                imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = TextMutedDark
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.isNotBlank() && passwordText.isNotBlank()) {
                        groupVm.createGroup(nameText, descText, passwordText) { success ->
                            if (success) onSuccess()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent, contentColor = PureBlack)
            ) {
                Text("Create Circle", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedDark)
            }
        }
    )
}

