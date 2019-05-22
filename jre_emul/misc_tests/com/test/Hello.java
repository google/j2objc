package com.test;

import java.io.Serializable;

public class Hello implements Serializable {
  private String greeting;
  private String name;
  private transient int n;
  private static final long serialVersionUID = 1L;

  // A default constructor. This should not be called during deserialization.
  public Hello() {
    n = 1;
  }

  public Hello(String greeting, String name, int n) {
    this.greeting = greeting;
    this.name = name;
    this.n = n;
  }

  public int getN() {
    return n;
  }

  @Override
  public String toString() {
    return String.format("%s, %s!", greeting, name);
  }
}
