CREATE UNIQUE INDEX uk_autorizacao_vigente_por_expedicao ON autorizacao_ambiental (expedicao_id) WHERE situacao = 'VIGENTE';

CREATE EXTENSION IF NOT EXISTS lo;

CREATE TRIGGER trg_lo_plano_mapa_rota BEFORE UPDATE OR DELETE ON plano_seguranca FOR EACH ROW EXECUTE FUNCTION lo_manage(mapa_rota);
CREATE TRIGGER trg_lo_autorizacao_pdf BEFORE UPDATE OR DELETE ON autorizacao_ambiental FOR EACH ROW EXECUTE FUNCTION lo_manage(arquivo_pdf);
CREATE TRIGGER trg_lo_amostra_fotografia BEFORE UPDATE OR DELETE ON amostra FOR EACH ROW EXECUTE FUNCTION lo_manage(fotografia);
CREATE TRIGGER trg_lo_relatorio_arquivo BEFORE UPDATE OR DELETE ON relatorio_final FOR EACH ROW EXECUTE FUNCTION lo_manage(arquivo);
