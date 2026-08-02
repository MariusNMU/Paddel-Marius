package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findAllByOrderByNomAsc();

    List<Site> findByActifTrueOrderByNomAsc();

    Optional<Site> findByCode(String code);

    boolean existsByCode(String code);
}
