package ru.netology.nework.util

import android.content.Context
import android.content.Intent
import ru.netology.nework.dto.User
import ru.netology.nework.ui.auth.SignInFragment

class SharedPrefManager private constructor(context: Context) {
        //this method will checker whether user is already logged in or not
        val isLoggedIn: Boolean
            get() {
                val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                return sharedPreferences?.getString(KEY_USERNAME, null) != null
            }
        //this method will give the logged in user
        val user: User
            get() {
                val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                return User(
                    sharedPreferences!!.getLong(KEY_ID, 0L),
                    sharedPreferences.getString(KEY_LOGIN, null),
                    sharedPreferences.getString(KEY_USERNAME, null),
                    sharedPreferences.getString(KEY_AVATAR, null)
                )
            }
        init {
            ctx = context
        }
        //this method will store the user data in shared preferences
        fun userLogin(user: User) {
            val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.putLong(KEY_ID, user.id)
            editor?.putString(KEY_USERNAME, user.name)
            editor?.putString(KEY_LOGIN, user.login)
            editor?.putString(KEY_AVATAR, user.avatar)
            editor?.apply()
        }
        //this method will logout the user
        fun logout() {
            val sharedPreferences = ctx?.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.clear()
            editor?.apply()
            ctx?.startActivity(Intent(ctx, SignInFragment::class.java))
        }
        companion object {
            private val SHARED_PREF_NAME = "volleyregisterlogin"
            private val KEY_USERNAME = "keyusername"
            private val KEY_LOGIN = "keylogin"
            private val KEY_AVATAR = "keygenderavatar"
            private val KEY_ID = "keyid"
            private var mInstance: SharedPrefManager? = null
            private var ctx: Context? = null
            @Synchronized
            fun getInstance(context: Context): SharedPrefManager {
                if(mInstance == null) {
                    mInstance = SharedPrefManager(context)
                }
                return mInstance as SharedPrefManager
            }
        }
    }
