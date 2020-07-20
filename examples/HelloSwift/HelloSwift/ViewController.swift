// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  ViewController.swift
//  HelloSwift
//


import UIKit

class ViewController: UIViewController {
  @IBOutlet weak var textView: UITextView!

  override func viewDidLoad() {
    super.viewDidLoad()
    var text = ""
    let hello = OrgJ2objcExample.HELLO_J2OBJC
    let wednesday = OrgJ2objcExample_Day.WEDNESDAY.getShortName()
    let sb = JavaLangStringBuilder().append(with: "StringBuilder\n").description()
    text += "Static var: " + hello! +  "\n"
    text += "Enum: " + wednesday + "\n"
    text += "No nullability annotation: " + String(describing: type(of: hello)) + "\n"
    text += "NonNull annotation: " + String(describing: type(of: wednesday)) + "\n"
    text += sb
    textView.text = text
  }

  // It fails to build if the JRE was not build with nullability annotations.
  func testNonNullReturnTypes() {
    let b = JavaLangBoolean.valueOf(with: "x")
    let _: JavaLangBoolean = b

    let f = JavaLangFloat.valueOf(with: 3.14)
    let _: JavaLangFloat = f

    let h = JavaLangLong.toHexString(withLong: 100)
    let _: String = h

    let it = JavaUtilArrayList().iterator()
    let _: JavaUtilIterator = it

    let t = JavaUtilCalendar.getInstance().getTime()
    let _: JavaUtilDate = t

    let c = JavaUtilCalendar_Builder().setDateWith(2019, with: 10, with: 18).build()
    let _: JavaUtilCalendar = c

    let e = JavaUtilVector().elements()
    let _: JavaUtilEnumeration = e

    let r = JavaUtilRegexPattern.compile(with: ".*")
    let _: JavaUtilRegexPattern = r

    let nf = JavaTextNumberFormat.getInstance().format(with: 2.71828)
    let _: String = nf
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }

}
