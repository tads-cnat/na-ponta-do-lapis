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
INSERT INTO conta_financeira (nome, saldo, tipo, usuario)
VALUES ('Banco Inter', 100, 'DEBITO', 1);

INSERT INTO conta_financeira (nome, saldo, tipo, usuario)
VALUES ('Banco Master', 50000000.50, 'CREDITO', 2);

INSERT INTO conta_financeira (nome, saldo, tipo, usuario)
VALUES ('Santander', 0, 'CREDITO_DEBITO', 3);

INSERT INTO conta_financeira (nome, saldo, tipo, usuario)
VALUES ('Itaú', 150, 'DEBITO', 4);

INSERT INTO conta_financeira (nome, saldo, tipo, usuario)
VALUES ('Nubank', 3000, 'DEBITO', 3);

INSERT INTO conta_financeira (nome, saldo, tipo, usuario)
VALUES ('BB', 10, 'CREDITO', 2);

INSERT INTO conta_financeira (nome, saldo, tipo, usuario)
VALUES ('PicPay', 1, 'CREDITO', 1);