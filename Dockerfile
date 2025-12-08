FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/HR-Management-0.0.1-SNAPSHOT.jar /app/HR-Management.jar
EXPOSE 8080
CMD ["java", "-jar", "HR-Management.jar"]