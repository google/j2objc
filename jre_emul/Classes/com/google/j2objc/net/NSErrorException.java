/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.j2objc.net;

/*-[
// Macros to simplify returning a string of a preprocessor definition.
#define NSStringize_helper(x) #x
#define NSStringize(x) @NSStringize_helper(x)
#define return_name_case(type) case type: return NSStringize(type)
]-*/

/**
 * A wrapper class for an NSError, which is documented here:
 * https://developer.apple.com/library/mac/documentation/Cocoa/Reference/Foundation/Classes/NSError_Class/
 */
public class NSErrorException extends RuntimeException {
  private Object nsError;

  private static final long serialVersionUID = 7794185784954426448L;

  public NSErrorException(Object nsError) {
    super(getErrorDescription(nsError));
    this.nsError = nsError;
    Object underlyingError = getUnderlyingError(nsError);
    if (underlyingError != null) {
      initCause(new NSErrorException(underlyingError));
    }
  }

  // These methods are static so they can be called from the constructor.
  private static native String getErrorDescription(Object nsError) /*-[
    return ((NSError *)nsError).localizedDescription;
  ]-*/;

  private static native Object getUnderlyingError(Object nsError) /*-[
    NSDictionary *userInfo = ((NSError *)nsError).userInfo;
    return [userInfo objectForKey:NSUnderlyingErrorKey];
  ]-*/;

  /**
   * Returns the native NSError instance.
   */
  public Object getNSError() {
    return nsError;
  }

  /**
   * Returns the error domain for this NSError.
   */
  public native String getDomain() /*-[
    return ((NSError *)nsError_).domain;
  ]-*/;

  /**
   * Returns the native NSError code.
   */
  public native long getErrorCode() /*-[
    return ((NSError *)nsError_).code;
  ]-*/;

  /**
   * Returns the failing URL string, or null if not defined
   * by this NSError.
   */
  public native String getFailingURLString() /*-[
    NSDictionary *userInfo = ((NSError *)nsError_).userInfo;
    return [userInfo objectForKey:NSURLErrorFailingURLStringErrorKey];
  ]-*/;

  /**
   * Returns the name of the NSError code, or null if not known.
   */
  public native String getErrorName() /*-[
    switch (((NSError *)nsError_).code) {
      return_name_case(NSFileNoSuchFileError);
      return_name_case(NSFileLockingError);
      return_name_case(NSFileReadUnknownError);
      return_name_case(NSFileReadNoPermissionError);
      return_name_case(NSFileReadInvalidFileNameError);
      return_name_case(NSFileReadCorruptFileError);
      return_name_case(NSFileReadNoSuchFileError);
      return_name_case(NSFileReadInapplicableStringEncodingError);
      return_name_case(NSFileReadUnsupportedSchemeError);
      return_name_case(NSFileReadTooLargeError);
      return_name_case(NSFileReadUnknownStringEncodingError);
      return_name_case(NSFileWriteUnknownError);
      return_name_case(NSFileWriteNoPermissionError);
      return_name_case(NSFileWriteInvalidFileNameError);
      return_name_case(NSFileWriteFileExistsError);
      return_name_case(NSFileWriteInapplicableStringEncodingError);
      return_name_case(NSFileWriteUnsupportedSchemeError);
      return_name_case(NSFileWriteOutOfSpaceError);
      return_name_case(NSFileWriteVolumeReadOnlyError);
      return_name_case(NSKeyValueValidationError);
      return_name_case(NSFormattingError);
      return_name_case(NSUserCancelledError);
      return_name_case(NSFeatureUnsupportedError);
      return_name_case(NSPropertyListReadCorruptError);
      return_name_case(NSPropertyListReadUnknownVersionError);
      return_name_case(NSPropertyListReadStreamError);
      return_name_case(NSPropertyListWriteStreamError);
      return_name_case(NSExecutableNotLoadableError);
      return_name_case(NSExecutableArchitectureMismatchError);
      return_name_case(NSExecutableRuntimeMismatchError);
      return_name_case(NSExecutableLoadError);
      return_name_case(NSExecutableLinkError);
      return_name_case(NSURLErrorUnknown);
      return_name_case(NSURLErrorCancelled);
      return_name_case(NSURLErrorBadURL);
      return_name_case(NSURLErrorTimedOut);
      return_name_case(NSURLErrorUnsupportedURL);
      return_name_case(NSURLErrorCannotFindHost);
      return_name_case(NSURLErrorCannotConnectToHost);
      return_name_case(NSURLErrorDataLengthExceedsMaximum);
      return_name_case(NSURLErrorNetworkConnectionLost);
      return_name_case(NSURLErrorDNSLookupFailed);
      return_name_case(NSURLErrorHTTPTooManyRedirects);
      return_name_case(NSURLErrorResourceUnavailable);
      return_name_case(NSURLErrorNotConnectedToInternet);
      return_name_case(NSURLErrorRedirectToNonExistentLocation);
      return_name_case(NSURLErrorBadServerResponse);
      return_name_case(NSURLErrorUserCancelledAuthentication);
      return_name_case(NSURLErrorUserAuthenticationRequired);
      return_name_case(NSURLErrorZeroByteResource);
      return_name_case(NSURLErrorCannotDecodeRawData);
      return_name_case(NSURLErrorCannotDecodeContentData);
      return_name_case(NSURLErrorCannotParseResponse);
      return_name_case(NSURLErrorInternationalRoamingOff);
      return_name_case(NSURLErrorCallIsActive);
      return_name_case(NSURLErrorDataNotAllowed);
      return_name_case(NSURLErrorRequestBodyStreamExhausted);
      return_name_case(NSURLErrorFileDoesNotExist);
      return_name_case(NSURLErrorFileIsDirectory);
      return_name_case(NSURLErrorNoPermissionsToReadFile);
      return_name_case(NSURLErrorSecureConnectionFailed);
      return_name_case(NSURLErrorServerCertificateHasBadDate);
      return_name_case(NSURLErrorServerCertificateUntrusted);
      return_name_case(NSURLErrorServerCertificateHasUnknownRoot);
      return_name_case(NSURLErrorServerCertificateNotYetValid);
      return_name_case(NSURLErrorClientCertificateRejected);
      return_name_case(NSURLErrorClientCertificateRequired);
      return_name_case(NSURLErrorCannotLoadFromNetwork);
      return_name_case(NSURLErrorCannotCreateFile);
      return_name_case(NSURLErrorCannotOpenFile);
      return_name_case(NSURLErrorCannotCloseFile);
      return_name_case(NSURLErrorCannotWriteToFile);
      return_name_case(NSURLErrorCannotRemoveFile);
      return_name_case(NSURLErrorCannotMoveFile);
      return_name_case(NSURLErrorDownloadDecodingFailedMidStream);
      return_name_case(NSURLErrorDownloadDecodingFailedToComplete);
    }
    return nil;
  ]-*/;
}
