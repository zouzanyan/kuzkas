# 目前找到的最小的 jre8 镜像
FROM openjdk:8u151-jre-alpine3.7
# 作者名称【可选项】
MAINTAINER zouzanyan
# 本地jar包 添加到 容器内jar包
COPY kuzkas-1.0-SNAPSHOT.jar /kuzkas/kuzkas-1.0-SNAPSHOT.jar
# 配置文件
COPY config.toml /kuzkas/config.toml
# 对外暴露的端口【可选项】（提示作用）
EXPOSE 7508
# 启动容器时执行的命令【入口点】
ENTRYPOINT ["sh", "-c", "cd /kuzkas && java -jar /kuzkas/kuzkas-1.0-SNAPSHOT.jar"]