import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DetteResponse } from '../../models/dette.model';
import { AuthContextService } from '../../services/auth-context.service';
import { DetteApiService } from '../../services/dette-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-mes-dettes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Mes dettes</h2>

      <p>
        Consultation et paiement des dettes ouvertes.
      </p>

      <form (ngSubmit)="chargerDettes()">
        <label for="matricule">Matricule</label>
        <input
          id="matricule"
          name="matricule"
          type="text"
          [(ngModel)]="matricule"
          required
        />

        <button type="submit" [disabled]="chargement">
          {{ chargement ? 'Chargement...' : 'Consulter mes dettes' }}
        </button>
      </form>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <p *ngIf="messageSucces" class="succes">
        {{ messageSucces }}
      </p>

      <div *ngIf="dettes.length === 0 && rechercheEffectuee && !messageErreur" class="resultat">
        <p>Aucune dette ouverte pour ce matricule.</p>
      </div>

      <div *ngIf="dettes.length > 0" class="resultat">
        <h3>Dettes ouvertes</h3>

        <table>
          <thead>
          <tr>
            <th>ID dette</th>
            <th>ID match</th>
            <th>Montant initial</th>
            <th>Montant restant</th>
            <th>Statut</th>
            <th>Paiement</th>
          </tr>
          </thead>
          <tbody>
          <tr *ngFor="let dette of dettes">
            <td>{{ dette.detteId }}</td>
            <td>{{ dette.matchId }}</td>
            <td>{{ dette.montantInitial }} €</td>
            <td>{{ dette.montantRestant }} €</td>
            <td>{{ dette.statutDette }}</td>
            <td>
              <input
                type="number"
                min="0"
                step="0.01"
                [(ngModel)]="montantsPaiement[dette.detteId]"
                [name]="'montantDette' + dette.detteId"
              />

              <button
                type="button"
                (click)="payerDette(dette)"
                [disabled]="paiementEnCoursDetteId === dette.detteId"
              >
                {{ paiementEnCoursDetteId === dette.detteId ? 'Paiement...' : 'Payer' }}
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>
  `
})
export class MesDettesComponent implements OnInit {
  matricule = 'G1002';

  dettes: DetteResponse[] = [];
  montantsPaiement: Record<number, number> = {};

  chargement = false;
  rechercheEffectuee = false;
  paiementEnCoursDetteId: number | null = null;

  messageErreur = '';
  messageSucces = '';

  constructor(
    private readonly detteApiService: DetteApiService,
    private readonly authContext: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
    this.matricule = this.authContext.joueur()?.matricule ?? 'G1002';
  }

  ngOnInit(): void {
    if (this.authContext.joueur()) {
      this.chargerDettes();
    }
  }

  chargerDettes(conserverMessageSucces = false): void {
    this.messageErreur = '';

    if (!conserverMessageSucces) {
      this.messageSucces = '';
    }

    this.dettes = [];
    this.rechercheEffectuee = false;

    const matriculeNettoye = this.matricule.trim();

    if (!matriculeNettoye) {
      this.messageErreur = 'Le matricule est obligatoire.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.detteApiService.consulterDettesOuvertes(matriculeNettoye).subscribe({
      next: (dettes) => {
        this.dettes = dettes;
        this.rechercheEffectuee = true;
        this.chargement = false;

        this.montantsPaiement = {};

        for (const dette of dettes) {
          this.montantsPaiement[dette.detteId] = dette.montantRestant;
        }

        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  payerDette(dette: DetteResponse): void {
    this.messageErreur = '';
    this.messageSucces = '';

    const montant = this.montantsPaiement[dette.detteId];

    if (!montant || montant <= 0) {
      this.messageErreur = 'Le montant du paiement doit être supérieur à 0.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.paiementEnCoursDetteId = dette.detteId;
    this.changeDetectorRef.detectChanges();

    this.detteApiService.payerDette(dette.detteId, { montant }).subscribe({
      next: (response) => {
        this.messageSucces = `Dette ${response.dette.detteId} payée pour ${response.montantPaye} €.`;
        this.paiementEnCoursDetteId = null;
        this.changeDetectorRef.detectChanges();
        this.chargerDettes(true);
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.paiementEnCoursDetteId = null;
        this.changeDetectorRef.detectChanges();
      }
    });
  }
}
