package cn.framework.oksharedpref.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

import cn.framework.R;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    MsgPrefs msgPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SeActivity.class));
            }
        });

        msgPrefs = MsgPrefs.defaultInstance(this);
        Log.i("MPM", "deviceId:" + msgPrefs.getDeviceId()
                + ";userId:" + msgPrefs.getUserid()
                + ";isLogin:" + msgPrefs.isLogin() + ";token:" + msgPrefs.getToken());

        msgPrefs.prefs().registerOnSharedPreferenceChangeListener(this);

        Set set = new HashSet<String>();
        set.add("a");
        set.add("b");
        msgPrefs.edit().setDeviceId(111).setLogin(true).setUserid("userid").setToken(set).apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("MP", "main_onSharedPreferenceChanged:" + key);
        Log.i("MPM", "deviceId:" + msgPrefs.getDeviceId()
                + ";userId:" + msgPrefs.getUserid()
                + ";isLogin:" + msgPrefs.isLogin() + ";token:" + msgPrefs.getToken());
    }
}
