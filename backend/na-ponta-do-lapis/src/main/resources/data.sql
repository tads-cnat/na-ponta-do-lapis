--SQL Familia
INSERT INTO familia (id, nome)
VALUES
    (1, 'Família Silva'),
    (2, 'Família Oliveira'),
    (3, 'Família Souza');

-- SQL Usuario as senhas são 123456
INSERT INTO usuario (nome, email, username, senha, foto_perfil, familia_id, papel)
VALUES 
    ('Lucas Henrique', 'lucas@gmail.com', 'Lucaasshq', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 1, 'ADMIN_SITE'),
    ('Bruno', 'bruno@gmail.com', 'Brunozz', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 1,'ADMIN_FAMILIA'),
    ('Eduardo', 'dudu@gmail.com', 'Dudurant', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 2, 'ADMIN_SITE'),
    ( 'Pedro', 'pedro@gmail.com', 'Predo', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 3, 'USUARIO');

INSERT INTO usuario (nome, email, username, senha, foto_perfil, papel)
VALUES
    ( 'test', 'test@gmail.com', 'test', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 'USUARIO');

-- SQL Categoria
INSERT INTO tipo_categoria (nome, usuario_id)
VALUES
    -- Categorias do Lucas (ID 1)
    ('Supermercado', 1),
    ('Restaurantes', 1),
    ('Academia', 1),
    ('Assinaturas Streaming', 1),

    -- Categorias do Bruno (ID 2)
    ('Combustível', 2),
    ('Manutenção Carro', 2),
    ('Estacionamento', 2),
    ('Pedágio', 2),

    -- Categorias do Eduardo (ID 3)
    ('Cinema', 3),
    ('Jogos Online', 3),
    ('Viagens', 3),
    ('Hospedagem', 3),

    -- Categorias do Pedro (ID 4)
    ('Farmácia', 4),
    ('Consultas Médicas', 4),
    ('Exames', 4),
    ('Suplementos', 4),

    -- Categorias do Test (ID 5)
    ('Cursos Online', 5),
    ('Livros', 5),
    ('Material Escolar', 5),
    ('Mensalidade Faculdade', 5);

-- SQL Conta Financeira
INSERT INTO conta_financeira (id, nome, saldo, usuario_id)
VALUES
    (1, 'Banco Inter', 100, 1),
    (2, 'Banco Master', 50000000.50, 2),
    (3, 'Santander', 0, 3),
    (4, 'Itaú', 150, 4),
    (5, 'Nubank', 3000, 3),
    (6, 'BB', 10, 2),
    (7, 'PicPay', 1, 1);
INSERT INTO transacao (descricao, valor, id_categoria, estado, tipo, data_hora, id_conta_financeira)
VALUES
-- Usuário 1 (Lucas): Contas 1 e 7
('Salário Mensal', 5000.00, 5, 'REALIZADA', 'RECEITA', '2026-05-01 08:00:00', 1),
('Supermercado', 450.50, 1, 'REALIZADA', 'DESPESA', '2026-05-01 18:30:00', 1),
('Gasolina', 200.00, 2, 'REALIZADA', 'DESPESA', '2026-05-02 10:00:00', 1),
('Netflix', 55.90, 3, 'REALIZADA', 'DESPESA', '2026-05-03 09:00:00', 7),
('Jantar Japonês', 120.00, 1, 'REALIZADA', 'DESPESA', '2026-05-03 21:00:00', 7),
('Rendimento CDI', 15.20, 5, 'REALIZADA', 'RECEITA', '2026-05-04 10:00:00', 1),
('Farmácia', 85.00, 4, 'PENDENTE', 'DESPESA', '2026-05-05 14:00:00', 1),
('Uber Faculdade', 25.00, 2, 'REALIZADA', 'DESPESA', '2026-05-02 19:00:00', 1),
('Livros Java', 150.00, 5, 'REALIZADA', 'DESPESA', '2026-05-04 15:00:00', 7),
('Venda de Teclado Antigo', 300.00, 3, 'REALIZADA', 'RECEITA', '2026-05-05 11:00:00', 1),

-- Usuário 2 (Bruno): Contas 2 e 6
('Dividendo Ações', 15000.00, 5, 'REALIZADA', 'RECEITA', '2026-05-01 09:00:00', 2),
('Compra de Luxo', 5000.00, 3, 'REALIZADA', 'DESPESA', '2026-05-01 14:00:00', 2),
('Manutenção Carro', 1200.00, 2, 'REALIZADA', 'DESPESA', '2026-05-02 11:00:00', 2),
('Almoço Executivo', 85.00, 1, 'REALIZADA', 'DESPESA', '2026-05-02 13:00:00', 6),
('Café Padaria', 12.50, 1, 'REALIZADA', 'DESPESA', '2026-05-03 08:00:00', 6),
('Mensalidade Software', 200.00, 5, 'REALIZADA', 'DESPESA', '2026-05-03 10:00:00', 2),
('Viagem Fim de Semana', 2500.00, 3, 'REALIZADA', 'DESPESA', '2026-05-04 17:00:00', 2),
('Freelance Backend', 4000.00, 5, 'REALIZADA', 'RECEITA', '2026-05-05 09:00:00', 2),
('Seguro Saúde', 800.00, 4, 'REALIZADA', 'DESPESA', '2026-05-05 10:00:00', 2),
('Depósito em Dinheiro', 1000.00, 5, 'REALIZADA', 'RECEITA', '2026-05-05 15:00:00', 6),

-- Usuário 3 (Eduardo): Contas 3 e 5
('Aporte Inicial', 3000.00, 5, 'REALIZADA', 'RECEITA', '2026-05-01 08:00:00', 5),
('Aluguel Escritório', 1200.00, 5, 'REALIZADA', 'DESPESA', '2026-05-01 10:00:00', 5),
('Internet Fibra', 120.00, 5, 'REALIZADA', 'DESPESA', '2026-05-02 09:00:00', 5),
('Rodízio de Pizza', 90.00, 1, 'REALIZADA', 'DESPESA', '2026-05-02 20:00:00', 5),
('Cinema', 45.00, 3, 'REALIZADA', 'DESPESA', '2026-05-03 16:00:00', 5),
('Consulta Dentista', 250.00, 4, 'REALIZADA', 'DESPESA', '2026-05-04 14:00:00', 3),
('Curso Udemy', 34.90, 5, 'REALIZADA', 'DESPESA', '2026-05-04 19:00:00', 3),
('Reembolso Viagem', 450.00, 2, 'REALIZADA', 'RECEITA', '2026-05-05 10:00:00', 5),
('Padaria Manhã', 22.00, 1, 'REALIZADA', 'DESPESA', '2026-05-05 07:30:00', 5),
('Spotify Família', 34.90, 3, 'PENDENTE', 'DESPESA', '2026-05-05 20:00:00', 3),

-- Usuário 4 (Pedro): Conta 4
('Mesada', 500.00, 5, 'REALIZADA', 'RECEITA', '2026-05-01 12:00:00', 4),
('Lanche Faculdade', 18.00, 1, 'REALIZADA', 'DESPESA', '2026-05-01 21:00:00', 4),
('Recarga Celular', 40.00, 5, 'REALIZADA', 'DESPESA', '2026-05-02 10:00:00', 4),
('Passagem Ônibus', 4.50, 2, 'REALIZADA', 'DESPESA', '2026-05-02 12:00:00', 4),
('Sorvete', 12.00, 1, 'REALIZADA', 'DESPESA', '2026-05-03 15:00:00', 4),
('Corte de Cabelo', 50.00, 3, 'REALIZADA', 'DESPESA', '2026-05-04 17:00:00', 4),
('Venda de Jogo', 80.00, 3, 'REALIZADA', 'RECEITA', '2026-05-05 09:00:00', 4),
('X-Burguer', 35.00, 1, 'PENDENTE', 'DESPESA', '2026-05-05 21:00:00', 4),
('Papelaria', 25.00, 5, 'REALIZADA', 'DESPESA', '2026-05-04 08:00:00', 4),
('Presente Amigo', 60.00, 3, 'REALIZADA', 'DESPESA', '2026-05-05 14:00:00', 4),
('Bônus Performance', 1000.00, 5, 'REALIZADA', 'RECEITA', '2026-05-01 08:00:00', 1),
('Troca de Óleo', 180.00, 2, 'REALIZADA', 'DESPESA', '2026-05-02 09:00:00', 2),
('Vitamina C', 30.00, 4, 'REALIZADA', 'DESPESA', '2026-05-03 11:00:00', 5),
('Salgado e Refri', 15.00, 1, 'REALIZADA', 'DESPESA', '2026-05-04 16:00:00', 4),
('Uber Cinema', 20.00, 2, 'REALIZADA', 'DESPESA', '2026-05-01 19:00:00', 1),
('Conserto Celular', 400.00, 5, 'REALIZADA', 'DESPESA', '2026-05-02 14:00:00', 2),
('Venda de Camisa', 50.00, 3, 'REALIZADA', 'RECEITA', '2026-05-03 10:00:00', 5),
('Açaí', 25.00, 1, 'REALIZADA', 'DESPESA', '2026-05-04 15:00:00', 4),
('Pizza Família', 85.00, 1, 'PENDENTE', 'DESPESA', '2026-05-05 20:30:00', 1),
('Dividendos Nubank', 5.40, 5, 'REALIZADA', 'RECEITA', '2026-05-05 09:00:00', 5);

--- SQL Meta
INSERT INTO tipo_meta(id, nome)
VALUES
    (1, 'ECONOMIA'),
    (2, 'LIMITE_GASTO'),
    (3, 'INVESTIMENTO'),
    (4, 'DIVIDA');
INSERT INTO meta(id, nome, descricao, valor, foto_url, data_inicio, tipo_meta_id)
VALUES
    (1, 'Reserva de Emergência', 'Guardar dinheiro para imprevistos', 5000.00, 'https://link-da-foto.com/reserva.png', '2026-12-31', 1),
    (2, 'Viagem de Férias', 'Juntar dinheiro para viajar no final do ano', 3000.00, 'https://link-da-foto.com/viagem.png', '2026-11-30', 1),
    (3, 'Limite Alimentação', 'Controlar gastos com comida', 800.00, 'https://link-da-foto.com/comida.png', '2026-06-30', 2),
    (4, 'Limite Lazer', 'Evitar gastar demais com lazer', 500.00, 'https://link-da-foto.com/lazer.png', '2026-06-30', 2),
    (5, 'Investimento Mensal', 'Investir mensalmente em renda fixa', 1000.00, 'https://link-da-foto.com/investimento.png', '2026-12-31', 3),
    (6, 'Quitar Cartão', 'Pagar dívida do cartão de crédito', 2000.00, 'https://link-da-foto.com/cartao.png', '2026-08-31', 4),
    (7, 'Comprar Notebook', 'Guardar dinheiro para comprar um notebook novo', 4000.00, 'https://link-da-foto.com/notebook.png', '2026-10-15', 1),
    (8, 'Academia', 'Controlar gastos com academia', 150.00, 'https://link-da-foto.com/academia.png', '2026-12-31', 2),
    (9, 'Fundo de Investimento', 'Aumentar patrimônio com investimentos', 10000.00, 'https://link-da-foto.com/fundo.png', '2027-12-31', 3),
    (10, 'Quitar Empréstimo', 'Finalizar pagamento do empréstimo pessoal', 7000.00, 'https://link-da-foto.com/emprestimo.png', '2027-06-30', 4);