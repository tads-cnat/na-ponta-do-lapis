import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { PasswordModule } from 'primeng/password';
import { TagModule } from 'primeng/tag';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { FloatLabelModule } from 'primeng/floatlabel';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { RadioButtonModule } from 'primeng/radiobutton';



@NgModule({
  exports: [
     ButtonModule,
      InputTextModule,
      TableModule,
      CardModule,
      ToastModule,
      PasswordModule,
      TagModule,
      IconFieldModule,
      InputIconModule,
      FloatLabelModule,
      DialogModule,
      InputNumberModule,
      SelectModule,
      RadioButtonModule
  ],
  declarations: [],
  imports: [CommonModule],
})
export class PrimeNGModuleModule {}
