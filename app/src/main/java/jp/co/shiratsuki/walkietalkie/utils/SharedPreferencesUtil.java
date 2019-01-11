package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * SharedPreferences工具类
 * Created at 2018/11/28 13:54
 *
 * @author LiYuliang
 * @version 1.0
 */

public class SharedPreferencesUtil {

    private static final String FILE_NAME = "ZBS";
    private static SharedPreferences mySharedPreference;
    private static SharedPreferencesUtil instance;
    private SharedPreferences.Editor editor;

    private SharedPreferencesUtil(Context context) {
        mySharedPreference = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = mySharedPreference.edit();
    }

    /**
     * 使用同步锁避免多线程的同步问题
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesUtil(context);
        }
    }

    public static SharedPreferencesUtil getInstance() {
        if (instance == null) {
            throw new RuntimeException("class should init!");
        }
        return instance;
    }

    /**
     * 保存数据
     *
     * @param key  键值
     * @param data 数据
     */
    public void saveData(String key, Object data) {
        if (data instanceof Integer) {
            editor.putInt(key, (Integer) data);
        } else if (data instanceof Boolean) {
            editor.putBoolean(key, (Boolean) data);
        } else if (data instanceof String) {
            editor.putString(key, (String) data);
        } else if (data instanceof Float) {
            editor.putFloat(key, (Float) data);
        } else if (data instanceof Long) {
            editor.putLong(key, (Long) data);
        } else if (data instanceof Set) {
            editor.putStringSet(key, (Set<String>) data);
        } else {
            editor.putString(key, data.toString());
        }
        editor.commit();
    }

    /**
     * 获取数据
     *
     * @param key      键值
     * @param defValue 默认值
     * @return 数据
     */
    public Object getData(String key, Object defValue) {
        if (defValue instanceof Integer) {
            return mySharedPreference.getInt(key, (Integer) defValue);
        } else if (defValue instanceof Boolean) {
            return mySharedPreference.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof String) {
            return mySharedPreference.getString(key, (String) defValue);
        } else if (defValue instanceof Float) {
            return mySharedPreference.getFloat(key, (Float) defValue);
        } else if (defValue instanceof Long) {
            return mySharedPreference.getLong(key, (Long) defValue);
        } else if (defValue instanceof Set) {
            return mySharedPreference.getStringSet(key, (Set<String>) defValue);
        } else {
            return mySharedPreference.getString(key, defValue.toString());
        }
    }

    /**
     * 判断是否包含数据
     *
     * @param key 键值
     * @return 是否包含
     */
    public boolean containsData(String key) {
        return mySharedPreference.getAll().containsKey(key);
    }

    /**
     * 清除所有数据
     */
    public void clearAllData() {
        editor.clear();
        editor.commit();
    }

    /**
     * 清除指定键值的数据
     *
     * @param key 键值
     */
    public void clearData(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * 模糊删除指定键值的数据
     *
     * @param key 键值
     */
    public void clearFuzzyData(String key) {
        Map<String, ?> allKeyValue = mySharedPreference.getAll();
        for (Map.Entry<String, ?> entry : allKeyValue.entrySet()) {
            if (entry.getKey().contains(key)) {
                editor.remove(entry.getKey());
            }
        }
        editor.commit();
    }

}
