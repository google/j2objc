/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio.charset;

/**
 * Used to indicate what kind of actions to take in case of encoding/decoding
 * errors. Currently three actions are defined: {@code IGNORE}, {@code REPLACE}
 * and {@code REPORT}.
 */
public class CodingErrorAction {

    /**
     * Denotes the action to ignore any errors.
     */
    public static final CodingErrorAction IGNORE = new CodingErrorAction(
            "IGNORE");

    /**
     * Denotes the action to fill in the output with a replacement character
     * when malformed input or an unmappable character is encountered.
     */
    public static final CodingErrorAction REPLACE = new CodingErrorAction(
            "REPLACE");

    /**
     * Denotes the action to report the encountered error in an appropriate
     * manner, for example to throw an exception or return an informative
     * result.
     */
    public static final CodingErrorAction REPORT = new CodingErrorAction(
            "REPORT");

    // The name of this action
    private String action;

    /*
     * Can't instantiate outside.
     */
    private CodingErrorAction(String action) {
        this.action = action;
    }

    /**
     * Returns a text description of this action indication.
     *
     * @return a text description of this action indication.
     */
    @Override
    public String toString() {
        return "Action: " + this.action;
    }
}
