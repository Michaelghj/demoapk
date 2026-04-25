package com.example.antiddiction.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.antiddiction.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * 设置 Activity
 * 功能：配置验证间隔、每日使用时长等
 */
public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etVerifyInterval;
    private TextInputEditText etDailyLimit;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etVerifyInterval = findViewById(R.id.etVerifyInterval);
        etDailyLimit = findViewById(R.id.etDailyLimit);
        btnSave = findViewById(R.id.btnSave);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettings() {
        int verifyInterval = PreferenceManager.getVerifyInterval(this);
        int dailyLimit = PreferenceManager.getDailyLimit(this);

        etVerifyInterval.setText(String.valueOf(verifyInterval));
        etDailyLimit.setText(String.valueOf(dailyLimit));
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        try {
            int verifyInterval = Integer.parseInt(etVerifyInterval.getText().toString().trim());
            int dailyLimit = Integer.parseInt(etDailyLimit.getText().toString().trim());

            // 验证输入
            if (verifyInterval < 1) {
                etVerifyInterval.setError("最少 1 分钟");
                return;
            }
            if (dailyLimit < 5) {
                etDailyLimit.setError("最少 5 分钟");
                return;
            }

            // 保存设置
            PreferenceManager.saveVerifyInterval(this, verifyInterval);
            PreferenceManager.saveDailyLimit(this, dailyLimit);

            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }
}
