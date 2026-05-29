import { Component } from '@angular/core';
import { MetaItemComponent } from './components/meta-item/meta-item.component';
import { Button } from 'primeng/button';

@Component({
  selector: 'app-metas',
  imports: [MetaItemComponent, Button],
  templateUrl: './metas.component.html',
  styleUrl: './metas.component.css',
})
export class MetasComponent {
  
}
