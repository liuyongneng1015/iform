package tech.ascs.icity.iform.support;

import org.junit.Assert;
import org.junit.Test;
import tech.ascs.icity.iform.utils.ELProcessorUtils;

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
}