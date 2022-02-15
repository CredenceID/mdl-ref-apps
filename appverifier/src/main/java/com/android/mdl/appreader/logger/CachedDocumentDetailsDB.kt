package com.android.mdl.appreader.logger

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

@Database(entities = [MIDDetails::class], version = 1, exportSchema = false)
abstract class CachedDocumentDetailsDB : RoomDatabase() {

    companion object {
        private val sLock = Any()

        private val DB_NAME = "cached_document_details.db"

        private var INSTANCE: CachedDocumentDetailsDB? = null

        private val mutex = Mutex()

        open fun getInstance(context: Context?): CachedDocumentDetailsDB? {
            synchronized(mutex) {
                if (null == INSTANCE) {
                    INSTANCE =
                        Room.databaseBuilder(
                            context!!,
                            CachedDocumentDetailsDB::class.java, DB_NAME
                        ).build()
                }
                return INSTANCE
            }
        }
    }

    open fun insert(log: MIDDetails?) {
        CoroutineScope(Dispatchers.IO).launch {
            mDAO().insert(log)
        }
    }

    open fun delete(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            mDAO().delete(id)
        }
    }

    open fun getLogs(callback: OnGet) {
        CoroutineScope(Dispatchers.IO).launch {
            callback.onGet(mDAO().get())
        }
    }

    abstract fun mDAO(): CachedLogDBDAO

    interface OnGet {
        fun onGet(logs: List<MIDDetails>)
    }
}