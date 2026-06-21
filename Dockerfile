# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# baixa dependências primeiro (camada cacheável)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests || true

# compila e empacota o jar executável
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
# o perfil e as credenciais vêm por variáveis de ambiente (ver podman-up.ps1)
ENTRYPOINT ["java", "-jar", "app.jar"]
