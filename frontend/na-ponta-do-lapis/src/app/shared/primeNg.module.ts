import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';



@NgModule({
  exports: [
     ButtonModule,
      InputTextModule,
      TableModule,
      CardModule,
      ToastModule
  ],
  declarations: [],
  imports: [CommonModule],
})
export class PrimeNGModuleModule {}
