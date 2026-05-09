--SQL Familia
INSERT INTO familia (id, nome)
VALUES
    (1, 'Família Silva'),
    (2, 'Família Oliveira'),
    (3, 'Família Souza');

-- SQL Usuario as senhas são 123456
INSERT INTO usuario (id ,nome, email, username, senha, foto_perfil, familia_id, papel)
VALUES
    (1,'Lucas Henrique', 'lucas@gmail.com', 'Lucaasshq', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 1, 'USUARIO'),
    (2,'Bruno', 'bruno@gmail.com', 'Brunozz', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 1,'ADMIN_FAMILIA'),
    (3,'Eduardo', 'dudu@gmail.com', 'Dudurant', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 2, 'ADMIN_SITE'),
    ( 4,'Pedro', 'pedro@gmail.com', 'Predo', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'http://foto.com', 3, 'USUARIO');

INSERT INTO usuario (id, nome, email, username, senha, papel) VALUES
    ( 5,'ADMIN', 'admin@gmail.com', 'ademirDaSilva', '$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6', 'USUARIO');

-- SQL Categoria
INSERT INTO tipo_categoria (nome)
VALUES
    ('Supermercado' ),
    ('Restaurantes'),
    ('Academia'),
    ('Assinaturas Streaming'),
    ('Combustível'),
    ('Manutenção Carro'),
    ('Estacionamento'),
    ('Pedágio'),
    ('Cinema'),
    ('Jogos Online'),
    ('Viagens'),
    ('Hospedagem'),
    ('Farmácia'),
    ('Consultas Médicas'),
    ('Exames'),
    ('Suplementos'),
    ('Cursos Online'),
    ('Livros'),
    ('Material Escolar'),
    ('Mensalidade Faculdade');

-- SQL Conta Financeira
INSERT INTO conta_financeira (id, nome, saldo, cor, usuario_id)
VALUES
    (1, 'Banco Inter', 100, 1),
    (2, 'Banco Master', 50000000.50, 2),
    (3, 'Santander', 0, 3),
    (4, 'Itaú', 150, 4),
    (5, 'Nubank', 3000, 3),
    (6, 'BB', 10, 2),
    (7, 'PicPay', 1, 1);

-- Marcadores para o Usuário 1 (Lucas Henrique)
INSERT INTO marcador (id ,nome, cor, usuario_id) VALUES
                                                 (1,'PIX', '#2196F3', 1),
                                                 (2,'CREDITO', '#9C27B0', 1),
                                                 (3,'DEBITO', '#F44336', 1);


-- Marcadores para o Usuário 2 (Bruno)
INSERT INTO marcador (id ,nome, cor, usuario_id) VALUES
                                                 (4,'PIX', '#00BCD4', 2),
                                                 (5,'CREDITO', '#3F51B5', 2),
                                                 (6,'DEBITO', '#000000', 2);


-- Marcadores para o Usuário 3 (Eduardo)
INSERT INTO marcador (id, nome, cor, usuario_id) VALUES
                                                 (7,'PIX', '#673AB7', 3),
                                                 (8,'CREDITO', '#E91E63', 3),
                                                 (9,'DEBITO', '#009688', 3);

-- Marcadores para o Usuário 4 (Pedro)
INSERT INTO marcador (id,nome, cor, usuario_id) VALUES
                                                 (10,'PIX', '#3F51B5', 4),
                                                 (11,'CREDITO', '#FF5722', 4),
                                                 (12,'DEBITO', '#9E9E9E', 4);

-- Marcadores para o Usuário 5 (test/ADMIN)
-- Nota: Como você inseriu dois usuários com ID 5, este SQL assume o último ID 5 válido.
INSERT INTO marcador (id, nome, cor, usuario_id) VALUES
                                                 (13,'PIX', '#212121', 5),
                                                 (14,'CREDITO', '#D32F2F', 5),
                                                 (15,'DEBITO', '#1976D2', 5);

INSERT INTO transacao (id, descricao, valor, id_categoria, estado, tipo, data_hora, id_conta_financeira, marcador_id)
VALUES
-- Usuário 1 (Lucas): Marcadores 1 (PIX), 2 (CREDITO), 3 (DEBITO)
(1, 'Salário Mensal', 5000.00, 17, 'REALIZADA', 'RECEITA', '2026-05-01 08:00:00', 1, 1),
(2, 'Supermercado', 450.50, 1, 'REALIZADA', 'DESPESA', '2026-05-01 18:30:00', 1, 3),
(3, 'Gasolina', 200.00, 5, 'REALIZADA', 'DESPESA', '2026-05-02 10:00:00', 1, 3),
(4, 'Netflix', 55.90, 4, 'REALIZADA', 'DESPESA', '2026-05-03 09:00:00', 7, 2),
(5, 'Jantar Japonês', 120.00, 2, 'REALIZADA', 'DESPESA', '2026-05-03 21:00:00', 7, 2),
(6, 'Rendimento CDI', 15.20, 17, 'REALIZADA', 'RECEITA', '2026-05-04 10:00:00', 1, 1),
(7, 'Farmácia', 85.00, 13, 'PENDENTE', 'DESPESA', '2026-05-05 14:00:00', 1, 3),
(8, 'Uber Faculdade', 25.00, 5, 'REALIZADA', 'DESPESA', '2026-05-02 19:00:00', 1, 3),
(9, 'Livros Java', 150.00, 18, 'REALIZADA', 'DESPESA', '2026-05-04 15:00:00', 7, 2),
(10, 'Venda de Teclado Antigo', 300.00, 10, 'REALIZADA', 'RECEITA', '2026-05-05 11:00:00', 1, 1),
(11, 'Bônus Performance', 1000.00, 17, 'REALIZADA', 'RECEITA', '2026-05-01 08:00:00', 1, 1),
(12, 'Uber Cinema', 20.00, 5, 'REALIZADA', 'DESPESA', '2026-05-01 19:00:00', 1, 3),
(13, 'Pizza Família', 85.00, 1, 'PENDENTE', 'DESPESA', '2026-05-05 20:30:00', 1, 3),

-- Usuário 2 (Bruno): Marcadores 4 (PIX), 5 (CREDITO), 6 (DEBITO)
(14, 'Dividendo Ações', 15000.00, 17, 'REALIZADA', 'RECEITA', '2026-05-01 09:00:00', 2, 4),
(15, 'Compra de Luxo', 5000.00, 9, 'REALIZADA', 'DESPESA', '2026-05-01 14:00:00', 2, 5),
(16, 'Manutenção Carro', 1200.00, 6, 'REALIZADA', 'DESPESA', '2026-05-02 11:00:00', 2, 6),
(17, 'Almoço Executivo', 85.00, 2, 'REALIZADA', 'DESPESA', '2026-05-02 13:00:00', 6, 6),
(18, 'Café Padaria', 12.50, 2, 'REALIZADA', 'DESPESA', '2026-05-03 08:00:00', 6, 6),
(19, 'Mensalidade Software', 200.00, 17, 'REALIZADA', 'DESPESA', '2026-05-03 10:00:00', 2, 5),
(20, 'Viagem Fim de Semana', 2500.00, 11, 'REALIZADA', 'DESPESA', '2026-05-04 17:00:00', 2, 5),
(21, 'Freelance Backend', 4000.00, 17, 'REALIZADA', 'RECEITA', '2026-05-05 09:00:00', 2, 4),
(22, 'Seguro Saúde', 800.00, 14, 'REALIZADA', 'DESPESA', '2026-05-05 10:00:00', 2, 5),
(23, 'Depósito em Dinheiro', 1000.00, 17, 'REALIZADA', 'RECEITA', '2026-05-05 15:00:00', 6, 4),
(24, 'Troca de Óleo', 180.00, 6, 'REALIZADA', 'DESPESA', '2026-05-02 09:00:00', 2, 6),
(25, 'Conserto Celular', 400.00, 17, 'REALIZADA', 'DESPESA', '2026-05-02 14:00:00', 2, 6),

-- Usuário 3 (Eduardo): Marcadores 7 (PIX), 8 (CREDITO), 9 (DEBITO)
(26, 'Aporte Inicial', 3000.00, 17, 'REALIZADA', 'RECEITA', '2026-05-01 08:00:00', 5, 7),
(27, 'Aluguel Escritório', 1200.00, 17, 'REALIZADA', 'DESPESA', '2026-05-01 10:00:00', 5, 9),
(28, 'Internet Fibra', 120.00, 17, 'REALIZADA', 'DESPESA', '2026-05-02 09:00:00', 5, 8),
(29, 'Rodízio de Pizza', 90.00, 2, 'REALIZADA', 'DESPESA', '2026-05-02 20:00:00', 5, 9),
(30, 'Cinema', 45.00, 9, 'REALIZADA', 'DESPESA', '2026-05-03 16:00:00', 5, 8),

-- Usuário 4 (Pedro): Marcadores 10 (PIX), 11 (CREDITO), 12 (DEBITO)
(39, 'Mesada', 500.00, 17, 'REALIZADA', 'RECEITA', '2026-05-01 12:00:00', 4, 10),
(40, 'Lanche Faculdade', 18.00, 2, 'REALIZADA', 'DESPESA', '2026-05-01 21:00:00', 4, 12),
(46, 'X-Burguer', 35.00, 2, 'PENDENTE', 'DESPESA', '2026-05-05 21:00:00', 4, 12);


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

-- Sincroniza a sequência do ID do Usuário
SELECT setval(pg_get_serial_sequence('usuario', 'id'), coalesce(MAX(id), 1)) FROM usuario;
SELECT setval(pg_get_serial_sequence('transacao', 'id'), coalesce(max(id), 0) + 1, false) FROM transacao;
