# Alpine-basiertes Image mit Java 25 (Base-Image spaeter wechselbar, z.B. Ubuntu)
FROM eclipse-temurin:25-jdk-alpine

# Werkzeuge, die man zum Entwickeln braucht
RUN apk add --no-cache bash git

# Benutzer mit UID:GID 1000:1000 anlegen (nicht als root arbeiten)
RUN addgroup -g 1000 dev \
    && adduser -u 1000 -G dev -s /bin/bash -D dev

USER dev
WORKDIR /workspace

# Gradle und JUnit kommen ueber den ./gradlew-Wrapper bzw. build.gradle
