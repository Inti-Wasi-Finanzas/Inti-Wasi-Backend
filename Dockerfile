# ===== Etapa de build =====
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiamos todo el repo
COPY . .

# Nos movemos a la carpeta donde está el pom.xml y mvnw
WORKDIR /app/inti-wasi

# Damos permiso de ejecución al wrapper de Maven
RUN chmod +x mvnw || true

# Compilamos el proyecto (sin tests)
RUN ./mvnw clean package -DskipTests

# ===== Etapa de runtime =====
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copiamos el JAR generado desde la carpeta del proyecto
COPY --from=build /app/inti-wasi/target/*.jar app.jar

# Puerto que usa Spring Boot
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","app.jar"]
