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
   return System.getProperty("os.name").startsWith("iPhone");
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

  public void testRSAPrivateKey_2048() throws Exception {
    if (supportedPlatform()) {
      byte[] encoded = Base64.getDecoder().decode(RSA_2048_PRIVATE_KEY);
      IosRSAKey.IosRSAPrivateKey key = new IosRSAKey.IosRSAPrivateKey(encoded);
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

  public static final String RSA_2048_PRIVATE_KEY =
      "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDKEQeKxltEILpz"
      + "LY4aT6E7wlK5MYhu/qesWWS43rrkfYDFBhaLHhlm1LW1Zb1pXZq62YiqIb6nrOJl"
      + "6XQoCPWH+8Rm97giT80OGawSWKoMNKS5Umlh3slMXMxvNDsa1/fE/3pcxsrVUcLg"
      + "5axzq8EZxh5qc20R9dWQixv6oYh30qD1d0n9T37cIMiN98tXJj93A8wnyTEn7t9A"
      + "Pklt/xhXleurwTLWS5kNY5o1kLS2gFUPpg56emgBxshwn+FSeNgxx/oJNfUQSJMq"
      + "ThldL/OOrrEigsudtC6VG+0xi4uPvi1Wf4xWOa4UjaNDHy1b+07xXSWET2AaNgRR"
      + "lNZKNzU3AgMBAAECggEAGc8Nr46g9EAEkIGDt53ux2XikFTgoDdWeI+nwGpyZIzD"
      + "IxHL/1OGMGQqxqiKsNKL2MxqxZAvJyt7H0PaL42KuXumx6AMR2r56JB5hz5b5IM0"
      + "0ZbSbvW4Wvs0rAoid83t5POHUCJJS0XOJlDaM0v/YKi1adNPCGQ7NY25sna/VM5l"
      + "KnA7+rEj4REMvFvGqC2sTSrCbLINP17tRuULJmXJxwYFKox2TajCg+Cy8a1u2M0F"
      + "g8D4ckgSxU2clFRz6s8tvdcIq1QrjMfXEVTi8Q+fdoHg6/tJDZkyrJT/hHs3fMZO"
      + "NsLQ30gRXT9PUUXqmojoQPT6WJlmIvbXGZjw2r4KAQKBgQD/EpDaktG8cUWTuAiO"
      + "5kYti0utlGwplgKO40eC2A0G0W+z5m3xpSr2v8BKeGd+chhO3mDqG4kC93+tJ3zZ"
      + "jvErA+gs05y++lYYuhOneN/F2ET+W28KBUk3dPDWzUW8pTg8bfJ0qQTx24nxdJCn"
      + "RFPBIZZ8P+Wlzxu5K5i1SQiN4QKBgQDKzR99hFnBiMyYAPrOYmOky52LjJ7dxUGk"
      + "GtVwW9mYwe9d1uQmtA0jGW0dGjQK7yUejbfC0Em6Ga4NcY5uIy4xtxDxbc4C7AGH"
      + "nUnkIZBYlf6ic1rhiatNuRfg2LsLRE0Bjb/Up/UvNx45K3NUP6wJJZMZZbxhl3Lh"
      + "W89HyyU2FwKBgBO1k4vQHHS7K+0mI0Mnd/S89rQK6/Cqrrfrx/LMTvVf6Ym1HHm3"
      + "kYJPfsRCWXzjxA0UEdkFF6krBqqSioslCG2Sd7Y/A7WeElkGx84BAQmAlJQy7HmR"
      + "vv6SAqoWYnUZLyc8N6fcB43IsPf/Uc4a8X3S4pXnOg992V6Nh9c6NXcBAoGAD5Y2"
      + "XmJGt74441Hn9ObQ+8B+ilQzfZ4EjoVVdD5K+heluxOiE2txOwpgPYVfDJwWwUNp"
      + "JTr2/6urmfLpXIOtsDp+vd+Pnt/eujqXjEYcHwLgQEIXcRAwr1eTgz+FGLeWJ5Fq"
      + "dgC2sntNAl3ZjlcadNWDecc89E88kB+LTdxKLt8CgYEA0XFmBG1C08eWKZCAKqd4"
      + "I2UUVop1S/QuTyooyCMv+vAJRk1y9EzH1e9siS9dDKQyUTI3Y61CYc1RIhFdmFmw"
      + "RN2K5v9naN6LStYfi1jvS2HEDt4ZagIULpf6uj4JmAU4CnDIBIaLdc6zJdd7PcvT"
      + "gfk4GbL9kwNzd76x5DCTzEU=";
}
