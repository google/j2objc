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
 * $Id: XPATHMessages.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath.res;

import java.util.ListResourceBundle;

import org.apache.xml.res.XMLMessages;

/**
 * A utility class for issuing XPath error messages.
 * @xsl.usage internal
 */
public class XPATHMessages extends XMLMessages
{
  /** The language specific resource object for XPath messages.  */
  private static ListResourceBundle XPATHBundle = new XPATHErrorResources();

  /** The class name of the XPath error message string table.     */
  private static final String XPATH_ERROR_RESOURCES =
    "org.apache.xpath.res.XPATHErrorResources";
  
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
  public static final String createXPATHMessage(String msgKey, Object args[])  //throws Exception 
  {
      // BEGIN android-changed
      //     don't localize exception messages
      return createXPATHMsg(XPATHBundle, msgKey, args);
      // END android-changed
  }

  /**
   * Creates a message from the specified key and replacement
   * arguments, localized to the given locale.
   *
   * @param msgKey The key for the message text.
   * @param args      The arguments to be used as replacement text
   *                  in the message created.
   *
   * @return The formatted warning string.
   */
  public static final String createXPATHWarning(String msgKey, Object args[])  //throws Exception
  {
      // BEGIN android-changed
      //     don't localize exception messages
      return createXPATHMsg(XPATHBundle, msgKey, args);
      // END android-changed
  }

  /**
   * Creates a message from the specified key and replacement
   * arguments, localized to the given locale.
   *
   * @param fResourceBundle The resource bundle to use.
   * @param msgKey  The message key to use.
   * @param args      The arguments to be used as replacement text
   *                  in the message created.
   *
   * @return The formatted message string.
   */
  public static final String createXPATHMsg(ListResourceBundle fResourceBundle,
                                            String msgKey, Object args[])  //throws Exception
  {

    String fmsg = null;
    boolean throwex = false;
    String msg = null;

    if (msgKey != null)
      msg = fResourceBundle.getString(msgKey); 

    if (msg == null)
    {
      msg = fResourceBundle.getString(XPATHErrorResources.BAD_CODE);
      throwex = true;
    }

    if (args != null)
    {
      try
      {

        // Do this to keep format from crying.
        // This is better than making a bunch of conditional
        // code all over the place.
        int n = args.length;

        for (int i = 0; i < n; i++)
        {
          if (null == args[i])
            args[i] = "";
        }

        fmsg = java.text.MessageFormat.format(msg, args);
      }
      catch (Exception e)
      {
        fmsg = fResourceBundle.getString(XPATHErrorResources.FORMAT_FAILED);
        fmsg += " " + msg;
      }
    }
    else
      fmsg = msg;

    if (throwex)
    {
      throw new RuntimeException(fmsg);
    }

    return fmsg;
  }

}
