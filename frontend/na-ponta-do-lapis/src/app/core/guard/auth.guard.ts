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

  const usuario = StorageService.getUsuarioDecodificado();
  if (usuario?.mustChangePassword && !state.url.startsWith('/app/perfil')) {
    router.navigateByUrl('/app/perfil');
    return false;
  }

  return true;
};
