package com.example.antiddiction.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * 偏好设置管理器
 * 统一管理应用的所有配置
 */
public class PreferenceManager {

    private static final String TAG = "PreferenceManager";
    private static final String PREF_NAME = "anti_addiction_prefs";
    
    // 配置键
    private static final String KEY_RESTRICTED_APPS = "restricted_apps";
    private static final String KEY_VERIFY_INTERVAL = "verify_interval_minutes";
    private static final String KEY_DAILY_LIMIT = "daily_limit_minutes";
    private static final String KEY_FACE_REGISTERED = "face_registered";
    private static final String KEY_FACE_EMBEDDING = "face_embedding";
    private static final String KEY_TODAY_USAGE = "today_usage";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";

    // 默认值
    public static final int DEFAULT_VERIFY_INTERVAL = 5; // 5 分钟
    public static final int DEFAULT_DAILY_LIMIT = 60; // 60 分钟

    /**
     * 获取限制应用列表
     */
    public static Set<String> getRestrictedApps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_RESTRICTED_APPS, new HashSet<>());
    }

    /**
     * 保存限制应用列表
     */
    public static void saveRestrictedApps(Context context, Set<String> packages) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_RESTRICTED_APPS, packages).apply();
        Log.i(TAG, "保存了 " + packages.size() + " 个限制应用");
    }

    /**
     * 获取验证间隔（分钟）
     */
    public static int getVerifyInterval(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_VERIFY_INTERVAL, DEFAULT_VERIFY_INTERVAL);
    }

    /**
     * 保存验证间隔
     */
    public static void saveVerifyInterval(Context context, int minutes) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_VERIFY_INTERVAL, minutes).apply();
        Log.i(TAG, "验证间隔：" + minutes + "分钟");
    }

    /**
     * 获取每日使用时长限制（分钟）
     */
    public static int getDailyLimit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DAILY_LIMIT, DEFAULT_DAILY_LIMIT);
    }

    /**
     * 保存每日使用时长限制
     */
    public static void saveDailyLimit(Context context, int minutes) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DAILY_LIMIT, minutes).apply();
        Log.i(TAG, "每日限制：" + minutes + "分钟");
    }

    /**
     * 检查人脸是否已注册
     */
    public static boolean isFaceRegistered(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_FACE_REGISTERED, false);
    }

    /**
     * 标记人脸已注册
     */
    public static void setFaceRegistered(Context context, boolean registered) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FACE_REGISTERED, registered).apply();
    }

    /**
     * 保存人脸特征（简化版，实际应加密存储）
     */
    public static void saveFaceEmbedding(Context context, byte[] embedding) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // 将 byte 数组转为 base64 字符串存储
        String base64 = android.util.Base64.encodeToString(embedding, android.util.Base64.DEFAULT);
        prefs.edit().putString(KEY_FACE_EMBEDDING, base64).apply();
        setFaceRegistered(context, true);
    }

    /**
     * 获取人脸特征
     */
    public static byte[] getFaceEmbedding(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String base64 = prefs.getString(KEY_FACE_EMBEDDING, null);
        if (base64 != null) {
            return android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
        }
        return null;
    }

    /**
     * 获取今日已使用时长（分钟）
     */
    public static int getTodayUsage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // 检查是否需要重置（新的一天）
        String today = getTodayDate();
        String lastReset = prefs.getString(KEY_LAST_RESET_DATE, "");
        
        if (!today.equals(lastReset)) {
            // 新的一天，重置使用时长
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putInt(KEY_TODAY_USAGE, 0)
                .apply();
            return 0;
        }
        
        return prefs.getInt(KEY_TODAY_USAGE, 0);
    }

    /**
     * 增加使用时长
     */
    public static void addUsage(Context context, int minutes) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_TODAY_USAGE, 0);
        prefs.edit().putInt(KEY_TODAY_USAGE, current + minutes).apply();
    }

    /**
     * 重置使用时长
     */
    public static void resetUsage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putInt(KEY_TODAY_USAGE, 0)
            .putString(KEY_LAST_RESET_DATE, getTodayDate())
            .apply();
    }

    /**
     * 获取今日日期字符串
     */
    private static String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA);
        return sdf.format(new java.util.Date());
    }

    /**
     * 清除所有配置（用于卸载前或重置）
     */
    public static void clearAll(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
