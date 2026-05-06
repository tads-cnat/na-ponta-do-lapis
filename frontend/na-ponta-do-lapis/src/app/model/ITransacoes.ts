export type EstadoTransacao = 'PENDENTE' | 'CONCLUIDO' | 'CANCELADO'
export type Conta = 'Nubank' | 'Santander' | 'Itau' | 'Caixa'

export interface ITransacoes {
    id: number;
    descricao:string;
    categoria:string;
    estado:EstadoTransacao;
    tipoTransacao: 'RECEITA' | 'DESPESA';
    dataHora:Date;
    conta:Conta;
    valor:Number;
}