-- Seed de desenvolvimento: 120 ambientes

INSERT INTO localizacao (bloco, unidade, andar)
SELECT
    CASE MOD(x, 8)
        WHEN 0 THEN 'BLOCO_1'
        WHEN 1 THEN 'BLOCO_2'
        WHEN 2 THEN 'BLOCO_3'
        WHEN 3 THEN 'BLOCO_4'
        WHEN 4 THEN 'BLOCO_5'
        WHEN 5 THEN 'BLOCO_6'
        WHEN 6 THEN 'BLOCO_7'
        ELSE 'BLOCO_8'
    END,
    CASE MOD(x, 3)
        WHEN 0 THEN 'SEDE'
        WHEN 1 THEN 'CIDADE_ALTA'
        ELSE 'UEPE'
    END,
    x
FROM SYSTEM_RANGE(1, 120);

INSERT INTO ambiente (nome, localizacao_id, capacidade, tipo, informacao_adicional, status)
SELECT
    CONCAT('Ambiente ', r.x),
    l.id,
    20 + MOD(r.x, 41),
    CASE MOD(r.x, 6)
        WHEN 0 THEN 'SALA_AULA'
        WHEN 1 THEN 'LABORATORIO'
        WHEN 2 THEN 'LABORATORIO_INFORMATICA'
        WHEN 3 THEN 'BIBLIOTECA'
        WHEN 4 THEN 'AUDITORIO'
        ELSE 'SALA_ADMINISTRATIVA'
    END,
    CONCAT('Registro seed dev #', r.x),
    'NAO_PUBLICADO'
FROM SYSTEM_RANGE(1, 120) r
JOIN localizacao l ON l.andar = r.x;

INSERT INTO geometria (tipo, base, altura, repeticao, ambiente_id)
SELECT
    'RETANGULAR',
    CAST(4 + MOD(r.x, 5) AS DECIMAL(9, 2)),
    CAST(3 + MOD(r.x, 4) AS DECIMAL(9, 2)),
    1,
    a.id
FROM SYSTEM_RANGE(1, 120) r
JOIN ambiente a ON a.nome = CONCAT('Ambiente ', r.x);

INSERT INTO esquadria (tipo, geometria_id, material, informacao_adicional, ambiente_id, altura_peitoril)
SELECT
    'PORTA',
    g.id,
    'MADEIRA_FICHA',
    'Porta padrao do seed dev',
    a.id,
    CAST(0.00 AS DECIMAL(9, 2))
FROM ambiente a
JOIN geometria g ON g.ambiente_id = a.id;

INSERT INTO esquadria (tipo, geometria_id, material, informacao_adicional, ambiente_id, altura_peitoril)
SELECT
    CASE MOD(r.x, 4)
        WHEN 0 THEN 'JANELA'
        WHEN 1 THEN 'COBOGO'
        WHEN 2 THEN 'VAO_ABERTO'
        ELSE 'ESQUADRIA_OUTRO_AMBIENTE'
    END,
    g.id,
    CASE MOD(r.x, 4)
        WHEN 0 THEN 'ALUMINIO_VIDRO'
        WHEN 1 THEN 'PRE_MOLDADO'
        ELSE 'NAO_SE_APLICA'
    END,
    CONCAT('Segunda esquadria seed dev #', r.x),
    a.id,
    CASE MOD(r.x, 4)
        WHEN 0 THEN CAST(1.00 AS DECIMAL(9, 2))
        ELSE CAST(0.00 AS DECIMAL(9, 2))
    END
FROM SYSTEM_RANGE(1, 120) r
JOIN ambiente a ON a.nome = CONCAT('Ambiente ', r.x)
JOIN geometria g ON g.ambiente_id = a.id;

INSERT INTO ambiente_pes_direitos (ambiente_id, pes_direitos)
SELECT
    a.id,
    CAST(2.80 + (MOD(r.x, 5) * 0.10) AS DECIMAL(9, 2))
FROM SYSTEM_RANGE(1, 120) r
JOIN ambiente a ON a.nome = CONCAT('Ambiente ', r.x);
