package com.google.j2objc;

import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

public class LinkedBlockingQueueTest extends TestCase {

  /**
   * Tests that dealloc proceeds without stack overflow: https://github.com/google/j2objc/issues/808
   */
  public void testLargeDealloc(){
    LinkedBlockingQueue<String> events = new LinkedBlockingQueue<String>();

    for (int i = 0; i < LinkedListTest.SIZE_LARGE; i++) {
      events.add("Heyo: " + i);
    }
  }
}
