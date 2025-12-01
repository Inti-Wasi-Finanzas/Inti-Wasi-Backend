# ===== Etapa de build =====
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiamos todo el proyecto
COPY . .

# Usamos el wrapper de Maven para compilar (lo mismo que ./mvnw clean package -DskipTests)
RUN chmod +x mvnw || true
RUN ./mvnw clean package -DskipTests

# ===== Etapa de runtime =====
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copiamos el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Render usará esta app como servicio web (Spring Boot expone HTTP en este puerto)
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","app.jar"]
