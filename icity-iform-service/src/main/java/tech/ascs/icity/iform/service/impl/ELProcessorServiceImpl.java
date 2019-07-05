package tech.ascs.icity.iform.service.impl;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.tree.TreeBuilderException;
import de.odysseus.el.util.SimpleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.service.ELProcessorService;
import tech.ascs.icity.iform.utils.ELProcessorUtils;

import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ELProcessorService基于JUEL的实现类
 *
 * @author renjie
 * @since 0.7.3.0
 **/
@Service
public class ELProcessorServiceImpl implements ELProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ELProcessorServiceImpl.class);

    private final ExpressionFactory expressionFactory;

    private final SimpleContext emptyContext = new SimpleContext();

    public ELProcessorServiceImpl() {
        this.expressionFactory = new ExpressionFactoryImpl();
    }

    @Override
    public boolean checkExpressionState(String expression) {
        if (expression == null) {
            return true;
        }
        try {
            expressionFactory.createValueExpression(emptyContext, ELProcessorUtils.process(expression), Object.class);
            return true;
        } catch (PropertyNotFoundException e) {
            return true;
        } catch (TreeBuilderException e) {
            return false;
        }
    }

    @Override
    public List<String> checkSubmitProcessor(Map<String, ItemInstance> itemMapping, List<FormSubmitCheckInfo> checkInfos) {

        Map<String, Object> contextData = itemMapping.entrySet()
                .stream()
                .filter(entry -> Objects.nonNull(entry.getValue().getValue()))
                .collect(Collectors.toMap(this::processMapKey, entry -> entry.getValue().getValue()));

        SimpleContext context = new SimpleContext();

        contextData.forEach((key, value) -> context.setVariable(key, expressionFactory.createValueExpression(value, value.getClass())));

        return checkInfos.stream()
                .sorted()
                .filter(info -> !isPass(context, info.getCueExpression()))
                .findAny()
                .map(info -> Collections.singletonList(info.getCueWords()))
                .orElse(Collections.emptyList());
    }

    private boolean isPass(SimpleContext context, String expression) {
        ValueExpression valueExpression = expressionFactory.createValueExpression(context, ELProcessorUtils.process(expression), Boolean.class);
        try {
            return Boolean.valueOf(Objects.toString(valueExpression.getValue(context), "false"));
        } catch (PropertyNotFoundException e) {
            LOGGER.warn("表达式里有不存在的属性: {}", e.getMessage().substring(21));
            return false;
        }
    }

    private String processMapKey(Map.Entry<String, ItemInstance> entry) {
        return "_" + entry.getKey();
    }

}
