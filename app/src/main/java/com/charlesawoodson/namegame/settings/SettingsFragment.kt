package com.charlesawoodson.namegame.settings

import android.os.Bundle
import android.view.*
import androidx.preference.PreferenceFragmentCompat
import com.charlesawoodson.namegame.MainActivity
import com.charlesawoodson.namegame.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.settings)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        menu.findItem(R.id.action_statistics).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}