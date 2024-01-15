FROM ibmjava:8-jre
COPY target/*.jar app.jar
RUN useradd -ms /bin/bash myuser
USER myuser
EXPOSE 8080
CMD ["java","-jar","/app.jar"]