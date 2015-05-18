---
layout: posts
title: cfront&#58; a J2ObjC Inspiration
author: Tom Ball
category: blog
tags: j2objc ios compiler
---

Developers new to J2ObjC may find its "Java compiles to Objective-C, which compiles to .o files" approach a little strange, but it's based on precedent: [`cfront`](http://en.wikipedia.org/wiki/Cfront). When C++ was first released, no compilers translated C++ sources directly into object files. Instead, the `cfront` script translated C++ into temporary C files, and then invoked cc to compile them. `cfront` took similar options as cc, so most C++ developers used it as if it was a true C++ compiler. The `cfront` script wrapped around a [transpiler](http://en.wikipedia.org/wiki/Source-to-source_compiler), though; it was a script very similar to combining the `j2objc` and `j2objcc` scripts in J2ObjC.

One of `cfront`'s innovations was [name mangling](http://en.wikipedia.org/wiki/Name_mangling), where C++ type information was embedded into C names. J2ObjC also uses name mangling ([described here](/docs/UsingTranslations)) to support features like packages and Java's method overloading semantics that Objective-C doesn't directly support. For example, the JRE has both `java.util.Date` and `java.sql.Date` classes; since C names are all global in scope, J2ObjC mangles those type names to `JavaUtilDate` and `JavaSqlDate` respectively. 

As many developers have complained, name mangling produces ugly, hard-to-read code. For example Guava's simply named `MultiSet` class name becomes `ComGoogleCommonCollectMultiSet`. Even worse is when method parameter names are also mangled, since each parameter name needs to contain type information to support method overloading. 

These complainers have my full sympathy. Way back when I ported `cfront`; since `cfront` was written in C++, new systems built it by compiling a set of `cfront`-created C files to create a bootstrap translator, then using that to build the real translator. That was rarely a problem on Unix systems, but since [CTOS](http://en.wikipedia.org/wiki/Convergent_Technologies_Operating_System) was a non-Unix system I had to pour over every mangled source file fixing issues before it could eventually translate itself.

`cfront`'s example is also why J2ObjC is not recommended for one-time translation. `cfront` had a flag to just generate C code, but its use was discouraged and eventually removed since it generated code that only a C compiler should have to read. J2ObjC recommends this same approach: maintain and share Java source across platforms, using `j2objc`/`j2objcc` or `j2objc`/Xcode to build Java as part of an iOS application. It's a waste of developer time to use J2ObjC once and then try to maintain its output as a project source file, both because it's hard to read and because we're improving what code gets generated with each release.

Who knows? Maybe if J2ObjC is successful enough, compiler developers will take note and fold its functionality into their tools, like C compiler engineers did with `cfront`. Our team can declare total victory if we're made redundant!
