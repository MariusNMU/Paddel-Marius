package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "administrateur",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_administrateur_email_ou_login", columnNames = "email_ou_login")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Administrateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "email_ou_login", nullable = false, length = 150)
    private String emailOuLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_administrateur", nullable = false, length = 20)
    private RoleAdministrateur roleAdministrateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(nullable = false)
    private boolean actif;
}