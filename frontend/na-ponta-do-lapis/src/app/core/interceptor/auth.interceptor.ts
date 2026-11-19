import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { StorageService } from '../../auth/service/storage.service';
import { environment } from '../../../environments/environment';
import { ConexaoService } from '../../shared/service/conexao.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const conexaoService = inject(ConexaoService);
  const authToken = StorageService.getToken();
  const headers: Record<string, string> = {
    Authorization: `Bearer ${authToken}`,
  };

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
      if (
        err instanceof HttpErrorResponse &&
        err.status === 0 &&
        (typeof navigator === 'undefined' || !navigator.onLine || req.url.startsWith(environment.apiBaseUrl))
      ) {
        conexaoService.marcarOffline();
      }
      return throwError(() => err);
    })
  );
};
