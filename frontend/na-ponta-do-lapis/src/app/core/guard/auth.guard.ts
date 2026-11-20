import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { StorageService } from '../../auth/service/storage.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);

  if (!StorageService.temTokenValido()) {
    StorageService.logout();
    return router.createUrlTree(['/login']);
  }

  return true;
};
