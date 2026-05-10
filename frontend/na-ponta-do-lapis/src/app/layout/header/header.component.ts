import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { Route, Router } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';
import { StorageService } from '../../auth/service/storage.service';

@Component({
  selector: 'app-header',
  imports: [PrimeNGModuleModule],
  templateUrl: './header.component.html',
  providers: [MessageService],
  styleUrl: './header.component.css',
})
export class HeaderComponent {

  constructor(private router:Router){}

  logout(){
    StorageService.logout()
    this.router.navigateByUrl("/login")
  }
}
