/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.util;

import java.util.Stack;

/**
 * A stack to store the parameters of a call, as well as the call stack.
 * 
 */
public class CallVerificationStack extends Stack<Object> {

	/*
	 * --------------------------------------------------------------------
	 * Class variables
	 * --------------------------------------------------------------------
	 */

	private static final long serialVersionUID = 1L;

	// the singleton
	private static final CallVerificationStack _instance = new CallVerificationStack();

	/*
	 * --------------------------------------------------------------------
	 * Instance variables
	 * --------------------------------------------------------------------
	 */

	// the call stack, store StackTraceElement
	private final Stack<StackTraceElement> callStack = new Stack<StackTraceElement>();

	/*
	 * -------------------------------------------------------------------
	 * Constructors
	 * -------------------------------------------------------------------
	 */

	/**
	 * Can't be instantiated.
	 */
	private CallVerificationStack() {
		// empty
	}

	/*
	 * -------------------------------------------------------------------
	 * Methods
	 * -------------------------------------------------------------------
	 */

	/**
	 * Gets the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static CallVerificationStack getInstance() {
		return _instance;
	}

	/**
	 * Pushes the call stack.
	 */
	private void pushCallStack() {
		StackTraceElement[] eles = (new Throwable()).getStackTrace();
		int i;
		for (i = 1; i < eles.length; i++) {
			if (!eles[i].getClassName().equals(this.getClass().getName())) {
				break;
			}
		}
		this.callStack.push(eles[i]);
	}

	/**
	 * Gets the "current" calling class name.
	 * 
	 * @return the "current" calling class name
	 */
	public String getCurrentSourceClass() {
		return this.callStack.peek().getClassName();
	}

	/**
	 * Gets the "current" calling method name.
	 * 
	 * @return the "current" calling method name
	 */
	public String getCurrentSourceMethod() {
		return this.callStack.peek().getMethodName();
	}

	/**
	 * Clear the parameter stack and the call stack.
	 * 
	 */
	@Override
    public void clear() {
		this.callStack.clear();
		super.clear();
	}

	@Override
    public Object push(Object o) {
		pushCallStack();
		return super.push(o);
	}

	/**
	 * Pushes a boolean onto the top of this stack.
	 * 
	 * @param val
	 *            the value to push
	 */
	public void push(boolean val) {
		this.push(new BaseTypeWrapper(val));
	}

	/**
	 * Pushes a char onto the top of this stack.
	 * 
	 * @param val
	 *            the value to push
	 */
	public void push(char val) {
		this.push(new BaseTypeWrapper(val));
	}

	/**
	 * Pushes a double onto the top of this stack.
	 * 
	 * @param val
	 *            the value to push
	 */
	public void push(double val) {
		this.push(new BaseTypeWrapper(val));
	}

	/**
	 * Pushes a float onto the top of this stack.
	 * 
	 * @param val
	 *            the value to push
	 */
	public void push(float val) {
		this.push(new BaseTypeWrapper(val));
	}

	/**
	 * Pushes an int onto the top of this stack.
	 * 
	 * @param val
	 *            the value to push
	 */
	public void push(int val) {
		this.push(new BaseTypeWrapper(val));
	}

	/**
	 * Pushes a long onto the top of this stack.
	 * 
	 * @param val
	 *            the value to push
	 */
	public void push(long val) {
		this.push(new BaseTypeWrapper(val));
	}

	/**
	 * Pushes a short onto the top of this stack.
	 * 
	 * @param val
	 *            the value to push
	 */
	public void push(short val) {
		this.push(new BaseTypeWrapper(val));
	}

	/**
	 * Pop an object.
	 * 
	 * @return the object
	 */
	@Override
    public Object pop() {
		this.callStack.pop();
		return super.pop();
	}

	/**
	 * Pop a boolean.
	 * 
	 * @return the value
	 */
	public boolean popBoolean() {
		BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
		Boolean value = (Boolean) wrapper.getValue();
		return value.booleanValue();
	}

	/**
	 * Pop a char.
	 * 
	 * @return the value
	 */
	public char popChar() {
		BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
		Character value = (Character) wrapper.getValue();
		return value.charValue();
	}

	/**
	 * Pop a double.
	 * 
	 * @return the value
	 */
	public double popDouble() {
		BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
		Double value = (Double) wrapper.getValue();
		return value.doubleValue();
	}

	/**
	 * Pop a float.
	 * 
	 * @return the value
	 */
	public float popFloat() {
		BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
		Float value = (Float) wrapper.getValue();
		return value.floatValue();
	}

	/**
	 * Pop a int.
	 * 
	 * @return the value
	 */
	public int popInt() {
		BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
		Integer value = (Integer) wrapper.getValue();
		return value.intValue();
	}

	/**
	 * Pop a long.
	 * 
	 * @return the value
	 */
	public long popLong() {
		BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
		Long value = (Long) wrapper.getValue();
		return value.longValue();
	}

	/**
	 * Pop a short.
	 * 
	 * @return the value
	 */
	public short popShort() {
		BaseTypeWrapper wrapper = (BaseTypeWrapper) this.pop();
		Short value = (Short) wrapper.getValue();
		return value.shortValue();
	}

	/*
	 * Wrapper of base types.
	 */
	class BaseTypeWrapper {

		// the internal value
		private Object value;

		/*
		 * Constructs a wrapper object for the base type <code> boolean </code> .
		 */
		public BaseTypeWrapper(boolean val) {
			this.value = new Boolean(val);
		}

		/*
		 * Constructs a wrapper object for the base type <code> c </code> .
		 */
		public BaseTypeWrapper(byte val) {
			this.value = new Byte(val);
		}

		/*
		 * Constructs a wrapper object for the base type <code> char </code> .
		 */
		public BaseTypeWrapper(char val) {
			this.value = new Character(val);
		}

		/*
		 * Constructs a wrapper object for the base type <code> double </code> .
		 */
		public BaseTypeWrapper(double val) {
			this.value = new Double(val);
		}

		/*
		 * Constructs a wrapper object for the base type <code> float </code> .
		 */
		public BaseTypeWrapper(float val) {
			this.value = new Float(val);
		}

		/*
		 * Constructs a wrapper object for the base type <code> int </code> .
		 */
		public BaseTypeWrapper(int val) {
			this.value = new Integer(val);
		}

		/*
		 * Constructs a wrapper object for the base type <code> long </code> .
		 */
		public BaseTypeWrapper(long val) {
			this.value = new Long(val);
		}

		/*
		 * Constructs a wrapper object for the base type <code> short </code> .
		 */
		public BaseTypeWrapper(short val) {
			this.value = new Short(val);
		}

		/*
		 * Gets the internal value.
		 */
		public Object getValue() {
			return this.value;
		}
	}
}
