package br.edu.ifpb.pweb3.turmalina.app;

import java.util.HashMap;
import java.util.Map;

final class Configuracao {

    static final String UNIDADE_PERSISTENCIA = "turmalinaPU";

    private Configuracao() {
    }

    static Map<String, Object> propriedadesDoAmbiente() {
        return propriedadesDoAmbiente(System.getenv());
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
