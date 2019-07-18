package tech.ascs.icity.iform.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import tech.ascs.icity.iform.api.model.*;

import javax.imageio.ImageIO;

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

	public static void outputFormdata(List<List<ItemInstance>> list, Sheet sheet, int rowIndex){

		for (int i = 0; i < list.size(); i++) {// 循环插入多少行
			Row row = sheet.createRow(rowIndex + i);
			Map<String, Object> map = getValue(list.get(i));
			boolean isContinuImages = (Boolean)map.get("isContinuImages");
			List<Object> dataList = (List<Object>)map.get("data");
			if(isContinuImages){
				row.setHeight((short)2000);
			}
   			for (int j = 0; j < dataList.size(); j++) {
 				Object obj = dataList.get(j);
   				if(obj == null){
					continue;
				}
 				if(!isContinuImages || !(obj instanceof ItemInstance)) {
 					createCell(row, j, obj);
				}else{
					try {
						XSSFSheet xssfSheet = ((XSSFSheet)sheet);
						sheet.setColumnWidth(j,10000);
						XSSFDrawing xssfDrawing = xssfSheet.createDrawingPatriarch();
						//图片一导出到单元格B2中
						List<String> urlList =  getImagesData((ItemInstance)obj);
						int size = urlList.size();
						for(int k = 0 ; k < size ; k ++) {
							String url = urlList.get(k);
							XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0,
									(short) j, i, (short) j + 1, i + 1);
							// 插入图片
							xssfDrawing.createPicture(anchor, xssfSheet.getWorkbook().addPicture(getOutputStream(url).toByteArray(), XSSFWorkbook.PICTURE_TYPE_JPEG));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static List<String> getImagesData(ItemInstance itemInstance){
		List<String> urList = new ArrayList<>();
		if (itemInstance.getDisplayValue() instanceof List) {
			urList.addAll(((List<FileUploadModel>)itemInstance.getDisplayValue()).parallelStream().map(FileUploadModel::getUrl).collect(Collectors.toList()));
		} else {
			urList.add(((FileUploadModel) itemInstance.getDisplayValue()).getUrl());
		}
		return urList;
	}

	private static Map<String, Object> getValue(List<ItemInstance> itemInstances){
		Map<String, Object> map = new HashMap<>();
		boolean isContinuImages = false;
		List<Object> dataList = new ArrayList<>();
		for(ItemInstance itemInstance : itemInstances) {
			if (itemInstance == null || itemInstance.getValue() == null) {
				dataList.add(null);
				continue;
			}
			if (itemInstance.getType() == ItemType.Media || itemInstance.getType() == ItemType.Attachment) {
				if (itemInstance.getType() == ItemType.Media) {
					isContinuImages = true;
				}
				dataList.add(itemInstance);
			} else if (itemInstance.getType() == ItemType.Location) {
				if (itemInstance.getValue() instanceof List) {
					List<String> list = new ArrayList<>();
					for (GeographicalMapModel mapModel : ((List<GeographicalMapModel>) itemInstance.getValue())) {
						if (StringUtils.hasText(mapModel.getDetailAddress())) {
							list.add(mapModel.getDetailAddress());
						} else {
							list.add("经度：" + mapModel.getLng() + "，纬度：" + mapModel.getLat());
						}
					}
				} else {
					GeographicalMapModel mapModel = (GeographicalMapModel) itemInstance.getValue();
					dataList.add(StringUtils.hasText(mapModel.getDetailAddress()) ? mapModel.getDetailAddress() : "经度：" + mapModel.getLng() + "，纬度：" + mapModel.getLat());
				}
			} else {
				dataList.add(itemInstance.getDisplayValue());
			}
		}
		map.put("isContinuImages", isContinuImages);
		map.put("data", dataList);
		return map;
	}

	public static void outputColumn(List<List<Object>> list, Sheet sheet, int rowIndex) {
		for (int i = 0; i < list.size(); i++) {// 循环插入多少行
			Row row = sheet.createRow(rowIndex + i);
			List<Object> dataList = list.get(i);
			for (int j = 0; j < dataList.size(); j++) {
				Object obj = dataList.get(j);
				if(obj == null){
					continue;
				}
				createCell(row, j, obj);
			}
		}
	}

	public static ByteArrayOutputStream getOutputStream(String url){
		// 先把读进来的图片放到一个ByteArrayOutputStream中，以便产生ByteArray
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		try {
			URL url1 = new URL(url);
			InputStream inputStream = url1.openStream();
			//将图片读到BufferedImage
			BufferedImage oldBufferedImage = ImageIO.read(inputStream);
			//定义一个BufferedImage对象，用于保存缩小后的位图
			BufferedImage bufferedImage = new BufferedImage(200, 300,BufferedImage.TYPE_INT_RGB);
			Graphics graphics = bufferedImage.getGraphics();;

			//将原始位图缩小后绘制到bufferedImage对象中
			graphics.drawImage(oldBufferedImage,0,0,200,300,null);
			ImageIO.write(bufferedImage, url.substring(url.lastIndexOf(".")+1), byteArrayOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return  byteArrayOut;
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
		} else if(object instanceof List) {
			row.createCell(index, CellType.STRING).setCellValue(String.join(",",(List<String>)object));
		}else {
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