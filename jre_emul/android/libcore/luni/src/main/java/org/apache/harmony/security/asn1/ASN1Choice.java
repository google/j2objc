/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
* @author Vladimir N. Molotkov, Stepan M. Mishura
* @version $Revision$
*/

package org.apache.harmony.security.asn1;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * This abstract class represents ASN.1 Choice type.
 *
 * To implement custom ASN.1 choice type an application class
 * must provide implementation for the following methods:
 *     getIndex()
 *     getObjectToEncode()
 *
 * There are two ways to implement custom ASN.1 choice type:
 * with application class that represents ASN.1 custom choice type or without.
 * The key point is how a value of choice type is stored by application classes.
 *
 * For example, let's consider the following ASN.1 notations
 * (see http://www.ietf.org/rfc/rfc3280.txt)
 *
 * Time ::= CHOICE {
 *       utcTime        UTCTime,
 *       generalTime    GeneralizedTime
 * }
 *
 * Validity ::= SEQUENCE {
 *       notBefore      Time,
 *       notAfter       Time
 *  }
 *
 * 1)First approach:
 * No application class to represent ASN.1 Time notation
 *
 * The Time notation is a choice of different time formats: UTC and Generalized.
 * Both of them are mapped to java.util.Date object, so an application
 * class that represents ASN.1 Validity notation may keep values
 * as Date objects.
 *
 * So a custom ASN.1 Time choice type should map its notation to Date object.
 *
 * class Time {
 *
 *     // custom ASN.1 choice class: maps Time to is notation
 *     public static final ASN1Choice asn1 = new ASN1Choice(new ASN1Type[] {
 *         ASN1GeneralizedTime.asn1, ASN1UTCTime.asn1 }) {
 *
 *         public int getIndex(java.lang.Object object) {
 *             return 0; // always encode as ASN1GeneralizedTime
 *         }
 *
 *         public Object getObjectToEncode(Object object) {
 *
 *             // A value to be encoded value is a Date object
 *             // pass it to custom time class
 *             return object;
 *         }
 *     };
 * }
 *
 * class Validity {
 *
 *     private Date notBefore;    // choice as Date
 *     private Date notAfter;     // choice as Date
 *
 *     ... // constructors and other methods go here
 *
 *     // custom ASN.1 sequence class: maps Validity class to is notation
 *     public static final ASN1Sequence ASN1
 *         = new ASN1Sequence(new ASN1Type[] {Time.asn1, Time.asn1 }) {
 *
 *         protected Object getObject(Object[] values) {
 *
 *             // ASN.1 Time choice passed Data object - use it
 *             return new Validity((Date) values[0], (Date) values[1]);
 *         }
 *
 *         protected void getValues(Object object, Object[] values) {
 *
 *             Validity validity = (Validity) object;
 *
 *             // pass Date objects to ASN.1 Time choice
 *             values[0] = validity.notBefore;
 *             values[1] = validity.notAfter;
 *         }
 *     }
 * }
 *
 * 2)Second approach:
 * There is an application class to represent ASN.1 Time notation
 *
 * If it is a matter what time format should be used to decode/encode
 * Date objects a class to represent ASN.1 Time notation must be created.
 *
 * For example,
 *
 * class Time {
 *
 *     private Date utcTime;
 *     private Date gTime;
 *
 *     ... // constructors and other methods go here
 *
 *     // custom ASN.1 choice class: maps Time to is notation
 *     public static final ASN1Choice asn1 = new ASN1Choice(new ASN1Type[] {
 *         ASN1GeneralizedTime.asn1, ASN1UTCTime.asn1 }) {
 *
 *         public Object getDecodedObject(BerInputStream in) {
 *
 *             // create Time object to pass as decoded value
 *             Time time = new Time();
 *
 *             if (in.choiceIndex==0) {
 *                 // we decoded GeneralizedTime
 *                 // store decoded Date value in corresponding field
 *                 time.gTime = in.content;
 *                 // return it
 *                 return time;
 *             } else {
 *                 // we decoded UTCTime
 *                 // store decoded Date value in corresponding field
 *                 time.utcTime = in.content;
 *                 // return it
 *                 return time;
 *             }
 *         }
 *
 *         public int getIndex(java.lang.Object object) {
 *             Time time = (Time)object;
 *             if(time.utcTime!=null){
 *                 // encode Date as UTCTime
 *                 return 1;
 *             } else {
 *                 // otherwise encode Date as GeneralizedTime
 *                 return 0;
 *             }
 *         }
 *
 *         public Object getObjectToEncode(Object object) {
 *             Time time = (Time)object;
 *             if(time.utcTime!=null){
 *                 // encode Date as UTCTime
 *                 return 1;
 *             } else {
 *                 // otherwise encode Date as GeneralizedTime
 *                 return 0;
 *             }
 *         }
 *     };
 * }
 *
 * So now Validity class must keep all values in Time object
 * and its custom ASN.1 sequence class must handle this class of objects
 *
 * class Validity {
 *
 *     private Time notBefore;    // now it is a Time!!!
 *     private Time notAfter;     // now it is a Time!!!
 *
 *     ... // constructors and other methods go here
 *
 *     // custom ASN.1 sequence class: maps Validity class to is notation
 *     public static final ASN1Sequence ASN1
 *         = new ASN1Sequence(new ASN1Type[] {Time.asn1, Time.asn1 }) {
 *
 *         protected Object getObject(Object[] values) {
 *
 *             // We've gotten Time objects here !!!
 *             return new Validity((Time) values[0], (Time) values[1]);
 *         }
 *
 *         protected void getValues(Object object, Object[] values) {
 *
 *             Validity validity = (Validity) object;
 *
 *             // pass Time objects to ASN.1 Time choice
 *             values[0] = validity.notBefore;
 *             values[1] = validity.notAfter;
 *         }
 *     }
 * }
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public abstract class ASN1Choice extends ASN1Type {
    public final ASN1Type[] type;

    /**
     * identifiers table: [2][number of distinct identifiers]
     * identifiers[0]: stores identifiers (includes nested choices)
     * identifiers[1]: stores identifiers' indexes in array of types
     */
    private final int[][] identifiers;

    /**
     * Constructs ASN.1 choice type.
     *
     * @param type -
     *            an array of one or more ASN.1 type alternatives.
     * @throws IllegalArgumentException -
     *             type parameter is invalid
     */
    public ASN1Choice(ASN1Type[] type) {
        super(TAG_CHOICE); // has not tag number

        if (type.length == 0) {
            throw new IllegalArgumentException("ASN.1 choice type MUST have at least one alternative: " + getClass().getName());
        }

        // create map of all identifiers
        TreeMap<BigInteger, BigInteger> map = new TreeMap<BigInteger, BigInteger>();
        for (int index = 0; index < type.length; index++) {
            ASN1Type t = type[index];

            if (t instanceof ASN1Any) {
                // ASN.1 ANY is not allowed,
                // even it is a single component (not good for nested choices)
                throw new IllegalArgumentException("ASN.1 choice type MUST have alternatives with distinct tags: " + getClass().getName()); // FIXME name
            } else if (t instanceof ASN1Choice) {

                // add all choice's identifiers
                int[][] choiceToAdd = ((ASN1Choice) t).identifiers;
                for (int j = 0; j < choiceToAdd[0].length; j++) {
                    addIdentifier(map, choiceToAdd[0][j], index);
                }
                continue;
            }

            // add primitive identifier
            if (t.checkTag(t.id)) {
                addIdentifier(map, t.id, index);
            }

            // add constructed identifier
            if (t.checkTag(t.constrId)) {
                addIdentifier(map, t.constrId, index);
            }
        }

        // fill identifiers array
        int size = map.size();
        identifiers = new int[2][size];
        Iterator<Map.Entry<BigInteger, BigInteger>> it = map.entrySet().iterator();

        for (int i = 0; i < size; i++) {
            Map.Entry<BigInteger, BigInteger> entry = it.next();
            BigInteger identifier = entry.getKey();

            identifiers[0][i] = identifier.intValue();
            identifiers[1][i] = entry.getValue().intValue();
        }

        this.type = type;
    }

    private void addIdentifier(TreeMap<BigInteger, BigInteger> map, int identifier, int index){
        if (map.put(BigInteger.valueOf(identifier), BigInteger.valueOf(index)) != null) {
            throw new IllegalArgumentException("ASN.1 choice type MUST have alternatives "
                    + "with distinct tags: " + getClass().getName());
        }
    }

    /**
     * Tests whether one of choice alternatives has the same identifier or not.
     *
     * @param identifier -
     *            ASN.1 identifier to be verified
     * @return - true if one of choice alternatives has the same identifier,
     *         otherwise false;
     */
    public final boolean checkTag(int identifier) {
        return Arrays.binarySearch(identifiers[0], identifier) >= 0;
    }

    public Object decode(BerInputStream in) throws IOException {
        int index = Arrays.binarySearch(identifiers[0], in.tag);
        if (index < 0) {
            throw new ASN1Exception("Failed to decode ASN.1 choice type.  No alternatives were found for " + getClass().getName());// FIXME message
        }

        index = identifiers[1][index];

        in.content = type[index].decode(in);

        // set index for getDecodedObject method
        in.choiceIndex = index;

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    public void encodeASN(BerOutputStream out) {
        encodeContent(out);
    }

    public final void encodeContent(BerOutputStream out) {
        out.encodeChoice(this);
    }

    public abstract int getIndex(Object object);

    public abstract Object getObjectToEncode(Object object);

    public final void setEncodingContent(BerOutputStream out) {
        out.getChoiceLength(this);
    }
}
