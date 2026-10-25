package com.npl.na_ponta_do_lapis.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping(value = "/error", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String handleErrorHtml(HttpServletRequest request) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = (statusObj != null) ? Integer.parseInt(statusObj.toString()) : 404;
        String requestedUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (requestedUri == null) {
            requestedUri = request.getRequestURI();
        }

        String title;
        String subtitle;
        if (status == 404) {
            title = "Rota não encontrada";
            subtitle = "O endpoint solicitado não existe nesta versão da API.";
        } else if (status == 403) {
            title = "Acesso não autorizado";
            subtitle = "Você não possui permissão para acessar este recurso da API.";
        } else {
            title = "Erro no servidor";
            subtitle = "Ocorreu uma instabilidade temporária ao processar sua solicitação.";
        }

        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Na Ponta do Lápis | %d</title>
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Urbanist:wght@500;700;800&display=swap" rel="stylesheet">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Urbanist', -apple-system, BlinkMacSystemFont, sans-serif;
                        background: radial-gradient(circle at top, #1e1638 0%%, #0f0c20 100%%);
                        color: #f8fafc;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 1.5rem;
                    }
                    .card {
                        background: rgba(30, 24, 56, 0.85);
                        backdrop-filter: blur(16px);
                        border: 1px solid rgba(120, 67, 205, 0.25);
                        border-radius: 1.5rem;
                        padding: 2.5rem 2rem;
                        max-width: 520px;
                        width: 100%%;
                        text-align: center;
                        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6);
                    }
                    .badge {
                        display: inline-block;
                        background: rgba(120, 67, 205, 0.2);
                        color: #FFDE59;
                        border: 1px solid rgba(255, 222, 89, 0.35);
                        font-weight: 700;
                        font-size: 0.8rem;
                        padding: 0.35rem 1rem;
                        border-radius: 9999px;
                        margin-bottom: 1.5rem;
                        letter-spacing: 0.05em;
                    }
                    .code {
                        font-size: 5rem;
                        font-weight: 800;
                        line-height: 1;
                        background: linear-gradient(135deg, #7843CD 0%%, #FFDE59 100%%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        margin-bottom: 1rem;
                    }
                    h1 {
                        font-size: 1.5rem;
                        font-weight: 700;
                        color: #f8fafc;
                        margin-bottom: 0.75rem;
                    }
                    p {
                        color: #cbd5e1;
                        font-size: 1rem;
                        line-height: 1.6;
                        margin-bottom: 1.5rem;
                    }
                    .path-box {
                        background: #0d0a1c;
                        border: 1px dashed rgba(255, 222, 89, 0.25);
                        padding: 0.6rem 1rem;
                        border-radius: 0.5rem;
                        font-family: monospace;
                        font-size: 0.85rem;
                        color: #e2e8f0;
                        margin-bottom: 2rem;
                        word-break: break-all;
                    }
                    .actions {
                        display: flex;
                        flex-direction: column;
                        gap: 0.75rem;
                    }
                    .btn {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        padding: 0.85rem 1.5rem;
                        border-radius: 0.75rem;
                        font-weight: 700;
                        font-size: 0.95rem;
                        text-decoration: none;
                        transition: all 0.2s ease;
                    }
                    .btn-primary {
                        background: #7843CD;
                        color: #ffffff;
                    }
                    .btn-primary:hover {
                        background: #6533b3;
                        transform: translateY(-2px);
                        box-shadow: 0 10px 20px -5px rgba(120, 67, 205, 0.5);
                    }
                    .btn-secondary {
                        background: rgba(255, 255, 255, 0.05);
                        color: #f8fafc;
                        border: 1px solid rgba(255, 255, 255, 0.2);
                    }
                    .btn-secondary:hover {
                        background: rgba(255, 255, 255, 0.1);
                        transform: translateY(-2px);
                    }
                    .footer {
                        margin-top: 2rem;
                        font-size: 0.8rem;
                        color: #94a3b8;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <span class="badge">NA PONTA DO LÁPIS | API</span>
                    <div class="code">%d</div>
                    <h1>%s</h1>
                    <p>%s</p>
                    <div class="path-box">Rota: %s</div>
                    <div class="actions">
                        <a href="/api/docs" class="btn btn-primary">Acessar Documentação Swagger</a>
                        <a href="https://napontadolapis.app" class="btn btn-secondary">Ir para a Aplicação Web</a>
                    </div>
                    <div class="footer">
                        Na Ponta do Lápis API | Versão 1.0.0
                    </div>
                </div>
            </body>
            </html>
            """.formatted(status, status, title, subtitle, requestedUri);
    }

    @RequestMapping(value = "/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleErrorJson(HttpServletRequest request) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = (statusObj != null) ? Integer.parseInt(statusObj.toString()) : 404;
        String requestedUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (requestedUri == null) {
            requestedUri = request.getRequestURI();
        }

        HttpStatus httpStatus;
        try {
            httpStatus = HttpStatus.valueOf(status);
        } catch (Exception e) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String message;
        if (status == 404) {
            message = "Endpoint não encontrado na API.";
        } else if (status == 403) {
            message = "Acesso não autorizado ao recurso solicitado.";
        } else {
            message = "Ocorreu um erro interno no servidor.";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", httpStatus.getReasonPhrase());
        body.put("message", message);
        body.put("path", requestedUri);

        return ResponseEntity.status(httpStatus).body(body);
    }
}
