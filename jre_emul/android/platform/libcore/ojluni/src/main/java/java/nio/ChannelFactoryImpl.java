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

package java.nio;

import java.io.FileDescriptor;
import java.nio.channels.FileChannel;
import sun.nio.ch.FileChannelImpl;

import static libcore.io.OsConstants.*;

public class ChannelFactoryImpl implements NioUtils.ChannelFactory {

  public FileChannel newFileChannel(Object ioObject, FileDescriptor fd, int mode) {
    boolean readable = (mode & O_ACCMODE) != O_WRONLY;
    boolean writable = (mode & O_ACCMODE) != O_RDONLY;
    boolean append = (mode & O_APPEND) != 0;

    return new FileChannelImpl(fd, null, readable, writable, append, ioObject);
  }
}
