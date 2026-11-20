import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { StorageService } from '../../auth/service/storage.service';
import { environment } from '../../../environments/environment';
import { ConexaoService } from '../../shared/service/conexao.service';

let redirecionandoSessaoExpirada = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const conexaoService = inject(ConexaoService);
  const router = inject(Router);
  const authToken = StorageService.getToken();
  const headers: Record<string, string> = {};

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  if (
    req.url.startsWith(environment.apiBaseUrl) &&
    environment.cfAccessClientId &&
    environment.cfAccessClientSecret
  ) {
    headers['CF-Access-Client-Id'] = environment.cfAccessClientId;
    headers['CF-Access-Client-Secret'] = environment.cfAccessClientSecret;
  }

  return next(req.clone({ setHeaders: headers })).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        if (
          err.status === 0 &&
          (typeof navigator === 'undefined' || !navigator.onLine || req.url.startsWith(environment.apiBaseUrl))
        ) {
          conexaoService.marcarOffline();
        } else if (
          err.status === 401 &&
          req.url.startsWith(environment.apiBaseUrl) &&
          !req.url.includes('/auth/')
        ) {
          StorageService.logout();
          if (!redirecionandoSessaoExpirada && !router.url.startsWith('/login')) {
            redirecionandoSessaoExpirada = true;
            router.navigateByUrl('/login').finally(() => {
              redirecionandoSessaoExpirada = false;
            });
          }
        }
      }
      return throwError(() => err);
    })
  );
};
