import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Capacitor } from '@capacitor/core';
import { StorageService } from '../../auth/service/storage.service';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);

  if (Capacitor.isNativePlatform()) {
    return router.createUrlTree(['/app/dashboard']);
  }

  if (!StorageService.temTokenValido()) {
    StorageService.logout();
    return router.createUrlTree(['/login']);
  }

  if (!StorageService.eAdminSiteLogado()) {
    return router.createUrlTree(['/app/dashboard']);
  }

  return true;
};
