---
title: JRE Emulation Reference
layout: docs
---

# JRE Emulation Reference

J2ObjC includes a library that emulates a subset of the Java runtime library (`lib/libjre_emul.a`). The list below shows the set of JRE packages, types and methods that J2ObjC can translate automatically. Note that in some cases, only a subset of methods is supported for a given type; if so, the supported methods are listed below the class name.

**NOTE:** the JRE emulation support is rapidly evolving, so this page may lag behind the actual release contents.  To find the exact list of classes and methods, check the J2ObjC distribution's include directory.  Each supported class will have a matching header (.h) file, which lists all supported methods.

- [java.beans](JRE-Emulation-Reference#wiki-javabeans)
- [java.io](JRE-Emulation-Reference#wiki-javaio)
- [java.lang](JRE-Emulation-Reference#wiki-javalang)
- [java.lang.annotation](JRE-Emulation-Reference#wiki-javalangannotation)
- [java.lang.ref](JRE-Emulation-Reference#wiki-javalangref)
- [java.lang.reflect](JRE-Emulation-Reference#wiki-javalangreflect)
- [java.math](JRE-Emulation-Reference#wiki-javamath)
- [java.nio](JRE-Emulation-Reference#wiki-javanio)
- [java.nio.charset](JRE-Emulation-Reference#wiki-javaniocharset)
- [java.text](JRE-Emulation-Reference#wiki-javatext)
- [java.util](JRE-Emulation-Reference#wiki-javautil)
- [java.util.concurrent](JRE-Emulation-Reference#wiki-javautilconcurrent)
- [java.util.concurrent.atomic](JRE-Emulation-Reference#wiki-javautilconcurrentatomic)
- [java.util.concurrent.locks](JRE-Emulation-Reference#wiki-javautilconcurrentlocks)
- [java.util.regex](JRE-Emulation-Reference#wiki-javautilregex)
- [javax.xml](JRE-Emulation-Reference#wiki-javaxxml)
- [javax.xml.parsers](JRE-Emulation-Reference#wiki-javaxxmlparsers)
- [javax.xml.transform](JRE-Emulation-Reference#wiki-javaxxmltransform)
- [org.w3c.dom](JRE-Emulation-Reference#wiki-orgw3cdom)
- [org.w3c.dom.ls](JRE-Emulation-Reference#wiki-orgw3cdomls)
- [org.xml.sax](JRE-Emulation-Reference#wiki-orgxmlsax)
- [org.xml.sax.ext](JRE-Emulation-Reference#wiki-orgxmlsax)
- [org.xml.sax.helpers](JRE-Emulation-Reference#wiki-orgxmlsax)

### java.beans

<table>
  <tr><td>IndexedPropertyChangeEvent</td><td>PropertyChangeEvent</td><td>PropertyChangeListener</td><td>PropertyChangeListenerProxy</td></tr>
  <tr><td>PropertyChangeSupport</td><td></td><td></td><td></td></tr>
</table>

### java.io

<table>
  <tr><td>BufferedInputStream</td><td>BufferedOutputStream</td><td>BufferedReader</td><td>BufferedWriter</td></tr>
  <tr><td>ByteArrayInputStream</td><td>ByteArrayOutputStream</td><td>CharArrayReader</td><td>CharArrayWriter</td></tr>
  <tr><td>CharConversionException</td><td>Closeable</td><td>DataInput</td><td>DataInputStream</td></tr>
  <tr><td>DataOutput</td><td>DataOutputStream</td><td>EOFException</td><td>Externalizable</td></tr>
  <tr><td>File</td><td>FileDescriptor</td><td>FileFilter</td><td>FileInputStream</td></tr>
  <tr><td>FileNotFoundException</td><td>FileOutputStream</td><td>FileReader</td><td>FileWriter</td></tr>
  <tr><td>FilenameFilter</td><td>FilterInputStream</td><td>FilterOutputStream</td><td>FilterReader</td></tr>
  <tr><td>FilterWriter</td><td>Flushable</td><td>IOException</td><td>InputStream</td></tr>
  <tr><td>InputStreamReader</td><td>InterruptedIOException</td><td>InvalidClassException</td><td>InvalidObjectException</td></tr>
  <tr><td>LineNumberReader</td><td>NotActiveException</td><td>NotSerializeableException</td><td>ObjectInput</td></tr>
  <tr><td>ObjectInputStream</td><td>ObjectInputValidation</td><td>ObjectOutput</td><td>ObjectOutputStream</td></tr>
  <tr><td>ObjectStreamClass</td><td>ObjectStreamConstants</td><td>ObjectStreamException</td><td>ObjectStreamField</td></tr>
  <tr><td>OptionalDataException</td><td>OutputStream</td><td>OutputStreamWriter</td><td>PipedInputStream</td></tr>
  <tr><td>PipedOutputStream</td><td>PipedReader</td><td>PipedWriter</td><td>PrintStream</td></tr>
  <tr><td>PrintWriter</td><td>PushbackInputStream</td><td>PushbackReader</td><td>RandomAccessFile</td></tr>
  <tr><td>Reader</td><td>SequenceInputStream</td><td>Serializable</td><td>SerializablePermission</td></tr>
  <tr><td>StreamCorruptedException</td><td>StreamTokenizer</td><td>StringReader</td><td>StringWriter</td></tr>
  <tr><td>SyncFailedException</td><td>UTFDataFormatException</td><td>UnsupportedEncodingException</td><td>WriteAbortedException</td></tr>
  <tr><td>Writer</td><td></td><td></td><td></td></tr>
</table>

### java.lang

<table>
  <tr><td>AbstractMethodError</td><td>Appendable</td><td>ArithmeticException</td><td>ArrayIndexOutOfBoundsException</td></tr>
  <tr><td>ArrayStoreException</td><td>AssertionError</td><td>AutoCloseable</td><td>Boolean</td></tr>
  <tr><td>Byte</td><td>CharSequence</td><td>Character</td><td>Class</td></tr>
  <tr><td>ClassCastException</td><td>ClassFormatError</td><td>ClassLoader</td><td>ClassNotFoundException</td></tr>
  <tr><td>CloneNotSupportedException</td><td>Comparable</td><td>Deprecated</td><td>Double</td></tr>
  <tr><td>Enum</td><td>Error</td><td>Exception</td><td>ExceptionInInitializerError</td></tr>
  <tr><td>Float</td><td>IllegalAccessException</td><td>IllegalArgumentException</td><td>IllegalMonitorState</td></tr>
  <tr><td>IllegalStateException</td><td>IllegalThreadStateException</td><td>IncompatibleClassChangeError</td><td>IndexOutOfBoundsException</td></tr>
  <tr><td>InstantiationException</td><td>Integer</td><td>InternalError</td><td>InterruptedException</td></tr>
  <tr><td>Iterable</td><td>LinkageError</td><td>Long</td><td>Math</td></tr>
  <tr><td>NegativeArraySizeException</td><td>NoSuchFieldError</td><td>NoSuchFieldException</td><td>NoSuchMethodError</td></tr>
  <tr><td>NoSuchMethodException</td><td>NullPointerException</td><td>Number</td><td>NumberFormatException</td></tr>
  <tr><td>Object</td><td>Override</td><td>Package</td><td>Readable</td></tr>
  <tr><td>Runnable</td><td>RuntimeException</td><td>SafeArgs</td><td>SecurityException</td></tr>
  <tr><td>SecurityManager</td><td>Short</td><td>StackTraceElement</td><td>StrictMath</td></tr>
  <tr><td>String</td><td>StringBuffer</td><td>StringBuilder</td><td>StringIndexOutOfBoundsException</td></tr>
  <tr><td>SuppressWarnings</td><td>System</td><td>Thread</td><td>ThreadDeath</td></tr>
  <tr><td>ThreadGroup</td><td>ThreadLocal</td><td>Throwable</td><td>TypeNotPresentException</td></tr>
  <tr><td>UnsupportedOperationException</td><td>VirtualMachineError</td>Void<td></td></tr>
</table>

* System:
 * Supported methods: currentTimeMillis(), identityHashCode(), arrancopy(), nanoTime(), exit()
 * System properties: only os.name, file.separator, line.separator, path.separator (plus any defined using System.setProperty())
* Object, Number, and String are supported but do not have header (.h) files, because these classes are translated directly to their respective Foundation classes (NSObject, NSNumber, and NSString).  Class is supported by J2ObjC's IOSClass.

### java.lang.annotation

<table>
  <tr><td>Annotation</td><td>AnnotationFormatError</td><td>AnnotationTypeMismatchException</td><td>Documented</td></tr>
  <tr><td>ElementType</td><td>IncompleteAnnotationException</td><td>Inherited</td><td>Retention</td></tr>
  <tr><td>RetentionPolicy</td><td>Target</td><td></td><td></td></tr>
</table>

### java.lang.ref

<table>
  <tr><td>PhantomReference</td><td>Reference</td><td>ReferenceQueue</td><td>SoftReference</td></tr>
  <tr><td>WeakReference</td><td></td><td></td><td></td></tr>
</table>

### java.lang.reflect

<table>
  <tr><td>AccessibleObject</td><td>AnnotatedElement</td><td>Array</td><td>Constructor</td></tr>
  <tr><td>ExecutableMember</td><td>Field</td><td>GenericArrayType</td><td>GenericDeclaration</td></tr>
  <tr><td>GenericSignatureFormatError</td><td>InvocationHandler</td><td>InvocationTargetException</td><td>MalformedParameterizedTypeException</td></tr>
  <tr><td>Member</td><td>Method</td><td>Modifier</td><td>ParameterizedType</td></tr>
  <tr><td>Proxy</td><td>ReflectPermission</td><td>Type</td><td>TypeVariable</td></tr>
  <tr><td>UndeclaredThrowableException</td><td>WildcardType</td><td></td><td></td></tr>
</table>

### java.math

<table>
  <tr><td>BigDecimal</td><td>BigInteger</td><td>BitLevel</td><td>Conversion</td></tr>
  <tr><td>Division</td><td>Elementary</td><td>Logical</td><td>MathContext</td></tr>
  <tr><td>Multiplication</td><td>Primality</td><td>RoundingMode</td><td></td></tr>
</table>

### java.nio

<table>
  <tr><td>Buffer</td><td>BufferFactory</td><td>BufferOverflowException</td><td>BufferUnderflowException</td></tr>
  <tr><td>ByteBuffer</td><td>ByteOrder</td><td>CharArrayBuffer</td><td>CharBuffer</td></tr>
  <tr><td>CharSequenceAdapter</td><td>DoubleArrayBuffer</td><td>DoubleBuffer</td><td>FloatArrayBuffer</td></tr>
  <tr><td>FloatBuffer</td><td>HeapByteBuffer</td><td>IntArrayBuffer</td><td>IntBuffer</td></tr>
  <tr><td>InvalidMarkException</td><td>LongArrayBuffer</td><td>LongBuffer</td><td>ReadOnlyBufferException</td></tr>
  <tr><td>ReadOnlyCharArrayBuffer</td><td>ReadOnlyDoubleArrayBuffer</td><td>ReadOnlyFloatArrayBuffer</td><td>ReadOnlyHeapByteBuffer</td></tr>
  <tr><td>ReadOnlyIntArrayBuffer</td><td>ReadOnlyLongArrayBuffer</td><td>ReadOnlyShortArrayBuffer</td><td>ReadWriteCharArrayBuffer</td></tr>
  <tr><td>ReadWriteDoubleArrayBuffer</td><td>ReadWriteFloatArrayBuffer</td><td>ReadWriteHeapByteBuffer</td><td>ReadWriteIntArrayBuffer</td></tr>
  <tr><td>ReadWriteLongArrayBuffer</td><td>ReadWriteShortArrayBuffer</td><td>ShortArrayBuffer</td><td>ShortBuffer</td></tr>
</table>

### java.nio.charset

<table>
  <tr><td>CharsetCodingException</td><td>Charset</td><td>CharsetDecoder</td><td>CharsetEncoder</td></tr>
  <tr><td>Charsets</td><td>CoderMalfunctionError</td><td>CoderResult</td><td>CodingErrorAction</td></tr>
  <tr><td>IllegalCharsetNameException</td><td>MalformedInputException</td><td>StandardCharsets</td><td>UnmappableCharacterException</td></tr>
  <tr><td>UnsupportedCharsetException</td><td></td><td></td><td></td></tr>
</table>

### java.text

<table>
  <tr><td>Annotation</td><td>AttributedCharacterIterator</td><td>AttributedString</td><td>CharacterIterator</td></tr>
  <tr><td>ChoiceFormat</td><td>CollationKey</td><td>Collator</td><td>DateFormat</td></tr>
  <tr><td>DateFormatSymbols</td><td>DecimalFormat</td><td>DecimalFormatSymbols</td><td>FieldPosition</td></tr>
  <tr><td>Format</td><td>MessageFormat</td><td>NumberFormat</td><td>ParseException</td></tr>
  <tr><td>ParsePosition</td><td>SimpleDateFormat</td><td></td><td></td></tr>
</table>

### java.util

<table>
  <tr><td>AbstractCollection</td><td>AbstractList</td><td>AbstractMap</td><td>AbstractQueue</td></tr>
  <tr><td>AbstractSequentialList</td><td>AbstractSet</td><td>ArrayDeque</td><td>ArrayList</td></tr>
  <tr><td>Arrays</td><td>BitSet</td><td>Calendar</td><td>Collection</td></tr>
  <tr><td>Collections</td><td>Comparator</td><td>ConcurrentModificationException</td><td>Currency</td></tr>
  <tr><td>Date</td><td>Deque</td><td>Dictionary</td><td>DuplicateFormatFlagsException</td></tr>
  <tr><td>EventListener</td><td>EmptyStackException</td><td>EnumMap</td><td>EnumSet</td></tr>
  <tr><td>Enumeration</td><td>EventListener</td><td>EventListenerProxy</td><td>EventObject</td></tr>
  <tr><td>GregorianCalendar</td><td>HashMap</td><td>HashSet</td><td>Hashtable</td></tr>
  <tr><td>IdentityHashMap</td><td>IllegalFormatCodePointException</td><td>IllegalFormatConversionException</td><td>IllegalFormatException</td></tr>
  <tr><td>IllegalFormatFlagsException</td><td>IllegalFormatPrecisionException</td><td>IllegalFormatWidthException</td><td>InputMismatchException</td></tr>
  <tr><td>InputMismatchException</td><td>InvalidPropertiesFormatException</td><td>Iterator</td><td>LinkedHashMap</td></tr>
  <tr><td>LinkedHashSet</td><td>LinkedList</td><td>List</td><td>ListIterator</td></tr>
  <tr><td>ListResourceBundle</td><td>Locale</td><td>Map</td><td>MissingFormatArgumentException</td></tr>
  <tr><td>MissingFormatWidthException</td><td>MissingResourceException</td><td>NavigableMap</td><td>NavigableSet</td></tr>
  <tr><td>NoSuchElementException</td><td>Observable</td><td>Observer</td><td>PriorityQueue</td></tr>
  <tr><td>Properties</td><td>PropertyResourceBundle</td><td>Queue</td><td>Random</td></tr>
  <tr><td>RandomAccess</td><td>ResourceBundle</td><td>ServiceConfigurationError</td><td>ServiceLoader</td></tr>
  <tr><td>Set</td><td>SimpleTimeZone</td><td>SortedMap</td><td>SortedSet</td></tr>
  <tr><td>Stack</td><td>TimeZone</td><td>Timer</td><td>TimerTask</td></tr>
  <tr><td>TreeMap</td><td>TreeSet</td><td>UUID</td><td>UnknownFormatConversionException</td></tr>
  <tr><td>UnknownFormatFlagsException</td><td>Vector</td><td>WeakHashMap</td><td></td></tr>
</table>

### java.util.concurrent

<table>
  <tr><td>AbstractExecutorService</td><td>ArrayBlockingQueue</td><td>BlockingDeque</td><td>BlockingQueue</td></tr>
  <tr><td>BrokenBarrierException</td><td>Callable</td><td>CancellationException</td><td>CompletionService</td></tr>
  <tr><td>ConcurrentHashMap</td><td>ConcurrentLinkedDeque</td><td>ConcurrentLinkedQueue</td><td>ConcurrentMap</td></tr>
  <tr><td>ConcurrentNavigableMap</td><td>ConcurrentSkipListMap</td><td>ConcurrentSkipListSet</td><td>CopyOnWriteArrayList</td></tr>
  <tr><td>CopyOnWriteArraySet</td><td>CountDownLatch</td><td>CyclicBarrier</td><td>DelayQueue</td></tr>
  <tr><td>Delayed</td><td>Exchanger</td><td>ExecutionException</td><td>Executor</td></tr>
  <tr><td>ExecutorCompletionService</td><td>ExecutorService</td><td>Executors</td><td>ForkJoinPool</td></tr>
  <tr><td>ForkJoinTask</td><td>ForkJoinWorkerThread</td><td>Future</td><td>FutureTask</td></tr>
  <tr><td>LinkedBlockingDeque</td><td>LinkedBlockingQueue</td><td>LinkedTransferQueue</td><td>Phaser</td></tr>
  <tr><td>PriorityBlockingQueue</td><td>RecursiveAction</td><td>RecursiveTask</td><td>RejectedExecutionException</td></tr>
  <tr><td>RejectedExecutionHandler</td><td>RunnableFuture</td><td>RunnableScheduledFuture</td><td>ScheduledExecutorService</td></tr>
  <tr><td>ScheduledFuture</td><td>ScheduledThreadPoolExecutor</td><td>Semaphore</td><td>SynchronousQueue</td></tr>
  <tr><td>ThreadFactory</td><td>ThreadLocalRandom</td><td>ThreadPoolExecutor</td><td>TimeUnit</td></tr>
  <tr><td>TimeoutException</td><td>TransferQueue</td><td></td><td></td></tr>
</table>

### java.util.concurrent.atomic

<table>
  <tr><td>AtomicBoolean</td><td>AtomicInteger</td><td>AtomicIntegerArray</td><td>AtomicLong</td></tr>
  <tr><td>AtomicLongArray</td><td>AtomicLongFieldUpdater</td><td>AtomicMarkableReference</td><td>AtomicReference</td></tr>
  <tr><td>AtomicReferenceArray</td><td>AtomicReferenceFieldUpdater</td><td>AtomicStampedReference</td><td>Fences</td></tr>
</table>

### java.util.concurrent.locks

<table>
  <tr><td>AbstractOwnableSynchronizer</td><td>AbstractQueuedLongSynchronizer</td><td>AbstractQueuedSynchronizer</td><td>Condition</td></tr>
  <tr><td>Lock</td><td>LockSupport</td><td>ReadWriteLock</td><td>ReentrantLock</td></tr>
  <tr><td>ReentrantReadWriteLock</td><td></td><td></td><td></td></tr>
</table>

### java.util.logging

<table>
  <tr><td>ConsoleHandler</td><td>ErrorManager</td><td>Filter</td><td>Formatter</td></tr>
  <tr><td>Handler</td><td>Level</td><td>LogManager</td><td>LogRecord</td></tr>
  <tr><td>Logger</td><td>LoggingMXBean</td><td>LoggingPermission</td><td>MemoryHandler</td></tr>
  <tr><td>SimpleFormatter</td><td>StreamHandler</td><td>XMLFormatter</td><td></td></tr>
</table>

### java.util.regex

<table>
  <tr><td>MatchResult</td><td>Matcher</td><td>Pattern</td><td>PatternSyntaxException</td></tr>
  <tr><td>Splitter</td><td></td><td></td><td></td></tr>
</table>

### javax.xml

<table>
  <tr><td>XMLConstants</td></tr>
</table>

### javax.xml.parsers

<table>
  <tr><td>FactoryConfigurationError</td><td>FilePathToURI</td><td>ParserConfigurationException</td><td>SAXParser</td></tr>
  <tr><td>SAXParserFactory</td><td></td><td></td><td></td></tr>
</table>

### javax.xml.transform

<table>
  <tr><td>Result</td><td>Source</td></tr>
</table>

### javax.xml.validation

<table>
  <tr><td>Schema</td><td>TypeInfoProvider</td><td>Validator</td><td>ValidatorHandler</td></tr>
</table>

### org.w3c.dom

<table>
  <tr><td>TypeInfo</td></tr>
</table>

### org.w3c.dom.ls

<table>
  <tr><td>LSInput</td><td>LSResourceResolver</td></tr>
</table>

### org.xml.sax

<table>
  <tr><td>AttributeList</td><td>Attributes</td><td>ContentHandler</td><td>DocumentHandler</td></tr>
  <tr><td>DTDHandler</td><td>EntityResolver</td><td>ErrorHandler</td><td>HandlerBase</td></tr>
  <tr><td>InputSource</td><td>Locator</td><td>Parser</td><td>SAXException</td></tr>
  <tr><td>SAXNotRecognizedException</td><td>SAXNotSupportedException</td><td>SAXParseException</td><td>XMLFilter</td></tr>
  <tr><td>XMLReader</td><td></td><td></td><td></td></tr>
</table>

### org.xml.sax.ext

<table>
  <tr><td>Attributes2</td><td>Attributes2Impl</td><td>DeclHandler</td><td>DefaultHandler2</td></tr>
  <tr><td>EntityResolver2</td><td>LexicalHandler</td><td>Locator2</td><td>Locator2Impl</td><td></td></tr>
</table>

### org.xml.sax.helpers

<table>
  <tr><td>AttributeListImpl</td><td>AttributesImpl</td><td>DefaultHandler</td><td>LocatorImpl</td></tr>
  <tr><td>NamespaceSupport</td><td>ParserAdapter</td><td>ParserFactory</td><td>XMLFilterImpl</td></tr>
  <tr><td>XMLReaderAdapter</td><td>XMLReaderFactory</td><td></td><td></td></tr>
</table>
