package com.android.mdl.appreader.logger;

import android.content.Context;

import com.android.mdl.appreader.util.DiskIOThreadExecutor;

import java.util.List;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/* Object used to interface with local cached analytics database on device. This database is
 * responsible for storing all analytics logs which have not yet been posted to server. Whenever a
 * log is generated it will be placed to this database. There is a separate thread in charge of
 * taking logs in this database and posting them to server.
 */
@SuppressWarnings("WeakerAccess")
@Database(entities = {MIDDetails.class},
        version = 1,
        exportSchema = false)
public abstract class CachedDocumentDetailsDataBase extends RoomDatabase {

    private static final Object sLock = new Object();

    private static final String DB_NAME = "cached_document_details.db";

    private static CachedDocumentDetailsDataBase INSTANCE = null;

    private static DiskIOThreadExecutor mDiskIOExecutor = null;

    public static CachedDocumentDetailsDataBase getInstance(Context context) {

        synchronized (sLock) {
            if (null == INSTANCE)
                INSTANCE = Room.databaseBuilder(context, CachedDocumentDetailsDataBase.class, DB_NAME).build();
            if (null == mDiskIOExecutor)
                mDiskIOExecutor = new DiskIOThreadExecutor();
            return INSTANCE;
        }
    }

    public void insert(MIDDetails log) {
        mDiskIOExecutor.execute(() -> mDAO().insert(log));
    }

    public void delete(long id) {
        mDiskIOExecutor.execute(() -> mDAO().delete(id));
    }

    public void getLogs(OnGet callback) {
        mDiskIOExecutor.execute(() -> callback.onGet(mDAO().get()));
    }

    public abstract CachedLogDataBaseDAO mDAO();

    interface OnGet {
        void onGet(List<MIDDetails> logs);
    }
}
