package com.example.casioplayer.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import android.widget.Toast

object BraveLauncher {

    const val BRAVE_PACKAGE = "com.brave.browser"

    fun isBraveInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(BRAVE_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openYouTube(context: Context, queryOrUrl: String) {
        val targetUrl = if (queryOrUrl.startsWith("http://") || queryOrUrl.startsWith("https://")) {
            queryOrUrl
        } else {
            val encodedQuery = Uri.encode(queryOrUrl)
            "https://www.youtube.com/results?search_query=$encodedQuery"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (isBraveInstalled(context)) {
            intent.setPackage(BRAVE_PACKAGE)
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "Brave no detectado. Abriendo en navegador por defecto.",
                Toast.LENGTH_LONG
            ).show()
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "No hay navegadores compatibles instalados.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openBraveInPlayStore(context: Context) {
        val marketUri = Uri.parse("market://details?id=$BRAVE_PACKAGE")
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$BRAVE_PACKAGE")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    fun sendMediaCommand(context: Context, keyCode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }
}