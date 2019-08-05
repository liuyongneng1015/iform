package tech.ascs.icity.iform.support;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import tech.ascs.icity.iform.utils.ExcelReaderUtils;
import tech.ascs.icity.iform.utils.XSSFDateUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author renjie
 * @since
 **/
public class ExcelUtilsTest {

    private String path = "/Users/penitence/BuildRepo/test.xlsx";

    @Test
    public void testReadExcel() throws IOException, InvalidFormatException {
        ExcelReaderUtils.ExcelReaderResult<List<Object>> result =  ExcelReaderUtils.readExcel(new FileInputStream(path), 1, 2, 0);

        System.out.println(result.getHeader());

        System.out.println(result.getResult());

    }


}
