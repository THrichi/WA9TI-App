package com.application.wa9ti.services.subscription;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

@Service
@AllArgsConstructor
public class InvoicePdfService {

    private final TemplateEngine templateEngine;


    public String generateInvoicePdf(String invoiceNumber, Long ownerId, Map<String, Object> data) {
        String pdfPath = "invoices/" + ownerId + "/" + invoiceNumber + ".pdf";
        File file = new File(pdfPath);
        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new RuntimeException("Impossible de créer le répertoire : " + parentDir.getAbsolutePath());
        }


        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {

            // Générer le HTML avec Thymeleaf
            Context context = new Context();
            context.setVariables(data);
            String html = templateEngine.process("invoice", context);

            // Convertir HTML en PDF
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            // Sauvegarde du PDF
            fileOutputStream.write(outputStream.toByteArray());

            return pdfPath;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}
