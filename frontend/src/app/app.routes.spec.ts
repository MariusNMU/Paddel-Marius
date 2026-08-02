import { routes } from './app.routes';
import { adminGlobalGuard } from './guards/admin-global.guard';

describe('routes', () => {
  it(
    'doit charger paresseusement toutes les pages routées',
    () => {
      const routesAvecComposant =
        routes.filter(route => route.component);

      const routesParesseuses =
        routes.filter(route => route.loadComponent);

      expect(routesAvecComposant)
        .toHaveLength(0);

      expect(routesParesseuses)
        .toHaveLength(19);
    }
  );

  it(
    'doit réserver le traitement d échéance à l admin global',
    () => {
      const route =
        routes.find(
          element =>
            element.path
            === 'admin/traitement-echeance'
        );

      expect(route)
        .toBeDefined();

      expect(route?.canActivate)
        .toContain(adminGlobalGuard);
    }
  );
});
