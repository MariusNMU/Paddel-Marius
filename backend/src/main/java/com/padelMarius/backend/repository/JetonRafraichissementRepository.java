package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.JetonRafraichissement;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JetonRafraichissementRepository extends
        JpaRepository<JetonRafraichissement, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select jeton
            from JetonRafraichissement jeton
            where jeton.identifiant = :identifiant
            """)
    Optional<JetonRafraichissement> findByIdentifiantForUpdate(
            @Param("identifiant") String identifiant
    );

    long deleteByDateExpirationBefore(LocalDateTime seuil);
}
