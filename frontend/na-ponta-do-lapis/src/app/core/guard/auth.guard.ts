import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { StorageService } from '../../auth/service/storage.service';

export const authGuard: CanActivateFn = (route, state) => {

  const router = inject(Router)

  const token = localStorage.getItem('token')

  if (!token) {
    router.navigateByUrl('/login')
    return false
  }

  return true;
};
