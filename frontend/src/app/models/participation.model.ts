export type RoleParticipation = 'ORGANISATEUR' | 'JOUEUR';
export type ModeEntreeParticipation = 'CREATION' | 'INVITATION_PRIVEE' | 'INSCRIPTION_PUBLIQUE';
export type StatutParticipation = 'EN_ATTENTE_PAIEMENT' | 'CONFIRMEE' | 'LIBEREE';

export interface AjouterParticipantPriveRequest {
  matriculeJoueur: string;
}

export interface InscriptionPubliqueRequest {
  matriculeJoueur: string;
}

export interface ParticipationResponse {
  participationId: number;
  matchId: number;
  membreId: number;
  matricule: string;
  nom: string;
  prenom: string;
  roleParticipation: RoleParticipation;
  modeEntree: ModeEntreeParticipation;
  statutParticipation: StatutParticipation;
}
