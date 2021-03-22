FROM openjdk:8-jdk-alpine
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} java-challenge.jar
EXPOSE 18080
ENTRYPOINT ["java","-jar", "java-challenge.jar"]