@echo off
chcp 65001 >nul
echo ========================================
echo PDF转电子书 APK 自动构建脚本
echo ========================================
echo.

REM 设置环境变量
set "JAVA_HOME=D:\软件安装\JDK17"
set "ANDROID_HOME=D:\软件安装\AndroidSDK"
set "ANDROID_SDK_ROOT=D:\软件安装\AndroidSDK"
set "PATH=D:\软件安装\JDK17\bin;D:\软件安装\gradle-8.2\bin;D:\软件安装\AndroidSDK\platform-tools;D:\软件安装\AndroidSDK\build-tools\34.0.0;%PATH%"

REM 进入项目目录
cd /d "D:\软件安装\DuMate\工作区\PDFToEBook"

echo [1/4] 清理构建缓存...
gradle clean --no-daemon

echo.
echo [2/4] 检查代码质量...
gradle check --no-daemon

echo.
echo [3/4] 构建Debug APK...
gradle assembleDebug --no-daemon

echo.
echo [4/4] 构建Release APK...
gradle assembleRelease --no-daemon

echo.
echo ========================================
echo 构建完成！
echo ========================================
echo.
echo Debug APK位置:
echo   app\build\outputs\apk\debug\app-debug.apk
echo.
echo Release APK位置:
echo   app\build\outputs\apk\release\app-release.apk
echo.
pause
