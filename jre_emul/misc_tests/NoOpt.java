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

/**
 * No-op class to inspect what the minimum set of JRE classes are linked into an app.
 *
 * <p>To see what those classes are, run: <code>
 *   make -f tests.mk no_opt_app
 *   nm build_result/tests/no_opt_app | grep __OBJC_CLASS_RO
 * </code>
 *
 * <p>The class-dump tool can also be used to see what classes are linked into an app (see
 * https://github.com/nygard/class-dump).
 *
 * <p>To see what dependencies a specific class has, add a static reference to that class in this
 * file (i.e. <code>private static final Foo foo = new Foo();</code>), run the above command, and
 * diff with the output when no reference is present.
 */
final class NoOpt {

  public static void main(String[] args) {}
}
