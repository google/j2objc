![](https://bitbucket.org/mstrobel/procyon/wiki/logo.png)

*Procyon* is a suite of Java metaprogramming tools focused on code generation and analysis.  It includes the following libraries:

  1. Core Framework
  2. Reflection Framework
  3. Expressions Framework
  4. Compiler Toolset (Experimental)
  5. [Java Decompiler](https://bitbucket.org/mstrobel/procyon/wiki/Java%20Decompiler)

The Procyon libraries are available from **Maven Central** under group ID `org.bitbucket.mstrobel`.

### Core Framework

The `procyon-core` framework contains common support classes used by the other Procyon APIs.  Its facilities include string manipulation, collection extensions, filesystem/path utilities, freezable objects and collections, attached data stores, and some runtime type helpers.

### Reflection Framework
The `procyon-reflection` framework provides a rich reflection and code generation API with full support for generics, wildcards, and other high-level Java type concepts.  It is based on .NET's `System.Reflection` and `System.Reflection.Emit` APIs and is meant to address many of the shortcomings of the core Java reflection API, which offers rather limited and cumbersome support for generic type inspection.  Its code generation facilities include a `TypeBuilder`, `MethodBuilder`, and a bytecode emitter.

For more information, see the [Reflection Framework](https://bitbucket.org/mstrobel/procyon/wiki/Reflection%20Framework) topic.

#### Example

	:::java
    final Type<Map> map = Type.of(Map.class);
    final Type<?> rawMap = map.getErasedType();
    final Type<Map<String, Integer>> boundMap = map.makeGenericType(Types.String, Types.Integer);
    
    System.out.println(map.getDeclaredMethods().get(1));
    System.out.println(rawMap.getDeclaredMethods().get(1));
    System.out.println(boundMap.getDeclaredMethods().get(1));
    
    System.out.println(boundMap.getGenericTypeParameters());
    System.out.println(boundMap.getTypeArguments());

#### Output

    :::text
    public abstract V put(K, V)
    public abstract Object put(Object, Object)
    public abstract Integer put(String, Integer)
    [K, V]
    [java.lang.String, java.lang.Integer]

### Expressions Framework

The `procyon-expressions` framework provides a more natural form of code generation.
Rather than requiring bytecode to be emitted directly, as with `procyon-reflection`
and other popular libraries like ASM, `procyon-expressions` enables code composition
using declarative expression trees.  These expression trees may then be compiled directly
into callbacks or coupled with a `MethodBuilder`.  The `procyon-expressions` API is
almost a direct port of `System.Linq.Expressions` from .NET's Dynamic Language Runtime,
minus the dynamic callsite support (and with more relaxed rules regarding type conversions).

#### Example
    :::java    
    //
    // This lambda closes over a complex constant (a String array).
    //
    
    final ConstantExpression items = constant(
        new String[] { "one", "two", "three", "four", "five" }
    );

    //
    // If written in Java, the constructed expression would look something like this:
    // 
    // () -> {
    //     for (String item : <closure>items)
    //         System.out.printf("Got item: %s\n", item);
    // }
    //

    final ParameterExpression item = variable(Types.String, "item");
    
    final LambdaExpression<Runnable> runnable = lambda(
        Type.of(Runnable.class),
        forEach(
            item,
            items,
            call(
                field(null, Types.System.getField("out")),
                "printf",
                constant("Got item: %s\n"),
                item
            )
        )
    );
    
    System.out.println(runnable);
    
    final Runnable delegate = runnable.compile();

    delegate.run();

#### Output
    :::text
    () => for (String item : [one, two, three, four, five])
        System.out.printf("Got item: %s\n", new Object[] { item })
    
    Got item: one
    Got item: two
    Got item: three
    Got item: four
    Got item: five

### Compiler Toolset

The `procyon-compilertools` project is a work in progress that includes:

  1. Class metadata and bytecode inspection/manipulation facilities based on `Mono.Cecil`
  2. An optimization and decompiler framework based on `ILSpy`

The Compiler Toolset is still early in development and subject to change.

### Decompiler Front-End

`procyon-decompiler` is a standalone front-end for the Java decompiler included in
`procyon-compilertools`.  All dependencies are embedded in the JAR for easy redistribution.
For more information about the decompiler, see the [Java Decompiler](https://bitbucket.org/mstrobel/procyon/wiki/Java%20Decompiler) wiki page.

## Powered by Procyon

Check out these third party products based on Procyon!  Are you using Procyon in one of your projects?  Contact me ([email](mailto:mike.strobel@gmail.com) / [twitter](https://twitter.com/mstrobel)) if you would like it listed here. 

  - [SecureTeam Java Decompiler](http://www.secureteam.net/Java-Decompiler.aspx)   
    A JavaFX-based decompiler front-end with fast and convenient code navigation.  Download it, or launch it directly from your browser.

  - [Bytecode Viewer](https://github.com/Konloch/bytecode-viewer) is an open source Java decompilation, disassembly, and debugging suite by [@Konloch](https://twitter.com/Konloch).  It can produce decompiled sources from several modern Java decompilers, including Procyon, CFR, and FernFlower.