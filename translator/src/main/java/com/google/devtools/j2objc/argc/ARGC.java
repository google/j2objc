/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//  ARC+GC.h
//
//  Created by daehoon.zee on 30/10/2016.
//  https://github.com/zeedh/j2objc.git
//

package com.google.devtools.j2objc.argc;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;

import com.google.devtools.j2objc.Options;

//import org.eclipse.jdt.core.dom.ITypeBinding;
import com.google.devtools.j2objc.ast.*;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
//import com.google.devtools.j2objc.javac.JavacEnvironment;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
//import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.HeaderMap;
import com.google.devtools.j2objc.util.Mappings;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.TypeUtil;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.ITypeLoader;

public class ARGC {
	private static ArrayList<String> excludeClasses = new ArrayList<String>();
	private static ArrayList<String> excludePackages = new ArrayList<String>();
	static HashMap<String, CompilationUnit> units = new HashMap<>();
	static HashMap<String, AbstractTypeDeclaration> types = new HashMap<>();


	static HashMap<String, String> map = new HashMap<String, String>();
	static {
		map.put("jboolean", "bool");
		map.put("void", "void");
		map.put("jbyte", "int8_t");
		map.put("jshort", "uint16_t");
		map.put("jchar", "UniChar");
		map.put("jint", "int32_t");
		map.put("jlong", "int64_t");
		map.put("jfloat", "float");
		map.put("jdouble", "double");
	}

	public static String getObjCType(String res) {
		return map.get(res);
	}

	public static ArrayList<String> processListFile(File lstf) {
		if (!lstf.exists()) return null;
		
		ArrayList<String> list = new ArrayList<>();
		try {
			InputStreamReader in = new InputStreamReader(new FileInputStream(lstf));
			StringBuilder sb = new StringBuilder();
			for (int ch; (ch = in.read()) >=0; ) {
				if (ch == '\n' && sb.length() > 0) {
					if (sb.charAt(0) != '#') {
						list.add(trimPath(sb));
					}
					sb.setLength(0);
				}
				else if (sb.length() > 0 || ch > ' ') {
					sb.append((char)ch);
				}
			}
			in.close();
			if (sb.length() > 0 && sb.charAt(0) != '#') {
				list.add(trimPath(sb));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return list;
	}
	
	private static String trimPath(StringBuilder sb) {
		while (sb.charAt(sb.length() - 1) <= ' ') {
			sb.setLength(sb.length() - 1);
		}
		while (sb.charAt(sb.length() - 1) == '*') {
			sb.setLength(sb.length() - 1);
		}
		while (sb.charAt(sb.length() - 1) <= ' ') {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	public static File extractSources(File f, Options options, boolean extractResources) {
		ZipFile zfile = null;
		try {
			zfile = new ZipFile(f);
			Enumeration<? extends ZipEntry> enumerator = zfile.entries();
			File tempDir = FileUtil.createTempDir(f.getName());
			while (enumerator.hasMoreElements()) {
				ZipEntry entry = enumerator.nextElement();
				String internalPath = entry.getName();
				if (internalPath.endsWith(".java")
						|| (options.translateClassfiles() && internalPath.endsWith(".class"))) {
					// Extract JAR file to a temporary directory
					if (isExcludedClass(internalPath)) {
						if (options.isVerbose()) {
							System.out.println(internalPath + " excluded");
						}
						continue;
					}
					options.fileUtil().extractZipEntry(tempDir, zfile, entry);
				}
				else if (!entry.isDirectory() && extractResources && internalPath.indexOf("/.") < 0) {
					options.fileUtil().extractZipEntry(tempDir, zfile, entry);
				}
			}
			return tempDir;
		} catch (ZipException e) { // Also catches JarExceptions
			e.printStackTrace();
			ErrorUtil.error("Error reading file " + f.getAbsolutePath() + " as a zip or jar file.");
		} catch (IOException e) {
			e.printStackTrace();
			ErrorUtil.error(e.getMessage());
		} finally {
			if (zfile != null) { 
				try {
					zfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static void addExcludeRule(String classpath) {
		if (classpath.charAt(0) != '@') {
			if ('.' == classpath.charAt(classpath.length() - 1)) {
				excludePackages.add(classpath);
			}
			else {
				excludeClasses.add(classpath);
			}
		}
		else {
			File lstf = new File(classpath.substring(1));
			ArrayList<String> files = processListFile(lstf);
			if (files != null) {
				for (String s : files) {
					addExcludeRule(s);
				}
			}
		}
	}

	public static boolean isExcludedPackage(String _package) {
		_package = _package.replace('/', '.') + '.';
		for (String s : excludePackages) {
			if (_package.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isExcludedClass(String filename) {
		filename = filename.replace('/', '.');
		if (filename.endsWith(".java")) {
			filename = filename.substring(0, filename.length() - 5);
		}
		for (String s : excludeClasses) {
			if (filename.equals(s)) {
				return true;
			}
		}
		for (String s : excludePackages) {
			if (filename.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasExcludeRule() {
		return excludeClasses.size() > 0;
	}

	public static List<String> resolveSources(ArrayList<String> sourceFiles) {
		// TODO Auto-generated method stub
		return null;
	}

	public static int trap() {
		int a = 3;
		a ++;
		return a;
	}

	

	private static HashMap<TypeMirror, TypeMirror> testClasses = new HashMap<>(); 
	
	public static boolean isTestClass(TypeMirror type) {
		return testClasses.containsKey(type);
	}
	
	public static void preprocessUnit(CompilationUnit unit) {
		for (AbstractTypeDeclaration type : unit.getTypes()) {
			types.put(type.getName().toString(), type);
		}
		preprocessUnreachableImportedClasses(unit, new HashMap<>());
		
		
	}

	
	private static HashMap<String, String> preprocessUnreachableImportedClasses(CompilationUnit unit, HashMap<String, String> processed) {
		HashMap<String, String> urMap = unit.getUnreachableImportedClasses();
		if (processed.containsKey(unit.getSourceFilePath())) {
			return urMap;
		}
		String src_f = unit.getSourceFilePath();
		processed.put(src_f, src_f);
		for (AbstractTypeDeclaration _t : unit.getTypes()) {
			TypeElement type = _t.getTypeElement();
			if (Options.isIOSTest()) {
				boolean isTestClass = false;
				try {
					for (BodyDeclaration body : _t.getBodyDeclarations()) {
						if (body instanceof MethodDeclaration) {
							isTestClass |= ((MethodDeclaration)body).checkTestMethod();
						}
					}
					if (isTestClass) {
						testClasses.put(type.asType(), type.asType()); 
					}
				} catch (InvalidClassException e) {
					System.err.println("Testcase conversion error: " + src_f + 
							"\nTest method name must start with 'test'." +
							"\nThe name of method annotated by @Before must be 'setUp'" +
							"\nThe name of method annotated by @After must be 'tearDown'");
				}
			}
			
	        for (TypeMirror inheritedType : TypeUtil.directSupertypes(type.asType())) {
	            String name = inheritedType.toString();
	            int idx = name.indexOf('<');
	            if (idx > 0) {
	            	name = name.substring(0, idx);
	            }
	    		CompilationUnit superUnit = units.get(name);
	    		if (superUnit != null) {
	    			urMap.putAll(preprocessUnreachableImportedClasses(superUnit, processed));
	    		}
	        }
		}
		return urMap;
	}
	
	public static void registerUnit(CompilationUnit unit) {
		if (unit.getSourceFilePath().endsWith("DateOrTimePropertyScribe.java")) {
			ARGC.trap();
		}
		for (AbstractTypeDeclaration _t : unit.getTypes()) {
			TypeElement type = _t.getTypeElement();
			String name = type.getQualifiedName().toString();
	    	units.put(name, unit);
		}
	}

	public static String getCanonicalPath(File f) {
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

