#!/bin/bash

# Nexus-Lite 知识预览系统 - 完整打包脚本
# 用于将前后端项目打包成一个可部署的 JAR 文件

set -e

echo "========================================"
echo "Nexus-Lite 知识预览系统打包脚本"
echo "========================================"

# 1. 编译 React 前端
echo "\n1. 编译 React 前端..."
cd frontend
npm install
npm run build
cd ..

# 2. 复制前端构建文件到 Spring Boot 静态目录
echo "\n2. 复制前端构建文件..."
rm -rf backend/src/main/resources/static/*
cp -r frontend/dist/* backend/src/main/resources/static/

# 3. 使用 Maven 打包 Spring Boot 应用
echo "\n3. 打包 Spring Boot 应用..."
cd backend
mvn clean package -DskipTests
cd ..

# 4. 运行测试套件（可选，默认跳过）
if [ "$1" == "--run-tests" ]; then
    echo "\n4. 运行测试套件..."
    mvn test
fi

echo "\n========================================"
echo "打包完成！"
echo "最终 JAR 文件：backend/target/nexus-lite-1.0.0-SNAPSHOT.jar"
echo "\n运行命令："
echo "  java -jar backend/target/nexus-lite-1.0.0-SNAPSHOT.jar"
echo "\n带自定义配置运行："
echo "  java -jar backend/target/nexus-lite-1.0.0-SNAPSHOT.jar --app.scan.root-dirs=/custom/path"
echo "========================================"
