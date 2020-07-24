package com.google.devtools.j2objc.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.javac.ImportManager;
import com.strobel.assembler.metadata.IMetadataResolver;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

public class SourceStore { 

	private String root;
	private Options options;
	private JarTypeLoader currTypeLoader;
	private HashSet<String> pathSet = new HashSet<>();
	private File jarFile;
	private static HashSet<String> rootPaths = new HashSet<>();
  private static HashMap<String, InputFile> inputFileMap = new HashMap<>(); 

	public SourceStore(Options options) {
		this.options = options;
	}

  private static InputFile registerInputFile(InputFile file) {
    InputFile old = inputFileMap.put(file.getUnitName(), file);
    return old;
  }
  
  public static InputFile getInputFile(String unitPath) {
    return inputFileMap.get(unitPath);
  }
  
	public static String addRootPath(File f) {
    String root = getCanonicalPath(f);
		rootPaths.add(root + '/');
		return root;
	}

  public static String getCanonicalPath(File f) {
    try {
      return f.getCanonicalPath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


	public boolean addSource(String filename) {
		this.root = "";
		File f = new File(filename);
		if (f.exists()) {
			filename = getCanonicalPath(f);
			for (String s : rootPaths) {
				if (filename.startsWith(s)) {
					this.root = s;
					break;
				}
			}
		}
		else {
			if (filename.charAt(0) == '!') {
				File lstf = new File(filename.substring(1));
				ArrayList<String> files = SourceStore.readPathList(lstf);
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
					addRootPath(new File(root));
					f = new File(absPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!f.exists()) {
				ErrorUtil.warning("Invalid source: " + filename);
				System.exit(-1);
				return false;
			}
		}
		
		if (!pathSet.add(getCanonicalPath(f))) {
			return false;
		}
		if (f.isDirectory()) {
			this.addFolderTree(f);
		}
		else if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
			this.pathSet.add(getCanonicalPath(f));
			File tempDir = extractSources(f, options, true);
			options.fileUtil().appendSourcePath(getCanonicalPath(tempDir));
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
    ArrayList<InputFile> inputFiles = new ArrayList<>();
    for (InputFile f : inputFileMap.values()) {
      inputFiles.add(f);
    }
	  Collections.sort(inputFiles, new Comparator<InputFile>() {

      @Override
      public int compare(InputFile o1, InputFile o2) {
        // TODO Auto-generated method stub
        return o1.getUnitName().compareTo(o2.getUnitName());
      }
	    
	  });
		return inputFiles;
	}

	private boolean registerSource(File src_file) {
		String filename = getCanonicalPath(src_file);
		filename = filename.substring(root.length());
		
		if (!ImportManager.canImportClass(filename)) {
			return false;
		}
		
    InputFile f = new RegularInputFile(root + filename, filename);
    InputFile old = registerInputFile(f);
		
    if (old != null) {
			System.out.println("Warning! Source is replaced.");
      System.out.println("  " + old.getAbsolutePath() + " -> " + root + filename);
		}
		
		//inputFiles.add(f);
		
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
			String filepath = getCanonicalPath(f);
			String source = doSaveClassDecompiled(f);
			if (source == null) return;

			filepath = filepath.substring(0, filepath.length() - 5) + "java";
			try {
				PrintStream out = new PrintStream(new FileOutputStream(filepath));
				out.println(source);
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			registerSource(new File(filepath));
		}
		else {
			File dir = options.fileUtil().getResourceDirectory();
			if (dir != null) {
		    // copy resource files into the specified resource directory.
				String filename = getCanonicalPath(f);
				filename = filename.substring(root.length());
				File of = new File(dir.getAbsolutePath() + "/" + filename);
				of.getParentFile().mkdirs();
				try {
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(of));
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
					for (int c; (c = in.read()) >= 0; ) {
						out.write(c);
					}
					out.flush();
					out.close();
					in.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
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
		String filepath = getCanonicalPath(inFile);
		String classsig = filepath.substring(root.length(), filepath.length() - 6);

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
		root = addRootPath(f) + '/';
		this.addFolder(f);
	}

  public static ArrayList<String> readPathList(File lstf) {
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
          if (!ImportManager.canImportClass(internalPath)) {
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
  
  	
}