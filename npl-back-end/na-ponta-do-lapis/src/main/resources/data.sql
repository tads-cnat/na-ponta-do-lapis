-- SQL Usuario
INSERT INTO usuario (nome, email, username, senha, foto_perfil, papel)
VALUES ('Lucas Henrique', 'lucas@gmail.com', 'Lucaasshq', '123456', 'http:foto.com', 'ADMIN_SITE');
INSERT INTO usuario (nome, username, email, senha, foto_perfil, papel)
VALUES ('Bruno', 'Brunozz', 'bruno@gmail.com', '123456', 'http:foto.com', 'ADMIN_FAMILIA');
INSERT INTO usuario (nome, username, email, senha, foto_perfil, papel)
VALUES ('Eduardo', 'Dudurant', 'dudu@gmail.com', '123456', 'http:foto.com', 'ADMIN_SITE');
INSERT INTO usuario (nome, username, email, senha, foto_perfil, papel)
VALUES ('Pedro', 'Predo', 'pedro@gmail.com', '123456', 'http:foto.com', 'USUARIO');
-- SQL Categoria
INSERT INTO tipo_categoria (nome)
VALUES ('Alimentação'),
       ('Transporte'),
       ('Lazer'),
       ('Saúde'),
       ('Educação');

-- SQL Conta Financeira
INSERT INTO conta_financeira (saldo, usuario_id, nome, tipo)
VALUES  (100, 1, 'Banco Inter','DEBITO'),
        (50000000.50, 2, 'Banco Master','CREDITO'),
        (0, 3, 'Santander', 'CREDITO_DEBITO'),
        (150, 4, 'Itaú', 'DEBITO'),
        (3000, 3, 'Nubank', 'DEBITO'),
        (10, 2, 'BB', 'CREDITO'),
        (1, 1, 'PicPay', 'CREDITO');










