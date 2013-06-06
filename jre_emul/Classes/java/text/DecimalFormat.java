package java.text;

public class DecimalFormat extends NumberFormat {
  
  public DecimalFormat() {
    super();
  }
  
  public native String format(double number) /*-[
    return [NSNumberFormatter
               localizedStringFromNumber:[NSNumber numberWithDouble:number]
                             numberStyle:NSNumberFormatterDecimalStyle];
  ]-*/;
}