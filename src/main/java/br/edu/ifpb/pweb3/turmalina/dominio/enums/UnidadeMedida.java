package br.edu.ifpb.pweb3.turmalina.dominio.enums;

public enum UnidadeMedida {
    MILIGRAMA("mg"),
    GRAMA("g"),
    QUILOGRAMA("kg"),
    MICROLITRO("µL"),
    MILILITRO("mL"),
    LITRO("L");

    private final String simbolo;

    UnidadeMedida(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
