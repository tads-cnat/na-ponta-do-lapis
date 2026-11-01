package com.npl.na_ponta_do_lapis.service.fatura;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * OCR local de PDFs escaneados usando Tesseract.
 *
 * O caminho padrão preserva a LGPD: o PDF não sai do servidor. Se o OCR falhar,
 * o frontend oferece o envio multimodal com consentimento explícito.
 */
@Service
public class FaturaOcrService {

    private static final Logger log = LoggerFactory.getLogger(FaturaOcrService.class);

    @Value("${fatura.ocr.enabled:true}")
    private boolean habilitado;

    @Value("${fatura.ocr.dpi:300}")
    private int dpi;

    @Value("${fatura.ocr.timeout-seconds:30}")
    private long timeoutSegundos;

    @Value("${fatura.ocr.max-pages:10}")
    private int maxPaginas;

    @Value("${fatura.regex.max-text-chars:300000}")
    private int maxCaracteres;

    public boolean habilitado() {
        return habilitado;
    }

    public String extrairTexto(byte[] pdf) {
        if (!habilitado || pdf == null || pdf.length == 0) {
            return "";
        }

        Path diretorio = null;
        try {
            diretorio = Files.createTempDirectory("npl-fatura-ocr-");
            try (PDDocument documento = Loader.loadPDF(pdf)) {
                PDFRenderer renderer = new PDFRenderer(documento);
                int paginas = Math.min(documento.getNumberOfPages(), maxPaginas);
                StringBuilder texto = new StringBuilder();

                for (int pagina = 0; pagina < paginas; pagina++) {
                    BufferedImage imagem = renderer.renderImageWithDPI(pagina, dpi, ImageType.RGB);
                    Path arquivo = diretorio.resolve("pagina-" + (pagina + 1) + ".png");
                    ImageIO.write(imagem, "png", arquivo.toFile());

                    String textoPagina = executarTesseract(arquivo);
                    if (!textoPagina.isBlank()) {
                        texto.append(textoPagina).append('\n');
                    }
                    Files.deleteIfExists(arquivo);

                    if (texto.length() >= maxCaracteres) {
                        break;
                    }
                }
                return texto.length() > maxCaracteres
                        ? texto.substring(0, maxCaracteres)
                        : texto.toString();
            }
        } catch (Exception e) {
            log.warn("OCR local não pôde ser concluído: {}", e.getMessage());
            return "";
        } finally {
            apagarDiretorio(diretorio);
        }
    }

    private String executarTesseract(Path imagem) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "tesseract", imagem.toString(), "stdout", "-l", "por", "--psm", "6");
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process processo = builder.start();

        boolean finalizou = processo.waitFor(timeoutSegundos, TimeUnit.SECONDS);
        if (!finalizou) {
            processo.destroyForcibly();
            log.warn("Tesseract excedeu {} s na página {}; seguindo sem OCR desta página.",
                    timeoutSegundos, imagem.getFileName());
            return "";
        }
        if (processo.exitValue() != 0) {
            log.warn("Tesseract retornou código {} para a página {}.",
                    processo.exitValue(), imagem.getFileName());
            return "";
        }
        return new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void apagarDiretorio(Path diretorio) {
        if (diretorio == null) {
            return;
        }
        try (var caminhos = Files.walk(diretorio)) {
            caminhos.sorted(Comparator.reverseOrder()).forEach(caminho -> {
                try {
                    Files.deleteIfExists(caminho);
                } catch (IOException ignored) {
                    // melhor esforço
                }
            });
        } catch (IOException ignored) {
            // melhor esforço
        }
    }
}
