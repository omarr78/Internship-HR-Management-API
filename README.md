# Internship HR Management API

[![Build Status](https://github.com/omarr78/Internship-HR-Management-API/actions/workflows/gradle.yml)](https://github.com/omarr78/Internship-HR-Management-API/actions/workflows/gradle.yml))
[![Docker Image](https://img.shields.io/badge/docker-ghcr.io-blue)](https://ghcr.io)

A Spring Boot REST API for managing employees, teams, departments and reporting structure. This repository uses Gradle,
JDK 21, Flyway for DB migrations, and GitHub Actions + Docker for CI/CD.

## Features

- REST endpoints for employee management
- Unit and Integration tests with predefined test data
- Database migrations using Flyway
- Checkstyle enforcement and CI via GitHub Actions
- Dockerfile and CI publishing to GitHub Container Registry (GHCR)

---

## Requirements

- Java 21 (Temurin/OpenJDK)
- Gradle (wrapper provided: use `./gradlew`)
- Docker (to build and run images)
- A relational database (MySQL) for production; tests use an H2 DB

---

## Quick Start

### **1. Clone the project and run locally**

1. Clone the repository:

```bash
git clone https://github.com/omarr78/Internship-HR-Management-API.git
cd <repo-folder>
```

2. Copy the example environment file to create your own `.env`:

```bash
cp .env.example .env
```

3. Edit `.env` and fill in the correct values for your setup:

```text
DB_URL=localhost
DB_PORT=3306
DB_NAME=hr_db
DB_USERNAME=root
DB_PASSWORD=12345
```

4. Start required services (like MySQL) using Docker Compose:

```bash
docker-compose up -d
```

5. Build the application with Gradle:

```bash
./gradlew clean build --info
```

6. Run tests (optional):

```bash
./gradlew test --info
./gradlew integrationTest --info
```

7. Run the application locally:

* Using Gradle bootRun:

```bash
./gradlew bootRun
# Then open http://localhost:8080
```

* Or run the generated JAR:

```bash
java -jar build/libs/*-SNAPSHOT.jar
```

### **2. Pull and run the Docker image from GHCR**

1. Pull the image:

```bash
docker pull ghcr.io/omarr78/internship-hr-management-api:<tag>
```

2. Run the container and pass database credentials (replace `<tag>` with the image tag):

### **Option 1 — Connect to MySQL on Your Local Machine**

If MySQL is installed locally, run the container with your database credentials:

```bash
sudo docker run \
  --add-host=host.docker.internal:host-gateway \
  -p 8080:8080 \
  -e DB_URL=host.docker.internal \
  -e DB_PORT=3306 \
  -e DB_NAME=hr_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=12345 \
  ghcr.io/omarr78/internship-hr-management-api:<tag>
```

### **Option 2 — Run MySQL in a Docker Container**

Spin up MySQL in Docker so your Spring Boot app can connect container-to-container:

```bash
docker run -d --name mysql-db \
  -e MYSQL_ROOT_PASSWORD=12345 \
  -e MYSQL_DATABASE=hr_db \
  -p 3306:3306 \
  mysql:8
```

Then run the HR Management API:

```bash
docker run -p 8080:8080 \
  -e DB_URL=mysql-db \
  -e DB_PORT=3306 \
  -e DB_NAME=hr_db \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=12345 \
  --link mysql-db:mysql-db \
  ghcr.io/omarr78/internship-hr-management-api:sha-ef574b8
```

---

## Running tests

- Unit tests: `./gradlew test`
- Integration tests: `./gradlew integrationTest` (CI runs integration tests after successful unit tests)
- Test fixtures and DBUnit datasets are located in `src/test/resources/dataset/`.

---

## Continuous Integration (GitHub Actions)

The workflow `.github/workflows/gradle.yml` performs the following on pushes and PRs:

- Checkout code and set up JDK 21
- Run Checkstyle: `checkstyleMain`, `checkstyleTest`, `checkstyleIntegration` (job fails on violations)
- Build and run tests (`./gradlew clean build`)
- Run unit and integration tests explicitly
- Publish Checkstyle and test reports as workflow artifacts
- Build a Docker image and push to GitHub Container Registry (GHCR) with a datetime tag
- On `main` branch merges the workflow builds and pushes an image tagged with the short commit SHA

Artifacts and reports can be downloaded from the workflow run page under `Artifacts`.

---

## Configuration

- Application configuration lives in `src/main/resources/application.yml` and `src/test/resources/application.yml` for
  tests.
- Use environment variables (SPRING_*) or profiles to override DB and other runtime settings.

---

## Database & Migrations

- Flyway migration scripts are placed under `src/main/resources/db/migration`.
- Tests include dataset XML fixtures under `src/test/resources/dataset/`.

---

## Development notes

- Recommended IDE: IntelliJ IDEA (Project is already configured for Gradle)
- Follow Checkstyle rules: run `./gradlew checkstyleMain checkstyleTest checkstyleIntegration` locally before push
- Use the Gradle wrapper (`./gradlew`) to ensure consistent Gradle version

---