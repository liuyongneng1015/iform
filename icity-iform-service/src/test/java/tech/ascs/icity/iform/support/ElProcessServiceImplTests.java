package tech.ascs.icity.iform.support;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.service.ELProcessorService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author renjie
 * @since 0.7.3
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ElProcessServiceImplTests {

    @Autowired
    private ELProcessorService service;

    @Test
    public void testElVilad() {
        String expression = "2c9580836bad299c016bb0b6aab00057 !== undefined && 2c9580836bad299c016bb0b6aab00057 < 4";


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

    @Test
    public void testArrayNotEmpty() {
        String expression = "2c9580836bdacdcf016bdea2c53f002a !== undefined && 2c9580836bdacdcf016bdea2c53f002a != []";

        String id = "2c9580836bdacdcf016bdea2c53f002a";

        ItemInstance instance = new ItemInstance();
        instance.setValue(new Integer[] {});
        instance.setId(id);
        Map<String, ItemInstance> context = new HashMap<>();
        context.put(id, instance);

        FormSubmitCheckInfo info = new FormSubmitCheckInfo();
        info.setCueExpression(expression);
        info.setCueWords("数组不能为空");

        List<String> msgs = service.checkSubmitProcessor(context, Arrays.asList(info));

        System.out.println(msgs);

        Assert.assertTrue("应该存在一个错误信息", msgs.size() == 1);

        Assert.assertEquals("错误信息应该相等", "数组不能为空", msgs.get(0));
    }

    @Test
    public void testNoVarInProcess() {
        String expression = "2c9580836bdacdcf016bdea2c53f002b !== undefined && 2c9580836bdacdcf016bdea2c53f002b != []";

        String id = "2c9580836bdacdcf016bdea2c53f002a";

        ItemInstance instance = new ItemInstance();
        instance.setValue(new Integer[] {});
        instance.setId(id);
        Map<String, ItemInstance> context = new HashMap<>();
        context.put(id, instance);

        FormSubmitCheckInfo info = new FormSubmitCheckInfo();
        info.setCueExpression(expression);
        info.setCueWords("数组不能为空");

        List<String> msgs = service.checkSubmitProcessor(context, Arrays.asList(info));

        System.out.println(msgs);

        Assert.assertTrue("应该存在一个错误信息", msgs.size() == 1);

        Assert.assertEquals("错误信息应该相等", "数组不能为空", msgs.get(0));
    }

    @Test
    public void testErrorReturnValue() {
        String expression = "2c9580836bdacdcf016bdea2c53f002a";

        String id = "2c9580836bdacdcf016bdea2c53f002a";

        ItemInstance instance = new ItemInstance();
        instance.setValue(new Integer[] {});
        instance.setId(id);
        Map<String, ItemInstance> context = new HashMap<>();
        context.put(id, instance);

        FormSubmitCheckInfo info = new FormSubmitCheckInfo();
        info.setCueExpression(expression);
        info.setCueWords("数组不能为空");

        List<String> msgs = service.checkSubmitProcessor(context, Arrays.asList(info));

        System.out.println(msgs);

        Assert.assertTrue("应该存在一个错误信息", msgs.size() == 1);

        Assert.assertEquals("错误信息应该相等", "数组不能为空", msgs.get(0));
    }

    @Test
    public void testELReturnBooleanType() {
        String expression = "2c9580836bdacdcf016bdea2c53f002a == null";

        boolean result = service.checkExpressionState(expression);

        Assert.assertTrue("返回的表达式的检查结果应该为true", result);
    }

    @Test
    public void testELReturnNoBooleanType() {
        String expression = "2c9580836bdacdcf016bdea2c53f002a";

        boolean result = service.checkExpressionState(expression);

        Assert.assertFalse("返回的表达式的检查结果应该为false", result);
    }

    @Test
    public void testErrrorEl() {
        String expression = "2c9580836bdacdcf016bdea2c53f002a]";

        boolean result = service.checkExpressionState(expression);

        Assert.assertFalse("返回的表达式的检查结果应该为false", result);
    }


}
