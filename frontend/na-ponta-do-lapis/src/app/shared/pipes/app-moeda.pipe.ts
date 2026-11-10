import { Pipe, PipeTransform } from '@angular/core';
import { PreferenciaMoedaService } from '../service/preferencia-moeda.service';

@Pipe({
  name: 'appMoeda',
  standalone: true,
  pure: false
})
export class AppMoedaPipe implements PipeTransform {
  constructor(private preferenciaMoedaService: PreferenciaMoedaService) {}

  transform(value: number | string | null | undefined, moedaSobrescrita?: string | null): string {
    if (value === null || value === undefined || value === '') {
      return this.formatar(0, moedaSobrescrita);
    }

    const num = typeof value === 'string' ? Number(value.replace(',', '.')) : Number(value);
    if (isNaN(num)) {
      return this.formatar(0, moedaSobrescrita);
    }

    return this.formatar(num, moedaSobrescrita);
  }

  private formatar(valor: number, moedaSobrescrita?: string | null): string {
    const cod = (moedaSobrescrita || this.preferenciaMoedaService.obterMoedaAtiva()).toUpperCase();
    const simbolo = this.preferenciaMoedaService.obterSimboloMoeda(cod);
    const locale = this.preferenciaMoedaService.obterLocaleMoeda(cod);

    const valorFormatado = valor.toLocaleString(locale, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });

    return `${simbolo} ${valorFormatado}`;
  }
}

