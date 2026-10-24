# CommerceAI — MVP v0.1.0

Plataforma de e-commerce demonstrativa com **Java 21, Spring Boot 4.1.1, React 19, Vite, PostgreSQL 17, Flyway e Docker Compose**.

## Arquitetura

```
React/Vite (:3000) -- /api --> Spring Boot (:8080) --> JPA --> PostgreSQL (:5432 interno; :5433 host)
                                                      \-> Flyway (V1, seed)
```

## Executar no Windows (PowerShell)

Requisitos: Docker Desktop iniciado e Docker Compose v2. Execute na pasta raiz do projeto:

```powershell
Copy-Item .env.example .env
# Altere DB_PASSWORD no .env se necessário

docker compose up --build -d
docker compose ps
curl.exe http://localhost:8080/actuator/health
curl.exe http://localhost:8080/api/products
```

Abra **http://localhost:3000**. Na primeira execução, o Docker compila e baixa dependências.

Ver logs: `docker compose logs -f backend`; parar sem apagar banco: `docker compose down`; **apagar banco**: `docker compose down -v` (destrutivo).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/actuator/health` | Healthcheck |
| GET | `/api/products` | Catálogo |
| GET | `/api/products?category=Eletrônicos` | Filtrar |
| GET | `/api/products/{id}` | Produto |
| GET | `/api/recommendations?category=Casa` | Sugestões por regras |
| GET | `/api/dashboard` | Indicadores |
| POST | `/api/orders` | Criar pedido e reduzir estoque |

Exemplo pedido (PowerShell):

```powershell
$body = @{ customerName='Cliente Demo'; customerEmail='demo@example.com'; items=@(@{productId=1;quantity=2}) } | ConvertTo-Json -Depth 4
Invoke-RestMethod -Uri 'http://localhost:8080/api/orders' -Method Post -ContentType 'application/json' -Body $body
```

## Critérios de aceite

1. `/actuator/health` responde `UP`.
2. `/api/products` devolve 6 produtos iniciais.
3. Catálogo, filtros, busca e carrinho operam no navegador.
4. Ao criar pedido válido, o estoque diminui e `/api/dashboard` mostra um pedido a mais.
5. Estoque insuficiente devolve HTTP 409; campo inválido devolve HTTP 400.

## IA: o que existe agora e o que virá

O endpoint `/api/recommendations` é **um motor determinístico de recomendações**, não um LLM nem um modelo treinado. Prioriza a categoria pedida e produtos disponíveis. Não utiliza histórico pessoal. A versão 0.2 pode adicionar um provedor LLM opcional, com chave apenas no backend, limites, logs sem PII, fallback, avaliação de respostas e política de consentimento.

## Limitações intencionais (MVP local)

- Pedidos são *demonstrações*, sem gateway, liquidação, captura ou reembolso. Status `CREATED` não equivale a pago.
- Dashboard usa soma de valores dos pedidos criados, não receita recebida.
- Não há autenticação nem autorização: **não publicar como serviço de produção**.
- Checkout usa transação única, mas bloqueio de concorrência/locking de estoque ainda não está implementado. Para carga real: `@Version` ou lock pessimista, idempotency key e testes concorrentes.
- CORS não é necessário no desenvolvimento: Vite encaminha `/api` ao backend via proxy.
- Senha padrão é apenas para desenvolvimento e deve ser trocada; Postgres exposto exclusivamente em loopback na porta 5433.
- Fotos são representadas por emojis, sem imagens externas obrigatórias.

## Próximo marco v0.2

Autenticação JWT/RBAC, gestão CRUD administrativa com auditoria, idempotência de pedidos, reserva de estoque concorrente, testes de integração com Testcontainers, API OpenAPI e IA generativa opcional com avaliações e proteção a prompt injection.
