package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.StatutDette;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DetteRepository extends JpaRepository<Dette, Long> {

    @Query("""
            select dette.membreResponsable.id
            from Dette dette
            where dette.id = :detteId
            """)
    Optional<Long> findMembreResponsableIdById(
            @Param("detteId") Long detteId
    );

    @Query("""
            select dette.match.id
            from Dette dette
            where dette.id = :detteId
            """)
    Optional<Long> findMatchIdById(
            @Param("detteId") Long detteId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select dette
            from Dette dette
            where dette.match.id = :matchId
            """)
    Optional<Dette> findByMatchIdForUpdate(
            @Param("matchId") Long matchId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select dette
            from Dette dette
            where dette.id = :detteId
            """)
    Optional<Dette> findByIdForUpdate(
            @Param("detteId") Long detteId
    );

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
