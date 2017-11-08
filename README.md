# oksharedpref

```
通过注解生成SharedPreferences实现的工具。解决安卓SharedPreferences多进程数据访问不一致的问题。
```

##简介
1.让你告别手写胶水代码管理SharedPreferences的包装类，通过注解的方式定义SharedPreferences包装类的属性和默认值。
2.现在看来，安卓官方基本放弃了解决SharedPreferences跨进程访问不一致这一问题了，跨进程访问数据官方更加推荐ContentProvider。
3.OkSharedPrefs将SharedPreferences和ContentProvider结合起来，让你使用SharedPreferences更加方便，并且通过ContentProvider交换数据，解决了跨进程数据访问不一致的问题。

##安装

```groovy
allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}

dependencies {
    compile 'com.github.sevenshal:api:1.0.0'
    annotationProcessor 'com.github.sevenshal:processor:1.0.0'
}
```

##用法

定一个interface类并且如下所示添加注解：

```java

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
```

然后就可以直接使用生成的包装类了：

``` java

    MsgPrefs msgPrefs = MsgPrefs.defaultInstance(this);
    long deviceId = msgPrefs.getDeviceId();
    String userId = msgPrefs.getUserid();
    boolean login = msgPrefs.isLogin();
    String nickName= msgPrefs.getNickName("未命名");
    
    msgPrefs.prefs().registerOnSharedPreferenceChangeListener(this);

    Set set = new HashSet<String>();
    set.add("a");
    set.add("b");
    msgPrefs.edit().setDeviceId(111).setLogin(true).setUserid("userid").setToken(set).apply();

```

##说明
生成的类的名称通过 @SharedPreferences 的 value属性定义。生成的类名称为 value+Prefs，比如

@SharedPreference(value = "Msg") 将生成 MsgPrefs 类。

如果你不希望以Prefs结尾，preferencesSuffix 修改。

@SharedPreference(value = "Msg", preferencesSuffix = "Preferences") 将生成 MsgPreferences类。

OkSharedPrefs生成的包装类默认实现了SharedPreferences接口，
这在key值通过变量方式存取时很方便，如果不希望生成的类实现SharedPreferences接口，
可以通过将 implSharedPreference 设置为 false，关闭该功能。此种情况下，可以通过生成的类的prefs()获取SharedPreferences接口实例。所以这种情况下不要在interface中定义名为PREFS的属性。

默认的SharedPreferences文件名为default_preferences，你可以通过 preferenceName 修改。

**默认生成的包装类不支持跨进程，但是可以通过将 multiProcess 设置为true打开该功能，默认关闭该功能是出于性能考虑，减少生成不必要的代码。**

生成的包装类是单例模式的，因为安卓底层SharedPreferences也是全局单实例的，所以不会单例模式并不会带来性能问题。
考虑到在插件化系统中Context可能会做隔离的使用场景，你仍然可以通过 new MsgPrefs(context)的方式来使用。
甚至可以new MsgPrefs(context,name)来通过相同结构的包装类管理不同的属性文件，这对那种多用户数据管理的app很有用。

所有属性默认类型是String类型，通过为interface的属性添加
@Type(PreferenceType.LONG)
来修改类型。支持完整的SharedPreferences数据类型。

通过 @DefaultValue(value = "null", createDefaultGetter = false) 可以设置默认值，以及是否生成默认值取值方法。
createDefaultGetter的取值意义在于你是希望通过 msgPrefs.getNickName("自定义默认值") 还是 msgPrefs.getNickName() 获取数据。如果你在编码期间不确定默认值是什么，那需要将createDefaultGetter设为true。

