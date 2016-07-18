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

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Simple command-line test that takes a URL and returns the content,
 * response headers and status code.
 *
 * @author Tom Ball
 */
public class URLTest {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("usage: URLTest <url>");
    } else {
      try {
        URL obj = new URL(args[0]);
        URLConnection conn = obj.openConnection();

        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          if (in == null) {
            System.out.println("no InputStream");
          } else {
            System.out.println("getInputStream:");
            for (int i = 0; i < 3; i++) {
              System.out.println(in.readLine());
            }
          }
        } catch (FileNotFoundException e) {
          System.out.println(e);
        }

        InputStream errorStream = ((HttpURLConnection) conn).getErrorStream();
        if (errorStream == null) {
          System.out.println("no ErrorStream");
        } else {
          System.out.println("getErrorStream:");
          BufferedReader in = new BufferedReader(new InputStreamReader(errorStream));
          for (int i = 0; i < 3; i++) {
            System.out.println(in.readLine());
          }
        }
        
        System.out.println("getHeaderFields:");
        Map<String, List<String>> map = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
          System.out.println("Key : " + entry.getKey() + ", Value : " + entry.getValue());
        }
      
        System.out.println("getHeaderField:");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
          String key = entry.getKey();
          System.out.println("Key : " + key + ", Value : " + conn.getHeaderField(key));
        }
        
        System.out.println("getHeaderFieldKey:");
        for (int i = 0; i < map.size(); i++) {
          System.out.println(Integer.toString(i) + " Key : " + conn.getHeaderFieldKey(i));
        }
      } catch (Exception e) {
        System.err.println(e);
        e.printStackTrace();
      }
    }
  }
}
