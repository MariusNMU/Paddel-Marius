export interface TerrainReservationInfoResponse {
  terrainId: number;
  numeroTerrain: string;
}

export interface SiteReservationInfoResponse {
  siteId: number;
  codeSite: string;
  nomSite: string;
  heureDebutReservation: string;
  heureFinReservation: string;
  terrains: TerrainReservationInfoResponse[];
}
