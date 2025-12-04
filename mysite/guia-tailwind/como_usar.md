# Guia de uso do Tailwind CSS no Django com Daisy UI na IDE VS Code

**Coloque isto no head do seu arquivo HTML:**

```html
 {% load static %}

    <link rel="stylesheet" href="{% static 'admin/css/tailwind_css/dist/output.css' %}">
```

Com dois terminais abertos, execute ```python manager.py runserver``` para rodar a aplicação que deseja verificar a formatação da ferramenta e no outro execute ```npm run watch:css``` no diretório do projeto para que o framework faça efeito no seu código

## Principal comando para compilar as classes do Tailwind

```bash
npm run watch:css
```

Instalar esta extensão do VS Code para facilitar a modificação e manipulação de suas classes: "Tailwind CSS IntelliSense"

---

Caso esteja com dúvidas, acesse este vídeo como guia de como instalá-lo e usá-lo:

[Tailwind CSS para Django com Daisy UI](https://www.youtube.com/watch?v=rx1PqrqLwZk)

## Documentação Oficial do Framework

[Tailwind CSS](https://tailwindcss.com/docs/installation/tailwind-cli)  

## Autores deste documento

- Criado por: Lucaasshq

- Editado por: DuduPOG
