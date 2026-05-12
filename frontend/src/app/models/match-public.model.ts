export interface MatchPublicResponse {
  matchId: number;
  siteId: number;
  nomSite: string;
  terrainId: number;
  numeroTerrain: string;
  dateHeureDebut: string;
  dateHeureFin: string;
  nombreParticipantsActifs: number;
  placesDisponibles: number;
  prixTotal: number;
  montantParticipation: number;
}

export interface RejoindreMatchPublicRequest {
  matriculeJoueur: string;
}

export interface RejoindreMatchPublicResponse {
  matchId: number;
  participationId: number;
  paiementId: number;
  matriculeJoueur: string;
  montantPaye: number;
  statutParticipation: string;
  soldeRestant: number;
}
