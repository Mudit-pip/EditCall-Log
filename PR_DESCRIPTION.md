## Title

feat: 秒级精度、表盘选择器、24小时制、GitHub Actions 自动构建

## Description

### Summary

对通话记录编辑器的 UI 和构建流程进行了全面改进：

1. **通话时长支持秒级精度** — 双 NumberPicker（分钟 + 秒）
2. **日期/时间改用 Material 表盘选择器** — 移除 Holo 主题滚动条，改为时钟表盘和日历视图
3. **时间改为 24 小时制** — 去除 AM/PM，直接显示 HH:mm
4. **通话时长选择器改为表盘** — 用 TimePicker 时钟模式替代 NumberPicker 滚动条，初始指向 00:00（12 点钟方向）
5. **GitHub Actions 自动构建 APK** — push 和 tag 时自动构建，上传 Artifact，创建 Release
6. **Gradle 缓存重定向到项目目录** — 不污染系统 `~/.gradle`

### Changes

| Commit | Description |
|--------|-------------|
| `1603299` | Add second-level duration precision and Material Design clock face pickers |
| `a5bb628` | Add GitHub Actions workflow for automated APK builds |
| `046ebcc` | Update README with new features and build instructions |
| `a958822` | Redirect Gradle cache to project directory to avoid system pollution |
| `d0d0d5c` | Remove stale APK files from project root and Downloads folder |
| `e1edd72` | Switch time picker to 24-hour format and replace duration scroll wheels with clock face dial |
| `6c0a349` | Duration clock dial starts at 00:00 (12 o'clock top position) |

### UI Changes

- **时间选择**: 打开时显示 Material 时钟表盘，24 小时制，按钮显示 `13:30` 格式
- **日期选择**: 打开时显示 Material 日历视图，无滚动条
- **时长选择**: 打开时显示时钟表盘，指针指向 00:00；小时环设为分钟（0-23），分钟环设为秒（0-59）；底部 ±15m 快调按钮支持长时长；实时显示 `Total: X min Y sec`
- **按钮文本**: 时长有秒时显示 `5 min 30 sec`，无秒时显示 `5 minutes`

### Build

- **本地构建**: `./gradlew assembleDebug` — APK 在 `app/build/outputs/apk/debug/app-debug.apk`
- **CI 构建**: 推送到 `main` 或 `v*` tag 时自动构建（`.github/workflows/build-apk.yml`）
- **Gradle 缓存**: 重定向到 `.gradle-home/`，不写系统 `~/.gradle`
- **Android SDK**: 项目内 `android-sdk/`，已加入 `.gitignore`，不跟踪

### Notes

- Android API 33+ 对 `WRITE_CALL_LOG` 权限做了严格限制，修改已有通话记录暂不支持
