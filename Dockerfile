# ===== Etapa de build =====
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiamos todo el repositorio al contenedor
COPY . .

# Damos permiso de ejecución al wrapper de Maven
RUN chmod +x mvnw || true

# Compilamos el proyecto (sin tests)
RUN ./mvnw clean package -DskipTests

# ===== Etapa de runtime =====
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copiamos el JAR generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Copiamos wait-for-it.sh y damos permisos
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# Puerto que usa Spring Boot
EXPOSE 8080

# Espera a que MySQL esté listo antes de arrancar la app
ENTRYPOINT ["/app/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]

