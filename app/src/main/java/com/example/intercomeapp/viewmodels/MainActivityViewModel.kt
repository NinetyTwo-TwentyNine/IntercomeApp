package com.example.intercomeapp.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.intercomeapp.data.AuthenticationStatus
import com.example.intercomeapp.models.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val rImplementation: FirebaseRepository,
    private val sPreferences: SharedPreferences
) : ViewModel() {
    var authState: MutableLiveData<AuthenticationStatus> = MutableLiveData()

    init {
        Log.d("TAG", "Created a view model for the outer app segment successfully.")
    }

    fun resetAuthState() {
        authState = MutableLiveData()
    }

    fun isUserSingedIn(): Boolean {
        if (rImplementation.getCurrentUser() != null) {
            return true
        }
        return false
    }

    fun getUserEmail(): String? {
        if (!isUserSingedIn()) {
            return null
        }
        return rImplementation.getCurrentUser()!!.email
    }

    fun signIn(email: String, password: String, newAccount: Boolean) {
        authState.value = AuthenticationStatus.Progress
        rImplementation.signIn(email, password, newAccount).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                authState.value = AuthenticationStatus.Success
            } else {
                authState.value = AuthenticationStatus.Error(task.exception!!.message.toString())
            }
        }
    }

    fun signOut() {
        rImplementation.signOut()
    }

    private fun <T> editPreferences(preference: String, value: T) {
        val sEdit: SharedPreferences.Editor
        if (preference.contains("_BOOL")) {
            sEdit = sPreferences.edit().putBoolean(preference, (value as Boolean))
        } else {
            sEdit = sPreferences.edit().putString(preference, (value as String))
        }
        sEdit.apply()
    }

    private fun <T> checkPreferences(preference: String, defValue: T): T {
        if (preference.contains("_BOOL")) {
            return (sPreferences.getBoolean(preference, (defValue as Boolean)) as T)
        } else {
            return (sPreferences.getString(preference, (defValue as String)) as T)
        }
    }

    private fun getPreferenceProperFormat(preference: String): String {
        if (preference.contains("_MAIL")) {
            return preference.replace("_MAIL", "_${getUserEmail()}")
        }
        return preference
    }

    fun <T> updatePreference(preference: String, def: T) {
        val actualPreference = getPreferenceProperFormat(preference)
        editPreferences(actualPreference, checkPreferences(actualPreference, def))
    }

    fun <T> setPreference(preference: String, newVal: T) {
        val actualPreference = getPreferenceProperFormat(preference)
        editPreferences(actualPreference, newVal)
    }

    fun <T> getPreference(preference: String, def: T): T {
        val actualPreference = getPreferenceProperFormat(preference)
        return checkPreferences(actualPreference, def)
    }
}