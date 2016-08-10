package ut.com.developex.plugins.confluence;

import org.junit.Test;
import com.developex.plugins.confluence.api.MyPluginComponent;
import com.developex.plugins.confluence.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}