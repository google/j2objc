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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
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
	public static boolean compatiable_2_0_2 = false;
	private static boolean isPureObjCGenerationMode;
	private static HashMap<String, PureObjCPackage> pureObjCMap = new HashMap<>();
	private static ArrayList<String> excludeClasses = new ArrayList<String>();
	private static ArrayList<String> excludePackages = new ArrayList<String>();
	static HashMap<String, CompilationUnit> units = new HashMap<>();
	static HashMap<String, AbstractTypeDeclaration> types = new HashMap<>();

	public static boolean inPureObjCMode() {
		return isPureObjCGenerationMode;
	}

	public static void endSourceFileGeneration() {
		isPureObjCGenerationMode = false;
	}

	public static void addPureObjC(String srcAndPackagePair) {
		int p = srcAndPackagePair.lastIndexOf('/') + 1;
		String root = srcAndPackagePair.substring(0, p);
		String package_ = srcAndPackagePair.substring(p).replace('.', '/');
		pureObjCMap.put(package_, new PureObjCPackage(root, package_));
	}

	public static void startSourceFileGeneration(String unitName) {
		isPureObjCGenerationMode = isPureObjC(unitName);
		if (isPureObjCGenerationMode) {
			trap();
		}
	}

	static boolean isPureObjC(String path) {
		int p = path.lastIndexOf('/');
		if (p < 0) {
			return false;
		}
		path = path.substring(0, p);
		return isPureObjFolder(path);
	}

	private static boolean isPureObjFolder(String path) {
		for (String s : pureObjCMap.keySet()) {
			if (path.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPureObjC(TypeMirror type) {
		String s = type.toString().replace('.', '/');
		boolean res = isPureObjC(s);
		return res;
	}

	public static void processPureObjC(Parser parser) {
		for (Entry<String, PureObjCPackage> e : pureObjCMap.entrySet()) {
			PureObjCPackage objc = e.getValue();
			objc.preprocess(parser);
		}
	}

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

	public static File extractSources(File f, Options options) {
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
			}
			return tempDir;
		} catch (ZipException e) { // Also catches JarExceptions
			e.printStackTrace();
			ErrorUtil.error("Error reading file " + f.getAbsolutePath() + " as a zip or jar file.");
		} catch (IOException e) {
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

	private static class PureObjCPackage {

		final String root;
		final String package_;
		private Parser parser;

		public PureObjCPackage(String root, String package_) {
			this.root = root;
			this.package_ = package_;
		}

		public void preprocess(Parser parser) {
			this.parser = parser;
			
			File f = new File(root, package_);
			add(f);
		}

		private void add(File f) {
			if (f.isDirectory()) {
				File files[] = f.listFiles();
				for (File f2 : files) {
					add(f2);
				}
			}
			else {
				String path = ARGC.getCanonicalPath(f);
				if (path.endsWith(".java")) {
					RegularInputFile inp = new RegularInputFile(path, path.substring(root.length()));				
				    CompilationUnit compilationUnit = parser.parse(inp);
				    new NoWithSuffixMethodRegister(compilationUnit).run();
				}
			}
		}
	}
	
	static public class NoWithSuffixMethodRegister extends UnitTreeVisitor {

		StringBuilder sb = new StringBuilder();
		private Map<String, String> map;
		private TypeUtil typeUtil;

		public NoWithSuffixMethodRegister(CompilationUnit unit) {
			super(unit);
			this.typeUtil = unit.getEnv()
					.typeUtil();
			map = options.getMappings()
					.getMethodMappings();
		}

		@Override
		public void endVisit(MethodDeclaration node) {
			ExecutableElement methodElement = node.getExecutableElement();
			if (!ElementUtil.isPublic(methodElement)) {
				return;
			}
			if (node.getParameters().size() == 0) {
				return;
			}

			String key = Mappings.getMethodKey(node.getExecutableElement(), typeUtil);
			int cntParam = node.getParameters().size();

			sb.setLength(0);
			sb.append(methodElement.getSimpleName().toString());
			if (cntParam > 1) {
				SingleVariableDeclaration first_p = node.getParameter(0);
				Type type = first_p.getType();
				if (type.isPrimitiveType()) {
					boolean all_type_matched = true;
					for (int i = 1; i < cntParam; i++) {
						SingleVariableDeclaration p = node.getParameter(i);
						if (p.getType().equals(first_p)) {
							all_type_matched = false;
							break;
						}
					}
					if (all_type_matched) {
						if (node.isConstructor()) {
							sb.setLength(0);
							sb.append("init");
						}
						sb.append('_');
						String n = first_p.getVariableElement().getSimpleName().toString();
						sb.append(n);
					}
				}
			}

			sb.append(':');
			for (int i = 1; i < cntParam; i++) {
				String n = node.getParameter(i).getVariableElement().getSimpleName().toString();
				sb.append(n);
				sb.append(':');
			}
			map.put(key, sb.toString());
		}

		@Override
		public void endVisit(TypeDeclaration node) {
			visitType(node);
		}

		@Override
		public void endVisit(EnumDeclaration node) {
			visitType(node);
		}

		@Override
		public void endVisit(AnnotationTypeDeclaration node) {
			visitType(node);
		}

		private void visitType(AbstractTypeDeclaration node) {
			addReturnTypeNarrowingDeclarations(node);
		}

		// Adds declarations for any methods where the known return type is more
		// specific than what is already declared in inherited types.
		private void addReturnTypeNarrowingDeclarations(AbstractTypeDeclaration node) {
		}
	}

	public static void preprocessUnit(CompilationUnit unit) {
		for (AbstractTypeDeclaration type : unit.getTypes()) {
			types.put(type.getName().toString(), type);
			if (type.getClassInitStatements().isEmpty()) {
				
			}
		}
		preprocessUnreachableImportedClasses(unit, new HashMap<>());
//		for (AbstractTypeDeclaration _t : unit.getTypes()) {
//		}
	}

//	private static boolean resolveStaticInitializer(AbstractTypeDeclaration _t) {
//		TypeElement type = _t.getTypeElement();
//		boolean hasStaticInitializer = false;
//        for (TypeMirror inheritedType : TypeUtil.directSupertypes(type.asType())) {
//            String name = inheritedType.toString();
//            int idx = name.indexOf('<');
//            if (idx > 0) {
//            	name = name.substring(0, idx);
//            }
//    		AbstractTypeDeclaration superType = types.get(name);
//    		if (superType != null) {
//    			hasStaticInitializer |= resolveStaticInitializer(superType);
//    		}
//			hasStaticInitializer |= types.get(superType);
//        }
//	}
	
	private static HashMap<String, String> preprocessUnreachableImportedClasses(CompilationUnit unit, HashMap<String, String> processed) {
		HashMap<String, String> urMap = unit.getUnreachableImportedClasses();
		if (processed.containsKey(unit.getSourceFilePath())) {
			return urMap;
		}
		processed.put(unit.getSourceFilePath(), unit.getSourceFilePath());
		for (AbstractTypeDeclaration _t : unit.getTypes()) {
			TypeElement type = _t.getTypeElement();
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

