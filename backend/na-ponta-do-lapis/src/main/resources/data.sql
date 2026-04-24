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
