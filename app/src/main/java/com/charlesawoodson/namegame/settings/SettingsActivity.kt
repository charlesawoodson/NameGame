package com.charlesawoodson.namegame.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.charlesawoodson.namegame.MainActivity
import com.charlesawoodson.namegame.R
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        // overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        beginGame.setOnClickListener {
            Intent(applicationContext, MainActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_slide_up, R.anim.exit_slide_down)
    }
}