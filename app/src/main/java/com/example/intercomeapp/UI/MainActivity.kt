package com.example.intercomeapp.UI

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.intercomeapp.R
import com.example.intercomeapp.data.Constants.APP_PREFERENCES_INTERCOM_RESPONSE
import com.example.intercomeapp.data.Constants.APP_PREFERENCES_INTERCOM_SOUND
import com.example.intercomeapp.data.Constants.APP_PREFERENCES_STAY
import com.example.intercomeapp.data.Constants.INTERCOM_RESPONSE_TYPE_NONE
import com.example.intercomeapp.databinding.ActivityMainBinding
import com.example.intercomeapp.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#05080D")))

        viewModel.updatePreference(APP_PREFERENCES_STAY, true)

        viewModel.updatePreference(APP_PREFERENCES_INTERCOM_RESPONSE, INTERCOM_RESPONSE_TYPE_NONE)
        viewModel.updatePreference(APP_PREFERENCES_INTERCOM_SOUND, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.signOut -> {
                if (viewModel.isUserSingedIn()) {
                    viewModel.signOut()
                    //viewModel.editPreferences(APP_PREFERENCES_STAY, false)
                    binding.container.findNavController().navigate(R.id.SignInFragment)
                } else {
                    Log.d("TAG", "Refusing to sign out.")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}