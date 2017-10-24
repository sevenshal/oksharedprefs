package cn.framework.mpsharedpreferences;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by sevenshal on 2017/10/23.
 */

public class BasePrefs {

    protected SharedPreferences mPreferences;

    private static String processName;

    private static HashMap<String, SharedPreferences.OnSharedPreferenceChangeListener> listenerHashMap;

    static {
        String processCmdPath = String.format(Locale.getDefault(),
                "/proc/%d/cmdline", Process.myPid());
        BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader(
                    processCmdPath));
            processName = inputStream.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
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

    protected BasePrefs(Context ctx, final String name) {
        try {
            ProviderInfo providerInfo = ctx.getPackageManager().getProviderInfo(new ComponentName(ctx, MPSPContentProvider.class), 0);
            if (!TextUtils.equals(providerInfo.processName, processName)) {
                mPreferences = MPSPContentProvider.getSharedPreferences(ctx, name, Context.MODE_PRIVATE);
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        this.mPreferences = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
        if (listenerHashMap == null || listenerHashMap.get(name) == null) {
            synchronized (BasePrefs.class) {
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
                    mPreferences.registerOnSharedPreferenceChangeListener(listener);
                    listenerHashMap.put(name, listener);
                }
            }
        }
    }

    public SharedPreferences prefs() {
        return mPreferences;
    }
}
