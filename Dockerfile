FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B verify

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
COPY --from=build /build/target/turmalina-pb-*.jar ./turmalina-pb.jar
EXPOSE 8080
CMD ["java", "-jar", "turmalina-pb.jar"]
