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

package libcore.io;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Implements the actual DeleteOnExit mechanism. Is registered as a shutdown
 * hook in the Runtime, once it is actually being used.
 */
public class DeleteOnExit extends Thread {

    /**
     * Our singleton instance.
     */
    private static DeleteOnExit instance;

    /**
     * Returns our singleton instance, creating it if necessary.
     */
    public static synchronized DeleteOnExit getInstance() {
        if (instance == null) {
            instance = new DeleteOnExit();
            Runtime.getRuntime().addShutdownHook(instance);
        }

        return instance;
    }

    /**
     * Our list of files scheduled for deletion.
     */
    private final ArrayList<String> files = new ArrayList<String>();


    private DeleteOnExit() {
    }

    /**
     * Schedules a file for deletion.
     *
     * @param filename The file to delete.
     */
    public void addFile(String filename) {
        synchronized (files) {
            if (!files.contains(filename)) {
                files.add(filename);
            }
        }
    }

    /**
     * Does the actual work. Note we (a) first sort the files lexicographically
     * and then (b) delete them in reverse order. This is to make sure files
     * get deleted before their parent directories.
     */
    @Override
    public void run() {
        Collections.sort(files);
        for (int i = files.size() - 1; i >= 0; i--) {
            new File(files.get(i)).delete();
        }
    }
}
