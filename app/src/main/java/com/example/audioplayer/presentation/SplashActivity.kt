package com.example.audioplayer.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audioplayer.R
import com.example.audioplayer.ui.theme.SplashScreen

import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ads
        MobileAds.initialize(this) {
            println("🔵 MobileAds initialized successfully")
        }

        setContent {
            SplashScreenWithAd()
        }
    }

    fun navigateToSongListSafely() {
        if (!hasNavigated) {
            hasNavigated = true
            println("🚀 Starting navigation to SongListActivity...")
            startActivity(Intent(this, SongListActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
            println("🚀 Navigation completed!")
        }
    }
}

@Composable
fun SplashScreenWithAd() {
    val context = LocalContext.current
    val activity = context as SplashActivity
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var adLoadingState by remember { mutableStateOf("Not Started") }
    var uiState by remember { mutableStateOf(UiState.SPLASH) }

    // Load ad using LaunchedEffect
    LaunchedEffect(Unit) {
        println("🟡 LaunchedEffect started - Beginning ad load process...")
        adLoadingState = "Loading"

        val adRequest = AdRequest.Builder().build()
        println("🟡 Ad request created successfully")

        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    println("✅ Ad loaded successfully!")
                    interstitialAd = ad
                    adLoadingState = "Loaded"
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    println("❌ Ad failed to load!")
                    println("❌ Error: ${adError.message}")
                    interstitialAd = null
                    adLoadingState = "Failed"
                }
            })
    }

    // Handle UI based on state
    when (uiState) {
        UiState.SPLASH -> {
            SplashScreen(
                onStartClick = {
                    println("🔘 Get Started button pressed!")

                    if (interstitialAd != null) {
                        println("🟢 Ad available - Showing interstitial...")
                        uiState = UiState.SHOWING_AD

                        interstitialAd?.fullScreenContentCallback =
                            object : com.google.android.gms.ads.FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    println("✅ Ad dismissed - Starting navigation...")
                                    uiState = UiState.LOADING_NEXT_SCREEN

                                    // Small delay to show loading then navigate
                                    kotlinx.coroutines.GlobalScope.launch {
                                        kotlinx.coroutines.delay(300) // Brief moment to show loader
                                        activity.runOnUiThread {
                                            activity.navigateToSongListSafely()
                                        }
                                    }
                                    interstitialAd = null
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                    println("❌ Ad failed to show - Starting navigation...")
                                    uiState = UiState.LOADING_NEXT_SCREEN

                                    kotlinx.coroutines.GlobalScope.launch {
                                        kotlinx.coroutines.delay(300)
                                        activity.runOnUiThread {
                                            activity.navigateToSongListSafely()
                                        }
                                    }
                                    interstitialAd = null
                                }

                                override fun onAdShowedFullScreenContent() {
                                    println("🟢 Ad is showing")
                                }
                            }

                        interstitialAd?.show(activity)
                    } else {
                        println("⚠️ No ad available - Direct navigation")
                        uiState = UiState.LOADING_NEXT_SCREEN

                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(300)
                            activity.runOnUiThread {
                                activity.navigateToSongListSafely()
                            }
                        }
                    }
                }
            )
        }

        UiState.SHOWING_AD -> {
            // Show minimal loading while ad is preparing/showing
            LoadingScreen("Preparing...")
        }

        UiState.LOADING_NEXT_SCREEN -> {
            // Show loading during navigation
            LoadingScreen("Loading Music...")
        }
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated loading indicator
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = "rotation"
            )

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = colorResource(R.color.Orange),
                strokeWidth = 4.dp
            )

            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

enum class UiState {
    SPLASH,
    SHOWING_AD,
    LOADING_NEXT_SCREEN
}