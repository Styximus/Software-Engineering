FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY src/ src/
RUN javac -encoding UTF-8 --release 17 -d out src/*.java

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/out/ out/
COPY samples/catalog/ samples/catalog/
EXPOSE 8765
VOLUME /data
CMD ["java", "-cp", "out", "WorkshopServer", "0.0.0.0", "8765", "/data", "samples/catalog"]
