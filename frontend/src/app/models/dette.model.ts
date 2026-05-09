export type StatutDette = 'OUVERTE' | 'REGLEE';

export interface DetteResponse {
  detteId: number;
  matchId: number;
  membreResponsableId: number;
  matriculeResponsable: string;
  montantInitial: number;
  montantRestant: number;
  statutDette: StatutDette;
  dateCreation: string;
  dateReglement: string | null;
}

export interface PayerDetteRequest {
  montant: number;
}

export interface PaiementDetteResponse {
  dette: DetteResponse;
  paiementId: number;
  montantPaye: number;
}
