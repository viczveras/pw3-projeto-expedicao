package br.edu.ifpb.pweb3.turmalina.app;

import java.util.HashMap;
import java.util.Map;

final class Configuracao {

    static final String UNIDADE_PERSISTENCIA = "turmalinaPU";
    static final String SCRIPT_POS_CRIACAO = "META-INF/sql/pos-criacao.sql";

    private Configuracao() {
    }

    static Map<String, Object> propriedadesDoAmbiente() {
        return propriedadesDoAmbiente(System.getenv());
    }

    static Map<String, Object> propriedadesDaDemonstracao() {
        Map<String, Object> propriedades = propriedadesDoAmbiente();
        propriedades.putAll(recriacaoDoEsquema(SCRIPT_POS_CRIACAO));
        propriedades.put("hibernate.show_sql", "true");
        propriedades.put("hibernate.format_sql", "true");
        propriedades.put("hibernate.use_sql_comments", "true");
        propriedades.put("hibernate.generate_statistics", "true");
        return propriedades;
    }

    static Map<String, Object> recriacaoDoEsquema(String script) {
        Map<String, Object> propriedades = new HashMap<>();
        propriedades.put("hibernate.hbm2ddl.auto", "create");
        if (Configuracao.class.getClassLoader().getResource(script) != null) {
            propriedades.put("jakarta.persistence.sql-load-script-source", script);
        }
        return propriedades;
    }

    static Map<String, Object> propriedadesDoAmbiente(Map<String, String> ambiente) {
        Map<String, Object> propriedades = new HashMap<>();
        copiarVariavel(ambiente, "TURMALINA_DB_URL", "jakarta.persistence.jdbc.url", propriedades);
        copiarVariavel(ambiente, "TURMALINA_DB_USER", "jakarta.persistence.jdbc.user", propriedades);
        copiarVariavel(ambiente, "TURMALINA_DB_PASSWORD", "jakarta.persistence.jdbc.password", propriedades);
        return propriedades;
    }

    private static void copiarVariavel(Map<String, String> ambiente, String variavel, String propriedade,
                                      Map<String, Object> propriedades) {
        String valor = ambiente.get(variavel);
        if (valor != null && !valor.isBlank()) {
            propriedades.put(propriedade, valor);
        }
    }
}
