package com.college.certgen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.college.certgen.data.db.AppDatabaseHelper
import com.college.certgen.data.models.UserAccount
import com.college.certgen.ui.MainContainerScreen
import com.college.certgen.ui.auth.LoginScreen
import com.college.certgen.ui.theme.CollegeCertificateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbHelper = AppDatabaseHelper.getInstance(this)

        setContent {
            CollegeCertificateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentUser by remember { mutableStateOf<UserAccount?>(null) }

                    if (currentUser == null) {
                        LoginScreen(
                            dbHelper = dbHelper,
                            onLoginSuccess = { user ->
                                currentUser = user
                            }
                        )
                    } else {
                        MainContainerScreen(
                            currentUser = currentUser!!,
                            dbHelper = dbHelper,
                            onLogout = {
                                currentUser = null
                            }
                        )
                    }
                }
            }
        }
    }
}
