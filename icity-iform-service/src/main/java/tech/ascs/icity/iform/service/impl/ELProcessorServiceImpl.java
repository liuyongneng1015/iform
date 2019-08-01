package tech.ascs.icity.iform.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import tech.ascs.icity.iform.api.model.*;
import tech.ascs.icity.iform.model.FormModelEntity;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.model.SubFormItemModelEntity;
import tech.ascs.icity.iform.service.ELProcessorService;
import tech.ascs.icity.iform.utils.ELProcessorUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ELProcessorService基于Spel的实现类
 *
 * @author renjie
 * @since 0.7.3.0
 **/
@Service
public class ELProcessorServiceImpl implements ELProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ELProcessorServiceImpl.class);

    private final SpelExpressionParser parser;

    public ELProcessorServiceImpl() {
        SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.OFF,
                this.getClass().getClassLoader());
        this.parser = new SpelExpressionParser(config);
    }

    @Override
    public boolean checkExpressionState(String expression) {
        if (expression == null) {
            return true;
        }
        try {
            Class<?> returnType = parser.parseExpression(ELProcessorUtils.process(expression)).getValueType();
            return Boolean.class.equals(returnType);
        } catch (SpelParseException e) {
            LOGGER.warn("{} 不是正确的Spel表达式", ELProcessorUtils.process(expression));
            return false;
        }

    }

    @Override
    public List<String> checkSubmitProcessor(Map<String, ItemInstance> itemMapping, List<FormSubmitCheckInfo> checkInfos) {

        Map<String, Object> contextData = itemMapping.entrySet()
                .stream()
                .filter(entry -> Objects.nonNull(entry.getValue().getValue()))
                .collect(Collectors.toMap(this::processMapKey, entry -> entry.getValue().getValue()));

        EvaluationContext context = new StandardEvaluationContext();

        contextData.forEach(context::setVariable);

        return checkInfos.stream()
                .sorted()
                .filter(info -> !isPass(context, info.getCueExpression()))
                .findAny()
                .map(info -> Collections.singletonList(info.getCueWords()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<String> checkSubmitProcessor(FormDataSaveInstance instance, FormModelEntity mainEntity, Function<String, FormModelEntity> subFormModelFunction) {
        Map<String, ItemInstance> mainFormInstanceMapping = instance.getItems().parallelStream()
                .collect(Collectors.toMap(ItemInstance::getId, i -> i));

        List<FormSubmitCheckInfo> submitCheckInfos = mainEntity.getSubmitChecks();


        List<SubFormItemModelEntity> subFormItemModelEntities = Optional.ofNullable(mainEntity.getItems())
                .map(items -> items.stream()
                        .filter(item -> item.getSystemItemType() == SystemItemType.SubForm)
                        .map(item -> (SubFormItemModelEntity) item)
                        .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());

        // 如果当前不存在子表 : 当然这样最好
        if (subFormItemModelEntities.size() == 0) {
            return checkSubmitProcessor(mainFormInstanceMapping, submitCheckInfos);
        } else {
            // 当存在子表的时候需要做如下处理
            // 1. 展开instance中的子表内容
            // 2. 判断某个子表是否没有传值, 没有的话把对应的id放入到跳过组内, 有的放到执行组
            // 3. 根据跳过组合执行组对校验表达式重新整理
            // 4. 过滤掉跳过的表达式
            // 5. 对展开的instance做校验

            //子表内容展开
            List<Map<String, ItemInstance>> subFormInstanceList = extendSubFormData(instance.getSubFormData());
            Set<String> skipId = findSkipId(subFormItemModelEntities, subFormInstanceList.stream().limit(1).findAny().orElse(new HashMap<>(0)));

            // 执行的表达式
            List<FormSubmitCheckInfo> submitCheckInfoExec = submitCheckInfos.stream()
                    .filter(info -> skipId.stream().noneMatch(id -> info.getCueExpression().contains(id)))
                    .collect(Collectors.toList());

            // 如果没有子表数据, 只提交主表数据和校验规则
            if (subFormInstanceList.isEmpty()) {
                return checkSubmitProcessor(mainFormInstanceMapping, submitCheckInfoExec);
            }

            for (Map<String, ItemInstance> subInstanceMap : subFormInstanceList) {
                Map<String, ItemInstance> commitInstance = new HashMap<>(mainFormInstanceMapping);
                commitInstance.putAll(subInstanceMap);
                List<String> msgs = checkSubmitProcessor(commitInstance, submitCheckInfoExec);
                if (msgs.size() != 0) {
                    return msgs;
                }
            }

        }
        return Collections.emptyList();
    }

    private boolean isPass(EvaluationContext context, String expression) {
        Boolean result = parser.parseExpression(ELProcessorUtils.process(expression)).getValue(context, Boolean.class);
        return result == null ? true : result;
    }

    private String processMapKey(Map.Entry<String, ItemInstance> entry) {
        return "_" + entry.getKey();
    }

    /**
     * 展开子表内容, 需要完成 : 多个子表的情况下, 每个返回的Map内都会包含所有子表的内容, 重复也可以
     *
     */
    private List<Map<String, ItemInstance>> extendSubFormData(List<SubFormItemInstance> subFormItemInstances) {

        // 二维数组, 第一层表示子表, 第二层表示子表子项(行), Map表示该行内容
        List<List<Map<String, ItemInstance>>> multiSubFormInstance = subFormItemInstances.stream()
                .map(SubFormItemInstance::getItemInstances)
                .map(instance -> instance.stream().map(SubFormDataItemInstance::getItems).collect(Collectors.toList()))
                .map(i1 -> i1.stream().map(i2 -> i2.stream().map(SubFormRowItemInstance::getItems).flatMap(Collection::stream).collect(Collectors.toMap(ItemInstance::getId, i -> i))).collect(Collectors.toList()))
                .collect(Collectors.toList());

        List<Map<String, ItemInstance>> extendFormData = new ArrayList<>();

        int subFormSize = multiSubFormInstance.size();
        int maxRowSize = multiSubFormInstance.stream().map(List::size).max(Integer::compareTo).orElse(0);

        for (int i = 0; i < maxRowSize; i ++) {

            Map<String, ItemInstance> tempMapping = new HashMap<>();
            for (int j = 0; j < subFormSize; j ++) {
                // 子表
                List<Map<String, ItemInstance>> subFormData = multiSubFormInstance.get(j);
                tempMapping.putAll(getOrLast(i, subFormData));
            }

            extendFormData.add(tempMapping);
        }
        return extendFormData;
    }

    private <T> T getOrLast(int index, List<T> list) {
        return list.get(Math.min(index, list.size() - 1));
    }

    private Set<String> findSkipId(List<SubFormItemModelEntity> subFormItemModelEntities, Map<String, ItemInstance> subFormData) {
        Set<String> skipId = new HashSet<>();
        subFormItemModelEntities.stream()
                .flatMap(entry -> entry.getItems().stream())
                .flatMap(entry -> entry.getItems().stream())
                .forEach(item -> {
                    if (!subFormData.containsKey(item.getId())) {
                        skipId.add(item.getId());
                    }
                });
        return skipId;
    }

}
