# JRE JUnit Tests

This is a simple iOS app that redirects System.out and System.err to a
scrollable panel, then runs the [JUnit](http://junit.org) tests for the
J2ObjC's JRE library as a background task. Test output is also saved as
a file on the iOS device, which can be uploaded from Xcode's Organizer
window.

### App Icons ###

The project's app icons for this app were created using
[GIMP for Mac OS X](http://www.gimp.org/downloads/), then converted into iOS
icons using the [Macappicon app icon generator](http://makeappicon.com/). To
update the icons to the lastest iOS requirements, drag the junit-tests-logo.png
file to MakeAppIcon's toaster, enter your email when it's created the icon set
and it will be mailed to your account. Unzip the icon set and copy
ios/AppIcon.appiconset to j2objc/jre_emul/junit_tests/Images.xcassets/.

### Reuse and Improve ###

Feel free to clone this app and change it to run your app's JUnit tests.
We're also very interested in any improvements that would help make testing
easier.
