package tech.ascs.icity.iform.utils;


import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ImportUtils {
    public static final String OFFICE_EXCEL_2003_POSTFIX = "xls";
    public static final String OFFICE_EXCEL_2010_POSTFIX = "xlsx";
    public static final String EMPTY = "";
    public static final String POINT = ".";
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static int totalRows; //sheet中总行数
    public static int totalCells; //每一行总单元格数

    /**
     * 获得path的后缀名
     *
     * @param path
     * @return
     */
    public static String getPostfix(String path) {
        if (!StringUtils.hasText(path.trim())) {
            return EMPTY;
        }
        if (path.contains(POINT)) {
            return path.substring(path.lastIndexOf(POINT) + 1, path.length());
        }
        return EMPTY;
    }

    /**
     * 单元格格式
     *
     * @param hssfCell
     * @return
     */
    @SuppressWarnings({"static-access", "deprecation"})
    public static String getHValue(HSSFCell hssfCell) {
        if (hssfCell.getCellType() == CellType.BOOLEAN.getCode()) {
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
            String cellValue = "";
            if (HSSFDateUtil.isCellDateFormatted(hssfCell)) {
                Date date = HSSFDateUtil.getJavaDate(hssfCell.getNumericCellValue());
                cellValue = sdf.format(date);
            } else {
                DecimalFormat df = new DecimalFormat("#.##");
                cellValue = df.format(hssfCell.getNumericCellValue());
                String strArr = cellValue.substring(cellValue.lastIndexOf(POINT) + 1, cellValue.length());
                if (strArr.equals("00")) {
                    cellValue = cellValue.substring(0, cellValue.lastIndexOf(POINT));
                }
            }
            return cellValue;
        } else {
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }

    /**
     * 单元格格式
     *
     * @param xssfCell
     * @return
     */
    public static String getXValue(XSSFCell xssfCell) {
        if (xssfCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(xssfCell.getBooleanCellValue());
        } else if (xssfCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            String cellValue = "";
            if (XSSFDateUtil.isCellDateFormatted(xssfCell)) {
                Date date = XSSFDateUtil.getJavaDate(xssfCell.getNumericCellValue());
                cellValue = sdf.format(date);
            } else {
                DecimalFormat df = new DecimalFormat("#.##");
                cellValue = df.format(xssfCell.getNumericCellValue());
                String strArr = cellValue.substring(cellValue.lastIndexOf(POINT) + 1, cellValue.length());
                if (strArr.equals("00")) {
                    cellValue = cellValue.substring(0, cellValue.lastIndexOf(POINT));
                }
            }
            return cellValue;
        } else {
            return String.valueOf(xssfCell.getStringCellValue());
        }
    }


    /**
     * read the Excel .xlsx,.xls
     * @param file jsp中的上传文件
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> readExcel(MultipartFile file) throws IOException {
        if(file==null|| EMPTY.equals(file.getOriginalFilename().trim())){
            return null;
        }else{
            String postfix = getPostfix(file.getOriginalFilename());
            if(!EMPTY.equals(postfix)){
                if(OFFICE_EXCEL_2003_POSTFIX.equals(postfix)){
                    return readXls(file);
                }else if(OFFICE_EXCEL_2010_POSTFIX.equals(postfix)){
                    return readXlsx(file);
                }else{
                    return null;
                }
            }
        }
        return null;
    }
    /**
     * read the Excel 2010 .xlsx
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    public static List<Map<String, Object>> readXlsx(MultipartFile file){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        // IO流读取文件
        InputStream input = null;
        XSSFWorkbook wb = null;
        Map<String, Object> rowMap = null;
        try {
            input = file.getInputStream();
            // 创建文档
            wb = new XSSFWorkbook(input);

            //读取sheet(页)
            for(int numSheet=0;numSheet<wb.getNumberOfSheets();numSheet++){
                XSSFSheet xssfSheet = wb.getSheetAt(numSheet);
                if(xssfSheet == null){
                    continue;
                }
                totalRows = xssfSheet.getLastRowNum();

                List<String> firstRow = new ArrayList<>();

                for(int rowNum = 0;rowNum <= 0;rowNum++){
                    XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                    if(xssfRow!=null){
                        totalCells = xssfRow.getLastCellNum();
                        //读取列，从第一列开始
                        for(int c = 0; c <totalCells;c++){
                            XSSFCell cell = xssfRow.getCell(c);
                            if(cell==null){
                                firstRow.add(EMPTY);
                                continue;
                            }
                            firstRow.add(getXValue(cell).trim());
                        }
                    }
                }

                //读取Row,从第二行开始
                for(int rowNum = 1;rowNum <= totalRows;rowNum++){
                    XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                    if(xssfRow!=null){
                        rowMap = new HashMap<>();
                        totalCells = xssfRow.getLastCellNum();
                        //读取列，从第一列开始
                        for(int c = 0;c < totalCells;c++){
                            XSSFCell cell = xssfRow.getCell(c);
                            if(cell==null){
                                rowMap.put(firstRow.get(c), EMPTY);
                                continue;
                            }
                            rowMap.put(firstRow.get(c), getXValue(cell).trim());
                        }
                        if(totalCells < firstRow.size()){
                            for(int i = totalCells ; i < firstRow.size(); i++){
                                rowMap.put(firstRow.get(i), EMPTY);
                            }
                        }
                        list.add(rowMap);
                    }
                }
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }
    /**
     * read the Excel 2003-2007 .xls
     * @param file
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> readXls(MultipartFile file){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        // IO流读取文件
        InputStream input = null;
        HSSFWorkbook wb = null;
        Map<String, Object> rowMap = null;
        try {
            input = file.getInputStream();
            // 创建文档
            wb = new HSSFWorkbook(input);
            //读取sheet(页)
            for(int numSheet = 0; numSheet< wb.getNumberOfSheets(); numSheet++){
                HSSFSheet hssfSheet = wb.getSheetAt(numSheet);
                if(hssfSheet == null){
                    continue;
                }
                totalRows = hssfSheet.getLastRowNum();
                List<String> firstRow = new ArrayList<>();
                //读取Row,从第二行开始
                for(int rowNum = 0;rowNum <= 0;rowNum++){
                    HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                    if(hssfRow!=null) {
                        //读取列，从第一列开始
                        for (short c = 0; c < totalCells; c++) {
                            HSSFCell cell = hssfRow.getCell(c);
                            firstRow.add(getHValue(cell).trim());
                        }
                    }
                }
                for(int rowNum = 1; rowNum <= totalRows;rowNum++){
                    HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                    if(hssfRow!=null){
                        rowMap = new HashMap<>();
                        totalCells = hssfRow.getLastCellNum();
                        //读取列，从第一列开始
                        for(short c=0; c < totalCells ;c++){
                            HSSFCell cell = hssfRow.getCell(c);
                            if(cell==null){
                                rowMap.put(firstRow.get(c), EMPTY);
                                continue;
                            }
                            rowMap.put(firstRow.get(c), getHValue(cell).trim());
                        }
                        if(totalCells < firstRow.size()){
                            for(int i = totalCells ; i < firstRow.size(); i++){
                                rowMap.put(firstRow.get(i), EMPTY);
                            }
                        }
                        list.add(rowMap);
                    }
                }
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}