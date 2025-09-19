# 📌 Cidadão Alerta
[![Java](https://img.shields.io/badge/Java-17-red)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)](https://spring.io/projects/spring-boot)
[![Coverage](https://img.shields.io/badge/Coverage-93%25-blue)]()
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=arthsdev_cidadao-alerta&metric=alert_status)](https://sonarcloud.io/project/overview?id=arthsdev_cidadao-alerta)
![Build](https://github.com/arthsdev/cidadao-alerta/actions/workflows/sonarcloud.yml/badge.svg)

API REST desenvolvida em **Java + Spring Boot** para **gerenciamento de reclamações públicas**, com autenticação via **JWT**, controle de acesso baseado em **roles (ADMIN e USER)**, exportação de dados em **CSV**, envio de **e-mails administrativos**, **logging estruturado**, validações customizadas e monitoramento de qualidade de código via **SonarCloud**.

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
├── config        # Configurações de segurança e JWT
├── controller    # Controladores REST (endpoints)
├── dto           # DTOs de entrada e saída
├── exception     # Tratamento de erros personalizados
├── mapper        # Interfaces do MapStruct
├── model         # Entidades JPA
│   ├── enums     # Enums (roles, categorias, status...)
├── repository    # Interfaces do Spring Data JPA
├── security      # Filtros, serviços e utilitários JWT
├── service       # Regras de negócio, envio de e-mails
├── validation    # Validações personalizadas (Latitude/Longitude)
└── util          # Utilitários (helpers, conversores, etc.)
```

---

## ⚙️ Configuração do Ambiente
Antes de rodar o projeto, configure as variáveis de ambiente no IntelliJ (Run → Edit Configurations → Environment Variables):

```
DB_URL=jdbc:mysql://localhost:3306/cidadaoalerta_db
DB_USERNAME=root
DB_PASSWORD=sua_senha
JWT_SECRET=umaChaveSeguraComMaisDe32Caracteres!
JWT_EXPIRATION=3600000
MAIL_USERNAME=seu_email@dominio.com
MAIL_PASSWORD=sua_senha_email
```

⚠️ **Troque `sua_senha`, `umaChaveSeguraComMaisDe32Caracteres!` e `seu_email@dominio.com` para valores reais.**  
O banco será atualizado automaticamente pelo **Flyway** na primeira execução.

---

## ▶️ Executando o Projeto
Clone o repositório e execute com Maven:

```bash
git clone https://github.com/arthsdev/cidadao-alerta.git
cd cidadao-alerta
./mvnw spring-boot:run
```

---

## 📖 Documentação da API
Após rodar o projeto, acesse o Swagger:

👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🔑 Autenticação
Autenticação via **JWT Token**. Primeiro faça login:

```bash
curl -X 'POST'   'http://localhost:8080/auth/login'   -H 'accept: application/json'   -H 'Content-Type: application/json'   -d '{
  "email": "usuario@email.com",
  "senha": "senha123"
}'
```

Resposta:
```json
{
  "token": "jwt-gerado-aqui"
}
```

Use o token no **Authorization Header**:
```
Authorization: Bearer jwt-gerado-aqui
```

### Roles Disponíveis
- `ROLE_USER` → registrar e visualizar suas próprias reclamações
- `ROLE_ADMIN` → gerenciar usuários e reclamações

---

## 🧪 Testes
O projeto utiliza:
- **JUnit 5**
- **Spring Boot Test**
- **Spring Security Test**
- **Banco H2 em memória** para integração

Para rodar os testes:
```bash
./mvnw test
```

📊 Cobertura de testes atual: **93% (JaCoCo)**

---

## ✨ Funcionalidades
- CRUD de usuários e reclamações
- Cadastro e autenticação de usuários (JWT)
- Controle de acesso baseado em roles (USER / ADMIN)
- Validação personalizada de Latitude e Longitude
- Documentação interativa com Swagger
- Migrações automáticas com Flyway
- Testes unitários e de integração com H2
- Exportação de reclamações em CSV
- Exceptions personalizadas para erros
- **Logging estruturado para auditoria**
- **Disparo de e-mails para eventos administrativos** (assíncrono via eventos)

---

## 📤 Exportação de Reclamações (CSV)
Endpoint exclusivo para **ROLE_ADMIN**:
```http
GET /reclamacoes/export
```

### Parâmetros opcionais:
- `status` → Filtrar por status da reclamação (ABERTA, RESOLVIDA, etc.)
- `usuarioId` → Reclamações de um usuário específico
- `categoria` → Categoria da reclamação
- `dataInicio` → Data inicial (YYYY-MM-DD)
- `dataFim` → Data final (YYYY-MM-DD)

Exemplo:
```bash
curl -X 'GET'   'http://localhost:8080/reclamacoes/export?status=ABERTA&categoria=ILUMINACAO'   -H 'accept: */*'   -H 'Authorization: Bearer <token-admin>'
```

Resposta: Arquivo CSV com as reclamações filtradas.

---

## 🚀 Possíveis Melhorias Futuras
- Exportação em **Excel** além de CSV
- Integração com **mapas interativos** (Google Maps / Leaflet)
- Envio de **notificações por e-mail** para usuários e admins
- Aplicativo frontend (React/Next.js) para consumo da API
- Relatórios e dashboards de estatísticas
- Monitoramento com **Prometheus + Grafana**

---

## 👨‍💻 Autor
Projeto desenvolvido por **Artheus** (estudante e desenvolvedor backend).  
📂 Repositório: [cidadao-alerta](https://github.com/arthsdev/cidadao-alerta)

