import { Injectable, signal } from '@angular/core';
import { catchError, EMPTY, finalize, tap } from 'rxjs';
import { InvitationPriveeResponse } from '../models/invitation.model';
import {
  CreerMatchRequest,
  MatchResponse,
  ModeCreation
} from '../models/match.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { TerrainResponse } from '../models/terrain.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { dateHeureDuJourPourInput } from '../shared/date-ui.util';
import { AuthContextService } from './auth-context.service';
import { InvitationApiService } from './invitation-api.service';
import { MatchApiService } from './match-api.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { TerrainApiService } from './terrain-api.service';

@Injectable({
  providedIn: 'root'
})
export class CreerMatchFacadeService {
  private readonly terrainsSignal = signal<TerrainResponse[]>([]);
  private readonly parametresMetierSignal =
    signal<ParametresMetierResponse | null>(null);

  private readonly terrainIdSignal = signal<number | null>(null);
  private readonly matriculeOrganisateurSignal = signal('');
  private readonly dateHeureDebutSignal = signal('');
  private readonly modeCreationSignal = signal<ModeCreation | ''>('');

  private readonly chargementTerrainsSignal = signal(false);
  private readonly chargementParametresMetierSignal = signal(false);
  private readonly chargementCreationSignal = signal(false);
  private readonly chargementInvitationSignal = signal(false);

  private readonly messageErreurSignal = signal('');
  private readonly matchCreeSignal = signal<MatchResponse | null>(null);

  private readonly matriculeInviteSignal = signal('');
  private readonly invitesSignal = signal<InvitationPriveeResponse[]>([]);
  private readonly messageInvitationSignal = signal('');

  readonly terrains = this.terrainsSignal.asReadonly();
  readonly parametresMetier = this.parametresMetierSignal.asReadonly();

  readonly terrainId = this.terrainIdSignal.asReadonly();
  readonly matriculeOrganisateur =
    this.matriculeOrganisateurSignal.asReadonly();
  readonly dateHeureDebut = this.dateHeureDebutSignal.asReadonly();
  readonly modeCreation = this.modeCreationSignal.asReadonly();

  readonly chargementTerrains =
    this.chargementTerrainsSignal.asReadonly();
  readonly chargementParametresMetier =
    this.chargementParametresMetierSignal.asReadonly();
  readonly chargementCreation =
    this.chargementCreationSignal.asReadonly();
  readonly chargementInvitation =
    this.chargementInvitationSignal.asReadonly();

  readonly messageErreur = this.messageErreurSignal.asReadonly();
  readonly matchCree = this.matchCreeSignal.asReadonly();

  readonly matriculeInvite = this.matriculeInviteSignal.asReadonly();
  readonly invites = this.invitesSignal.asReadonly();
  readonly messageInvitation =
    this.messageInvitationSignal.asReadonly();

  constructor(
    private readonly matchApiService: MatchApiService,
    private readonly invitationApiService: InvitationApiService,
    private readonly terrainApiService: TerrainApiService,
    private readonly parametresMetierApiService:
    ParametresMetierApiService,
    private readonly authContextService: AuthContextService
  ) {
  }

  initialiser(
    terrainIdParam: string | null,
    dateHeureDebutParam: string | null
  ): void {
    this.reinitialiserParcours();

    this.matriculeOrganisateurSignal.set(
      this.authContextService.joueur()?.matricule ?? ''
    );

    this.terrainIdSignal.set(
      this.extraireTerrainId(terrainIdParam)
    );

    this.dateHeureDebutSignal.set(
      dateHeureDebutParam
        ? dateHeureDebutParam.substring(0, 16)
        : dateHeureDuJourPourInput('13:00')
    );

    this.chargerParametresMetier();
    this.chargerTerrains();
  }

  modifierTerrainId(terrainId: number | null): void {
    this.terrainIdSignal.set(terrainId);
  }

  modifierMatriculeOrganisateur(matricule: string): void {
    this.matriculeOrganisateurSignal.set(matricule);
  }

  modifierDateHeureDebut(dateHeureDebut: string): void {
    this.dateHeureDebutSignal.set(dateHeureDebut);
  }

  modifierModeCreation(modeCreation: ModeCreation | ''): void {
    this.modeCreationSignal.set(modeCreation);
  }

  modifierMatriculeInvite(matriculeInvite: string): void {
    this.matriculeInviteSignal.set(matriculeInvite);
  }

  dureeMatchLibelle(): string {
    const parametres = this.parametresMetierSignal();

    if (!parametres) {
      return 'Non chargé';
    }

    const minutes = parametres.dureeMatchMinutes;
    const heures = Math.floor(minutes / 60);
    const minutesRestantes = minutes % 60;

    if (minutesRestantes === 0) {
      return `${heures}h`;
    }

    return `${heures}h${String(minutesRestantes).padStart(2, '0')}`;
  }

  terrainSelectionne(): TerrainResponse | undefined {
    return this.terrainsSignal().find(
      terrain => terrain.terrainId === Number(this.terrainIdSignal())
    );
  }

  creerMatch(): void {
    this.messageErreurSignal.set('');
    this.matchCreeSignal.set(null);

    const terrainId = this.terrainIdSignal();
    const matriculeOrganisateur =
      this.matriculeOrganisateurSignal().trim();
    const dateHeureDebut = this.dateHeureDebutSignal();
    const modeCreation = this.modeCreationSignal();

    if (
      !terrainId
      || !matriculeOrganisateur
      || !dateHeureDebut
      || !modeCreation
    ) {
      this.messageErreurSignal.set(
        'Tous les champs sont obligatoires.'
      );
      return;
    }

    const request: CreerMatchRequest = {
      terrainId,
      matriculeOrganisateur,
      dateHeureDebut,
      modeCreation
    };

    this.chargementCreationSignal.set(true);

    this.matchApiService.creerMatch(request).pipe(
      tap(response => {
        this.matchCreeSignal.set(response);
        this.invitesSignal.set([]);
        this.messageInvitationSignal.set('');
        this.matriculeInviteSignal.set('');
      }),
      catchError(error => {
        this.messageErreurSignal.set(
          extraireMessageErreur(error)
        );
        return EMPTY;
      }),
      finalize(() => {
        this.chargementCreationSignal.set(false);
      })
    ).subscribe();
  }

  inviterJoueur(): void {
    this.messageInvitationSignal.set('');

    const matchCree = this.matchCreeSignal();
    const matriculeInvite = this.matriculeInviteSignal().trim();
    const matriculeOrganisateur =
      this.matriculeOrganisateurSignal().trim();

    if (!matchCree) {
      this.messageInvitationSignal.set(
        'Crée d’abord le match privé.'
      );
      return;
    }

    if (!matriculeInvite) {
      this.messageInvitationSignal.set(
        'Le matricule invité est obligatoire.'
      );
      return;
    }

    this.chargementInvitationSignal.set(true);

    this.invitationApiService.inviterJoueur(
      matchCree.matchId,
      {
        matriculeOrganisateur,
        matriculeInvite
      }
    ).pipe(
      tap(response => {
        this.invitesSignal.update(
          invites => [...invites, response]
        );

        this.messageInvitationSignal.set(
          `${response.prenomInvite} ${response.nomInvite} `
          + `(${response.matriculeInvite}) invité.`
        );

        this.matriculeInviteSignal.set('');
      }),
      catchError(error => {
        this.messageInvitationSignal.set(
          extraireMessageErreur(error)
        );
        return EMPTY;
      }),
      finalize(() => {
        this.chargementInvitationSignal.set(false);
      })
    ).subscribe();
  }

  private reinitialiserParcours(): void {
    this.terrainsSignal.set([]);
    this.parametresMetierSignal.set(null);

    this.terrainIdSignal.set(null);
    this.matriculeOrganisateurSignal.set('');
    this.dateHeureDebutSignal.set('');
    this.modeCreationSignal.set('');

    this.chargementTerrainsSignal.set(false);
    this.chargementParametresMetierSignal.set(false);
    this.chargementCreationSignal.set(false);
    this.chargementInvitationSignal.set(false);

    this.messageErreurSignal.set('');
    this.matchCreeSignal.set(null);

    this.matriculeInviteSignal.set('');
    this.invitesSignal.set([]);
    this.messageInvitationSignal.set('');
  }

  private extraireTerrainId(
    terrainIdParam: string | null
  ): number | null {
    if (!terrainIdParam) {
      return null;
    }

    const terrainId = Number(terrainIdParam);

    return Number.isNaN(terrainId)
      ? null
      : terrainId;
  }

  private chargerParametresMetier(): void {
    this.chargementParametresMetierSignal.set(true);

    this.parametresMetierApiService
      .consulterParametresMetier()
      .pipe(
        tap(parametres => {
          this.parametresMetierSignal.set(parametres);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );
          this.parametresMetierSignal.set(null);
          return EMPTY;
        }),
        finalize(() => {
          this.chargementParametresMetierSignal.set(false);
        })
      )
      .subscribe();
  }

  private chargerTerrains(): void {
    this.chargementTerrainsSignal.set(true);

    this.terrainApiService.listerTerrainsActifs().pipe(
      tap(terrains => {
        this.terrainsSignal.set(terrains);

        const terrainIdActuel = this.terrainIdSignal();
        const terrainSelectionExiste =
          terrainIdActuel !== null
          && terrains.some(
            terrain => terrain.terrainId === terrainIdActuel
          );

        if (!terrainSelectionExiste) {
          this.terrainIdSignal.set(
            terrains.length > 0
              ? terrains[0].terrainId
              : null
          );
        }
      }),
      catchError(error => {
        this.messageErreurSignal.set(
          extraireMessageErreur(error)
        );
        this.terrainsSignal.set([]);
        this.terrainIdSignal.set(null);
        return EMPTY;
      }),
      finalize(() => {
        this.chargementTerrainsSignal.set(false);
      })
    ).subscribe();
  }
}
