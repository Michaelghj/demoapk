# 防沉迷助手 Demo - Android 应用

一个青少年手机防沉迷应用的演示版本，通过人脸识别验证来限制特定应用的使用。

## 📱 功能特性

### 核心功能
- ✅ **应用黑名单管理** - 设置需要限制的应用（抖音、游戏等）
- ✅ **人脸识别验证** - 打开限制应用前需人脸验证
- ✅ **定时验证** - 使用期间每 5 分钟重新验证
- ✅ **辅助功能监控** - 实时监控应用切换
- ✅ **前台服务** - 保持应用持续运行

### 技术亮点
- Google ML Kit 人脸检测（离线验证，保护隐私）
- Android CameraX 相机 API
- AccessibilityService 应用监控
- Material Design 3 UI

## 🛠️ 编译说明

### 环境要求
- Android Studio Arctic Fox (2020.3.1) 或更高版本
- JDK 11 或更高版本
- Android SDK 33
- 最低支持 Android 7.0 (API 24)

### 编译步骤

1. **打开项目**
   ```bash
   # 在 Android Studio 中打开 anti-addiction-demo 文件夹
   ```

2. **同步 Gradle**
   - 打开项目后，Android Studio 会自动同步 Gradle
   - 或点击 "Sync Project with Gradle Files"

3. **连接设备/模拟器**
   - 连接 Android 真机（推荐）
   - 或启动 Android 模拟器

4. **运行应用**
   - 点击 Run 按钮 (▶️)
   - 或使用快捷键 Shift+F10

5. **生成 APK**
   ```bash
   # Debug APK
   ./gradlew assembleDebug
   
   # Release APK
   ./gradlew assembleRelease
   
   # APK 输出位置：
   # app/build/outputs/apk/debug/app-debug.apk
   # app/build/outputs/apk/release/app-release.apk
   ```

## 📋 使用说明

### 首次使用

1. **启动应用** - 打开"防沉迷助手"

2. **注册人脸**
   - 点击"注册人脸"按钮
   - 授予相机权限
   - 面向摄像头完成人脸录入

3. **添加限制应用**
   - 点击"添加限制应用"
   - 从列表中选择要限制的应用
   - 默认已添加：抖音、王者荣耀

4. **启动监控**
   - 点击"启动监控"按钮
   - 授予辅助功能权限（重要！）
   - 服务启动后状态变为"监控中"

### 使用流程

```
打开限制应用 → 人脸验证 → 使用 5 分钟 → 再次验证 → 继续使用
```

### 权限说明

| 权限 | 用途 | 必需 |
|------|------|------|
| 相机 | 人脸验证 | ✅ |
| 辅助功能 | 监控应用切换 | ✅ |
| 前台服务 | 保持后台运行 | ✅ |
| 通知 | 显示运行状态 | ✅ |

## 📁 项目结构

```
anti-addiction-demo/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/antiddiction/
│   │   │   ├── activities/
│   │   │   │   ├── MainActivity.java          # 主界面
│   │   │   │   └── FaceVerifyActivity.java    # 人脸验证界面
│   │   │   └── services/
│   │   │       ├── AccessibilityMonitorService.java  # 辅助功能服务
│   │   │       └── MonitorForegroundService.java     # 前台服务
│   │   ├── res/
│   │   │   ├── layout/           # 布局文件
│   │   │   ├── values/           # 资源值
│   │   │   ├── drawable/         # 图形资源
│   │   │   └── xml/              # 配置文件
│   │   └── AndroidManifest.xml   # 应用清单
│   └── build.gradle              # 应用构建配置
├── build.gradle                  # 项目构建配置
├── settings.gradle               # 项目设置
└── README.md                     # 说明文档
```

## ⚠️ 注意事项

### Demo 限制
这是演示版本，以下功能需要完善：

1. **人脸注册** - 当前版本仅做演示，未实现真正的人脸特征存储和比对
2. **应用列表** - 限制应用列表未持久化存储
3. **绕过防护** - 未实现防绕过机制（如检测卸载、检测安全模式等）
4. **家长控制** - 未实现远程管理和使用报告

### 兼容性
- 不同品牌 Android 手机可能有不同的权限管理策略
- 部分厂商定制系统可能限制辅助功能使用
- 建议在原生 Android 或接近原生的系统上测试

### 隐私安全
- 人脸数据仅在本地处理，不上传云端
- 实际产品需要完善的隐私政策和用户协议
- 需符合《个人信息保护法》和《儿童个人信息网络保护规定》

## 🚀 后续优化建议

1. **完善人脸验证**
   - 实现真正的人脸特征提取和比对
   - 添加活体检测防止照片绕过
   - 支持多人脸注册（父母 + 孩子）

2. **增强防护**
   - 检测应用卸载尝试
   - 检测安全模式/开发者选项
   - 隐藏应用图标防止关闭

3. **家长控制端**
   - 独立家长管理 APP
   - 远程查看使用记录
   - 远程配置限制策略
   - 推送异常通知

4. **智能策略**
   - 学习时段自动禁用
   - 完成任务奖励使用时间
   - 使用报告和分析

## 📄 许可证

本项目仅供学习和演示使用。

## 📞 问题反馈

如有问题，请检查：
1. Gradle 同步是否成功
2. 是否授予所有必需权限
3. Logcat 中的错误日志

---

**编译时间**: 2026 年 4 月  
**版本**: 1.0 Demo


> Updated: 2026-04-09 22:27:26
