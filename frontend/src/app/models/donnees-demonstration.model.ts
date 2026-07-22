import { SiteResponse } from './site.model';

export interface CategorieMembreDemoResponse {
  prefixe: string;
  categorie: string;
  regle: string;
}

export interface CompteJoueurDemoResponse {
  matricule: string;
  motDePasse: string;
  description: string;
}

export interface CompteAdministrateurDemoResponse {
  login: string;
  motDePasse: string;
  description: string;
}

export interface PresentationDemoResponse {
  categoriesMembres: CategorieMembreDemoResponse[];
  sites: SiteResponse[];
  joueurs: CompteJoueurDemoResponse[];
  administrateurs: CompteAdministrateurDemoResponse[];
}
