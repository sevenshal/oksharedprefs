package cn.framework.mpsharedpreferences.app;


import cn.framework.mpsharedpreferences.annotations.DefaultValue;
import cn.framework.mpsharedpreferences.annotations.PreferenceType;
import cn.framework.mpsharedpreferences.annotations.SharedPreference;
import cn.framework.mpsharedpreferences.annotations.Type;

/**
 * Created by sevenshal on 2016/11/28.
 */

@SharedPreference(value = "Msg", implSharedPreference = false, preferenceName = "msg")
public interface IMsg {

    @DefaultValue(value = "null", createDefaultGetter = false)
    String USERID = "userName";

    @Type(PreferenceType.STRING_SET)
    @DefaultValue(value = "null", createDefaultGetter = false)
    String TOKEN = "token";

    @Type(PreferenceType.LONG)
    @DefaultValue(value = "0", createDefaultGetter = false)
    String DEVICE_ID = "deviceId";

    @Type(PreferenceType.BOOLEAN)
    @DefaultValue(value = "false", createDefaultGetter = false)
    String LOGIN = "hasAuth";

}
