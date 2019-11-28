package test.j2objc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@Suite.SuiteClasses(
        {CurrencyTest.class,
                DateTest.class, NumberFormatTest.class,
                OldTimeZoneTest.class, SimpleDateFormatTest.class,
                TimeZoneTest.class
})
public class J2ObjcTest {

}
