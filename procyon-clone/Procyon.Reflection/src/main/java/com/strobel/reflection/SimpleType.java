package com.strobel.reflection;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 2:50 PM
 */
public enum SimpleType {
    Boolean,
    Byte,
    Character,
    Short,
    Integer,
    Long,
    Float,
    Double,
    Object,
    Array,
    TypeVariable,
    Wildcard,
    Void;

    public final String getDescriptorPrefix() {
        switch (this) {
            case Boolean:
                return "Z";
            case Byte:
                return "B";
            case Character:
                return "C";
            case Short:
                return "S";
            case Integer:
                return "I";
            case Long:
                return "J";
            case Float:
                return "F";
            case Double:
                return "D";
            case Object:
                return "L";
            case Array:
                return "[";
            case TypeVariable:
                return "T";
            case Wildcard:
                return "*";
            case Void:
                return "V";
            default:
                return "L";
        }
    }

    public final String getPrimitiveName() {
        switch (this) {
            case Boolean:
                return "boolean";
            case Byte:
                return "byte";
            case Character:
                return "char";
            case Short:
                return "short";
            case Integer:
                return "int";
            case Long:
                return "long";
            case Float:
                return "float";
            case Double:
                return "double";
            case Void:
                return "void";
            default:
                return null;
        }
    }

    public final boolean isPrimitive() {
        switch (this) {
            case Object:
            case Array:
            case TypeVariable:
            case Wildcard:
            case Void:
                return false;
            default:
                return true;
        }
    }

    public final boolean isPrimitiveOrVoid() {
        switch (this) {
            case Object:
            case Array:
            case TypeVariable:
            case Wildcard:
                return false;
            default:
                return true;
        }
    }

    public final int bitWidth() {
        switch (this) {
            case Boolean:
                return 1;
            case Byte:
                return 8;
            case Character:
            case Short:
                return 16;
            case Integer:
                return 32;
            case Long:
                return 64;
            case Float:
                return 32;
            case Double:
                return 64;
            default:
                return 0;
        }
    }

    public final int stackSlots() {
        switch (this) {
            case Long:
            case Double:
                return 2;
            case Void:
                return 0;
            default:
                return 1;
        }
    }

    public final boolean isSingleWord() {
        switch (this) {
            case Long:
            case Double:
            case Void:
                return false;
            default:
                return true;
        }
    }

    public final boolean isDoubleWord() {
        switch (this) {
            case Long:
            case Double:
                return true;
            default:
                return false;
        }
    }

    public final boolean isNumeric() {
        switch (this) {
            case Boolean:
            case Byte:
            case Character:
            case Short:
            case Integer:
            case Long:
            case Float:
            case Double:
                return true;
            default:
                return false;
        }
    }

    public final boolean isIntegral() {
        switch (this) {
            case Boolean:
            case Byte:
            case Character:
            case Short:
            case Integer:
            case Long:
                return true;
            default:
            case Float:
            case Double:
                return false;
        }
    }

    public final boolean isSubWordOrInt32() {
        switch (this) {
            case Boolean:
            case Byte:
            case Character:
            case Short:
            case Integer:
                return true;
            default:
                return false;
        }
    }

    public final boolean isSigned() {
        switch (this) {
            default:
            case Boolean:
            case Character:
                return false;
            case Byte:
            case Short:
            case Integer:
            case Long:
            case Float:
            case Double:
                return true;
        }
    }

    public final boolean isUnsigned() {
        switch (this) {
            case Boolean:
            case Character:
                return true;
            default:
                return false;
        }
    }

    public final boolean isFloating() {
        switch (this) {
            case Float:
            case Double:
                return true;
            default:
                return false;
        }
    }

    public final boolean isOther() {
        switch (this) {
            case Object:
            case Array:
            case TypeVariable:
            case Wildcard:
            case Void:
                return true;
            default:
                return false;
        }
    }
}
