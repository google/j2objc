---
layout: docs
---

Java enums are like C enums in that they both have names and ordinal values, but Java enums are true types with visible methods, fields, and occasionally, separate inner classes per constant.  It was tempting to translate enums as a just special kind of type, but many Objective-C client apps use enums in efficient switch statements.  The decision was to therefore translate enums twice; one as a C enum typedef, and once as an Objective-C type.

## C Enum Output

C enum output is straightforward:  each constant name is used with each declared or implied ordinal value.  The enum name is prepended to each constant name, to avoid name clashes:
```java
public enum Colors {
   RED, YELLOW, BLUE;
}
```
becomes:
```obj-c
typedef enum {
  Colors_RED = 0,
  Colors_YELLOW = 1,
  Colors_BLUE = 2,
} Colors;
```

## Enum Type Output

Since Java enums are also types, an Objective-C interface is defined for them.  The enum constants are declared as class methods that each return the associated constant, along with the built-in Java methods for enums.  The Colors enum is translated to:
```obj-c
@interface ColorsEnum : JavaLangEnum {
}
+ (ColorsEnum *)RED;
+ (ColorsEnum *)YELLOW;
+ (ColorsEnum *)BLUE;
+ (NSArray *)values;
+ (ColorsEnum *)valueOf:(NSString *)name;
- (id)initWithNSString:(NSString *)name
               withInt:(int)ordinal;
@end
```
Enum fields, constructors, additional methods, and constant types are all supported.

Because J2ObjC generates both enum definitions in the same header, programs can rely on any C enum value being equal to sending the equivalent enum constant the ordinal message.  To use the above example, it is always true that:
```obj-c
assert [[ColorsEnum RED] ordinal] == Colors_RED;
```

## Use

Because of the two translations, translated enums can be used differently, depending on need.  The translator uses the C enum typedef for switch statements, for example, and messages for all other code.  Objective-C apps can use either type of enum.
