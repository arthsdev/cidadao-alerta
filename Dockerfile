# Imagem base com JDK + Maven
FROM maven:3.9.5-eclipse-temurin-17

WORKDIR /app

# Copia pom.xml e faz cache das dependências
COPY pom.xml .

# Baixa dependências offline
RUN mvn dependency:go-offline

# Copia todo o projeto
COPY . .

# Build da aplicação
RUN mvn clean package -DskipTests

# Expor porta do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
CMD ["java", "-jar", "target/cidadaoalerta-0.0.1-SNAPSHOT.jar"]
