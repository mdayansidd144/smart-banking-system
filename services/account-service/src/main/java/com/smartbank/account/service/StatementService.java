package com.smartbank.account.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartbank.account.entity.Account;
import com.smartbank.account.entity.Transaction;
import com.smartbank.account.exception.AccountNotFoundException;
import com.smartbank.account.repository.AccountRepository;
import com.smartbank.account.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class StatementService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public StatementService(AccountRepository accountRepository,
                            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public byte[] generateStatement(UUID accountId, LocalDate from, LocalDate to) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        LocalDateTime fromDT = from.atStartOfDay();
        LocalDateTime toDT = to.plusDays(1).atStartOfDay();

        // Get all transactions for this account
        List<Transaction> allTx = transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId);

        // Filter by date range
        List<Transaction> inRange = allTx.stream()
                .filter(t -> !t.getCreatedAt().isBefore(fromDT)
                        && t.getCreatedAt().isBefore(toDT))
                .toList();

        // Calculate totals
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        for (Transaction t : inRange) {
            if (t.getType().name().equals("DEPOSIT")) {
                totalDeposits = totalDeposits.add(t.getAmount());
            } else {
                totalWithdrawals = totalWithdrawals.add(t.getAmount());
            }
        }

        // Opening and closing balance
        BigDecimal closingBalance = account.getBalance();
        BigDecimal openingBalance = closingBalance
                .subtract(totalDeposits)
                .add(totalWithdrawals);

        try {
            return buildPdf(account, from, to, openingBalance, closingBalance,
                    totalDeposits, totalWithdrawals, inRange);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF statement", e);
        }
    }

    private byte[] buildPdf(Account account, LocalDate from, LocalDate to,
                            BigDecimal openingBalance, BigDecimal closingBalance,
                            BigDecimal totalDeposits, BigDecimal totalWithdrawals,
                            List<Transaction> transactions) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 40);
        PdfWriter.getInstance(document, out);
        document.open();

        // ===== Header =====
        Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(26, 115, 232));
        Paragraph title = new Paragraph("Smart Bank", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font subFont = new Font(Font.HELVETICA, 14, Font.NORMAL, Color.DARK_GRAY);
        Paragraph sub = new Paragraph("Account Statement", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(20);
        document.add(sub);

        // ===== Account info =====
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);

        addInfoRow(infoTable, "Account Holder", account.getOwnerName(), labelFont, valueFont);
        addInfoRow(infoTable, "Account Number", account.getAccountNumber(), labelFont, valueFont);
        addInfoRow(infoTable, "Currency", account.getCurrency(), labelFont, valueFont);
        addInfoRow(infoTable, "Statement Period", from.format(DATE_FMT) + " — " + to.format(DATE_FMT), labelFont, valueFont);

        document.add(infoTable);

        // ===== Summary =====
        Paragraph summaryHeader = new Paragraph("Summary", new Font(Font.HELVETICA, 12, Font.BOLD));
        summaryHeader.setSpacingBefore(10);
        summaryHeader.setSpacingAfter(8);
        document.add(summaryHeader);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20);

        addSummaryRow(summaryTable, "Opening Balance", openingBalance, account.getCurrency());
        addSummaryRow(summaryTable, "Total Deposits", totalDeposits, account.getCurrency());
        addSummaryRow(summaryTable, "Total Withdrawals", totalWithdrawals, account.getCurrency());
        addSummaryRow(summaryTable, "Closing Balance", closingBalance, account.getCurrency());

        document.add(summaryTable);

        // ===== Transactions =====
        Paragraph txHeader = new Paragraph("Transactions", new Font(Font.HELVETICA, 12, Font.BOLD));
        txHeader.setSpacingBefore(10);
        txHeader.setSpacingAfter(8);
        document.add(txHeader);

        if (transactions.isEmpty()) {
            document.add(new Paragraph("No transactions in this period.", valueFont));
        } else {
            PdfPTable txTable = new PdfPTable(new float[]{3, 2, 2, 3});
            txTable.setWidthPercentage(100);

            // Header row
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Color headerBg = new Color(26, 115, 232);
            addHeaderCell(txTable, "Date", headerFont, headerBg);
            addHeaderCell(txTable, "Type", headerFont, headerBg);
            addHeaderCell(txTable, "Amount", headerFont, headerBg);
            addHeaderCell(txTable, "Description", headerFont, headerBg);

            // Data rows
            boolean alternate = false;
            for (Transaction t : transactions) {
                Color bg = alternate ? new Color(245, 245, 245) : Color.WHITE;
                addDataCell(txTable, t.getCreatedAt().format(TS_FMT), valueFont, bg, Element.ALIGN_LEFT);
                addDataCell(txTable, t.getType().name(), valueFont, bg, Element.ALIGN_LEFT);
                addDataCell(txTable, account.getCurrency() + " " + t.getAmount(), valueFont, bg, Element.ALIGN_RIGHT);
                addDataCell(txTable, t.getDescription() != null ? t.getDescription() : "-", valueFont, bg, Element.ALIGN_LEFT);
                alternate = !alternate;
            }

            document.add(txTable);
        }

        // ===== Footer =====
        Paragraph footer = new Paragraph(
                "\nThis is a system-generated statement. No signature required.",
                new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    // ===== Helpers =====

    private void addInfoRow(PdfPTable table, String label, String value,
                            Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private void addSummaryRow(PdfPTable table, String label, BigDecimal value, String currency) {
        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(new Color(248, 249, 250));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(currency + " " + value, valueFont));
        valueCell.setPadding(6);
        valueCell.setBackgroundColor(new Color(248, 249, 250));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }
}