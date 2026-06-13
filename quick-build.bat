@echo off
chcp 65001 >nul
echo ========================================
echo PDF转电子书 APK 构建脚本
echo ========================================
echo.

REM 设置环境变量
set "JAVA_HOME=D:\软件安装\JDK17"
set "ANDROID_HOME=D:\软件安装\AndroidSDK"
set "ANDROID_SDK_ROOT=D:\软件安装\AndroidSDK"
set "PATH=D:\软件安装\JDK17\bin;D:\软件安装\gradle-8.2\bin;D:\软件安装\AndroidSDK\platform-tools;D:\软件安装\AndroidSDK\build-tools\34.0.0;%PATH%"

REM 切换到项目目录
cd /d "D:\软件安装\DuMate\工作区\PDFToEBook"

echo 当前目录: %CD%
echo.

echo [步骤1/4] 停止Gradle守护进程...
call gradle --stop
echo.

echo [步骤2/4] 清理构建缓存...
call gradle clean --no-daemon --no-build-cache
echo.

echo [步骤3/4] 构建Debug APK...
call gradle assembleDebug --no-daemon --no-build-cache
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 构建成功！
    echo ========================================
    echo.
    echo Debug APK位置:
    echo   %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    REM 复制APK到更易访问的位置
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        copy "app\build\outputs\apk\debug\app-debug.apk" "C:\temp\PDFToEBook_Build\app-debug.apk" /Y
        echo APK已复制到:
        echo   C:\temp\PDFToEBook_Build\app-debug.apk
        echo.
    )
) else (
    echo.
    echo ========================================
    echo 构建失败！错误代码: %ERRORLEVEL%
    echo ========================================
    echo.
)

pause
