package java.text;

/**
 * Simple implementation emulating the java.text.ParsePosition api.
 */
public class ParsePosition {
  private int index;
  private int errorIndex;

  public ParsePosition(int index) {
    this.index = index;
  }

  public void setIndex(int index) {
   this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public void setErrorIndex(int ei) {
   this.errorIndex = ei;
  }

  public int getErrorIndex() {
    return errorIndex;
  }
}
