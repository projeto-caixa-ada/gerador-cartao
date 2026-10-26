# Build da aplicação Quarkus (JVM) e imagem final enxuta.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /deployments

COPY --from=build /build/target/quarkus-app/lib/ ./lib/
COPY --from=build /build/target/quarkus-app/*.jar ./
COPY --from=build /build/target/quarkus-app/app/ ./app/
COPY --from=build /build/target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -jar /deployments/quarkus-run.jar"]
