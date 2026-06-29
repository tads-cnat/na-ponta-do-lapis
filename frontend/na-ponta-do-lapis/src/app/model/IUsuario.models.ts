export interface UsuarioResume {
    id:number,
    nome:string
}

export interface Usuario {
    id:number,
    nome:string,
    email:string,
    username:string,
    fotoPerfil:string,
    papel:string,
    idFamilia?: number | null,
} 