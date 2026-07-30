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
      <h2>État des matchs et terrains</h2>

      @if (!facade.admin()) {
        <p class="erreur">
          Tu dois te connecter comme admin avant de consulter cet état.
        </p>

        <p>
          <a mat-button routerLink="/admin/login">
            Connexion admin
          </a>
        </p>
      } @else {
        <p>
          Consulte l’occupation des terrains, les fermetures et l’état
          de chaque match pour une date et un site.
        </p>

        <form
          (ngSubmit)="chargerEtatOperationnel()"
          class="formulaire"
        >
          <mat-form-field appearance="outline">
            <mat-label>Date</mat-label>
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
            <mat-card
              appearance="outlined"
              class="bloc-info"
            >
              <strong>Vue limitée à ton site :</strong>
              {{ admin.nomSite || 'Site' }}
            </mat-card>
          }

          <button
            mat-flat-button
            type="submit"
            [disabled]="
              facade.chargement()
              || facade.chargementSites()
            "
          >
            {{
              facade.chargement()
                ? 'Chargement...'
                : 'Afficher l’état'
            }}
          </button>
        </form>

        @if (facade.messageErreur()) {
          <p class="erreur">
            {{ facade.messageErreur() }}
          </p>
        }

        @if (
          facade.etatOperationnel();
          as etat
          ) {
          <mat-card
            appearance="outlined"
            class="bloc-info"
          >
            <h3>Vue affichée</h3>

            <p>
              Site :
              <strong>{{ etat.nomSite }}</strong>
            </p>

            <p>
              Date :
              <strong>
                {{ etat.date | date:'dd/MM/yyyy' }}
              </strong>
            </p>

            <p>
              État du site :
              <strong>
                {{ etat.siteActif ? 'Actif' : 'Inactif' }}
              </strong>
            </p>
          </mat-card>

          @if (etat.ferme) {
            <mat-card
              appearance="outlined"
              class="bloc-info fermeture"
            >
              <h3>Site fermé pour cette date</h3>
              <p>
                {{
                  etat.motifFermeture
                    || 'Aucun motif renseigné.'
                }}
              </p>
            </mat-card>
          }

          @if (etat.terrains.length === 0) {
            <mat-card
              appearance="outlined"
              class="bloc-info"
            >
              Aucun terrain n’est configuré pour ce site.
            </mat-card>
          }

          <div class="terrains-grid">
            @for (
              terrain of etat.terrains;
              track terrain.terrainId
              ) {
              <mat-card
                appearance="outlined"
                class="terrain-card"
              >
                <div class="terrain-entete">
                  <h3>
                    Terrain {{ terrain.numeroTerrain }}
                  </h3>

                  <span
                    class="statut"
                    [class.ferme]="
                      terrain.etatTerrain === 'FERME'
                    "
                    [class.inactif]="
                      terrain.etatTerrain === 'INACTIF'
                    "
                  >
                    {{ enumLabel(terrain.etatTerrain) }}
                  </span>
                </div>

                @if (
                  terrain.matches.length === 0
                  ) {
                  <p class="etat-vide">
                    Aucun match pour cette date.
                  </p>
                } @else {
                  <div class="table-scroll">
                    <table>
                      <thead>
                      <tr>
                        <th>Horaire</th>
                        <th>Visibilité</th>
                        <th>État</th>
                        <th>Joueurs</th>
                      </tr>
                      </thead>

                      <tbody>
                        @for (
                          match of terrain.matches;
                          track match.matchId
                          ) {
                          <tr>
                            <td>
                              {{
                                match.dateHeureDebut
                                  | date:'HH:mm'
                              }}
                              –
                              {{
                                match.dateHeureFin
                                  | date:'HH:mm'
                              }}
                            </td>
                            <td>
                              {{
                                enumLabel(
                                  match.visibiliteCourante
                                )
                              }}
                            </td>
                            <td>
                              {{
                                enumLabel(
                                  match.etatCycle
                                )
                              }}
                            </td>
                            <td>
                              {{ match.nombreParticipants }} / 4
                            </td>
                          </tr>
                        }
                      </tbody>
                    </table>
                  </div>
                }
              </mat-card>
            }
          </div>
        }

        <p>
          <a mat-button routerLink="/admin/dashboard">
            Retour dashboard admin
          </a>
        </p>
      }
    </section>
  `,
  styles: [`
    form mat-form-field {
      width: 100%;
    }

    .terrains-grid {
      display: grid;
      gap: 16px;
      margin: 20px 0;
    }

    .terrain-card {
      padding: 18px;
    }

    .terrain-entete {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 12px;
    }

    .terrain-entete h3 {
      margin: 0;
      color: #003b95;
    }

    .statut {
      padding: 6px 10px;
      border-radius: 999px;
      background: #dbeafe;
      color: #001f5c;
      font-size: 13px;
      font-weight: 700;
    }

    .statut.ferme,
    .fermeture {
      background: #fee2e2;
      color: #991b1b;
    }

    .statut.inactif {
      background: #e2e8f0;
      color: #334155;
    }

    .table-scroll {
      overflow-x: auto;
    }

    table {
      width: 100%;
    }

    .etat-vide {
      margin: 0;
      color: #64748b;
    }

    @media (max-width: 600px) {
      .terrain-entete {
        align-items: flex-start;
        flex-direction: column;
      }
    }
  `]
})
export class AdminEtatOperationnelComponent
  implements OnInit {

  readonly enumLabel = enumLabel;

  constructor(
    readonly facade:
    AdminEtatOperationnelFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }

  chargerEtatOperationnel(): void {
    this.facade.chargerEtatOperationnel();
  }
}
