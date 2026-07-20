import {
  signal,
  type WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import {
  provideRouter
} from '@angular/router';
import {
  AuthAdminResponse
} from '../../models/auth.model';
import {
  AuthFacadeService
} from '../../services/auth-facade.service';
import {
  AdminDashboardComponent
} from './admin-dashboard.component';

describe(
  'AdminDashboardComponent',
  () => {
    let fixture:
      ComponentFixture<AdminDashboardComponent>;

    let component:
      AdminDashboardComponent;

    let adminSignal:
      WritableSignal<AuthAdminResponse | null>;

    let authFacade: {
      admin:
        WritableSignal<AuthAdminResponse | null>;
      deconnecterAdmin:
        ReturnType<typeof vi.fn>;
    };

    const adminGlobal:
      AuthAdminResponse = {
      administrateurId: 1,
      login: 'admin-global',
      nom: 'Admin',
      prenom: 'Global',
      roleAdministrateur: 'GLOBAL',
      siteId: null,
      nomSite: null,
      actif: true
    };

    const adminSite:
      AuthAdminResponse = {
      administrateurId: 2,
      login: 'admin-bruxelles',
      nom: 'Admin',
      prenom: 'Bruxelles',
      roleAdministrateur: 'SITE',
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
      actif: true
    };

    beforeEach(async () => {
      adminSignal =
        signal<AuthAdminResponse | null>(
          adminGlobal
        );

      authFacade = {
        admin: adminSignal,
        deconnecterAdmin: vi.fn()
      };

      await TestBed
        .configureTestingModule({
          imports: [
            AdminDashboardComponent
          ],
          providers: [
            provideRouter([]),
            {
              provide:
              AuthFacadeService,
              useValue:
              authFacade
            }
          ]
        })
        .compileComponents();

      fixture =
        TestBed.createComponent(
          AdminDashboardComponent
        );

      component =
        fixture.componentInstance;

      fixture.detectChanges();
    });

    it(
      'doit créer le composant',
      () => {
        expect(component)
          .toBeTruthy();
      }
    );

    it(
      'doit afficher les informations de l admin GLOBAL',
      () => {
        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Administrateur connecté'
          );

        expect(contenu)
          .toContain(
            'Global Admin'
          );

        expect(contenu)
          .toContain(
            'admin-global'
          );

        expect(contenu)
          .toContain(
            'accès global à tous les sites'
          );

        expect(contenu)
          .toContain(
            'Traitement de veille'
          );
      }
    );

    it(
      'doit limiter visuellement l admin SITE',
      () => {
        adminSignal.set(adminSite);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Padel Bruxelles'
          );

        expect(contenu)
          .toContain(
            'limité au site'
          );

        expect(contenu)
          .not.toContain(
          'Traitement de veille'
        );
      }
    );

    it(
      'doit afficher un message sans administrateur connecté',
      () => {
        adminSignal.set(null);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Aucun administrateur connecté'
          );

        expect(contenu)
          .toContain(
            'Aller à la connexion admin'
          );
      }
    );

    it(
      'doit déléguer la déconnexion à la façade',
      () => {
        const bouton =
          fixture.nativeElement
            .querySelector(
              'button.danger-button'
            ) as HTMLButtonElement;

        expect(bouton)
          .not.toBeNull();

        bouton.click();

        expect(
          authFacade.deconnecterAdmin
        ).toHaveBeenCalledTimes(1);
      }
    );
  }
);
