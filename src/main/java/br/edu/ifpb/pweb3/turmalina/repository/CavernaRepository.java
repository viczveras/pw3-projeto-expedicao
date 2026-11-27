package br.edu.ifpb.pweb3.turmalina.repository;

import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CavernaRepository extends JpaRepository<Caverna, Long> {

    boolean existsByCodigoCadastroAmbiental(String codigoCadastroAmbiental);

    boolean existsByCodigoCadastroAmbientalAndIdNot(String codigoCadastroAmbiental, Long id);

    @Query("select count(e) > 0 from Expedicao e where e.caverna.id = :cavernaId")
    boolean possuiExpedicao(@Param("cavernaId") Long cavernaId);
}
