package tech.ascs.icity.iform.support;

import org.junit.Assert;
import org.junit.Test;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.service.ELProcessorService;
import tech.ascs.icity.iform.service.impl.ELProcessorServiceImpl;
import tech.ascs.icity.iform.utils.ELProcessorUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * test {@link tech.ascs.icity.iform.utils.ELProcessorUtils}
 * @author renjie
 * @since 0.7.3
 **/
public class ELProcessorUtilsTests {

    @Test
    public void testNormalExpressionProcess(){
        String expression = "${ 2c9580836bad299c016bb67a7d2f019b == '2c9580836bad299c016bb67a7d2f019b  ' and 2c9580836bad299c016bb67a7d2f019c == 'c' }";

        String newExpression = "${ _2c9580836bad299c016bb67a7d2f019b == '2c9580836bad299c016bb67a7d2f019b  ' and _2c9580836bad299c016bb67a7d2f019c == 'c' }";

        String processExpression = ELProcessorUtils.process(expression);

        Assert.assertEquals("加工之后应该变成期望值", newExpression, processExpression);
    }

    @Test
    public void testNoKeyExpressionProcess() {
        String expression = "${ '2c9580836bad299c016bb67a7d2f019b' == '2c9580836bad299c016bb67a7d2f019b  ' and '2c9580836bad299c016bb67a7d2f019c' == 'c' }";

        String processExpression = ELProcessorUtils.process(expression);

        Assert.assertEquals("加工之后应该不变", expression, processExpression);

    }

    @Test
    public void testProcessInJsEL () {
        String expression = "2c9580836bad299c016bb0b6aab00057 !== undefined && 2c9580836bad299c016bb0b6aab00057 < 4";

        System.out.println(ELProcessorUtils.process(expression));
    }

    @Test
    public void testElVilad() {
        String expression = "2c9580836bad299c016bb0b6aab00057 !== undefined && 2c9580836bad299c016bb0b6aab00057 < 4";

        ELProcessorService service = new ELProcessorServiceImpl();

        String id = "2c9580836bad299c016bb0b6aab00057";

        ItemInstance instance = new ItemInstance();
        instance.setValue(1);
        instance.setId(id);
        Map<String, ItemInstance> context = new HashMap<>();
        context.put(id, instance);

        FormSubmitCheckInfo info = new FormSubmitCheckInfo();
        info.setCueExpression(expression);
        info.setCueWords("不能大于4");

        List<String> msgs = service.checkSubmitProcessor(context, Arrays.asList(info));

        System.out.println(msgs);
    }
}