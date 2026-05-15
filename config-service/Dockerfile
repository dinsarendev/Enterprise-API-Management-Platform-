FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

COPY target/config-service-*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring
EXPOSE 8888

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
