package tech.ascs.icity.iform.utils;

import com.google.common.collect.Sets;
import tech.ascs.icity.ICityException;
import tech.ascs.icity.iform.api.model.export.ImportType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author renjie
 * @since 0.7.3
 **/
public final class ImportDataComputer {

    public interface DataCompute extends BiFunction<Map<String, Map<String, Object>>, Map<String, Map<String, Object>>, List<Map<String, Object>>> {

    }

    private static DataCompute SAVE_ONLY = (importDatas, currentDatas) -> Sets.difference(importDatas.keySet(), currentDatas.keySet())
            .stream()
            .map(importDatas::get)
            .collect(Collectors.toList());


    private static DataCompute UPDATE_ONLY = (importDatas, currentDatas) -> Sets.intersection(importDatas.keySet(), currentDatas.keySet())
            .stream()
            .map(key -> {
                Map<String, Object> importRow = importDatas.get(key);
                Map<String, Object> currentRow = currentDatas.get(key);
                currentRow.putAll(importRow);
                return currentRow;
            })
            .collect(Collectors.toList());

    private static DataCompute SAVE_OR_UPDATE = (importDatas, currentDatas) -> importDatas.keySet()
            .stream()
            .map(key -> {
                Map<String, Object> importRow = importDatas.get(key);
                if (currentDatas.containsKey(key)) {
                    Map<String, Object> currentRow = currentDatas.get(key);
                    currentRow.putAll(importRow);
                    return currentRow;
                } else {
                    return importRow;
                }
            })
            .collect(Collectors.toList());

    private static DataCompute REWRITE = (importDatas, currentDatas) -> new ArrayList<>(importDatas.values());

    public static DataCompute getInstance(ImportType importType) {
        switch (importType) {
            case SaveOrUpdate:
                return SAVE_OR_UPDATE;
            case SaveOnly:
                return SAVE_ONLY;
            case Rewrite:
                return REWRITE;
            case UpdateOnly:
                return UPDATE_ONLY;
            default:
                throw new ICityException("未支持的导入方式");
        }
    }

}
