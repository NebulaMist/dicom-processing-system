# GitHub 上传指南

本指南将帮助您将DICOM处理系统项目上传到GitHub。

## 前置条件

1. **安装Git**
   - Windows: 下载 [Git for Windows](https://git-scm.com/download/win)
   - macOS: 使用 Homebrew `brew install git`
   - Linux: 使用包管理器 `sudo apt install git` 或 `sudo yum install git`

2. **GitHub账户**
   - 在 [GitHub](https://github.com) 注册账户
   - 配置SSH密钥或使用Personal Access Token

## 步骤1: 初始化Git仓库

在项目根目录打开终端/命令提示符：

```bash
# 初始化Git仓库
git init

# 添加所有文件到暂存区
git add .

# 提交初始版本
git commit -m "feat: initial commit - DICOM Processing System v7.0"
```

## 步骤2: 在GitHub创建仓库

1. 登录GitHub
2. 点击右上角的 "+" 按钮，选择 "New repository"
3. 填写仓库信息：
   - **Repository name**: `dicom-processing-system`
   - **Description**: `Java-based DICOM medical imaging data processing system with complete VR encoding, SQ sequence support, and file I/O capabilities`
   - **Visibility**: Public (推荐) 或 Private
   - **不要**初始化README.md、.gitignore或LICENSE（我们已经创建了）

4. 点击 "Create repository"

## 步骤3: 连接本地仓库到GitHub

复制GitHub提供的仓库URL，然后执行：

```bash
# 添加远程仓库 (替换YOUR_USERNAME为您的GitHub用户名)
git remote add origin https://github.com/YOUR_USERNAME/dicom-processing-system.git

# 推送到GitHub
git branch -M main
git push -u origin main
```

## 步骤4: 验证上传

1. 刷新GitHub仓库页面
2. 确认所有文件已上传
3. 检查CI/CD是否自动运行（查看Actions标签）

## 步骤5: 设置仓库

### 启用Issues和Discussions
1. 进入仓库的 "Settings" 页面
2. 滚动到 "Features" 部分
3. 确保 "Issues" 和 "Discussions" 已启用

### 设置分支保护
1. 在 "Settings" -> "Branches"
2. 点击 "Add rule"
3. 输入分支名称: `main`
4. 启用以下选项：
   - Require a pull request before merging
   - Require status checks to pass before merging
   - Restrict pushes that create files larger than 100 MB

### 添加Topics标签
在仓库主页面，点击设置图标添加topics：
- `dicom`
- `medical-imaging`
- `java`
- `maven`
- `healthcare`
- `imaging-processing`
- `vr-encoding`
- `sq-sequence`

## 步骤6: 创建Release

```bash
# 创建标签
git tag -a v7.0.0 -m "Release v7.0.0: Complete SQ sequence and file I/O support"

# 推送标签
git push origin v7.0.0
```

然后在GitHub上：
1. 进入 "Releases" 页面
2. 点击 "Create a new release"
3. 选择标签 `v7.0.0`
4. 填写发布说明
5. 上传编译好的JAR文件（可选）
6. 点击 "Publish release"

## 后续维护

### 日常开发流程
```bash
# 拉取最新代码
git pull origin main

# 创建功能分支
git checkout -b feature/new-feature

# 进行开发...
# 提交变更
git add .
git commit -m "feat: add new feature"

# 推送分支
git push origin feature/new-feature

# 在GitHub创建Pull Request
```

### 保持仓库同步
```bash
# 定期更新主分支
git checkout main
git pull origin main

# 删除已合并的分支
git branch -d feature/old-feature
git push origin --delete feature/old-feature
```

## 自动化检查

项目已配置GitHub Actions，每次推送都会自动：
- 运行所有测试
- 检查代码编译
- 生成测试覆盖率报告
- 构建JAR文件

## 故障排除

### 常见问题

1. **推送被拒绝**
   ```bash
   git pull origin main --rebase
   git push origin main
   ```

2. **文件过大**
   - 检查.gitignore是否正确配置
   - 移除大文件：`git rm --cached large-file.dcm`

3. **权限问题**
   - 确保SSH密钥配置正确
   - 或使用Personal Access Token

### 联系支持
如果遇到问题，可以：
- 查看GitHub文档
- 在项目中创建Issue
- 联系项目维护者

---

**恭喜！您的DICOM处理系统项目现在已经在GitHub上了！** 🎉
