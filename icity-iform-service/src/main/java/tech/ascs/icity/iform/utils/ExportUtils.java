package tech.ascs.icity.iform.utils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

public class ExportUtils {
	// 设置sheet表头信息
	public static void outputHeaders(String[] headerInfo, Sheet sheet) {
		Row row = sheet.createRow(0);
		Workbook wb = sheet.getWorkbook();
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);//靠左
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getIndex());//设置背景颜色
		for (int i = 0; i < headerInfo.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headerInfo[i]);
			cell.setCellStyle(cellStyle);
		}
	}

	public static void outputColumn(String[] headerInfo, List list, Sheet sheet, int rowIndex) {
		for (int i = 0; i < list.size(); i++) {// 循环插入多少行
			Row row = sheet.createRow(rowIndex + i);
			Object obj = list.get(i);
			for (int j = 0; j < headerInfo.length; j++) {
				Object fieldValue = getFieldValueByName(headerInfo[j], obj);
				if (null != fieldValue) {
					createCell(row, j, fieldValue);
				}
			}
		}
	}

	public static void outputColumn(Object[][] arrs, Sheet sheet, int rowIndex) {
		for (int i = 0; i < arrs.length; i++) {// 循环插入多少行
			Row row = sheet.createRow(rowIndex + i);
			Object[] arr = arrs[i];
			for (int j = 0; j < arr.length; j++) {
				Object obj = arr[j];
				if (null != obj) {
					createCell(row, j, obj);
				}
			}
		}
	}

	public static void outputColumn(List<List<Object>> list, Sheet sheet, int rowIndex) {
		for (int i = 0; i < list.size(); i++) {// 循环插入多少行
			Row row = sheet.createRow(rowIndex + i);
			List<Object> dataList = list.get(i);
			for (int j = 0; j < dataList.size(); j++) {
				Object obj = dataList.get(j);
				if (null != obj) {
					createCell(row, j, obj);
				}
			}
		}
	}

	private static void createCell(Row row, int index, Object object) {
		if(object instanceof Long) {
			row.createCell(index, CellType.NUMERIC).setCellValue((Long) object);
		} else if(object instanceof Double) {
			row.createCell(index, CellType.NUMERIC).setCellValue((double) object);
		} else if(object instanceof Float) {
			row.createCell(index, CellType.NUMERIC).setCellValue((float) object);
		} else if(object instanceof Integer) {
			row.createCell(index, CellType.NUMERIC).setCellValue((Integer) object);
		} else if(object instanceof Date) {
			row.createCell(index).setCellValue(object.toString());
		} else {
			row.createCell(index, CellType.STRING).setCellValue(object.toString());
		}
	}

	// 根据对象属性获取值
	private static Object getFieldValueByName(String fieldName, Object obj) {
		String firstLetter = fieldName.substring(0, 1).toUpperCase();
		String getter = "get" + firstLetter + fieldName.substring(1);
		try {
			Method method = obj.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(obj, new Object[] {});
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("属性不存在！");
			return null;
		}
	}
}