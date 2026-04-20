-- SQL Usuario
INSERT INTO usuario (id, nome, email, username, senha, foto_perfil, papel)
VALUES 
    (1, 'Lucas Henrique', 'lucas@gmail.com', 'Lucaasshq', '123456', 'http://foto.com', 'ADMIN_SITE'),
    (2, 'Bruno', 'bruno@gmail.com', 'Brunozz', '123456', 'http://foto.com', 'ADMIN_FAMILIA'),
    (3, 'Eduardo', 'dudu@gmail.com', 'Dudurant', '123456', 'http://foto.com', 'ADMIN_SITE'),
    (4, 'Pedro', 'pedro@gmail.com', 'Predo', '123456', 'http://foto.com', 'USUARIO')
ON CONFLICT (id) DO NOTHING;

-- SQL Categoria
INSERT INTO tipo_categoria (id, nome)
VALUES 
    (1, 'Alimentação'),
    (2, 'Transporte'),
    (3, 'Lazer'),
    (4, 'Saúde'),
    (5, 'Educação')
ON CONFLICT (id) DO NOTHING;

-- SQL Conta Financeira
INSERT INTO conta_financeira (id, saldo, usuario_id, nome, tipo)
VALUES  
    (1, 100, 1, 'Banco Inter', 'DEBITO'),
    (2, 50000000.50, 2, 'Banco Master', 'CREDITO'),
    (3, 0, 3, 'Santander', 'CREDITO_DEBITO'),
    (4, 150, 4, 'Itaú', 'DEBITO'),
    (5, 3000, 3, 'Nubank', 'DEBITO'),
    (6, 10, 2, 'BB', 'CREDITO'),
    (7, 1, 1, 'PicPay', 'CREDITO')
ON CONFLICT (id) DO NOTHING;