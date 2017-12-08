import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class MockMakerTest {

    @Test
    public void isAssinableFromMockObject() {

        ClassA mockClassA = Mockito.mock(ClassA.class);
        
        assertTrue(ClassA.class.isAssignableFrom(mockClassA.getClass()));
    }   
    
    @Test
    public void whenThenReturnMockObject() {

        InterfaceA mockInterfaceA = Mockito.mock(InterfaceA.class);
        ClassA mockClassA = Mockito.mock(ClassA.class);

        when(mockInterfaceA.getClassA()).thenReturn(mockClassA);
    }

    @Test
    public void shouldInstanceOfOriginalClass_true() {

        ClassA mockClassA = Mockito.mock(ClassA.class);

        assertTrue(mockClassA instanceof ClassA);
    }

    @Test
    public void shouldInstanceOfOriginalInterface_true() {

        InterfaceA mockInterfaceA = Mockito.mock(InterfaceA.class);

        assertTrue(mockInterfaceA instanceof InterfaceA);
    }

    static class ClassA {}
    static interface InterfaceA {
        ClassA getClassA();
    }
}
