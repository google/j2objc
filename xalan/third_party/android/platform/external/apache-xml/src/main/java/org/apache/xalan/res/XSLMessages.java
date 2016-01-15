/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: XSLMessages.java 468641 2006-10-28 06:54:42Z minchau $
 */
package org.apache.xalan.res;

import java.util.ListResourceBundle;

import org.apache.xpath.res.XPATHMessages;

/**
 * Sets things up for issuing error messages.  This class is misnamed, and
 * should be called XalanMessages, or some such.
 * @xsl.usage internal
 */
public class XSLMessages extends XPATHMessages
{

  /** The language specific resource object for Xalan messages.  */
  private static ListResourceBundle XSLTBundle = new XSLTErrorResources(); // android-changed

  /**
   * Creates a message from the specified key and replacement
   * arguments, localized to the given locale.
   *
   * @param msgKey    The key for the message text.
   * @param args      The arguments to be used as replacement text
   *                  in the message created.
   *
   * @return The formatted message string.
   */
  public static final String createMessage(String msgKey, Object args[])  //throws Exception
  {
      // BEGIN android-changed
      //     don't localize resources
      return createMsg(XSLTBundle, msgKey, args);
      // END android-changed
  }
  
  /**
   * Creates a message from the specified key and replacement
   * arguments, localized to the given locale.
   *
   * @param msgKey    The key for the message text.
   * @param args      The arguments to be used as replacement text
   *                  in the message created.
   *
   * @return The formatted warning string.
   */
  public static final String createWarning(String msgKey, Object args[])  //throws Exception
  {
      // BEGIN android-changed
      //     don't localize exception messages
      return createMsg(XSLTBundle, msgKey, args);
      // END android-changed
  }
}
