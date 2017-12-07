import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class MockMakerTest {

    @Test
    public void isAssinableFromMockObject() {

        ClassA mockClassA = Mockito.mock(ClassA.class);
        
        Assert.assertTrue(ClassA.class.isAssignableFrom(mockClassA.getClass()));
    }   
    
    @Test
    public void whenThenReturnMockObject() {

        InterfaceA mockInterfaceA = Mockito.mock(InterfaceA.class);
        ClassA mockClassA = Mockito.mock(ClassA.class);

        when(mockInterfaceA.getClassA()).thenReturn(mockClassA);
    }

    static class ClassA {}
    static interface InterfaceA {
        ClassA getClassA();
    }
}
