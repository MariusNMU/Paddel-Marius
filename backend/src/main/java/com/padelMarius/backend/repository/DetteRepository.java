package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.StatutDette;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DetteRepository extends JpaRepository<Dette, Long> {

    Optional<Dette> findByMatchId(Long matchId);

    List<Dette> findByMembreResponsableId(Long membreResponsableId);

    List<Dette> findByMembreResponsableIdAndStatutDette(
            Long membreResponsableId,
            StatutDette statutDette
    );

    boolean existsByMembreResponsableIdAndStatutDette(
            Long membreResponsableId,
            StatutDette statutDette
    );

    List<Dette> findByStatutDette(StatutDette statutDette);
}