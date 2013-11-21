# Copyright 2011 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Makefile for building the iOS emulation library.
#
# Author: Tom Ball

.SUFFIXES: .java .m
.PHONY: default clean

include environment.mk

# Native obj-c sources that do not emulate a specific java class.
IOS_OBJS = \
	cbigint.o \
	IOSArray.o \
	IOSArrayClass.o \
	IOSBooleanArray.o \
	IOSByteArray.o \
	IOSCharArray.o \
	IOSClass.o \
	IOSConcreteClass.o \
	IOSDoubleArray.o \
	IOSFloatArray.o \
	IOSIntArray.o \
	IOSLocaleData.o \
	IOSLongArray.o \
	IOSObjectArray.o \
	IOSPrimitiveClass.o \
	IOSProtocolClass.o \
	IOSReference.o \
	IOSReflection.o \
	IOSShortArray.o \
	JavaMetadata.o \
	JavaToIOSInputStreamAdapter.o \
	JreEmulation.o \
	JreMemDebug.o \
	JreMemDebugStrongReference.o \
	NSDataInputStream.o \
	NSDataOutputStream.o \
	NSDictionaryMap.o \
	NSObject+JavaObject.o \
	NSString+JavaString.o \
	objc-sync.o \
	java_lang_RealToString.o \
	java/lang/reflect/ExecutableMember.o \
	java/lang/reflect/ParameterizedTypeImpl.o \
	java/lang/reflect/TypeVariableImpl.o

# Java classes with hand written obj-c implementations. Shouldn't be translated,
# but need to include the .java file in jre_emul.jar and the .m file in
# libjre_emul.a.
IOS_JAVA_OBJS = \
	java/lang/Iterable.o \
	java/lang/Throwable.o \
	java/lang/reflect/AccessibleObject.o \
	java/lang/reflect/Constructor.o \
	java/lang/reflect/Field.o \
	java/lang/reflect/Method.o \
	java/util/HashMap.o \
	java/util/LinkedHashMap.o

# Java interfaces with hand written obj-c headers. Don't need to be translated,
# but need to be included in the jre_emul.jar.
IOS_INTERFACES = \
	java/lang/CharSequence.h \
	java/lang/Cloneable.h \
	java/lang/Comparable.h

# Java interfaces that will generate empty .m files.
JRE_INTERFACES = \
	java/io/Closeable.h \
	java/io/DataInput.h \
	java/io/DataOutput.h \
	java/io/FileFilter.h \
	java/io/FilenameFilter.h \
	java/io/Flushable.h \
	java/io/Serializable.h \
	java/lang/Appendable.h \
	java/lang/Runnable.h \
	java/lang/annotation/Annotation.h \
	java/lang/reflect/AnnotatedElement.h \
	java/lang/reflect/GenericArrayType.h \
	java/lang/reflect/GenericDeclaration.h \
	java/lang/reflect/Member.h \
	java/lang/reflect/ParameterizedType.h \
	java/lang/reflect/Type.h \
	java/lang/reflect/TypeVariable.h \
	java/lang/reflect/WildcardType.h \
	java/security/Guard.h \
	java/security/PrivilegedAction.h \
	java/util/Collection.h \
	java/util/Comparator.h \
	java/util/Deque.h \
	java/util/Enumeration.h \
	java/util/EventListener.h \
	java/util/Formattable.h \
	java/util/Iterator.h \
	java/util/List.h \
	java/util/ListIterator.h \
	java/util/Map.h \
	java/util/NavigableMap.h \
	java/util/NavigableSet.h \
	java/util/Observer.h \
	java/util/Queue.h \
	java/util/RandomAccess.h \
	java/util/Set.h \
	java/util/SortedMap.h \
	java/util/SortedSet.h \
	java/util/concurrent/BlockingDeque.h \
	java/util/concurrent/BlockingQueue.h \
	java/util/concurrent/Callable.h \
	java/util/concurrent/CompletionService.h \
	java/util/concurrent/ConcurrentMap.h \
	java/util/concurrent/ConcurrentNavigableMap.h \
	java/util/concurrent/Delayed.h \
	java/util/concurrent/Executor.h \
	java/util/concurrent/ExecutorService.h \
	java/util/concurrent/Future.h \
	java/util/concurrent/RunnableFuture.h \
	java/util/concurrent/RunnableScheduledFuture.h \
	java/util/concurrent/ScheduledExecutorService.h \
	java/util/concurrent/ScheduledFuture.h \
	java/util/concurrent/ThreadFactory.h \
	java/util/concurrent/TransferQueue.h \
	org/apache/harmony/security/fortress/SecurityAccess.h \
	org/w3c/dom/TypeInfo.h \
	org/w3c/dom/ls/LSInput.h \
	org/w3c/dom/ls/LSResourceResolver.h \
	org/xml/sax/AttributeList.h \
	org/xml/sax/Attributes.h \
	org/xml/sax/ContentHandler.h \
	org/xml/sax/DocumentHandler.h \
	org/xml/sax/DTDHandler.h \
	org/xml/sax/EntityResolver.h \
	org/xml/sax/ErrorHandler.h \
	org/xml/sax/ext/Attributes2.h \
	org/xml/sax/ext/DeclHandler.h \
	org/xml/sax/ext/EntityResolver2.h \
	org/xml/sax/ext/LexicalHandler.h \
	org/xml/sax/ext/Locator2.h \
	org/xml/sax/Parser.h \
	org/xml/sax/XMLFilter.h \
	org/xml/sax/XMLReader.h \
	org/xmlpull/v1/XmlSerializer.h

# Java sources to be translated normally.
JAVA_SOURCES = \
	java/io/BufferedInputStream.java \
	java/io/BufferedOutputStream.java \
	java/io/BufferedReader.java \
	java/io/BufferedWriter.java \
	java/io/ByteArrayInputStream.java \
	java/io/ByteArrayOutputStream.java \
	java/io/CharArrayReader.java \
	java/io/CharArrayWriter.java \
	java/io/CharConversionException.java \
	java/io/DataInputStream.java \
	java/io/DataOutputStream.java \
	java/io/EOFException.java \
	java/io/File.java \
	java/io/FileDescriptor.java \
	java/io/FileInputStream.java \
	java/io/FileNotFoundException.java \
	java/io/FileOutputStream.java \
	java/io/FileReader.java \
	java/io/FileWriter.java \
	java/io/FilterInputStream.java \
	java/io/FilterOutputStream.java \
	java/io/FilterReader.java \
	java/io/FilterWriter.java \
	java/io/InputStream.java \
	java/io/InputStreamReader.java \
	java/io/InterruptedIOException.java \
	java/io/InvalidObjectException.java \
	java/io/IOException.java \
	java/io/LineNumberReader.java \
	java/io/NotSerializableException.java \
	java/io/ObjectStreamException.java \
	java/io/OutputStream.java \
	java/io/OutputStreamWriter.java \
	java/io/PipedInputStream.java \
	java/io/PipedOutputStream.java \
	java/io/PipedReader.java \
	java/io/PipedWriter.java \
	java/io/PrintStream.java \
	java/io/PrintWriter.java \
	java/io/PushbackInputStream.java \
	java/io/PushbackReader.java \
	java/io/Reader.java \
	java/io/SequenceInputStream.java \
	java/io/StreamTokenizer.java \
	java/io/StringReader.java \
	java/io/StringWriter.java \
	java/io/SyncFailedException.java \
	java/io/UnsupportedEncodingException.java \
	java/io/UTFDataFormatException.java \
	java/io/Writer.java \
	java/lang/AbstractMethodError.java \
	java/lang/AbstractStringBuilder.java \
	java/lang/annotation/AnnotationFormatError.java \
	java/lang/annotation/AnnotationTypeMismatchException.java \
	java/lang/annotation/ElementType.java \
	java/lang/annotation/IncompleteAnnotationException.java \
	java/lang/annotation/RetentionPolicy.java \
	java/lang/ArithmeticException.java \
	java/lang/ArrayIndexOutOfBoundsException.java \
	java/lang/ArrayStoreException.java \
	java/lang/AssertionError.java \
	java/lang/AutoCloseable.java \
	java/lang/Boolean.java \
	java/lang/Byte.java \
	java/lang/Character.java \
	java/lang/ClassCastException.java \
	java/lang/ClassFormatError.java \
	java/lang/ClassLoader.java \
	java/lang/ClassNotFoundException.java \
	java/lang/CloneNotSupportedException.java \
	java/lang/Deprecated.java \
	java/lang/Double.java \
	java/lang/Enum.java \
	java/lang/Error.java \
	java/lang/Exception.java \
	java/lang/ExceptionInInitializerError.java \
	java/lang/Float.java \
	java/lang/IllegalAccessException.java \
	java/lang/IllegalArgumentException.java \
	java/lang/IllegalMonitorStateException.java \
	java/lang/IllegalStateException.java \
	java/lang/IllegalThreadStateException.java \
	java/lang/IncompatibleClassChangeError.java \
	java/lang/IndexOutOfBoundsException.java \
	java/lang/InstantiationException.java \
	java/lang/Integer.java \
	java/lang/IntegralToString.java \
	java/lang/InternalError.java \
	java/lang/InterruptedException.java \
	java/lang/LinkageError.java \
	java/lang/Long.java \
	java/lang/Math.java \
	java/lang/NegativeArraySizeException.java \
	java/lang/NoSuchFieldError.java \
	java/lang/NoSuchFieldException.java \
	java/lang/NoSuchMethodError.java \
	java/lang/NoSuchMethodException.java \
	java/lang/NullPointerException.java \
	java/lang/NumberFormatException.java \
	java/lang/OutOfMemoryError.java \
	java/lang/Override.java \
	java/lang/Package.java \
	java/lang/Readable.java \
	java/lang/RealToString.java \
	java/lang/annotation/Documented.java \
	java/lang/annotation/Inherited.java \
	java/lang/annotation/Retention.java \
	java/lang/annotation/Target.java \
	java/lang/reflect/Array.java \
	java/lang/reflect/GenericSignatureFormatError.java \
	java/lang/reflect/InvocationHandler.java \
	java/lang/reflect/InvocationTargetException.java \
	java/lang/reflect/MalformedParameterizedTypeException.java \
	java/lang/reflect/Modifier.java \
	java/lang/reflect/Proxy.java \
	java/lang/reflect/ReflectPermission.java \
	java/lang/reflect/UndeclaredThrowableException.java \
	java/lang/Runtime.java \
	java/lang/RuntimeException.java \
	java/lang/RuntimePermission.java \
	java/lang/SafeVarargs.java \
	java/lang/SecurityException.java \
	java/lang/SecurityManager.java \
	java/lang/Short.java \
	java/lang/StackTraceElement.java \
	java/lang/StrictMath.java \
	java/lang/StringBuffer.java \
	java/lang/StringBuilder.java \
	java/lang/StringIndexOutOfBoundsException.java \
	java/lang/SuppressWarnings.java \
	java/lang/System.java \
	java/lang/Thread.java \
	java/lang/ThreadDeath.java \
	java/lang/ThreadGroup.java \
	java/lang/ThreadLocal.java \
	java/lang/TypeNotPresentException.java \
	java/lang/UnsupportedOperationException.java \
	java/lang/VirtualMachineError.java \
	java/lang/Void.java \
	java/lang/ref/PhantomReference.java \
	java/lang/ref/Reference.java \
	java/lang/ref/ReferenceQueue.java \
	java/lang/ref/SoftReference.java \
	java/lang/ref/WeakReference.java \
	java/math/BigDecimal.java \
	java/math/BigInteger.java \
	java/math/BitLevel.java \
	java/math/Conversion.java \
	java/math/Division.java \
	java/math/Elementary.java \
	java/math/Logical.java \
	java/math/MathContext.java \
	java/math/Multiplication.java \
	java/math/Primality.java \
	java/math/RoundingMode.java \
	java/net/MalformedURLException.java \
	java/net/URI.java \
	java/net/URL.java \
	java/net/URLEncoder.java \
	java/net/URLStreamHandler.java \
	java/net/URLStreamHandlerFactory.java \
	java/net/URIEncoderDecoder.java \
	java/net/URISyntaxException.java \
	java/nio/Buffer.java \
	java/nio/BufferFactory.java \
	java/nio/BufferOverflowException.java \
	java/nio/BufferUnderflowException.java \
	java/nio/ByteBuffer.java \
	java/nio/ByteOrder.java \
	java/nio/CharArrayBuffer.java \
	java/nio/CharBuffer.java \
	java/nio/CharSequenceAdapter.java \
	java/nio/DoubleArrayBuffer.java \
	java/nio/DoubleBuffer.java \
	java/nio/FloatArrayBuffer.java \
	java/nio/FloatBuffer.java \
	java/nio/HeapByteBuffer.java \
	java/nio/IntArrayBuffer.java \
	java/nio/IntBuffer.java \
	java/nio/InvalidMarkException.java \
	java/nio/LongArrayBuffer.java \
	java/nio/LongBuffer.java \
	java/nio/ReadOnlyBufferException.java \
	java/nio/ReadOnlyCharArrayBuffer.java \
	java/nio/ReadOnlyDoubleArrayBuffer.java \
	java/nio/ReadOnlyFloatArrayBuffer.java \
	java/nio/ReadOnlyHeapByteBuffer.java \
	java/nio/ReadOnlyIntArrayBuffer.java \
	java/nio/ReadOnlyLongArrayBuffer.java \
	java/nio/ReadOnlyShortArrayBuffer.java \
	java/nio/ReadWriteCharArrayBuffer.java \
	java/nio/ReadWriteDoubleArrayBuffer.java \
	java/nio/ReadWriteFloatArrayBuffer.java \
	java/nio/ReadWriteHeapByteBuffer.java \
	java/nio/ReadWriteIntArrayBuffer.java \
	java/nio/ReadWriteLongArrayBuffer.java \
	java/nio/ReadWriteShortArrayBuffer.java \
	java/nio/ShortArrayBuffer.java \
	java/nio/ShortBuffer.java \
	java/nio/charset/CharacterCodingException.java \
	java/nio/charset/Charset.java \
	java/nio/charset/CharsetDecoder.java \
	java/nio/charset/CharsetEncoder.java \
	java/nio/charset/Charsets.java \
	java/nio/charset/CoderMalfunctionError.java \
	java/nio/charset/CoderResult.java \
	java/nio/charset/CodingErrorAction.java \
	java/nio/charset/IllegalCharsetNameException.java \
	java/nio/charset/IOSCharset.java \
	java/nio/charset/IOSCharsetDecoder.java \
	java/nio/charset/IOSCharsetEncoder.java \
	java/nio/charset/MalformedInputException.java \
	java/nio/charset/StandardCharsets.java \
	java/nio/charset/UnmappableCharacterException.java \
	java/nio/charset/UnsupportedCharsetException.java \
	java/security/AccessControlContext.java \
	java/security/AccessControlException.java \
	java/security/AccessController.java \
	java/security/AllPermission.java \
	java/security/AllPermissionCollection.java \
	java/security/BasicPermission.java \
	java/security/CodeSource.java \
	java/security/DomainCombiner.java \
	java/security/GeneralSecurityException.java \
	java/security/GuardedObject.java \
	java/security/InvalidParameterException.java \
	java/security/NoSuchAlgorithmException.java \
	java/security/NoSuchProviderException.java \
	java/security/Permission.java \
	java/security/PermissionCollection.java \
	java/security/Permissions.java \
	java/security/PermissionsHash.java \
	java/security/Policy.java \
	java/security/Principal.java \
	java/security/PrivilegedActionException.java \
	java/security/PrivilegedExceptionAction.java \
	java/security/ProtectionDomain.java \
	java/security/Provider.java \
	java/security/SecureClassLoader.java \
	java/security/SecureRandom.java \
	java/security/Security.java \
	java/security/SecurityPermission.java \
	java/sql/Date.java \
	java/sql/Time.java \
	java/sql/Timestamp.java \
	java/text/Annotation.java \
	java/text/AttributedCharacterIterator.java \
	java/text/AttributedString.java \
	java/text/CharacterIterator.java \
	java/text/ChoiceFormat.java \
	java/text/CollationKey.java \
	java/text/Collator.java \
	java/text/DateFormat.java \
	java/text/DateFormatSymbols.java \
	java/text/DecimalFormat.java \
	java/text/DecimalFormatSymbols.java \
	java/text/FieldPosition.java \
	java/text/Format.java \
	java/text/IOSCollator.java \
	java/text/MessageFormat.java \
	java/text/NumberFormat.java \
	java/text/ParseException.java \
	java/text/ParsePosition.java \
	java/text/SimpleDateFormat.java \
	java/util/AbstractCollection.java \
	java/util/AbstractList.java \
	java/util/AbstractMap.java \
	java/util/AbstractQueue.java \
	java/util/AbstractSequentialList.java \
	java/util/AbstractSet.java \
	java/util/ArrayDeque.java \
	java/util/ArrayList.java \
	java/util/Arrays.java \
	java/util/BitSet.java \
	java/util/Calendar.java \
	java/util/Collections.java \
	java/util/ConcurrentModificationException.java \
	java/util/concurrent/AbstractExecutorService.java \
	java/util/concurrent/ArrayBlockingQueue.java \
	java/util/concurrent/BrokenBarrierException.java \
	java/util/concurrent/CancellationException.java \
	java/util/concurrent/ConcurrentHashMap.java \
	java/util/concurrent/ConcurrentLinkedDeque.java \
	java/util/concurrent/ConcurrentLinkedQueue.java \
	java/util/concurrent/ConcurrentSkipListMap.java \
	java/util/concurrent/ConcurrentSkipListSet.java \
	java/util/concurrent/CountDownLatch.java \
	java/util/concurrent/CopyOnWriteArrayList.java \
	java/util/concurrent/CopyOnWriteArraySet.java \
	java/util/concurrent/CyclicBarrier.java \
	java/util/concurrent/DelayQueue.java \
	java/util/concurrent/Exchanger.java \
	java/util/concurrent/ExecutorCompletionService.java \
	java/util/concurrent/Executors.java \
	java/util/concurrent/ExecutionException.java \
	java/util/concurrent/ForkJoinPool.java \
	java/util/concurrent/ForkJoinTask.java \
	java/util/concurrent/ForkJoinWorkerThread.java \
	java/util/concurrent/FutureTask.java \
	java/util/concurrent/LinkedBlockingDeque.java \
	java/util/concurrent/LinkedBlockingQueue.java \
	java/util/concurrent/LinkedTransferQueue.java \
	java/util/concurrent/Phaser.java \
	java/util/concurrent/PriorityBlockingQueue.java \
	java/util/concurrent/RecursiveAction.java \
	java/util/concurrent/RecursiveTask.java \
	java/util/concurrent/RejectedExecutionException.java \
	java/util/concurrent/RejectedExecutionHandler.java \
	java/util/concurrent/ScheduledThreadPoolExecutor.java \
	java/util/concurrent/Semaphore.java \
	java/util/concurrent/SynchronousQueue.java \
	java/util/concurrent/ThreadLocalRandom.java \
	java/util/concurrent/ThreadPoolExecutor.java \
	java/util/concurrent/TimeoutException.java \
	java/util/concurrent/TimeUnit.java \
	java/util/concurrent/atomic/AtomicBoolean.java \
	java/util/concurrent/atomic/AtomicInteger.java \
	java/util/concurrent/atomic/AtomicIntegerArray.java \
	java/util/concurrent/atomic/AtomicLong.java \
	java/util/concurrent/atomic/AtomicLongArray.java \
	java/util/concurrent/atomic/AtomicMarkableReference.java \
	java/util/concurrent/atomic/AtomicReference.java \
	java/util/concurrent/atomic/AtomicReferenceArray.java \
	java/util/concurrent/atomic/AtomicStampedReference.java \
	java/util/concurrent/atomic/Fences.java \
	java/util/concurrent/locks/AbstractOwnableSynchronizer.java \
	java/util/concurrent/locks/AbstractQueuedLongSynchronizer.java \
	java/util/concurrent/locks/AbstractQueuedSynchronizer.java \
	java/util/concurrent/locks/Condition.java \
	java/util/concurrent/locks/Lock.java \
	java/util/concurrent/locks/LockSupport.java \
	java/util/concurrent/locks/ReadWriteLock.java \
	java/util/concurrent/locks/ReentrantLock.java \
	java/util/concurrent/locks/ReentrantReadWriteLock.java \
	java/util/Currency.java \
	java/util/Date.java \
	java/util/Dictionary.java \
	java/util/DuplicateFormatFlagsException.java \
	java/util/EnumMap.java \
	java/util/EnumSet.java \
	java/util/EventObject.java \
	java/util/EventListenerProxy.java \
	java/util/EmptyStackException.java \
	java/util/FormatFlagsConversionMismatchException.java \
	java/util/FormattableFlags.java \
	java/util/Formatter.java \
	java/util/FormatterClosedException.java \
	java/util/Grego.java \
	java/util/GregorianCalendar.java \
	java/util/HashSet.java \
	java/util/Hashtable.java \
	java/util/HugeEnumSet.java \
	java/util/IdentityHashMap.java \
	java/util/IllegalFormatException.java \
	java/util/IllegalFormatCodePointException.java \
	java/util/IllegalFormatConversionException.java \
	java/util/IllegalFormatFlagsException.java \
	java/util/IllegalFormatPrecisionException.java \
	java/util/IllegalFormatWidthException.java \
	java/util/InputMismatchException.java \
	java/util/InvalidPropertiesFormatException.java \
	java/util/LinkedList.java \
	java/util/LinkedHashSet.java \
	java/util/ListResourceBundle.java \
	java/util/Locale.java \
	java/util/logging/Formatter.java \
	java/util/logging/Handler.java \
	java/util/logging/Level.java \
	java/util/logging/Logger.java \
	java/util/logging/LogManager.java \
	java/util/logging/LogRecord.java \
	java/util/logging/NSLogHandler.java \
	java/util/MapEntry.java \
	java/util/MiniEnumSet.java \
	java/util/MissingFormatArgumentException.java \
	java/util/MissingFormatWidthException.java \
	java/util/MissingResourceException.java \
	java/util/NoSuchElementException.java \
	java/util/Observable.java \
	java/util/PriorityQueue.java \
	java/util/Properties.java \
	java/util/PropertyPermission.java \
	java/util/PropertyResourceBundle.java \
	java/util/Random.java \
	java/util/ResourceBundle.java \
	java/util/ServiceConfigurationError.java \
	java/util/ServiceLoader.java \
	java/util/SimpleTimeZone.java \
	java/util/Stack.java \
	java/util/StringTokenizer.java \
	java/util/Timer.java \
	java/util/TimerTask.java \
	java/util/TimeZone.java \
	java/util/TooManyListenersException.java \
	java/util/TreeMap.java \
	java/util/TreeSet.java \
	java/util/UnknownFormatConversionException.java \
	java/util/UnknownFormatFlagsException.java \
	java/util/UUID.java \
	java/util/Vector.java \
	java/util/WeakHashMap.java \
	java/util/regex/Matcher.java \
	java/util/regex/MatchResult.java \
	java/util/regex/MatchResultImpl.java \
	java/util/regex/Pattern.java \
	java/util/regex/PatternSyntaxException.java \
	java/util/regex/Splitter.java \
	java/util/zip/Checksum.java \
	java/util/zip/CRC32.java \
	java/util/zip/DataFormatException.java \
	java/util/zip/GZIPInputStream.java \
	java/util/zip/Inflater.java \
	java/util/zip/InflaterInputStream.java \
	javax/xml/XMLConstants.java \
	javax/xml/parsers/FactoryConfigurationError.java \
	javax/xml/parsers/FilePathToURI.java \
	javax/xml/parsers/ParserConfigurationException.java \
	javax/xml/parsers/SAXParser.java \
	javax/xml/parsers/SAXParserFactory.java \
	javax/xml/transform/Result.java \
	javax/xml/transform/Source.java \
	javax/xml/validation/Schema.java \
	javax/xml/validation/TypeInfoProvider.java \
	javax/xml/validation/Validator.java \
	javax/xml/validation/ValidatorHandler.java \
	libcore/icu/ICU.java \
	libcore/icu/LocaleData.java \
	libcore/icu/NativeDecimalFormat.java \
	libcore/icu/TimeZoneNames.java \
	libcore/icu/TimeZones.java \
	libcore/internal/StringPool.java \
	libcore/io/ErrnoException.java \
	libcore/io/IoUtils.java \
	libcore/io/Libcore.java \
	libcore/io/Os.java \
	libcore/io/OsConstants.java \
	libcore/io/Posix.java \
	libcore/math/MathUtils.java \
	libcore/net/UriCodec.java \
	libcore/net/url/FileHandler.java \
	libcore/net/url/UrlUtils.java \
	libcore/util/BasicLruCache.java \
	libcore/util/EmptyArray.java \
	libcore/util/Objects.java \
	libcore/util/SneakyThrow.java \
	org/apache/harmony/luni/platform/Endianness.java \
	org/apache/harmony/luni/util/HistoricalNamesUtil.java \
	org/apache/harmony/luni/util/Util.java \
	org/apache/harmony/security/fortress/Engine.java \
	org/apache/harmony/security/fortress/Services.java \
	org/apache/harmony/xml/parsers/SAXParserFactoryImpl.java \
	org/apache/harmony/xml/parsers/SAXParserImpl.java \
	org/kxml2/io/KXmlParser.java \
	org/kxml2/io/KXmlSerializer.java \
	org/xml/sax/HandlerBase.java \
	org/xml/sax/InputSource.java \
	org/xml/sax/Locator.java \
	org/xml/sax/SAXException.java \
	org/xml/sax/SAXNotRecognizedException.java \
	org/xml/sax/SAXNotSupportedException.java \
	org/xml/sax/SAXParseException.java \
	org/xml/sax/ext/Attributes2Impl.java \
	org/xml/sax/ext/DefaultHandler2.java \
	org/xml/sax/ext/Locator2Impl.java \
	org/xml/sax/helpers/AttributeListImpl.java \
	org/xml/sax/helpers/AttributesImpl.java \
	org/xml/sax/helpers/DefaultHandler.java \
	org/xml/sax/helpers/LocatorImpl.java \
	org/xml/sax/helpers/NamespaceSupport.java \
	org/xml/sax/helpers/NewInstance.java \
	org/xml/sax/helpers/ParserAdapter.java \
	org/xml/sax/helpers/ParserFactory.java \
	org/xml/sax/helpers/XMLFilterImpl.java \
	org/xml/sax/helpers/XMLReaderAdapter.java \
	org/xml/sax/helpers/XMLReaderFactory.java \
	org/xmlpull/v1/XmlPullParser.java \
	org/xmlpull/v1/XmlPullParserException.java \
	org/xmlpull/v1/XmlPullParserFactory.java \
	org/xmlpull/v1/sax2/Driver.java \
	sun/misc/Unsafe.java

MAIN_OBJ = J2ObjCMain.o

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

STUB_JAVA_SOURCES = $(shell find $(STUBS_DIR) -name *.java)
ALL_JAVA_SOURCES = $(JAVA_SOURCES) $(IOS_JAVA_OBJS:.o=.java) $(IOS_INTERFACES:.h=.java) \
    $(JRE_INTERFACES:.h=.java)
JAVA_TO_TRANSLATE = $(JRE_INTERFACES:%.h=%.java) $(JAVA_SOURCES)
ALL_OBJS = $(JAVA_SOURCES:.java=.o) $(IOS_OBJS) $(IOS_JAVA_OBJS)
SOURCE_HEADERS = $(shell find $(EMULATION_CLASS_DIR) -name *.h)
INCLUDES = $(JAVA_TO_TRANSLATE:.java=.h) $(SOURCE_HEADERS:$(EMULATION_CLASS_DIR)/%=%)
DIST_INCLUDES = $(INCLUDES:%=$(DIST_INCLUDE_DIR)/%)
ARCH_INCLUDES = $(INCLUDES:%=$(ARCH_INCLUDE_DIR)/%)

ANNOTATIONS_JAR = $(DIST_JAR_DIR)/j2objc_annotations.jar

IPHONE_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphoneos)
SIMULATOR_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphonesimulator)

# Settings for building both Mac OS X, iPhone, and iPhone simulator libraries.
EMULATION_MACOSX_LIB = $(BUILD_DIR)/macosx-libjre_emul.a
EMULATION_IPHONE_LIB = $(BUILD_DIR)/iphone-libjre_emul.a
EMULATION_IPHONEV7S_LIB = $(BUILD_DIR)/iphonev7s-libjre_emul.a
EMULATION_SIMULATOR_LIB = $(BUILD_DIR)/simulator-libjre_emul.a
ARCH_LIBS = $(J2OBJC_ARCHS)
ARCH_LIBS := $(ARCH_LIBS:macosx=$(EMULATION_MACOSX_LIB))
ARCH_LIBS := $(ARCH_LIBS:iphone=$(EMULATION_IPHONE_LIB))
ARCH_LIBS := $(ARCH_LIBS:iphonev7s=$(EMULATION_IPHONEV7S_LIB))
ARCH_LIBS := $(ARCH_LIBS:simulator=$(EMULATION_SIMULATOR_LIB))
MACOSX_OBJS = $(ALL_OBJS:%=$(CLASS_DIR)/%)
MACOSX_FLAGS = $(SDK_FLAGS)
IPHONE_BUILD_DIR = $(BUILD_DIR)/iphone
IPHONE_OBJS = $(ALL_OBJS:%=$(IPHONE_BUILD_DIR)/%)
IPHONE_FLAGS = -arch armv7 -isysroot $(IPHONE_SDK_DIR)
IPHONEV7S_BUILD_DIR = $(BUILD_DIR)/iphonev7s
IPHONEV7S_OBJS = $(ALL_OBJS:%=$(IPHONEV7S_BUILD_DIR)/%)
IPHONEV7S_FLAGS = -arch armv7s -isysroot $(IPHONE_SDK_DIR)
SIMULATOR_BUILD_DIR = $(BUILD_DIR)/simulator
SIMULATOR_OBJS = $(ALL_OBJS:%=$(SIMULATOR_BUILD_DIR)/%)
SIMULATOR_FLAGS = -arch i386 -isysroot $(SIMULATOR_SDK_DIR)
MAIN_LIB_OBJS = $(MAIN_OBJ:%=$(CLASS_DIR)/%)

# translate is the default so that Xcode can build its own object files
default: libs
	@: # suppress make's "nothing to be done" message

GEN_OBJC_DIR = $(TRANSLATED_SOURCE_DIR)
TRANSLATE_JAVA_FULL = $(JAVA_TO_TRANSLATE)
TRANSLATE_JAVA_RELATIVE = $(JAVA_TO_TRANSLATE)
TRANSLATE_ARGS = --mem-debug -sourcepath $(JRE_SRC) -classpath $(EMULATION_JAR) \
  $(J2OBJC_DEBUGFLAGS) -encoding UTF-8
TRANSLATE_USE_SYSTEM_BOOT_PATH = TRUE
include ../make/translate.mk

analyze: $(ALL_OBJS:%.o=$(CLASS_DIR)/%.plist)

libs: $(EMULATION_LIB) $(MAIN_LIB)
	@:

xcode_build: $(XCODE_LIB) $(ARCH_BIN_DIR)/j2objcc $(ARCH_BUILD_DIR)/.includes

xcode_clean:
	@rm -rf $(GEN_OBJC_DIR) $(ARCH_BIN_DIR)/j2objcc

xcode_analyze: $(ALL_OBJS:%.o=$(TARGET_TEMP_DIR)/%.plist)

$(EMULATION_LIB): $(ARCH_LIBS)
	$(LIPO) -create $^ -output $@

$(EMULATION_MACOSX_LIB): $(MACOSX_OBJS)
	@echo "building Mac OS X library"
	@$(LIBTOOL) -static -o $@ $^

$(EMULATION_IPHONE_LIB): $(IPHONE_OBJS)
	@echo "building iPhoneOS library"
	@$(LIBTOOL) -static -o $@ $^

$(EMULATION_IPHONEV7S_LIB): $(IPHONEV7S_OBJS)
	@echo "building iPhonev7S library"
	@$(LIBTOOL) -static -o $@ $^

$(EMULATION_SIMULATOR_LIB): $(SIMULATOR_OBJS)
	@echo "building iPhoneSimulator library"
	@$(LIBTOOL) -static -o $@ $^

$(XCODE_LIB): $(ALL_OBJS:%=$(TARGET_TEMP_DIR)/%)
	@mkdir -p $(@D)
	$(LIBTOOL) -static -o $@ $^

$(MAIN_LIB): $(MAIN_LIB_OBJS)
	$(LIBTOOL) -static -o $@ $^

dist_includes: $(BUILD_DIR)/.jre_dist_includes

$(BUILD_DIR)/.jre_dist_includes: $(TRANSLATE_HEADERS) $(SOURCE_HEADERS) | \
    $(DIST_INCLUDE_DIR)
	(cd $(EMULATION_CLASS_DIR) && tar cf - $(SOURCE_HEADERS:$(EMULATION_CLASS_DIR)/%=%)) \
	    | (cd $(DIST_INCLUDE_DIR); tar xfp -)
	(cd $(GEN_OBJC_DIR) && tar cf - \
	    $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=%)) \
	    | (cd $(DIST_INCLUDE_DIR); tar xfp -)
	touch $@

$(ARCH_BUILD_DIR)/.includes: $(TRANSLATE_HEADERS) $(SOURCE_HEADERS) | \
    $(ARCH_BUILD_DIR) $(ARCH_INCLUDE_DIR)
	(cd $(EMULATION_CLASS_DIR) && tar cf - $(SOURCE_HEADERS:$(EMULATION_CLASS_DIR)/%=%)) \
	    | (cd $(ARCH_INCLUDE_DIR); tar xfp -)
	(cd $(GEN_OBJC_DIR) && tar cf - \
	    $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=%)) \
	    | (cd $(ARCH_INCLUDE_DIR); tar xfp -)
	touch $@

%/j2objcc: $(J2OBJC_ROOT)/scripts/j2objcc.sh
	@mkdir -p $(@D)
	@install -C $< $@

DIRS_TO_MAKE = $(BUILD_DIR) $(ARCH_BUILD_DIR) $(ARCH_INCLUDE_DIR) \
    $(DIST_INCLUDE_DIR) $(DIST_JAR_DIR) $(DIST_LIB_DIR)
$(sort $(DIRS_TO_MAKE)):
	@mkdir -p $@

define compile_rules
$(1)/%.o: $(3)/%.$(4) | translate
	@mkdir -p $$(@D)
	$$(CLANG) -c $$< -o $$@ $(2) $$(OBJCFLAGS) -I$$(EMULATION_CLASS_DIR) \
	    -I$$(GEN_OBJC_DIR) -I$$(ANDROID_NATIVE_DIR)

$(1)/%.plist: $(3)/%.$(4) | translate
	@mkdir -p $$(@D)
	$$(CLANG) -c $$< -o $$@ $(2) $$(OBJCFLAGS) $$(STATIC_ANALYZER_FLAGS) \
	    -I$$(EMULATION_CLASS_DIR) -I$$(GEN_OBJC_DIR) -I$$(ANDROID_NATIVE_DIR)
endef

define compile_rules_no_arc
$(1)/$(3:.m=.o): Classes/$(3) | translate
	@mkdir -p $$(@D)
	$$(CLANG) -c $$< -o $$@ $(2) $$(OBJCFLAGS_NO_ARC) \
	    -I$$(EMULATION_CLASS_DIR) -I$$(GEN_OBJC_DIR) -I$$(ANDROID_NATIVE_DIR)

$(1)/$(3:.m=.plist): Classes/$(3) | translate
	@mkdir -p $$(@D)
	$$(CLANG) -c $$< -o $$@ $(2) $$(OBJCFLAGS) $$(STATIC_ANALYZER_FLAGS) \
	    -I$$(EMULATION_CLASS_DIR) -I$$(GEN_OBJC_DIR) -I$$(ANDROID_NATIVE_DIR)
endef

define additional_compile_rules
$(1)/objc-sync.o: $(APPLE_ROOT)/objc-sync.m
	@mkdir -p $$(@D)
	$$(CLANG) -c $$< -o $$@ $(2) $$(OBJCFLAGS_NO_ARC) \
	    -I$$(EMULATION_CLASS_DIR) -I$$(GEN_OBJC_DIR) -I$$(ANDROID_NATIVE_DIR)

$(1)/objc-sync.plist: $(APPLE_ROOT)/objc-sync.m
	@mkdir -p $$(@D)
	$$(CLANG) -c $$< -o $$@ $(2) $$(OBJCFLAGS) $$(STATIC_ANALYZER_FLAGS) \
	    -I$$(EMULATION_CLASS_DIR) -I$$(GEN_OBJC_DIR) -I$$(ANDROID_NATIVE_DIR)
endef

NON_ARC_SRCS = \
    java/util/HashMap.m \
    java/util/LinkedHashMap.m \
    IOSReference.m

OBJC_SOURCE_DIRS = $(EMULATION_CLASS_DIR) $(GEN_OBJC_DIR) $(ANDROID_NATIVE_DIR)
OBJC_EXTENSIONS = m mm
arch_compile_rules = $(foreach src_dir,$(OBJC_SOURCE_DIRS),$(foreach ext,$(OBJC_EXTENSIONS),\
    $(eval $(call compile_rules,$(1),$(2),$(src_dir),$(ext))))) \
    $(foreach src,$(NON_ARC_SRCS),$(eval $(call compile_rules_no_arc,$(1),$(2),$(src)))) \
    $(eval $(call additional_compile_rules,$(1),$(2)))

$(call arch_compile_rules,$(CLASS_DIR),$(MACOSX_FLAGS))
$(call arch_compile_rules,$(IPHONE_BUILD_DIR),$(IPHONE_FLAGS))
$(call arch_compile_rules,$(IPHONEV7S_BUILD_DIR),$(IPHONEV7S_FLAGS))
$(call arch_compile_rules,$(SIMULATOR_BUILD_DIR),$(SIMULATOR_FLAGS))
ifdef TARGET_TEMP_DIR
$(call arch_compile_rules,$(TARGET_TEMP_DIR),$(ARCH_FLAGS) $(SDK_FLAGS))
endif

test: libs_dist $(DIST_DIR)/j2objcc
	$(MAKE) -f tests.mk test

clean:
	@rm -rf $(BUILD_DIR) $(GEN_OBJC_DIR)
	@rm -f $(DIST_DIR)/j2objcc $(EMULATION_JAR_DIST) $(EMULATION_LIB_DIST) $(MAIN_LIB_DIST)
	@rm -f $(DIST_INCLUDES)

dist: emul_jar_dist libs_dist $(DIST_DIR)/j2objcc

libs_dist: libs dist_includes $(EMULATION_LIB_DIST) $(MAIN_LIB_DIST)

$(EMULATION_LIB_DIST): $(EMULATION_LIB) | $(DIST_LIB_DIR)
	@install -m 0644 $< $@

$(MAIN_LIB_DIST): $(MAIN_LIB) | $(DIST_LIB_DIR)
	@install -m 0644 $< $@

java: emul_jar_dist java_sources_manifest

emul_jar_dist: $(EMULATION_JAR_DIST)
	@:

$(EMULATION_JAR_DIST): $(EMULATION_JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

$(EMULATION_JAR): $(ALL_JAVA_SOURCES) $(STUB_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "building jre_emul.jar"
	@rm -rf $(EMULATION_STAGE)
	@mkdir $(EMULATION_STAGE)
	@$(JAVAC) -classpath $(ANNOTATIONS_JAR) -d $(EMULATION_STAGE) -encoding UTF-8 \
	    -source 1.6 -target 1.6 -nowarn $^
	@jar cf $(EMULATION_JAR) -C $(EMULATION_STAGE) .

$(JAVA_SOURCES_MANIFEST): $(ALL_JAVA_SOURCES) $(STUB_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $^; do echo $(CURDIR)/$$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST): $(ALL_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $(JAVA_SOURCES); do \
	  echo $(GEN_OBJC_DIR)/$${i%.java}.h >> $@; \
	  echo $(GEN_OBJC_DIR)/$${i%.java}.m >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)

find_cycles: cycle_finder_dist $(JAVA_SOURCES_MANIFEST)
	$(DIST_DIR)/cycle_finder -w cycle_whitelist.txt `cat $(JAVA_SOURCES_MANIFEST)`
