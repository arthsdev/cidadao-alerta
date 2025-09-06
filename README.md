# Cidadão Alerta

![Build](https://github.com/arthsdev/cidadao-alerta/actions/workflows/sonarcloud.yml/badge.svg)
![Coverage](https://sonarcloud.io/api/project_badges/measure?project=arthsdev_cidadao-alerta&metric=coverage)
![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=arthsdev_cidadao-alerta&metric=alert_status)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)



Cidadão Alerta é uma API REST desenvolvida em Java + Spring Boot para gerenciamento de reclamações públicas com autenticação via JWT, controle de acesso baseado em roles (ADMIN e USER) e validações personalizadas para latitude/longitude. O projeto segue arquitetura em camadas, utiliza Flyway para migrações de banco e possui documentação interativa com Swagger (OpenAPI).

---

## 🚀 Tecnologias Utilizadas

- Java 17+
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA (MySQL + H2 para testes)
- Flyway (controle de migrações)
- MapStruct (mapeamento de DTOs)
- Lombok
- Jakarta Validation (validações + anotações customizadas)
- Swagger / OpenAPI
- JUnit + Spring Security Test
- Jacoco (cobertura de testes)
- SonarCloud (análise de código e CI)

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
├── service       # Regras de negócio
└── validation    # Validações personalizadas (Latitude/Longitude)
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
```

> ⚠️ Troque `sua_senha` e `umaChaveSeguraComMaisDe32Caracteres!` para valores reais.

O banco de dados será atualizado automaticamente pelo Flyway na primeira execução.

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

👉 http://localhost:8080/swagger-ui.html

---

## 🔑 Autenticação

A autenticação é feita via JWT Token. Para acessar os endpoints protegidos, primeiro faça login:

```bash
curl -X 'POST' \
  'http://localhost:8080/auth/login' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
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

Use o token no Authorization Header:

```
Authorization: Bearer jwt-gerado-aqui
```

### Roles Disponíveis

- **ROLE_USER** → pode registrar e visualizar suas reclamações
- **ROLE_ADMIN** → pode gerenciar usuários e reclamações

---

## 🧪 Testes

O projeto utiliza:

- JUnit 5
- Spring Boot Test
- Spring Security Test
- Banco H2 em memória para integração

Para rodar os testes:

```bash
./mvnw test
```

Cobertura de testes atual: **93% (Jacoco)**

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

---

## 📤 Exportação de Reclamações (CSV)

O projeto permite exportar todas as reclamações em formato CSV, útil para relatórios ou análises externas.

> ⚠️ Apenas usuários com `ROLE_ADMIN` podem acessar este endpoint.

**Endpoint:**
```
GET /reclamacoes/export
```

**Descrição:** Todos os parâmetros são opcionais. Se nenhum parâmetro for informado, o endpoint retorna **todas as reclamações**.

| Parâmetro  | Tipo      | Descrição                                      | Obrigatório |
|------------|-----------|------------------------------------------------|-------------|
| status     | String    | Filtra por status da reclamação (ABERTA, RESOLVIDA, etc.) | Não |
| usuarioId  | Long      | Filtra reclamações de um usuário específico    | Não         |
| categoria  | String    | Filtra por categoria da reclamação             | Não |
| dataInicio | Date      | Data inicial para filtro (formato ISO: YYYY-MM-DD) | Não |
| dataFim    | Date      | Data final para filtro (formato ISO: YYYY-MM-DD)  | Não |

**Exemplo de requisição com filtros:**
```bash
curl -X 'GET' \
  'http://localhost:8080/reclamacoes/export?status=ABERTA&categoria=ILUMINACAO' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <token-admin>'
```

**Resposta:** Retorna o arquivo CSV contendo as reclamações filtradas.

---

## 🚀 Possíveis Melhorias Futuras

- Exportar reclamações em CSV/Excel aprimorado
- Integração com mapas (Google Maps / Leaflet)
- Envio de notificações por e-mail
- Aplicativo frontend para consumo da API
- Cobertura de testes e métricas com JaCoCo e SonarCloud
- Logging estruturado e monitoramento de aplicações

---

## 👨‍💻 Autor

Projeto desenvolvido por **Artheus** (estudante e desenvolvedor backend). Repositório: [cidadao-alerta](https://github.com/arthsdev/cidadao-alerta)

