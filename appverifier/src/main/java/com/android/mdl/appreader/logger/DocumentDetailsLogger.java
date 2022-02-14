package com.android.mdl.appreader.logger;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.android.mdl.appreader.repository.CIDRepository;
import com.android.mdl.appreader.util.DefaultExecutorSupplier;
import com.android.mdl.appreader.util.NetworkHelper;
import com.android.mdl.appreader.util.NetworkUtils;
import com.google.gson.Gson;

import java.util.List;

import androidx.annotation.Nullable;

/* Class used to send analytics logs to CredenceConnect. If internet is unavailable this will save
 * logs to a local database to be sent at a later time.
 */
public class DocumentDetailsLogger {

    private static final int HTTP_CODE_OK = 200;
    private static final int HTTP_INACTIVE = 500;
    private static final int DELAY_MS = 500;
    private static final Object mSyncObject = new Object();
    private static CachedDocumentDetailsDataBase mDataBase = null;
    public static String logUrl = "";

    /* If there is internet connectivity will send log to CredenceConnect, otherwise saves log to
     * local database.
     *
     * @param log log object to send to server. Object MUST be one of the following types:
     *
     * LogOnBioInitModel.class
     * LogFingerprintModel.class
     * LogCardModel.class
     * LogFaceModel.class
     * LogICAOModel.class
     * LogMRZModel.class
     * LogIrisModel.class
     *
     * If object is of an invalid type, method will do nothing.
     */
    public static void log(Context context, NetworkHelper.MIDDetailsRequest log) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            if (null == log)
                return;
            try {
                //TODO: write a file
//                JSONObject jsonParam = new JSONObject();
//                jsonParam.put("lastReportedActivity", GenericUtils.getDeviceEpochTime());
//                FileUtils.writeLastActivityTimeToFile(App.Context, jsonParam.toString());

            } catch (Exception e) {
                 Log.d("App.TAG", "Failed to write last reported activity to file :" + e.getMessage());
            }


            /* Obtain database Instance if none. */
            if (null == mDataBase)
                mDataBase = CachedDocumentDetailsDataBase.getInstance(context);

            /* Convert log to type database understands. This means extracting all fields and converting
             * them to a JSON string. If object was of an invalid type, null be returned.
             */
            MIDDetails midDetails = DocumentDetailsLogger.toLogObject(log);
            if (null == midDetails)
                return;

            /* Save to database if no internet. */
            if (!NetworkUtils.INSTANCE.hasInternet(context)) {
                Log.d("C-service", "network not available");
                mDataBase.insert(midDetails);
            } else {
                Log.d("C-service", "network available");
                /* Send log immediately if there is internet. */
                DocumentDetailsLogger.sendLog(midDetails);

//            new Thread(() -> {
//                if(DEBUG)Log.d("C-service","inside new thread");
//                /* If log was successfully sent over, delete it from database. */
//                AnalyticsLogger.sendLog(logObject);
//                /*if (AnalyticsLogger.sendLog(logObject))
//                    mDataBase.delete(logObject.getID());*/
//            }).start();
            }
        });
    }

    /* Attempts to send over any cached analytics logs to CredenceConnect if there is internet. */
    public static void sendCachedLogs(Context context) {

        /* Obtain database Instance if none. */
        if (null == mDataBase)
            mDataBase = CachedDocumentDetailsDataBase.getInstance(context);

        /* If internet send over all logs from database. */
        if (NetworkUtils.INSTANCE.hasInternet(context))
            mDataBase.getLogs(DocumentDetailsLogger::sendLogs);
    }

    /* Converts a LogModel object to a type analytics database can understand and save.
     *
     * @param object Object to convert. Object MUST be one of the following types:
     *
     * LogOnBioInitModel.class
     * LogFingerprintModel.class
     * LogCardModel.class
     * LogFaceModel.class
     * LogICAOModel.class
     * LogMRZModel.class
     * LogIrisModel.class
     *
     * @return null is returned if object type is not recognized.
     */
    @Nullable
    private static MIDDetails toLogObject(NetworkHelper.MIDDetailsRequest detailsRequest) {
        MIDDetails midDetails = new MIDDetails();
        try {
            String jsonParam = new Gson().toJson(detailsRequest);
            Log.d("TAG", jsonParam);
            midDetails.setJSON(jsonParam);

        } catch (Exception ignore) {
        }
        return midDetails;
    }

    public static NetworkHelper.MIDDetailsRequest createDetailsRequest() {
        NetworkHelper.MIDDetailsRequest mIDRequest = new NetworkHelper.MIDDetailsRequest();
        mIDRequest.setCreatedOn(String.valueOf(System.currentTimeMillis()));
        mIDRequest.setImei("356905071680409");
        mIDRequest.setFirstName("first name");
        mIDRequest.setLastName("last name");
        mIDRequest.setMidReaderStatus("verified");
        mIDRequest.setDocId("1231222");
        mIDRequest.setDob("2012-12-12");
        mIDRequest.setLatitude("18.4880822");
        mIDRequest.setLongitude("73.9518927");
        return mIDRequest;
    }

    /* Sends over a given set of logs to CredenceConnect server in separate Thread. */
    private static void sendLogs(List<MIDDetails> logs) {

            DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .execute(() -> {
                    for (MIDDetails log : logs) {
                        /* If log was successfully sent over, delete it from database. */
                        if (DocumentDetailsLogger.sendLog(log))
                            mDataBase.delete(log.getID());

                        /* Small delay before sending over next log. */
                        SystemClock.sleep(DELAY_MS);
                    }
                });
    }

    /* Sends over a given log to CredenceConnect server.
     *
     * NOTE: You MUST call this method from a different thread. This method performs a network
     * operation and all network operations must take place on a separate thread then main thread.
     */
    private static boolean sendLog(MIDDetails log) {
        if (null == log)
            return false;

        synchronized (mSyncObject) {
            try {
                new CIDRepository().submitDetailsToServer();
            } catch (Exception e) {
                Log.w("TAG", "Unable to send analytic(s) to server." + e.getMessage());
            }

            return false;
        }
    }
}