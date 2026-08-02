import {
  NaturePaiement,
  StatutPaiement
} from './paiement.model';

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
  paiementId: number;
  detteId: number;
  membreId: number;
  matriculeMembre: string;
  naturePaiement: NaturePaiement;
  montant: number;
  statutPaiement: StatutPaiement;
  statutDette: StatutDette;
  dateHeurePaiement: string;
  dateReglementDette: string | null;
}
