-- SQL Usuario
INSERT INTO usuario (id, nome, email, username, senha, foto_perfil, papel)
VALUES 
    (1, 'Lucas Henrique', 'lucas@gmail.com', 'Lucaasshq', '123456', 'http://foto.com', 'ADMIN_SITE'),
    (2, 'Bruno', 'bruno@gmail.com', 'Brunozz', '123456', 'http://foto.com', 'ADMIN_FAMILIA'),
    (3, 'Eduardo', 'dudu@gmail.com', 'Dudurant', '123456', 'http://foto.com', 'ADMIN_SITE'),
    (4, 'Pedro', 'pedro@gmail.com', 'Predo', '123456', 'http://foto.com', 'USUARIO');

-- SQL Categoria
INSERT INTO tipo_categoria (id, nome)
VALUES 
    (1, 'Alimentação'),
    (2, 'Transporte'),
    (3, 'Lazer'),
    (4, 'Saúde'),
    (5, 'Educação');

-- SQL Conta Financeira
INSERT INTO conta_financeira (id, nome, saldo, tipo, usuario_id)
VALUES  
    (1, 'Banco Inter', 100, 'DEBITO', 1),
    (2, 'Banco Master', 50000000.50, 'CREDITO', 2),
    (3, 'Santander', 0, 'CREDITO_DEBITO', 3),
    (4, 'Itaú', 150, 'DEBITO', 4),
    (5, 'Nubank', 3000, 'DEBITO', 3),
    (6, 'BB', 10, 'CREDITO', 2),
    (7, 'PicPay', 1, 'CREDITO', 1);


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
