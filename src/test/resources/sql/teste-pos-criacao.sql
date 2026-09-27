CREATE UNIQUE INDEX uk_teste_setor_interditado_por_caverna ON setor (caverna_id) WHERE condicao_corrente = 'INTERDITADO';
