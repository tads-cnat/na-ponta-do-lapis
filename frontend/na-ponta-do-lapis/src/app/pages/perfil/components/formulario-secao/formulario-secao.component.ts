import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

export interface CampoFormulario {
  key: string;
  label: string;
  placeholder: string;
  type?: string;
}

@Component({
  selector: 'app-formulario-secao',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './formulario-secao.component.html'
})
export class FormularioSecaoComponent {
  @Input() titulo: string = '';
  @Input() campos: CampoFormulario[] = [];
  @Input() formGroupPai!: FormGroup;
}