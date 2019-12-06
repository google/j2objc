package org.slowstream.j2objc;

import com.google.j2objc.annotations.ObjectiveCType;

public class UnreachableError extends RuntimeException {
	
	public UnreachableError(Object... objects) {
		throw this;
	}
	
	public static Object throwUnreachableObjectError(Object... objects) {
		throw new UnreachableError();
	}

	public static byte throwUnreachablePrimitiveError(Object... objects) {
		throw new UnreachableError();
	}
}
