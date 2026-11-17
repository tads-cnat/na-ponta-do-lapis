export interface Token {
    token:string
}

export interface TokenPayload {
  sub: string;
  email: string;
  papeis: string[];
  exp: number;
}