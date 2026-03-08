# Docker Desktop 安装指南 (Windows)

由于系统权限限制，我无法直接为您安装系统级软件。请按照以下步骤手动安装 Docker Desktop，它是运行本系统容器化版本的基础。

## 1. 下载安装包
- **官方下载地址**: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
- 点击页面上的 "Download for Windows" 按钮。

## 2. 安装步骤
1.  **运行安装程序**: 双击下载的 `Docker Desktop Installer.exe`。
2.  **配置选项**:
    - 勾选 "Use WSL 2 instead of Hyper-V" (推荐，性能更好)。
    - 勾选 "Add shortcut to desktop" (可选)。
3.  **等待安装**: 点击 "OK" 开始安装，这可能需要几分钟。
4.  **完成重启**: 安装完成后，点击 "Close and restart" 重启电脑。

## 3. 首次启动配置
1.  重启后，Docker Desktop 会自动启动。如果未启动，请在开始菜单搜索并运行它。
2.  **接受协议**: 阅读并接受服务协议 (Accept Service Agreement)。
3.  **登录 (可选)**: 可以选择 "Continue without signing in" 跳过登录。
4.  **引擎启动**: 观察 Docker 界面左下角的鲸鱼图标，当背景变绿并显示 "Engine running" 时，表示准备就绪。

## 4. 验证安装
打开 PowerShell 或 CMD，运行以下命令：

```bash
docker --version
docker-compose --version
```

如果能看到版本号输出（例如 `Docker version 24.0.x`），则说明安装成功。

## 5. 常见问题
- **WSL 2 错误**: 如果提示 WSL 2 未安装，请以管理员身份打开 PowerShell 运行 `wsl --update`，然后重启。
- **虚拟化未开启**: 确保 BIOS 中开启了 Virtualization (VT-x/AMD-V)。

---

**不想安装 Docker？**
您仍然可以使用我为您准备的 **本地演示模式**，无需 Docker 即可运行系统：
1. 确保已安装 Java 17 和 Node.js 18。
2. 双击项目根目录下的 `local_demo_run.bat`。
