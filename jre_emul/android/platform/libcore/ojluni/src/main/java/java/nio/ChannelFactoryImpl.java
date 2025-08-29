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

import com.google.j2objc.annotations.ReflectionSupport;
import java.io.FileDescriptor;
import java.nio.channels.FileChannel;
import sun.nio.ch.FileChannelImpl;

import static com.google.j2objc.annotations.ReflectionSupport.Level.FULL;
import static libcore.io.OsConstants.*;

/**
 * J2ObjC split of NioUtils to move method to jre_channels subset library.
 * This class is only referenced by reflection.
 */
@SuppressWarnings("unused")
@ReflectionSupport(FULL)
class ChannelFactoryImpl implements NioUtils.ChannelFactory {

  public FileChannel newFileChannel(Object ioObject, FileDescriptor fd, int mode) {
    boolean readable = (mode & O_ACCMODE) != O_WRONLY;
    boolean writable = (mode & O_ACCMODE) != O_RDONLY;
    boolean append = (mode & O_APPEND) != 0;

    return new FileChannelImpl(fd, null, readable, writable, append, ioObject);
  }
}
