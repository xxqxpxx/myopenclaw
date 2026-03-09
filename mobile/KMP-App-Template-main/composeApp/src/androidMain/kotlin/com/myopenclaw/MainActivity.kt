package com.myopenclaw

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.myopenclaw.data.auth.GoogleSignInProvider
import com.myopenclaw.data.auth.clearGoogleSignInActivity
import com.myopenclaw.data.auth.handleGoogleSignInResult
import com.myopenclaw.data.auth.setGoogleSignInActivity
import com.myopenclaw.data.repository.clearCurrentActivity
import com.myopenclaw.data.repository.setCurrentActivity
import com.myopenclaw.util.ImagePicker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ImagePicker with activity result launchers
        // This must be called before setContent()
        ImagePicker.initialize(this, applicationContext)

        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        // Set current activity for RevenueCat purchases
        setCurrentActivity(this)
        // Set current activity for Google Sign-In
        setGoogleSignInActivity(this)
    }

    override fun onPause() {
        super.onPause()
        // Clear activity references when paused
        clearCurrentActivity()
        clearGoogleSignInActivity()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle legacy Google Sign-In result
        if (requestCode == GoogleSignInProvider.GOOGLE_SIGN_IN_REQUEST_CODE) {
            handleGoogleSignInResult(data)
        }
    }
}
