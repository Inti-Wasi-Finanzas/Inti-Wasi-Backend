package com.intiwasi.platform.simulations.application.internal.pdfservices;

import com.intiwasi.platform.simulations.domain.model.aggregates.Simulation;
import com.intiwasi.platform.simulations.domain.model.valueobjects.PaymentScheduleEntry;
import com.intiwasi.platform.simulations.domain.services.LoanCalculatorDomainService;
import com.intiwasi.platform.simulations.domain.services.pdf.PdfGenerationService;
import com.lowagie.text.Chunk;
import java.awt.Color;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implementación del generador de PDF usando iText (lowagie).
 */
@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final LoanCalculatorDomainService loanCalculatorDomainService;

    public PdfGenerationServiceImpl(LoanCalculatorDomainService loanCalculatorDomainService) {
        this.loanCalculatorDomainService = loanCalculatorDomainService;
    }

    @Override
    public byte[] generatePaymentSchedulePdf(Simulation simulation) {
        try {
            // Generamos el cronograma con el mismo “cerebro financiero”
            List<PaymentScheduleEntry> schedule = loanCalculatorDomainService.generateSchedule(simulation);

            Document document = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font headerFont = new Font(Font.HELVETICA, 8, Font.BOLD);

            Paragraph title = new Paragraph("Cronograma de Pagos - Método Francés (30 días)", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            BigDecimal teaAnnual = null;
            if (!schedule.isEmpty()) {
                teaAnnual = schedule.get(0).teaAnnual(); // ya es TEA en fracción
            }

            String typeRate = simulation.getTypeRate() != null
                    ? simulation.getTypeRate().trim().toUpperCase()
                    : "TEA";

            // Cabecera con datos de la simulación
            document.add(new Paragraph("Cliente: " + simulation.getFullName(), normalFont));
            document.add(new Paragraph("Programa: " + simulation.getProgramName(), normalFont));
            document.add(new Paragraph("Entidad financiera: " + simulation.getFinancialInstitution(), normalFont));
            document.add(new Paragraph("Monto financiado: " + money(simulation.getAmountFinanced()), normalFont));
            document.add(new Paragraph("Plazo (meses): " + simulation.getDeadlinesMonths(), normalFont));

            if("TNA".equals(typeRate)){
                String teaText = teaAnnual != null ? percent(teaAnnual) : "-";
                BigDecimal tna = simulation.getInterestRate() != null
                        ? simulation.getInterestRate().setScale(4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                document.add(new Paragraph(
                        "TEA equivalente: " + teaText + " (TNA declarada: " + tna + " %)",
                        normalFont
                ));
            } else {
                BigDecimal teaInput = simulation.getInterestRate() != null
                        ? simulation.getInterestRate().setScale(4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                document.add(new Paragraph(
                        "TEA declarada: " + teaInput + " %",
                        normalFont
                ));
            }
            document.add(new Paragraph("Cuota Inicial: " + simulation.getDownPayment(), normalFont));
            document.add(new Paragraph("Bono aplicado: " + money(simulation.getAmountBond()), normalFont));
            document.add(new Paragraph("Cuota mensual estimada: " + money(simulation.getMonthlyFee()), normalFont));
            document.add(new Paragraph("VAN: " + money(simulation.getVan()), normalFont));
            document.add(new Paragraph("TIR: " + (simulation.getTir() != null ? simulation.getTir() + " %" : "-"), normalFont));
            document.add(new Paragraph("TCEA: " + (simulation.getTcea() != null ? simulation.getTcea() + " %" : "-"), normalFont));
            document.add(Chunk.NEWLINE);

            // Tabla: N° de Cuota, TEA, TEP, Saldo Inicial, Interés, Cuota (inc SegDes),
            // Amortización, SegDes, SegRie, Comisión, Saldo Final, Flujo
            PdfPTable table = new PdfPTable(12);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 1.75f, 1.75f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f});

            addHeader(table, "N°", headerFont);
            addHeader(table, "TEA %", headerFont);
            addHeader(table, "TEP %", headerFont);
            addHeader(table, "Saldo Inicial", headerFont);
            addHeader(table, "Interés", headerFont);
            addHeader(table, "Cuota (inc SegDes)", headerFont);
            addHeader(table, "Amortización", headerFont);
            addHeader(table, "Seg. Desgrav.", headerFont);
            addHeader(table, "Seg. Inmueble", headerFont);
            addHeader(table, "Comisión", headerFont);
            addHeader(table, "Saldo Final", headerFont);
            addHeader(table, "Flujo", headerFont);

            for (PaymentScheduleEntry e : schedule) {
                addCell(table, String.valueOf(e.installmentNumber()), normalFont);
                addCell(table, percent(e.teaAnnual()), normalFont);
                addCell(table, percent(e.tepPeriod()), normalFont);
                addCell(table, money(e.beginningBalance()), normalFont);
                addCell(table, money(e.interest()), normalFont);
                addCell(table, money(e.installmentWithSegDes()), normalFont);
                addCell(table, money(e.amortization()), normalFont);
                addCell(table, money(e.seguroDesgravamen()), normalFont);
                addCell(table, money(e.seguroRiesgo()), normalFont);
                addCell(table, money(e.comision()), normalFont);
                addCell(table, money(e.endingBalance()), normalFont);
                addCell(table, money(e.flujo()), normalFont);
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error generating payment schedule PDF", ex);
        }
    }

    private void addHeader(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBackgroundColor(Color.LIGHT_GRAY);
        t.addCell(c);
    }

    private void addCell(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(c);
    }

    private String money(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return "S/ " + v.setScale(2, RoundingMode.HALF_UP);
    }

    private String percent(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;

        // pasar de fracción a porcentaje
        BigDecimal pct = v.multiply(BigDecimal.valueOf(100));

        // redondear a 4 decimales como tú quieres (1.09789% -> 1.0979%)
        return pct.setScale(4, RoundingMode.HALF_UP) + " %";    }
}