FROM eclipse-temurin:21-jre
WORKDIR /app
# O * garante que pegue o jar independente da versão (0.0.1-SNAPSHOT)
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]