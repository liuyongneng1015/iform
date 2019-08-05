package tech.ascs.icity.iform.utils;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import tech.ascs.icity.iform.function.ExcelRowMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * excel读取工具类
 *
 * @author renjie
 * @since 0.7.3
 **/
public final class ExcelReaderUtils {


    public static ExcelRowMapper<List<Object>> LIST_MAPPER = ((data, header, rowNum) -> data);

    public static ExcelReaderResult<List<Object>> readExcel(InputStream is, int headerRow, int startRow, int endRow) throws IOException, InvalidFormatException {
        return readExcel(is, headerRow, startRow, endRow, LIST_MAPPER);
    }

    /**
     * 通过 {@link InputStream } 输入流来读取excel文件, 返回数据的第一行为header, 读取的为第一个sheet页的数据
     *
     * @param is        excel文件输入流
     * @param headerRow 头部的行数 由{@code 1} 开始
     * @param startRow  数据的开始行数, 必须比 {@code headerRow} 小
     * @param endRow    数据结束行, 比 {@code startRow} 小的时候表示读取全部
     * @param rowMapper {@link ExcelRowMapper} 行处理
     * @param <T>       泛型T, 表示返回数据每行的类型
     * @return 返回整个excel的数据
     */
    public static <T> ExcelReaderResult<T> readExcel(InputStream is, int headerRow, int startRow, int endRow, ExcelRowMapper<T> rowMapper) throws IOException, InvalidFormatException {
        Workbook wb = WorkbookFactory.create(is);
        Sheet sheet = wb.getSheetAt(0);
        Row header = sheet.getRow(headerRow - 1);
        List<String> headers = readHeader(header);
        ExcelReaderResult<T> result = new ExcelReaderResult<>();
        result.setHeader(headers);
        result.setResult(readRows(sheet, startRow - 1, endRow - 1, headers, rowMapper));
        return result;
    }

    private static <T> List<T> readRows(Sheet sheet, int startRow, int endRow, List<String> headers, ExcelRowMapper<T> rowMapper) {
        int tmpEndRow = endRow > startRow ? endRow : sheet.getLastRowNum();
        tmpEndRow = tmpEndRow > sheet.getLastRowNum()  ? sheet.getLastRowNum() : tmpEndRow;
        List<T> result = new ArrayList<>();
        for (int i = startRow; i <= tmpEndRow; i++) {
            result.add(readRow(sheet.getRow(i), headers, rowMapper));
        }
        return result;
    }

    private static <T> T readRow(Row row, List<String> headers, ExcelRowMapper<T> rowMapper) {
        List<Object> datas = new ArrayList<>();
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            datas.add(getCellValue(row.getCell(i)));
        }
        return rowMapper.mapRow(datas, headers, row.getRowNum());
    }


    private static List<String> readHeader(Row row) {
        short endCell = row.getLastCellNum();
        List<String> headers = new ArrayList<>();
        for (short i = row.getFirstCellNum(); i < endCell; i++) {
            headers.add(Objects.toString(getCellValue(row.getCell(i))));
        }
        return headers;
    }

    private static Object getCellValue(Cell cell) {
        return handleCellType(cell, cell.getCellTypeEnum());
    }

    private static Object handleCellType(Cell cell, CellType cellType) {
        switch (cellType) {
            case NUMERIC:
                return handleNumberic(cell);
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                return cell.getStringCellValue();
            case ERROR:
                return cell.getErrorCellValue();
            case FORMULA:
                return handleCellType(cell, cell.getCachedFormulaResultTypeEnum());
            default:
                return null;
        }
    }

    private static Object handleNumberic(Cell cell) {
        if (XSSFDateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        } else {
            return cell.getNumericCellValue();
        }
    }

    public static class ExcelReaderResult<R> {
        private List<String> header;

        private List<R> result;

        public List<String> getHeader() {
            return header;
        }

        public void setHeader(List<String> header) {
            this.header = header;
        }

        public List<R> getResult() {
            return result;
        }

        public void setResult(List<R> result) {
            this.result = result;
        }
    }


}
