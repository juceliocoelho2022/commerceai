# CommerceAI — Plataforma Inteligente de E-commerce

![Java 21](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-green) ![React](https://img.shields.io/badge/React-19-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)

MVP educacional de e-commerce com catálogo, carrinho, pedidos demonstrativos, gestão de estoque, dashboard e recomendações baseadas em regras. **Não há pagamento real nem modelo de IA generativa nesta versão.**

## Arquitetura

```text
Navegador → React + Vite (:3000) → /api (proxy) → Spring Boot (:8080 interno)
                                                   ├── JPA/Hibernate
                                                   ├── Flyway (migração V1)
                                                   └── PostgreSQL (db:5432)
Host Windows: API 8085 · PostgreSQL 5440 · Frontend 3000
```

## Estado validado (24/10/2026)

- PostgreSQL saudável e Flyway V1 aplicado; tabelas `products`, `customer_orders`, `order_items`, `flyway_schema_history`.
- Actuator respondeu HTTP 200 / `UP`; Vite iniciado.
- Catálogo com seis produtos; dois pedidos demonstrativos `CREATED`, cada um de R$ 89,90.
- Valores dos pedidos conferidos com `SUM(quantity * unit_price)`, sem divergências.
- Garrafa Térmica (ID 4) com estoque observado de 63 unidades. **A baixa ainda requer teste antes/depois e concorrência**.
- Evidências obtidas no ambiente local do desenvolvedor; não equivalem a homologação de produção.

## Regras de negócio

### Catálogo de produtos (RN-CAT)

| Código | Regra | Estado |
|---|---|---|
| RN-CAT-01 | Produtos são persistidos com identificador único. | Implementada |
| RN-CAT-02 | Catálogo expõe nome, categoria, preço, estoque e descrição/imagem quando cadastradas. | Implementada |
| RN-CAT-03 | A busca e filtragem por categoria são disponibilizadas na API. | Implementada |
| RN-CAT-04 | Consultar produto inexistente retorna 404. | Implementada |
| RN-CAT-05 | Cadastro e atualização administrativa com autorização exigirão autenticação. | Planejada |

### Carrinho e pedidos (RN-PED)

| Código | Regra | Estado |
|---|---|---|
| RN-PED-01 | Pedido precisa identificar cliente com nome e e-mail. | Implementada |
| RN-PED-02 | Pedido precisa ter ao menos um item válido e quantidade positiva. | Implementada |
| RN-PED-03 | Produtos repetidos na solicitação são agregados por quantidade. | Implementada |
| RN-PED-04 | Produto inexistente na compra deve impedir a criação (400). | Implementada |
| RN-PED-05 | Quantidade superior ao estoque disponível deve ser rejeitada (409). | Implementada; sem teste concorrente |
| RN-PED-06 | Pedido aceito deve reduzir o estoque pela quantidade comprada. | Implementada; validar antes/depois |
| RN-PED-07 | Item armazena nome e preço unitário no momento da compra (snapshot). | Implementada |
| RN-PED-08 | `total` = soma de `quantity × unit_price`; cálculos monetários com `BigDecimal`. | Implementada e validada para 2 pedidos |
| RN-PED-09 | Pedido nasce com status `CREATED`, que não significa pagamento confirmado. | Implementada |
| RN-PED-10 | Pedido, itens e estoque devem ser processados de modo transacional. | Implementada com `@Transactional`; teste de rollback pendente |
| RN-PED-11 | Controle de concorrência de estoque e idempotência devem evitar venda excessiva/duplicidade. | Pendente |
| RN-PED-12 | Pagamento, frete, tributos, cupons, cancelamento e reembolso não integram este MVP. | Fora do escopo |

### Analytics (RN-ANA)

| Código | Regra | Estado |
|---|---|---|
| RN-ANA-01 | Contar produtos existentes. | Implementada |
| RN-ANA-02 | Contar pedidos persistidos. | Implementada |
| RN-ANA-03 | Indicador `revenue` soma valores de pedidos criados, **não receita recebida**. | Implementada |
| RN-ANA-04 | Estoque baixo significa quantidade menor ou igual a 10. | Implementada |

### Recomendações (RN-REC)

| Código | Regra | Estado |
|---|---|---|
| RN-REC-01 | Recomendar apenas produtos com estoque positivo. | Implementada |
| RN-REC-02 | Priorizar a categoria solicitada, quando informada. | Implementada |
| RN-REC-03 | Desempate por estoque decrescente e ID crescente. | Implementada |
| RN-REC-04 | Limitar resposta a quatro sugestões. | Implementada |
| RN-REC-05 | Recomendações são determinísticas; não usam LLM ou histórico personalizado. | Implementada |

### Segurança e dados (RN-SEG)

| Código | Regra | Estado |
|---|---|---|
| RN-SEG-01 | Autenticação JWT e perfis ADMIN/CUSTOMER. | Planejada |
| RN-SEG-02 | Serviços locais ficam publicados no loopback `127.0.0.1`. | Configuração Docker |
| RN-SEG-03 | Não versionar `.env`, senhas, tokens e chaves. | Prática obrigatória |
| RN-SEG-04 | Tratamento de dados pessoais, auditoria e LGPD exigem implementação antes de produção. | Planejada |

## Modelo de dados

- `products`: id, name, category, description, price, stock, image_url.
- `customer_orders`: id, customer_name, customer_email, status, total, created_at.
- `order_items`: id, order_id, product_id, product_name, quantity, unit_price.
- `flyway_schema_history`: histórico das migrações.

## API REST

| Método | Rota | Descrição |
|---|---|---|
| GET | `/actuator/health` | Saúde da API |
| GET | `/api/products` | Catálogo |
| GET | `/api/products/{id}` | Detalhe do produto |
| GET | `/api/recommendations?category=Casa` | Sugestões por regras |
| GET | `/api/dashboard` | Métricas agregadas |
| POST | `/api/orders` | Criação de pedido demonstrativo |

## Executar localmente (Windows)

Pré-requisito: Docker Desktop ativo. Configure `.env` a partir do exemplo sem versionar credenciais.

```powershell
cd C:\Projetos\commerceai
docker compose up -d --build
docker compose ps
curl.exe http://127.0.0.1:8085/actuator/health
curl.exe http://127.0.0.1:8085/api/products
```

Acesse http://localhost:3000. No Compose atualizado, host PostgreSQL = `5440`, host API = `8085`; **entre contêineres** use `db:5432` e `backend:8080`. Para Java executado pelo IntelliJ fora do Docker, use `localhost:5440` no JDBC. Não execute `docker compose down -v` se quiser preservar dados.

Valide os pedidos:

```powershell
docker compose exec db psql -U commerceai -d commerceai -c "SELECT id,status,total FROM customer_orders ORDER BY id DESC LIMIT 5;"
docker compose exec db psql -U commerceai -d commerceai -c "SELECT o.id,o.total,SUM(i.quantity*i.unit_price) AS calculated FROM customer_orders o JOIN order_items i ON i.order_id=o.id GROUP BY o.id,o.total ORDER BY o.id;"
```

## Qualidade e CI

```powershell
cd backend
mvn test
cd ..\frontend
npm install
npm run build
```

O GitHub Actions executa testes Maven (Java 21) e build frontend (Node 22) em push e pull request para `main`. **Ainda é necessário confirmar a execução verde da pipeline**. Testes com PostgreSQL/Testcontainers, concorrência e E2E seguem pendentes.

## Roadmap

- **v0.1.2**: documentação de regras de negócio, CI e estabilização.
- **v0.2.0**: JWT/RBAC, CRUD administrativo, proteção concorrente do estoque, idempotência e testes integrados.
- **v0.3.0**: assistente com IA generativa via backend, observabilidade, limites de custo e proteções.
- **Antes da produção**: pagamentos com integração certificada, LGPD, auditoria, testes de carga e segurança.

## Licença

Licença ainda não definida. Todos os direitos permanecem reservados ao titular do projeto.

## Autenticação v0.2.0 (branch de desenvolvimento)

- `POST /api/auth/register`: cadastro público sempre com perfil **CUSTOMER** (e-mail único, senha de 8 a 72 caracteres); senha armazenada como hash BCrypt.
- `POST /api/auth/login`: emite JWT HS256 com prazo de 2 horas; exige variável `JWT_SECRET` com pelo menos 32 caracteres.
- `/api/admin/**`: protegido por perfil ADMIN; **nenhuma conta ADMIN é criada automaticamente**.
- Catálogo GET, dashboard GET e checkout POST permanecem públicos nesta etapa para preservar o MVP demonstrativo.
- **Não coloque JWT_SECRET no GitHub.** Configure no seu `.env` local; para desenvolvimento use uma sequência aleatória longa, gerada localmente, e não a compartilhe.
- Esta etapa ainda depende de CI e testes de integração para validação; cadastro, login e autorização devem ser exercitados antes do merge.

## Administração de produtos — v0.2.0 (em validação)

Todos os endpoints abaixo exigem JWT com perfil `ADMIN`; um usuário criado por `/api/auth/register` recebe somente `CUSTOMER`.

| Método | Rota | Comportamento |
| --- | --- | --- |
| GET | `/api/admin/products` | Lista o catálogo para administração |
| POST | `/api/admin/products` | Cria produto (HTTP 201) |
| PUT | `/api/admin/products/{id}` | Atualiza os campos do produto |
| DELETE | `/api/admin/products/{id}` | Exclui produto (HTTP 204), somente se não houver itens de pedidos associados |

O corpo para criar ou editar produtos exige `name` (até 150 caracteres), `category` (até 70), `description` (até 500), `price` positivo com até duas casas decimais e `stock` não negativo. `imageUrl` é opcional (até 500 caracteres). Ao excluir produto já referenciado em `order_items`, a API retorna 409 para preservar o histórico.

**Limite atual:** ainda não existe provisionamento de ADMIN por endpoint público nem painel visual administrativo; testes unitários não substituem testes HTTP de autorização ou testes integrados no PostgreSQL. Não promover esta branch para produção antes dessas validações.
