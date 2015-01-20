/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.apache.harmony.xml.dom;

import java.util.Map;
import java.util.TreeMap;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A minimal implementation of DOMConfiguration. This implementation uses inner
 * parameter instances to centralize each parameter's behavior.
 */
public final class DOMConfigurationImpl implements DOMConfiguration {

    private static final Map<String, Parameter> PARAMETERS
            = new TreeMap<String, Parameter>(String.CASE_INSENSITIVE_ORDER);

    static {
        /*
         * True to canonicalize the document (unsupported). This includes
         * removing DocumentType nodes from the tree and removing unused
         * namespace declarations. Setting this to true also sets these
         * parameters:
         *   entities = false
         *   normalize-characters = false
         *   cdata-sections = false
         *   namespaces = true
         *   namespace-declarations = true
         *   well-formed = true
         *   element-content-whitespace = true
         * Setting these parameters to another value shall revert the canonical
         * form to false.
         */
        PARAMETERS.put("canonical-form", new FixedParameter(false));

        /*
         * True to keep existing CDATA nodes; false to replace them/merge them
         * into adjacent text nodes.
         */
        PARAMETERS.put("cdata-sections", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.cdataSections;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.cdataSections = (Boolean) value;
            }
        });

        /*
         * True to check character normalization (unsupported).
         */
        PARAMETERS.put("check-character-normalization", new FixedParameter(false));

        /*
         * True to keep comments in the document; false to discard them.
         */
        PARAMETERS.put("comments", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.comments;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.comments = (Boolean) value;
            }
        });

        /*
         * True to expose schema normalized values. Setting this to true sets
         * the validate parameter to true. Has no effect when validate is false.
         */
        PARAMETERS.put("datatype-normalization", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.datatypeNormalization;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                if ((Boolean) value) {
                    config.datatypeNormalization = true;
                    config.validate = true;
                } else {
                    config.datatypeNormalization = false;
                }
            }
        });

        /*
         * True to keep whitespace elements in the document; false to discard
         * them (unsupported).
         */
        PARAMETERS.put("element-content-whitespace", new FixedParameter(true));

        /*
         * True to keep entity references in the document; false to expand them.
         */
        PARAMETERS.put("entities", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.entities;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.entities = (Boolean) value;
            }
        });

        /*
         * Handler to be invoked when errors are encountered.
         */
        PARAMETERS.put("error-handler", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.errorHandler;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.errorHandler = (DOMErrorHandler) value;
            }
            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || value instanceof DOMErrorHandler;
            }
        });

        /*
         * Bulk alias to set the following parameter values:
         *   validate-if-schema = false
         *   entities = false
         *   datatype-normalization = false
         *   cdata-sections = false
         *   namespace-declarations = true
         *   well-formed = true
         *   element-content-whitespace = true
         *   comments = true
         *   namespaces = true.
         * Querying this returns true if all of the above parameters have the
         * listed values; false otherwise.
         */
        PARAMETERS.put("infoset", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                // validate-if-schema is always false
                // element-content-whitespace is always true
                // namespace-declarations is always true
                return !config.entities
                        && !config.datatypeNormalization
                        && !config.cdataSections
                        && config.wellFormed
                        && config.comments
                        && config.namespaces;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                if ((Boolean) value) {
                    // validate-if-schema is always false
                    // element-content-whitespace is always true
                    // namespace-declarations is always true
                    config.entities = false;
                    config.datatypeNormalization = false;
                    config.cdataSections = false;
                    config.wellFormed = true;
                    config.comments = true;
                    config.namespaces = true;
                }
            }
        });

        /*
         * True to perform namespace processing; false for none.
         */
        PARAMETERS.put("namespaces", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.namespaces;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.namespaces = (Boolean) value;
            }
        });

        /**
         * True to include namespace declarations; false to discard them
         * (unsupported). Even when namespace declarations are discarded,
         * prefixes are retained.
         *
         * Has no effect if namespaces is false.
         */
        PARAMETERS.put("namespace-declarations", new FixedParameter(true));

        /*
         * True to fully normalize characters (unsupported).
         */
        PARAMETERS.put("normalize-characters", new FixedParameter(false));

        /*
         * A list of whitespace-separated URIs representing the schemas to validate
         * against. Has no effect if schema-type is null.
         */
        PARAMETERS.put("schema-location", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.schemaLocation;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.schemaLocation = (String) value;
            }
            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || value instanceof String;
            }
        });

        /*
         * URI representing the type of schema language, such as
         * "http://www.w3.org/2001/XMLSchema" or "http://www.w3.org/TR/REC-xml".
         */
        PARAMETERS.put("schema-type", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.schemaType;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.schemaType = (String) value;
            }
            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || value instanceof String;
            }
        });

        /*
         * True to split CDATA sections containing "]]>"; false to signal an
         * error instead.
         */
        PARAMETERS.put("split-cdata-sections", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.splitCdataSections;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.splitCdataSections = (Boolean) value;
            }
        });

        /*
         * True to require validation against a schema or DTD. Validation will
         * recompute element content whitespace, ID and schema type data.
         *
         * Setting this unsets validate-if-schema.
         */
        PARAMETERS.put("validate", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.validate;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                // validate-if-schema is always false
                config.validate = (Boolean) value;
            }
        });

        /*
         * True to validate if a schema was declared (unsupported). Setting this
         * unsets validate.
         */
        PARAMETERS.put("validate-if-schema", new FixedParameter(false));

        /*
         * True to report invalid characters in node names, attributes, elements,
         * comments, text, CDATA sections and processing instructions.
         */
        PARAMETERS.put("well-formed", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.wellFormed;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.wellFormed = (Boolean) value;
            }
        });

        // TODO add "resource-resolver" property for use with LS feature...
    }

    private boolean cdataSections = true;
    private boolean comments = true;
    private boolean datatypeNormalization = false;
    private boolean entities = true;
    private DOMErrorHandler errorHandler;
    private boolean namespaces = true;
    private String schemaLocation;
    private String schemaType;
    private boolean splitCdataSections = true;
    private boolean validate = false;
    private boolean wellFormed = true;

    interface Parameter {
        Object get(DOMConfigurationImpl config);
        void set(DOMConfigurationImpl config, Object value);
        boolean canSet(DOMConfigurationImpl config, Object value);
    }

    static class FixedParameter implements Parameter {
        final Object onlyValue;
        FixedParameter(Object onlyValue) {
            this.onlyValue = onlyValue;
        }
        public Object get(DOMConfigurationImpl config) {
            return onlyValue;
        }
        public void set(DOMConfigurationImpl config, Object value) {
            if (!onlyValue.equals(value)) {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported value: " + value);
            }
        }
        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return onlyValue.equals(value);
        }
    }

    static abstract class BooleanParameter implements Parameter {
        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value instanceof Boolean;
        }
    }

    public boolean canSetParameter(String name, Object value) {
        Parameter parameter = PARAMETERS.get(name);
        return parameter != null && parameter.canSet(this, value);
    }

    public void setParameter(String name, Object value) throws DOMException {
        Parameter parameter = PARAMETERS.get(name);
        if (parameter == null) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "No such parameter: " + name);
        }
        try {
            parameter.set(this, value);
        } catch (NullPointerException e) {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
                    "Null not allowed for " + name);
        } catch (ClassCastException e) {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
                    "Invalid type for " + name + ": " + value.getClass());
        }
    }

    public Object getParameter(String name) throws DOMException {
        Parameter parameter = PARAMETERS.get(name);
        if (parameter == null) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "No such parameter: " + name);
        }
        return parameter.get(this);
    }

    public DOMStringList getParameterNames() {
        final String[] result = PARAMETERS.keySet().toArray(new String[PARAMETERS.size()]);
        return new DOMStringList() {
            public String item(int index) {
                return index < result.length ? result[index] : null;
            }
            public int getLength() {
                return result.length;
            }
            public boolean contains(String str) {
                return PARAMETERS.containsKey(str); // case-insensitive.
            }
        };
    }

    public void normalize(Node node) {
        /*
         * Since we don't validate, this code doesn't take into account the
         * following "supported" parameters: datatype-normalization, entities,
         * schema-location, schema-type, or validate.
         *
         * TODO: normalize namespaces
         */

        switch (node.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
                CDATASectionImpl cdata = (CDATASectionImpl) node;
                if (cdataSections) {
                    if (cdata.needsSplitting()) {
                        if (splitCdataSections) {
                            cdata.split();
                            report(DOMError.SEVERITY_WARNING, "cdata-sections-splitted");
                        } else {
                            report(DOMError.SEVERITY_ERROR, "wf-invalid-character");
                        }
                    }
                    checkTextValidity(cdata.buffer);
                    break;
                }
                node = cdata.replaceWithText();
                // fall through

            case Node.TEXT_NODE:
                TextImpl text = (TextImpl) node;
                text = text.minimize();
                if (text != null) {
                    checkTextValidity(text.buffer);
                }
                break;

            case Node.COMMENT_NODE:
                CommentImpl comment = (CommentImpl) node;
                if (!comments) {
                    comment.getParentNode().removeChild(comment);
                    break;
                }
                if (comment.containsDashDash()) {
                    report(DOMError.SEVERITY_ERROR, "wf-invalid-character");
                }
                checkTextValidity(comment.buffer);
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                checkTextValidity(((ProcessingInstructionImpl) node).getData());
                break;

            case Node.ATTRIBUTE_NODE:
                checkTextValidity(((AttrImpl) node).getValue());
                break;

            case Node.ELEMENT_NODE:
                ElementImpl element = (ElementImpl) node;
                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    normalize(attributes.item(i));
                }
                // fall through

            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
                Node next;
                for (Node child = node.getFirstChild(); child != null; child = next) {
                    // lookup next eagerly because normalize() may remove its subject
                    next = child.getNextSibling();
                    normalize(child);
                }
                break;

            case Node.NOTATION_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
                break;

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + node.getNodeType());
        }
    }

    private void checkTextValidity(CharSequence s) {
        if (wellFormed && !isValid(s)) {
            report(DOMError.SEVERITY_ERROR, "wf-invalid-character");
        }
    }

    /**
     * Returns true if all of the characters in the text are permitted for use
     * in XML documents.
     */
    private boolean isValid(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // as defined by http://www.w3.org/TR/REC-xml/#charsets.
            boolean valid = c == 0x9 || c == 0xA || c == 0xD
                    || (c >= 0x20 && c <= 0xd7ff)
                    || (c >= 0xe000 && c <= 0xfffd);
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    private void report(short severity, String type) {
        if (errorHandler != null) {
            // TODO: abort if handleError returns false
            errorHandler.handleError(new DOMErrorImpl(severity, type));
        }
    }
}
