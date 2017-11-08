package cn.framework.oksharedpref.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sevenshal on 2017/10/23.
 */

public class SeActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    MsgPrefs msgPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msgPrefs = MsgPrefs.defaultInstance(this);
        Log.i("MPM", "deviceId:" + msgPrefs.getDeviceId()
                + ";userId:" + msgPrefs.getUserid()
                + ";isLogin:" + msgPrefs.isLogin() + ";token:" + msgPrefs.getToken());

        Set set = new HashSet<String>();
        set.add("a2");
        set.add("b2");

        msgPrefs.prefs().registerOnSharedPreferenceChangeListener(this);

        msgPrefs.edit().setToken(set).setUserid("userid2").setLogin(false).setDeviceId(222).apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("MP", "onSharedPreferenceChanged:" + key);
        Log.i("MPM", "deviceId:" + msgPrefs.getDeviceId()
                + ";userId:" + msgPrefs.getUserid()
                + ";isLogin:" + msgPrefs.isLogin() + ";token:" + msgPrefs.getToken());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        msgPrefs.prefs().unregisterOnSharedPreferenceChangeListener(this);
    }
}
