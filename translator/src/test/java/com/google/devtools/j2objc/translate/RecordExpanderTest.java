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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

/**
 * Unit tests for {@link RecordExpander}.
 *
 * @author Tom Ball
 */
public class RecordExpanderTest extends GenerationTest {

  public void testMinimalRecord() throws IOException {
    String translation =
        translateSourceFile("public record Point(int x, int y) {}", "Point", "Point.h");
    assertInTranslation(translation, "@interface Point : JavaLangRecord");
    assertTranslatedLines(
        translation, "- (instancetype)initWithInt:(int32_t)x", "withInt:(int32_t)y;");
    assertInTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
    assertInTranslation(
        translation, "void Point_initWithInt_withInt_(Point *self, int32_t x, int32_t y);");

    // Verify accessors declared for record's members.
    assertInTranslation(translation, "- (int32_t)x;");
    assertInTranslation(translation, "- (int32_t)y;");

    // Verify implementation.
    translation = getTranslatedFile("Point.m");
    assertTranslatedLines(translation, "- (int32_t)x {", "return x_;", "}");
    assertTranslatedLines(translation, "- (int32_t)y {", "return y_;", "}");

    // Verify constructor contains the record's field assignments.
    assertTranslatedLines(
        translation,
        """
        void Point_initWithInt_withInt_(Point *self, int32_t x, int32_t y) {
          JavaLangRecord_init(self);
          self->x_ = x;
          self->y_ = y;
        """);

    assertTranslatedLines(
        translation,
        """
        if (!([o isKindOfClass:[Point class]])) return false;
        Point *other = (Point *) cast_chk(o, [Point class]);
        return ((Point *) nil_chk(other))->x_ == x_ && other->y_ == y_;
        """);
    assertInTranslation(
        translation,
        "return JavaUtilObjects_hash__WithNSObjectArray_([IOSObjectArray "
            + "arrayWithObjects:(id[]){ JavaLangInteger_valueOfWithInt_(x_), "
            + "JavaLangInteger_valueOfWithInt_(y_) } count:2 type:NSObject_class_()]);");
    assertTranslatedLines(
        translation,
        """
        - (NSString *)description {
          return JreStrcat(\"$I$IC\", @\"Point[x=\", x_, @\", y=\", y_, ']');
        """);
  }

  public void testExplicitlyDeclaredRecord() throws IOException {
    options.setDisallowInheritedConstructors(true);
    String translation =
        translateSourceFile(
            """
            import java.util.Objects;
            public record Point(int x, int y) {
                public Point(int x, int y) {
                    if (x > 100) {
                        x = 100;
                    }
                    this.x = x;
                    this.y = y;
                }
                public int x() { return x; }
                public int y() { return y; }
                public boolean equals(Object o) {
                    if (!(o instanceof Point)) return false;
                    Point other = (Point) o;
                    return other.x == x && other.y == y;
                }
                public int hashCode() {
                    return Objects.hash(x, y);
                }
                public String toString() {
                    return "Point[x=" + x + ", y=" + y + "]";
                }
            }
            """,
            "Point",
            "Point.h");
    assertInTranslation(translation, "@interface Point : JavaLangRecord");
    assertInTranslation(translation, "- (int32_t)x;");
    assertInTranslation(translation, "- (int32_t)y;");
    assertInTranslation(translation, "- (bool)isEqual:(id)o;");
    assertInTranslation(translation, "- (NSUInteger)hash;");
    assertInTranslation(translation, "- (NSString *)description;");
    assertTranslatedLines(
        translation, "- (instancetype)initWithInt:(int32_t)x", "withInt:(int32_t)y;");
    assertInTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
    assertInTranslation(
        translation, "void Point_initWithInt_withInt_(Point *self, int32_t x, int32_t y);");

    // Verify implementation.
    translation = getTranslatedFile("Point.m");

    // Verify constructor contains the code above and not just the record component
    // assignments.
    assertTranslatedLines(
        translation,
        """
        void Point_initWithInt_withInt_(Point *self, int32_t x, int32_t y) {
          JavaLangRecord_init(self);
          if (x > 100) {
            x = 100;
          }
          self->x_ = x;
          self->y_ = y;
        }
        """);

    // Verify that Object methods use the above code.
    assertTranslatedLines(
        translation,
        """
        if (!([o isKindOfClass:[Point class]])) return false;
        Point *other = (Point *) cast_chk(o, [Point class]);
        return ((Point *) nil_chk(other))->x_ == x_ && other->y_ == y_;
        """);
    assertInTranslation(
        translation,
        "return JavaUtilObjects_hash__WithNSObjectArray_([IOSObjectArray "
            + "arrayWithObjects:(id[]){ JavaLangInteger_valueOfWithInt_(x_), "
            + "JavaLangInteger_valueOfWithInt_(y_) } count:2 type:NSObject_class_()]);");
    assertTranslatedLines(
        translation,
        """
        - (NSString *)description {
          return JreStrcat(\"$I$IC\", @\"Point[x=\", x_, @\", y=\", y_, ']');
        """);
  }

  public void testRecordWithImplicitInitialization() throws IOException {
    options.setDisallowInheritedConstructors(true);
    translateSourceFile(
        """
        import java.util.Objects;
        public record Point(int x, int y) {
            public Point {
                if (x > 100) {
                    x = 100;
                }
            }
        }
        """,
        "Point",
        "Point.h");

    // Verify implementation.
    String translation = getTranslatedFile("Point.m");

    // Verify constructor contains the code above plus the implicit initialization of the fields.
    assertTranslatedLines(
        translation,
        """
        void Point_initWithInt_withInt_(Point *self, int32_t x, int32_t y) {
          JavaLangRecord_init(self);
          if (x > 100) {
            x = 100;
          }
          self->x_ = x;
          self->y_ = y;
        }
        """);

    // Verify that Object methods use the above code.
    assertTranslatedLines(
        translation,
        """
        if (!([o isKindOfClass:[Point class]])) return false;
        Point *other = (Point *) cast_chk(o, [Point class]);
        return ((Point *) nil_chk(other))->x_ == x_ && other->y_ == y_;
        """);
    assertInTranslation(
        translation,
        "return JavaUtilObjects_hash__WithNSObjectArray_([IOSObjectArray "
            + "arrayWithObjects:(id[]){ JavaLangInteger_valueOfWithInt_(x_), "
            + "JavaLangInteger_valueOfWithInt_(y_) } count:2 type:NSObject_class_()]);");
    assertTranslatedLines(
        translation,
        """
        - (NSString *)description {
          return JreStrcat(\"$I$IC\", @\"Point[x=\", x_, @\", y=\", y_, ']');
        """);
  }

  public void testEqualsUsesObjectsEqualsForObjectTypes() throws IOException {
    String translation =
        translateSourceFile(
            """
            import java.util.List;
            public record Point(String s, int i, List<?>[] l) {}
            """,
            "Point",
            "Point.m");
    assertTranslatedLines(
        translation,
        """
        if (!([o isKindOfClass:[Point class]])) return false;
        Point *other = (Point *) cast_chk(o, [Point class]);
        return JavaUtilObjects_equalsWithId_withId_(((Point *) nil_chk(other))->s_, s_)\
         && other->i_ == i_\
         && JavaUtilObjects_equalsWithId_withId_(other->l_, l_);
        """);
  }
}
