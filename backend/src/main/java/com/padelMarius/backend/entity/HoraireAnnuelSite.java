package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "horaire_annuel_site",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_horaire_annuel_site_annee",
                        columnNames = {"site_id", "annee_civile"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoraireAnnuelSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "annee_civile", nullable = false)
    private Integer anneeCivile;

    @Column(name = "heure_debut_reservation", nullable = false)
    private LocalTime heureDebutReservation;

    @Column(name = "heure_fin_reservation", nullable = false)
    private LocalTime heureFinReservation;
}