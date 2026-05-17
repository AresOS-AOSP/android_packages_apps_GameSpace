/*
 * Copyright (C) 2026 AresOS
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences.quickstart

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.View

import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference

import com.android.settingslib.widget.SettingsBasePreferenceFragment

import dagger.hilt.android.AndroidEntryPoint

import io.chaldeaprjkt.gamespace.data.AppSettings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject

@AndroidEntryPoint(SettingsBasePreferenceFragment::class)
class QuickStartAppsFragment : Hilt_QuickStartAppsFragment() {

    @Inject
    lateinit var appSettings: AppSettings

    private val selected = mutableSetOf<String>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selected.clear()
        appSettings.quickStartApps
            .split(",")
            .filter { it.isNotBlank() }
            .forEach { selected.add(it) }
        loadApps()
    }

    private fun loadApps() {
        val ctx = requireContext()
        val pm = ctx.packageManager
        val ownPkg = ctx.packageName

        viewLifecycleOwner.lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
                    .asSequence()
                    .mapNotNull { ri: ResolveInfo ->
                        val info = ri.activityInfo ?: return@mapNotNull null
                        if (info.packageName == ownPkg) return@mapNotNull null
                        try {
                            val ai = pm.getApplicationInfo(
                                info.packageName,
                                PackageManager.ApplicationInfoFlags.of(0)
                            )
                            AppEntry(
                                packageName = info.packageName,
                                label = pm.getApplicationLabel(ai).toString(),
                                icon = ai.loadIcon(pm)
                            )
                        } catch (_: PackageManager.NameNotFoundException) {
                            null
                        }
                    }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase() }
                    .toList()
            }

            val screen = preferenceScreen ?: return@launch
            screen.removeAll()
            apps.forEach { app ->
                val pref = CheckBoxPreference(requireContext()).apply {
                    key = app.packageName
                    title = app.label
                    summary = app.packageName
                    icon = app.icon
                    isPersistent = false
                    isChecked = selected.contains(app.packageName)
                    setOnPreferenceChangeListener { _, newValue ->
                        if (newValue as Boolean) {
                            selected.add(app.packageName)
                        } else {
                            selected.remove(app.packageName)
                        }
                        persistSelection()
                        true
                    }
                }
                screen.addPreference(pref)
            }
        }
    }

    private fun persistSelection() {
        appSettings.quickStartApps = selected.joinToString(",")
    }

    private data class AppEntry(
        val packageName: String,
        val label: String,
        val icon: android.graphics.drawable.Drawable?
    )
}
