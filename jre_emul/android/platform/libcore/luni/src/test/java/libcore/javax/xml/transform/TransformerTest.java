/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.javax.xml.transform;

import java.util.Properties;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TransformerTest {

    @Test(expected = UnsupportedOperationException.class)
    public void reset() {
        Transformer transformer = new TransformerImpl();
        transformer.reset();
    }

    private static final class TransformerImpl extends Transformer {

        @Override
        public void reset() { super.reset(); }

        @Override
        public void transform(Source xmlSource, Result outputTarget) throws TransformerException {}

        @Override
        public void setParameter(String name, Object value) {}

        @Override
        public Object getParameter(String name) { return null; }

        @Override
        public void clearParameters() {}

        @Override
        public void setURIResolver(URIResolver resolver) {}

        @Override
        public URIResolver getURIResolver() { return null;  }

        @Override
        public void setOutputProperties(Properties oformat) {}

        @Override
        public Properties getOutputProperties() { return null; }

        @Override
        public void setOutputProperty(String name, String value) throws IllegalArgumentException {}

        @Override
        public String getOutputProperty(String name) throws IllegalArgumentException {
            return null;
        }

        @Override
        public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {}

        @Override
        public ErrorListener getErrorListener() { return null; }
    }
}
