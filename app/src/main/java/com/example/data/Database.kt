package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Amina Bello",
    val preferredLanguage: String = "English", // English, Hausa, Arabic, French
    val walletBalance: Double = 250000.0,
    val twinStreak: Int = 7
)

@Entity(tableName = "chat_history")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "social_posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorTitle: String,
    val content: String,
    val likes: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val translatedContent: String? = null,
    val isTranslated: Boolean = false
)

@Entity(tableName = "marketplace_items")
data class MarketplaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val price: Double,
    val type: String, // "SELL", "BUY", "SERVICE"
    val description: String,
    val contactPhone: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val company: String,
    val location: String,
    val type: String, // "LOCAL", "REMOTE", "FREELANCE"
    val salary: String,
    val skillsRequired: String,
    val isApplied: Boolean = false
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isCredit: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val target: Double,
    val current: Double
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val time: String,
    val isActive: Boolean = true
)

@Entity(tableName = "copilot_goals")
data class CopilotGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "SKILL", "BUSINESS", "FINANCE", "HABIT"
    val isCompleted: Boolean = false
)

// ==========================================
// 2. DAOs
// ==========================================

@Dao
interface NuDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferences(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(pref: UserPreferencesEntity)

    // Chat History
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getChatHistoryFlow(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("DELETE FROM chat_history")
    suspend fun clearChatHistory()

    // Social Posts
    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    fun getSocialPostsFlow(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    // Marketplace
    @Query("SELECT * FROM marketplace_items ORDER BY timestamp DESC")
    fun getMarketplaceItemsFlow(): Flow<List<MarketplaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketplaceItem(item: MarketplaceEntity)

    // Jobs
    @Query("SELECT * FROM jobs ORDER BY id ASC")
    fun getJobsFlow(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)

    // Wallet Transactions
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getTransactionsFlow(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransactionEntity)

    // Savings Goals
    @Query("SELECT * FROM savings_goals")
    fun getSavingsGoalsFlow(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

    // Reminders (Health/Habits)
    @Query("SELECT * FROM reminders")
    fun getRemindersFlow(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Int)

    // Copilot Goals (Digital Twin)
    @Query("SELECT * FROM copilot_goals")
    fun getCopilotGoalsFlow(): Flow<List<CopilotGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCopilotGoal(goal: CopilotGoalEntity)

    @Update
    suspend fun updateCopilotGoal(goal: CopilotGoalEntity)
}

// ==========================================
// 3. Database
// ==========================================

@Database(
    entities = [
        UserPreferencesEntity::class,
        ChatEntity::class,
        PostEntity::class,
        MarketplaceEntity::class,
        JobEntity::class,
        WalletTransactionEntity::class,
        SavingsGoalEntity::class,
        ReminderEntity::class,
        CopilotGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NuDatabase : RoomDatabase() {
    abstract fun nuDao(): NuDao

    companion object {
        @Volatile
        private var INSTANCE: NuDatabase? = null

        fun getDatabase(context: Context): NuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NuDatabase::class.java,
                    "nu_super_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ==========================================
// 4. Repository
// ==========================================

class NuRepository(private val nuDao: NuDao) {

    val userPreferences: Flow<UserPreferencesEntity?> = nuDao.getUserPreferencesFlow()
    val chatHistory: Flow<List<ChatEntity>> = nuDao.getChatHistoryFlow()
    val socialPosts: Flow<List<PostEntity>> = nuDao.getSocialPostsFlow()
    val marketplaceItems: Flow<List<MarketplaceEntity>> = nuDao.getMarketplaceItemsFlow()
    val jobs: Flow<List<JobEntity>> = nuDao.getJobsFlow()
    val transactions: Flow<List<WalletTransactionEntity>> = nuDao.getTransactionsFlow()
    val savingsGoals: Flow<List<SavingsGoalEntity>> = nuDao.getSavingsGoalsFlow()
    val reminders: Flow<List<ReminderEntity>> = nuDao.getRemindersFlow()
    val copilotGoals: Flow<List<CopilotGoalEntity>> = nuDao.getCopilotGoalsFlow()

    suspend fun ensureInitialData() {
        if (nuDao.getUserPreferences() == null) {
            nuDao.insertUserPreferences(UserPreferencesEntity())
            
            // Insert Initial Chats
            nuDao.insertChat(ChatEntity(sender = "ai", text = "Welcome Amina! I am NU Intelligence, your Digital twin and life copilot. What can I help you accomplish today? You can type 'I need money' or ask me any question!"))

            // Insert Initial Posts
            nuDao.insertPost(PostEntity(
                authorName = "Farouk Yusuf",
                authorTitle = "Agribusiness Lead",
                content = "AI crop monitoring is transforming yield rates in Kano! Just harvested 40% higher yields using NU Soil recommendations.",
                likes = 24
            ))
            nuDao.insertPost(PostEntity(
                authorName = "Miriam Kone",
                authorTitle = "EdTech Creator",
                content = "Preparing my students for JAMB and WAEC exams with our integrated study guides. The AI simulator has worked wonders!",
                likes = 45
            ))

            // Insert Initial Marketplace Items
            nuDao.insertMarketplaceItem(MarketplaceEntity(
                title = "Smart Solar Hybrid Inverter 5KV",
                price = 320000.0,
                type = "SELL",
                description = "High efficiency pure sine wave inverter, perfect for office and remote work setup in Abuja. Includes battery pack.",
                contactPhone = "+2348012345678"
            ))
            nuDao.insertMarketplaceItem(MarketplaceEntity(
                title = "Professional Logo & Premium Brand Guide",
                price = 15000.0,
                type = "SERVICE",
                description = "Custom elegant vector logos delivered in 24 hours. Comes with copyright license and source files.",
                contactPhone = "+2348123456789"
            ))

            // Insert Initial Jobs
            nuDao.insertJob(JobEntity(
                title = "Community Health Facilitator",
                company = "CareFirst Org",
                location = "Kaduna (On-site)",
                type = "LOCAL",
                salary = "₦180,000 / month",
                skillsRequired = "First Aid, Hausa Fluency, Public Healthcare"
            ))
            nuDao.insertJob(JobEntity(
                title = "E-Commerce Virtual Assistant",
                company = "Global Trade Ltd",
                location = "Remote",
                type = "REMOTE",
                salary = "₦120,000 / month",
                skillsRequired = "Excel, English, Customer Support, Marketing"
            ))
            nuDao.insertJob(JobEntity(
                title = "Junior Software Developer Intern",
                company = "NuTech Ventures",
                location = "Abuja / Hybrid",
                type = "FREELANCE",
                salary = "₦250,000 / project",
                skillsRequired = "Kotlin, Android Compose, Git"
            ))

            // Insert Initial Transactions
            nuDao.insertTransaction(WalletTransactionEntity(title = "Peer Transfer from Chidi", amount = 15000.0, isCredit = true))
            nuDao.insertTransaction(WalletTransactionEntity(title = "WAEC Syllabus Purchase", amount = 3500.0, isCredit = false))
            nuDao.insertTransaction(WalletTransactionEntity(title = "AI Business Kit Generation Fee", amount = 1000.0, isCredit = false))

            // Insert Initial Savings Goals
            nuDao.insertSavingsGoal(SavingsGoalEntity(title = "Laptop Upgrade (M3 Air)", target = 650000.0, current = 420000.0))
            nuDao.insertSavingsGoal(SavingsGoalEntity(title = "JAMB/WAEC Online Exam Fees", target = 45000.0, current = 45000.0))

            // Insert Initial Reminders
            nuDao.insertReminder(ReminderEntity(title = "Check blood pressure & hydrate", time = "09:00 AM"))
            nuDao.insertReminder(ReminderEntity(title = "Practice 15 mins of French Vocabulary", time = "06:00 PM"))

            // Insert Initial Copilot Goals (Digital Twin goals)
            nuDao.insertCopilotGoal(CopilotGoalEntity(title = "Master Advanced Hausa & French Business Terms", category = "SKILL"))
            nuDao.insertCopilotGoal(CopilotGoalEntity(title = "Launch 'Solar Abundance' Marketplace Store", category = "BUSINESS"))
            nuDao.insertCopilotGoal(CopilotGoalEntity(title = "Reach ₦500,000 in personal wallet reserves", category = "FINANCE"))
            nuDao.insertCopilotGoal(CopilotGoalEntity(title = "Complete 3 WAEC/JAMB exam training tests", category = "HABIT"))
        }
    }

    suspend fun insertChat(chat: ChatEntity) = nuDao.insertChat(chat)
    suspend fun clearChat() = nuDao.clearChatHistory()

    suspend fun insertPost(post: PostEntity) = nuDao.insertPost(post)
    suspend fun updatePost(post: PostEntity) = nuDao.updatePost(post)

    suspend fun insertMarketplaceItem(item: MarketplaceEntity) = nuDao.insertMarketplaceItem(item)

    suspend fun insertJob(job: JobEntity) = nuDao.insertJob(job)
    suspend fun updateJob(job: JobEntity) = nuDao.updateJob(job)

    suspend fun insertTransaction(tx: WalletTransactionEntity) {
        nuDao.insertTransaction(tx)
        val currentPrefs = nuDao.getUserPreferences() ?: UserPreferencesEntity()
        val newBalance = if (tx.isCredit) currentPrefs.walletBalance + tx.amount else currentPrefs.walletBalance - tx.amount
        nuDao.insertUserPreferences(currentPrefs.copy(walletBalance = newBalance))
    }

    suspend fun insertSavingsGoal(goal: SavingsGoalEntity) = nuDao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = nuDao.updateSavingsGoal(goal)

    suspend fun insertReminder(reminder: ReminderEntity) = nuDao.insertReminder(reminder)
    suspend fun deleteReminderById(id: Int) = nuDao.deleteReminder(id)

    suspend fun insertCopilotGoal(goal: CopilotGoalEntity) = nuDao.insertCopilotGoal(goal)
    suspend fun updateCopilotGoal(goal: CopilotGoalEntity) = nuDao.updateCopilotGoal(goal)

    suspend fun updateUserPreferences(pref: UserPreferencesEntity) = nuDao.insertUserPreferences(pref)
    suspend fun getUserPreferences(): UserPreferencesEntity? = nuDao.getUserPreferences()
}
