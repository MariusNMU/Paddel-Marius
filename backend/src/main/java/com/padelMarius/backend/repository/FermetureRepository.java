package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FermetureRepository extends JpaRepository<Fermeture, Long> {

    List<Fermeture> findByDateFermeture(LocalDate dateFermeture);

    List<Fermeture> findByDateFermetureBetweenOrderByDateFermetureAsc(
            LocalDate dateDebut,
            LocalDate dateFin
    );

    List<Fermeture> findBySite(Site site);

    List<Fermeture> findBySiteId(Long siteId);

    List<Fermeture> findByPortee(PorteeFermeture portee);

    Optional<Fermeture> findFirstByDateFermetureAndPortee(
            LocalDate dateFermeture,
            PorteeFermeture portee
    );

    Optional<Fermeture> findFirstByDateFermetureAndPorteeAndSite(
            LocalDate dateFermeture,
            PorteeFermeture portee,
            Site site
    );

    boolean existsByDateFermetureAndPorteeAndSiteIsNull(
            LocalDate dateFermeture,
            PorteeFermeture portee
    );

    List<Fermeture> findByDateFermetureAndPortee(
            LocalDate dateFermeture,
            PorteeFermeture portee
    );

    boolean existsBySiteIdAndDateFermetureAndPortee(
            Long siteId,
            LocalDate dateFermeture,
            PorteeFermeture portee
    );
}
