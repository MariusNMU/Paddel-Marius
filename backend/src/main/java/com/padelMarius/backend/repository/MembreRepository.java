package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Membre;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembreRepository extends JpaRepository<Membre, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select membre
            from Membre membre
            where membre.id = :membreId
            """)
    Optional<Membre> findByIdForUpdate(
            @Param("membreId") Long membreId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select membre
            from Membre membre
            where membre.matricule = :matricule
            """)
    Optional<Membre> findByMatriculeForUpdate(
            @Param("matricule") String matricule
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select membre
            from Membre membre
            where membre.id in :membreIds
            order by membre.id
            """)
    List<Membre> findAllByIdForUpdate(
            @Param("membreIds") List<Long> membreIds
    );

    Optional<Membre> findByMatricule(String matricule);

    Optional<Membre> findByMatriculeIgnoreCase(String matricule);

    boolean existsByMatricule(String matricule);

    List<Membre> findByMatriculeStartingWith(String prefixe);

    List<Membre> findBySiteRattachementId(Long siteId);
}
