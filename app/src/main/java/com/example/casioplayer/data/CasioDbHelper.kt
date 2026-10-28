package com.example.casioplayer.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CasioDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "casio_tracks.db"
        const val DATABASE_VERSION = 1

        const val TABLE_TRACKS = "tracks"
        const val COL_ID = "_id"
        const val COL_TITLE = "title"
        const val COL_QUERY = "query"
        const val COL_IS_FAV = "is_favorite"
        const val COL_PLAYLIST = "playlist_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSql = """
            CREATE TABLE $TABLE_TRACKS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_QUERY TEXT NOT NULL,
                $COL_IS_FAV INTEGER NOT NULL DEFAULT 0,
                $COL_PLAYLIST TEXT NOT NULL DEFAULT 'DEFAULT'
            );
        """.trimIndent()
        db.execSQL(createSql)

        insertInitialTrack(db, "DAFT PUNK - ONE MORE TIME", "https://www.youtube.com/results?search_query=daft+punk+one+more+time", 1, "FAVORITES")
        insertInitialTrack(db, "KRAFTWERK - THE MODEL", "https://www.youtube.com/results?search_query=kraftwerk+the+model", 1, "FAVORITES")
        insertInitialTrack(db, "DEPECHE MODE - ENJOY THE SILENCE", "https://www.youtube.com/results?search_query=depeche+mode+enjoy+the+silence", 0, "RETRO 80S")
    }

    private fun insertInitialTrack(db: SQLiteDatabase, title: String, query: String, isFav: Int, playlist: String) {
        val cv = ContentValues().apply {
            put(COL_TITLE, title)
            put(COL_QUERY, query)
            put(COL_IS_FAV, isFav)
            put(COL_PLAYLIST, playlist)
        }
        db.insert(TABLE_TRACKS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRACKS")
        onCreate(db)
    }

    fun getAllTracks(): List<TrackItem> {
        val list = mutableListOf<TrackItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TRACKS, null, null, null, null, null, "$COL_ID DESC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COL_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COL_TITLE))
                val query = it.getString(it.getColumnIndexOrThrow(COL_QUERY))
                val isFav = it.getInt(it.getColumnIndexOrThrow(COL_IS_FAV)) == 1
                val playlist = it.getString(it.getColumnIndexOrThrow(COL_PLAYLIST))
                list.add(TrackItem(id, title, query, isFav, playlist))
            }
        }
        return list
    }

    fun insertTrack(title: String, query: String, isFav: Boolean, playlist: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_TITLE, title.uppercase())
            put(COL_QUERY, query)
            put(COL_IS_FAV, if (isFav) 1 else 0)
            put(COL_PLAYLIST, playlist.uppercase())
        }
        return db.insert(TABLE_TRACKS, null, cv)
    }

    fun deleteTrack(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_TRACKS, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_IS_FAV, if (current) 0 else 1)
        }
        db.update(TABLE_TRACKS, cv, "$COL_ID = ?", arrayOf(id.toString()))
    }
}