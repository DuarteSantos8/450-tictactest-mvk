FROM eclipse-temurin:21-jdk

# git wird von actions/checkout im Container benötigt
RUN apt-get update && apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Gradle selbst wird nicht installiert – der ./gradlew-Wrapper lädt die
# passende Gradle-Version zur Laufzeit. Das Image liefert nur das JDK 21 + git.
