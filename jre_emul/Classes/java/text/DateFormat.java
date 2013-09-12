package java.text;

/**
 * Temporary class that defines DateFormat constants used by LocaleData.
 *
 * TODO(user): replace with full version.
 */
public abstract class DateFormat extends Format {
  
  public static final int FULL = 0;
  public static final int LONG = 1;
  public static final int MEDIUM = 2;
  public static final int SHORT = 3;

  public final StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
    return null;
  }
  
  public abstract Object parseObject(String string, ParsePosition position);
}