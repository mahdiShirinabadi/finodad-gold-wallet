package com.melli.hub.util.pdf.sina.travel;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.melli.hub.util.StringUtils;
import com.melli.hub.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Log4j2
@Component
public class SinaInsuranceTravelPdfCreator {
    private static final String PERSIAN_FONT = "/fonts/IRANSansX-Regular.ttf";

    public byte[] createPdf(
            String policyNo,
            String agentsCode,
            Date dateOfIssue,
            String firstName,
            String lastName,
            Date dateOfBirth,
            String nationality,
            String passportNo,
            String coverageEuro,
            String zone,
            String destination,
            String priceRial,
            String validityDays
    ) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
        writer.setPageEvent(new SinaHeaderEventHelper());
        document.setMargins(20, 20, 10, 10);
        document.open();
        PdfPTable conditionNotice = createConditionNotice(FontFactory.getFont(PERSIAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.NORMAL, BaseColor.CYAN));
        PdfPTable policyInfo = createPolicyInfo(
                FontFactory.getFont(PERSIAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.NORMAL, BaseColor.BLACK),
                policyNo != null ? policyNo : "",
                agentsCode != null ? agentsCode : "",
                dateOfIssue != null ? DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, dateOfIssue, "yyyy/MM/dd", false) : "",
                firstName != null ? firstName : "",
                lastName != null ? lastName : "",
                dateOfBirth != null ? DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, dateOfBirth, "yyyy/MM/dd", false) : "",
                nationality != null ? nationality : "",
                passportNo != null ? passportNo : "",
                coverageEuro != null ? StringUtils.separateNumberByComma(coverageEuro) + " EUR" : "",
                zone != null ? zone : "",
                destination != null ? destination : "",
                priceRial != null ? StringUtils.separateNumberByComma(priceRial) + " Rials" : "",
                validityDays != null ? validityDays + " DAYS" : ""
        );
        PdfPTable validityNotice = createValidityNotice(FontFactory.getFont(PERSIAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8, Font.BOLD, BaseColor.BLACK));
        PdfPTable coverageHeader = createCoverageHeader(FontFactory.getFont(PERSIAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.BOLD, BaseColor.CYAN));

        List<PdfPTable> coverageTables = createCoverageTables(FontFactory.getFont(PERSIAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8, Font.NORMAL, BaseColor.BLACK));

        PdfPTable footer = createFooterNotice(FontFactory.getFont(PERSIAN_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.NORMAL, BaseColor.BLACK));

        document.add(conditionNotice);
        document.add(policyInfo);
        document.add(validityNotice);
        document.add(coverageHeader);
        for (PdfPTable table : coverageTables) {
            document.add(table);
        }
        document.add(footer);
        document.close();
        return byteArrayOutputStream.toByteArray();
    }

    private List<PdfPTable> createCoverageTables(Font font) {
        List<PdfPTable> coverageTables = new ArrayList<>();
        PdfPTable table1 = createCoverageRow(
                font,
                "در صورت بروز بیماری یا صدمات جسمی، هزینه های معمول متعارف، ضروری و معقول بستری شدن در بیمارستان، جراحی، معاینات پزشکی و داروی تجویزی از سوی پزشک بیمه گذار تا سقف مندرج در بیمه نامه",
                "50000",
                "In the event of illness or injury of the Insured, the usual, customary, necessary and reasonable costs of hospitalization, surgery, medical fees and pharmaceutical products, prescribed by the attending doctor. 25 Euro excess is applicable per claim, except in case of bodily injury and/or hospitalization for at least 24 hours."
        );
        coverageTables.add(table1);
        PdfPTable table2 = createCoverageRow(
                font,
                "در صورت ضرورت، موارد فوریتی دندانپزشکی. فرانشیز هر مورد خسارت دندانپزشکی برابر ۲۵ یورو است. این هزینه ها به معالجه دندان درد، درمان عفونت و کشیدن دندان محدود می گردد.",
                "400",
                "If necessary, the dental assistance 25 Euro excess is applicable per claim. This coverage is restricted to the treatment of pain, infection and removal of the tooth affected."
        );
        coverageTables.add(table2);
        PdfPTable table3 = createCoverageRow(
                font,
                "در صورتی که بیمه گذار به علت حوادث یا بیماری مشمول این بیمه نامه، بیشتر از ۱۰ روز در بیمارستان بستری شود، مخارج انتقال یکی از اعضای بلافصل خانواده وی را از کشور محل اقامت بیمه گذار، از جمله هزینه رفت و برگشت به محل بستری شدن و مخارج اقامت تا سقف ۸۵ یورو برای هر روز، حداکثر به مدت ۱۰ روز",
                "850",
                "In the event that the Insured should be admitted to hospital for more than ten days as a result of an accident or illness covered in the policy, charge of the transfer of immediate family member from the Usual Country of Residence of the Insured, including meeting the cost of the outbound and return journey to the place of hospitalization and the accommodation expenses there, up to a limit of 85 Euro per day for a maximum of 10 days."
        );
        coverageTables.add(table3);
        PdfPTable table4 = createCoverageRow(
                font,
                "در صورت فقدان گذرنامه، گواهینامه رانندگی و شناسنامه بیمه گذار در خارج از کشور، هزینه های لازم برای تهیه گذرنامه، گواهینامه رانندگی، شناسنامه المثنی و یا مدارک کنسولی مشابه",
                "200",
                "In the case of loss of the Insured party’s passport , driving license, national identity card while abroad, the expenses of the displacements necessary for obtaining a new passport, driving license, national identity card or equivalent consular document,"
        );
        coverageTables.add(table4);
        return coverageTables;
    }

    private PdfPTable createConditionNotice(Font font) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        Phrase phrase = new Phrase("This policy is subjected to the following conditions and attached provinces.", font);
        cell.addElement(phrase);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        table.setSpacingAfter(4);
        return table;
    }

    private PdfPTable createPolicyInfo(
            Font font,
            String policyNo,
            String agentCode,
            String dateOfIssue,
            String insuredName,
            String insuredLastName,
            String dateOfBirth,
            String nationality,
            String passportNo,
            String coverage,
            String zone,
            String destinationCountry,
            String premiumRial,
            String validityDays
    ) {
        PdfPTable table = new PdfPTable(new float[]{3, 1, 3});
        table.setWidthPercentage(100);
        PdfPCell policyNoCell = createPolicyInfoCell("Policy No.", policyNo != null ? policyNo : "", font);
        policyNoCell.setBorderWidthTop(1);
        PdfPCell agentsCodeCell = createPolicyInfoCell("Agent's Code", agentCode, font);
        agentsCodeCell.setBorderWidthTop(1);
        PdfPCell dateOfIssueCell = createPolicyInfoCell("Date Of Issue", dateOfIssue, font);
        PdfPCell insuredNameCell = createPolicyInfoCell("Insured's Name", insuredName, font);
        PdfPCell insuredLastNameCell = createPolicyInfoCell("Insured's Family Name", insuredLastName, font);
        PdfPCell dateOfBirthCell = createPolicyInfoCell("Date Of Birth", dateOfBirth, font);
        PdfPCell nationalityCell = createPolicyInfoCell("Nationality", nationality, font);
        PdfPCell passportNoCell = createPolicyInfoCell("Passport No", passportNo, font);
        PdfPCell coverageCell = createPolicyInfoCell("Coverage", coverage, font);
        PdfPCell zoneCell = createPolicyInfoCell("Zone", zone, font);
        PdfPCell destinationCountryCell = createPolicyInfoCell("Name of Country to be visited", destinationCountry, font);
        PdfPCell premiumRialCell = createPolicyInfoCell("Premium", premiumRial, font);
        PdfPCell validityDaysCell = createPolicyInfoCell("Validity (Days)", validityDays, font);
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);

        table.addCell(policyNoCell);
        PdfPCell firstEmptyCell = new PdfPCell();
        firstEmptyCell.setBorder(Rectangle.NO_BORDER);
        firstEmptyCell.setBorderWidthTop(1);
        table.addCell(firstEmptyCell);
        table.addCell(agentsCodeCell);
        table.addCell(dateOfIssueCell);
        table.addCell(emptyCell);
        table.addCell(insuredNameCell);
        table.addCell(insuredLastNameCell);
        table.addCell(emptyCell);
        table.addCell(dateOfBirthCell);
        table.addCell(nationalityCell);
        table.addCell(emptyCell);
        table.addCell(passportNoCell);
        table.addCell(coverageCell);
        table.addCell(emptyCell);
        table.addCell(zoneCell);
        table.addCell(destinationCountryCell);
        table.addCell(emptyCell);
        table.addCell(premiumRialCell);
        table.addCell(validityDaysCell);
        table.addCell(emptyCell);
        table.addCell(emptyCell);
        return table;
    }

    private PdfPCell createPolicyInfoCell(String key, String value, Font font) {
        Font boldFont = new Font(font.getBaseFont(), 10, Font.BOLD);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(key + ":     ", boldFont));
        phrase.add(new Chunk(value, font));
        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.addElement(phrase);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        pdfPCell.setBorder(Rectangle.NO_BORDER);
        return pdfPCell;
    }

    private PdfPTable createValidityNotice(Font font) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        String validityTitle = """
                Validity of the coverage begins on the date of insured’s departure from Iran as indicated in passport
                """;
        Paragraph paragraphTitle = new Paragraph(validityTitle, new Font(font.getBaseFont(), 10, Font.NORMAL, BaseColor.CYAN));
        paragraphTitle.setAlignment(Element.ALIGN_LEFT);
        paragraphTitle.setIndentationLeft(4);
        paragraphTitle.setSpacingAfter(4);
        String validityNotice = """
                Attention: The Insured will not be entitled to the reimbursement of the expenses directly paid by him/her or by any person on insured's
                behalf without the previous declaration by telephone and fax in between 7 days (168 hours) to SOS Alarm Center. The notification of claim
                must be sent by mail or fax to SOS , No.24 ,15th Gandi ave, Tehran, Iran, Iran FAX: 009821 88648620, Email: info@iranassistance.com
                """;
        Paragraph paragraph = new Paragraph(validityNotice, font);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(4);
        paragraph.setSpacingAfter(4);
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthTop(1);
        cell.setBorderWidthBottom(1);
        cell.addElement(paragraphTitle);
        cell.addElement(paragraph);
        table.addCell(cell);
        table.setSpacingBefore(4);
        return table;
    }

    private PdfPTable createCoverageHeader(Font font) {
        PdfPTable table = new PdfPTable(new float[]{30, 1, 20, 1, 30});
        table.setWidthPercentage(100);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        PdfPCell persianHeader = new PdfPCell(new Phrase("برخی پوشش ها", font));
        persianHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        persianHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        persianHeader.setBorder(Rectangle.NO_BORDER);
        persianHeader.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        persianHeader.setPaddingBottom(6);

        PdfPCell coverageLimit = new PdfPCell(new Phrase("سقف تعهدات به یورو", font));
        coverageLimit.setHorizontalAlignment(Element.ALIGN_CENTER);
        coverageLimit.setVerticalAlignment(Element.ALIGN_MIDDLE);
        coverageLimit.setBorder(Rectangle.NO_BORDER);
        coverageLimit.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        coverageLimit.setPaddingBottom(6);

        PdfPCell englishHeader = new PdfPCell(new Phrase("Some Coverage", font));
        englishHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        englishHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        englishHeader.setBorder(Rectangle.NO_BORDER);
        englishHeader.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
        englishHeader.setPaddingBottom(6);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);

        table.addCell(persianHeader);
        table.addCell(emptyCell);
        table.addCell(coverageLimit);
        table.addCell(emptyCell);
        table.addCell(englishHeader);

        return table;
    }

    private PdfPTable createCoverageRow(Font font, String persianDescription, String coverageEuro, String latinDescription) {
        PdfPTable table = new PdfPTable(new float[]{30, 1, 20, 1, 30});
        table.setWidthPercentage(100);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        PdfPCell coverageInPersian = new PdfPCell(new Phrase(persianDescription, font));
        coverageInPersian.setBorder(Rectangle.NO_BORDER);
        coverageInPersian.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        coverageInPersian.setPaddingBottom(6);
        coverageInPersian.setBorderWidthTop(1);

        PdfPCell coverageLimit = new PdfPCell(new Phrase(StringUtils.separateNumberByComma(coverageEuro) + " یورو", font));
        coverageLimit.setHorizontalAlignment(Element.ALIGN_CENTER);
        coverageLimit.setVerticalAlignment(Element.ALIGN_MIDDLE);
        coverageLimit.setBorder(Rectangle.NO_BORDER);
        coverageLimit.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        coverageLimit.setPaddingBottom(6);
        coverageLimit.setBorderWidthTop(1);

        PdfPCell coverageInEnglish = new PdfPCell(new Phrase(latinDescription, font));
        coverageInEnglish.setHorizontalAlignment(Element.ALIGN_LEFT);
        coverageInEnglish.setBorder(Rectangle.NO_BORDER);
        coverageInEnglish.setRunDirection(PdfWriter.RUN_DIRECTION_LTR);
        coverageInEnglish.setPaddingBottom(6);
        coverageInEnglish.setBorderWidthTop(1);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);

        table.addCell(coverageInPersian);
        table.addCell(emptyCell);
        table.addCell(coverageLimit);
        table.addCell(emptyCell);
        table.addCell(coverageInEnglish);

        return table;
    }

    private PdfPTable createFooterNotice(Font font) {
        Font boldFont = new Font(font.getBaseFont(), 7, Font.NORMAL);
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        String text = """
                 * "الزامیست حداکثر ظرف مدت ۷ روز (۱۶۸ ساعت) از تاریخ بستری یا مراجعه به پزشک یا مرکز درمانی، به شرکت کمک رسان اعلام خسارت گردد. در غیراین صورت، هرگونه پرداخت هزینه پزشکی توسط بیمه شده یا نماینده وی مردود قلمداد شده و قابل پرداخت نیست. اعلام خسارت باید توسط نامه یا فاکس به آدرس زیر ارسال شود."
                 تهران، خیابان گاندی، خیابان ۱۵، شماره ۲۴         تلفن: ۹۸۲۱۹۱۰۰۳۴۰۴+          و          info@iranassistance.com
                 * "اعتبار این بیمه نامه از زمان خروج از کشور آغاز می گردد. شرایط و تعهدات بیمه گر (شرایط عمومی بیمه نامه) به صورت کامل در وب سایت بیمه سینا (SinaInsurance.com) قید شده و قابل مشاهده است "
                 * "مبنای پرداخت خسارت در داخل ایران نرخ ارز نیمایی در تاریخ وقوع خسارت می باشد."
                 * "هزینه های ناشی از بیماری کرونا تحت پوشش می باشد."
                 * "دوره انتظار برای پوشش بیماری کرونا، ۱۴ روز از تاریخ خروج از کشور می باشد، مگر آنکه مسافر دارای برگه آزمایش منقی تست کرونا (PCR) معتبر در مراکز مورد تایید بیمه گر که ظرف مدت ۷۲ ساعت قبل از خروج از کشور انجام شده است، باشد."
                 تهران، بلوار میرداماد، شماره ۲۲۵         کدپستی ۱۹۱۸۹۷۳۶۶۸         تلفن  ۲۸۰۷ ۰۲۱         کد اقتصادی ۴۱۱۳۳۴۱۶۴۴۶۵         شناسه ملی ۱۰۱۰۲۵۲۹۰۰۶
                """;
        Paragraph paragraph = new Paragraph(text);
        paragraph.setFont(boldFont);
        PdfPCell cell = new PdfPCell();
        cell.addElement(paragraph);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthTop(1);
        table.addCell(cell);
        table.setSpacingBefore(4);
        return table;
    }
}
