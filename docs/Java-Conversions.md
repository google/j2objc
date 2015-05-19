---
layout: default
---

Here is a map of what J2ObjC translates from Java to Objective-C:

<table>
  <tr>
  <tr><td><b>Java</b></td><td><b>Objective-C</b></td></tr>
  <tr><td>packages</td><td>[[class naming|Class-Naming]]</td></tr>
  <tr><td>classes</td><td> interfaces</td></tr>
  <tr><td>interfaces</td><td>protocols plus [[static variables|Static-Variables-and-Constants]]</td></tr>
  <tr><td>enum</td><td>[[enum translation|Enum-Translation]]</td></tr>
  <tr><td>annotations</td><td>class instances returned by reflection</td></tr>
  <tr><td></td><td></td></tr>
  <tr><td>method overloading</td><td>[[embedded parameter types|Embedded-Method-Parameter-Types]]</td></tr>
  <tr><td>static variables and constants</td><td>[[static variables|Static-Variables-and-Constants]]</td></tr>
  <tr><td>inner classes</td><td>outer classes ([[class naming|Class-Naming]])</td></tr>
  <tr><td>anonymous classes</td><td>outer classes ([[class naming|Class-Naming]])</td></tr>
  <tr><td></td><td></td></tr>
  <tr><td>arrays</td><td>[[array emulation|Arrays]]</td></tr>
  <tr><td>Object.clone, java.lang.Cloneable</td><td>[[clone support|Object.clone()-support]], NSCopying</td></tr>
  <tr><td></td><td></td></tr>
  <tr><td>synchronized</td><td>@synchronized</td></tr>
  <tr><td>try/catch/finally</td><td>@try/@catch/@finally</td></tr>
  <tr><td></td><td></td></tr>
  <tr><td>java.lang.Object</td><td>NSObject [[(extended)|Core-Objective-C-Class-Extensions]]</td></tr>
  <tr><td>java.lang.String</td><td>NSString [[(extended)|Core-Objective-C-Class-Extensions]]</td></tr>
  <tr><td>java.lang.Number</td><td>NSNumber [[(extended)|Core-Objective-C-Class-Extensions]]</td></tr>
  <tr><td>java.lang.Throwable</td><td>NSException [[(extended)|Core-Objective-C-Class-Extensions]]</td></tr>
  <tr><td>java.lang.Class</td><td>native wrapper around Objective-C Class</td></tr>
  <tr><td></td><td></td></tr>
  <tr><td>boolean</td><td>BOOL</td></tr>
  <tr><td>byte</td><td>char</td></tr>
  <tr><td>char</td><td>unichar</td></tr>
  <tr><td>double</td><td>double</td></tr>
  <tr><td>float</td><td>float</td></tr>
  <tr><td>int</td><td>int</td></tr>
  <tr><td>long</td><td>long long int</td></tr>
  <tr><td>short</td><td>short int</td></tr>
  <tr><td></td><td></td></tr>
  <tr><td>Java serialization</td><td>not implemented</td></tr>
  <tr><td></td><td></td></tr>
  <tr><td>JUnit tests</td><td>[[JUnit translation|JUnit-Translation]]</td></tr>
</table>
