import { DatePipe } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import {
  MatFormFieldModule
} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import {
  EtatOperationnelAdminResponse,
  MatchEtatAdminResponse,
  OccupationHebdomadaireAdminResponse,
  TerrainEtatAdminResponse
} from '../../models/etat-operationnel.model';
import {
  AdminEtatOperationnelFacadeService
} from '../../services/admin-etat-operationnel-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-etat-operationnel',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink
  ],
  providers: [
    AdminEtatOperationnelFacadeService
  ],
  template: `
    <section class="page">
      <h2>Occupation hebdomadaire des terrains</h2>

      @if (!facade.admin()) {
        <p class="erreur">
          Tu dois te connecter comme admin avant de consulter cette vue.
        </p>

        <p>
          <a mat-button routerLink="/admin/login">
            Connexion admin
          </a>
        </p>
      } @else {
        <p>
          Visualise, sur une seule semaine, les réservations, les places
          occupées, les fermetures et la disponibilité de chaque terrain.
        </p>

        <mat-card
          appearance="outlined"
          class="filtres-card"
        >
          <form
            (ngSubmit)="chargerOccupationHebdomadaire()"
            class="formulaire filtres-form"
          >
            <mat-form-field appearance="outline">
              <mat-label>Une date de la semaine</mat-label>
              <input
                matInput
                id="date"
                name="date"
                type="date"
                [ngModel]="facade.date()"
                (ngModelChange)="
                  facade.modifierDate($event)
                "
                required
              >
            </mat-form-field>

            @if (facade.estAdminGlobal()) {
              <mat-form-field appearance="outline">
                <mat-label>Site</mat-label>
                <select
                  matNativeControl
                  id="siteId"
                  name="siteId"
                  [ngModel]="facade.siteId()"
                  (ngModelChange)="
                    facade.modifierSiteId($event)
                  "
                  [disabled]="facade.chargementSites()"
                  required
                >
                  @for (
                    site of facade.sites();
                    track site.siteId
                    ) {
                    <option [ngValue]="site.siteId">
                      {{ site.nom }}
                    </option>
                  }
                </select>
              </mat-form-field>
            } @else if (facade.admin(); as admin) {
              <div class="site-impose">
                <span>Site administré</span>
                <strong>{{ admin.nomSite || 'Site non renseigné' }}</strong>
              </div>
            }

            <button
              mat-flat-button
              type="submit"
              class="bouton-afficher"
              [disabled]="
                facade.chargement()
                || facade.chargementSites()
              "
            >
              {{
                facade.chargement()
                  ? 'Chargement...'
                  : 'Afficher la semaine'
              }}
            </button>
          </form>
        </mat-card>

        @if (facade.messageErreur()) {
          <p class="erreur">
            {{ facade.messageErreur() }}
          </p>
        }

        @if (
          facade.occupationHebdomadaire();
          as occupation
          ) {
          <div class="semaine-entete">
            <div>
              <span class="sur-titre">Planning central</span>
              <h3>{{ occupation.nomSite }}</h3>
              <p>
                Du
                <strong>
                  {{ occupation.dateDebut | date:'dd/MM/yyyy' }}
                </strong>
                au
                <strong>
                  {{ occupation.dateFin | date:'dd/MM/yyyy' }}
                </strong>
              </p>
            </div>

            <div
              class="navigation-semaine"
              aria-label="Navigation entre les semaines"
            >
              <button
                mat-stroked-button
                type="button"
                (click)="facade.decalerSemaine(-1)"
              >
                Semaine précédente
              </button>

              <button
                mat-stroked-button
                type="button"
                (click)="facade.selectionnerSemaineCourante()"
              >
                Cette semaine
              </button>

              <button
                mat-stroked-button
                type="button"
                (click)="facade.decalerSemaine(1)"
              >
                Semaine suivante
              </button>
            </div>
          </div>

          @if (!occupation.siteActif) {
            <p class="alerte-inactive">
              Ce site est actuellement inactif. Les données restent visibles
              pour le suivi administratif.
            </p>
          }

          <div class="indicateurs-grid">
            <mat-card appearance="outlined" class="indicateur-card">
              <span>Terrains</span>
              <strong>{{ nombreTerrains(occupation) }}</strong>
            </mat-card>

            <mat-card appearance="outlined" class="indicateur-card">
              <span>Réservations actives</span>
              <strong>{{ nombreReservationsActives(occupation) }}</strong>
            </mat-card>

            <mat-card appearance="outlined" class="indicateur-card">
              <span>Jours de fermeture</span>
              <strong>{{ nombreJoursFermes(occupation) }}</strong>
            </mat-card>
          </div>

          <div class="legende" aria-label="Légende du planning">
            <span class="legende-item">
              <i class="pastille reserve"></i>
              Réservé
            </span>
            <span class="legende-item">
              <i class="pastille libre"></i>
              Libre
            </span>
            <span class="legende-item">
              <i class="pastille ferme"></i>
              Fermé
            </span>
            <span class="legende-item">
              <i class="pastille annule"></i>
              Annulé
            </span>
          </div>

          @if (nombreTerrains(occupation) === 0) {
            <mat-card
              appearance="outlined"
              class="bloc-info"
            >
              Aucun terrain n’est configuré pour ce site.
            </mat-card>
          } @else {
            <div class="planning-scroll">
              <table class="occupation-table">
                <caption>
                  Occupation des terrains du
                  {{ occupation.dateDebut | date:'dd/MM/yyyy' }}
                  au
                  {{ occupation.dateFin | date:'dd/MM/yyyy' }}
                </caption>

                <thead>
                  <tr>
                    <th scope="col" class="colonne-terrain">
                      Terrain
                    </th>

                    @for (
                      jour of occupation.jours;
                      track jour.date;
                      let indexJour = $index
                      ) {
                      <th scope="col" class="colonne-jour">
                        <span>{{ libelleJour(indexJour) }}</span>
                        <strong>
                          {{ jour.date | date:'dd/MM' }}
                        </strong>
                      </th>
                    }
                  </tr>
                </thead>

                <tbody>
                  @for (
                    terrainReference of terrainsReference(occupation);
                    track terrainReference.terrainId
                    ) {
                    <tr>
                      <th scope="row" class="colonne-terrain">
                        <strong>
                          Terrain {{ terrainReference.numeroTerrain }}
                        </strong>
                        <span>
                          {{ terrainReference.actif ? 'Actif' : 'Inactif' }}
                        </span>
                      </th>

                      @for (
                        jour of occupation.jours;
                        track jour.date
                        ) {
                        <td
                          [class.cellule-fermee]="jour.ferme"
                        >
                          @if (
                            terrainPourJour(
                              jour,
                              terrainReference.terrainId
                            );
                            as terrainJour
                            ) {
                            @if (!terrainJour.actif) {
                              <span class="etat-cellule inactif">
                                Terrain inactif
                              </span>
                            } @else if (jour.ferme) {
                              <span class="etat-cellule ferme">
                                Site fermé
                              </span>
                              <small>
                                {{ jour.motifFermeture || 'Fermeture planifiée' }}
                              </small>
                            } @else {
                              @if (matchesActifs(terrainJour).length === 0) {
                                <span class="etat-cellule disponible">
                                  Libre
                                </span>
                              }

                              @for (
                                match of matchesActifs(terrainJour);
                                track match.matchId
                                ) {
                                <div class="reservation active">
                                  <strong>
                                    {{ match.dateHeureDebut | date:'HH:mm' }}
                                    –
                                    {{ match.dateHeureFin | date:'HH:mm' }}
                                  </strong>
                                  <span>
                                    {{ match.nombreParticipants }} / 4 joueurs
                                  </span>
                                  <small>
                                    {{ enumLabel(match.etatCycle) }}
                                    ·
                                    {{ enumLabel(match.visibiliteCourante) }}
                                  </small>
                                </div>
                              }

                              @for (
                                match of matchesAnnules(terrainJour);
                                track match.matchId
                                ) {
                                <div class="reservation annulee">
                                  <strong>
                                    {{ match.dateHeureDebut | date:'HH:mm' }}
                                    –
                                    {{ match.dateHeureFin | date:'HH:mm' }}
                                  </strong>
                                  <span>Match annulé</span>
                                </div>
                              }
                            }
                          } @else {
                            <span class="etat-cellule inactif">
                              Donnée indisponible
                            </span>
                          }
                        </td>
                      }
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        }

        <p class="retour-dashboard">
          <a mat-button routerLink="/admin/dashboard">
            Retour dashboard admin
          </a>
        </p>
      }
    </section>
  `,
  styles: [`
    .filtres-card {
      margin-top: 20px;
      padding: 20px;
      border-color: #bfdbfe;
      background: #f8fbff;
    }

    .formulaire.filtres-form {
      grid-template-columns: repeat(3, minmax(0, 1fr));
      align-items: start;
      width: 100%;
      max-width: none;
      margin: 0;
    }

    .filtres-form mat-form-field {
      width: 100%;
    }

    .site-impose {
      display: flex;
      min-height: 56px;
      flex-direction: column;
      justify-content: center;
      padding: 8px 14px;
      border: 1px solid #bfdbfe;
      border-radius: 8px;
      background: #ffffff;
    }

    .site-impose span,
    .sur-titre {
      color: #64748b;
      font-size: 12px;
      font-weight: 700;
      text-transform: uppercase;
    }

    .bouton-afficher {
      width: 100%;
      min-height: 56px;
      align-self: start;
      justify-self: stretch;
    }

    .semaine-entete {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 20px;
      margin-top: 24px;
      padding: 20px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
    }

    .semaine-entete h3 {
      margin: 4px 0;
      color: #003b95;
      font-size: 1.3rem;
    }

    .semaine-entete p {
      margin: 0;
      color: #475569;
    }

    .navigation-semaine {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 8px;
    }

    .indicateurs-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 14px;
      margin: 18px 0;
    }

    .indicateur-card {
      display: flex;
      min-height: 92px;
      flex-direction: column;
      justify-content: center;
      padding: 16px 18px;
      border-color: #dbeafe;
      background: #ffffff;
    }

    .indicateur-card span {
      color: #64748b;
      font-size: 13px;
      font-weight: 700;
      text-transform: uppercase;
    }

    .indicateur-card strong {
      color: #003b95;
      font-size: 1.7rem;
    }

    .alerte-inactive {
      margin: 16px 0 0;
      padding: 12px 14px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      background: #f1f5f9;
      color: #334155;
    }

    .legende {
      display: flex;
      flex-wrap: wrap;
      gap: 14px;
      margin: 0 0 12px;
      color: #475569;
      font-size: 13px;
    }

    .legende-item {
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }

    .pastille {
      width: 10px;
      height: 10px;
      border-radius: 50%;
    }

    .pastille.reserve {
      background: #2563eb;
    }

    .pastille.libre {
      background: #16a34a;
    }

    .pastille.ferme {
      background: #dc2626;
    }

    .pastille.annule {
      background: #94a3b8;
    }

    .planning-scroll {
      overflow-x: auto;
      border: 1px solid #dbeafe;
      border-radius: 12px;
      background: #ffffff;
    }

    .occupation-table {
      min-width: 1180px;
      margin: 0;
      border-collapse: separate;
      border-spacing: 0;
      table-layout: fixed;
    }

    .occupation-table caption {
      padding: 14px 16px;
      border-bottom: 1px solid #dbeafe;
      color: #1e3a5f;
      font-weight: 700;
      text-align: left;
    }

    .occupation-table th,
    .occupation-table td {
      padding: 12px;
      border: 0;
      border-right: 1px solid #e2e8f0;
      border-bottom: 1px solid #e2e8f0;
      vertical-align: top;
    }

    .occupation-table thead th {
      background: #eff6ff;
      color: #003b95;
      text-align: center;
    }

    .occupation-table tr:last-child > * {
      border-bottom: 0;
    }

    .occupation-table tr > *:last-child {
      border-right: 0;
    }

    .colonne-terrain {
      position: sticky;
      left: 0;
      z-index: 2;
      width: 150px;
      background: #f8fafc;
      text-align: left;
    }

    thead .colonne-terrain {
      z-index: 3;
    }

    tbody .colonne-terrain strong,
    tbody .colonne-terrain span {
      display: block;
    }

    tbody .colonne-terrain span {
      margin-top: 4px;
      color: #64748b;
      font-size: 12px;
      font-weight: 500;
    }

    .colonne-jour span,
    .colonne-jour strong {
      display: block;
    }

    .colonne-jour span {
      font-size: 12px;
      text-transform: uppercase;
    }

    .occupation-table td {
      background: #ffffff;
    }

    .occupation-table td.cellule-fermee {
      background: #fff7f7;
    }

    .etat-cellule {
      display: inline-flex;
      padding: 4px 8px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 700;
    }

    .etat-cellule.disponible {
      background: #dcfce7;
      color: #166534;
    }

    .etat-cellule.ferme {
      background: #fee2e2;
      color: #991b1b;
    }

    .etat-cellule.inactif {
      background: #e2e8f0;
      color: #475569;
    }

    td > small {
      display: block;
      margin-top: 8px;
      color: #7f1d1d;
    }

    .reservation {
      display: flex;
      flex-direction: column;
      gap: 2px;
      margin-top: 8px;
      padding: 8px 9px;
      border-left: 4px solid #2563eb;
      border-radius: 7px;
      background: #eff6ff;
      color: #1e3a5f;
      font-size: 12px;
    }

    .reservation small {
      color: #64748b;
    }

    .reservation.annulee {
      border-left-color: #94a3b8;
      background: #f1f5f9;
      color: #64748b;
      text-decoration: line-through;
    }

    .retour-dashboard {
      margin: 20px 0 0;
    }

    @media (max-width: 900px) {
      .formulaire.filtres-form {
        grid-template-columns: 1fr;
      }

      .semaine-entete {
        align-items: flex-start;
        flex-direction: column;
      }

      .navigation-semaine {
        justify-content: flex-start;
      }
    }

    @media (max-width: 640px) {
      .filtres-card {
        padding: 16px;
      }

      .indicateurs-grid {
        grid-template-columns: 1fr;
      }

      .navigation-semaine button {
        width: 100%;
      }
    }
  `]
})
export class AdminEtatOperationnelComponent
  implements OnInit {

  readonly enumLabel = enumLabel;

  private readonly libellesJours = [
    'Lundi',
    'Mardi',
    'Mercredi',
    'Jeudi',
    'Vendredi',
    'Samedi',
    'Dimanche'
  ];

  constructor(
    readonly facade:
    AdminEtatOperationnelFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }

  chargerOccupationHebdomadaire(): void {
    this.facade.chargerOccupationHebdomadaire();
  }

  libelleJour(indexJour: number): string {
    return this.libellesJours[indexJour]
      ?? `Jour ${indexJour + 1}`;
  }

  terrainsReference(
    occupation:
    OccupationHebdomadaireAdminResponse
  ): TerrainEtatAdminResponse[] {
    return occupation.jours[0]
      ?.terrains ?? [];
  }

  terrainPourJour(
    jour: EtatOperationnelAdminResponse,
    terrainId: number
  ): TerrainEtatAdminResponse | undefined {
    return jour.terrains.find(
      terrain =>
        terrain.terrainId === terrainId
    );
  }

  matchesActifs(
    terrain: TerrainEtatAdminResponse
  ): MatchEtatAdminResponse[] {
    return terrain.matches.filter(
      match => match.etatCycle !== 'ANNULE'
    );
  }

  matchesAnnules(
    terrain: TerrainEtatAdminResponse
  ): MatchEtatAdminResponse[] {
    return terrain.matches.filter(
      match => match.etatCycle === 'ANNULE'
    );
  }

  nombreTerrains(
    occupation:
    OccupationHebdomadaireAdminResponse
  ): number {
    return this.terrainsReference(
      occupation
    ).length;
  }

  nombreReservationsActives(
    occupation:
    OccupationHebdomadaireAdminResponse
  ): number {
    return occupation.jours.reduce(
      (total, jour) =>
        total + jour.terrains.reduce(
          (totalJour, terrain) =>
            totalJour
            + this.matchesActifs(terrain).length,
          0
        ),
      0
    );
  }

  nombreJoursFermes(
    occupation:
    OccupationHebdomadaireAdminResponse
  ): number {
    return occupation.jours.filter(
      jour => jour.ferme
    ).length;
  }
}
