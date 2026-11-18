import { HttpInterceptorFn } from '@angular/common/http';
import { StorageService } from '../../auth/service/storage.service';
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
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

  return next(req.clone({ setHeaders: headers }));
};
