export interface LoginRequest {
    email:string,
    password:string
}

export interface SignupRequest {
    nomeCompleto: string,
    email:string,
    username:string
    telefone:string,
    password:string,
    passwordConfirmation:string
}