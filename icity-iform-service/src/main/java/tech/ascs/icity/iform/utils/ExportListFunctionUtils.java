package tech.ascs.icity.iform.utils;

import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.DefaultFunctionType;
import tech.ascs.icity.iform.api.model.ParseArea;
import tech.ascs.icity.iform.api.model.ReturnResult;
import tech.ascs.icity.iform.api.model.export.*;
import tech.ascs.icity.iform.model.ExportListFunction;
import tech.ascs.icity.iform.model.ImportBaseFunctionEntity;
import tech.ascs.icity.iform.model.ListFunction;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 导出功能相关的工具类
 * @author renjie
 * @since 0.7.3
 **/
public class ExportListFunctionUtils {

    private static DefaultFunctionType[] functionDefaultActions = {DefaultFunctionType.Export, DefaultFunctionType.TemplateDownload, DefaultFunctionType.Import};
    private static String[] parseAreas = { ParseArea.PC.value(), ParseArea.PC.value(), ParseArea.PC.value()};
    private static String[] functionDefaultIcons = new String[]{ null, null, null};
    private static String[] functionDefaultMethods = new String[]{"GET", "GET", "POST"};
    private static Boolean[] functionVisibles = {true, true, true};
    private static List<Consumer<ListFunction>> functionOtherControl = Arrays.asList(ExportListFunctionUtils::assemblyDefaultExportListFunction, null, ExportListFunctionUtils::assemblyDefaultImportBaseFunction);

    public static ListFunction generateListFunction(FunctionsType type) {
        int i = type.index;
        ListFunction function = new ListFunction();
        function.setLabel(functionDefaultActions[i].getDesc());
        function.setAction(functionDefaultActions[i].getValue());
        function.setReturnResult(ReturnResult.NONE);
        function.setMethod(functionDefaultMethods[i]);
        function.setParseArea(parseAreas[i]);
        function.setIcon(functionDefaultIcons[i]);
        function.setVisible(functionVisibles[i]);
        function.setSystemBtn(true);
        function.setOrderNo(i+10);
        if (functionOtherControl.get(i) != null) {
            functionOtherControl.get(i).accept(function);
        }
        return function;
    }

    public static void assemblyDefaultExportListFunction(ListFunction listFunction) {
        ExportListFunction function = new ExportListFunction();
        function.setFormat(ExportFormat.Excel);
        function.setControl(ExportControl.All);
        function.setType(ExportType.All);
        listFunction.setExportFunction(function);
    }

    public static void assemblyDefaultImportBaseFunction(ListFunction listFunction) {
        ImportBaseFunctionEntity entity = new ImportBaseFunctionEntity();
        entity.setDateSeparator("-");
        entity.setTimeSeparator(":");
        entity.setDateFormatter("yyyy/MM/dd HH:mm:ss");
        entity.setFileType(ImportFileType.Excel);
        entity.setType(ImportType.SaveOrUpdate);
        listFunction.setImportFunction(entity);
    }

    public enum FunctionsType {
        /**
         * 导出功能
         */
        Export(DefaultFunctionType.Export.getValue(),0),
        /**
         * 模板下载功能
         */
        TemplateDownload(DefaultFunctionType.TemplateDownload.getValue(), 1),
        /**
         * 导入功能
         */
        Import(DefaultFunctionType.Import.getValue(), 2);

        private int index;
        private String name;

        private FunctionsType(String name, int index) {
            this.index = index;
            this.name = name;
        }

        public static FunctionsType valueOfName(String value) {
            for (FunctionsType type: values()) {
                if (type.name.equals(value)) {
                    return type;
                }
            }
            throw new ICityException("功能类型无法转换");
        }
    }
}
