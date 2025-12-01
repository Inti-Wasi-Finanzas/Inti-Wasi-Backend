# ===== Etapa de build =====
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiamos todo el proyecto
COPY . .

# Nos movemos a la carpeta donde está mvnw y pom.xml
WORKDIR /app/inti-wasi

# Damos permisos y compilamos
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ===== Etapa de runtime =====
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copiamos el JAR generado
COPY --from=build /app/inti-wasi/target/*.jar app.jar

# Copiamos wait-for-it.sh
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

EXPOSE 8080

ENTRYPOINT ["/app/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]
