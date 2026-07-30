import { routes } from './app.routes';

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
        .toHaveLength(17);
    }
  );
});
