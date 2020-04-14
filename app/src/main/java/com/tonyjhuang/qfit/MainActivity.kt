package com.tonyjhuang.qfit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tonyjhuang.qfit.data.QfDb
import com.tonyjhuang.qfit.data.UserRepository

class MainActivity : AppCompatActivity() {

    private lateinit var database: QfDb
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        database = QfDb(Firebase.database.reference)
        userRepository = UserRepository(database)
        authenticateUser()
    }

    private fun authenticateUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            launchAuthenticationFlow()
            return
        }
        userRepository.exists(user.uid) { exists ->
            if (exists) {
                launchViews()
                return@exists
            }
            userRepository.create(user.uid, user.displayName!!, user.photoUrl!!.toString()) {
                launchViews()
            }
        }
    }

    private fun launchAuthenticationFlow() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            authenticateUser()
        }
    }

    private fun launchViews() {
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_group_list
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    companion object {
        const val RC_SIGN_IN = 0
    }
}
