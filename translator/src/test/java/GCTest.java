
public class GCTest {
	static Object staticNSObjectField;
	static GCTest staticField;
    static volatile GCTest staticVolatile;
    
	Object instanceNSObjectField;
	GCTest instanceField;
    volatile GCTest instanceVolatile;
    String str;
    boolean sealed;
    Integer no;
    Exception ex;
    Object obj;
	
	GCTest() {
	}
	
	void test(Object unknown) {
		staticNSObjectField = new GCTest();
		staticField = new GCTest();
		staticNSObjectField = unknown;
		
		this.instanceField = this;
        this.str = "Heelo";
        this.no = 3;
        this.ex = new Exception();
        this.obj = unknown;
		this.instanceNSObjectField = unknown;
		this.instanceNSObjectField = this;
        this.instanceVolatile = this;
        staticVolatile = this;
	}
	
}
