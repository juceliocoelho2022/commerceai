# CommerceAI B2B — Produto comercial e roadmap v0.4.0

> Status: **planejamento**. Documento de arquitetura e oferta comercial; não indica que checkout real, frete, isolamento multi-tenant ou recursos listados estejam implementados.
> Base de código: `feature/v0.3.1-marketplace-premium` (React/Vite, Java 21/Spring Boot, PostgreSQL/Flyway, Docker Compose).

## 1. Proposta de valor

**CommerceAI Lojas**: implantação de loja virtual própria para pequenos comerciantes, com catálogo, carrinho, gestão de estoque, domínio e suporte. Modelo de receita inicial: **taxa de implantação + assinatura mensal por loja**, sem promessa de crescimento de vendas.

**Cliente-piloto recomendado:** comerciante de São Paulo com 10–100 SKUs, operação de bairro, fotos e tabela de preços existentes, entrega local/retirada disponível e pessoa responsável por atendimento. Evitar começar com setores com regras especiais de venda.

## 2. Estrutura do produto

### Fase 1 — 1 loja por instalação (primeiros 1 a 3 clientes)

Optar inicialmente por **instâncias isoladas por comerciante**, cada uma com:
- domínio/subdomínio próprio e HTTPS;
- banco PostgreSQL e credenciais independentes;
- catálogo, pedidos e contas administrativas exclusivos;
- configuração própria de marca, dados da empresa, contatos, políticas e frete;
- backup e monitoramento separados;
- conta do provedor de pagamentos pertencente ao comerciante.

Isso evita introduzir um multi-tenant compartilhado no MVP sem testes suficientes de isolamento. **Não reutilizar** o banco `commerceai` de demonstração nem expor contas/senhas de teste. Os dados demo e a arquitetura local não estão prontos para produção.

### Fase 2 — SaaS multi-tenant, apenas após pilotos e auditoria

`merchant` (empresa) -> `store` (loja) -> `product`, `order`, `order_item`, `customer`, `shipping_rule`, `payment_connection`.

Requisitos obrigatórios:
1. Toda consulta/escrita é vinculada ao **tenant resolvido no servidor** (domínio/slug + autenticação e autorização); nunca confiar em `merchantId` enviado arbitrariamente pelo cliente.
2. Dono da loja não pode acessar produtos, pedidos, relatórios, tokens ou clientes de outra loja, mesmo alterando IDs das URLs.
3. JWT com identificação/associação à loja validada no banco; perfis `PLATFORM_ADMIN`, `MERCHANT_OWNER`, `STAFF` e `CUSTOMER`, sem endpoints públicos para elevar privilégios.
4. Chaves do gateway por loja somente em gerenciador de segredos; criptografia e rotação; nenhum token de pagamento no React.
5. Índices/constraints por loja, auditoria de operações e testes de isolamento concorrente. Avaliar políticas PostgreSQL RLS como defesa adicional **sem substituir** os filtros e testes na aplicação.
6. Migrações Flyway versionadas e **backfill controlado** das linhas existentes para uma loja demo antes de aplicar `NOT NULL`; plano de rollback e backup testado.
7. Em fluxos de checkout, validar que cada item pertence à loja atual, além de preço, estoque e autorização.

## 3. Escopo da versão comercial mínima

| Módulo | Estado atual | Entrega necessária |
| --- | --- | --- |
| Catálogo + carrinho | MVP demonstrativo | Identidade da loja, fotos, URL pública e conteúdo institucional |
| Pedidos | `CREATED` (sem cobrança real) | Fluxo claro `CREATED` -> `AWAITING_PAYMENT` -> `PAID` / `CANCELLED` / `REFUNDED` |
| Pagamento | Inexistente | Checkout hospedado por PSP, webhooks autenticados/idempotentes; status `PAID` **somente** após confirmação do provedor |
| Entrega/frete | Inexistente | Piloto: retirada local + taxa de entrega por região configurada; depois cotação por CEP e etiqueta |
| Gestão | CRUD e analytics demo | Admin do comerciante, dados da loja, pedidos, política de atendimento, estoque |
| Operação | Docker local | Hospedagem HTTPS, domínio, observabilidade, backups restauráveis, e-mails transacionais |
| Privacidade | Ainda não auditada | Privacidade, retenção, controle de acesso, LGPD, logs e trilha de suporte |

**Escolha inicial para pagamento:** usar um checkout hospedado e **uma conta PSP do próprio lojista**. Não implementar split/comissão de marketplace na primeira entrega; isso adiciona fluxo OAuth, credenciais por vendedor e reconciliação específica. O recebimento pelo lojista e a assinatura do CommerceAI são cobranças separadas.

## 4. Regras de negócio

- RB-B2B-01: catálogo público exibe apenas itens ativos da loja correspondente.
- RB-B2B-02: estoque e preço são validados pelo backend no momento de criar o pedido.
- RB-B2B-03: pedido nasce `CREATED` ou `AWAITING_PAYMENT`; não contar como faturamento realizado.
- RB-B2B-04: somente webhook confirmado ou consulta segura ao PSP pode definir `PAID`; callback duplicado não duplica operação.
- RB-B2B-05: encomendas e envios são exclusivos de cada loja.
- RB-B2B-06: produtos com histórico não são removidos de forma a apagar/invalidar pedidos.
- RB-B2B-07: `MERCHANT_OWNER` administra sua loja; `STAFF` tem permissões limitadas; sem acesso cruzado.
- RB-B2B-08: valores de dashboard devem distinguir pedidos, recebimentos confirmados, estornos e cancelamentos.
- RB-B2B-09: dados e identidade do fornecedor, contato, políticas comerciais e direito de arrependimento devem ser visíveis nas páginas de venda aplicáveis.
- RB-B2B-10: alterações administrativas relevantes geram trilha de auditoria.

## 5. Proposta comercial exploratória (valores a validar com clientes)

| Pacote | Implantação (única) | Manutenção (mensal) | Escopo indicativo |
| --- | ---: | ---: | --- |
| Essencial | R$ 990 | R$ 149 | Loja pronta, catálogo inicial limitado, treinamento de uso, suporte por horário definido |
| Crescimento | R$ 1.990 | R$ 299 | Personalização visual, integração de pagamento, mais treinamento, relatórios e suporte prioritário |

**Não são preços de mercado verificados nem promessa de rentabilidade.** Antes de fechar contratos, calcular custo de hospedagem, domínio, PSP, suporte, tributos, terceiros, implantação, aquisição de clientes e margem. Formalizar limites de produtos cadastrados inicialmente, horas de suporte, responsabilidades pela logística, propriedade dos dados, disponibilidade, cobrança e cancelamento. Taxas transacionais do PSP podem variar e devem ser informadas separadamente.

**Exemplo de hipótese de receita recorrente:** 5 clientes no plano Essencial = 5 × R$ 149 = **R$ 745/mês brutos**, antes de todas as despesas. Receita de implantação é pontual, não MRR.

## 6. Entregas por marcos

### Marco A — Primeira loja branca (white label)
- [ ] Página `/loja` com marca do lojista, logo, paleta, WhatsApp, dados do fornecedor e políticas.
- [ ] Configuração `StoreProfile` administrável e produtos adequados ao catálogo real.
- [ ] Testes de acessibilidade/responsividade, HTTPS, sem dados demo.
- [ ] Proteger acesso ADMIN e desativar criação de ADMIN via cadastro público.

### Marco B — Venda real controlada
- [ ] Checkout hospedado em ambiente de testes, com pedido persistido no banco.
- [ ] Webhook verificado, idempotente, logs de conciliação e alteração segura de status.
- [ ] Retirada no local e entrega local configurável, preço total informado antes da confirmação.
- [ ] Testes de compra paga, falhada, duplicada, reembolsada e tentativas fraudulentas.

### Marco C — 1 comerciante piloto
- [ ] Implantação de instância própria com domínio, HTTPS, backup e recuperação testados.
- [ ] Treinamento operacional, suporte e políticas publicadas.
- [ ] Teste integral de compra com consentimento do comerciante.
- [ ] Coletar feedback e medir métricas reais sem inventar conversão/receita.

### Marco D — Repetibilidade comercial
- [ ] Automação de provisionamento de novas lojas isoladas.
- [ ] Painel de administração da plataforma (assinatura, status da loja e suporte).
- [ ] Decidir com evidências se vale migrar para SaaS multi-tenant.
- [ ] Somente após testes completos, lançar plano multi-tenant.

## 7. Restrições de segurança, privacidade e operação

Antes de colocar lojas de terceiros online:
- Fazer revisão de segurança de autenticação, permissões, exposição de dados, CORS, rate limiting e backups.
- Remover contas, senhas e chaves demonstrativas do ambiente de produção; guardar segredos fora do Git.
- Prover informações claras do fornecedor, oferta, contato e exercício do arrependimento quando aplicável (Decreto 7.962/2013).
- Revisar LGPD, tratamento de dados e medidas de segurança; aplicar os controles e acordos contratuais adequados ao papel de cada parte.
- Garantir operação e atendimento humano para problemas reais de pagamento, expedição, devolução e chargeback.
- Não apresentar recomendações determinísticas como IA personalizada, nem pedidos `CREATED` como faturamento.

## 8. Links oficiais para implementação

- Decreto de comércio eletrônico: https://www.planalto.gov.br/ccivil_03/_ato2011-2014/2013/decreto/d7962.htm
- ANPD — segurança para pequenos agentes: https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/guia-orientativo-sobre-seguranca-da-informacao-para-agentes-de-tratamento-de-pequeno-porte
- Mercado Pago — checkout marketplace/split (para **etapa futura**, não MVP): https://www.mercadopago.com.br/developers/pt/docs/split-payments/split-1-1/integration-configuration/integrate-marketplace

## 9. Critérios de aceite do piloto

O piloto comercial só poderá ser considerado pronto quando um usuário conseguir visitar a loja pelo domínio do comerciante, selecionar produtos reais, ver frete e total, pagar em um checkout verificado, receber confirmação legítima e visualizar seu pedido; o lojista deverá confirmar e cumprir o pedido, enquanto outra loja — caso exista — não poderá acessar seus dados. A operação deverá ter recuperação de backups, logs, suporte e atendimento definido.
