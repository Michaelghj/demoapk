package com.example.antiddiction.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.antiddiction.activities.FaceVerifyActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 辅助功能监控服务
 * 功能：监控应用切换，检测是否打开限制应用
 */
public class AccessibilityMonitorService extends AccessibilityService {

    private static final String TAG = "AccessibilityMonitor";
    
    // 限制应用包名列表（实际应从存储中读取）
    private final Set<String> restrictedPackages = new HashSet<>();
    
    // 当前正在使用的应用
    @Nullable
    private String currentPackageName = null;
    
    // 验证计时器
    private Handler mainHandler;
    private Runnable verifyRunnable;
    private long lastVerifyTime = 0;
    private static final long VERIFY_INTERVAL_MS = 5 * 60 * 1000; // 5 分钟
    
    // 当前是否在限制应用中
    private boolean isInRestrictedApp = false;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        
        Log.i(TAG, "Accessibility service connected");
        
        // 初始化限制应用列表
        initRestrictedApps();
        
        // 配置服务
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT | 
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        Toast.makeText(this, "防沉迷监控已启动", Toast.LENGTH_SHORT).show();
    }

    private void initRestrictedApps() {
        // Demo 版本：添加常见的限制应用
        restrictedPackages.add("com.ss.android.ugc.aweme");      // 抖音
        restrictedPackages.add("com.smile.gifmaker");            // 快手
        restrictedPackages.add("com.tencent.tmgp.sgame");        // 王者荣耀
        restrictedPackages.add("com.tencent.tmgp.pubgmhd");      // 和平精英
        restrictedPackages.add("com.miHoYo.GenshinImpact");      // 原神
        restrictedPackages.add("com.netease.onmyoji");           // 阴阳师
        // 可以添加更多游戏和娱乐应用
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        
        int eventType = event.getEventType();
        
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                handleWindowStateChanged(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                handleWindowContentChanged(event);
                break;
        }
    }

    private void handleWindowStateChanged(AccessibilityEvent event) {
        String packageName = event.getPackageName() != null ? 
            event.getPackageName().toString() : null;
        
        if (packageName == null || packageName.equals(currentPackageName)) {
            return;
        }
        
        currentPackageName = packageName;
        Log.i(TAG, "应用切换：" + packageName);
        
        // 检查是否是限制应用
        if (restrictedPackages.contains(packageName)) {
            Log.i(TAG, "检测到限制应用：" + packageName);
            onEnterRestrictedApp(packageName);
        } else {
            onLeaveRestrictedApp();
        }
    }

    private void handleWindowContentChanged(AccessibilityEvent event) {
        // 可以在这里添加更多细粒度的监控逻辑
        // 例如检测应用内的特定 activity
    }

    private void onEnterRestrictedApp(String packageName) {
        isInRestrictedApp = true;
        
        // 启动人脸验证
        launchFaceVerification();
        
        // 启动定时验证
        startPeriodicVerification();
    }

    private void onLeaveRestrictedApp() {
        isInRestrictedApp = false;
        stopPeriodicVerification();
    }

    private void launchFaceVerification() {
        long currentTime = System.currentTimeMillis();
        
        // 如果距离上次验证时间较短，跳过验证
        if (currentTime - lastVerifyTime < VERIFY_INTERVAL_MS) {
            Log.i(TAG, "验证间隔未到，跳过");
            return;
        }
        
        mainHandler.post(() -> {
            Intent intent = new Intent(this, FaceVerifyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void startPeriodicVerification() {
        stopPeriodicVerification();
        
        verifyRunnable = new Runnable() {
            @Override
            public void run() {
                if (isInRestrictedApp) {
                    launchFaceVerification();
                }
                // 继续调度
                mainHandler.postDelayed(this, VERIFY_INTERVAL_MS);
            }
        };
        
        mainHandler.post(verifyRunnable);
        Log.i(TAG, "定时验证已启动");
    }

    private void stopPeriodicVerification() {
        if (verifyRunnable != null) {
            mainHandler.removeCallbacks(verifyRunnable);
            verifyRunnable = null;
            Log.i(TAG, "定时验证已停止");
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPeriodicVerification();
        Log.i(TAG, "Accessibility service destroyed");
    }
}
