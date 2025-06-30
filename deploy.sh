#!/bin/bash

# DICOM Processing System 部署脚本
# 用于快速设置开发环境和运行测试

set -e

echo "=== DICOM Processing System 部署脚本 ==="
echo

# 检查Java版本
echo "检查Java环境..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "✓ Java版本: $JAVA_VERSION"
else
    echo "✗ 未找到Java，请安装Java 11或更高版本"
    exit 1
fi

# 检查Maven
echo "检查Maven环境..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    echo "✓ Maven版本: $MVN_VERSION"
else
    echo "✗ 未找到Maven，请安装Maven 3.6或更高版本"
    exit 1
fi

echo

# 清理并编译
echo "清理并编译项目..."
mvn clean compile
echo "✓ 编译完成"
echo

# 运行核心测试
echo "运行核心功能测试..."

echo "1. 测试DICOM数据字典..."
mvn exec:java -Dexec.mainClass="com.dicom.dictionary.SimpleDicomTest" -q

echo "2. 测试VR工厂模式..."
mvn exec:java -Dexec.mainClass="com.dicom.vr.VRFactoryTest" -q

echo "3. 测试SQ序列编码..."
mvn exec:java -Dexec.mainClass="com.dicom.data.SQEncodeTest" -q

echo "4. 测试定长vs未定长编码差异..."
mvn exec:java -Dexec.mainClass="com.dicom.data.RealLengthDifferenceTest" -q

echo "✓ 核心测试完成"
echo

# 运行完整测试套件
echo "运行完整测试套件..."
mvn test
echo "✓ 所有测试通过"
echo

# 生成文档
echo "生成项目文档..."
mvn javadoc:javadoc
echo "✓ 文档生成完成"
echo

# 打包项目
echo "打包项目..."
mvn package -DskipTests
echo "✓ 项目打包完成"
echo

echo "=== 部署完成 ==="
echo "项目已准备就绪，可以开始使用！"
echo
echo "快速开始："
echo "1. 运行DICOM图像查看器: mvn exec:java -Dexec.mainClass=\"com.dicom.gui.ImageFrame\""
echo "2. 运行SQ序列测试GUI: mvn exec:java -Dexec.mainClass=\"com.dicom.data.SQTestProgram\""
echo "3. 查看生成的文档: target/site/apidocs/index.html"
echo "4. 查看打包结果: target/*.jar"
