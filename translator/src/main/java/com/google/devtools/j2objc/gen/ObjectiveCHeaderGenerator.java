/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.gen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.ImportCollector;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates Objective-C header files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCHeaderGenerator extends ObjectiveCSourceFileGenerator {

  private static final String DEPRECATED_ATTRIBUTE = "__attribute__((deprecated))";

  /**
   * Generate an Objective-C header file for each type declared in a specified
   * compilation unit.
   */
  public static void generate(String fileName, String source, CompilationUnit unit) {
    ObjectiveCHeaderGenerator headerGenerator =
        new ObjectiveCHeaderGenerator(fileName, source, unit);
    headerGenerator.generate(unit);
  }

  private ObjectiveCHeaderGenerator(String fileName, String source, CompilationUnit unit) {
    super(fileName, source, unit, false);
  }

  @Override
  protected String getSuffix() {
    return ".h";
  }

  public void generate(CompilationUnit unit) {
    println(J2ObjC.getFileHeader(getSourceFileName()));

    @SuppressWarnings("unchecked")
    List<AbstractTypeDeclaration> types = unit.types(); // safe by definition
    Set<ITypeBinding> moreForwardTypes = sortTypes(types);
    printImportsAndForwardReferences(unit, moreForwardTypes);

    for (AbstractTypeDeclaration type : types) {
      newline();
      generate(type);
    }
    save(unit);
  }

  @Override
  public void generate(TypeDeclaration node) {
    String typeName = NameTable.getFullName(node);
    String superName = NameTable.getSuperClassName(node);
    List<FieldDeclaration> fields = Lists.newArrayList(node.getFields());
    List<MethodDeclaration> methods = Lists.newArrayList(node.getMethods());
    boolean isInterface = node.isInterface();

    printConstantDefines(node);

    if (Options.generateDeprecatedDeclarations()) {
      @SuppressWarnings("unchecked")
      List<IExtendedModifier> modifiers = node.modifiers();
      if (hasDeprecated(modifiers)) {
        println(DEPRECATED_ATTRIBUTE);
      }
    }

    if (isInterface) {
      printf("@protocol %s", typeName);
    } else {
      printf("@interface %s : %s", typeName, superName);
    }
    @SuppressWarnings("unchecked")
    List<Type> interfaces = node.superInterfaceTypes(); // safe by definition
    if (!interfaces.isEmpty()) {
      print(" < ");
      for (Iterator<Type> iterator = interfaces.iterator(); iterator.hasNext();) {
        print(NameTable.javaTypeToObjC(iterator.next(), true));
        if (iterator.hasNext()) {
          print(", ");
        }
      }
      print(isInterface ? ", NSObject >" : " >");
    } else if (isInterface) {
      print(" < NSObject >");
    }
    if (isInterface) {
      newline();
    } else {
      println(" {");
      printInstanceVariables(fields);
      println("}\n");
      printProperties(fields);
      printStaticFieldAccessors(fields, methods, isInterface);
    }
    printMethods(methods);
    println("@end");

    if (isInterface) {
      printStaticInterface(typeName, fields, methods);
    }

    ITypeBinding binding = Types.getTypeBinding(node);
    String pkg = binding.getPackage().getName();
    if (NameTable.hasPrefix(pkg) && binding.isTopLevel()) {
      String unprefixedName = NameTable.camelCaseQualifiedName(binding.getQualifiedName());
      if (binding.isInterface()) {
        // Protocols can't be used in typedefs.
        printf("\n#define %s %s\n", unprefixedName, typeName);
      } else {
        printf("\ntypedef %s %s;\n", typeName, unprefixedName);
      }
    }
    printExternalNativeMethodCategory(node, typeName);
  }

  @Override
  protected void generate(AnnotationTypeDeclaration node) {
    String typeName = NameTable.getFullName(node);
    printf("@protocol %s < NSObject >\n@end\n", typeName);
  }

  private void printExternalNativeMethodCategory(TypeDeclaration node, String typeName) {
    final List<MethodDeclaration> externalMethods = Lists.newArrayList();
    node.accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if ((node.getModifiers() & Modifier.NATIVE) > 0 && !hasNativeCode(node)) {
          externalMethods.add(node);
        }
      }
    });
    if (!externalMethods.isEmpty()) {
      printf("\n@interface %s (NativeMethods)\n", typeName);
      for (MethodDeclaration m : externalMethods) {
        print(super.methodDeclaration(m));
        println(";");
      }
      println("@end");
    }
  }

  private void printStaticInterface(
      String typeName, List<FieldDeclaration> fields, List<MethodDeclaration> methods) {
    // Print @interface for static constants, if any.
    List<IVariableBinding> staticFields =
        getStaticFieldsNeedingAccessors(fields, /* isInterface */ true);
    if (staticFields.isEmpty()) {
      return;
    }
    printf("\n@interface %s : NSObject {\n}\n", typeName);
    printStaticFieldAccessors(staticFields, methods);
    println("@end");
  }

  @Override
  protected void generate(EnumDeclaration node) {
    printConstantDefines(node);
    String typeName = NameTable.getFullName(node);
    @SuppressWarnings("unchecked")
    List<EnumConstantDeclaration> constants = node.enumConstants();

    // C doesn't allow empty enum declarations.  Java does, so we skip the
    // C enum declaration and generate the type declaration.
    if (!constants.isEmpty()) {
      println("typedef enum {");

      // Strip enum type suffix.
      String bareTypeName = typeName.endsWith("Enum") ?
          typeName.substring(0, typeName.length() - 4) : typeName;

      // Print C enum typedef.
      indent();
      int ordinal = 0;
      for (EnumConstantDeclaration constant : constants) {
        printIndent();
        printf("%s_%s = %d,\n", bareTypeName, constant.getName().getIdentifier(), ordinal++);
      }
      unindent();
      printf("} %s;\n\n", bareTypeName);
    }

    List<FieldDeclaration> fields = Lists.newArrayList();
    List<MethodDeclaration> methods = Lists.newArrayList();
    for (Object decl : node.bodyDeclarations()) {
      if (decl instanceof FieldDeclaration) {
        fields.add((FieldDeclaration) decl);
      } else if (decl instanceof MethodDeclaration) {
        methods.add((MethodDeclaration) decl);
      }
    }

    // Print enum type.
    printf("@interface %s : JavaLangEnum < NSCopying", typeName);
    ITypeBinding enumType = Types.getTypeBinding(node);
    for (ITypeBinding intrface : enumType.getInterfaces()) {
      if (!intrface.getName().equals(("Cloneable"))) { // Cloneable handled below.
        printf(", %s", NameTable.getFullName(intrface));
      }
    }
    println(" > {");
    printInstanceVariables(fields);
    println("}");
    printProperties(fields);
    for (EnumConstantDeclaration constant : constants) {
      printf("+ (%s *)%s;\n", typeName, NameTable.getName(constant.getName()));
    }
    println("+ (IOSObjectArray *)values;");
    printf("+ (%s *)valueOfWithNSString:(NSString *)name;\n", typeName);
    println("- (id)copyWithZone:(NSZone *)zone;");
    printStaticFieldAccessors(fields, methods, /* isInterface */ false);
    printMethods(methods);
    println("@end");
  }

  @Override
  protected void printStaticFieldGetter(IVariableBinding var) {
    printf(staticFieldGetterSignature(var) + ";\n");
  }

  @Override
  protected void printStaticFieldReferenceGetter(IVariableBinding var) {
    printf(staticFieldReferenceGetterSignature(var) + ";\n");
  }

  @Override
  protected void printStaticFieldSetter(IVariableBinding var) {
    printf(staticFieldSetterSignature(var) + ";\n");
  }

  @Override
  protected String methodDeclaration(MethodDeclaration m) {
    if ((m.getModifiers() & Modifier.NATIVE) > 0 && !hasNativeCode(m)) {
      return "";
    }
    String result = super.methodDeclaration(m);

    if (Options.generateDeprecatedDeclarations()) {
      @SuppressWarnings("unchecked")
      List<IExtendedModifier> modifiers = m.modifiers();
      if (hasDeprecated(modifiers)) {
        result += " " + DEPRECATED_ATTRIBUTE;
      }
    }

    return result + ";\n";
  }

  @Override
  protected String mappedMethodDeclaration(MethodDeclaration method, IOSMethod mappedMethod) {
    return super.mappedMethodDeclaration(method, mappedMethod) + ";\n";
  }

  @Override
  protected String constructorDeclaration(MethodDeclaration m) {
    return super.constructorDeclaration(m) + ";\n";
  }

  @Override
  protected void printStaticConstructorDeclaration(MethodDeclaration m) {
    // Don't do anything.
  }

  @Override
  protected void printMethod(MethodDeclaration m) {
    IMethodBinding binding = Types.getMethodBinding(m);
    if (!binding.isSynthetic()) {
      super.printMethod(m);
    }
  }

  private void printImportsAndForwardReferences(CompilationUnit unit, Set<ITypeBinding> forwards) {
    HeaderImportCollector collector = new HeaderImportCollector();
    collector.collect(unit, getSourceFileName());
    Set<ImportCollector.Import> imports = collector.getImports();
    Set<ImportCollector.Import> superTypes = collector.getSuperTypes();

    // Print forward declarations.
    Set<String> forwardStmts = Sets.newTreeSet();
    for (ImportCollector.Import imp : imports) {
      forwardStmts.add(createForwardDeclaration(imp.getTypeName(), imp.isInterface()));
    }
    for (ITypeBinding forward : forwards) {
      forwardStmts.add(
          createForwardDeclaration(NameTable.getFullName(forward), forward.isInterface()));
    }
    if (!forwardStmts.isEmpty()) {
      for (String stmt : forwardStmts) {
        println(stmt);
      }
      newline();
    }

    // Print collected imports.
    println("#import \"JreEmulation.h\"");
    if (!superTypes.isEmpty()) {
      Set<String> importStmts = Sets.newTreeSet();
      for (ImportCollector.Import imp : superTypes) {
        importStmts.add(createImport(imp));
      }
      for (String stmt : importStmts) {
        println(stmt);
      }
    }
  }

  protected String createForwardDeclaration(String typeName, boolean isInterface) {
    return String.format("@%s %s;", isInterface ? "protocol" : "class", typeName);
  }

  protected String createImport(ImportCollector.Import imp) {
    return String.format("#import \"%s.h\"", imp.getImportFileName());
  }

  /**
   * If an inner type is defined before any of its super types are, put the
   * super types first.  Otherwise, keep the order as is, to make debugging
   * easier.  For field and method references to a following type, add a
   * forward type.
   *
   * @return the set of forward types to declare.
   */
  static Set<ITypeBinding> sortTypes(List<AbstractTypeDeclaration> types) {
    final List<AbstractTypeDeclaration> typesCopy =
        new ArrayList<AbstractTypeDeclaration>(types);

    final Map<String, AbstractTypeDeclaration> index = Maps.newHashMap();
    for (AbstractTypeDeclaration type : typesCopy) {
      index.put(Types.getTypeBinding(type).getBinaryName(), type);
    }

    final Multimap<String, String> references = HashMultimap.create();
    final Multimap<String, String> superTypes = HashMultimap.create();

    // Collect all references to other types, but track super types
    // separately from other references.
    for (AbstractTypeDeclaration type : typesCopy) {
      final String typeName = Types.getTypeBinding(type).getBinaryName();

      ErrorReportingASTVisitor collector = new ErrorReportingASTVisitor() {
        protected void addSuperType(Type type) {
          ITypeBinding binding = type == null ? null : Types.getTypeBinding(type);
          if (binding != null && isMember(binding)) {
            superTypes.put(typeName, binding.getBinaryName());
          }
        }

        private void addReference(Type type) {
          ITypeBinding binding = type == null ? null : Types.getTypeBinding(type);
          if (binding != null && isMember(binding)) {
            references.put(typeName, binding.getBinaryName());
          }
        }

        // Only collect references to types members.
        private boolean isMember(ITypeBinding binding) {
          return index.containsKey(binding.getBinaryName());
        }

        @Override
        public boolean visit(FieldDeclaration node) {
          addReference(node.getType());
          return true;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
          addReference(node.getReturnType2());
          @SuppressWarnings("unchecked")
          List<SingleVariableDeclaration> params = node.parameters();
          for (SingleVariableDeclaration param : params) {
            addReference(param.getType());
          }
          return true;
        }

        @Override
        public boolean visit(TypeDeclaration node) {
          ITypeBinding binding = Types.getTypeBinding(node);
          if (binding.isEqualTo(Types.getNSObject())) {
            return false;
          }
          addSuperType(node.getSuperclassType());
          for (Iterator<?> iterator = node.superInterfaceTypes().iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if (o instanceof Type) {
              addSuperType((Type) o);
            } else {
              throw new AssertionError("unknown AST type: " + o.getClass());
            }
          }
          return true;
        }
      };
      collector.run(type);
    }

    // Do a topological sort on the types declared in this unit, with an edge
    // in the graph denoting a type inheritance. Super types will end up
    // higher up in the sort.

    types.clear();
    LinkedHashSet<AbstractTypeDeclaration> rootTypes = Sets.newLinkedHashSet();
    for (AbstractTypeDeclaration type : typesCopy) {
      String name = Types.getTypeBinding(type).getBinaryName();
      if (!superTypes.containsValue(name)) {
        rootTypes.add(type);
      }
    }

    while (!rootTypes.isEmpty()) {
      AbstractTypeDeclaration type =
          (AbstractTypeDeclaration) rootTypes.toArray()[rootTypes.size() - 1];
      rootTypes.remove(type);
      types.add(0, type);

      ITypeBinding binding = Types.getTypeBinding(type);
      String typeName = binding.getBinaryName();
      // Copy the values to avoid a ConcurrentModificationException.
      List<String> values = Lists.newArrayList(superTypes.get(typeName));
      for (String superTypeName : values) {
        superTypes.remove(typeName, superTypeName);
        if (!superTypes.containsValue(superTypeName)) {
          AbstractTypeDeclaration superType = index.get(superTypeName);
          rootTypes.add(superType);
        }
      }
    }

    assert types.size() == typesCopy.size();

    // For all other references, if the referred to type is declared
    // after the reference, add a forward reference.
    final Set<ITypeBinding> moreForwardTypes = Sets.newHashSet();
    for (Map.Entry<String, String> entry : references.entries()) {
      AbstractTypeDeclaration referrer = index.get(entry.getKey());
      AbstractTypeDeclaration referred = index.get(entry.getValue());
      if (types.indexOf(referred) > types.indexOf(referrer)) {
        // Referred to type occurs after the reference; add a forward decl
        moreForwardTypes.add(Types.getTypeBinding(referred));
      }
    }

    return moreForwardTypes;
  }

  private void printInstanceVariables(List<FieldDeclaration> fields) {
    indent();
    String lastAccess = "@protected";
    for (FieldDeclaration field : fields) {
      if ((field.getModifiers() & Modifier.STATIC) == 0) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> vars = field.fragments(); // safe by definition
        assert !vars.isEmpty();
        VariableDeclarationFragment var = vars.get(0);
        if (var.getName().getIdentifier().startsWith("this$") && superDefinesVariable(var)) {
          // Don't print, as it shadows an inner field in a super class.
          continue;
        }
        String access = accessScope(field.getModifiers());
        if (!access.equals(lastAccess)) {
          print(' ');
          println(access);
          lastAccess = access;
        }
        printIndent();
        if (Types.isWeakReference(Types.getVariableBinding(var)) && Options.useARC()) {
          print("__weak ");
        }
        ITypeBinding varType = Types.getTypeBinding(vars.get(0));
        String objcType = NameTable.javaRefToObjC(varType);
        boolean needsAsterisk = !varType.isPrimitive() && !objcType.matches("id|id<.*>|Class");
        if (needsAsterisk && objcType.endsWith(" *")) {
          // Strip pointer from type, as it will be added when appending fragment.
          // This is necessary to create "Foo *one, *two;" declarations.
          objcType = objcType.substring(0, objcType.length() - 2);
        }
        print(objcType);
        print(' ');
        for (Iterator<?> it = field.fragments().iterator(); it.hasNext(); ) {
          VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
          if (needsAsterisk) {
            print('*');
          }
          String name = NameTable.getName(f.getName());
          print(NameTable.javaFieldToObjC(name));
          if (it.hasNext()) {
            print(", ");
          }
        }
        println(";");
      }
    }
    unindent();
  }

  private void printProperties(List<FieldDeclaration> fields) {
    int nPrinted = 0;
    for (FieldDeclaration field : fields) {
      if ((field.getModifiers() & Modifier.STATIC) == 0) {
        ITypeBinding type = Types.getTypeBinding(field.getType());
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> vars = field.fragments(); // safe by definition
        for (VariableDeclarationFragment var : vars) {
          if (var.getName().getIdentifier().startsWith("this$") && superDefinesVariable(var)) {
            // Don't print, as it shadows an inner field in a super class.
            continue;
          }
          print("@property (nonatomic, ");
          IVariableBinding varBinding = Types.getVariableBinding(var);
          if (type.isPrimitive()) {
            print("assign");
          } else if (Types.isWeakReference(varBinding)) {
            print(Options.useARC() ? "weak" : "assign");
          } else if (type.isEqualTo(Types.getNSString())) {
            print("copy");
          } else {
            print(Options.useARC() ? "strong" : "retain");
          }
          String typeString = NameTable.javaRefToObjC(type);
          if (!typeString.endsWith("*")) {
            typeString += " ";
          }
          String propertyName = NameTable.getName(var.getName());
          println(String.format(") %s%s;", typeString, propertyName));
          if (propertyName.startsWith("new") || propertyName.startsWith("copy")
              || propertyName.startsWith("alloc") || propertyName.startsWith("init")) {
            println(String.format("- (%s)%s OBJC_METHOD_FAMILY_NONE;",
                NameTable.javaRefToObjC(type), propertyName));
          }
          nPrinted++;
        }
      }
    }
    if (nPrinted > 0) {
      newline();
    }
  }

  private void printConstantDefines(AbstractTypeDeclaration node) {
    ITypeBinding type = Types.getTypeBinding(node);
    boolean hadConstant = false;
    for (IVariableBinding field : type.getDeclaredFields()) {
      if (Types.isPrimitiveConstant(field)) {
        printf("#define %s ", NameTable.getPrimitiveConstantName(field));
        Object value = field.getConstantValue();
        assert value != null;
        if (value instanceof Boolean) {
          println(((Boolean) value).booleanValue() ? "TRUE" : "FALSE");
        } else if (value instanceof Character) {
          char c = ((Character) value).charValue();
          String convertedChar = UnicodeUtils.escapeCharacter(c);
          if (convertedChar != null) {
            if (convertedChar.equals("'") || convertedChar.equals("\\")) {
              printf("'\\%s'\n", convertedChar);
            } else {
              printf("'%s'\n", convertedChar);
            }
          } else {
            // The Java char constant is likely not a valid C character; just
            // print it as an int.
            printf("0x%4x\n", c & 0xffff);
          }
        } else if (value instanceof Long) {
          long l = ((Long) value).longValue();
          if (l == Long.MIN_VALUE) {
            println("-0x7fffffffffffffffLL - 1");
          } else {
            println(value.toString());
          }
        } else if (value instanceof Integer) {
          long l = ((Integer) value).intValue();
          if (l == Integer.MIN_VALUE) {
            println("-0x7fffffff - 1");
          } else {
            println(value.toString());
          }
        } else if (value instanceof Float) {
          float f = ((Float) value).floatValue();
          if (Float.isNaN(f)) {
            println("NAN");
          } else if (f == Float.POSITIVE_INFINITY) {
            println("INFINITY");
          } else if (f == Float.NEGATIVE_INFINITY) {
            // FP representations are symmetrical.
            println("-INFINITY");
          } else if (f == Float.MAX_VALUE) {
            println("__FLT_MAX__");
          } else if (f == Float.MIN_VALUE) {
            println("__FLT_MIN__");
          } else {
            println(value.toString());
          }
        } else if (value instanceof Double) {
          double d = ((Double) value).doubleValue();
          if (Double.isNaN(d)) {
            println("NAN");
          } else if (d == Double.POSITIVE_INFINITY) {
            println("INFINITY");
          } else if (d == Double.NEGATIVE_INFINITY) {
            // FP representations are symmetrical.
            println("-INFINITY");
          } else if (d == Double.MAX_VALUE) {
            println("__DBL_MAX__");
          } else if (d == Double.MIN_VALUE) {
            println("__DBL_MIN__");
          } else {
            println(value.toString());
          }
        } else {
          println(value.toString());
        }
        hadConstant = true;
      }
    }
    if (hadConstant) {
      newline();
    }
  }

  private String accessScope(int modifiers) {
    if (Options.inlineFieldAccess()) {
      // Need direct access to fields possibly from inner classes that are
      // promoted to top level classes, so must make all fields public.
      return "@public";
    }

    if ((modifiers & Modifier.PUBLIC) > 0) {
      return "@public";
    }
    if ((modifiers & Modifier.PROTECTED) > 0) {
      return "@protected";
    }
    if ((modifiers & Modifier.PRIVATE) > 0) {
      return "@private";
    }
    return "@package";
  }

  /**
   * Checks if the list of modifiers contains a Deprecated annotation.
   *
   * @param modifiers extended modifiers
   * @return true if the list has {@link Deprecated @Deprecated}, false otherwise
   */
  boolean hasDeprecated(List<IExtendedModifier> modifiers) {
    for (IExtendedModifier modifier : modifiers) {
      if (modifier.isAnnotation()) {
        Annotation annotation = (Annotation) modifier;
        Name annotationTypeName = annotation.getTypeName();
        String expectedTypeName = annotationTypeName.isQualifiedName() ?
            "java.lang.Deprecated" : "Deprecated";
        if (expectedTypeName.equals(annotationTypeName.getFullyQualifiedName())) {
          return true;
        }
      }
    }

    return false;
  }
}
