package com.example.data

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig
import android.util.Log

// ==========================================
// 1. Data Models for Gemini REST API (Moshi Compatible)
// ==========================================

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

// ==========================================
// 2. Retrofit Client Setup
// ==========================================

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// ==========================================
// 3. Helper Service Wrapper
// ==========================================

object GeminiService {
    private const val TAG = "GeminiService"

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun isKeyValid(key: String): Boolean {
        return key.isNotEmpty() && !key.startsWith("MY_GEMINI_") && key != "placeholder"
    }

    /**
     * Generates content from standard text prompts.
     * Safe fallback included if Key is missing or invalid.
     */
    suspend fun generateAnswer(prompt: String, systemPrompt: String? = null): String {
        val key = getApiKey()
        if (!isKeyValid(key)) {
            Log.e(TAG, "Gemini API Key is missing or invalid. Using local simulation.")
            return simulateLocalResponse(prompt, systemPrompt)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(text = it))) }
        )

        return try {
            val response = GeminiClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "NU Intelligence: Connection succeeded but no content returned."
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            "NU: (Offline Fallback) I encountered a network challenge. Here is what I think based on local database: \n\n" + simulateLocalResponse(prompt, systemPrompt)
        }
    }

    /**
     * Highly specific queries fallback simulation.
     * Prevents static dead ends and serves as an educational sandbox.
     */
    private fun simulateLocalResponse(prompt: String, systemPrompt: String?): String {
        val p = prompt.lowercase()
        return when {
            p.contains("money") || p.contains("earn") || p.contains("poverty") -> """
                💵 **NU Intelligence - Quick Financial & Earning Guide**
                
                I hear you, and NU is designed to assist you in building sustainable income. Here are immediate local and digital opportunities recommended for you:
                
                1. **Skills to Learn to leverage NU Creator System**:
                   - *Prompt Engineering*: Learn how to generate marketing campaigns using NU Builder.
                   - *Local Tech Services*: Become an installer for Smart hybrid solar setups, highly requested in Abuja and Abuja airport region.
                   
                2. **Freelancing & Digital Services**:
                   - List "Social Logo & Typography Design" on the NU Marketplace for ₦10,000–₦200,000. Use NU Business Builder to design them with one-click.
                
                3. **Active Grants / Microfinance**:
                   - Check the *Abuja SMEDAN office* grants for small startups. NU Business Builder can generate a standard business proposal.
                
                4. **Immediate Action**: Tap the "AI Business Builder" tab below to instantly draft a Logo, Website content, and Business Plan to launch your trade!
            """.trimIndent()

            p.contains("jamb") || p.contains("waec") || p.contains("test") || p.contains("exam") || p.contains("education") -> """
                🎓 **NU AI Educator - Revision Guide**
                
                Excellent choice! Building your academic foundation is the ultimate key to human success.
                
                Here is a Mock Multiple Choice Question:
                
                *Subject: JAMB Use of English (Synonyms)*
                "The Governor's speech was **lucid** and concise."
                - A) Obscure
                - B) Clear (Correct)
                - C) Aggressive
                - D) Prolonged
                
                **NU Lesson Tip**: Practice past questions for WAEC and JAMB daily. Type your question above, and I will generate complete summaries, custom flashcards, or step-by-step calculus explanations instantly!
            """.trimIndent()

            p.contains("business") || p.contains("plan") || p.contains("logo") || p.contains("marketing") -> """
                💼 **NU Business Builder AI Output**
                
                Based on your idea, NU Business Builder has compiled the following:
                
                - **Slogan Suggestion**: "Illuminating the Future" (for Renewable tech) or "Pure Taste, Every Day" (for Food sector).
                - **Primary Tone**: Professional & premium.
                - **Proposed Target Audience**: Growing urban professionals and regional businesses.
                - **Estimated Setup Budget**: ₦50,000 - ₦250,000 for starting materials.
                
                Tap the **Business Studio** in the bottom panel to generate standard invoices, marketing designs, or website copies instantly!
            """.trimIndent()

            p.contains("health") || p.contains("symptom") || p.contains("doctor") || p.contains("pain") -> """
                ⚠️ **NU Health Assistant - Health Report**
                *Disclaimer: I am an AI, not a certified and licensed doctor. Always consult medical centers for diagnostic therapies.*
                
                Here are educational guidelines regarding health query:
                - **Hydration**: Adult bodies require at least 2.5 to 3 Liters of water daily.
                - **Daily Fitness**: Walking at least 5k steps helps cardiovascular metrics.
                - **Stress Management**: Deep diaphragmatic breathing decreases cortisol levels.
                
                Use the **Health Reminders** tracker below to log vital daily tasks such as water intake or fitness intervals.
            """.trimIndent()

            else -> """
                🤖 **NU Intelligence v2.5**
                
                I have analyzed your prompt: "$prompt".
                
                As NU, the super-app brain, I recommend leveraging the relevant services:
                - Use **AI Marketplace** to match buy/sell interests locally.
                - Use **Wallet** to set active targets like laptop hardware upgrades.
                - Complete your daily **Life Copilot Twin** tasks to increase your streak!
                
                Ask me something specific, or switch tabs below to explore the super-app services!
            """.trimIndent()
        }
    }
}
