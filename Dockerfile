FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B verify dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/lib

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
COPY --from=build /build/target/lib ./lib
COPY --from=build /build/target/turmalina-pb-*.jar ./turmalina-pb.jar
CMD ["java", "-cp", "turmalina-pb.jar:lib/*", "br.edu.ifpb.pweb3.turmalina.app.Inicializacao"]
