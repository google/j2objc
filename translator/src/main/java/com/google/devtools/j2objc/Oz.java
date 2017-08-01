package com.google.devtools.j2objc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.dom.ITypeBinding;

import com.google.common.collect.Lists;
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
import com.google.devtools.j2objc.pipeline.GenerationBatch.Oz_InputFile;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.HeaderMap;
import com.google.devtools.j2objc.util.Mappings;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.TypeUtil;

public class Oz {
	public final boolean NO_FINALLY = true;
	// private int inTryDepth;
	private int inFinallyDepth;
	private int[] finallyBlockCounts = new int[1024];
	private String currentRetType;
	private NameTable nameTable;
	private String ctx$;
	private boolean hasFinally;

	private String getTryContext() {
		return "z_finallyInfo" + inFinallyDepth;
	}

	public void setCurrentReturnType(NameTable nameTable, String returnType) {
		this.nameTable = nameTable;
		if (returnType != null) {
			if ("void".equals(returnType)) {
				returnType = null;
			} else if (returnType.startsWith("const")) {
				int a = 3;
				a++;
			}
		}
		this.currentRetType = returnType;
		for (int i = 0; finallyBlockCounts[i] != 0; i++) {
			finallyBlockCounts[i] = 0;
		}
	}

	public void initFinallyInfo(SourceBuilder buffer) {
		// if (inFinallyDepth == 0 && finallyBlockCounts[0] == 0) {
		// buffer.append("NSException* jpp_finallyInfo_thrown_ex = nil;\n");
		// buffer.append("bool jpp_finallyInfo_hasReturn = false;\n");
		// if (currentRetType != null) {
		// buffer.append(currentRetType + " jpp_finallyInfo_retValue = 0;\n");
		// }
		// }
		finallyBlockCounts[inFinallyDepth]++;
		inFinallyDepth++;
		updateContext$();
	}

	private void updateContext$() {
		int cntFinally = inFinallyDepth == 0 ? 0 : finallyBlockCounts[inFinallyDepth - 1];
		this.ctx$ = "z_finallyInfo" + inFinallyDepth + "_" + cntFinally;
	}

	private String getContext$() {
		int cntFinally = inFinallyDepth == 0 ? 0 : finallyBlockCounts[inFinallyDepth - 1];
		return inFinallyDepth + "_" + cntFinally;
	}

	public void enterFinallyBlock(SourceBuilder buffer, boolean catchedAll) {
		if (!catchedAll) {
			buffer.append("JPP_START_FINALLY_EX(" + getContext$() + ") {\n");
		} else {
			buffer.append("JPP_START_FINALLY(" + getContext$() + ") {\n");
		}
		/*
		 * if (!catchedAll) { buffer.append("@catch (NSException* t) {\n");
		 * buffer.append("\tjpp_finallyInfo_thrown_ex = t;\n");
		 * buffer.append("}\n"); } buffer.append(ctx$ + "_finally: {\n");
		 */
		inFinallyDepth--;
		updateContext$();
	}

	public void leaveFinallyBlock(SourceBuilder buffer) {
		if (inFinallyDepth > 0) {
			buffer.append("JPP_END_FINALLY_EX(" + getContext$() + ")\n}\n");
		} else if (currentRetType != null) {
			buffer.append("JPP_END_FINALLY()\n}\n");
		} else {
			buffer.append("JPP_END_FINALLY_void()\n}\n");
		}
		//
		//
		//
		// buffer.append("if (jpp_finallyInfo_thrown_ex != nil) {\n");
		// if (inFinallyDepth > 0) {
		// buffer.append("\tgoto " + ctx$ + "_finally;\n");
		// }
		// else {
		// buffer.append("\t@throw jpp_finallyInfo_thrown_ex;\n");
		// }
		// buffer.append("}\n");
		// buffer.append("else if (jpp_finallyInfo_hasReturn) {\n");
		// if (inFinallyDepth > 0) {
		// buffer.append("\tgoto " + ctx$ + "_finally;\n");
		// }
		// else {
		// if (currentRetType != null) {
		// buffer.append("\treturn jpp_finallyInfo_retValue;\n");
		// }
		// else {
		// buffer.append("\treturn;\n");
		// }
		// }
		// buffer.append("}\n");
		//
		// buffer.append("}\n");
	}

	public boolean inFinallyBlock() {
		return this.inFinallyDepth > 0;
	}

	public void generateReturnStatement(SourceBuilder buffer, StatementGenerator statementGenerator,
			Expression expression) {
		if (currentRetType != null) {
			buffer.append("JPP_RETURN_IN_FINALLY(" + getContext$() + ", ");
			expression.accept(statementGenerator);
			buffer.append(");\n");
		} else {
			buffer.append("JPP_RETURN_IN_FINALLY_void(" + getContext$() + ");\n");
		}
		/*
		 * if (currentRetType != null) { buffer.append("{\n");
		 * buffer.append("\tjpp_finallyInfo_hasReturn = true;\n");
		 * buffer.append("\tjpp_finallyInfo_retValue = "); } else {
		 * buffer.append("// "); }
		 */
	}

	// public void leaveReaturnStatement(SourceBuilder buffer) {
	// buffer.append("\tgoto " + ctx$ + "_finally;\n");
	// if (currentRetType != null) {
	// buffer.append("}\n");
	// }
	// }

	public void generateThrowStatement(SourceBuilder buffer, StatementGenerator statementGenerator,
			Expression expression) {
		buffer.append("JPP_THROW_IN_FINALLY(" + getContext$() + ", ");
		expression.accept(statementGenerator);
		buffer.append(");\n");
		//
		// buffer.append("{\n");
		//
		//
		// buffer.append("{\n");
		// buffer.append("\tjpp_finallyInfo_thrown_ex = ");
	}

	// public void leaveThrowStatement(SourceBuilder buffer) {
	// buffer.append("\tgoto " + ctx$ + "_finally;\n");
	// buffer.append("}\n");
	// }

	public boolean generateCatchStatement(SourceBuilder buffer, CatchClause cc) {
		SingleVariableDeclaration ex = cc.getException();
		String name = ex.getVariableElement().getSimpleName().toString();
		buffer.append("JPP_CATCH_IN_FINALLY(" + name + ");\n");
		String s = nameTable.getObjCType(cc.getException().getType().getTypeMirror());// .toString();
		return ("NSException*".equals(s) || "NSException *".equals(s));
	}

	public void markHasFinallyStratement() {
		this.hasFinally = true;
	}

	public boolean clearHasFinallyStratement() {
		boolean hasFinally = this.hasFinally;
		this.hasFinally = false;
		return hasFinally;
	}

	public String getFinallyContextInitializeStatement() {
		if (currentRetType != null) {
			return "JPP_INIT_FINALLY_CONTEXT_EX(" + currentRetType + ");\n";
		} else {
			return "JPP_INIT_FINALLY_CONTEXT();\n";
		}
		/*
		 * String s = "NSException* jpp_finallyInfo_thrown_ex = nil;\n" +
		 * "bool jpp_finallyInfo_hasReturn = false;\n"; if (currentRetType !=
		 * null) { s += currentRetType + " jpp_finallyInfo_retValue = 0;\n"; }
		 * return s;
		 */
	}

	static boolean zee_inCleanObjCMode;

	public static boolean inPureObjCMode() {
		return zee_inCleanObjCMode;
	}

	public static boolean setPureObjCMode(boolean bClean) {
		boolean old_v = zee_inCleanObjCMode;
		zee_inCleanObjCMode = bClean;
		return old_v;
	}

	private static ArrayList<String> pureObjCMap = new ArrayList<String>();

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

		//System.out.println("gen " + file.getUnitName());
		CompilationUnit unit = parser.parse(file);
		new NoWithSuffixMethodRegister(unit, options).run();
	}

//	public static void processAutoMethodMapRegister(InputFile file, CompilationUnit unit, Options options) {
//		if (!isPureObjC(file.getUnitName())) {
//			return;
//		}
//		new NoWithSuffixMethodRegister(unit, options).run();
//	}
	
	static public class NoWithSuffixMethodRegister extends UnitTreeVisitor {

		StringBuilder sb = new StringBuilder();
		private Map<String, String> map;
		private Options options;
		private TypeUtil typeUtil;

		public NoWithSuffixMethodRegister(CompilationUnit unit, Options options) {
			super(unit);
			this.options = options;
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

			// JDT only adds the abstract bit to a MethodDeclaration node's
			// modifiers if the abstract
			// method is from a class. Since we want our code generator to go
			// over an interface's
			// method nodes for default method support and skip abstract
			// methods, we add the bit if the
			// method is from an interface.
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
			//System.out.println(key + " -> " + sb);
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

	public static void initOutputMode(InputFile file) {
		zee_inCleanObjCMode = isPureObjC(file.getUnitName());
		if (zee_inCleanObjCMode) {
			//System.out.println(file.getUnitName());
		}

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

	
//	public static boolean isPureObjC(ElementUtil util, TypeElement type) {
//		String s = util.getBinaryName(type);
//		boolean res = isPureObjC(s);
//		return res;
//	}
	
	public static boolean isPureObjC(TypeMirror type) {
		String s = type.toString().replace('.', '/');// TypeUtil.getBinaryName(type).substring(1);
		boolean res = isPureObjC(s);
		return res;
	}

//	public static boolean isNativeEnum(TypeMirror type) {
//		if (type.isEnum()) {
//			String s = type.TypeUtil.getBinaryName(type).substring(1);
//			boolean res = isPureObjC(s);
//			if (res) {
//				int a = 3;
//				a ++;
//			}
//			return res;
//		}
//		else {
//			return false;
//		}
//	}
	
	public static boolean isNativeEnum(ITypeBinding type) {
		if (type.isEnum()) {
			String s = type.getBinaryName().replace('.', '/');
			boolean res = isPureObjC(s);
			if (res) {
				int a = 3;
				a ++;
			}
			return res;
		}
		else {
			return false;
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
				// System.out.println(this.fsPath + "!" + unitPath);
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
					Oz.processAutoMethodMapRegister(parser, new RegularInputFile(filepath, filename), options);
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

