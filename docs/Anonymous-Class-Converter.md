---
layout: default
---

The Anonymous Class Converter modifies anonymous classes to be inner classes.  This involves adding fields for the final variables referenced by that class, which are stored as final fields in the class.  If there are any final fields, a constructor is added to the class which has those fields as its parameters.  If there is an initialization block (not common), it's statements are added to the new constructor.

The anonymous class is named like Java anonymous class files are, with a dollar sign followed by an integer.  The number is the one-based index into the list of anonymous classes in the same containing class.

After conversion, an anonymous class is a true inner class that was declared as a class in the outer class's source file.  For example, the Runnable anonymous class:
```java
    void test() {
      final boolean[] result = new boolean[1];
      Runnable r = new Runnable() {
        public void run() {
          result[0] = true;
        }
      );
      r.run();
    }
```
becomes:
```java
    class $1 implements Runnable {
      final boolean[] result;
      $1(boolean[] result) {
        this.result = result;
      }
      public void run() {
        result[0] = true;
      }
    }
    
    void test() {
      final boolean[] result = new boolean[1];
      Runnable r = new $1(result) ;
      r.run();
    }
```