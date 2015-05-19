---
layout: default
---

## The Motivation behind J2ObjC

J2ObjC started out of frustration; the frustration several development teams
had of trying to quickly iterate their web and mobile products without them
drifting apart in functionality. Many of Google's client products were created
using [GWT](https://developers.google.com/web-toolkit/) for web apps, and the
Android API for Android mobile devices. That left iPod/iPad apps, which had to
be either JavaScript web apps, or handwritten in Objective-C. Although GWT
and Android apps could share business logic (non-UI) code, there wasn't any
solution for sharing that code with iOS apps.

Several approaches to solving this problem were investigated.
[XMLVM](http://xmlvm.org/overview/) looked very promising, but at the time its
[iOS translator](http://xmlvm.org/iphone/) page stated that the project was
suspended (it's now active again, and is a good alternative to this project).
Other translation tools did one-time code conversion, requiring additional
editing before their output would successfully build and run.

## A New Project is Born

From the start, many engineers thought a translator like J2ObjC wasn't
possible. Creating a tool that can accurately translate all Java application
code to iOS while perfectly preserving its semantics is indeed impossible!
That's because iOS has rigorous user interface design standards, and its users
are very aware when an app doesn't adhere to them. In our opinion, the only
way to get a world-class, fast iOS UI is to write it in Objective-C using
Apple's iOS SDK frameworks.

As most engineers learned from limits in differential calculus, however, it
can be very useful to just get close to the impossible. We therefore set out
with a set of limits that would improve the chances of J2ObjC succeeding:

- Only support client-side development. Command-line tools and server code in
theory could be translated, but that use case is likely to have problems not
addressed by J2ObjC.
- Only support business logic code, and stay far, far
away from user interface APIs (as the old maps used to have in their outlying
corners, "here there be monsters").
- Require the [iOS Foundation
Framework](http://developer.apple.com/library/ios/#DOCUMENTATION/Miscellaneous/Conceptual/iPhoneOSTechOverview/CoreServicesLayer/CoreServicesLayer.html#//apple_ref/doc/uid/TP40007898-CH10-SW19),
not a more general base.
- Use [Xcode's
Instruments](https://developer.apple.com/library/mac/#documentation/developertools/conceptual/InstrumentsUserGuide/Introduction/Introduction.html)
to verify acceptable performance and memory use, after implementing Apple's
best practices for memory management.
- Only focus on what's needed by the
application developers, rather than what is needed for completeness. Real
applications' needs drive project requirements.

## We Find It Useful, Maybe You Will, Too

We open-sourced J2ObjC as some internal projects were finding it solves the
problem of sharing Java business logic with their iOS apps. Several teams
rely on the translator now, and we're busily adding
new functionality and fixing lots of bugs. We welcome other mobile app teams
to give it a try, and let us know what works and what needs improving.

We also find working on the project rewarding. The two hardest tasks for any
Java translator are correctly parsing and resolving Java source, and providing
a compliant Java runtime environment. The first is handled well by the
[Eclipse JDT parser](http://www.eclipse.org/jdt/), and the runtime (including its
unit tests) is based on the [Android libcore library](https://android.googlesource.com/platform/libcore/). That leaves the fun stuff
for us: mutating abstract syntax trees and generating generally easy to debug source
output. If you have an interest in Java tools or compilers, join us! There's
still lots to do, and we would love the help.
