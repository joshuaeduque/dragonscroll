package com.teamoranges.dragonscroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // Get Context
        Context context = requireContext();

        // Get SharedPreferences
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );

        // Try getting clear preferences button
        Preference clearPreferencesButton = findPreference(getString(R.string.clear_prefs_key));
        if (clearPreferencesButton != null) {
            // Set clear prefs button onclick
            clearPreferencesButton.setOnPreferenceClickListener((preference) -> {
                // Clear SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                return true;
            });
        }

        // Setup nuke database button
        Preference nukeDatabaseButton = findPreference("nuke_db_preference");
        if (nukeDatabaseButton != null) {
            nukeDatabaseButton.setOnPreferenceClickListener(preference -> {
                BookDao bookDao = ((MainActivity) requireActivity()).getBookDao();
                bookDao.nukeTable();
                return true;
            });
        }

        // Setup themes list
        ListPreference themesList = findPreference(getString(R.string.themes_preference_key));
        if(themesList != null) {
            themesList.setOnPreferenceChangeListener(this::onThemesPreferenceChanged);
        }

        // Setup text size seekbar
        SeekBarPreference textSizeSlider = findPreference(getString(R.string.text_size_preference_key));
        if (textSizeSlider != null) {
            textSizeSlider.setOnPreferenceChangeListener((preference, newValue) -> {
                int sliderValue = (Integer) newValue;
                float textSizeMultiplier = sliderValue / 100f;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat(getString(R.string.text_size_preference_key), textSizeMultiplier);
                editor.apply();

                // Restart the activity to apply font size
                requireActivity().recreate();
                return true;
            });
        }
    }

    private boolean onThemesPreferenceChanged(Preference preference, Object o) {
        // Why do I have to commit the preference manually?
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.themes_preference_key), o.toString());
        editor.commit();

        requireActivity().recreate();
        return true;
    }
}