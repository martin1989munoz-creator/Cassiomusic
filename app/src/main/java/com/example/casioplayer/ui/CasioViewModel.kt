package com.example.casioplayer.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.casioplayer.data.CasioDbHelper
import com.example.casioplayer.data.CasioThemeMode
import com.example.casioplayer.data.PreferencesManager
import com.example.casioplayer.data.TrackItem
import com.example.casioplayer.util.BatteryMonitor
import com.example.casioplayer.util.BraveLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CasioUiState(
    val timeFormatted: String = "00:00:00",
    val dayOfWeek: String = "MO",
    val dayOfMonth: String = "01",
    val batteryPercent: Int = 100,
    val currentTheme: CasioThemeMode = CasioThemeMode.GREEN,
    val hapticEnabled: Boolean = true,
    val isBraveInstalled: Boolean = false,
    val tracks: List<TrackItem> = emptyList(),
    val selectedIndex: Int = 0,
    val selectedFilterPlaylist: String? = null,
    val messageLine: String = "CASIO READY"
)

class CasioViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = CasioDbHelper(application)
    private val prefs = PreferencesManager(application)
    private val batteryMonitor = BatteryMonitor(application)

    private val _uiState = MutableStateFlow(
        CasioUiState(
            currentTheme = prefs.getTheme(),
            hapticEnabled = prefs.isHapticEnabled(),
            isBraveInstalled = BraveLauncher.isBraveInstalled(application)
        )
    )
    val uiState: StateFlow<CasioUiState> = _uiState.asStateFlow()

    init {
        refreshTracks()
        startTimeEngine()
        startBatteryEngine()
    }

    fun checkBraveStatus() {
        val installed = BraveLauncher.isBraveInstalled(getApplication())
        _uiState.value = _uiState.value.copy(isBraveInstalled = installed)
    }

    private fun startTimeEngine() {
        viewModelScope.launch(Dispatchers.Default) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
            val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.US)
            val dayFormat = SimpleDateFormat("dd", Locale.US)

            while (isActive) {
                val now = Date()
                val t = timeFormat.format(now)
                val dow = dayOfWeekFormat.format(now).uppercase().take(2)
                val dom = dayFormat.format(now)

                _uiState.value = _uiState.value.copy(
                    timeFormatted = t,
                    dayOfWeek = dow,
                    dayOfMonth = dom
                )
                delay(500)
            }
        }
    }

    private fun startBatteryEngine() {
        viewModelScope.launch {
            batteryMonitor.batteryPercentage.collect { pct ->
                _uiState.value = _uiState.value.copy(batteryPercent = pct)
            }
        }
    }

    fun refreshTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = dbHelper.getAllTracks()
            val filtered = if (_uiState.value.selectedFilterPlaylist == null) {
                list
            } else {
                list.filter { it.playlistName.equals(_uiState.value.selectedFilterPlaylist, ignoreCase = true) }
            }
            _uiState.value = _uiState.value.copy(
                tracks = filtered,
                selectedIndex = if (filtered.isEmpty()) 0 else _uiState.value.selectedIndex.coerceIn(0, filtered.size - 1)
            )
        }
    }

    fun selectTrack(index: Int) {
        vibrateClick()
        if (index in _uiState.value.tracks.indices) {
            val track = _uiState.value.tracks[index]
            _uiState.value = _uiState.value.copy(
                selectedIndex = index,
                messageLine = "SEL: ${track.title}"
            )
        }
    }

    fun playCurrentTrack() {
        vibrateClick()
        val tracks = _uiState.value.tracks
        if (tracks.isNotEmpty() && _uiState.value.selectedIndex in tracks.indices) {
            val item = tracks[_uiState.value.selectedIndex]
            _uiState.value = _uiState.value.copy(messageLine = "PLAY: ${item.title}")
            BraveLauncher.openYouTube(getApplication(), item.queryOrUrl)
        } else {
            _uiState.value = _uiState.value.copy(messageLine = "NO TRACK SELECTED")
        }
    }

    fun searchAndLaunch(query: String) {
        vibrateClick()
        if (query.isNotBlank()) {
            _uiState.value = _uiState.value.copy(messageLine = "SEARCH: ${query.uppercase()}")
            BraveLauncher.openYouTube(getApplication(), query)
        }
    }

    fun addTrack(title: String, queryOrUrl: String, isFav: Boolean, playlist: String) {
        vibrateClick()
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.insertTrack(
                title = title.ifBlank { "UNTITLED TRACK" },
                query = queryOrUrl.ifBlank { title },
                isFav = isFav,
                playlist = playlist.ifBlank { "DEFAULT" }
            )
            refreshTracks()
        }
    }

    fun deleteCurrentTrack() {
        vibrateClick()
        val tracks = _uiState.value.tracks
        if (tracks.isNotEmpty() && _uiState.value.selectedIndex in tracks.indices) {
            val item = tracks[_uiState.value.selectedIndex]
            viewModelScope.launch(Dispatchers.IO) {
                dbHelper.deleteTrack(item.id)
                refreshTracks()
            }
            _uiState.value = _uiState.value.copy(messageLine = "DELETED ITEM")
        }
    }

    fun toggleFavoriteCurrent() {
        vibrateClick()
        val tracks = _uiState.value.tracks
        if (tracks.isNotEmpty() && _uiState.value.selectedIndex in tracks.indices) {
            val item = tracks[_uiState.value.selectedIndex]
            viewModelScope.launch(Dispatchers.IO) {
                dbHelper.toggleFavorite(item.id, item.isFavorite)
                refreshTracks()
            }
        }
    }

    fun filterByPlaylist(playlist: String?) {
        vibrateClick()
        _uiState.value = _uiState.value.copy(selectedFilterPlaylist = playlist)
        refreshTracks()
    }

    fun cycleTheme() {
        vibrateClick()
        val current = _uiState.value.currentTheme
        val next = when (current) {
            CasioThemeMode.GREEN -> CasioThemeMode.AMBER
            CasioThemeMode.AMBER -> CasioThemeMode.MONO
            CasioThemeMode.MONO -> CasioThemeMode.GREEN
        }
        prefs.setTheme(next)
        _uiState.value = _uiState.value.copy(
            currentTheme = next,
            messageLine = "THEME: ${next.name}"
        )
    }

    fun toggleHaptic() {
        val next = !_uiState.value.hapticEnabled
        prefs.setHapticEnabled(next)
        _uiState.value = _uiState.value.copy(
            hapticEnabled = next,
            messageLine = if (next) "BEEP ON" else "BEEP MUTE"
        )
        if (next) vibrateClick(force = true)
    }

    fun dispatchMediaPlayPause() {
        vibrateClick()
        _uiState.value = _uiState.value.copy(messageLine = "CMD: TOGGLE PLAY")
        BraveLauncher.sendMediaCommand(getApplication(), KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun adjustVolume(up: Boolean) {
        vibrateClick()
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.let {
            val direction = if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            it.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            val current = it.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            _uiState.value = _uiState.value.copy(messageLine = "VOL: $current / $max")
        }
    }

    private fun vibrateClick(force: Boolean = false) {
        if (!_uiState.value.hapticEnabled && !force) return
        val app = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(35)
        }
    }
}