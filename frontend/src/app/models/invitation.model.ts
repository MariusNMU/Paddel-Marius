export type StatutParticipation = 'EN_ATTENTE_PAIEMENT' | 'CONFIRMEE' | 'LIBEREE';

export interface InviterJoueurPriveRequest {
  matriculeOrganisateur: string;
  matriculeInvite: string;
}

export interface DeclinerInvitationRequest {
  matriculeJoueur: string;
}

export interface InvitationPriveeResponse {
  participationId: number;
  matchId: number;
  siteId: number;
  nomSite: string;
  terrainId: number;
  numeroTerrain: string;
  dateHeureDebut: string;
  dateHeureFin: string;

  organisateurId: number;
  matriculeOrganisateur: string;
  nomOrganisateur: string;
  prenomOrganisateur: string;

  joueurInviteId: number;
  matriculeInvite: string;
  nomInvite: string;
  prenomInvite: string;

  statutParticipation: StatutParticipation;
}
