package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.RoleAdministrateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {

    Optional<Administrateur> findByEmailOuLogin(String emailOuLogin);

    Optional<Administrateur> findByEmailOuLoginIgnoreCase(
            String emailOuLogin
    );

    boolean existsByEmailOuLogin(String emailOuLogin);

    List<Administrateur> findByRoleAdministrateur(RoleAdministrateur roleAdministrateur);

    List<Administrateur> findBySiteId(Long siteId);

    List<Administrateur> findByActifTrue();
}
