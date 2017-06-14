/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.security;

import java.util.Base64;
import junit.framework.TestCase;

/**
 * Unit tests for {@link IosRSAKey}.
 *
 * @author Tom Ball
 */
public class IosRSAKeyTest extends TestCase {

  // TODO(tball): remove when macOS signature support implemented.
  private static boolean supportedPlatform() {
   return System.getProperty("os.name").equals("iPhone");
  }

  public void testOpenRSAPublicKey_512() throws Exception {
    if (supportedPlatform()) {
      byte[] encoded = Base64.getDecoder().decode(RSA_512_PUBLIC_KEY);
      IosRSAKey.IosRSAPublicKey key = new IosRSAKey.IosRSAPublicKey(encoded);
      assertFalse(key.getSecKeyRef() == 0L);
    }
  }

  public void testOpenRSAPublicKey_1024() throws Exception {
    if (supportedPlatform()) {
      byte[] encoded = Base64.getDecoder().decode(RSA_1024_PUBLIC_KEY);
      IosRSAKey.IosRSAPublicKey key = new IosRSAKey.IosRSAPublicKey(encoded);
      assertFalse(key.getSecKeyRef() == 0L);
    }
  }

  public void testOpenRSAPublicKey_2048() throws Exception {
    if (supportedPlatform()) {
      byte[] encoded = Base64.getDecoder().decode(RSA_2048_PUBLIC_KEY);
      IosRSAKey.IosRSAPublicKey key = new IosRSAKey.IosRSAPublicKey(encoded);
      assertFalse(key.getSecKeyRef() == 0L);
    }
  }

  /**
   * OpenSSL RSA public key, generated with:
   *
   * $ openssl genrsa -out testkeypair.pem 512
   * $ openssl rsa -in testkeypair.pem -pubout -outform PEM -out testpublic.pem
   */
  public static final String RSA_512_PUBLIC_KEY =
      "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAOFq/kOeQaT/ee7mINS2zzyfG2BWxG4O"
      + "IsMCHxFGGi5EIZuB98xlf26wySmz9c3Ps2imgm6+ZvxuV/yHInbumokCAwEAAQ==";

  /**
   * OpenSSL RSA public key, generated with:
   *
   * $ openssl genrsa -out testkeypair.pem 1024
   * $ openssl rsa -in testkeypair.pem -pubout -outform PEM -out testpublic.pem
   */
  public static final String RSA_1024_PUBLIC_KEY =
      "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDAkmuZQepy/UuprQhg9khwK9mT"
      + "ba75N9Zl1bIKB06z621er5DK7epJVG+gKRUAAZt6s5RNZdje81UopR2oFX+X7Dmc"
      + "IZ4CLMKY6+tRxHG9+hT0GxhsGgRy2lcW6dtAuPBIkquyDebBlMAHgxINKAFkwKyI"
      + "ohWQQBLfTrPIfgulSQIDAQAB";

  /**
   * OpenSSL RSA public key, generated with:
   *
   * $ openssl genrsa -out testkeypair.pem 2048
   * $ openssl rsa -in testkeypair.pem -pubout -outform PEM -out testpublic.pem
   */
  public static final String RSA_2048_PUBLIC_KEY =
      "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvclWTosY3bDH9hC6UlYu"
      + "Z8CF43KQuq6aGpz0khkKgPm4GnD76S+7qXku6IYZTBDVeQRDcHhyGUeMhdu0O6ub"
      + "1P5DXpr+BL3ISJsBtOWxeYuXLzHf2KnYnR0trudTzi2dCVvvRh/SJh8fUXWwXM0w"
      + "Qfese+HcQFaT6rQSSWrv7Qyn6u1/kI9df0u9qC7nEFhZVJAuJpNa6rzgJUdeNd6U"
      + "ssQINBx4FEmYqmTjIrt5GDjFtnZMuudJQkBybBfxM/Y1cZac5FNFtCuG7B4NCGPI"
      + "EOT0JkoY7ml8fASZzL8vp4yS/ENi+5JHzVtAEhdTQvPAXgLPS0/TcKBxjb+oYNcs"
      + "bQIDAQAB";
}
