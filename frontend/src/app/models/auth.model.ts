export type CategorieMembre = 'GLOBAL' | 'SITE' | 'LIBRE';
export type RoleAdministrateur = 'GLOBAL' | 'SITE';

export interface ConnexionJoueurRequest {
  matricule: string;
  motDePasse: string;
}

export interface AuthJoueurResponse {
  membreId: number;
  matricule: string;
  nom: string;
  prenom: string;
  categorieMembre: CategorieMembre;
  siteRattachementId: number | null;
  nomSiteRattachement: string | null;
  actif: boolean;
}

export interface ConnexionAdminRequest {
  login: string;
  motDePasse: string;
}

export interface AuthAdminResponse {
  administrateurId: number;
  login: string;
  nom: string;
  prenom: string;
  roleAdministrateur: RoleAdministrateur;
  siteId: number | null;
  nomSite: string | null;
  actif: boolean;
}
