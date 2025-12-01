# ----------------------------------------------------------------------------------
# ETAPA 1: BUILD (Compilación y generación del JAR)
# Usamos el JDK completo para compilar el proyecto.
# ----------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-focal AS build

# Establecer el directorio de trabajo
WORKDIR /app/inti-wasi

# Mejorar la eficiencia del caché: Copiamos solo los archivos de dependencias primero.
# Esto asegura que la capa de instalación de dependencias solo se reconstruya si cambian
# el pom.xml o el mvnw, lo cual es menos frecuente que el cambio de código.
COPY inti-wasi/pom.xml .
COPY inti-wasi/mvnw .
COPY inti-wasi/.mvn .

# Descargamos las dependencias
RUN ./mvnw dependency:go-offline -B

# Copiamos el resto del código fuente del proyecto
COPY . .

# Compilamos el proyecto, saltando los tests para acelerar la construcción.
# El output se genera en /app/inti-wasi/target/
RUN ./mvnw clean package -DskipTests

# ----------------------------------------------------------------------------------
# ETAPA 2: RUNTIME (Ejecución de la aplicación)
# Usamos una imagen de JRE 'slim' (solo el runtime) para una imagen final pequeña y segura.
# ----------------------------------------------------------------------------------
# Es recomendable usar la misma versión que en el build, pero con el JRE más pequeño.
FROM eclipse-temurin:21-jre-focal AS runtime

# Establecer el directorio de trabajo
WORKDIR /app

# Copiamos el JAR generado desde la etapa de build. El nombre se cambia a 'app.jar'.
COPY --from=build /app/inti-wasi/target/*.jar app.jar

# MEJORA DE SEGURIDAD: Crear y usar un usuario no-root (ID 1000 es el primer usuario no-root estándar)
# Esto reduce el riesgo de seguridad si el contenedor es comprometido.
RUN groupadd -r springboot && useradd -r -g springboot springboot
USER springboot

# Puerto que usará Spring Boot (Azure leerá esta información)
EXPOSE 8080

# Comando de arranque: Ejecutar el JAR con el JRE.
ENTRYPOINT ["java","-jar","app.jar"]
