package com.padelMarius.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fermeture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fermeture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_fermeture", nullable = false)
    private LocalDate dateFermeture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PorteeFermeture portee;

    @Column(length = 255)
    private String motif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;
}