import { HttpInterceptorFn } from '@angular/common/http';
import { StorageService } from '../../auth/service/storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authToken = StorageService.getToken();
  // console.log(req.url)
  // console.log(authToken)

  const authReq = req.clone({
    setHeaders:{
      Authorization: `Bearer ${authToken}`
    } 
  })

  return next(authReq);
};
