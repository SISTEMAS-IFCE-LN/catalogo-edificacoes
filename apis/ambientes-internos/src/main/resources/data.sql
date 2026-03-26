-- Seed de desenvolvimento: 120 ambientes

INSERT INTO localizacao (bloco, unidade, andar)
SELECT
    CASE MOD(x - 1, 8)
        WHEN 0 THEN 'BLOCO_1'
        WHEN 1 THEN 'BLOCO_2'
        WHEN 2 THEN 'BLOCO_3'
        WHEN 3 THEN 'BLOCO_4'
        WHEN 4 THEN 'BLOCO_5'
        WHEN 5 THEN 'BLOCO_6'
        WHEN 6 THEN 'BLOCO_7'
        ELSE 'BLOCO_8'
    END,
    CASE MOD(FLOOR((x - 1) / 8), 3)
        WHEN 0 THEN 'SEDE'
        WHEN 1 THEN 'CIDADE_ALTA'
        ELSE 'UEPE'
    END,
    MOD(FLOOR((x - 1) / 24), 4)
FROM SYSTEM_RANGE(1, 96);

INSERT INTO ambiente (nome, localizacao_id, capacidade, tipo, informacao_adicional, status)
SELECT
    CASE
        WHEN MOD(r.x, 12) = 0 OR MOD(r.x, 12) = 1 THEN CONCAT(
            'Sala de Aula ',
            CASE MOD(r.x - 1, 10)
                WHEN 0 THEN '01'
                WHEN 1 THEN '02'
                WHEN 2 THEN '03'
                WHEN 3 THEN '04'
                WHEN 4 THEN '05'
                WHEN 5 THEN '06'
                WHEN 6 THEN '07'
                WHEN 7 THEN '08'
                WHEN 8 THEN '09'
                ELSE '10'
            END
        )
        ELSE CONCAT(
            CASE MOD(r.x, 12)
                WHEN 2 THEN 'Laboratorio de Eletronica'
                WHEN 3 THEN 'Laboratorio de Informatica'
                WHEN 4 THEN 'Laboratorio de Quimica'
                WHEN 5 THEN 'Biblioteca'
                WHEN 6 THEN 'Auditorio'
                WHEN 7 THEN 'Departamento de Administracao'
                WHEN 8 THEN 'Coordenacao de Cursos'
                WHEN 9 THEN 'Sala dos Professores'
                WHEN 10 THEN 'Secretaria Academica'
                ELSE 'Sala Administrativa'
            END,
            ' ',
            CASE
                WHEN r.x < 10 THEN CONCAT('00', r.x)
                WHEN r.x < 100 THEN CONCAT('0', r.x)
                ELSE CAST(r.x AS VARCHAR)
            END
        )
    END,
    l.id,
    20 + MOD(r.x, 41),
    CASE MOD(r.x, 12)
        WHEN 0 THEN 'SALA_AULA'
        WHEN 1 THEN 'SALA_AULA'
        WHEN 2 THEN 'LABORATORIO'
        WHEN 3 THEN 'LABORATORIO_INFORMATICA'
        WHEN 4 THEN 'LABORATORIO'
        WHEN 5 THEN 'BIBLIOTECA'
        WHEN 6 THEN 'AUDITORIO'
        WHEN 8 THEN 'SALA_COORDENACAO'
        WHEN 9 THEN 'SALA_PROFESSORES'
        ELSE 'SALA_ADMINISTRATIVA'
    END,
    CONCAT('Registro seed dev #', r.x),
    'NAO_PUBLICADO'
FROM SYSTEM_RANGE(1, 120) r
JOIN localizacao l
    ON l.bloco = CASE MOD(MOD(r.x - 1, 96), 8)
        WHEN 0 THEN 'BLOCO_1'
        WHEN 1 THEN 'BLOCO_2'
        WHEN 2 THEN 'BLOCO_3'
        WHEN 3 THEN 'BLOCO_4'
        WHEN 4 THEN 'BLOCO_5'
        WHEN 5 THEN 'BLOCO_6'
        WHEN 6 THEN 'BLOCO_7'
        ELSE 'BLOCO_8'
    END
    AND l.unidade = CASE MOD(FLOOR(MOD(r.x - 1, 96) / 8), 3)
        WHEN 0 THEN 'SEDE'
        WHEN 1 THEN 'CIDADE_ALTA'
        ELSE 'UEPE'
    END
    AND l.andar = MOD(FLOOR(MOD(r.x - 1, 96) / 24), 4);

INSERT INTO geometria (tipo, base, altura, repeticao, ambiente_id)
SELECT
    'RETANGULAR',
    CAST(
        CASE a.tipo
            WHEN 'AUDITORIO' THEN 20.00
            WHEN 'BIBLIOTECA' THEN 10.00
            ELSE 9.50
        END AS DECIMAL(9, 2)
    ),
    CAST(
        CASE a.tipo
            WHEN 'AUDITORIO' THEN 50.00
            WHEN 'BIBLIOTECA' THEN 20.00
            ELSE 5.40
        END AS DECIMAL(9, 2)
    ),
    1,
    a.id
FROM ambiente a;

INSERT INTO geometria (tipo, base, altura, repeticao, ambiente_id)
SELECT
    'RETANGULAR',
    CAST(0.80 AS DECIMAL(9, 2)),
    CAST(2.10 AS DECIMAL(9, 2)),
    1,
    a.id
FROM ambiente a;

INSERT INTO geometria (tipo, base, altura, repeticao, ambiente_id)
SELECT
    'RETANGULAR',
    CAST(1.10 AS DECIMAL(9, 2)),
    CAST(1.50 AS DECIMAL(9, 2)),
    1,
    a.id
FROM ambiente a;

INSERT INTO geometria (tipo, base, altura, repeticao, ambiente_id)
SELECT
    'RETANGULAR',
    CAST(0.60 AS DECIMAL(9, 2)),
    CAST(0.60 AS DECIMAL(9, 2)),
    1,
    a.id
FROM ambiente a;

INSERT INTO esquadria (tipo, geometria_id, material, informacao_adicional, ambiente_id, altura_peitoril)
SELECT
    'PORTA',
    g.id,
    'MADEIRA_FICHA',
    'Porta padrao 1',
    a.id,
    CAST(0.00 AS DECIMAL(9, 2))
FROM ambiente a
JOIN geometria g
    ON g.ambiente_id = a.id
    AND g.base = CAST(0.80 AS DECIMAL(9, 2))
    AND g.altura = CAST(2.10 AS DECIMAL(9, 2));

INSERT INTO esquadria (tipo, geometria_id, material, informacao_adicional, ambiente_id, altura_peitoril)
SELECT
    'JANELA',
    g.id,
    'ALUMINIO_VIDRO',
    'Janela padrao 1',
    a.id,
    CAST(0.90 AS DECIMAL(9, 2))
FROM ambiente a
JOIN geometria g
    ON g.ambiente_id = a.id
    AND g.base = CAST(1.10 AS DECIMAL(9, 2))
    AND g.altura = CAST(1.50 AS DECIMAL(9, 2));

INSERT INTO esquadria (tipo, geometria_id, material, informacao_adicional, ambiente_id, altura_peitoril)
SELECT
    'JANELA',
    g.id,
    'ALUMINIO_VIDRO',
    'Janela padrao 2',
    a.id,
    CAST(1.60 AS DECIMAL(9, 2))
FROM ambiente a
JOIN geometria g
    ON g.ambiente_id = a.id
    AND g.base = CAST(0.60 AS DECIMAL(9, 2))
    AND g.altura = CAST(0.60 AS DECIMAL(9, 2));

INSERT INTO ambiente_pes_direitos (ambiente_id, pes_direitos)
SELECT
    a.id,
    CAST(2.80 + (MOD(r.x, 5) * 0.10) AS DECIMAL(9, 2))
FROM SYSTEM_RANGE(1, 120) r
JOIN ambiente a ON a.informacao_adicional = CONCAT('Registro seed dev #', r.x);
