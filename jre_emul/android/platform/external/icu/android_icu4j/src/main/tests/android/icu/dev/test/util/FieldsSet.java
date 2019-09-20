/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import android.icu.impl.Utility;

/**
 * @author srl
 * 
 * analog of FieldsSet in C++
 */
public class FieldsSet {
    public static final int NO_ENUM = -1;

    protected FieldsSet(int whichEnum, int fieldsCount) {
        if (fieldsCount <= 0 && whichEnum != NO_ENUM) {
            fieldsCount = DebugUtilities.enumCount(whichEnum);
        }
        fEnum = whichEnum;
        fFieldsCount = fieldsCount;
        if(fieldsCount<0) {
            throw new InternalError("Preposterous field count " + fieldsCount);
        }
        fValues = new int[fFieldsCount];
        fIsSet = new boolean[fFieldsCount];
        clear();
    }

    protected int fEnum = NO_ENUM;

    protected int fFieldsCount = 0;

    protected int fValues[] = null;

    protected boolean fIsSet[] = null;

    public void clear() {
        for (int i = 0; i < fFieldsCount; i++) {
            clear(i);
        }
    }

    public void clear(int field) {
        fValues[field] = -1;
        fIsSet[field] = false;
    }

    public void set(int field, int amount) {
        fValues[field] = amount;
        fIsSet[field] = true;
    }

    public boolean isSet(int field) {
        return fIsSet[field];
    }

    public int get(int field) {
        if (fIsSet[field]) {
            return fValues[field];
        } else {
            return -1;
        }
    }

    public boolean isSameType(FieldsSet other) {
        return ((other.fEnum == fEnum) && (other.fFieldsCount == fFieldsCount));
    }

    public int fieldCount() {
        return fFieldsCount;
    }

    /**
     * @param other  "expected" set to match against
     * @return a formatted string listing which fields are set in this, with the
     *         comparison made agaainst those fields in other, or, 'null' if there is no difference.
     */
    public String diffFrom(FieldsSet other) {
        StringBuffer str = new StringBuffer();
        if(!isSameType(other)) {
            throw new IllegalArgumentException("U_ILLEGAL_ARGUMENT_ERROR: FieldsSet of a different type!");
        }
        for (int i=0; i<fieldCount(); i++) {
            if (isSet(i)) {
                int myVal = get(i);
                int theirVal = other.get(i);
                
                if(fEnum != NO_ENUM) {
                    String fieldName = DebugUtilities.enumString(fEnum, i);
                    
                    String aval = Integer.toString(myVal);
                    String bval = Integer.toString(theirVal);

                    str.append(fieldName +"="+aval+" not "+bval+", ");
                } else {
                    str.append(Integer.toString(i) + "=" + myVal+" not " + theirVal+", ");
                }
            }
        }
        if(str.length()==0) {
            return null;
        }
        return str.toString();
    }

    /**
     * @param str string to parse
     * @param status formatted string for status
     */
    public int parseFrom(String str) {
        return parseFrom(str, null);
    }

    public int parseFrom(String str, FieldsSet inheritFrom) {
        int goodFields = 0;
        
        String[] fields = Utility.split(str, ',');
        for(int i=0;i<fields.length;i++) {
            String fieldStr = fields[i];
            String kv[] = Utility.split(fieldStr, '=');
            if(kv.length < 1 || kv.length > 2) {
                throw new InternalError("split around '=' failed: " + fieldStr);
            }
            String key = kv[0];
            String value = "";
            if(kv.length>1) {
                value = kv[1];
            }
            
            int field = handleParseName(inheritFrom, key, value);
            if(field != -1) {
                handleParseValue(inheritFrom, field, value);
                goodFields++;
            }
        }

        return goodFields;
    }

    /**
     * Callback interface for subclass. This function is called when parsing a
     * field name, such as "MONTH" in "MONTH=4". Base implementation is to
     * lookup the enum value using udbg_* utilities, or else as an integer if
     * enum is not available.
     * 
     * If there is a special directive, the implementer can catch it here and
     * return -1 after special processing completes.
     * 
     * @param inheritFrom  the set inheriting from - may be null.
     * @param name  the field name (key side)
     * @param substr  the string in question (value side)
     * @param status  error status - set to error for failure.
     * @return field number, or negative if field should be skipped.
     */
    protected int handleParseName(FieldsSet inheritFrom, String name,
            String substr) {
        int field = -1;
        if(fEnum != NO_ENUM) {
            field = DebugUtilities.enumByString(fEnum, name);
        }
        if(field < 0) {
            field = Integer.parseInt(name);
        }
        return field;
    }

    /**
     * Callback interface for subclass. Base implementation is to call
     * parseValueDefault(...)
     * 
     * @param inheritFrom  the set inheriting from - may be null.
     * @param field   which field is being parsed
     * @param substr  the string in question (value side)
     * @param status  error status - set to error for failure.
     * @see parseValueDefault
     */
    protected void handleParseValue(FieldsSet inheritFrom, int field,
            String substr) {
        parseValueDefault(inheritFrom, field, substr);
    }

    /**
     * the default implementation for handleParseValue. Base implementation is
     * to parse a decimal integer value, or inherit from inheritFrom if the
     * string is 0-length. Implementations of this function should call
     * set(field,...) on successful parse.
     * 
     * @see handleParseValue
     */
    protected void parseValueDefault(FieldsSet inheritFrom, int field,
            String substr) {
        if(substr.length()==0) {
            if(inheritFrom == null) {
                throw new InternalError("Trying to inherit from field " + field + " but inheritFrom is null");
            }
            if(!inheritFrom.isSet(field)) {
                throw new InternalError("Trying to inherit from field " + field + " but inheritFrom["+field+"] is  not set");
            }
            set(field,inheritFrom.get(field));
        } else {
            int value = Integer.parseInt(substr);
            set(field, value);
        }
    }

    /**
     * convenience implementation for handleParseValue attempt to load a value
     * from an enum value using udbg_enumByString() if fails, will call
     * parseValueDefault()
     * 
     * @see handleParseValue
     */
    protected void parseValueEnum(int type, FieldsSet inheritFrom, int field,
            String substr) {
        int value = DebugUtilities.enumByString(type, substr);
        if(value>=0) {
            set(field,value);
            return;
        }
        parseValueDefault(inheritFrom, field, substr);
    }
    
    public String fieldName(int field) {
        return (fEnum!=NO_ENUM)?DebugUtilities.enumString(fEnum, field):Integer.toString(field);
    }
    
    public String toString() {
        String str = getClass().getName()+" ["+fFieldsCount+","
        +(fEnum!=NO_ENUM?DebugUtilities.typeString(fEnum):Integer.toString(fEnum))+"]: ";
        for(int i=0;i<fFieldsCount;i++) {
            if(isSet(i)) {
                str = str + fieldName(i)+"="+get(i)+",";
            }
        }
        return str; 
    }
}
