import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import javax.net.ssl.HttpsURLConnection;

/**
 * Test that we can inspect the SSL session after connect()
 *
 * Move to here from the test suite, due to security concerns
 */
final public class InspectSSLAfterConnect {
  private static HttpsURLConnection connection;
  private static InputStream inputStream;
  private static Certificate[] certs;

  public static void main(String[] args) {
    try {
        connect();
		printSSLCertificates();
	    printPublicKeysHexEncoded();
	} catch (Exception e) {
		e.printStackTrace();
	}
  }
  
  public static void connect() throws MalformedURLException, IOException {
    connection = (HttpsURLConnection) new URL("https://www.google.com").openConnection();
    connection.connect();
    inputStream = connection.getInputStream();
  }
  
  public static void printSSLCertificates() throws Exception {
    certs = connection.getServerCertificates();
    for (Certificate cert : certs) {
      System.out.println(cert.toString());
    }
  }
  
  // tip: One way to do cross-platform public key/certificate pinning
  // is to add your public key pin set via your build.gradle BuildConfig params
  // e.g. BuildConfigField "String[]", "HTTPS_PINSET", "new String[] { ... }"
  // then compare it in runtime with the ones fetched from the server
  // as a bonus you could also compare the salted MessageDigest (e.g. SHA-256) results
  public static void printPublicKeysHexEncoded() {
    StringBuilder sb = new StringBuilder();
    for (Certificate cert : certs) {
      for (byte b : cert.getPublicKey().getEncoded()) {
        sb.append(String.format("%02X ", b));
      }
      sb.append("\n");
    }
    System.out.println(sb.toString());
  }
}
