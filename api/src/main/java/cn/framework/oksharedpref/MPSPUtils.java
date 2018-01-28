package cn.framework.oksharedpref;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Locale;

/**
 * Created by sevenshal on 2017/10/26.
 */

public class MPSPUtils {

    private static Boolean isProviderProcess;

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
            ProviderInfo providerInfo = ctx.getPackageManager().getProviderInfo(new ComponentName(ctx, OkSharedPrefContentProvider.class), 0);
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

    public static SharedPreferences getSharedPref(Context ctx, String name) {
        SharedPreferences preferences;
        if (MPSPUtils.isProviderProcess(ctx)) {
            preferences = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
        } else {
            preferences = OkSharedPrefContentProvider.getSharedPreferences(ctx, name, Context.MODE_PRIVATE);
        }
        return preferences;
    }

}
