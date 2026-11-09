import {
  Component,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef,
  forwardRef,
  OnInit,
  OnDestroy,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { PreferenciaMoedaService } from '../../service/preferencia-moeda.service';

@Component({
  selector: 'app-input-moeda',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputMoedaComponent),
      multi: true
    }
  ],
  template: `
    <div
      (click)="focar()"
      [class]="obterClassesContainer()"
      [class.opacity-60]="disabled"
      [class.cursor-not-allowed]="disabled"
      [class.cursor-text]="!disabled">

      <!-- BADGE / PREFIXO DA MOEDA -->
      @if (tamanho === 'lg') {
        <div class="w-9 h-9 rounded-xl bg-indigo-50 text-indigo-600 font-bold text-sm flex items-center justify-center shrink-0 select-none shadow-2xs">
          {{ simbolo }}
        </div>
      } @else {
        <span class="font-bold text-slate-400 select-none shrink-0"
              [class.text-xs]="tamanho === 'sm'"
              [class.text-sm]="tamanho === 'md'">
          {{ simbolo }}
        </span>
      }

      <!-- INPUT NATIVO CONTROLADO -->
      <input
        #inputElement
        type="text"
        inputmode="numeric"
        [id]="inputId || null"
        [disabled]="disabled"
        [value]="valorFormatado"
        (input)="aoDigitar($event)"
        (focus)="aoFoco($event)"
        (blur)="aoDesfoco($event)"
        [placeholder]="placeholder"
        [class]="obterClassesInput()" />
    </div>
  `
})
export class InputMoedaComponent implements ControlValueAccessor, OnInit, OnDestroy {
  @ViewChild('inputElement') inputRef!: ElementRef<HTMLInputElement>;

  @Input() moeda?: string | null;
  @Input() placeholder: string = '0,00';
  @Input() tamanho: 'sm' | 'md' | 'lg' = 'md';
  @Input() inputId?: string;
  @Input() disabled: boolean | undefined = false;
  @Input() permitirVazio: boolean = false;
  @Input() classesContainer?: string;
  @Input() classesInput?: string;

  @Output() aoMudar = new EventEmitter<number | null>();
  @Output() aoFocar = new EventEmitter<FocusEvent>();
  @Output() aoDesfocar = new EventEmitter<FocusEvent>();

  public valorFormatado: string = '';
  private valorNumericoAtual: number | null = null;
  private subMoeda?: Subscription;

  private onChange: (valor: number | null) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(
    private preferenciaMoedaService: PreferenciaMoedaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.moeda) {
      this.subMoeda = this.preferenciaMoedaService.moeda$.subscribe(() => {
        this.cdr.markForCheck();
      });
    }
  }

  ngOnDestroy(): void {
    this.subMoeda?.unsubscribe();
  }

  get simbolo(): string {
    return this.preferenciaMoedaService.obterSimboloMoeda(this.moeda);
  }

  public focar(): void {
    if (!this.disabled && this.inputRef?.nativeElement) {
      this.inputRef.nativeElement.focus();
    }
  }

  // =========================================
  // CONTROL VALUE ACCESSOR
  // =========================================

  writeValue(valor: any): void {
    if (valor === null || valor === undefined || valor === '') {
      this.valorNumericoAtual = this.permitirVazio ? null : 0;
      this.valorFormatado = this.permitirVazio ? '' : '0,00';
    } else {
      const num = Number(valor);
      if (isNaN(num)) {
        this.valorNumericoAtual = this.permitirVazio ? null : 0;
        this.valorFormatado = this.permitirVazio ? '' : '0,00';
      } else {
        this.valorNumericoAtual = num;
        this.valorFormatado = num.toLocaleString('pt-BR', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        });
      }
    }
    this.cdr.markForCheck();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    this.cdr.markForCheck();
  }

  // =========================================
  // DIGITAÇÃO FLUIDA ESTILO FINTECH
  // =========================================

  public aoDigitar(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digitos = input.value.replace(/\D/g, '');

    if (!digitos) {
      if (this.permitirVazio) {
        this.valorNumericoAtual = null;
        this.valorFormatado = '';
        input.value = '';
      } else {
        this.valorNumericoAtual = 0;
        this.valorFormatado = '0,00';
        input.value = '0,00';
      }
      this.onChange(this.valorNumericoAtual);
      this.aoMudar.emit(this.valorNumericoAtual);
      return;
    }

    const valor = Number(digitos) / 100;
    this.valorNumericoAtual = valor;
    const formatado = valor.toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });

    this.valorFormatado = formatado;
    input.value = formatado;

    this.onChange(valor);
    this.aoMudar.emit(valor);
  }

  public aoFoco(event: FocusEvent): void {
    this.aoFocar.emit(event);
  }

  public aoDesfoco(event: FocusEvent): void {
    this.onTouched();
    this.aoDesfocar.emit(event);
  }

  // =========================================
  // ESTILOS DINÂMICOS
  // =========================================

  public obterClassesContainer(): string {
    if (this.classesContainer) {
      return this.classesContainer;
    }

    const base = 'flex items-center gap-2 border-2 transition-all w-full';

    switch (this.tamanho) {
      case 'sm':
        return `${base} px-3 py-1.5 bg-slate-50 border-slate-200 rounded-xl focus-within:border-indigo-600 focus-within:bg-white focus-within:ring-2 focus-within:ring-indigo-100`;
      case 'lg':
        return `${base} px-4 py-3 bg-slate-50 border-slate-200 rounded-2xl focus-within:border-indigo-600 focus-within:bg-white focus-within:ring-4 focus-within:ring-indigo-100 shadow-2xs`;
      case 'md':
      default:
        return `${base} px-3.5 py-2.5 bg-slate-50 border-slate-200 rounded-xl focus-within:border-indigo-600 focus-within:bg-white focus-within:ring-4 focus-within:ring-indigo-100 shadow-2xs`;
    }
  }

  public obterClassesInput(): string {
    if (this.classesInput) {
      return this.classesInput;
    }

    const base = 'w-full bg-transparent border-0 p-0 placeholder-slate-400 focus:outline-none focus:ring-0 disabled:cursor-not-allowed';

    switch (this.tamanho) {
      case 'sm':
        return `${base} text-sm font-semibold text-slate-800`;
      case 'lg':
        return `${base} text-2xl sm:text-3xl font-black text-slate-900 tracking-tight`;
      case 'md':
      default:
        return `${base} text-base font-bold text-slate-800`;
    }
  }
}

