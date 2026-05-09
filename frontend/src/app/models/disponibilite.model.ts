export interface CreneauDisponibiliteResponse {
  terrainId: number;
  numeroTerrain: string;
  dateHeureDebut: string;
  dateHeureFin: string;
}

export interface DisponibilitesResponse {
  siteId: number;
  nomSite: string;
  date: string;
  ferme: boolean;
  motifFermeture: string | null;
  creneaux: CreneauDisponibiliteResponse[];
}
