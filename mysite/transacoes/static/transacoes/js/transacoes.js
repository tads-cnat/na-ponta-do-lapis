// Ordenação

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


// adicionar transacao 

form = document.getElementById("form_add_transacao")
form.addEventListener("submit", (event) => add_transacao(event))

async function add_transacao(event) {
    
    btn = event.submitter
    event.preventDefault();
    
    const formData = new FormData(form);
    const response = await fetch("/transacoes/adicionar_transacao/", {
        method: "POST",
        body: formData
    });

    if (btn.id == "btn_add_again"){
       
        const reListar = await fetch(`/transacoes/api/listar?order=data_hora`);
        const html = await reListar.text();
        table.innerHTML =  html
        form.reset()

    }else{
        window.location.href = "/transacoes/";
    }

}

// Fechar alert

setTimeout(() => {
    document.querySelectorAll('[data-alert]').forEach(alert => {
      alert.classList.add('opacity-0')

      setTimeout(() => alert.remove(), 500)
    })
  }, 2000)