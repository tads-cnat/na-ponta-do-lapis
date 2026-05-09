import { UsuarioResume } from "./IUsuario.models";

export interface ContasRequest{
    id:number,
    nome:string,
    saldo:number,
    usuarioResume:UsuarioResume,
}