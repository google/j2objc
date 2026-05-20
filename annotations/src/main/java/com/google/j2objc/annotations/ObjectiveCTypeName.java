package com.google.j2objc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation that specifies what the Objective-C class or protocol should be when translated. */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCTypeName {

  /**
   * The Objective-C name to use for this type.
   *
   * @return the Objective-C name.
   */
  String value();
}
