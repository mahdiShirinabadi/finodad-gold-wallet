package com.melli.hub.util.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Azimi
 */
public class FileUtility {

    private static final String CSV = "text/csv";
    private static final String[] EXCEL_TYPES = new String[]{
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
    };

    private static final Logger log = LogManager.getLogger(FileUtility.class);

    private FileUtility() {
    }

    public static List<String> readFirstColumnFromExcel(String contentType, InputStream inputStream) throws IOException {
        log.info("start reading first column from excel with contentType ({}) ...", contentType);
        if (Arrays.asList(EXCEL_TYPES).contains(contentType)) {
            return excelReadFirstColumn(new XSSFWorkbook(inputStream));
        } else if (CSV.equals(contentType)) {
            return csvReadFirstColumn(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
        throw new InvalidObjectException("file format not supported exception");
    }

    private static List<String> excelReadFirstColumn(Workbook workbook) {
        log.info("excel file has ({}) number of sheets", workbook.getNumberOfSheets());
        if (workbook.getNumberOfSheets() < 1) {
            log.error("excel file has less than 1 sheet returning empty list");
            return new ArrayList<>();
        }
        log.info("reading first sheet ...");
        return excelReadColumn(workbook.getSheetAt(0));
    }

    private static List<String> excelReadColumn(Sheet sheet) {
        log.info("sheet ({}) has ({}) number of rows", sheet.getSheetName(), sheet.getPhysicalNumberOfRows());
        if (sheet.getPhysicalNumberOfRows() < 1) {
            log.error("excel sheet has less than 1 rows returning empty list");
            return new ArrayList<>();
        }
        List<String> rows = new ArrayList<>();
        for (Row row : sheet) {
            rows.add(getCellValue(row.getCell(0)));
        }
        return rows;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> getFormulaCellValue(cell, cell.getCachedFormulaResultType());
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    private static String getFormulaCellValue(Cell cell, CellType resultType) {
        return switch (resultType) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    private static List<String> csvReadFirstColumn(InputStreamReader inputStreamReader) throws IOException {
        BufferedReader bReader = new BufferedReader(inputStreamReader);
        CSVParser csvParser = new CSVParser(bReader, CSVFormat.DEFAULT);
        List<CSVRecord> sheet = csvParser.getRecords();
        log.info("csv file has ({}) number of rows", csvParser.getRecordNumber());
        List<String> rows = new ArrayList<>();
        for (CSVRecord row : sheet) {
            rows.add(row.get(0));
        }
        return rows;
    }
}
