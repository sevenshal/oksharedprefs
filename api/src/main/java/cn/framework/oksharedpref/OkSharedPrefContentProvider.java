package cn.framework.oksharedpref;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 使用ContentProvider实现多进程SharedPreferences读写
 */
public class OkSharedPrefContentProvider extends ContentProvider {
    private static final String TAG = "MPSP";

    private static String AUTHORITY;
    private static volatile Uri AUTHORITY_URI;
    private static final String METHOD_MP = "multi_process";
    private static final String KEY = "key";
    private static final String KEY_TYPE = "type";
    private static final String KEY_MODE = "mode";
    private static final String KEY_VALUE = "value";
    private static final String KEY_CLEAR = "clear";

    private static final int GET_ALL = 1;
    private static final int GET_STRING = 2;
    private static final int GET_INT = 3;
    private static final int GET_LONG = 4;
    private static final int GET_FLOAT = 5;
    private static final int GET_BOOLEAN = 6;
    private static final int GET_STRING_SET = 7;
    private static final int CONTAINS = 8;
    private static final int APPLY = 9;
    private static final int COMMIT = 10;

    private HashMap<String, SharedPreferences.OnSharedPreferenceChangeListener> listenerHashMap;

    private static Bundle handle(SharedPreferences sp, Bundle extras) {
        String key = extras.getString(KEY);
        int type = extras.getInt(KEY_TYPE);
        Bundle bundle = new Bundle();
        switch (type) {
            case GET_ALL:
                bundle.putSerializable(KEY_VALUE, new HashMap(sp.getAll()));
                break;
            case GET_STRING:
                bundle.putString(KEY_VALUE, sp.getString(key, extras.getString(KEY_VALUE)));
                break;
            case GET_INT:
                bundle.putInt(KEY_VALUE, sp.getInt(key, extras.getInt(KEY_VALUE)));
                break;
            case GET_LONG:
                bundle.putLong(KEY_VALUE, sp.getLong(key, extras.getLong(KEY_VALUE)));
                break;
            case GET_FLOAT:
                bundle.putFloat(KEY_VALUE, sp.getFloat(key, extras.getFloat(KEY_VALUE)));
                break;
            case GET_BOOLEAN:
                bundle.putBoolean(KEY_VALUE, sp.getBoolean(key, extras.getBoolean(KEY_VALUE)));
                break;
            case GET_STRING_SET:
                bundle.putSerializable(KEY_VALUE, wrapperSet(
                        sp.getStringSet(key, (Set<String>) extras.getSerializable(KEY_VALUE))));
                break;
            case CONTAINS:
                bundle.putBoolean(KEY_VALUE, sp.contains(key));
                break;
            case APPLY:
            case COMMIT:
                boolean clear = extras.getBoolean(KEY_CLEAR, false);
                SharedPreferences.Editor editor = sp.edit();
                if (clear) {
                    editor.clear();
                }
                HashMap<String, ?> values = (HashMap) extras.getSerializable(KEY_VALUE);
                for (Map.Entry entry : values.entrySet()) {
                    String k = (String) entry.getKey();
                    Object v = entry.getValue();
                    if (v == null) {
                        editor.remove(k);
                    } else if (v instanceof String) {
                        editor.putString(k, (String) v);
                    } else if (v instanceof Set) {
                        editor.putStringSet(k, (Set) v);
                    } else if (v instanceof Integer) {
                        editor.putInt(k, (Integer) v);
                    } else if (v instanceof Long) {
                        editor.putLong(k, (Long) v);
                    } else if (v instanceof Float) {
                        editor.putFloat(k, (Float) v);
                    } else if (v instanceof Boolean) {
                        editor.putBoolean(k, (Boolean) v);
                    }
                }
                if (type == APPLY) {
                    editor.apply();
                    bundle.putBoolean(KEY_VALUE, true);
                } else {
                    bundle.putBoolean(KEY_VALUE, editor.commit());
                }
                break;
            default:
                break;
        }
        return bundle;
    }

    @Override
    public Bundle call(String method, String name, Bundle extras) {
        if (!method.equals(METHOD_MP)) {
            return null;
        }
        int mode = extras.getInt(KEY_MODE);
        SharedPreferences sp = getContext().getSharedPreferences(name, mode);
        registListener(sp, name);
        return handle(sp, extras);
    }

    void registListener(SharedPreferences pref, final String name) {
        if (listenerHashMap == null || listenerHashMap.get(name) == null) {
            synchronized (MPSPUtils.class) {
                if (listenerHashMap == null) {
                    listenerHashMap = new HashMap<>();
                }
                if (listenerHashMap.get(name) == null) {
                    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences
                            .OnSharedPreferenceChangeListener() {
                        @Override
                        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                            Uri uri = Uri.withAppendedPath(OkSharedPrefContentProvider.getAuthorityUri(getContext(), name), key);
                            getContext().getContentResolver().notifyChange(uri, null);
                        }
                    };
                    pref.registerOnSharedPreferenceChangeListener(listener);
                    listenerHashMap.put(name, listener);
                }
            }
        }
    }

    /**
     * 如果设备处在“安全模式”下，只有系统自带的ContentProvider才能被正常解析使用；
     */
    private static boolean isSafeMode(Context context) {
        boolean isSafeMode = false;
        try {
            isSafeMode = context.getPackageManager().isSafeMode();
            // 解决崩溃：
            // java.lang.RuntimeException: Package manager has died
            // at android.app.ApplicationPackageManager.isSafeMode(ApplicationPackageManager.java:820)
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return isSafeMode;
    }

    public static Uri getAuthorityUri(Context context, String name) {
        if (AUTHORITY_URI == null) {
            synchronized (OkSharedPrefContentProvider.class) {
                if (AUTHORITY_URI == null) {
                    if (AUTHORITY == null) {
                        AUTHORITY = context.getPackageName() + ".oksharedpref";
                    }
                    AUTHORITY_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY);
                }
            }
        }
        return Uri.withAppendedPath(AUTHORITY_URI, name);
    }

    /**
     * 获取异常栈中最底层的 Throwable Cause；
     *
     * @param tr
     * @return
     */
    private static Throwable getLastCause(Throwable tr) {
        Throwable cause = tr.getCause();
        Throwable causeLast = null;
        while (cause != null) {
            causeLast = cause;
            cause = cause.getCause();
        }
        if (causeLast == null) {
            causeLast = new Throwable();
        }
        return causeLast;
    }

    /**
     * mode不使用{@link Context#MODE_MULTI_PROCESS}特可以支持多进程了；
     *
     * @param mode
     * @see Context#MODE_PRIVATE
     * @see Context#MODE_WORLD_READABLE
     * @see Context#MODE_WORLD_WRITEABLE
     */
    public static SharedPreferences getSharedPreferences(Context context, String name, int mode) {
        return isSafeMode(context) ? context.getSharedPreferences(name, mode) : new SharedPreferencesImpl(context, name, mode);
    }

    /**
     * @deprecated 此默认构造函数只用于父类ContentProvider在初始化时使用；
     */
    @Deprecated
    public OkSharedPrefContentProvider() {

    }

    private static final class SharedPreferencesImpl implements SharedPreferences {

        private static Handler uiHandler = new Handler(Looper.getMainLooper());

        private Context mContext;
        private String mName;
        private int mMode;
        private WeakHashMap<OnSharedPreferenceChangeListener, ContentObserverImplHolder> mListeners;

        private SharedPreferencesImpl(Context context, String name, int mode) {
            mContext = context;
            mName = name;
            mMode = mode;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<String, ?> getAll() {
            Map<String, ?> value = (Map<String, ?>) call(null, GET_ALL, new Bundle());
            return value == null ? new HashMap<String, Object>() : value;
        }

        @Override
        public String getString(String key, String defValue) {
            Bundle arg = new Bundle();
            arg.putString(KEY_VALUE, defValue);
            return (String) call(key, GET_STRING, arg);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Bundle arg = new Bundle();
            arg.putSerializable(KEY_VALUE, wrapperSet(defValues));
            return (Set<String>) call(key, GET_STRING_SET, arg);
        }

        @Override
        public int getInt(String key, int defValue) {
            Bundle arg = new Bundle();
            arg.putInt(KEY_VALUE, defValue);
            return (Integer) call(key, GET_INT, arg);
        }

        @Override
        public long getLong(String key, long defValue) {
            Bundle arg = new Bundle();
            arg.putLong(KEY_VALUE, defValue);
            return (Long) call(key, GET_LONG, arg);
        }

        @Override
        public float getFloat(String key, float defValue) {
            Bundle arg = new Bundle();
            arg.putFloat(KEY_VALUE, defValue);
            return (Float) call(key, GET_FLOAT, arg);
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Bundle arg = new Bundle();
            arg.putBoolean(KEY_VALUE, defValue);
            return (Boolean) call(key, GET_BOOLEAN, arg);
        }

        @Override
        public boolean contains(String key) {
            return (Boolean) call(key, CONTAINS, new Bundle());
        }

        @Override
        public Editor edit() {
            return new EditorImpl();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
            Uri uri = getAuthorityUri(mContext, mName);
            ContentObserverImplHolder observer = new ContentObserverImplHolder(listener);
            mContext.getContentResolver()
                    .registerContentObserver(uri, true, observer.observer);
            getListeners().put(listener, observer);
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            ContentObserverImplHolder holder = getListeners().remove(listener);
            if (holder != null) {
                holder.observer.destroy();
            }
        }

        private Map<OnSharedPreferenceChangeListener, ContentObserverImplHolder> getListeners() {
            if (mListeners == null) {
                synchronized (this) {
                    if (mListeners == null) {
                        mListeners = new WeakHashMap<>();
                    }
                }
            }
            return mListeners;
        }

        private class ContentObserverImplHolder {
            ContentObserverImpl observer;

            ContentObserverImplHolder(OnSharedPreferenceChangeListener listener) {
                observer = new ContentObserverImpl(listener);
            }

            @Override
            protected void finalize() throws Throwable {
                try {
                    observer.destroy();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                super.finalize();
            }
        }

        private class ContentObserverImpl extends ContentObserver {

            private WeakReference<OnSharedPreferenceChangeListener> listenerRef;

            private boolean destroy;

            private ContentObserverImpl(OnSharedPreferenceChangeListener listener) {
                super(uiHandler);
                this.listenerRef = new WeakReference<OnSharedPreferenceChangeListener>(listener);
            }

            public void destroy() {
                if (!destroy) {
                    synchronized (this) {
                        if (!destroy) {
                            mContext.getContentResolver()
                                    .unregisterContentObserver(this);
                            destroy = true;
                        }
                    }
                }
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                OnSharedPreferenceChangeListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, uri.getLastPathSegment());
                } else {
                    destroy();
                }
            }
        }

        private Object call(String key, int type, Bundle arg) {
            Uri uri = getAuthorityUri(mContext, mName);
            arg.putInt(KEY_MODE, mMode);
            arg.putString(KEY, key);
            arg.putInt(KEY_TYPE, type);
            try {
                Bundle ret = mContext.getContentResolver().call(uri, METHOD_MP, mName, arg);
                return ret.get(KEY_VALUE);
            } catch (Throwable e) {
                e.printStackTrace();
                return handle(mContext.getSharedPreferences(mName, mMode), arg).get(KEY_VALUE);
            }
        }

        public final class EditorImpl implements Editor {
            private final HashMap<String, Object> mModified = new HashMap();
            private boolean mClear = false;

            @Override
            public Editor putString(String key, String value) {
                synchronized (this) {
                    mModified.put(key, value);
                    return this;
                }
            }

            @Override
            public Editor putStringSet(String key, Set<String> values) {
                synchronized (this) {
                    mModified.put(key, values);
                    return this;
                }
            }

            @Override
            public Editor putInt(String key, int value) {
                synchronized (this) {
                    mModified.put(key, value);
                    return this;
                }
            }

            @Override
            public Editor putLong(String key, long value) {
                synchronized (this) {
                    mModified.put(key, value);
                    return this;
                }
            }

            @Override
            public Editor putFloat(String key, float value) {
                synchronized (this) {
                    mModified.put(key, value);
                    return this;
                }
            }

            @Override
            public Editor putBoolean(String key, boolean value) {
                synchronized (this) {
                    mModified.put(key, value);
                    return this;
                }
            }

            @Override
            public Editor remove(String key) {
                synchronized (this) {
                    mModified.put(key, null);
                    return this;
                }
            }

            @Override
            public Editor clear() {
                synchronized (this) {
                    mModified.clear();
                    mClear = true;
                    return this;
                }
            }

            @Override
            public void apply() {
                setValue(APPLY);
            }

            @Override
            public boolean commit() {
                return setValue(COMMIT);
            }

            private boolean setValue(int type) {
                Bundle extras = new Bundle();
                extras.putSerializable(KEY_VALUE, mModified);
                extras.putBoolean(KEY_CLEAR, mClear);
                return (Boolean) call(null, type, extras);
            }
        }

    }

    private static HashSet<String> wrapperSet(Set<String> set) {
        return set == null ? null : (set instanceof HashMap ? (HashSet<String>) set : new HashSet<String>(set));
    }

    private static String makeAction(String name) {
        return String.format("%1$s_%2$s", OkSharedPrefContentProvider.class.getName(), name);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException(" No external query");
    }

    @SuppressWarnings("unchecked")
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException(" No external update");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("No external call");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external insert");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("No external delete");
    }

}