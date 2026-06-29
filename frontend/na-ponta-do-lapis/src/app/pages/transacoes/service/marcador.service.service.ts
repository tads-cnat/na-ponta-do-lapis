import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class MarcadorServiceService {
  http = Inject(HttpClient);

  private BASE_URL:string = environment.apiBaseUrl


}
