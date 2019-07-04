package tech.ascs.icity.iform.support;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;
import tech.ascs.icity.iform.service.ELProcessorService;
import tech.ascs.icity.iform.service.impl.ELProcessorServiceImpl;

import java.util.*;

/**
 * test {@link tech.ascs.icity.iform.service.impl.ELProcessorServiceImpl}
 *
 * @author renjie
 * @since 0.7.3
 **/
public class JuelProcessorTests {

    private ELProcessorService processorService;
    private Map<String, ItemInstance> testData;

    @Before
    public void init() {
        processorService = new ELProcessorServiceImpl();
        testData = new HashMap<>();
        testData.put("id1", buildInstance("id1", "value1"));
        testData.put("id2", buildInstance("id2", "value2"));
        testData.put("id3", buildInstance("id3", "value3"));

    }

    /**
     * 测试两个为null的时候提交, 应该成功
     */
    @Test
    public void testSubmitSuccess() {
        List<String> result = processorService.checkSubmitProcessor(Collections.emptyMap(), Collections.EMPTY_LIST);
        Assert.assertTrue("返回的结果应该为empty, 表示通过", result.isEmpty());
    }

    @Test
    public void testSubmitHasDataSuccess() {
        List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
        checkInfos.add(buildInfo("${ id1 == 'value1'}", "id1的值错误", 0));
        checkInfos.add(buildInfo("${ id2 == 'value2'}", "id2的值错误", 1));
        checkInfos.add(buildInfo("${ id3 == 'value3'}", "id3的值错误", 2));
        List<String> result = processorService.checkSubmitProcessor(testData, checkInfos);
        System.out.println(result);
        Assert.assertTrue("返回的结果应该为empty, 表示通过", result.isEmpty());
    }

    @Test
    public void testSubmitHasNotKeyContent() {
        List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
        checkInfos.add(buildInfo("${ id4 == 'value1'}", "id1的值错误", 0));
        List<String> result = processorService.checkSubmitProcessor(testData, checkInfos);
        System.out.println(result);
        Assert.assertTrue("返回的结果应该不为空, 表示不通过", !result.isEmpty());
    }

    @Test
    public void testSubmitFaildOnConfusionOrder() {
        List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
        checkInfos.add(buildInfo("${ id1 == 'value3'}", "id1的值错误", 3));
        checkInfos.add(buildInfo("${ id2 == 'value4'}", "id2的值错误", 2));
        checkInfos.add(buildInfo("${ id3 == 'value5'}", "id3的值错误", 1));
        List<String> result = processorService.checkSubmitProcessor(testData, checkInfos);
        System.out.println(result);
        Assert.assertTrue("返回的结果应该不为空, 表示不通过", !result.isEmpty());
        String message = result.get(0);
        Assert.assertEquals("返回信息中的第一条应该为: id3的值错误", "id3的值错误", message);
    }

    @Test
    public void testErrorExpression() {
        List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
        checkInfos.add(buildInfo("id1 == 'value3'", "id1的值错误", 3));
        List<String> result = processorService.checkSubmitProcessor(testData, checkInfos);
        System.out.println(result);
    }

    @Test
    public void testCheckExpressionSuccess() {
        String express = "${ id == 'value'}";
        boolean result = processorService.checkExpressionState(express);
        Assert.assertTrue("表达式应该是正确的", result);
    }

    @Test
    public void testCheckExpressionFail() {
        String express = "${ id = 'value'";
        boolean result = processorService.checkExpressionState(express);
        Assert.assertFalse("表达式应该是错误的", result);
    }

    @Test
    public void testExpressionAndSuccess() {
        List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
        checkInfos.add(buildInfo("${ id1 == 'value1' and id2 == 'value2' }", "id1的值错误", 0));
        List<String> result = processorService.checkSubmitProcessor(testData, checkInfos);
        System.out.println(result);
        Assert.assertTrue("返回的结果应该为empty, 表示通过", result.isEmpty());
    }

    @Test
    public void testRealKeyAndSuccess () {
        List<FormSubmitCheckInfo> checkInfos = new ArrayList<>();
        checkInfos.add(buildInfo("${ 2c9580836bad299c016bb67a7d2f019b == 'value1' and 2c9580836bad299c016bb67a7d30019f == 'value2' }", "id1的值错误", 0));
        Map<String, ItemInstance> map = new HashMap<>();
        map.put("2c9580836bad299c016bb67a7d2f019b", buildInstance("2c9580836bad299c016bb67a7d2f019b", "value1"));
        map.put("2c9580836bad299c016bb67a7d30019f", buildInstance("2c9580836bad299c016bb67a7d30019f", "value2"));

        List<String> result = processorService.checkSubmitProcessor(map, checkInfos);
        System.out.println(result);
        Assert.assertTrue("返回的结果应该为empty, 表示通过", result.isEmpty());
    }

    private ItemInstance buildInstance(String id, String value) {
        ItemInstance instance = new ItemInstance();
        instance.setId(id);
        instance.setValue(value);
        return instance;
    }

    private FormSubmitCheckInfo buildInfo(String cueEx, String cueWord, Integer order) {
        FormSubmitCheckInfo info = new FormSubmitCheckInfo();
        info.setName(cueWord);
        info.setCueExpression(cueEx);
        info.setCueWords(cueWord);
        info.setOrderNo(order);
        return info;
    }

}
