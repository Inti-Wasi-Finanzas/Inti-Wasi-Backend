# ===== Etapa de build =====
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiamos todo el repositorio
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


# Copiamos wait-for-it.sh al contenedor
COPY wait-for-it.sh /app/inti-wasi/wait-for-it.sh
RUN chmod +x /app/inti-wasi/wait-for-it.sh
# Puerto que usa Spring Boot
EXPOSE 8080


# Espera a que MySQL esté listo antes de arrancar
ENTRYPOINT ["/app/inti-wasi/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]

