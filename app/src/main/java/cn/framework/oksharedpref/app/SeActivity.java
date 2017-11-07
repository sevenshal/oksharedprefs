package cn.framework.oksharedpref.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sevenshal on 2017/10/23.
 */

public class SeActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    MsgPrefs msgPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msgPrefs = MsgPrefs.defaultInstance(this);
        Log.d("MP", "did:" + msgPrefs.getDeviceId()
                + ";uid:" + msgPrefs.getUserid()
                + ";l:" + msgPrefs.isLogin() + ";s:" + msgPrefs.getToken());

        Set set = new HashSet<String>();
        set.add("aSF");
        set.add("bSDF");

        msgPrefs.prefs().registerOnSharedPreferenceChangeListener(this);

        msgPrefs.edit().setToken(set).setUserid("3rerwer").setLogin(false).setDeviceId(473894234).apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("MP", "onSharedPreferenceChanged:" + key);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        msgPrefs.prefs().unregisterOnSharedPreferenceChangeListener(this);
    }
}
