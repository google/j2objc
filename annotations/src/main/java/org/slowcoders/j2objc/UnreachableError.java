package org.slowcoders.j2objc;

import org.slowcoders.j2objc.UnreachableError;

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


