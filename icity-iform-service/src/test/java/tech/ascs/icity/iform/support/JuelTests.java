package tech.ascs.icity.iform.support;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import org.junit.Assert;
import org.junit.Test;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/**
 * @author renjie
 * @since
 **/
public class JuelTests {


    @Test
    public void testSimple() {
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext simpleContext = new SimpleContext();

        simpleContext.setVariable("id1",  factory.createValueExpression("hello", String.class));
        simpleContext.setVariable("id2",  factory.createValueExpression("world", String.class));

        String a = "${id1 == 'hello'}";

        ValueExpression valueExpression = factory.createValueExpression(simpleContext, a, Boolean.class);

        Object value = valueExpression.getValue(simpleContext);

        Assert.assertTrue("应该返回true",(Boolean) value);

    }

}
