package com.google.j2objc;

import com.google.j2objc.NotImportedError;

public class NotImportedError extends RuntimeException {
	
	public NotImportedError(Object... objects) {
		throw this;
	}
	
	public static Object throwUnreachableObjectError(Object... objects) {
		throw new NotImportedError();
	}

	public static byte throwUnreachablePrimitiveError(Object... objects) {
		throw new NotImportedError();
	}
}


