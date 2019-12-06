package org.slowstream.j2objc;

import com.google.j2objc.annotations.ObjectiveCType;

public class UnreachableError extends RuntimeException {
	
	public UnreachableError(Object... objects) {
		throw this;
	}
	
	@ObjectiveCType("void *")
	public static Object throwUnreachableError(Object... objects) {
		throw new UnreachableError();
	}
}
