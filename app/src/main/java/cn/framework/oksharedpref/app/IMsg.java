package cn.framework.oksharedpref.app;


import cn.framework.oksharedpref.annotations.DefaultValue;
import cn.framework.oksharedpref.annotations.PreferenceType;
import cn.framework.oksharedpref.annotations.SharedPreference;
import cn.framework.oksharedpref.annotations.Type;

/**
 * Created by sevenshal on 2016/11/28.
 */

@SharedPreference(value = "Msg", implSharedPreference = false, preferenceName = "msg", multiProcess = false)
public interface IMsg {

    @DefaultValue(value = "null", createDefaultGetter = false)
    String USERID = "userId";

    @Type(PreferenceType.STRING_SET)
    @DefaultValue(value = "null", createDefaultGetter = false)
    String TOKEN = "token";

    @Type(PreferenceType.LONG)
    @DefaultValue(value = "0", createDefaultGetter = false)
    String DEVICE_ID = "deviceId";

    @Type(PreferenceType.BOOLEAN)
    @DefaultValue(value = "false", createDefaultGetter = false)
    String LOGIN = "hasAuth";

    String NICK_NAME = "nickName";

}
