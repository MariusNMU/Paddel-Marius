package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Penalite;
import com.padelMarius.backend.entity.StatutPenalite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PenaliteRepository extends JpaRepository<Penalite, Long> {

    List<Penalite> findByMembreId(Long membreId);

    List<Penalite> findByMembreIdAndStatutPenalite(
            Long membreId,
            StatutPenalite statutPenalite
    );

    List<Penalite> findByMatchSourceId(Long matchSourceId);

    boolean existsByMembreIdAndStatutPenalite(
            Long membreId,
            StatutPenalite statutPenalite
    );
}