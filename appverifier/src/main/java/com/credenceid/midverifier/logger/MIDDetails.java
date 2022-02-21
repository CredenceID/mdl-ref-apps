package com.credenceid.midverifier.logger;

import java.util.Locale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/* Object saved in cached analytics database.
 *
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity
public class MIDDetails {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    /* Database record ID. This will be auto assigned by database. */
    private long mID;

    @ColumnInfo(name = "data")
    /* Server API log input fields as a JSON string. */
    private String mJSON;

    public MIDDetails() {
        mJSON = "";
    }

    public MIDDetails(String JSON) {
        mJSON = JSON;
    }

    public long getID() {
        return mID;
    }

    public void setID(long ID) {
        mID = ID;
    }

    public String getJSON() {
        return mJSON;
    }

    public void setJSON(String JSON) {
        mJSON = JSON;
    }

    /*@Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "ID: %d\nLogType: %s, JSON: %s",
                mID, mJSON);
    }*/
}
