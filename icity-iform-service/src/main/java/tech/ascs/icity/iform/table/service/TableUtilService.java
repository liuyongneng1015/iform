package tech.ascs.icity.iform.table.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import tech.ascs.icity.iform.model.ColumnData;
import tech.ascs.icity.iform.model.TabInfo;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TableUtilService {

	static String classFilePath = "";
	static {
		classFilePath = TableUtilService.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		if ("\\".equals(File.separator)) {
			classFilePath = classFilePath.substring(1);
		}
	}

	public void createTable(TabInfo tabInfo) throws Exception {
		createTable(getTemplateMap(tabInfo));
	}

	public String getXmlFile(TabInfo tabInfo) throws Exception {
		return getGenFile("mapping.ftl", getTemplateMap(tabInfo));
	}

	public void createTable(Map<String, Object> map) throws Exception {

		createTable(getGenFile("mapping.ftl", map));
	}

	public static String getGenFile(String ftlFileName, Map<String, Object> map)
			throws Exception {
		final Configuration cfg;

		cfg = new Configuration(Configuration.VERSION_2_3_28);

		StringTemplateLoader stringLoader = new StringTemplateLoader();
		String templateContent = getMappingTemplateFile(ftlFileName);
		stringLoader.putTemplate("mappingTemplate", templateContent);

		cfg.setTemplateLoader(stringLoader);
		StringWriter writer = new StringWriter();
		try {
			Template template = cfg.getTemplate("mappingTemplate", "utf-8");
			try {
				template.process(map, writer);
				System.out.println(writer.toString());
			} catch (TemplateException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}

	public static String getMappingTemplateFile(String ftlFileName) {
		InputStream stream = TableUtilService.class.getClassLoader()
				.getResourceAsStream(ftlFileName);

		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String s = null;
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
			br.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}

		return sb.toString();
	}

	private static void createTable(String xmlContent) throws Exception {

		MetadataSources metadata = new MetadataSources(
				new StandardServiceRegistryBuilder()
						.applySetting("hibernate.connection.driver_class",
								"com.mysql.jdbc.Driver")
						.applySetting("hibernate.connection.url",
								"jdbc:mysql://192.168.4.151:3306/icity")
						.applySetting("hibernate.connection.username", "root")
						.applySetting("hibernate.connection.password",
								"icityDB2018!@#")
						.applySetting("hibernate.dialect",
								"org.hibernate.dialect.MySQL5Dialect")
						.applySetting("hibernate.show_sql", "true")
						.applySetting("hibernate.hbm2ddl.auto", "create")
						.build());

		metadata.addInputStream(IOUtils.toInputStream(xmlContent, "UTF-8"));

		MetadataImplementor metadataImplementor = (MetadataImplementor) metadata
				.buildMetadata();
		SchemaExport export = new SchemaExport();
		export.setFormat(true);

		EnumSet<TargetType> enumSet = EnumSet.of(TargetType.DATABASE);
		export.execute(enumSet, SchemaExport.Action.CREATE, metadataImplementor);// TODO
	}

	private Map<String, Object> getTemplateMap(TabInfo tabInfo) {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("table", tabInfo.getTabName());

		List<ColumnBean> list = new ArrayList<>();

		ColumnBean column = null;

		List<ColumnData> list0 = tabInfo.getColumnDatas();
		for (ColumnData columnData : list0) {
			column = new ColumnBean();
			column.setColName(columnData.getColName());
			if (columnData.getNotNull() != null && columnData.getNotNull())
				column.setNotnullable("true");
			else
				column.setNotnullable("false");

			if ("Integer".equalsIgnoreCase(columnData.getType()))
				column.setType("java.lang.Integer");
			else if ("Int".equalsIgnoreCase(columnData.getType()))
				column.setType("java.lang.Integer");
			else if ("Long".equalsIgnoreCase(columnData.getType()))
				column.setType("java.lang.Long");
			else if ("Double".equalsIgnoreCase(columnData.getType()))
				column.setType("java.lang.Double");
			else if ("BigDecimal".equalsIgnoreCase(columnData.getType()))
				column.setType("java.math.BigDecimal");
			else if ("BigInteger".equalsIgnoreCase(columnData.getType()))
				column.setType("java.math.BigInteger");
			else if ("Timestamp".equalsIgnoreCase(columnData.getType()))
				column.setType("java.sql.Timestamp");
			else if ("Date".equalsIgnoreCase(columnData.getType()))
				column.setType("java.sql.Date");
			else if ("Datetime".equalsIgnoreCase(columnData.getType()))
				column.setType("java.sql.Date");
			else if ("Boolean".equalsIgnoreCase(columnData.getType()))
				column.setType("java.lang.Boolean");
			else
				column.setType("java.lang.String");

			list.add(column);

		}// for

		map.put("columns", list);

		return map;
	}

}
