# 📌 Cidadão Alerta
[![Java](https://img.shields.io/badge/Java-17-red)](https://www.oracle.com/java/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)](https://spring.io/projects/spring-boot) [![Coverage](https://img.shields.io/badge/Coverage-93%25-blue)]() [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=arthsdev_cidadao-alerta&metric=alert_status)](https://sonarcloud.io/project/overview?id=arthsdev_cidadao-alerta) ![Build](https://github.com/arthsdev/cidadao-alerta/actions/workflows/sonarcloud.yml/badge.svg)

API REST desenvolvida em **Java + Spring Boot** para **gerenciamento de reclamações públicas**, com **autenticação via JWT**, **controle de acesso baseado em roles (ADMIN e USER)**, **exportação de dados em CSV**, **envio de e-mails administrativos**, **logging estruturado**, **validações customizadas**, **contêineres Docker para fácil execução local** e **deploy automatizado no Railway**, além de **monitoramento de qualidade de código via SonarCloud**.

---

## 🚀 Tecnologias Utilizadas
- Java 17+
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA (MySQL + H2 para testes)
- Flyway (migrações de banco)
- MapStruct (mapeamento de DTOs)
- Lombok
- Jakarta Validation (validações + anotações customizadas)
- Swagger / OpenAPI (documentação interativa)
- JUnit 5 + Spring Security Test
- JaCoCo (cobertura de testes)
- SonarCloud (análise de qualidade e CI)
- Logging Estruturado (SLF4J + Logback)
- Envio de E-mails (JavaMailSender, eventos assíncronos via ApplicationEventPublisher)

---

## 📂 Estrutura do Projeto
```
src/main/java/com/artheus/cidadaoalerta
├── config
├── controller
├── dto
├── exception
├── mapper
├── model
│   ├── enums
├── repository
├── security
├── service
├── validation
└── util
```

---

## ⚙️ Configuração do Ambiente
Configure as variáveis de ambiente:

```
DB_URL=jdbc:mysql://localhost:3306/cidadaoalerta_db
DB_USERNAME=root
DB_PASSWORD=sua_senha
JWT_SECRET=umaChaveSeguraComMaisDe32Caracteres!
JWT_EXPIRATION=3600000
MAIL_USERNAME=seu_email@dominio.com
MAIL_PASSWORD=sua_senha_email
```

⚠️ Troque os valores para reais. O banco será atualizado automaticamente pelo **Flyway**.

---

## ▶️ Executando o Projeto

### Localmente com Maven
```bash
git clone https://github.com/arthsdev/cidadao-alerta.git
cd cidadao-alerta
./mvnw spring-boot:run
```

### Com Docker Compose
```bash
docker compose up --build -d
```
O container estará disponível em `http://localhost:8080`.

Exemplo de `docker-compose.yml`:

```yaml
version: "3.8"

services:
  db:
    image: mysql:8
    container_name: cidadaoalerta-db
    environment:
      MYSQL_DATABASE: cidadaoalerta_db
      MYSQL_ROOT_PASSWORD: sua_senha
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql

  app:
    build: .
    container_name: cidadaoalerta-app
    environment:
      DB_URL: jdbc:mysql://db:3306/cidadaoalerta_db
      DB_USERNAME: root
      DB_PASSWORD: sua_senha
      JWT_SECRET: umaChaveSeguraComMaisDe32Caracteres!
      JWT_EXPIRATION: 3600000
      MAIL_USERNAME: seu_email@dominio.com
      MAIL_PASSWORD: sua_senha_email
    ports:
      - "8080:8080"
    depends_on:
      - db

volumes:
  db_data:
```

---

## 🌐 Deploy em Railway
- Deploy automático via GitHub Actions.
- Variáveis de ambiente devem ser configuradas no Railway antes do deploy.
- A aplicação não iniciará corretamente sem o banco de dados ativo.

---

## 📖 Documentação da API
Acesse o Swagger:  
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🔑 Autenticação
Login via JWT:

```bash
POST /auth/login
{
  "email": "usuario@email.com",
  "senha": "senha123"
}
```

Resposta:
```json
{
  "token": "jwt-gerado-aqui"
}
```

Use no header:
```
Authorization: Bearer jwt-gerado-aqui
```

Roles disponíveis:
- `ROLE_USER`
- `ROLE_ADMIN`

---

## 🧪 Testes
- JUnit 5 + Spring Boot Test + Spring Security Test  
- Banco H2 para integração

Rodar testes:
```bash
./mvnw test
```

---

## ✨ Funcionalidades
- CRUD de usuários e reclamações
- Controle de acesso baseado em roles
- Exportação de reclamações em CSV
- Validações personalizadas
- Migrações automáticas com Flyway
- Logging estruturado
- Disparo de e-mails assíncrono
- Testes unitários e de integração

---

## 📤 Exportação de Reclamações (CSV)
- Endpoint: `GET /reclamacoes/export`
- Parâmetros opcionais: `status`, `usuarioId`, `categoria`, `dataInicio`, `dataFim`

Exemplo:
```bash
curl -X 'GET' 'http://localhost:8080/reclamacoes/export?status=ABERTA&categoria=ILUMINACAO' -H 'Authorization: Bearer <token-admin>'
```

---

## 🚀 Possíveis Melhorias Futuras
- Exportação em Excel
- Mapas interativos (Google Maps / Leaflet)
- Notificações por e-mail
- Frontend React/Next.js
- Dashboards e relatórios
- Monitoramento com Prometheus + Grafana

---

## 👨‍💻 Autor
Desenvolvido por **Artheus**  
📂 Repositório: [cidadao-alerta](https://github.com/arthsdev/cidadao-alerta)
