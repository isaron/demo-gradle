# demo-gradle

Spring Boot project with Gradle building.

Spring Boot compoments:
- Web
- JPA
- Security

Run Sonar Scanner for Gradle:   
- declare the `org.sonarqube` plugin in your `build.gradle` file:
```
plugins {
  id "org.sonarqube" version "2.6"
}
```
- and run the following command:
```
./gradlew sonarqube \
  -Dsonar.host.url=https://sonar.ssii.com \
  -Dsonar.login=78e4c996818567e429196c8076dee35166351f1e
```
