package com.example.casioplayer.data

enum class CasioThemeMode(val displayName: String) {
    GREEN("LCD GREEN (F-91W)"),
    AMBER("LCD AMBER (RETRO)"),
    MONO("LCD MONOCHROME")
}

data class TrackItem(
    val id: Long = 0,
    val title: String,
    val queryOrUrl: String,
    val isFavorite: Boolean = false,
    val playlistName: String = "DEFAULT"
)