package libcore.java.util.concurrent;

import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

public class LinkedBlockingQueueTest extends TestCase {
	public void testLargeDealloc(){
		LinkedBlockingQueue<String> events = new LinkedBlockingQueue<String>();

        for(int i=0; i<10000; i++)
        {
            events.add("Heyo: "+ i);
        }
	}
}
