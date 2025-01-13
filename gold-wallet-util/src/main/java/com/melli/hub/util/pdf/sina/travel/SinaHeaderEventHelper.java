package com.melli.hub.util.pdf.sina.travel;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.log4j.Log4j2;

import java.net.URL;

@Log4j2
public class SinaHeaderEventHelper extends PdfPageEventHelper {
    private static final String PERSIAN_FONT = "/fonts/IRANSansX-Regular.ttf";
    private Image sinaInsuranceLogo;
    private Image sosLogo;
    private PdfPTable table;

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        log.info("start open document");
        try {
            URL sosLogoUrl = Thread.currentThread().getContextClassLoader().getResource("images/SOSLogo.png");
            log.info("sosLogoUrl ({})", sosLogoUrl);
            if (sosLogoUrl != null) {
                sosLogo = Image.getInstance(sosLogoUrl);
            }
            URL sinaLogoUrl = Thread.currentThread().getContextClassLoader().getResource("images/SinaInsuranceLogo.jpg");
            log.info("sinaLogoUrl ({})", sinaLogoUrl);
            if (sinaLogoUrl != null) {
                sinaInsuranceLogo = Image.getInstance(sinaLogoUrl);
            }

            table = new PdfPTable(3);
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            table.setWidthPercentage(100);

            PdfPCell logoRightCell = new PdfPCell(sinaInsuranceLogo, true);
            logoRightCell.addElement(sinaInsuranceLogo);
            logoRightCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell titleCell = new PdfPCell();
            Paragraph p = new Paragraph("بیمه نامه مسافرتی خارج از کشور", FontFactory.getFont(PERSIAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.NORMAL, BaseColor.CYAN));
            p.setAlignment(Element.ALIGN_CENTER);
            titleCell.addElement(p);
            titleCell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_CENTER);

            PdfPCell logoLeftCell = new PdfPCell(sosLogo, true);
            logoLeftCell.setVerticalAlignment(Element.ALIGN_CENTER);
            logoLeftCell.setBorder(Rectangle.NO_BORDER);

            table.setWidths(new int[]{2, 7, 2});
            table.addCell(logoRightCell);
            table.addCell(titleCell);
            table.addCell(logoLeftCell);
        } catch (Exception e) {
            log.info("exception in pdf open document, exception ({})", e.getMessage());
        }
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        log.info("start page");
        try {
            document.add(table);
        } catch (DocumentException e) {
            log.error("exception in pdf start page, exception ({})", e.getMessage());
        }
    }
}
