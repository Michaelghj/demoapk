# GitHub Actions 自动编译 APK 指南

## 📋 步骤说明

### 1️⃣ 创建 GitHub 仓库

1. 登录 [GitHub](https://github.com/)
2. 点击右上角 **+** → **New repository**
3. 填写：
   - Repository name: `anti-addiction-demo`（或你喜欢的名字）
   - 选择 **Public**（公开）
   - 不要勾选 "Initialize this repository with a README"
4. 点击 **Create repository**

---

### 2️⃣ 上传项目代码

**方法 A：使用 Git 命令行（推荐）**

```bash
# 进入项目目录
cd anti-addiction-demo

# 初始化 git
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit"

# 添加远程仓库（替换 YOUR_USERNAME 为你的 GitHub 用户名）
git remote add origin https://github.com/YOUR_USERNAME/anti-addiction-demo.git

# 推送到 GitHub
git push -u origin main
```

**方法 B：网页上传**

1. 在 GitHub 仓库页面点击 **uploading an existing file**
2. 把整个项目文件夹拖进去
3. 点击 **Commit changes**

---

### 3️⃣ 自动编译

代码推送到 GitHub 后：

1. 进入你的 GitHub 仓库
2. 点击 **Actions** 标签
3. 你会看到 "Build Android APK" 工作流正在运行 🔄
4. 等待 10-15 分钟，看到绿色 ✅ 表示编译成功

---

### 4️⃣ 下载 APK

**方式 A：从 Actions 下载（每次编译都有）**

1. 点击 **Actions** 标签
2. 点击最近的一次运行记录
3. 在页面底部找到 **Artifacts**
4. 点击 **app-debug** 下载 APK

**方式 B：从 Releases 下载（打标签时）**

```bash
# 创建版本标签
git tag v1.0
git push origin v1.0
```

然后去 **Releases** 页面下载 APK

---

## 📱 安装到手机

1. 下载 `app-debug.apk` 到手机
2. 如果提示"未知来源"，去设置里允许安装未知应用
3. 点击安装即可

---

## ⚙️ 自定义配置

### 修改应用名称

编辑 `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">你的应用名称</string>
```

### 修改包名

1. 编辑 `app/build.gradle`:
```groovy
defaultConfig {
    applicationId "com.yourname.antiddiction"
}
```

2. 编辑 `AndroidManifest.xml` 中的包名

---

## 🔧 常见问题

### Q: 编译失败怎么办？
A: 点击 Actions 中失败的运行记录，查看错误日志

### Q: 编译太慢？
A: GitHub Actions 免费用户通常 10-15 分钟，正常现象

### Q: 想编译 Release 版本？
A: 需要配置签名，参考 GitHub 文档 "Signing Android apps"

---

## 📞 需要帮助？

有问题可以：
1. 查看 GitHub Actions 日志
2. 检查项目文件是否完整
3. 确保 Gradle 配置正确

---

**祝编译顺利！** 🎉
