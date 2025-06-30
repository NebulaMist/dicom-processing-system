# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 安装Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY pom.xml .
COPY src ./src
COPY README.md .
COPY LICENSE .

# 编译项目
RUN mvn clean compile

# 运行测试
RUN mvn test

# 打包项目
RUN mvn package -DskipTests

# 设置环境变量
ENV JAVA_OPTS="-Xmx2g -Xms1g"

# 暴露端口（如果有网络功能）
EXPOSE 8080

# 创建非root用户
RUN groupadd -r dicom && useradd -r -g dicom dicom
RUN chown -R dicom:dicom /app
USER dicom

# 默认命令 - 运行测试程序
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.dicom.dictionary.SimpleDicomTest"]
