package com.example.antiddiction.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antiddiction.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用选择 Activity
 * 功能：从手机已安装应用列表中选择要限制的应用
 */
public class AppSelectActivity extends AppCompatActivity {

    private RecyclerView rvApps;
    private MaterialToolbar toolbar;
    private MaterialButton btnSave;
    private TextInputEditText etSearch;

    private AppAdapter appAdapter;
    private List<AppInfo> allApps = new ArrayList<>();
    private List<AppInfo> filteredApps = new ArrayList<>();
    private Set<String> selectedPackages = new HashSet<>();

    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_select);

        executor = Executors.newSingleThreadExecutor();
        initViews();
        loadApps();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvApps = findViewById(R.id.rvApps);
        btnSave = findViewById(R.id.btnSave);
        etSearch = findViewById(R.id.etSearch);

        toolbar.setNavigationOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSelection());

        // 加载已选择的应用
        selectedPackages = PreferenceManager.getRestrictedApps(this);

        // 设置搜索监听
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            filterApps(etSearch.getText().toString());
            return true;
        });

        // 设置 RecyclerView
        appAdapter = new AppAdapter(filteredApps);
        rvApps.setLayoutManager(new LinearLayoutManager(this));
        rvApps.setAdapter(appAdapter);
    }

    private void loadApps() {
        executor.execute(() -> {
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);

            List<AppInfo> appList = new ArrayList<>();
            for (ApplicationInfo app : apps) {
                // 只显示用户应用，排除系统应用
                if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    AppInfo appInfo = new AppInfo(
                        app.loadLabel(pm).toString(),
                        app.packageName,
                        app.loadIcon(pm)
                    );
                    appList.add(appInfo);
                }
            }

            // 按名称排序
            appList.sort((a, b) -> a.name.compareToIgnoreCase(b.name));

            runOnUiThread(() -> {
                allApps = appList;
                filteredApps = new ArrayList<>(allApps);
                appAdapter.notifyDataSetChanged();
            });
        });
    }

    private void filterApps(String query) {
        filteredApps.clear();
        if (query.isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            for (AppInfo app : allApps) {
                if (app.name.toLowerCase().contains(query.toLowerCase()) ||
                    app.packageName.toLowerCase().contains(query.toLowerCase())) {
                    filteredApps.add(app);
                }
            }
        }
        appAdapter.notifyDataSetChanged();
    }

    private void saveSelection() {
        PreferenceManager.saveRestrictedApps(this, selectedPackages);
        Toast.makeText(this, "已保存 " + selectedPackages.size() + " 个应用", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }

    // 应用信息类
    public static class AppInfo {
        public String name;
        public String packageName;
        public Drawable icon;

        public AppInfo(String name, String packageName, Drawable icon) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
        }
    }

    // RecyclerView 适配器
    class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        private List<AppInfo> apps;

        public AppAdapter(List<AppInfo> apps) {
            this.apps = apps;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppInfo app = apps.get(position);
            holder.bind(app);
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvName;
            CheckBox cbSelect;

            ViewHolder(View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivIcon);
                tvName = itemView.findViewById(R.id.tvName);
                cbSelect = itemView.findViewById(R.id.cbSelect);
            }

            void bind(AppInfo app) {
                ivIcon.setImageDrawable(app.icon);
                tvName.setText(app.name);
                cbSelect.setChecked(selectedPackages.contains(app.packageName));
                
                itemView.setOnClickListener(v -> {
                    if (selectedPackages.contains(app.packageName)) {
                        selectedPackages.remove(app.packageName);
                        cbSelect.setChecked(false);
                    } else {
                        selectedPackages.add(app.packageName);
                        cbSelect.setChecked(true);
                    }
                });
            }
        }
    }
}
