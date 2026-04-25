package com.example.antiddiction.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antiddiction.R;
import com.example.antiddiction.services.AccessibilityMonitorService;
import com.example.antiddiction.services.MonitorForegroundService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面 Activity
 * 功能：启动/停止监控、注册人脸、管理限制应用列表
 */
public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardStatus;
    private MaterialTextView tvStatus;
    private MaterialButton btnToggleMonitor;
    private MaterialButton btnFaceRegister;
    private MaterialButton btnAddApp;
    private RecyclerView rvRestrictedApps;

    private boolean isMonitoring = false;
    private List<String> restrictedApps = new ArrayList<>();

    // 权限请求启动器
    private final ActivityResultLauncher<String> cameraPermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                startActivity(new Intent(this, FaceVerifyActivity.class));
            } else {
                Toast.makeText(this, R.string.permission_camera_required, Toast.LENGTH_LONG).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        loadRestrictedApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void initViews() {
        cardStatus = findViewById(R.id.cardStatus);
        tvStatus = findViewById(R.id.tvStatus);
        btnToggleMonitor = findViewById(R.id.btnToggleMonitor);
        btnFaceRegister = findViewById(R.id.btnFaceRegister);
        btnAddApp = findViewById(R.id.btnAddApp);
        rvRestrictedApps = findViewById(R.id.rvRestrictedApps);

        rvRestrictedApps.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        btnToggleMonitor.setOnClickListener(v -> toggleMonitoring());
        btnFaceRegister.setOnClickListener(v -> requestCameraPermission());
        btnAddApp.setOnClickListener(v -> showAddAppDialog());
    }

    private void toggleMonitoring() {
        if (isMonitoring) {
            stopMonitoring();
        } else {
            startMonitoring();
        }
    }

    private void startMonitoring() {
        // 检查辅助功能权限
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityPermissionDialog();
            return;
        }

        // 启动前台服务
        Intent serviceIntent = new Intent(this, MonitorForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        isMonitoring = true;
        updateStatus();
        Toast.makeText(this, "监控已启动", Toast.LENGTH_SHORT).show();
    }

    private void stopMonitoring() {
        Intent serviceIntent = new Intent(this, MonitorForegroundService.class);
        stopService(serviceIntent);

        isMonitoring = false;
        updateStatus();
        Toast.makeText(this, "监控已停止", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus() {
        if (isMonitoring) {
            tvStatus.setText(R.string.status_monitoring);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
            btnToggleMonitor.setText(R.string.btn_stop_monitor);
        } else {
            tvStatus.setText(R.string.status_stopped);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
            btnToggleMonitor.setText(R.string.btn_start_monitor);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                return settingValue.contains(getPackageName() + "/" + 
                    AccessibilityMonitorService.class.getCanonicalName());
            }
        }

        return false;
    }

    private void showAccessibilityPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("需要开启辅助功能")
            .setMessage(R.string.permission_accessibility_required)
            .setPositiveButton("去设置", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, FaceVerifyActivity.class));
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showAddAppDialog() {
        // Demo 版本：添加预设的热门应用
        String[] popularApps = {
            "抖音 - com.ss.android.ugc.aweme",
            "快手 - com.smile.gifmaker",
            "王者荣耀 - com.tencent.tmgp.sgame",
            "和平精英 - com.tencent.tmgp.pubgmhd",
            "原神 - com.miHoYo.GenshinImpact",
            "微信 - com.tencent.mm",
            "QQ - com.tencent.mobileqq"
        };

        new AlertDialog.Builder(this)
            .setTitle("添加限制应用")
            .setItems(popularApps, (dialog, which) -> {
                String[] parts = popularApps[which].split(" - ");
                if (parts.length == 2) {
                    String packageName = parts[1].trim();
                    if (!restrictedApps.contains(packageName)) {
                        restrictedApps.add(packageName);
                        saveRestrictedApps();
                        Toast.makeText(this, "已添加：" + parts[0], Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "该应用已在列表中", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .show();
    }

    private void loadRestrictedApps() {
        // Demo 版本：加载默认应用
        restrictedApps.add("com.ss.android.ugc.aweme"); // 抖音
        restrictedApps.add("com.tencent.tmgp.sgame");   // 王者荣耀
        saveRestrictedApps();
    }

    private void saveRestrictedApps() {
        // Demo 版本：仅保存到内存，实际应保存到 SharedPreferences 或数据库
        // 这里简单刷新列表显示
    }
}
