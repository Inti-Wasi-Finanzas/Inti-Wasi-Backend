FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiamos todo el proyecto al contenedor
COPY . .

# Damos permiso de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Compilamos el proyecto (sin tests)
RUN ./mvnw clean package -DskipTests

# ===== Etapa de runtime =====
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Copiamos el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Copiamos wait-for-it.sh al contenedor
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

EXPOSE 8080

ENTRYPOINT ["/app/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]

