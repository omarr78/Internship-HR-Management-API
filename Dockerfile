FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY build/libs/HR-Management-0.0.1-SNAPSHOT.jar HR-Management.jar
EXPOSE 8080
LABEL org.opencontainers.image.source="https://github.com/omarr78/Internship-HR-Management-API"
CMD ["java", "-jar", "HR-Management.jar"]