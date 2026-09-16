# Alpine-basiertes Image mit Java 25 von Azul Zulu.
# Azul, weil das Projekt (gradle/gradle-daemon-jvm.properties) genau diese JDK
# erwartet - sonst wuerde Gradle sie herunterladen, was auf Alpine fehlschlaegt.
# (Base-Image spaeter wechselbar, z.B. auf Ubuntu.)
FROM azul/zulu-openjdk-alpine:25

# Werkzeuge, die man zum Entwickeln braucht
RUN apk add --no-cache bash git

# Benutzer mit UID:GID 1000:1000 anlegen (nicht als root arbeiten)
RUN addgroup -g 1000 dev \
    && adduser -u 1000 -G dev -s /bin/bash -D dev

USER dev
WORKDIR /workspace

# Gradle und JUnit kommen ueber den ./gradlew-Wrapper bzw. build.gradle
