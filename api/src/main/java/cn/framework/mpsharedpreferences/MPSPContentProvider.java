package cn.framework.mpsharedpreferences;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 使用ContentProvider实现多进程SharedPreferences读写
 */
public class MPSPContentProvider extends ContentProvider {
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
    private static final int CONTAINS = 7;
    private static final int APPLY = 8;
    private static final int COMMIT = 9;
    private static final int REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER = 10;
    private static final int UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER = 11;
    private static final int GET_STRING_SET = 12;
    private HashMap<String, Integer> mListenersCount;

    @Override
    public Bundle call(String method, String name, Bundle extras) {
        if (!method.equals(METHOD_MP)) {
            return null;
        }
        int mode = extras.getInt(KEY_MODE);
        SharedPreferences sp = getSystemSharedPreferences(name, mode);
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
                bundle.putSerializable(KEY_VALUE,
                        new HashSet<>(
                                sp.getStringSet(key, (Set<String>) extras.getSerializable(KEY_VALUE))));
                break;
            case CONTAINS:
                bundle.putBoolean(KEY_VALUE, sp.contains(key));
                break;
            case REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER: {
                checkInitListenersCount();
                Integer countInteger = mListenersCount.get(name);
                int count = (countInteger == null ? 0 : countInteger) + 1;
                mListenersCount.put(name, count);
                countInteger = mListenersCount.get(name);
                bundle.putBoolean(KEY_VALUE, count == (countInteger == null ? 0 : countInteger));
            }
            break;
            case UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER: {
                checkInitListenersCount();
                Integer countInteger = mListenersCount.get(name);
                int count = (countInteger == null ? 0 : countInteger) - 1;
                if (count <= 0) {
                    mListenersCount.remove(name);
                    bundle.putBoolean(KEY_VALUE, !mListenersCount.containsKey(name));
                } else {
                    mListenersCount.put(name, count);
                    countInteger = mListenersCount.get(name);
                    bundle.putBoolean(KEY_VALUE, count == (countInteger == null ? 0 : countInteger));
                }
            }
            break;
            case APPLY:
            case COMMIT: {
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
            }
            break;
        }
        return bundle;
    }

    // 如果设备处在“安全模式”下，只有系统自带的ContentProvider才能被正常解析使用；
    private static boolean isSafeMode(Context context) {
        boolean isSafeMode = false;
        try {
            isSafeMode = context.getPackageManager().isSafeMode();
            // 解决崩溃：
            // java.lang.RuntimeException: Package manager has died
            // at android.app.ApplicationPackageManager.isSafeMode(ApplicationPackageManager.java:820)
        } catch (RuntimeException e) {
            if (!isPackageManagerHasDiedException(e)) {
                throw e;
            }
        }
        return isSafeMode;
    }

    public static Uri getAuthorityUri(Context context, String name) {
        if (AUTHORITY_URI == null) {
            synchronized (MPSPContentProvider.class) {
                if (AUTHORITY_URI == null) {
                    if (AUTHORITY == null) {
                        AUTHORITY = context.getPackageName() + ".multiprocesssharedpreferences";
                    }
                    AUTHORITY_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY);
                }
            }
        }
        return Uri.withAppendedPath(AUTHORITY_URI, name);
    }

    private static boolean isPackageManagerHasDiedException(Throwable e) {
        if (e instanceof RuntimeException
                && e.getMessage() != null
                && e.getMessage().contains("Package manager has died")) {
            Throwable cause = getLastCause(e);
            if (cause instanceof DeadObjectException || cause.getClass().getName().equals("android.os.TransactionTooLargeException")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUnstableCountException(Throwable e) {
        if (e instanceof RuntimeException
                && e.getMessage() != null
                && e.getMessage().contains("unstableCount < 0: -1")) {
            if (getLastCause(e) instanceof IllegalStateException) {
                return true;
            }
        }
        return false;
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
        return new SharedPreferencesImpl(context, name, mode);
    }

    /**
     * @deprecated 此默认构造函数只用于父类ContentProvider在初始化时使用；
     */
    @Deprecated
    public MPSPContentProvider() {

    }

    private static final class SharedPreferencesImpl implements SharedPreferences {

        private static Handler uiHandler = new Handler(Looper.getMainLooper());

        private Context mContext;
        private String mName;
        private int mMode;
        private boolean mIsSafeMode;
        private Map<OnSharedPreferenceChangeListener, ContentObserverImpl> mListeners;

        private SharedPreferencesImpl(Context context, String name, int mode) {
            mContext = context;
            mName = name;
            mMode = mode;
            mIsSafeMode = isSafeMode(mContext);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<String, ?> getAll() {
            Map<String, ?> value = (Map<String, ?>) call(null, GET_ALL, null);
            return value == null ? new HashMap<String, Object>() : value;
        }

        @Override
        public String getString(String key, String defValue) {
            Bundle arg = new Bundle();
            arg.putString(KEY_VALUE, defValue);
            return (String) call(key, GET_STRING, arg);
        }

        //	@Override // Android 3.0
        @SuppressWarnings("unchecked")
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Bundle arg = new Bundle();
            arg.putSerializable(KEY_VALUE, defValues == null ?
                    null : (defValues instanceof Serializable ?
                    (Serializable) defValues : new HashSet(defValues)));
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
            Bundle arg = new Bundle();
            arg.putBoolean(KEY_VALUE, false);
            return (Boolean) call(key, CONTAINS, arg);
        }

        @Override
        public Editor edit() {
            return new EditorImpl();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
            if (!mIsSafeMode) {
                Uri uri = getAuthorityUri(mContext, mName);
                ContentObserverImpl observer = new ContentObserverImpl(listener);
                mContext.getContentResolver()
                        .registerContentObserver(uri, true, observer);

                getListeners().put(listener, observer);
            }
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            if (!mIsSafeMode) {
                mContext.getContentResolver()
                        .unregisterContentObserver(getListeners().get(listener));
            }
        }

        private Map<OnSharedPreferenceChangeListener, ContentObserverImpl> getListeners() {
            if (mListeners == null) {
                synchronized (this) {
                    if (mListeners == null) {
                        mListeners = new HashMap<>();
                    }
                }
            }
            return mListeners;
        }

        private class ContentObserverImpl extends ContentObserver {

            private OnSharedPreferenceChangeListener listener;

            private ContentObserverImpl(OnSharedPreferenceChangeListener listener) {
                super(uiHandler);
                this.listener = listener;
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, uri.getLastPathSegment());
            }
        }

        private Object call(String key, int type, Bundle arg) {
            if (!mIsSafeMode) {
                Uri uri = getAuthorityUri(mContext, mName);
                arg.putInt(KEY_MODE, mMode);
                arg.putString(KEY, key);
                arg.putInt(KEY_TYPE, type);
                Bundle ret = mContext.getContentResolver().call(uri, METHOD_MP, mName, arg);
                return ret.get(KEY_VALUE);
            }
            return arg.get(KEY_VALUE);
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
                synchronized (this) {
                    try {
                        Bundle extras = new Bundle();
                        extras.putSerializable(KEY_VALUE, mModified);
                        extras.putBoolean(KEY_CLEAR, mClear);
                        return (Boolean) call(null, type, extras);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (RuntimeException e) {
                        if (!isPackageManagerHasDiedException(e) && !isUnstableCountException(e)) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return false;
            }
        }

    }

    private static String makeAction(String name) {
        return String.format("%1$s_%2$s", MPSPContentProvider.class.getName(), name);
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

    private SharedPreferences getSystemSharedPreferences(String name, int mode) {
        return getContext().getSharedPreferences(name, mode);
    }

    private void checkInitListenersCount() {
        if (mListenersCount == null) {
            mListenersCount = new HashMap<String, Integer>();
        }
    }
}