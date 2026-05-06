package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.PorteeFermeture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FermetureRepository extends JpaRepository<Fermeture, Long> {

    List<Fermeture> findByDateFermeture(LocalDate dateFermeture);

    List<Fermeture> findBySiteId(Long siteId);

    boolean existsByDateFermetureAndPorteeAndSiteIsNull(
            LocalDate dateFermeture,
            PorteeFermeture portee
    );

    boolean existsBySiteIdAndDateFermetureAndPortee(
            Long siteId,
            LocalDate dateFermeture,
            PorteeFermeture portee
    );
}