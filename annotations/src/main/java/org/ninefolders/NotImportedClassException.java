package org.ninefolders;

import com.google.j2objc.annotations.ObjectiveCType;

public class NotImportedClassException extends RuntimeException {
	
	public NotImportedClassException(Object... objects) {
		throw this;
	}
	
	public static int throwNotImported(Object... objects) {
		throw new NotImportedClassException();
	}
}
