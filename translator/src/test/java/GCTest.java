
public class GCTest {
	static Object staticNSObjectField;
	static GCTest staticField;
	Object instanceNSObjectField;
	GCTest instanceField;
	
	GCTest() {
	}
	
	void test(Object unknown) {
		staticNSObjectField = new GCTest();
		staticField = new GCTest();
		staticNSObjectField = unknown;
		
		this.instanceField = this;
		this.instanceNSObjectField = unknown;
		this.instanceNSObjectField = this;
	}
	
}
