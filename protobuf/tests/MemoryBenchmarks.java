/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.j2objc.annotations.AutoreleasePool;
import com.google.protobuf.GeneratedMessage;

import protos.MessageWithExtensions;
import protos.NoFields;
import protos.SingleInt;
import protos.SingleLong;
import protos.SingleMessage;
import protos.SingleRepeatedInt;
import protos.SingleRepeatedLong;
import protos.SingleRepeatedMessage;
import protos.SizeTest;
import protos.TypicalData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/*-[
#include "my_malloc.h"
]-*/

/**
 * Tests the memory usage of protocol buffers.
 *
 * @author Keith Stanger
 */
class MemoryBenchmarks {

  // Used to maintain a strong reference to a protocol buffer and avoid
  // deallocation by the autorelease pool on "testMemUsageInner".
  private static Object protoReference;

  public static void main(String[] args) {
    System.out.println("Running memory usage tests...");
    memUsageEmptyProto(NoFields.class);
    memUsageEmptyProto(MessageWithExtensions.class);
    memUsageEmptyProto(SingleInt.class);
    memUsageEmptyProto(SingleLong.class);
    memUsageEmptyProto(SingleRepeatedInt.class);
    memUsageEmptyProto(SingleRepeatedLong.class);
    memUsageEmptyProto(SingleMessage.class);
    memUsageEmptyProto(SingleRepeatedMessage.class);
    memUsageEmptyProto(TypicalData.class);
    memUsageRepeatedInts(1);
    memUsageRepeatedInts(2);
    memUsageRepeatedInts(10);
    memUsageRepeatedInts(50);
    memUsageSingleInnerMessage();
    memUsageRepeatedMessages(1);
    memUsageRepeatedMessages(2);
    memUsageRepeatedMessages(10);
    memUsageIntExtension();
    memUsageRepeatedIntExtension(1);
    memUsageRepeatedIntExtension(2);
    memUsageRepeatedIntExtension(10);
  }

  private static void memUsageEmptyProto(final Class<? extends GeneratedMessage> protoClass) {
    System.out.println("*** memUsageEmptyProto - " + protoClass.getName() + " ***");
    testMemUsage(new Runnable() {
      public void run() {
        try {
          protoReference = newBuilder(protoClass);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public native Object newBuilder(Class<? extends GeneratedMessage> protoClass) /*-[
        return [protoClass.objcClass newBuilder];
      ]-*/;
    });
  }

  private static void memUsageRepeatedInts(final int numInts) {
    System.out.println("*** memUsageRepeatedInts - " + numInts + " ***");
    testMemUsage(new Runnable() {
      public void run() {
        SingleRepeatedInt.Builder builder = SingleRepeatedInt.newBuilder();
        for (int i = 0; i < numInts; i++) {
          builder.addInt(123);
        }
        protoReference = builder.build();
      }
    });
  }

  private static void memUsageSingleInnerMessage() {
    System.out.println("*** memUsageSingleInnerMessage ***");
    testMemUsage(new Runnable() {
      public void run() {
        protoReference = SingleMessage.newBuilder().setMsg(NoFields.newBuilder().build()).build();
      }
    });
  }

  private static void memUsageRepeatedMessages(final int numMessages) {
    System.out.println("*** memUsageRepeatedMessages - " + numMessages + " ***");
    testMemUsage(new Runnable() {
      public void run() {
        SingleRepeatedMessage.Builder builder = SingleRepeatedMessage.newBuilder();
        for (int i = 0; i < numMessages; i++) {
          builder.addMsg(NoFields.newBuilder().build());
        }
        protoReference = builder.build();
      }
    });
  }

  private static void memUsageIntExtension() {
    System.out.println("*** memUsageIntExtension ***");
    testMemUsage(new Runnable() {
      public void run() {
        protoReference = MessageWithExtensions.newBuilder()
            .setExtension(SizeTest.intExt, 321)
            .build();
      }
    });
  }

  private static void memUsageRepeatedIntExtension(final int numInts) {
    System.out.println("*** memUsageRepeatedIntExtension - " + numInts + " ***");
    testMemUsage(new Runnable() {
      public void run() {
        List<Integer> ints = new ArrayList<Integer>(numInts);
        for (int i = 0; i < numInts; i++) {
          ints.add(i);
        }
        protoReference = MessageWithExtensions.newBuilder()
            .setExtension(SizeTest.repeatedIntExt, ints)
            .build();
      }
    });
  }

  /**
   * Tests the size of a protocol buffer by enabling the malloc hook, then
   * calling the provided runnable within an autorelease pool so that all
   * temporary objects are cleaned up before memory usage is reported. The
   * runnable parameter must create a protocol buffer and assign it to
   * "protoReference".
   */
  private static void testMemUsage(Runnable runnable) {
    // Do a couple pre-runs to avoid capturing allocations that come from class
    // initialization and other runtime magic.
    testMemUsageInner(runnable);
    testMemUsageInner(runnable);
    protoReference = null;
    startTrackingMemory();
    testMemUsageInner(runnable);
    stopTrackingMemoryAndReport();
    System.out.println();
  }

  @AutoreleasePool
  private static void testMemUsageInner(Runnable runnable) {
    runnable.run();
  }

  private static native void startTrackingMemory() /*-[
    my_malloc_install();
    my_malloc_clear();
  ]-*/;

  private static native void stopTrackingMemoryAndReport() /*-[
    my_malloc_reset_and_report();
  ]-*/;
}
