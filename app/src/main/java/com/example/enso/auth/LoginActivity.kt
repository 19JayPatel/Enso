package com.example.enso.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.enso.customer.activities.MainActivity
import com.example.enso.R
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //CONTINUE BUTTON → MAIN SCREEN
        val continueBtn = findViewById<MaterialButton>(R.id.continueBtn)

        continueBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //SIGN UP REDIRECT
        val loginRedirect = findViewById<TextView>(R.id.loginRedirect)

        val fullText = "Don’t have an account? Sign up"
        val spannable = SpannableString(fullText)

        val startIndex = fullText.indexOf("Sign up")
        val endIndex = fullText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)
            }
        }

        spannable.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan("#A46C54".toColorInt()),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        loginRedirect.text = spannable
        loginRedirect.movementMethod = LinkMovementMethod.getInstance()
        loginRedirect.highlightColor = Color.TRANSPARENT
    }
}