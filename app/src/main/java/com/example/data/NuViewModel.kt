package com.example.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log

enum class Screen {
    Home, Chat, Marketplace, BusinessBuilder, VideoStudio, SocialNetwork, Jobs, Health, Travel, Wallet, LifeCopilot
}

class NuViewModel(private val repository: NuRepository) : ViewModel() {

    // ==========================================
    // 1. Reactive DB Observables (Room)
    // ==========================================
    val userPreferences = repository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val chatHistory = repository.chatHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val socialPosts = repository.socialPosts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val marketplaceItems = repository.marketplaceItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val jobs = repository.jobs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savingsGoals = repository.savingsGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminders = repository.reminders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val copilotGoals = repository.copilotGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ==========================================
    // 2. UI Action State Flows
    // ==========================================
    private val _currentScreen = MutableStateFlow(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Temporary Form Inputs
    val chatInput = MutableStateFlow("")

    // Marketplace Form
    val marketTitle = MutableStateFlow("")
    val marketPrice = MutableStateFlow("")
    val marketDescription = MutableStateFlow("")
    val marketPhone = MutableStateFlow("")
    val marketType = MutableStateFlow("SELL") // SELL, BUY, SERVICE

    // Social Post Form
    val postContentSubmit = MutableStateFlow("")

    // Business Builder Form
    val businessIdeaInput = MutableStateFlow("")
    val businessTypeSelect = MutableStateFlow("Retail & Cafe")
    private val _businessKitResult = MutableStateFlow<String?>(null)
    val businessKitResult: StateFlow<String?> = _businessKitResult.asStateFlow()

    // Video Studio Form
    val videoIdeaInput = MutableStateFlow("")
    val videoTypeSelect = MutableStateFlow("TikTok Video")
    private val _videoStudioResult = MutableStateFlow<String?>(null)
    val videoStudioResult: StateFlow<String?> = _videoStudioResult.asStateFlow()

    // Education Form & States
    val eduTopicInput = MutableStateFlow("JAMB Physics - Electromagnetic Waves")
    private val _eduMaterialResult = MutableStateFlow<String?>(null)
    val eduMaterialResult: StateFlow<String?> = _eduMaterialResult.asStateFlow()
    private val _eduQuizResult = MutableStateFlow<String?>(null)
    val eduQuizResult: StateFlow<String?> = _eduQuizResult.asStateFlow()

    // Symptom Tracker Form
    val symptomInput = MutableStateFlow("")
    private val _symptomCheckerResult = MutableStateFlow<String?>(null)
    val symptomCheckerResult: StateFlow<String?> = _symptomCheckerResult.asStateFlow()
    val reminderTitleInput = MutableStateFlow("")
    val reminderTimeInput = MutableStateFlow("08:00 AM")

    // Travel Guide Form
    val travelDestination = MutableStateFlow("Bauchi Yankari Game Reserve Safari")
    val travelDays = MutableStateFlow("4")
    private val _travelGuideResult = MutableStateFlow<String?>(null)
    val travelGuideResult: StateFlow<String?> = _travelGuideResult.asStateFlow()

    // Wallet States
    val walletTransferTarget = MutableStateFlow("")
    val walletTransferAmount = MutableStateFlow("")
    val isTransferCompleted = MutableStateFlow(false)
    val savingsGoalTitle = MutableStateFlow("")
    val savingsGoalTarget = MutableStateFlow("")

    // Custom Digital Twin success coach log
    private val _copilotCoachAdvice = MutableStateFlow<String?>(null)
    val copilotCoachAdvice: StateFlow<String?> = _copilotCoachAdvice.asStateFlow()
    val copilotNewGoalTitle = MutableStateFlow("")
    val copilotNewGoalCategory = MutableStateFlow("SKILL")

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    // ==========================================
    // 3. User Actions / Implementation Methods
    // ==========================================

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // A. CORE ASSISTANT CHAT
    fun sendChatMessage(overrideText: String? = null) {
        val userPrompt = overrideText ?: chatInput.value
        if (userPrompt.isBlank()) return

        if (overrideText == null) {
            chatInput.value = ""
        }

        viewModelScope.launch {
            // Save User Message
            repository.insertChat(ChatEntity(sender = "user", text = userPrompt))
            _isAiLoading.value = true

            // Generate Prompt System Instruction
            val systemIns = """
                You are NU Intelligence, the ultimate super-app brain. Your tagline is 'Anything You Need. One App. One AI.'
                Be professional, warm, supportive, and visionary. Avoid generic AI layouts. 
                Answer in the selected language preset or default to English. If asked, suggest local jobs, grants, or learning materials.
            """.trimIndent()

            val reply = GeminiService.generateAnswer(userPrompt, systemPrompt = systemIns)
            repository.insertChat(ChatEntity(sender = "ai", text = reply))
            _isAiLoading.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // B. AI SOCIAL TRANSLATION & POSTINGS
    fun submitSocialPost() {
        val c = postContentSubmit.value
        if (c.isBlank()) return
        postContentSubmit.value = ""

        viewModelScope.launch {
            repository.insertPost(
                PostEntity(
                    authorName = "Amina Bello (You)",
                    authorTitle = "Digital Creator",
                    content = c,
                    likes = 0
                )
            )
        }
    }

    fun translatePost(post: PostEntity, targetLanguage: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = "Translate the following social media post into $targetLanguage. Return only the core translated text itself: \n\n\"${post.content}\""
            val result = GeminiService.generateAnswer(prompt, "You are a professional multilingual translator. Respond ONLY with the translation.")
            repository.updatePost(
                post.copy(
                    translatedContent = result,
                    isTranslated = true
                )
            )
            _isAiLoading.value = false
        }
    }

    fun untranslatePost(post: PostEntity) {
        viewModelScope.launch {
            repository.updatePost(post.copy(isTranslated = false))
        }
    }

    // C. AI MARKETPLACE
    fun submitMarketListing() {
        val t = marketTitle.value
        val p = marketPrice.value.toDoubleOrNull() ?: 1000.0
        val d = marketDescription.value
        val ph = marketPhone.value
        val ty = marketType.value

        if (t.isBlank() || d.isBlank()) return

        marketTitle.value = ""
        marketPrice.value = ""
        marketDescription.value = ""
        marketPhone.value = ""

        viewModelScope.launch {
            repository.insertMarketplaceItem(
                MarketplaceEntity(
                    title = t,
                    price = p,
                    type = ty,
                    description = d,
                    contactPhone = ph
                )
            )

            // Auto AI Matching Engine simulation
            _isAiLoading.value = true
            val matchPrompt = "An item was uploaded to our marketplace: Title: $t, Decr: $d, Price: ₦$p. Simulate a prospective matched buyer or local contact who wants to hire/buy this. Write a 2-sentence match proposal from someone needing this."
            val reply = GeminiService.generateAnswer(matchPrompt, "You are our smart Matching Engine. Keep it brief, realistic and encouraging.")
            
            // Insert match response directly as a neat notification chat log
            repository.insertChat(ChatEntity(
                sender = "ai",
                text = "✨ **NU Marketplace Match!** We automatically matched your upload \"$t\" with local opportunities:\n\n$reply"
            ))
            _isAiLoading.value = false
        }
    }

    // D. AI BUSINESS BUILDER
    fun generateBusinessKit() {
        val idea = businessIdeaInput.value
        if (idea.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = """
                Generate a complete startup kit for business idea: '$idea' of type '${businessTypeSelect.value}'.
                Please output the following distinct elements cleanly formatted with markdown:
                1. Catchy modern English & Hausa/French Slogan
                2. One-page Website landing draft content outline
                3. Business Launch Checklist
                4. Sample invoice line-items for premium services
                5. A 3-step marketing campaign plan
            """.trimIndent()
            
            val result = GeminiService.generateAnswer(prompt, "You are NU Business Builder. Deliver production-ready business concepts.")
            _businessKitResult.value = result
            _isAiLoading.value = false
        }
    }

    // E. AI VIDEO STUDIO
    fun generateVideoScript() {
        val concept = videoIdeaInput.value
        if (concept.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = """
                Generate a cinematic AI video production storyboard script based on concept: '$concept' for platform type '${videoTypeSelect.value}'.
                Please output:
                - Scene-by-Scene Visual breakdown (3 Scenes)
                - Suggested voiceover text / Narratives
                - Custom subtitles time-staged
                - Background soundtrack & visual direction instructions.
            """.trimIndent()

            val result = GeminiService.generateAnswer(prompt, "You are NU AI Video Director. Output professional creative video outlines.")
            _videoStudioResult.value = result
            _isAiLoading.value = false
        }
    }

    // F. AI EDUCATION preparation
    fun generateEduNotes() {
        val topic = eduTopicInput.value
        if (topic.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = """
                Write summarized, high-quality revision study notes for WAEC, NECO, and JAMB prep topic: '$topic'.
                Make it easy to read, with key highlights in bullet points, custom formula definitions if mathematical, and high-scoring advice.
            """.trimIndent()

            val result = GeminiService.generateAnswer(prompt, "You are NU Elite Tutor. Focus on educational excellence.")
            _eduMaterialResult.value = result
            _isAiLoading.value = false
        }
    }

    fun generateEduQuiz() {
        val topic = eduTopicInput.value
        if (topic.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = """
                Generate a 3-question Multiple Choice Test (A, B, C, D) with immediate answer key and explanations for syllabus topic: '$topic'.
                Structure it clearly. Make questions challenging and typical representing WAEC standards.
            """.trimIndent()

            val result = GeminiService.generateAnswer(prompt, "You are NU Examiner. Output clear tests.")
            _eduQuizResult.value = result
            _isAiLoading.value = false
        }
    }

    // G. NU WALLET DIGITAL TRANSACTIONS
    fun executeWalletTransfer() {
        val tar = walletTransferTarget.value
        val amt = walletTransferAmount.value.toDoubleOrNull() ?: 0.0

        if (tar.isBlank() || amt <= 0.0) return

        viewModelScope.launch {
            repository.insertTransaction(
                WalletTransactionEntity(
                    title = "Transfer to $tar",
                    amount = amt,
                    isCredit = false
                )
            )
            isTransferCompleted.value = true
            walletTransferTarget.value = ""
            walletTransferAmount.value = ""

            // System Notification
            repository.insertChat(ChatEntity(
                sender = "ai",
                text = "💸 **NU Wallet System alert:** Instant transfer of ₦$amt to $tar was approved. Transaction ID: TX-${System.currentTimeMillis() % 1000000}."
            ))
        }
    }

    fun addSavingsGoal() {
        val t = savingsGoalTitle.value
        val tar = savingsGoalTarget.value.toDoubleOrNull() ?: 10000.0
        if (t.isBlank()) return

        savingsGoalTitle.value = ""
        savingsGoalTarget.value = ""

        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoalEntity(
                    title = t,
                    target = tar,
                    current = 0.0
                )
            )
        }
    }

    fun fundSavingsGoal(goal: SavingsGoalEntity, fundAmount: Double) {
        val currentBalance = userPreferences.value?.walletBalance ?: 0.0
        if (currentBalance < fundAmount) return

        viewModelScope.launch {
            val updated = goal.copy(current = goal.current + fundAmount)
            repository.updateSavingsGoal(updated)

            repository.insertTransaction(
                WalletTransactionEntity(
                    title = "Allocated to '${goal.title}' Savings",
                    amount = fundAmount,
                    isCredit = false
                )
            )
        }
    }

    // H. HEALTH SYMPTOM CHECKER & REMINDERS
    fun checkSymptoms() {
        val s = symptomInput.value
        if (s.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = """
                Check symptom input: '$s' and provide a safe educational triage guide.
                List potential risk classifications, non-drug lifestyle advices, and clear indications when to visit a state hospital.
                Start with a prominent disclaimer: "⚠️ NOT A MEDICAL REMEDY".
            """.trimIndent()

            val result = GeminiService.generateAnswer(prompt, "You are NU Medical Advisor. Prioritize patient safety, clear warnings, and active hydration.")
            _symptomCheckerResult.value = result
            _isAiLoading.value = false
        }
    }

    fun submitReminder() {
        val t = reminderTitleInput.value
        val tm = reminderTimeInput.value
        if (t.isBlank()) return

        reminderTitleInput.value = ""
        viewModelScope.launch {
            repository.insertReminder(
                ReminderEntity(title = t, time = tm)
            )
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            repository.deleteReminderById(id)
        }
    }

    // I. TRAVEL GUIDE
    fun planTravelGuide() {
        val dest = travelDestination.value
        val days = travelDays.value
        if (dest.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = "Create a customized $days-day travel itinerary and budget guide for: '$dest'. Include local transport guidance, average hotel ranges in Abuja/West Africa, and must-see historical attractions."
            val result = GeminiService.generateAnswer(prompt, "You are NU Travel Copilot. Provide concise, exciting travel summaries.")
            _travelGuideResult.value = result
            _isAiLoading.value = false
        }
    }

    // J. JOBS MATCHING
    fun applyForJob(job: JobEntity) {
        viewModelScope.launch {
            repository.updateJob(job.copy(isApplied = true))

            // Transaction log
            repository.insertChat(ChatEntity(
                sender = "ai",
                text = "💼 **Job Application Submitted!** NU sent your digital profile to \"${job.company}\" for the \"${job.title}\" position. Good luck!"
            ))
        }
    }

    // K. NU LIFE COPILOT (DIGITAL TWIN success coaching)
    fun askCopilotCoach() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val currentG = copilotGoals.value
            val completed = currentG.filter { it.isCompleted }.map { it.title }.joinToString()
            val pending = currentG.filter { !it.isCompleted }.map { it.title }.joinToString()
            val name = userPreferences.value?.userName ?: "Amina"

            val prompt = """
                You are $name's NU Life Digital Twin success coach.
                Here is progress data:
                Completed success habits: $completed
                Aspirations pending focus: $pending
                
                Please evaluate daily productivity, analyze what habits/skills complement their work network, list 2 success advice steps, and formulate an inspiring daily planning challenge for tomorrow.
            """.trimIndent()

            val result = GeminiService.generateAnswer(prompt, "You are our ultra-futuristic Life Success Twin. Keep it highly motivational, positive, and direct.")
            _copilotCoachAdvice.value = result
            _isAiLoading.value = false
        }
    }

    fun addCopilotGoal() {
        val t = copilotNewGoalTitle.value
        val cat = copilotNewGoalCategory.value
        if (t.isBlank()) return

        copilotNewGoalTitle.value = ""
        viewModelScope.launch {
            repository.insertCopilotGoal(
                CopilotGoalEntity(title = t, category = cat)
            )
        }
    }

    fun updateUserPreferences(pref: UserPreferencesEntity) {
        viewModelScope.launch {
            repository.updateUserPreferences(pref)
        }
    }

    fun toggleCopilotGoal(goal: CopilotGoalEntity) {
        viewModelScope.launch {
            repository.updateCopilotGoal(
                goal.copy(isCompleted = !goal.isCompleted)
            )
            // Increment streak if completed
            val currentPrefs = repository.getUserPreferences() ?: UserPreferencesEntity()
            if (!goal.isCompleted) {
                repository.updateUserPreferences(currentPrefs.copy(twinStreak = currentPrefs.twinStreak + 1))
            }
        }
    }
}

class NuViewModelFactory(private val repository: NuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
