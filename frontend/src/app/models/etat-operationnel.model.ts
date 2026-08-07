import {
  EtatCycleMatch,
  VisibiliteMatch
} from './match.model';

export type EtatTerrainOperationnel =
  'DISPONIBLE'
  | 'RESERVE'
  | 'FERME'
  | 'INACTIF';

export interface MatchEtatAdminResponse {
  matchId: number;
  dateHeureDebut: string;
  dateHeureFin: string;
  visibiliteCourante: VisibiliteMatch;
  etatCycle: EtatCycleMatch;
  nombreParticipants: number;
}

export interface TerrainEtatAdminResponse {
  terrainId: number;
  numeroTerrain: string;
  actif: boolean;
  etatTerrain: EtatTerrainOperationnel;
  matches: MatchEtatAdminResponse[];
}

export interface EtatOperationnelAdminResponse {
  date: string;
  siteId: number;
  nomSite: string;
  siteActif: boolean;
  ferme: boolean;
  motifFermeture: string | null;
  terrains: TerrainEtatAdminResponse[];
}

export interface OccupationHebdomadaireAdminResponse {
  dateDebut: string;
  dateFin: string;
  siteId: number;
  nomSite: string;
  siteActif: boolean;
  jours: EtatOperationnelAdminResponse[];
}
