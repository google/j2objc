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

package java.text;

import java.util.Locale;

/**
 * A concrete implementation class for {@code Collation} for iOS. Although
 * iOS uses ICU, its collation data is not available from any public APIs.
 * This class implements collation by invoking the NSString localized
 * comparison methods.
 *
 * @author Tom Ball
 */
public class IOSCollator extends Collator {

  private Object nsLocale;
  private int strength;
  private int decomposition;

  IOSCollator(Locale locale) {
    initNativeLocale(locale);
    strength = Collator.PRIMARY;
    decomposition = Collator.NO_DECOMPOSITION;
  }

  private native void initNativeLocale(Locale locale) /*-[
    self->nsLocale_ = [[NSLocale alloc] initWithLocaleIdentifier:[locale description]];
  ]-*/;

  @Override
  public native int compare(String string1, String string2) /*-[
    return (jint) [string1 compare:string2
                           options:NSLiteralSearch
                             range:NSMakeRange(0, [string1 length])
                            locale:self->nsLocale_];
  ]-*/;

  @Override
  public int getDecomposition() {
    return decomposition;
  }

  @Override
  public int getStrength() {
    return strength;
  }

  /**
   * Sets decomposition field, but is otherwise unused.
   */
  @Override
  public void setDecomposition(int value) {
    if (value < Collator.NO_DECOMPOSITION || value > Collator.FULL_DECOMPOSITION) {
      throw new IllegalArgumentException();
    }
    decomposition = value;
  }

  /**
   * Sets strength field, but is otherwise unused.
   */
  @Override
  public void setStrength(int value) {
    if (value < Collator.PRIMARY || value > Collator.IDENTICAL) {
      throw new IllegalArgumentException();
    }
    strength = value;
  }

  @Override
  public native int hashCode() /*-[
    return [(NSLocale *) self->nsLocale_ hash];
  ]-*/;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof IOSCollator)) {
      return false;
    }
    IOSCollator other = (IOSCollator) obj;
    return nativeLocalesEqual(nsLocale, other.nsLocale) && strength == other.strength &&
        decomposition == other.decomposition;
  }

  private static native boolean nativeLocalesEqual(Object locale1, Object locale2) /*-[
    return [(NSLocale *) locale1 isEqual:(NSLocale *) locale2];
  ]-*/;

  @Override
  public CollationKey getCollationKey(String string) {
    return new IOSCollationKey(string);
  }

  static class IOSCollationKey extends CollationKey {

    protected IOSCollationKey(String source) {
      super(source);
    }

    @Override
    public int compareTo(CollationKey value) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public native byte[] toByteArray() /*-[
      const char *utf = [[self getSourceString] UTF8String];
      // Include null terminator.
      return [IOSByteArray arrayWithBytes:(const jbyte *)utf count:(jint)strlen(utf) + 1];
    ]-*/;
  }
}
