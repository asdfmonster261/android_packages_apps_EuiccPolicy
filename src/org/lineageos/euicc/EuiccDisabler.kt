/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.euicc

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags

object EuiccDisabler {
    private val EUICC_DEPENDENCIES = listOf("com.google.android.gms")

    private val EUICC_PACKAGES = listOf(
        "com.google.android.euicc",
        "com.google.euiccpixel",
    )

    private fun isInstalled(pm: PackageManager, pkgName: String) =
        runCatching {
            val info = pm.getApplicationInfo(pkgName, ApplicationInfoFlags.of(0))
            info.flags and ApplicationInfo.FLAG_INSTALLED != 0
        }
        .getOrDefault(false)

    private fun isInstalledAndEnabled(pm: PackageManager, pkgName: String) =
        runCatching {
            val info = pm.getApplicationInfo(pkgName, ApplicationInfoFlags.of(0))
            info.enabled
        }
        .getOrDefault(false)

    fun enableOrDisableEuicc(context: Context) {
        val pm = context.packageManager
        val supportsEuicc = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_EUICC)
        val disable = !supportsEuicc || EUICC_DEPENDENCIES.any { !isInstalledAndEnabled(pm, it) }
        val flag =
            if (disable) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

        for (pkg in EUICC_PACKAGES) {
            if (isInstalled(pm, pkg)) {
                pm.setApplicationEnabledSetting(pkg, flag, 0)
            }
        }
    }
}
