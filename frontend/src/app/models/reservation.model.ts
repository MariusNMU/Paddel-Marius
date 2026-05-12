export type RoleParticipation = 'ORGANISATEUR' | 'JOUEUR';
export type ModeEntreeParticipation = 'CREATION' | 'INVITATION_PRIVEE' | 'INSCRIPTION_PUBLIQUE';
export type StatutParticipation = 'EN_ATTENTE_PAIEMENT' | 'CONFIRMEE' | 'LIBEREE';
export type ModeCreation = 'PRIVE' | 'PUBLIC';
export type VisibiliteMatch = 'PRIVE' | 'PUBLIC';
export type EtatCycleMatch = 'A_VENIR' | 'DEMARRE' | 'TERMINE' | 'ANNULE';

export interface ReservationJoueurResponse {
  participationId: number;
  matchId: number;
  siteId: number;
  nomSite: string;
  terrainId: number;
  numeroTerrain: string;
  dateHeureDebut: string;
  dateHeureFin: string;
  roleParticipation: RoleParticipation;
  modeEntree: ModeEntreeParticipation;
  statutParticipation: StatutParticipation;
  modeCreation: ModeCreation;
  visibiliteCourante: VisibiliteMatch;
  etatCycle: EtatCycleMatch;
  prixTotal: number;
}
