package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Membre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembreRepository extends JpaRepository<Membre, Long> {

    Optional<Membre> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    List<Membre> findByMatriculeStartingWith(String prefixe);

    List<Membre> findBySiteRattachementId(Long siteId);
}