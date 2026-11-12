declare module '@edusites/bandeiras-paises/core' {
  export interface SvgPaisOpcoes {
    nome: string;
    tamanho?: number | string;
    redonda?: boolean;
    className?: string;
  }
  export function svgPais(opcoes: SvgPaisOpcoes | string): string | null;
  export function svgPaisAsync(opcoes: SvgPaisOpcoes | string): Promise<string | null>;
  export function temPais(codigoOuNome: string): boolean;
  export function listarPaises(): Array<{ nome: string; iso: string; ddi: string }>;
  export function obterPais(codigoOuNome: string): { nome: string; iso: string; ddi: string } | null;
  export function obterCodigoPais(nome: string): string | null;
}

declare module '@edusites/bandeiras-paises' {
  export * from '@edusites/bandeiras-paises/core';
}

