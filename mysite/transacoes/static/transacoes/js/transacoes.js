document.getElementById("btn_estado").addEventListener("click", () => ordenar("estado"));
document.getElementById("btn_data_hora").addEventListener("click", () => ordenar("data_hora"));;
document.getElementById("btn_valor").addEventListener("click", () => ordenar("valor"));;
document.getElementById("btn_descricao").addEventListener("click", () => ordenar("descricao"));;
document.getElementById("btn_categoria").addEventListener("click", () => ordenar("categoria"));;
document.getElementById("btn_conta").addEventListener("click", () => ordenar("conta_financeira"));;

let table = document.querySelector("#t_body")

let ultimaOrdem= null;
let direcaoOrdem= "asc";

async function ordenar (order) {
    console.log("Apertado");
    if(ultimaOrdem == order){
        if(direcaoOrdem == "asc"){
            direcaoOrdem = "desc"
        }else{
            direcaoOrdem = "asc"
        }
    }
    const response = await fetch(`/transacoes/api/listar?order=${order}&direcao=${direcaoOrdem}`);
    const html = await response.text();
    table.innerHTML =  html

    ultimaOrdem = order;

};


