package com.google.devtools.j2objc.argc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.jar.JarFile;

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.strobel.assembler.metadata.IMetadataResolver;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

public class TranslateSourceList { 

	private String root;
	private Options options;
	private JarTypeLoader currTypeLoader;
	private ArrayList<InputFile> pureObjCFiles = new ArrayList<>();
	private HashSet<String> pathSet = new HashSet<>();
	private File jarFile;
	private ArrayList<InputFile> inputFiles = new ArrayList<>();
	static HashSet<String> rootPaths = new HashSet<>();


	public TranslateSourceList(Options options) {
		this.options = options;
	}

	public static void addRootPath(String root) {
		if (root.charAt(root.length() - 1) != '/') {
			root += '/';
		}
		rootPaths.add(root);
	}


	public boolean addSource(String filename) {
		this.root = "";
		File f = new File(filename);
		if (f.exists()) {
			filename = ARGC.getCanonicalPath(f);
			for (String s : rootPaths) {
				if (filename.startsWith(s)) {
					this.root = s;
					break;
				}
			}
		}
		else {
			if (filename.charAt(0) == '@') {
				File lstf = new File(filename.substring(1));
				ArrayList<String> files = ARGC.processListFile(lstf);
				if (files != null) {
					if (lstf.getName().charAt(0) == '.') {
						lstf = new File("j2objc compatible mode - PWD");
					}
					String dir = lstf.getAbsolutePath();
					dir = dir.substring(0, dir.lastIndexOf('/') + 1);
					for (String s : files) {
						if (!s.startsWith(dir)) {
							s = dir + s;
						}
						this.addSource(s);
					}
					return true;
				}
			}
			
			try {
				InputFile inp = options.fileUtil().findFileOnSourcePath(filename);
				if (inp != null) {
					String absPath = inp.getAbsolutePath();
					root = absPath.substring(0, absPath.length() - filename.length());
					addRootPath(root);
					f = new File(absPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!f.exists()) {
				ErrorUtil.warning("Invalid source: " + filename);
				//new RuntimeException("---").printStackTrace();
				System.exit(-1);
				return false;
			}
		}
		
		if (!pathSet.add(ARGC.getCanonicalPath(f))) {
			return false;
		}
		if (f.isDirectory()) {
			this.addFolderTree(f);
		}
		else if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
			this.pathSet.add(ARGC.getCanonicalPath(f));
			File tempDir = ARGC.extractSources(f, options);
			options.fileUtil().appendSourcePath(ARGC.getCanonicalPath(tempDir));
			this.metadataSystem = null;
			this.jarFile = f; 
			this.addFolderTree(tempDir);
		}
		else {
			this.registerSource(f);
		}
		return true;
	}
	
	public ArrayList<InputFile> getInputFiles() {
		return inputFiles;
	}

	private boolean registerSource(File src_file) {
		String filename = ARGC.getCanonicalPath(src_file);
		filename = filename.substring(root.length());
		
		if (ARGC.isExcludedClass(filename)) {
			return false;
		}
		
		InputFile f = InputFile.getInputFile(filename);
		
		if (filename.contains("SQLiteJDBCLoader")) {
			ARGC.trap();
		}
		if (f == null) {
			f = new RegularInputFile(root + filename, filename);
		}
		else if (inputFiles.indexOf(f) >= 0) {
			System.out.println("Warning! Source is replaced.");
			System.out.println("  -- " + root + filename);
			System.out.println("  ++ " + f.getAbsolutePath());
			return false;
		}
		
		inputFiles.add(f);
		
		if (ARGC.isPureObjC(filename)) {
			pureObjCFiles.add(f);
		}
		
		return true;
	}
	
	private void add(File f)  {
		if (f.isDirectory()) {
			addFolder(f);
		}
		else if (f.getName().endsWith(".java")) {
			registerSource(f);
		}
		else if (options.translateClassfiles() && f.getName().endsWith(".class")) {
			String filepath = ARGC.getCanonicalPath(f);
			String source = doSaveClassDecompiled(f);
			if (source == null) return;

			filepath = filepath.substring(0, filepath.length() - 5) + "java";
			System.out.println("discompiled: " + filepath);
			System.out.println(source);
			try {
				PrintStream out = new PrintStream(new FileOutputStream(filepath));
				out.println(source);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			registerSource(new File(filepath));
		}


	}

	MetadataSystem metadataSystem;
	private TypeReference lookupType(String path) {
		/* Hack to get around classes whose descriptors clash with primitive types. */
		if (metadataSystem == null) {
			try {
				this.currTypeLoader = new JarTypeLoader(new JarFile(jarFile));
				this.metadataSystem = new MetadataSystem(currTypeLoader);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (path.length() == 1) {
			MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
			return metadataSystem.resolve(parser.parseTypeDescriptor(path));
		}
		return metadataSystem.lookupType(path);
	}

	private String doSaveClassDecompiled(File inFile) {
		//			      List<File> classPath = new ArrayList<>();
		//			      classPath.add(new File(rootPath));
		//			      parserEnv.fileManager().setLocation(StandardLocation.CLASS_PATH, classPath);				  

		String filepath = ARGC.getCanonicalPath(inFile);
		String classsig = filepath.substring(root.length(), filepath.length() - 6);

		if (classsig.endsWith("JFlexLexer")) {//$ZzFlexStreamInfo")) {
			int a = 3;
			a ++;
		}
		TypeReference typeRef = lookupType(classsig); 
		if (typeRef.getDeclaringType() != null) {
			return null;
		}
		TypeDefinition resolvedType = null;
		if (typeRef == null || ((resolvedType = typeRef.resolve()) == null)) {
			throw new RuntimeException("Unable to resolve type.");
		}
		DecompilerSettings settings = DecompilerSettings.javaDefaults();
		settings.setForceExplicitImports(true);
		settings.setShowSyntheticMembers(true);
		StringWriter stringwriter = new StringWriter();
		DecompilationOptions decompilationOptions;
		decompilationOptions = new DecompilationOptions();
		decompilationOptions.setSettings(settings);
		decompilationOptions.setFullDecompilation(false);
		PlainTextOutput plainTextOutput = new PlainTextOutput(stringwriter);
		plainTextOutput.setUnicodeOutputEnabled(
				decompilationOptions.getSettings().isUnicodeOutputEnabled());
		settings.getLanguage().decompileType(resolvedType, plainTextOutput,
				decompilationOptions);
		String decompiledSource = stringwriter.toString();
		//System.out.println(decompiledSource);
		return decompiledSource;
		//		            if (decompiledSource.contains(textField.getText().toLowerCase())) {
		//		                addClassName(entry.getName());
		//		            }


	}

	private void addFolder(File f)  {
		File files[] = f.listFiles();
		for (File f2 : files) {
			add(f2);
		}
	}

	private void addFolderTree(File f) {
		if (options.fileUtil().getSourcePathEntries().indexOf(f.getAbsolutePath()) < 0) {
			options.fileUtil().getSourcePathEntries().add(f.getAbsolutePath());
		}

		//options.getHeaderMap().setOutputStyle(HeaderMap.OutputStyleOption.SOURCE);
		root = ARGC.getCanonicalPath(f) + '/';
		addRootPath(root);
		this.addFolder(f);
	}

}