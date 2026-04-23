package com.example.enso.auth

import com.google.android.material.transition.platform.MaterialFadeThrough
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.enso.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.exitTransition = MaterialFadeThrough()
        window.enterTransition = MaterialFadeThrough()

        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logoImage)

// Fade Animation
        val fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)

// Scale Animations
        val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.8f, 1f)

// Combine Animations
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, scaleX, scaleY)
        animatorSet.duration = 1200
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        animatorSet.start()
        // Move to MainActivity
        lifecycleScope.launch {
            delay(3000)

            val intent = Intent(this@SplashActivity, LoginActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this@SplashActivity,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

            startActivity(intent, options.toBundle())
            finish()
        }
    }
}