import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;
import static constant.ConstExperiment.*;

public class ExcelWriter {
    Workbook book = null;
    Sheet sheet = null;
    CellStyle style_header;
    CellStyle style_string;
    CellStyle style_int;
    CellStyle style_double;
    CellStyle style_datetime;

    public ExcelWriter() {
        book = new SXSSFWorkbook();
        sheet = book.createSheet();
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        }
        setStyles();
        createHeader();
    }

    private void setStyles() {
        Font font = book.createFont();
        font.setFontName("Helvetica");
        font.setFontHeightInPoints((short) 12);

        DataFormat format = book.createDataFormat();

        // header style
        style_header = book.createCellStyle();
        style_header.setBorderBottom(BorderStyle.THIN);
        ExcelWriter.setBorder(style_header, BorderStyle.THIN);
        style_header.setVerticalAlignment(VerticalAlignment.TOP);
        style_header.setFont(font);

        // string style
        style_string = book.createCellStyle();
        ExcelWriter.setBorder(style_string, BorderStyle.THIN);
        style_string.setVerticalAlignment(VerticalAlignment.TOP);
        style_string.setFont(font);

        // interger style
        style_int = book.createCellStyle();
        ExcelWriter.setBorder(style_int, BorderStyle.THIN);
        style_int.setDataFormat(format.getFormat("#,##0;-#,##0"));
        style_int.setVerticalAlignment(VerticalAlignment.TOP);
        style_int.setFont(font);

        // double style
        style_double = book.createCellStyle();
        ExcelWriter.setBorder(style_double, BorderStyle.THIN);
        style_double.setDataFormat(format.getFormat("#,##0.000;-#,##0.000"));
        style_double.setVerticalAlignment(VerticalAlignment.TOP);
        style_double.setFont(font);

        // date style
        style_datetime = book.createCellStyle();
        ExcelWriter.setBorder(style_datetime, BorderStyle.THIN);
        style_datetime.setDataFormat(format.getFormat("yyyy/mm/dd hh:mm:ss"));
        style_datetime.setVerticalAlignment(VerticalAlignment.TOP);
        style_datetime.setFont(font);
    }

    private void createHeader() {
        Row row = sheet.createRow(0);
        int colNumber = 0;
        Cell cell;

        // delay
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_header);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("d");

        // size of window
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_header);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("w");

        // number of robots
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_header);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("N");

        // sum of staying
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_header);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("sum of stays");

        // sum of completed tasks
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_header);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("sum of tasks");

        // variable of completed tasks
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_header);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("var of tasks");

        // fix header
        sheet.createFreezePane(0, 1);

        // auto sizing columns
        for (int i = 0; i <= colNumber; i++) {
            sheet.autoSizeColumn(i, true);
        }
    }

    /**
     * Add data as a row
     * "dataNo" represents the number of the data
     */
    public void addData(int dataNo, int delay, int windowSize, int NodeNum, int sumStays, int sumTasks, double varTasks) {
        // "1" means the header row
        int rowNumber = dataNo+1;
        int colNumber = 0;
        Row row = sheet.createRow(rowNumber);
        Cell cell;

        // delay
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_int);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(delay);

        // size of window
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_int);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(windowSize);

        // number of robots
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_int);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(NodeNum);

        // sum of staying
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_int);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(sumStays);

        // sum of tasks
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_int);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(sumTasks);

        // var of tasks
        cell = row.createCell(colNumber++);
        cell.setCellStyle(style_double);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(varTasks);

        // auto sizing columns
        for (int i = 0; i <= colNumber; i++) {
            sheet.autoSizeColumn(i, true);
        }
    }

    public void writeFile() {
        String outputFilePath = FILE_PATH;
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(outputFilePath);
            book.write(fout);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (book != null) {
                try {
                    ((SXSSFWorkbook) book).dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void setBorder(CellStyle style, BorderStyle border) {
        style.setBorderBottom(border);
        style.setBorderTop(border);
        style.setBorderLeft(border);
        style.setBorderRight(border);
    }
}
