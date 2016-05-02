package com.app.mhwan.easymessage.CustomBase;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by Mhwan on 2016. 4. 5..
 */
public class AppContext extends Application {
    private static Context context;
    private static boolean DEBUG_MODE = false;
    public static Context getContext() {
        return context;
    }
    public static boolean getDEBUG_MODE(){
        return DEBUG_MODE;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        this.DEBUG_MODE = isDebuggableMode();
    }

    private boolean isDebuggableMode() {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /* debuggable variable will remain false */
        }

        return debuggable;
    }
}
