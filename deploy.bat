@echo off
setlocal enabledelayedexpansion

:: DICOM Processing System Windows 部署脚本
:: 用于快速设置开发环境和运行测试

echo === DICOM Processing System 部署脚本 ===
echo.

:: 检查Java版本
echo 检查Java环境...
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo ✗ 未找到Java，请安装Java 11或更高版本
    pause
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr version') do (
    set JAVA_VERSION=%%i
    set JAVA_VERSION=!JAVA_VERSION:"=!
)
echo ✓ Java版本: !JAVA_VERSION!

:: 检查Maven
echo 检查Maven环境...
mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    echo ✗ 未找到Maven，请安装Maven 3.6或更高版本
    pause
    exit /b 1
)

for /f "tokens=3" %%i in ('mvn -version ^| findstr "Apache Maven"') do (
    set MVN_VERSION=%%i
)
echo ✓ Maven版本: !MVN_VERSION!
echo.

:: 清理并编译
echo 清理并编译项目...
call mvn clean compile
if !errorlevel! neq 0 (
    echo ✗ 编译失败
    pause
    exit /b 1
)
echo ✓ 编译完成
echo.

:: 运行核心测试
echo 运行核心功能测试...

echo 1. 测试DICOM数据字典...
call mvn exec:java -Dexec.mainClass="com.dicom.dictionary.SimpleDicomTest" -q

echo 2. 测试VR工厂模式...
call mvn exec:java -Dexec.mainClass="com.dicom.vr.VRFactoryTest" -q

echo 3. 测试SQ序列编码...
call mvn exec:java -Dexec.mainClass="com.dicom.data.SQEncodeTest" -q

echo 4. 测试定长vs未定长编码差异...
call mvn exec:java -Dexec.mainClass="com.dicom.data.RealLengthDifferenceTest" -q

echo ✓ 核心测试完成
echo.

:: 运行完整测试套件
echo 运行完整测试套件...
call mvn test
if !errorlevel! neq 0 (
    echo ⚠️ 某些测试失败，请检查日志
) else (
    echo ✓ 所有测试通过
)
echo.

:: 生成文档
echo 生成项目文档...
call mvn javadoc:javadoc
echo ✓ 文档生成完成
echo.

:: 打包项目
echo 打包项目...
call mvn package -DskipTests
echo ✓ 项目打包完成
echo.

echo === 部署完成 ===
echo 项目已准备就绪，可以开始使用！
echo.
echo 快速开始：
echo 1. 运行DICOM图像查看器: mvn exec:java -Dexec.mainClass="com.dicom.gui.ImageFrame"
echo 2. 运行SQ序列测试GUI: mvn exec:java -Dexec.mainClass="com.dicom.data.SQTestProgram"
echo 3. 查看生成的文档: target\site\apidocs\index.html
echo 4. 查看打包结果: target\*.jar
echo.

pause
