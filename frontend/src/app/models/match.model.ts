export type ModeCreation = 'PRIVE' | 'PUBLIC';
export type VisibiliteMatch = 'PRIVE' | 'PUBLIC';
export type EtatCycleMatch = 'A_VENIR' | 'DEMARRE' | 'TERMINE';

export interface CreerMatchRequest {
  terrainId: number;
  matriculeOrganisateur: string;
  dateHeureDebut: string;
  modeCreation: ModeCreation;
}

export interface MatchResponse {
  matchId: number;
  terrainId: number;
  numeroTerrain: string;
  siteId: number;
  nomSite: string;
  dateHeureDebut: string;
  dateHeureFin: string;
  modeCreation: ModeCreation;
  visibiliteCourante: VisibiliteMatch;
  prixTotal: number;
  etatCycle: EtatCycleMatch;
}
