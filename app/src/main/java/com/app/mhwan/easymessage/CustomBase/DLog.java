package com.app.mhwan.easymessage.CustomBase;

import android.util.Log;

/**
 * Created by Mhwan on 2016. 4. 8..
 */
public class DLog {
    static final String TAG = "Mhwan";

    /** Log Level Error **/
    public static final void e(String message) {
        if (AppContext.getDEBUG_MODE()) Log.e(TAG, buildLogMsg(message));
    }
    /** Log Level Warning **/
    public static final void w(String message) {
        if (AppContext.getDEBUG_MODE()) Log.w(TAG, buildLogMsg(message));
    }
    /** Log Level Information **/
    public static final void i(String message) {
        if (AppContext.getDEBUG_MODE()) Log.i(TAG, buildLogMsg(message));
    }
    /** Log Level Debug **/
    public static final void d(String message) {
        if (AppContext.getDEBUG_MODE()) Log.d(TAG, buildLogMsg(message));
    }
    /** Log Level Verbose **/
    public static final void v(String message) {
        if (AppContext.getDEBUG_MODE()) Log.v(TAG, buildLogMsg(message));
    }


    private static String buildLogMsg(String message) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);

        return sb.toString();
    }
}
