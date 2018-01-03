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

package com.google.devtools.j2objc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

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
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
//import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.HeaderMap;
import com.google.devtools.j2objc.util.Mappings;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.TypeUtil;

public class ARGC {
	public static boolean compatiable_2_0_2 = false;
	private static boolean isPureObjCGenerationMode;
	private static ArrayList<String> pureObjCMap = new ArrayList<String>();


	public static boolean inPureObjCMode() {
		return isPureObjCGenerationMode;
	}

	public static void endSourceFileGeneration() {
		isPureObjCGenerationMode = false;
	}

	public static void addPureObjC(String arg) {
		arg = arg.replace('.', '/');
		if (pureObjCMap.indexOf(arg) < 0) {
			pureObjCMap.add(arg);
		}
	}

	public static void processAutoMethodMapRegister(Parser parser, InputFile file, Options options) {
		if (!isPureObjC(file.getUnitName())) {
			return;
		}

		CompilationUnit unit = parser.parse(file);
		new NoWithSuffixMethodRegister(unit).run();
	}

	static public class NoWithSuffixMethodRegister extends UnitTreeVisitor {

		StringBuilder sb = new StringBuilder();
		private Map<String, String> map;
		private TypeUtil typeUtil;

		public NoWithSuffixMethodRegister(CompilationUnit unit) {
			super(unit);
			this.typeUtil = unit.getEnv().typeUtil();
			map = options.getMappings().getMethodMappings();
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

	public static void startSourceFileGeneration(InputFile file) {
		isPureObjCGenerationMode = isPureObjC(file.getUnitName());
	}

	private static boolean isPureObjC(String path) {
		int p = path.lastIndexOf('/');
		if (p < 0) {
			return false;
		}
		path = path.substring(0, p);
		return isPureObjFolder(path);
	}

	private static boolean isPureObjFolder(String path) {
		for (String s : pureObjCMap) {
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

	
	
	public static class SourceList extends ArrayList<String> {
		
		private String root;
		private Options options;

		public SourceList(Options options) {
			this.options = options;
		}

		public boolean add(String filename) {
			File f = new File(filename);
			if (f.isDirectory()) {
				options.getHeaderMap().setOutputStyle(HeaderMap.OutputStyleOption.SOURCE);
				root = f.getAbsolutePath() + '/';
				this.addFolder(f);
			}
			else {
				this.addSource(filename);
			}
			return true;
		}
		
		void addSource(String filename) {
			super.add(filename);
		}

		private void addSource(File f) {
			if (f.isDirectory()) {
				addFolder(f);
			}
			else if (f.getName().endsWith(".java")) {
				String filepath = f.getAbsolutePath();
				String filename = filepath.substring(root.length());
				this.addSource(filename);
			}
		}
	
		private void addFolder(File f) {
			File files[] = f.listFiles();
			for (File f2 : files) {
				addSource(f2);
			}
		}
	
	    

		public class Oz_InputFile implements InputFile {
			private final String path, fsPath, unitPath;
	
			public Oz_InputFile(String fsPath, String path0) {
				this.fsPath = fsPath;
				this.path = path0.replace('\\', '/');
				this.unitPath = path.substring(fsPath.length() + 1);
			}
	
			@Override
			public boolean exists() {
				return new File(path).exists();
			}
	
			@Override
			public InputStream getInputStream() throws IOException {
				return new FileInputStream(new File(path));
			}
	
			@Override
			public Reader openReader(Charset charset) throws IOException {
				return new InputStreamReader(getInputStream(), charset);
			}
	
			public String getPath() {
				return path;
			}
	
			public String getContainingPath() {
				return fsPath;
			}
	
			@Override
			public String getUnitName() {
				return unitPath;
			}
	
			@Override
			public String getBasename() {
				return unitPath.substring(unitPath.lastIndexOf('/') + 1);
			}
	
			@Override
			public long lastModified() {
				return new File(path).lastModified();
			}
	
			@Override
			public String toString() {
				return getPath();
			}
	
			@Override
			public String getAbsolutePath() {
				return path;
			}
	
			@Override
			public String getOriginalLocation() {
				// TODO Auto-generated method stub
				return null;
			}
	
		}

	}

	public static class Preprocessor  {
		
		private String root;
		private com.google.devtools.j2objc.util.Parser parser;
		private Options options;
		int inPureObjC; 

		public Preprocessor(com.google.devtools.j2objc.util.Parser parser, Options options) {
			this.parser = parser;
			this.options = options;
		}
		
		private void addFolder(File f) {
			File files[] = f.listFiles();
			for (File f2 : files) {
				addSource(f2);
			}
		}
		
		private void addSource(File f) {
			String filepath = f.getAbsolutePath();
			String filename = filepath.substring(root.length());
			if (f.isDirectory()) {
				boolean isPureObjFolder = isPureObjFolder(filename);
				if (isPureObjFolder) {
					inPureObjC ++;
					addFolder(f);
					inPureObjC --;
				}
				else {
					addFolder(f);
				}
			}
			else if (f.getName().endsWith(".java")) {
				if (root == null || inPureObjC > 0) {
					ARGC.processAutoMethodMapRegister(parser, new RegularInputFile(filepath, filename), options);
				}
			}
		}
	
		public void preprocess(List<String> srcArgs) {
	  		for (String filename : srcArgs) {
				File f = new File(filename);
				if (f.isDirectory()) {
					root = f.getAbsolutePath() + '/';
					inPureObjC = 0;
					this.addFolder(f);
				}
				else {
					root = "";
					inPureObjC = 1;
					addSource(f);
				}
	  		}
		}
	}

}

