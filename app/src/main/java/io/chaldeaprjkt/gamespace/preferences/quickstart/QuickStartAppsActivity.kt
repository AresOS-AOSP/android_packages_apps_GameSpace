/*
 * Copyright (C) 2026 AresOS
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences.quickstart

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.chaldeaprjkt.gamespace.R

@AndroidEntryPoint(CollapsingToolbarBaseActivity::class)
class QuickStartAppsActivity : Hilt_QuickStartAppsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.quick_start_apps_title)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    QuickStartAppsFragment()
                )
                .commit()
        }
    }
}
