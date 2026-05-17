# **Jenkins CI/CD Pipeline for Spring Boot with Docker**

A declarative Jenkins pipeline that builds, containerizes, and publishes a Spring Boot REST application to Docker Hub — fully automated from source commit to registry push.

---

## Project at a Glance

| | |
|---|---|
| **Tools Used** | Jenkins, Docker, Maven, Spring Boot, JUnit |
| **Platform** | Any Jenkins server with Docker and Maven installed |
| **Languages** | Java 8, Groovy (Jenkinsfile) |
| **What It Does** | Compiles a Spring Boot app, builds a Docker image, validates it at runtime, and pushes versioned and latest tags to Docker Hub |

---

## The Problem This Project Solves

In many development teams, the path from a code change to a running Docker image involves a chain of manual steps: run the build, check the tests, build the image locally, tag it, log in to a registry, and push. Each of those steps is error-prone when done by hand. A developer who skips the test run, pushes an untagged image, or forgets to bump a version introduces risk that accumulates silently over time.

This project eliminates that chain. A single Jenkinsfile defines the complete delivery path: Maven compiles the application and runs the test suite, Docker builds the image and validates it is reachable at port 8080, and then the image is pushed to Docker Hub under both a build-number tag and the `latest` tag. No human intervention is required between `git push` and a published, versioned image.

The pipeline also enforces non-root execution inside the container. The Dockerfile creates a dedicated `myuser` account and switches to it before starting the JVM, reducing the attack surface of the running container. This combination of automated testing, deterministic image versioning, and secure container configuration reflects production-grade CI/CD practice rather than a toy pipeline.

---

## Architecture

```
Developer pushes code
        |
        v
+-------------------+
|   Jenkins Master  |
+-------------------+
        |
        |-- Stage 1: Build
        |     mvn clean install
        |     (compiles source, runs JUnit tests)
        |
        |-- Stage 2: Build Docker Image
        |     docker build -> shanemannion/spring-boot-app
        |     container smoke test: curl localhost:8080
        |
        |-- Stage 3: Push Docker Image
              docker push :<BUILD_NUMBER>
              docker push :latest
              (authenticates via Jenkins credentials store)
                  |
                  v
          +------------------+
          |  Docker Hub      |
          |  Registry        |
          +------------------+
```

---

## Repository Structure

```
jenkins-ci-cd-springboot-docker/
├── Jenkinsfile                          # Declarative pipeline definition
├── Dockerfile                          # Container image build instructions
├── pom.xml                             # Maven project descriptor (Spring Boot 2.6.3, Java 8)
└── src/
    ├── main/
    │   ├── java/com/example/springboot/
    │   │   ├── Application.java        # Spring Boot entry point
    │   │   └── HelloController.java    # REST controller (GET /, GET /DevOps, POST /DevOps, POST /YorkU)
    │   └── resources/
    │       └── application.properties  # Application configuration
    └── test/
        └── java/
            ├── SampleTest.java         # JUnit: null-check assertion
            └── TestSample.java         # JUnit: arithmetic assertion
```

---

## How It Works

**Stage 1 — Build**

Maven downloads all dependencies declared in `pom.xml`, compiles the Java source, and executes the JUnit test suite via the Surefire plugin. Both `SampleTest` and `TestSample` must pass for the stage to succeed. A test failure stops the pipeline immediately.

**Stage 2 — Build Docker Image**

The Dockerfile uses `ibmjava:8-jre` as its base, copies the compiled JAR from the Maven `target/` directory, creates a non-root user (`myuser`), and exposes port 8080. Jenkins builds this image and then launches a container from it to verify the application responds on the expected port before any image is published.

**Stage 3 — Push Docker Image**

Jenkins authenticates to Docker Hub using a stored credential (`dockerhub_login`). The image is pushed twice: once with the Jenkins `BUILD_NUMBER` as the tag (providing a permanent, reproducible artifact identifier) and once as `latest` (providing a stable pointer for downstream consumers).

---

## Walkthrough

**Pipeline execution sequence**

```bash
# Jenkins triggers the pipeline on commit; the following commands run inside the pipeline stages

# Stage 1: compile and test
mvn clean install

# Stage 2: build image and smoke test
docker build -t shanemannion/spring-boot-app .
# Jenkins uses docker.inside{} to run curl inside the container:
curl localhost:8080
# expected response: "Greetings from Spring Boot!"

# Stage 3: push to registry
docker push shanemannion/spring-boot-app:<BUILD_NUMBER>
docker push shanemannion/spring-boot-app:latest
```

**REST endpoints exposed by the application**

```bash
# Root endpoint
GET /
# Returns: "Greetings from Spring Boot!"

# DevOps philosophy endpoint
GET /DevOps
# Returns: "Flow; fast feedback; learning & experimentation"

# Parameterised POST endpoints
POST /DevOps?name=Integration
# Returns: "Integration testing"

POST /YorkU?name=Lecture
# Returns: "Lecture Day"
```

---

## How to Reproduce

**Prerequisites**

- Jenkins server with the Docker Pipeline plugin installed
- Docker daemon accessible to the Jenkins process
- Maven 3.x available on the Jenkins build agent
- Docker Hub account with a credential stored in Jenkins as `dockerhub_login`
- SSH key pair or GitHub webhook configured to trigger the pipeline on push

**Steps**

```bash
# 1. Fork or clone this repository
git clone https://github.com/<your-username>/jenkins-ci-cd-springboot-docker.git

# 2. In Jenkins, create a new Pipeline job
#    - Set "Pipeline definition" to "Pipeline script from SCM"
#    - Set SCM to Git and point to your repository URL
#    - Set Script Path to: Jenkinsfile

# 3. Add your Docker Hub credentials to Jenkins
#    Manage Jenkins -> Credentials -> (global) -> Add Credentials
#    Kind: Username with password
#    ID: dockerhub_login

# 4. Update the image name in the Jenkinsfile if needed
#    Change "shanemannion/spring-boot-app" to "<your-dockerhub-username>/<your-image-name>"

# 5. Trigger a build manually or push a commit to the configured branch
#    Jenkins will execute all three stages automatically

# 6. Verify the image was pushed to Docker Hub
docker pull <your-dockerhub-username>/<your-image-name>:latest
docker run -p 8080:8080 <your-dockerhub-username>/<your-image-name>:latest
curl http://localhost:8080/
```

**Running the application locally without Jenkins**

```bash
# Build and run with Maven
mvn clean install
java -jar target/*.jar

# Or build and run with Docker
docker build -t spring-boot-app .
docker run -p 8080:8080 spring-boot-app
```

---

*Oluwaseyi Michael Falode · Cybersecurity & Cloud Security Engineer · Toronto, ON*
