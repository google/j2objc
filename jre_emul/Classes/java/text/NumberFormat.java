package java.text;

public abstract class NumberFormat extends Format {
  
  private static NumberFormat instance = new DecimalFormat();
  
  protected NumberFormat() {
    super();
  }
  
  public static NumberFormat getInstance() {
    return instance;
  }
  
  public final static NumberFormat getNumberInstance() {
    return instance;
  }
  
  public abstract String format(double number);
}