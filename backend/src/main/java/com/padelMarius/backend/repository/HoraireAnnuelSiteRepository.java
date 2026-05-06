package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.HoraireAnnuelSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoraireAnnuelSiteRepository extends JpaRepository<HoraireAnnuelSite, Long> {

    List<HoraireAnnuelSite> findBySiteId(Long siteId);

    Optional<HoraireAnnuelSite> findBySiteIdAndAnneeCivile(Long siteId, Integer anneeCivile);

    boolean existsBySiteIdAndAnneeCivile(Long siteId, Integer anneeCivile);
}