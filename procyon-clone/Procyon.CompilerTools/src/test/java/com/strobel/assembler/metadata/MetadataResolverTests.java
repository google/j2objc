package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.Instruction;
import com.strobel.assembler.ir.OpCode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetadataResolverTests {
    @Test
    public void testGenericParameterArrayResolution() throws Throwable {
        final String typeName = GenericArrayTest.class.getName().replace('.', '/');
        final TypeDefinition td = MetadataSystem.instance().lookupType(typeName).resolve();
        final List<MethodReference> mds = MetadataHelper.findMethods(td, MetadataFilters.matchName("getData"));

        assertEquals(1, mds.size());

        final MethodDefinition md = mds.get(0).resolve();

        assertNotNull(md.getReturnType().resolve());

        for(final Instruction p : md.getBody().getInstructions()) {
            if(p.getOpCode() == OpCode.GETFIELD) {
                final FieldReference fr = p.getOperand(0);
                final FieldReference fd = fr.resolve();

                assertNotNull(fr.getFieldType());
                assertNotNull(fr.getFieldType().resolve());

                assertNotNull(fd);
                assertNotNull(fd.getFieldType());
                assertNotNull(fd.getFieldType().resolve());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Helper Classes">

    @SuppressWarnings("unused")
    private static final class GenericArrayTest<T> {
        private T[] data;

        public T[] getData() {
            return data;
        }
    }

    // </editor-fold>
}
