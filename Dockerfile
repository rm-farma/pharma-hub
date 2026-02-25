FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o uber-jar gerado pelo Maven com quarkus.package.jar.type=uber-jar
COPY target/*-runner.jar /app/application.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]

