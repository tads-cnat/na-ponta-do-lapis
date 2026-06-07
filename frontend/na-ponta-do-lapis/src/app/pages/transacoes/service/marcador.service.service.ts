import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class MarcadorServiceService {
  http = Inject(HttpClient);
  
  private BASE_URL:string = "http://localhost:8080"


}
