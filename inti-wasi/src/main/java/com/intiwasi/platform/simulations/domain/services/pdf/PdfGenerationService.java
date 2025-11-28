package com.intiwasi.platform.simulations.domain.services.pdf;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Servicio de dominio para generar el PDF del cronograma de pagos.
 */
public interface PdfGenerationService {

    /**
     * Genera el PDF del cronograma de pagos (método francés, meses de 30 días)
     * para la simulación indicada.
     *
     * @param simulation simulación ya calculada (con monto financiado, tasas, etc.)
     * @return arreglo de bytes del archivo PDF
     */
    byte[] generatePaymentSchedulePdf(Simulation simulation);
}