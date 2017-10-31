package cn.framework.mpsharedpreferences;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by sevenshal on 2017/10/26.
 */

public class MPSPUtils {

    private static Boolean isProviderProcess;

    private static HashMap<String, SharedPreferences.OnSharedPreferenceChangeListener> listenerHashMap;

    private static boolean isProviderProcess(Context ctx) {
        if (isProviderProcess != null) {
            return isProviderProcess;
        }
        String processCmdPath = String.format(Locale.getDefault(),
                "/proc/%d/cmdline", Process.myPid());
        BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader(
                    processCmdPath));
            String processName = inputStream.readLine().trim();
            ProviderInfo providerInfo = ctx.getPackageManager().getProviderInfo(new ComponentName(ctx, MPSPContentProvider.class), 0);
            isProviderProcess = TextUtils.equals(providerInfo.processName, processName);
            return isProviderProcess;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static void registListener(Context ctx, SharedPreferences pref, final String name) {
        if (listenerHashMap == null || listenerHashMap.get(name) == null) {
            synchronized (MPSPUtils.class) {
                if (listenerHashMap == null) {
                    listenerHashMap = new HashMap<>();
                }
                if (listenerHashMap.get(name) == null) {
                    final Context appCtx = ctx.getApplicationContext();
                    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences
                            .OnSharedPreferenceChangeListener() {
                        @Override
                        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                            Uri uri = Uri.withAppendedPath(MPSPContentProvider.getAuthorityUri(appCtx, name), key);
                            appCtx.getContentResolver().notifyChange(uri, null);
                        }
                    };
                    pref.registerOnSharedPreferenceChangeListener(listener);
                    listenerHashMap.put(name, listener);
                }
            }
        }
    }

    public static SharedPreferences getSharedPref(Context ctx, String name) {
        SharedPreferences preferences;
        if (MPSPUtils.isProviderProcess(ctx)) {
            preferences = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
            MPSPUtils.registListener(ctx, preferences, name);
        } else {
            preferences = MPSPContentProvider.getSharedPreferences(ctx, name, Context.MODE_PRIVATE);
        }
        return preferences;
    }

}
