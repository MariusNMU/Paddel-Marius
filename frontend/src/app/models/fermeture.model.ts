export type PorteeFermeture = 'GLOBALE' | 'LOCALE';

export interface CreerFermetureRequest {
  dateFermeture: string;
  portee: PorteeFermeture;
  siteId: number | null;
  motif: string;
}

export interface FermetureAdminResponse {
  fermetureId: number;
  dateFermeture: string;
  portee: PorteeFermeture;
  siteId: number | null;
  nomSite: string | null;
  motif: string | null;
  nombreMatchesAnnules: number;
}
