CREATE TABLE products (id BIGSERIAL PRIMARY KEY, name VARCHAR(150) NOT NULL, category VARCHAR(70) NOT NULL, description VARCHAR(500) NOT NULL, price NUMERIC(12,2) NOT NULL CHECK(price >= 0), stock INTEGER NOT NULL CHECK(stock >= 0), image_url VARCHAR(500));
CREATE TABLE customer_orders (id BIGSERIAL PRIMARY KEY, customer_name VARCHAR(120) NOT NULL, customer_email VARCHAR(200) NOT NULL, status VARCHAR(30) NOT NULL, total NUMERIC(12,2) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE order_items (id BIGSERIAL PRIMARY KEY, order_id BIGINT NOT NULL REFERENCES customer_orders(id), product_id BIGINT NOT NULL REFERENCES products(id), product_name VARCHAR(150) NOT NULL, quantity INTEGER NOT NULL CHECK(quantity > 0), unit_price NUMERIC(12,2) NOT NULL);
INSERT INTO products(name,category,description,price,stock,image_url) VALUES
('Headset Pro','Eletrônicos','Fone premium para trabalho e jogos',289.90,32,NULL),
('Teclado Mecânico','Eletrônicos','Teclado mecânico compacto RGB',399.00,18,NULL),
('Mouse Sem Fio','Eletrônicos','Mouse ergonômico e silencioso',149.90,42,NULL),
('Garrafa Térmica','Casa','Garrafa inox 750 ml',89.90,65,NULL),
('Mochila Tech','Acessórios','Mochila com compartimento para notebook',219.00,25,NULL),
('Suporte Notebook','Acessórios','Suporte de mesa ajustável',119.00,34,NULL);
