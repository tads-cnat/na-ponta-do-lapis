import { Injectable } from '@angular/core';

const CACHE_PREFIX = 'npl_cache_v1_';

/**
 * Cache local leve (stale-while-revalidate) para exibir os últimos dados
 * imediatamente ao abrir o app enquanto revalida em segundo plano.
 */
@Injectable({
  providedIn: 'root',
})
export class CacheLocalService {
  obter<T>(chave: string): T | null {
    if (typeof localStorage === 'undefined') return null;
    try {
      const raw = localStorage.getItem(CACHE_PREFIX + chave);
      if (!raw) return null;
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }

  salvar<T>(chave: string, valor: T): void {
    if (typeof localStorage === 'undefined') return;
    try {
      localStorage.setItem(CACHE_PREFIX + chave, JSON.stringify(valor));
    } catch {
      // Ignora caso storage esteja cheio ou indisponível
    }
  }

  static limparTudo(): void {
    if (typeof localStorage === 'undefined') return;
    try {
      for (let i = localStorage.length - 1; i >= 0; i--) {
        const key = localStorage.key(i);
        if (key?.startsWith(CACHE_PREFIX)) {
          localStorage.removeItem(key);
        }
      }
    } catch {
      // Ignora erro de acesso ao storage
    }
  }
}

