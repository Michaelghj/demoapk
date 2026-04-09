# 快速编译指南

## 方法一：Android Studio（推荐）

1. **打开 Android Studio**

2. **打开项目**
   - File → Open
   - 选择 `anti-addiction-demo` 文件夹

3. **等待 Gradle 同步完成**
   - 首次打开需要下载依赖，可能需要几分钟

4. **运行应用**
   - 连接 Android 手机或启动模拟器
   - 点击绿色运行按钮 ▶️

5. **生成 APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

## 方法二：命令行编译

```bash
# 进入项目目录
cd anti-addiction-demo

# 如果有 gradlew
./gradlew assembleDebug

# 或使用系统 gradle
gradle assembleDebug

# APK 输出位置
ls -lh app/build/outputs/apk/debug/
```

## 遇到问题？

### Gradle 同步失败
```bash
# 清理项目
./gradlew clean

# 重新同步
./gradlew build --refresh-dependencies
```

### 缺少 SDK
- 打开 Android Studio
- Tools → SDK Manager
- 安装 Android SDK 33

### 证书问题（Release 版本）
需要创建签名证书：
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
```

## 安装到手机

1. **启用开发者选项**
   - 设置 → 关于手机 → 连续点击"版本号"7 次

2. **启用 USB 调试**
   - 设置 → 开发者选项 → USB 调试

3. **连接手机**
   - USB 连接电脑
   - 授权 USB 调试

4. **安装 APK**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 必需权限

安装后首次使用需要授予：
- ✅ 相机权限（人脸验证）
- ✅ 辅助功能权限（应用监控）
- ✅ 通知权限（运行状态）

---

**提示**: 建议使用真机测试，模拟器相机功能可能有限。
