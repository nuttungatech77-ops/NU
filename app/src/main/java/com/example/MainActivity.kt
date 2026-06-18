package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize database and repository inside parent context securely
                val context = LocalContext.current
                val database = remember { NuDatabase.getDatabase(context) }
                val repository = remember { NuRepository(database.nuDao()) }
                val factory = remember { NuViewModelFactory(repository) }
                
                val viewModel: NuViewModel = viewModel(factory = factory)
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NuMainLayout(viewModel)
                }
            }
        }
    }
}

// ==========================================
// Main Super-App Scaffold Layout
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuMainLayout(viewModel: NuViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState(initial = null)
    
    val userName = prefs?.userName ?: "Amina Bello"
    val balance = prefs?.walletBalance ?: 250000.0
    val streak = prefs?.twinStreak ?: 7
    val language = prefs?.preferredLanguage ?: "English"

    // State to toggle the bottom sheet navigation menu catalog
    var showMenuBottomSheet by remember { mutableStateOf(false) }

    // Multi-language greetings dictionary
    val greetingText = when (language) {
        "Hausa" -> "Sannu, $userName"
        "Arabic" -> "أهلاً, $userName"
        "French" -> "Bonjour, $userName"
        else -> "Hello, $userName"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(NuDeepBlueBg),
            bottomBar = {
                NuBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                        showMenuBottomSheet = false
                    },
                    onMenuClick = {
                        showMenuBottomSheet = !showMenuBottomSheet
                    }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            // Premium subtle starry space animated glow background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(NuDeepBlueBg)
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x0C00F2FE), Color.Transparent),
                                radius = size.minDimension * 0.9f
                            )
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cosmic UI Header Block
                    NuHeaderBlock(
                        greeting = greetingText,
                        balance = balance,
                        streak = streak,
                        language = language,
                        onLangChanged = { newLang ->
                            viewModel.updateUserPreferences(
                                UserPreferencesEntity(
                                    id = 1,
                                    userName = userName,
                                    walletBalance = balance,
                                    preferredLanguage = newLang,
                                    twinStreak = streak
                                )
                            )
                        }
                    )

                    // Subtitle/Tagline banner
                    if (currentScreen == Screen.Home) {
                        NuFeaturedBanner()
                    }

                    // AI Progress Loader Overlay indicator
                    if (isAiLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .testTag("ai_progress_indicator"),
                            color = NuPrimaryGold,
                            trackColor = NuGlassWhite
                        )
                    }

                    // Active Core Render Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "ScreenTransition"
                        ) { target ->
                            when (target) {
                                Screen.Home -> ScreenHome(viewModel)
                                Screen.Chat -> ScreenChat(viewModel)
                                Screen.Marketplace -> ScreenMarketplace(viewModel)
                                Screen.BusinessBuilder -> ScreenBusinessBuilder(viewModel)
                                Screen.VideoStudio -> ScreenVideoStudio(viewModel)
                                Screen.SocialNetwork -> ScreenSocialNetwork(viewModel)
                                Screen.Jobs -> ScreenJobs(viewModel)
                                Screen.Health -> ScreenHealth(viewModel)
                                Screen.Travel -> ScreenTravel(viewModel)
                                Screen.Wallet -> ScreenWallet(viewModel)
                                Screen.LifeCopilot -> ScreenLifeCopilot(viewModel)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // -------------------------------------------------------------
        // Premium Sophisticated Dark Custom Bottom Sheet Navigation Menu
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = showMenuBottomSheet,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenuBottomSheet = false }
            )
        }

        AnimatedVisibility(
            visible = showMenuBottomSheet,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Catch & swallow clicks to prevent closing */ },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NuDeepBlueBg),
                border = BorderStroke(1.dp, NuGlassWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header of Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NU SUPER-APP HUB",
                                color = NuPrimaryGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Seamless navigation across modules",
                                color = NuTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = { showMenuBottomSheet = false },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x13FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = NuTextWhite
                            )
                        }
                    }

                    HorizontalDivider(color = NuGlassWhite, thickness = 1.dp)

                    // Render catalog list of ALL 11 modules beautifully
                    val allModules = listOf(
                        Triple(Screen.Home, "Home Core Dashboard", Icons.Default.Home),
                        Triple(Screen.Chat, "AI Intelligence Chat", Icons.Default.Send),
                        Triple(Screen.SocialNetwork, "NU Social Network Forums", Icons.Default.Share),
                        Triple(Screen.Wallet, "Digital Naira Wallet", Icons.Default.Email),
                        Triple(Screen.Marketplace, "Services Marketplace", Icons.Default.ShoppingCart),
                        Triple(Screen.BusinessBuilder, "AI Business Builder", Icons.Default.List),
                        Triple(Screen.VideoStudio, "Video & Media Studio", Icons.Default.PlayArrow),
                        Triple(Screen.Jobs, "NU High-Impact Jobs", Icons.Default.Search),
                        Triple(Screen.Health, "AI Symptom & Health Coach", Icons.Default.Warning),
                        Triple(Screen.Travel, "NU Travel Planner & Guide", Icons.Default.LocationOn),
                        Triple(Screen.LifeCopilot, "Life Copilot & Twin Goal setting", Icons.Default.Face)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f) // Limits modal max height to be perfectly proportioned
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allModules.forEach { (screen, title, icon) ->
                            val isSelected = currentScreen == screen
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0x19D4AF37) else Color(0x05FFFFFF))
                                    .border(
                                        1.dp,
                                        if (isSelected) NuPrimaryGold.copy(alpha = 0.5f) else Color(0x0FFFFFFF),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        viewModel.navigateTo(screen)
                                        showMenuBottomSheet = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) NuPrimaryGold.copy(alpha = 0.2f) else Color(0x0FFFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = if (isSelected) NuPrimaryGold else NuTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = title,
                                        color = if (isSelected) NuPrimaryGold else NuTextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = "Tap to open ${screen.name} module",
                                        color = NuTextMuted,
                                        fontSize = 10.5.sp
                                    )
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
// UI Component: Glassmorphic Top Header Block
// ==========================================
@Composable
fun NuHeaderBlock(
    greeting: String,
    balance: Double,
    streak: Int,
    language: String,
    onLangChanged: (String) -> Unit
) {
    var showLanguageMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NuCardSlate)
            .border(1.dp, NuGlassWhite, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Profile info and Streaks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(NuPrimaryGold, NuAccentGold))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            color = NuDeepBlueBg,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = FontStyle.Italic)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("NU ", color = NuTextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "Intelligence".uppercase(),
                                color = NuPrimaryGold,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            )
                        }
                        Text(greeting, color = NuTextMuted, fontSize = 11.sp)
                    }
                }

                // Language selection dropdown trigger styled like the profile icon in the design HTML
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x0DFFFFFF))
                            .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                            .clickable { showLanguageMenu = true }
                            .testTag("lang_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Language",
                            tint = NuTextWhite.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier.background(NuCardSlate)
                    ) {
                        listOf("English", "Hausa", "Arabic", "French").forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang, color = NuTextWhite) },
                                onClick = {
                                    onLangChanged(lang)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = NuGlassWhite, thickness = 1.dp)

            // Row 2: Finances and Digital Twin Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("NU SECURE WALLET", color = NuTextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "₦%,.2f".format(balance),
                        color = NuPrimaryGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x11FFD700))
                        .border(1.dp, NuBorderGold, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Streak", tint = NuPrimaryGold, modifier = Modifier.size(16.dp))
                        Text(
                            text = "$streak-Day Twin Streak",
                            color = NuPrimaryGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// UI Component: Slogan & Featured Banner
// ==========================================
@Composable
fun NuFeaturedBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0x2E00F2FE), Color(0x20FFD700)),
                )
            )
            .border(1.dp, NuBorderGold, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NuPrimaryGold)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "NU SUPER-APP PLATFORM",
                    color = NuDeepBlueBg,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Anything You Need. One App. One AI.",
                color = NuTextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Learn, work, create, transact, and guide your lifestyle through NU Intelligence Core.",
                color = NuTextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}

// ==========================================
// UI Component: Glassmorphism Action Buttons / Cards
// ==========================================
@Composable
fun NuGlassCard(
    modifier: Modifier = Modifier,
    borderAccent: Color = NuBorderGold,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Premium rounded-3xl matching the Sophisticated Dark look
        colors = CardDefaults.cardColors(containerColor = NuCardSlate),
        border = BorderStroke(1.dp, borderAccent)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

// ==========================================
// Bottom Navigation Bar
// ==========================================
@Composable
fun NuBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onMenuClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) // border-white/5 styled curve top
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = NuDeepBlueBg, // Matches footer BG color #050A18 exactly
        tonalElevation = 8.dp
    ) {
        // 1. Home
        val homeSelected = currentScreen == Screen.Home
        NavigationBarItem(
            selected = homeSelected,
            onClick = { onNavigate(Screen.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (homeSelected) NuPrimaryGold else NuTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Home",
                    color = if (homeSelected) NuPrimaryGold else NuTextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (homeSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x1AD4AF37)
            )
        )

        // 2. AI Assistant
        val aiSelected = currentScreen == Screen.Chat
        NavigationBarItem(
            selected = aiSelected,
            onClick = { onNavigate(Screen.Chat) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "AI Assistant",
                    tint = if (aiSelected) NuPrimaryGold else NuTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "AI Assistant",
                    color = if (aiSelected) NuPrimaryGold else NuTextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (aiSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x1AD4AF37)
            )
        )

        // 3. Social
        val socialSelected = currentScreen == Screen.SocialNetwork
        NavigationBarItem(
            selected = socialSelected,
            onClick = { onNavigate(Screen.SocialNetwork) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Social",
                    tint = if (socialSelected) NuPrimaryGold else NuTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Social",
                    color = if (socialSelected) NuPrimaryGold else NuTextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (socialSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x1AD4AF37)
            )
        )

        // 4. Wallet
        val walletSelected = currentScreen == Screen.Wallet
        NavigationBarItem(
            selected = walletSelected,
            onClick = { onNavigate(Screen.Wallet) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Email, // Email as general representative card
                    contentDescription = "Wallet",
                    tint = if (walletSelected) NuPrimaryGold else NuTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Wallet",
                    color = if (walletSelected) NuPrimaryGold else NuTextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (walletSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x1AD4AF37)
            )
        )

        // 5. Menu
        NavigationBarItem(
            selected = false,
            onClick = onMenuClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = NuTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Menu",
                    color = NuTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0x1AD4AF37)
            )
        )
    }
}

// ==========================================
// HUB 1: MAIN HOME (Quick Hub grid)
// ==========================================
@Composable
fun ScreenHome(viewModel: NuViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Sophisticated Dark Premium Hero Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NuCardSlate)
                .border(1.dp, NuGlassWhite, RoundedCornerShape(24.dp))
                .clickable { viewModel.navigateTo(Screen.Chat) }
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background glow effect
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NuPrimaryGold.copy(alpha = 0.12f), Color.Transparent)
                            ),
                            radius = size.minDimension * 0.9f
                        )
                    }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Life Copilot Active".uppercase(),
                        color = NuPrimaryGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Good morning, Abubakar.",
                        color = NuTextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                }

                // Beautiful voice mic circular indicator containing beautiful active visual
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.dp, NuPrimaryGold.copy(alpha = 0.3f), CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0x0DFFFFFF), Color.Transparent))),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NuPrimaryGold, NuAccentGold))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Voice Assistant Active",
                            tint = NuDeepBlueBg,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = "\"I need to start a business today.\"",
                    color = NuTextWhite.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Services Hub grid
        Text(
            text = "EXPLORE AI SERVICES",
            color = NuTextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        val services = listOf(
            Triple(Screen.Marketplace, "Marketplace", Icons.Default.ShoppingCart),
            Triple(Screen.BusinessBuilder, "Business Builder", Icons.Default.List), // Standard icon definitions
            Triple(Screen.VideoStudio, "Video Studio", Icons.Default.PlayArrow),
            Triple(Screen.SocialNetwork, "NU Social Network", Icons.Default.Share),
            Triple(Screen.Jobs, "NU Jobs Portal", Icons.Default.Search),
            Triple(Screen.Health, "Health Assistant", Icons.Default.Warning),
            Triple(Screen.Travel, "NU Travel Planner", Icons.Default.LocationOn),
            Triple(Screen.Wallet, "Digital Wallet", Icons.Default.Email),
            Triple(Screen.LifeCopilot, "Life Copilot Twin", Icons.Default.Face),
            Triple(Screen.Chat, "Ask AI Brain", Icons.Default.Send)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Render service rows
            services.chunked(2).forEach { rowList ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowList.forEach { (screen, label, icon) ->
                        val isCopilot = screen == Screen.LifeCopilot
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCopilot) Color(0x1F00F2FE) else NuCardSlate)
                                .border(1.dp, if (isCopilot) NuGlowBlue else NuGlassWhite, RoundedCornerShape(12.dp))
                                .clickable { viewModel.navigateTo(screen) }
                                .padding(12.dp)
                                .height(56.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0x0EFFFFFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isCopilot) NuGlowBlue else NuPrimaryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = label,
                                    color = NuTextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        NuDailySuggestionBlock()
    }
}

// ==========================================
// UI Component: Sophisticated Dark Daily Suggestion Card
// ==========================================
@Composable
fun NuDailySuggestionBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp, topStart = 4.dp, bottomStart = 4.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(NuPrimaryGold.copy(alpha = 0.15f), Color.Transparent)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(NuPrimaryGold, Color.Transparent)
                ),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp, topStart = 4.dp, bottomStart = 4.dp)
            )
            .drawBehind {
                // Gold vertical border ribbon matching the Sophisticated Dark look
                drawRect(
                    color = NuPrimaryGold,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                )
            }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY SUGGESTION",
                    color = NuPrimaryGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "AI suggestion indicator",
                    tint = NuPrimaryGold.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "Based on your Business Goals, I've drafted a marketing plan for your new shop. Shall we review the invoices?",
                color = NuTextWhite,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// ==========================================
// HUB 2: CORE AI CHAT
// ==========================================
@Composable
fun ScreenChat(viewModel: NuViewModel) {
    val chatLogs by viewModel.chatHistory.collectAsState()
    val chatText by viewModel.chatInput.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Scroll to bottom when log updates
    LaunchedEffect(chatLogs.size) {
        if (chatLogs.isNotEmpty()) {
            listState.animateScrollToItem(chatLogs.size - 1)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("NU INTELLIGENCE BRAIN", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        // Conversation list
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0E000000))
                .border(1.dp, NuGlassWhite, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            if (chatLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No conversation record. Say 'Hello' to begin!", color = NuTextMuted, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatLogs) { log ->
                        val isUser = log.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .background(if (isUser) Color(0x3BFFD700) else NuCardSlate)
                                    .border(1.dp, if (isUser) NuBorderGold else NuGlassWhite, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = log.text,
                                    color = NuTextWhite,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick suggestions row
        Text("SUGGESTIONS FOR YOU", color = NuTextMuted, fontSize = 11.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf("I need money.", "JAMB Exam Revision", "Create a modern startup logo", "Check flu symptoms")
            suggestions.forEach { sug ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NuGlassWhite)
                        .clickable {
                            viewModel.sendChatMessage(overrideText = sug)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(sug, color = NuPrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Text input form
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatText,
                onValueChange = { viewModel.chatInput.value = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text"),
                placeholder = { Text("Ask NU anything...", color = NuTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NuTextWhite,
                    unfocusedTextColor = NuTextWhite,
                    focusedBorderColor = NuPrimaryGold,
                    unfocusedBorderColor = NuGlassWhite,
                    focusedContainerColor = NuCardSlate,
                    unfocusedContainerColor = NuCardSlate
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    viewModel.sendChatMessage()
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .size(54.dp)
                    .testTag("chat_send_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = NuDeepBlueBg)
            }
        }

        Button(
            onClick = { viewModel.clearChatHistory() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Clear Memory", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// HUB 3: AI MARKETPLACE
// ==========================================
@Composable
fun ScreenMarketplace(viewModel: NuViewModel) {
    val items by viewModel.marketplaceItems.collectAsState()
    
    val title by viewModel.marketTitle.collectAsState()
    val price by viewModel.marketPrice.collectAsState()
    val desc by viewModel.marketDescription.collectAsState()
    val phone by viewModel.marketPhone.collectAsState()
    val type by viewModel.marketType.collectAsState()

    var showPostForm by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI MARKETPLACE", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
                onClick = { showPostForm = !showPostForm },
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
            ) {
                Text(if (showPostForm) "View Feed" else "Post Item +", color = NuDeepBlueBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showPostForm) {
            NuGlassCard(borderAccent = NuBorderGold) {
                Text("POST A PRODUCT OR SERVICE", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SELL", "BUY", "SERVICE").forEach { listType ->
                        Button(
                            onClick = { viewModel.marketType.value = listType },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == listType) NuPrimaryGold else NuGlassWhite
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(listType, color = if (type == listType) NuDeepBlueBg else NuTextWhite, fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.marketTitle.value = it },
                    label = { Text("Title (e.g. Inverter, Freelance Logo etc.)", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { viewModel.marketPrice.value = it },
                    label = { Text("Price (₦)", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { viewModel.marketDescription.value = it },
                    label = { Text("Detailed Description", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.marketPhone.value = it },
                    label = { Text("Contact Phone Recipient", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                Button(
                    onClick = {
                        viewModel.submitMarketListing()
                        showPostForm = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
                ) {
                    Text("Publish to AI Feed", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Display active shop uploads
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NuCardSlate)
                            .border(1.dp, NuBorderGold, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (item.type == "SELL") Color(0xFF2ECC71) else NuPrimaryGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(item.type, color = NuDeepBlueBg, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("₦%,.2f".format(item.price), color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Text(item.title, color = NuTextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text(item.description, color = NuTextMuted, fontSize = 12.sp, lineHeight = 16.sp)

                            Divider(color = NuGlassWhite, modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Contact: ${item.contactPhone}", color = NuTextMuted, fontSize = 10.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Matched", tint = NuGlowBlue, modifier = Modifier.size(12.dp))
                                    Text("AI Auto-matching active", color = NuGlowBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
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
// HUB 4: BUSINESS BUILDER
// ==========================================
@Composable
fun ScreenBusinessBuilder(viewModel: NuViewModel) {
    val idea by viewModel.businessIdeaInput.collectAsState()
    val type by viewModel.businessTypeSelect.collectAsState()
    val kitResult by viewModel.businessKitResult.collectAsState()

    var showTypeMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AI BUSINESS BUILDER", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        NuGlassCard(borderAccent = NuGlowBlue) {
            Text("LAUNCH A VENTURE INSTANTLY", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Text("Select Industry", color = NuTextWhite, fontSize = 11.sp)
            Box {
                Button(
                    onClick = { showTypeMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NuGlassWhite)
                ) {
                    Text(type, color = NuTextWhite)
                }
                DropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false },
                    modifier = Modifier.background(NuCardSlate)
                ) {
                    listOf("Renewable Tech & Solar", "Retail & Diner Cafe", "Freelance Software & Dev", "Regional Agriculture Hub").forEach { itemType ->
                        DropdownMenuItem(
                            text = { Text(itemType, color = NuTextWhite) },
                            onClick = {
                                viewModel.businessTypeSelect.value = itemType
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = idea,
                onValueChange = { viewModel.businessIdeaInput.value = it },
                label = { Text("What is your startup concept?", color = NuTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("E.g., Solar installer store with remote dispatch", color = NuTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
            )

            Button(
                onClick = { viewModel.generateBusinessKit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("business_generate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
            ) {
                Text("Generate Business Kit (1-Click)", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
            }
        }

        // Output Result Panel
        kitResult?.let { result ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE00C192E))
                    .border(1.dp, NuBorderGold, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("GENERATED BRAND ASSETS", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = result,
                        color = NuTextWhite,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

// ==========================================
// HUB 5: VIDEO STUDIO
// ==========================================
@Composable
fun ScreenVideoStudio(viewModel: NuViewModel) {
    val inputConcept by viewModel.videoIdeaInput.collectAsState()
    val videoType by viewModel.videoTypeSelect.collectAsState()
    val videoResult by viewModel.videoStudioResult.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AI VIDEO STUDIO DIRECTOR", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        NuGlassCard(borderAccent = NuGlowBlue) {
            Text("CREATE ADS, ANIMATIONS & CLIPS", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Text("Platform Format", color = NuTextWhite, fontSize = 11.sp)
            Box {
                Button(
                    onClick = { showMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NuGlassWhite)
                ) {
                    Text(videoType, color = NuTextWhite)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(NuCardSlate)
                ) {
                    listOf("TikTok Video", "YouTube explainer", "Cartoons Animation", "Startup Commercial Ad").forEach { v ->
                        DropdownMenuItem(
                            text = { Text(v, color = NuTextWhite) },
                            onClick = {
                                viewModel.videoTypeSelect.value = v
                                showMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = inputConcept,
                onValueChange = { viewModel.videoIdeaInput.value = it },
                label = { Text("What is your video promo topic?", color = NuTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("E.g., Benefits of smart hybrid backup solar setup", color = NuTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
            )

            Button(
                onClick = { viewModel.generateVideoScript() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
            ) {
                Text("Compile Scripts & Subtitles (1-Click)", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
            }
        }

        // Render response
        videoResult?.let { result ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NuCardSlate)
                    .border(1.dp, NuGlowBlue, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AI DIRECTOR STORYBOARD SCRIPT", color = NuGlowBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = result,
                        color = NuTextWhite,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .height(260.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

// ==========================================
// HUB 6: SOCIAL NETWORK
// ==========================================
@Composable
fun ScreenSocialNetwork(viewModel: NuViewModel) {
    val feeds by viewModel.socialPosts.collectAsState()
    val postText by viewModel.postContentSubmit.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("NU SOCIAL NETWORK (INTERLOCK)", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        // Post Publisher Column
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NuCardSlate)
                .border(2.dp, NuGlassWhite, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Share with global communities", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                OutlinedTextField(
                    value = postText,
                    onValueChange = { viewModel.postContentSubmit.value = it },
                    placeholder = { Text("What projects are you working on today?", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                Button(
                    onClick = {
                        viewModel.submitSocialPost()
                        keyboardController?.hide()
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
                ) {
                    Text("Post Update +", color = NuDeepBlueBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feed display
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(feeds) { post ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NuCardSlate)
                        .border(1.dp, NuGlassWhite, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(NuGlassWhite, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(post.authorName.take(2).uppercase(), color = NuPrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(post.authorName, color = NuTextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(post.authorTitle, color = NuTextMuted, fontSize = 10.sp)
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = Color(0xFFFF5252), modifier = Modifier.size(12.dp))
                                Text("${post.likes} LIKES", color = NuTextWhite, fontSize = 10.sp)
                            }
                        }

                        Text(post.content, color = NuTextWhite, fontSize = 12.sp, lineHeight = 16.sp)

                        if (post.isTranslated && post.translatedContent != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x3BFFD700))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🤖 Translated: ${post.translatedContent}",
                                    color = NuTextWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Translate Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (post.isTranslated) {
                                TextButton(onClick = { viewModel.untranslatePost(post) }) {
                                    Text("Show Original", color = NuPrimaryGold, fontSize = 11.sp)
                                }
                            } else {
                                TextButton(onClick = { viewModel.translatePost(post, "Hausa") }) {
                                    Text("Translate to Hausa", color = NuGlowBlue, fontSize = 10.sp)
                                }
                                TextButton(onClick = { viewModel.translatePost(post, "French") }) {
                                    Text("Translate to French", color = NuPrimaryGold, fontSize = 10.sp)
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
// HUB 7: NU JOBS
// ==========================================
@Composable
fun ScreenJobs(viewModel: NuViewModel) {
    val jobList by viewModel.jobs.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("NU JOBS & OPPORTUNITIES", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(jobList) { job ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NuCardSlate)
                        .border(1.dp, if (job.isApplied) NuGlassWhite else NuBorderGold, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(job.title, color = NuTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NuGlassWhite)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(job.type, color = NuPrimaryGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("${job.company} • ${job.location}", color = NuTextMuted, fontSize = 11.sp)
                        Text("Salary: ${job.salary}", color = NuPrimaryGold, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Skills: ${job.skillsRequired}", color = NuTextWhite, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(4.dp))

                        if (job.isApplied) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2ECC71).copy(alpha = 0.2f))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Applied Successfully ✓", color = Color(0xFF2ECC71), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.applyForJob(job) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
                            ) {
                                Text("Submit Real-time Profile", color = NuDeepBlueBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// HUB 8: HEALTH ASSISTANT
// ==========================================
@Composable
fun ScreenHealth(viewModel: NuViewModel) {
    val symText by viewModel.symptomInput.collectAsState()
    val checkerResult by viewModel.symptomCheckerResult.collectAsState()
    val remindersList by viewModel.reminders.collectAsState()
    
    val rTitle by viewModel.reminderTitleInput.collectAsState()
    val rTime by viewModel.reminderTimeInput.collectAsState()

    var showReminderPanel by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI HEALTH ASSISTANT", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
                onClick = { showReminderPanel = !showReminderPanel },
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
            ) {
                Text(if (showReminderPanel) "Symptom Checker" else "My Reminders", color = NuDeepBlueBg, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showReminderPanel) {
            // Reminders Panel
            NuGlassCard(borderAccent = NuPrimaryGold) {
                Text("ADD HEALTH / FITNESS REMINDER", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(
                    value = rTitle,
                    onValueChange = { viewModel.reminderTitleInput.value = it },
                    label = { Text("Topic (e.g. hydration check, meds)", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                OutlinedTextField(
                    value = rTime,
                    onValueChange = { viewModel.reminderTimeInput.value = it },
                    label = { Text("Trigger Time", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                Button(
                    onClick = { viewModel.submitReminder() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
                ) {
                    Text("Add Active Tracker +", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
                }
            }

            // Reminders list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(remindersList) { rem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NuCardSlate)
                            .border(1.dp, NuGlassWhite, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(rem.title, color = NuTextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(rem.time, color = NuPrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = { viewModel.deleteReminder(rem.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                        }
                    }
                }
            }
        } else {
            // Symptom Checker Core
            NuGlassCard(borderAccent = NuBorderGold) {
                Text("INTELLIGENT SYMPTOM TRIAGE", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(
                    value = symText,
                    onValueChange = { viewModel.symptomInput.value = it },
                    placeholder = { Text("What symptoms or health queries are you experiencing today?", color = NuTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                )

                Button(
                    onClick = { viewModel.checkSymptoms() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
                ) {
                    Text("Check Advice Guidelines", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
                }
            }

            // Report Output
            checkerResult?.let { report ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NuCardSlate)
                        .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = report,
                        color = NuTextWhite,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

// ==========================================
// HUB 9: TRAVEL HUB
// ==========================================
@Composable
fun ScreenTravel(viewModel: NuViewModel) {
    val dest by viewModel.travelDestination.collectAsState()
    val days by viewModel.travelDays.collectAsState()
    val guideResult by viewModel.travelGuideResult.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AI TRAVEL COPILOT", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        NuGlassCard(borderAccent = NuPrimaryGold) {
            Text("PLAN ADVENTURE & TRIPS", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            OutlinedTextField(
                value = dest,
                onValueChange = { viewModel.travelDestination.value = it },
                label = { Text("Where do you plan to travel?", color = NuTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
            )

            OutlinedTextField(
                value = days,
                onValueChange = { viewModel.travelDays.value = it },
                label = { Text("Trip duration (days)", color = NuTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
            )

            Button(
                onClick = { viewModel.planTravelGuide() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
            ) {
                Text("Generate Travel Plan (1-Click)", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
            }
        }

        // Render report
        guideResult?.let { plan ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NuCardSlate)
                    .border(1.dp, NuBorderGold, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = plan,
                    color = NuTextWhite,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

// ==========================================
// HUB 10: DIGITAL WALLET & TRANSACTION LISTS
// ==========================================
@Composable
fun ScreenWallet(viewModel: NuViewModel) {
    val txLogs by viewModel.transactions.collectAsState()
    val savings by viewModel.savingsGoals.collectAsState()

    val targetRecip by viewModel.walletTransferTarget.collectAsState()
    val transferAmt by viewModel.walletTransferAmount.collectAsState()
    val transferSuccess by viewModel.isTransferCompleted.collectAsState()

    val goalTitle by viewModel.savingsGoalTitle.collectAsState()
    val goalTarget by viewModel.savingsGoalTarget.collectAsState()

    var activeTab by remember { mutableStateOf("TRANSFER") } // TRANSFER, SAVINGS, RECORDS

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("NU DIGITAL WALLET", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        // Tab Selectors
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("TRANSFER", "SAVINGS", "RECORDS").forEach { currentTab ->
                Button(
                    onClick = { activeTab = currentTab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == currentTab) NuPrimaryGold else NuGlassWhite
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(currentTab, color = if (activeTab == currentTab) NuDeepBlueBg else NuTextWhite, fontSize = 9.sp)
                }
            }
        }

        when (activeTab) {
            "TRANSFER" -> {
                NuGlassCard(borderAccent = NuBorderGold) {
                    Text("PEER-TO-PEER INSTANT PAYMENTS", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    OutlinedTextField(
                        value = targetRecip,
                        onValueChange = { viewModel.walletTransferTarget.value = it },
                        modifier = Modifier.testTag("transfer_target_input"),
                        label = { Text("Recipient email or phone number", color = NuTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                    )

                    OutlinedTextField(
                        value = transferAmt,
                        onValueChange = { viewModel.walletTransferAmount.value = it },
                        modifier = Modifier.testTag("transfer_amount_input"),
                        label = { Text("Transfer amount (₦)", color = NuTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                    )

                    Button(
                        onClick = { viewModel.executeWalletTransfer() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wallet_transfer_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
                    ) {
                        Text("Confirm Secure Payment", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
                    }

                    if (transferSuccess) {
                        Text("Secure payment executed successfully ✓", color = Color(0xFF2ECC71), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            "SAVINGS" -> {
                NuGlassCard(borderAccent = NuGlowBlue) {
                    Text("SET NEW FINANCIAL TARGETS", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { viewModel.savingsGoalTitle.value = it },
                        label = { Text("Goal Title (e.g. Laptop Upgrade)", color = NuTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                    )

                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { viewModel.savingsGoalTarget.value = it },
                        label = { Text("Target reserve amount (₦)", color = NuTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
                    )

                    Button(
                        onClick = { viewModel.addSavingsGoal() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
                    ) {
                        Text("Create Savings Target", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
                    }
                }

                // Render current active layout
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savings) { goal ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NuCardSlate)
                                .border(1.dp, NuGlassWhite, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(goal.title, color = NuTextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("₦%,.2f".format(goal.current), color = NuPrimaryGold, fontSize = 11.sp)
                                    Text("Target: ₦%,.2f".format(goal.target), color = NuTextMuted, fontSize = 11.sp)
                                }

                                val percentage = if (goal.target > 0) (goal.current / goal.target).toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = percentage.coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth().clip(CircleShape),
                                    color = NuPrimaryGold,
                                    trackColor = NuGlassWhite
                                )

                                Button(
                                    onClick = { viewModel.fundSavingsGoal(goal, 10000.0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NuGlassWhite),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Allocate ₦10,000", color = NuPrimaryGold, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            "RECORDS" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(txLogs) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NuCardSlate)
                                .border(1.dp, NuGlassWhite, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tx.title, color = NuTextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Timestamp ID: TX-${tx.timestamp % 10000}", color = NuTextMuted, fontSize = 10.sp)
                            }
                            Text(
                                text = (if (tx.isCredit) "+" else "-") + "₦%,.2f".format(tx.amount),
                                color = if (tx.isCredit) Color(0xFF2ECC71) else Color(0xFFFF5252),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COPILOT DIGITAL TWIN (Ultimate Unique Feature)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenLifeCopilot(viewModel: NuViewModel) {
    val habitsList by viewModel.copilotGoals.collectAsState()
    val coachAdvice by viewModel.copilotCoachAdvice.collectAsState()
    
    val gTitle by viewModel.copilotNewGoalTitle.collectAsState()
    val gCat by viewModel.copilotNewGoalCategory.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("NU LIFE COPILOT (DIGITAL TWIN)", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(NuCardSlate, Color(0x3BFFD700))))
                .border(1.dp, NuBorderGold, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("YOUR DIGITAL CO-PILOT IS ACTIVE", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(
                    text = "A virtual duplicate of your mind studies daily habits and maps custom micro-businesses. Checking off parameters enhances streak multiplier values.",
                    color = NuTextWhite,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Action coach
        NuGlassCard(borderAccent = NuGlowBlue) {
            Text("GENERATE SUCCESS TWIN INSIGHTS", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                text = "Let NU Intelligence core aggregate completed targets and provide custom career planning feedback instantly.",
                color = NuTextMuted,
                fontSize = 11.sp
            )
            Button(
                onClick = { viewModel.askCopilotCoach() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
            ) {
                Text("Request Coaching Consultation (AI)", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
            }
        }

        // Coaching advice output
        coachAdvice?.let { report ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NuCardSlate)
                    .border(2.dp, NuBorderGold, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("COACH TWIN REPORT", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = report,
                        color = NuTextWhite,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .height(200.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }

        // Custom Habit Creator Form
        NuGlassCard(borderAccent = NuGlassWhite) {
            Text("REGISTER A NEW SUCCESS DISCIPLINE", color = NuPrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            
            OutlinedTextField(
                value = gTitle,
                onValueChange = { viewModel.copilotNewGoalTitle.value = it },
                label = { Text("Action or Habit (e.g. Code 2 hours)", color = NuTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NuTextWhite, unfocusedTextColor = NuTextWhite)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SKILL", "BUSINESS", "FINANCE", "HABIT").forEach { cat ->
                    Button(
                        onClick = { viewModel.copilotNewGoalCategory.value = cat },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gCat == cat) NuPrimaryGold else NuGlassWhite
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(cat, color = if (gCat == cat) NuDeepBlueBg else NuTextWhite, fontSize = 8.sp)
                    }
                }
            }

            Button(
                onClick = { viewModel.addCopilotGoal() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NuPrimaryGold)
            ) {
                Text("Insert Active Rule", color = NuDeepBlueBg, fontWeight = FontWeight.Bold)
            }
        }

        // Goals checked list
        Text("MY CORE FOCUS METRICS", color = NuTextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Box(modifier = Modifier.height(280.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habitsList) { goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NuCardSlate)
                            .border(1.dp, if (goal.isCompleted) NuBorderGold else NuGlassWhite, RoundedCornerShape(10.dp))
                            .clickable { viewModel.toggleCopilotGoal(goal) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(if (goal.isCompleted) NuPrimaryGold else NuGlassWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (goal.isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = "Done", tint = NuDeepBlueBg, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = goal.title,
                                color = if (goal.isCompleted) NuPrimaryGold else NuTextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NuGlassWhite)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(goal.category, color = NuGlowBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
