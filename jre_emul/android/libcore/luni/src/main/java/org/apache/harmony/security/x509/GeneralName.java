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
* @author Vladimir N. Molotkov, Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.x509;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.security.auth.x500.X500Principal;
import org.apache.harmony.security.asn1.ASN1Choice;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1StringType;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.ObjectIdentifier;
import org.apache.harmony.security.utils.Array;
import org.apache.harmony.security.x501.Name;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the GeneralName structure which is a part of X.509 certificate
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 *
 *   GeneralName::= CHOICE {
 *        otherName                       [0]     OtherName,
 *        rfc822Name                      [1]     IA5String,
 *        dNSName                         [2]     IA5String,
 *        x400Address                     [3]     ORAddress,
 *        directoryName                   [4]     Name,
 *        ediPartyName                    [5]     EDIPartyName,
 *        uniformResourceIdentifier       [6]     IA5String,
 *        iPAddress                       [7]     OCTET STRING,
 *        registeredID                    [8]     OBJECT IDENTIFIER
 *   }
 *
 *   OtherName::= SEQUENCE {
 *        type-id    OBJECT IDENTIFIER,
 *        value      [0] EXPLICIT ANY DEFINED BY type-id
 *   }
 *
 *   EDIPartyName::= SEQUENCE {
 *        nameAssigner            [0]     DirectoryString OPTIONAL,
 *        partyName               [1]     DirectoryString
 *   }
 *
 *   DirectoryString::= CHOICE {
 *        teletexString             TeletexString   (SIZE (1..MAX)),
 *        printableString           PrintableString (SIZE (1..MAX)),
 *        universalString           UniversalString (SIZE (1..MAX)),
 *        utf8String              UTF8String      (SIZE (1..MAX)),
 *        bmpString               BMPString       (SIZE (1..MAX))
 *   }
 *
 * </pre>
 *
 * <p>This class doesn't support masked addresses like "10.9.8.0/255.255.255.0".
 * These are only necessary for NameConstraints, which are not exposed in the
 * Java certificate API.
 *
 * @see org.apache.harmony.security.x509.NameConstraints
 * @see org.apache.harmony.security.x509.GeneralSubtree
 */
public final class GeneralName {

    /**
     * The values of the tags of fields
     */
    public static final int OTHER_NAME = 0;
    public static final int RFC822_NAME = 1;
    public static final int DNS_NAME = 2;
    public static final int X400_ADDR = 3;
    public static final int DIR_NAME = 4;
    public static final int EDIP_NAME = 5;
    public static final int UR_ID = 6;
    public static final int IP_ADDR = 7;
    public static final int REG_ID = 8;

    // ASN1 encoders/decoders for name choices
    private static ASN1Type[] nameASN1 = new ASN1Type[9];

    static {
        nameASN1[OTHER_NAME] = OtherName.ASN1;
        nameASN1[RFC822_NAME] = ASN1StringType.IA5STRING;
        nameASN1[DNS_NAME] = ASN1StringType.IA5STRING;
        nameASN1[UR_ID] = ASN1StringType.IA5STRING;
        nameASN1[X400_ADDR] = ORAddress.ASN1;
        nameASN1[DIR_NAME] = Name.ASN1;
        nameASN1[EDIP_NAME] = EDIPartyName.ASN1;
        nameASN1[IP_ADDR] = ASN1OctetString.getInstance();
        nameASN1[REG_ID] = ASN1Oid.getInstance();
    }

    /** the tag of the name type */
    private int tag;
    /** the name value (can be String or byte array) */
    private Object name;
    /** the ASN.1 encoded form of GeneralName */
    private byte[] encoding;
    /** the ASN.1 encoded form of GeneralName's field */
    private byte[] name_encoding;

    /**
     * Makes the GeneralName object from the tag type and corresponding
     * well established string representation of the name value.
     * The String representation of [7] iPAddress is such as:
     *  For IP v4, as specified in RFC 791, the address must
     *  contain exactly 4 byte component.  For IP v6, as specified in
     *  RFC 1883, the address must contain exactly 16 byte component.
     *  If GeneralName structure is used as a part of Name Constraints
     *  extension, to represent an address range the number of address
     *  component is doubled (to 8 and 32 bytes respectively).
     * Note that the names:
     * [0] otherName, [3] x400Address, [5] ediPartyName
     *   have no the string representation, so exception will be thrown.
     * To make the GeneralName object with such names use another constructor.
     * @param tag is an integer which value corresponds to the name type.
     * @param name is a name value corresponding to the tag.
     */
    public GeneralName(int tag, String name) throws IOException {
        if (name == null) {
            throw new IOException("name == null");
        }
        this.tag = tag;
        switch (tag) {
            case OTHER_NAME :
            case X400_ADDR :
            case EDIP_NAME :
                throw new IOException("Unknown string representation for type [" + tag + "]");
            case DNS_NAME :
                // according to RFC 3280 p.34 the DNS name should be
                // checked against the
                // RFC 1034 p.10 (3.5. Preferred name syntax):
                checkDNS(name);
                this.name = name;
                break;
            case UR_ID :
                // check the uniformResourceIdentifier for correctness
                // according to RFC 3280 p.34
                checkURI(name);
                this.name = name;
                break;
            case RFC822_NAME :
                this.name = name;
                break;
            case REG_ID:
                this.name = oidStrToInts(name);
                break;
            case DIR_NAME :
                this.name = new Name(name);
                break;
            case IP_ADDR :
                this.name = ipStrToBytes(name);
                break;
            default:
                throw new IOException("Unknown type: [" + tag + "]");
        }
    }

    public GeneralName(OtherName name) {
        this.tag = OTHER_NAME;
        this.name = name;
    }

    public GeneralName(ORAddress name) {
        this.tag = X400_ADDR;
        this.name = name;
    }

    public GeneralName(Name name) {
        this.tag = DIR_NAME;
        this.name = name;
    }

    public GeneralName(EDIPartyName name) {
        this.tag = EDIP_NAME;
        this.name = name;
    }
    /**
     * Constructor for type [7] iPAddress.
     * name is an array of bytes such as:
     *  For IP v4, as specified in RFC 791, the address must
     *  contain exactly 4 byte component.  For IP v6, as specified in
     *  RFC 1883, the address must contain exactly 16 byte component.
     *  If GeneralName structure is used as a part of Name Constraints
     *  extension, to represent an address range the number of address
     *  component is doubled (to 8 and 32 bytes respectively).
     */
    public GeneralName(byte[] name) throws IllegalArgumentException {
        this.tag = IP_ADDR;
        this.name = new byte[name.length];
        System.arraycopy(name, 0, this.name, 0, name.length);
    }

    /**
     * Constructs an object representing the value of GeneralName.
     * @param tag is an integer which value corresponds
     * to the name type (0-8),
     * @param name is a DER encoded for of the name value
     */
    public GeneralName(int tag, byte[] name) throws IOException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if ((tag < 0) || (tag > 8)) {
            throw new IOException("GeneralName: unknown tag: " + tag);
        }
        this.tag = tag;
        this.name_encoding = new byte[name.length];
        System.arraycopy(name, 0, this.name_encoding, 0, name.length);
        this.name = nameASN1[tag].decode(this.name_encoding);
    }

    /**
     * Returns the tag of the name in the structure
     */
    public int getTag() {
        return tag;
    }

    /**
     * @return the value of the name.
     * The class of name object depends on the tag as follows:
     * [0] otherName - OtherName object,
     * [1] rfc822Name - String object,
     * [2] dNSName - String object,
     * [3] x400Address - ORAddress object,
     * [4] directoryName - instance of Name object,
     * [5] ediPartyName - EDIPartyName object,
     * [6] uniformResourceIdentifier - String object,
     * [7] iPAddress - array of bytes such as:
     *  For IP v4, as specified in RFC 791, the address must
     *  contain exactly 4 byte component.  For IP v6, as specified in
     *  RFC 1883, the address must contain exactly 16 byte component.
     *  If GeneralName structure is used as a part of Name Constraints
     *  extension, to represent an address range the number of address
     *  component is doubled (to 8 and 32 bytes respectively).
     * [8] registeredID - String.
     */
    public Object getName() {
        return name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof GeneralName)) {
            return false;
        }
        GeneralName gname = (GeneralName) other;
        if (this.tag != gname.tag) {
            return false;
        }
        switch(tag) {
            case RFC822_NAME:
            case DNS_NAME:
            case UR_ID:
                return ((String) name).equalsIgnoreCase(
                        (String) gname.getName());
            case REG_ID:
                return Arrays.equals((int[]) name, (int[]) gname.name);
            case IP_ADDR:
                // iPAddress [7], check by using ranges.
                return Arrays.equals((byte[]) name, (byte[]) gname.name);
            case DIR_NAME:
            case X400_ADDR:
            case OTHER_NAME:
            case EDIP_NAME:
                return Arrays.equals(getEncoded(), gname.getEncoded());
            default:
                // should never happen
        }
        return false;
    }

    public int hashCode() {
        switch (tag) {
        case RFC822_NAME:
        case DNS_NAME:
        case UR_ID:
        case REG_ID:
        case IP_ADDR:
            return name.hashCode();
        case DIR_NAME:
        case X400_ADDR:
        case OTHER_NAME:
        case EDIP_NAME:
            return Arrays.hashCode(getEncoded());
        default:
            return super.hashCode();
        }
    }

    /**
     * Checks if the other general name is acceptable by this object.
     * The name is acceptable if it has the same type name and its
     * name value is equal to name value of this object. Also the name
     * is acceptable if this general name object is a part of name
     * constraints and the specified name is satisfied the restriction
     * provided by this object (for more detail see section 4.2.1.11
     * of rfc 3280).
     * Note that for X400Address [3] check procedure is unclear so method
     * just checks the equality of encoded forms.
     * For otherName [0], ediPartyName [5], and registeredID [8]
     * the check procedure if not defined by rfc 3280 and for names of
     * these types this method also checks only for equality of encoded forms.
     */
    public boolean isAcceptable(GeneralName gname) {
        if (this.tag != gname.getTag()) {
            return false;
        }
        switch (this.tag) {
            case RFC822_NAME:
                // Mail address [1]:
                // a@b.c - particular address is acceptable by the same address,
                // or by b.c - host name.
                return ((String) gname.getName()).toLowerCase(Locale.US)
                    .endsWith(((String) name).toLowerCase(Locale.US));
            case DNS_NAME:
                // DNS name [2] that can be constructed by simply adding
                // to the left hand side of the name satisfies the name
                // constraint: aaa.aa.aa satisfies to aaa.aa.aa, aa.aa, ..
                String dns = (String) name;
                String _dns = (String) gname.getName();
                if (dns.equalsIgnoreCase(_dns)) {
                    return true;
                } else {
                    return _dns.toLowerCase(Locale.US).endsWith("." + dns.toLowerCase(Locale.US));
                }
            case UR_ID:
                // For URIs the constraint ".xyz.com" is satisfied by both
                // abc.xyz.com and abc.def.xyz.com.  However, the constraint
                // ".xyz.com" is not satisfied by "xyz.com".
                // When the constraint does not begin with a period, it
                // specifies a host.
                // Extract the host from URI:
                String uri = (String) name;
                int begin = uri.indexOf("://")+3;
                int end = uri.indexOf('/', begin);
                String host = (end == -1)
                                ? uri.substring(begin)
                                : uri.substring(begin, end);
                uri = (String) gname.getName();
                begin = uri.indexOf("://")+3;
                end = uri.indexOf('/', begin);
                String _host = (end == -1)
                                ? uri.substring(begin)
                                : uri.substring(begin, end);
                if (host.startsWith(".")) {
                    return _host.toLowerCase(Locale.US).endsWith(host.toLowerCase(Locale.US));
                } else {
                    return host.equalsIgnoreCase(_host);
                }
            case IP_ADDR:
                // iPAddress [7], check by using ranges.
                byte[] address = (byte[]) name;
                byte[] _address = (byte[]) gname.getName();
                int length = address.length;

                /*
                 * For IP v4, as specified in RFC 791, the address must contain
                 * exactly 4 byte component. For IP v6, as specified in RFC
                 * 1883, the address must contain exactly 16 byte component. If
                 * GeneralName structure is used as a part of Name Constraints
                 * extension, to represent an address range the number of
                 * address component is doubled (to 8 and 32 bytes
                 * respectively).
                 */
                if (length != 4 && length != 8 && length != 16 && length != 32) {
                    return false;
                }

                int _length = _address.length;
                if (length == _length) {
                    return Arrays.equals(address, _address);
                } else if (length == 2*_length) {
                    for (int i = 0; i < _address.length; i++) {
                        // TODO: should the 2nd IP address be treated as a range or as a mask?
                        int octet = _address[i] & 0xff;
                        int min = address[i] & 0xff;
                        int max = address[i + _length] & 0xff;
                        if ((octet < min) || (octet > max)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            case DIR_NAME:
                // FIXME: false:
                // directoryName according to 4.1.2.4
                // comparing the encoded forms of the names
                //TODO:
                //Legacy implementations exist where an RFC 822 name
                //is embedded in the subject distinguished name in an
                //attribute of type EmailAddress
            case X400_ADDR:
            case OTHER_NAME:
            case EDIP_NAME:
            case REG_ID:
                return Arrays.equals(getEncoded(), gname.getEncoded());
            default:
                // should never happen
        }
        return true;
    }

    /**
     * Gets a list representation of this GeneralName object.
     * The first entry of the list is an Integer object representing
     * the type of mane (0-8), and the second entry is a value of the name:
     * string or ASN.1 DER encoded form depending on the type as follows:
     * rfc822Name, dNSName, uniformResourceIdentifier names are returned
     * as Strings, using the string formats for those types (rfc 3280)
     * IP v4 address names are returned using dotted quad notation.
     * IP v6 address names are returned in the form "p1:p2:...:p8",
     * where p1-p8 are hexadecimal values representing the eight 16-bit
     * pieces of the address. registeredID name are returned as Strings
     * represented as a series of nonnegative integers separated by periods.
     * And directory names (distinguished names) are returned in
     * RFC 2253 string format.
     * otherName, X400Address, ediPartyName returned as byte arrays
     * containing the ASN.1 DER encoded form of the name.
     */
    public List<Object> getAsList() {
        ArrayList<Object> result = new ArrayList<Object>();
        result.add(tag);
        switch (tag) {
            case OTHER_NAME:
                result.add(((OtherName) name).getEncoded());
                break;
            case RFC822_NAME:
            case DNS_NAME:
            case UR_ID:
                result.add(name); // String
                break;
            case REG_ID:
                result.add(ObjectIdentifier.toString((int[]) name));
                break;
            case X400_ADDR:
                result.add(((ORAddress) name).getEncoded());
                break;
            case DIR_NAME: // directoryName is returned as a String
                result.add(((Name) name).getName(X500Principal.RFC2253));
                break;
            case EDIP_NAME:
                result.add(((EDIPartyName) name).getEncoded());
                break;
            case IP_ADDR: //iPAddress is returned as a String, not as a byte array
                result.add(ipBytesToStr((byte[]) name));
                break;
            default:
                // should never happen
        }
        return Collections.unmodifiableList(result);
    }

    public String toString() {
        String result = "";
        switch (tag) {
            case OTHER_NAME:
                result = "otherName[0]: "
                         + Array.getBytesAsString(getEncoded());
                break;
            case RFC822_NAME:
                result = "rfc822Name[1]: " + name;
                break;
            case DNS_NAME:
                result = "dNSName[2]: " + name;
                break;
            case UR_ID:
                result = "uniformResourceIdentifier[6]: " + name;
                break;
            case REG_ID:
                result = "registeredID[8]: " + ObjectIdentifier.toString((int[]) name);
                break;
            case X400_ADDR:
                result = "x400Address[3]: "
                         + Array.getBytesAsString(getEncoded());
                break;
            case DIR_NAME:
                result = "directoryName[4]: "
                         + ((Name) name).getName(X500Principal.RFC2253);
                break;
            case EDIP_NAME:
                result = "ediPartyName[5]: "
                         + Array.getBytesAsString(getEncoded());
                break;
            case IP_ADDR:
                result = "iPAddress[7]: " + ipBytesToStr((byte[]) name);
                break;
            default:
                // should never happen
        }
        return result;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 GeneralName value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    /**
     * @return the encoded value of the name without the tag associated
     *         with the name in the GeneralName structure
     * @throws  IOException
     */
    public byte[] getEncodedName() {
        if (name_encoding == null) {
            name_encoding = nameASN1[tag].encode(name);
        }
        return name_encoding;
    }

    /**
     * Checks the correctness of the string representation of DNS name as
     * specified in RFC 1034 p. 10 and RFC 1123 section 2.1.
     *
     * <p>This permits a wildcard character '*' anywhere in the name; it is up
     * to the application to check which wildcards are permitted. See RFC 6125
     * for recommended wildcard matching rules.
     */
    public static void checkDNS(String dns) throws IOException {
        String string = dns.toLowerCase(Locale.US);
        int length = string.length();
        // indicates if it is a first letter of the label
        boolean first_letter = true;
        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);
            if (first_letter) {
                if ((ch > 'z' || ch < 'a') && (ch < '0' || ch > '9') && (ch != '*')) {
                    throw new IOException("DNS name must start with a letter: " + dns);
                }
                first_letter = false;
                continue;
            }
            if (!((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
                    || (ch == '-') || (ch == '.') || (ch == '*'))) {
                throw new IOException("Incorrect DNS name: " + dns);
            }
            if (ch == '.') {
                // check the end of the previous label, it should not
                // be '-' sign
                if (string.charAt(i-1) == '-') {
                    throw new IOException("Incorrect DNS name: label ends with '-': " + dns);
                }
                first_letter = true;
            }
        }
    }

    /**
     * Checks the correctness of the string representation of URI name.
     * The correctness is checked as pointed out in RFC 3280 p. 34.
     */
    public static void checkURI(String uri) throws IOException {
        try {
            URI ur = new URI(uri);
            if (ur.getScheme() == null || ur.getRawSchemeSpecificPart().isEmpty()) {
                throw new IOException("No scheme or scheme-specific-part in uniformResourceIdentifier: " + uri);
            }
            if (!ur.isAbsolute()) {
                throw new IOException("Relative uniformResourceIdentifier: " + uri);
            }
        } catch (URISyntaxException e) {
            throw (IOException) new IOException("Bad representation of uniformResourceIdentifier: " + uri).initCause(e);

        }
    }

    /**
     * Converts OID into array of ints.
     */
    public static int[] oidStrToInts(String oid) throws IOException {
        int length = oid.length();
        if (oid.charAt(length-1) == '.') {
            throw new IOException("Bad OID: " + oid);
        }
        int[] result = new int[length/2+1]; // best case: a.b.c.d.e
        int number = 0; // the number of OID's components
        for (int i = 0; i < length; i++) {
            int value = 0;
            int pos = i;
            for (; i < length; i++) {
                char ch = oid.charAt(i);
                if ((ch < '0') || (ch > '9')) {
                    break;
                }
                value = 10 * value + (ch - '0');
            }
            if (i == pos) {
                // the number was not read
                throw new IOException("Bad OID: " + oid);
            }
            result[number++] = value;
            if (i == length) {
                break;
            }
            char ch = oid.charAt(i);
            if (ch != '.') {
                throw new IOException("Bad OID: " + oid);
            }
        }
        if (number < 2) {
            throw new IOException("OID should consist of no less than 2 components: " + oid);
        }
        return Arrays.copyOfRange(result, 0, number);
    }

    /**
     * Returns the bytes of the given IP address or masked IP address.
     */
    public static byte[] ipStrToBytes(String ip) throws IOException {
        if (!InetAddress.isNumeric(ip)) {
            throw new IOException("Not an IP address: " + ip);
        }
        return InetAddress.getByName(ip).getAddress();
    }

    /**
     * Returns the string form of the given IP address. If the address is not 4
     * octets for IPv4 or 16 octets for IPv6, an IllegalArgumentException will
     * be thrown.
     */
    public static String ipBytesToStr(byte[] ip) {
        try {
            return InetAddress.getByAddress(null, ip).getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unexpected IP address: " + Arrays.toString(ip));
        }
    }

    /**
     * The "Name" is actually a CHOICE of one entry, so we need to pretend it's
     * a SEQUENCE OF and just grab the first entry.
     */
    private static final ASN1SequenceOf NAME_ASN1 = new ASN1SequenceOf(Name.ASN1) {
        @Override
        public Object decode(BerInputStream in) throws IOException {
            return ((List<?>) super.decode(in)).get(0);
        }
    };

    public static final ASN1Choice ASN1 = new ASN1Choice(new ASN1Type[] {
           new ASN1Implicit(0, OtherName.ASN1),
           new ASN1Implicit(1, ASN1StringType.IA5STRING),
           new ASN1Implicit(2, ASN1StringType.IA5STRING),
           new ASN1Implicit(3, ORAddress.ASN1),
           new ASN1Implicit(4, NAME_ASN1),
           new ASN1Implicit(5, EDIPartyName.ASN1),
           new ASN1Implicit(6, ASN1StringType.IA5STRING),
           new ASN1Implicit(7, ASN1OctetString.getInstance()),
           new ASN1Implicit(8, ASN1Oid.getInstance()) }) {

        public Object getObjectToEncode(Object value) {
            return ((GeneralName) value).name;
        }

        public int getIndex(java.lang.Object object) {
            return  ((GeneralName) object).tag;
        }

        @Override public Object getDecodedObject(BerInputStream in) throws IOException {
            GeneralName result;
            switch (in.choiceIndex) {
                case OTHER_NAME: // OtherName
                    result = new GeneralName((OtherName) in.content);
                    break;
                case RFC822_NAME: // rfc822Name
                case DNS_NAME: // dNSName
                    result = new GeneralName(in.choiceIndex, (String) in.content);
                    break;
                case X400_ADDR:
                    result = new GeneralName((ORAddress) in.content);
                    break;
                case DIR_NAME: // directoryName (X.500 Name)
                    result = new GeneralName((Name) in.content);
                    break;
                case EDIP_NAME: // ediPartyName
                    result = new GeneralName((EDIPartyName) in.content);
                    break;
                case UR_ID: // uniformResourceIdentifier
                    String uri = (String) in.content;
                    if (uri.indexOf(":") == -1) {
                        throw new IOException("GeneralName: scheme is missing in URI: " + uri);
                    }
                    result = new GeneralName(in.choiceIndex, uri);
                    break;
                case IP_ADDR: // iPAddress
                    result = new GeneralName((byte[]) in.content);
                    break;
                case REG_ID: // registeredID
                    result = new GeneralName(in.choiceIndex,
                            ObjectIdentifier.toString((int[]) in.content));
                    break;
                default:
                    throw new IOException("GeneralName: unknown tag: " + in.choiceIndex);
            }
            result.encoding = in.getEncoded();
            return result;
        }
    };
}
