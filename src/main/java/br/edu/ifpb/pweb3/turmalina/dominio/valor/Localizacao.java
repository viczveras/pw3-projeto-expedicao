package br.edu.ifpb.pweb3.turmalina.dominio.valor;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Localizacao {

    private static final BigDecimal LAT_MAX = new BigDecimal("90");
    private static final BigDecimal LON_MAX = new BigDecimal("180");

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "datum", nullable = false, length = 20)
    private DatumGeodesico datum;

    protected Localizacao() {
    }

    public Localizacao(BigDecimal latitude, BigDecimal longitude, DatumGeodesico datum) {
        Objects.requireNonNull(latitude, "latitude");
        Objects.requireNonNull(longitude, "longitude");
        if (latitude.abs().compareTo(LAT_MAX) > 0) {
            throw new IllegalArgumentException("Latitude fora do intervalo [-90, 90]: " + latitude);
        }
        if (longitude.abs().compareTo(LON_MAX) > 0) {
            throw new IllegalArgumentException("Longitude fora do intervalo [-180, 180]: " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.datum = Objects.requireNonNull(datum, "datum");
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public DatumGeodesico getDatum() {
        return datum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Localizacao outra)) return false;
        return latitude.compareTo(outra.latitude) == 0
                && longitude.compareTo(outra.longitude) == 0
                && datum == outra.datum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude.stripTrailingZeros(), longitude.stripTrailingZeros(), datum);
    }

    @Override
    public String toString() {
        return latitude + ", " + longitude + " (" + datum + ")";
    }
}
