package br.edu.ifpb.pweb3.turmalina.dominio;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.proxy.HibernateProxy;

@MappedSuperclass
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntidadeBase outra) || raizDaHierarquia(this) != raizDaHierarquia(outra)) return false;
        return getId() != null && getId().equals(outra.getId());
    }

    @Override
    public int hashCode() {
        return raizDaHierarquia(this).hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getId();
    }

    static boolean mesmaEntidade(EntidadeBase a, EntidadeBase b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.getId() != null && a.getId().equals(b.getId());
    }

    private static Class<?> raizDaHierarquia(EntidadeBase entidade) {
        Class<?> classe = entidade instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : entidade.getClass();
        while (classe.getSuperclass() != EntidadeBase.class) {
            classe = classe.getSuperclass();
        }
        return classe;
    }
}
