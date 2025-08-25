# Usa una imagen base de OpenJDK
FROM amazoncorretto:17-alpine-jdk


# Crea un directorio para la aplicación
WORKDIR /usr/app

# Copia el archivo JAR al contenedor
COPY ms-diagnostico-basico-cpe.jar /usr/app/ms-diagnostico-basico-cpe.jar

# Crea un directorio para los logs y establece permisos
RUN mkdir -p /var/log/ms-diagnostico-basico-cpe && chmod -R 755 /var/log/ms-diagnostico-basico-cpe

# Expone el puerto en el que la aplicación escuchará
EXPOSE 9100

# Establecer la carpeta de logs como volumen
VOLUME /var/log/ms-diagnostico-basico-cpe

# Comando para ejecutar el archivo JAR
ENTRYPOINT ["java", "-jar", "ms-diagnostico-basico-cpe.jar"]