// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
// Throwable.h
// JreEmulation
//
// Generated using j2objc, then edited to change the superclass to
// NSException. This allows all Java exceptions to be handled as
// Foundation exceptions.
//

#ifndef _NSException_JavaThrowable_H_
#define _NSException_JavaThrowable_H_

#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"

#include "J2ObjC_header.h"
#include "java/io/Serializable.h"

@class IOSObjectArray;
@class JavaIoPrintStream;
@class JavaIoPrintWriter;

/*!
 @brief The superclass of all classes which can be thrown by the VM.
 The two direct subclasses are recoverable exceptions (<code>Exception</code>) and
 unrecoverable errors (<code>Error</code>). This class provides common methods for
 accessing a string message which provides extra information about the
 circumstances in which the <code>Throwable</code> was created (basically an error
 message in most cases), and for saving a stack trace (that is, a record of
 the call stack at a particular point in time) which can be printed later.
 <p>A <code>Throwable</code> can also include a cause, which is a nested
 <code>Throwable</code> that represents the original problem that led to this
 <code>Throwable</code>. It is often used for wrapping various types of errors into
 a common <code>Throwable</code> without losing the detailed original error
 information. When printing the stack trace, the trace of the cause is included.
 @see Error
 @see Exception
 @see RuntimeException
 */
@interface NSException(JavaLangThrowable) <JavaIoSerializable>

#pragma mark Public

/*!
 @brief Constructs a new <code>Throwable</code> with the current stack trace and the
 given detail message.
 */
- (instancetype)initWithNSString:(NSString *)detailMessage;

/*!
 @brief Constructs a new <code>Throwable</code> with the current stack trace, the
 given detail message and cause.
 */
- (instancetype)initWithNSString:(NSString *)detailMessage
                 withNSException:(NSException *)cause;

/*!
 @brief Constructs a new <code>Throwable</code> with the current stack trace and the
 given cause.
 */
- (instancetype)initWithNSException:(NSException *)cause;

/*!
 @brief Adds <code>throwable</code> to the list of throwables suppressed by this.
 The
 throwable will included when this exception's stack trace is printed.
 @throws IllegalArgumentException if <code>throwable == this</code>.
 @throws NullPointerException if <code>throwable == null</code>.
 @since 1.7
 */
- (void)addSuppressedWithNSException:(NSException *)throwable;

/*!
 @brief Records the stack trace from the point where this method has been called
 to this <code>Throwable</code>.
 This method is invoked by the <code>Throwable</code> constructors.
 <p>This method is public so that code (such as an RPC system) which catches
 a <code>Throwable</code> and then re-throws it can replace the construction-time stack trace
 with a stack trace from the location where the exception was re-thrown, by <i>calling</i>
 <code>fillInStackTrace</code>.
 <p>This method is non-final so that non-Java language implementations can disable VM stack
 traces for their language. Filling in the stack trace is relatively expensive.
 <i>Overriding</i> this method in the root of a language's exception hierarchy allows the
 language to avoid paying for something it doesn't need.
 @return this <code>Throwable</code> instance.
 */
- (NSException *)fillInStackTrace;

/*!
 @brief Returns the cause of this <code>Throwable</code>, or <code>null</code> if there is
 no cause.
 */
- (NSException *)getCause;

/*!
 @brief Returns the detail message which was provided when this
 <code>Throwable</code> was created.
 Returns <code>null</code> if no message was
 provided at creation time. Subclasses may override this method to return
 localized text for the message. Android returns the regular detail message.
 */
- (NSString *)getLocalizedMessage;

/*!
 @brief Returns the detail message which was provided when this
 <code>Throwable</code> was created.
 Returns <code>null</code> if no message was
 provided at creation time.
 */
- (NSString *)getMessage;

/*!
 @brief Returns a clone of the array of stack trace elements of this <code>Throwable</code>.
 Each
 <code>StackTraceElement</code> represents an entry in the call stack. The
 element at position 0 is the top of the stack, that is, the stack frame
 where this <code>Throwable</code> is thrown.
 - seealso: #printStackTrace()
 */
- (IOSObjectArray *)getStackTrace;

/*!
 @brief Returns the throwables suppressed by this.
 @since 1.7
 */
- (IOSObjectArray *)getSuppressed;

/*!
 @brief Initializes the cause of this <code>Throwable</code>.
 The cause can only be
 initialized once.
 @param throwable
 the cause of this <code>Throwable</code>.
 @return this <code>Throwable</code> instance.
 @throws IllegalArgumentException
 if <code>Throwable</code> is this object.
 @throws IllegalStateException
 if the cause has already been initialized.
 */
- (NSException *)initCauseWithNSException:(NSException *)throwable
    OBJC_METHOD_FAMILY_NONE;

/*!
 @brief Writes a printable representation of this <code>Throwable</code>'s stack trace
 to the <code>System.err</code> stream.
 */
- (void)printStackTrace;

/*!
 @brief Writes a printable representation of this <code>Throwable</code>'s stack trace
 to the given print stream.
 If the <code>Throwable</code> contains a
 <code>cause</code>, the method will be invoked recursively for
 the nested <code>Throwable</code>.
 */
- (void)printStackTraceWithJavaIoPrintStream:(JavaIoPrintStream *)err;

/*!
 @brief Writes a printable representation of this <code>Throwable</code>'s stack trace
 to the specified print writer.
 If the <code>Throwable</code> contains a
 <code>cause</code>, the method will be invoked recursively for the
 nested <code>Throwable</code>.
 @param err 
 the writer to write the stack trace on.
 */
- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)err;

/*!
 @brief Sets the array of stack trace elements.
 Each <code>StackTraceElement</code>
 represents an entry in the call stack. A copy of the specified array is
 stored in this <code>Throwable</code>. will be returned by <code>getStackTrace()</code>
  and printed by <code>printStackTrace()</code>.
 @param trace
 the new array of <code>StackTraceElement</code>s. A copy of the
 array is stored in this <code>Throwable</code>, so subsequent
 changes to <code>trace</code> will not change the call stack stored
 in this <code>Throwable</code>.
 @throws NullPointerException
 if any element in <code>trace</code> is <code>null</code>.
 - seealso: #printStackTrace()
 */
- (void)setStackTraceWithJavaLangStackTraceElementArray:(IOSObjectArray *)trace;

#pragma mark Protected

/*!
 @brief Constructs a new <code>Throwable</code> with the current stack trace, the
 specified detail message and the specified cause.
 @param enableSuppression if false, <code>addSuppressed(Throwable)</code> will be a no-op.
 @param writableStackTrace if false, <code>fillInStackTrace</code> will not be called,
 this object's <code>stackTrace</code> will be null,
 calls to <code>fillInStackTrace</code> and <code>setStackTrace</code> will be no-ops,
 and <code>getStackTrace</code> will return a zero-length array.
 @since 1.7
 */
- (instancetype)initWithNSString:(NSString *)detailMessage
                 withNSException:(NSException *)cause
                     withBoolean:(jboolean)enableSuppression
                     withBoolean:(jboolean)writableStackTrace;

@end

J2OBJC_EMPTY_STATIC_INIT(NSException)

CF_EXTERN_C_BEGIN

void NSException_init(NSException *self);
NSException *new_NSException_init() NS_RETURNS_RETAINED;
NSException *create_NSException_init();

void NSException_initWithNSString_(NSException *self, NSString *detailMessage);
NSException *new_NSException_initWithNSString_(NSString *detailMessage) NS_RETURNS_RETAINED;
NSException *create_NSException_initWithNSString_(NSString *detailMessage);

void NSException_initWithNSString_withNSException_(
    NSException *self, NSString *detailMessage, NSException *cause);
NSException *new_NSException_initWithNSString_withNSException_(
    NSString *detailMessage, NSException *cause) NS_RETURNS_RETAINED;
NSException *create_NSException_initWithNSString_withNSException_(
    NSString *detailMessage, NSException *cause);

void NSException_initWithNSException_(NSException *self, NSException *cause);
NSException *new_NSException_initWithNSException_(NSException *cause) NS_RETURNS_RETAINED;
NSException *create_NSException_initWithNSException_(NSException *cause);

void NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
    NSException *self, NSString *detailMessage, NSException *cause, jboolean enableSuppression,
    jboolean writableStackTrace);
NSException *new_NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
    NSString *detailMessage, NSException *cause, jboolean enableSuppression,
    jboolean writableStackTrace) NS_RETURNS_RETAINED;
NSException *create_NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
    NSString *detailMessage, NSException *cause, jboolean enableSuppression,
    jboolean writableStackTrace);

CF_EXTERN_C_END

J2OBJC_TYPE_LITERAL_HEADER(NSException)

// Empty class to force category to be loaded.
@interface JreThrowableCategoryDummy : NSObject
@end

#pragma clang diagnostic pop
#endif // _NSException_JavaThrowable_H_
