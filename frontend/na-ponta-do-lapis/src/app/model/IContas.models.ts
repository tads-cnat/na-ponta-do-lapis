export interface IContaFinanceira {
    id: number | null,
    nome: string,
    saldo: number,
    cor: string | undefined,
    usuarioResume: UsuarioResume | null,
}

export interface UsuarioResume {
  id: number;
  nome: string;
}

export interface IContaFinanceiraRequest {
    nome: string;
    saldo: number;
    cor: string;
    idUsuario: number
}
