package com.strobel.decompiler;

import com.beust.jcommander.JCommander;
import com.strobel.Procyon;
import com.strobel.annotations.NotNull;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.*;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.LineNumberFormatter.LineNumberOption;
import com.strobel.decompiler.languages.BytecodeOutputOptions;
import com.strobel.decompiler.languages.BytecodeLanguage;
import com.strobel.decompiler.languages.Languages;
import com.strobel.decompiler.languages.LineNumberPosition;
import com.strobel.decompiler.languages.TypeDecompilationResults;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;
import com.strobel.io.PathHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DecompilerDriver {
    public static void main(final String[] args) {
        final CommandLineOptions options = new CommandLineOptions();
        final JCommander jCommander;
        final List<String> typeNames;

        try {
            jCommander = new JCommander(options);
            jCommander.setAllowAbbreviatedOptions(false);
            jCommander.parse(args);
            typeNames = options.getInputs();
        }
        catch (final Throwable t) {
            System.err.println(ExceptionUtilities.getMessage(t));
            System.exit(-1);
            return;
        }

        configureLogging(options);

        final String jarFile = options.getJarFile();
        final boolean decompileJar = !StringUtilities.isNullOrWhitespace(jarFile);

        if (options.getPrintVersion()) {
            JCommander.getConsole().println(Procyon.version());
            if (options.getPrintUsage()) {
                jCommander.usage();
            }
            return;
        }

        if (options.getPrintUsage() ||
            typeNames.isEmpty() && !decompileJar) {

            jCommander.usage();
            return;
        }

        final DecompilerSettings settings = new DecompilerSettings();

        settings.setFlattenSwitchBlocks(options.getFlattenSwitchBlocks());
        settings.setForceExplicitImports(!options.getCollapseImports());
        settings.setForceExplicitTypeArguments(options.getForceExplicitTypeArguments());
        settings.setRetainRedundantCasts(options.getRetainRedundantCasts());
        settings.setShowSyntheticMembers(options.getShowSyntheticMembers());
        settings.setExcludeNestedTypes(options.getExcludeNestedTypes());
        settings.setOutputDirectory(options.getOutputDirectory());
        settings.setIncludeLineNumbersInBytecode(options.getIncludeLineNumbers());
        settings.setRetainPointlessSwitches(options.getRetainPointlessSwitches());
        settings.setUnicodeOutputEnabled(options.isUnicodeOutputEnabled());
        settings.setMergeVariables(options.getMergeVariables());
        settings.setShowDebugLineNumbers(options.getShowDebugLineNumbers());
        settings.setSimplifyMemberReferences(options.getSimplifyMemberReferences());
        settings.setForceFullyQualifiedReferences(options.getForceFullyQualifiedReferences());
        settings.setDisableForEachTransforms(options.getDisableForEachTransforms());
        settings.setTypeLoader(new InputTypeLoader());

        if (!options.getSuppressBanner()) {
            settings.setOutputFileHeaderText("\nDecompiled by Procyon v" + Procyon.version() + "\n");
        }

        if (options.isRawBytecode()) {
            settings.setLanguage(Languages.bytecode());
            settings.setBytecodeOutputOptions(createBytecodeFormattingOptions(options));
        }
        else if (options.isBytecodeAst()) {
            settings.setLanguage(
                options.isUnoptimized() ? Languages.bytecodeAstUnoptimized()
                                        : Languages.bytecodeAst()
            );
        }

        final DecompilationOptions decompilationOptions = new DecompilationOptions();

        decompilationOptions.setSettings(settings);
        decompilationOptions.setFullDecompilation(true);

        if (settings.getJavaFormattingOptions() == null) {
            settings.setJavaFormattingOptions(JavaFormattingOptions.createDefault());
        }

        if (decompileJar) {
            try {
                decompileJar(jarFile, options, decompilationOptions);
            }
            catch (final Throwable t) {
                System.err.println(ExceptionUtilities.getMessage(t));
                System.exit(-1);
            }
        }
        else {
            final MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());

            metadataSystem.setEagerMethodLoadingEnabled(options.isEagerMethodLoadingEnabled());

            for (final String typeName : typeNames) {
                try {
                    if (typeName.endsWith(".jar")) {
                        decompileJar(typeName, options, decompilationOptions);
                    }
                    else {
                        decompileType(metadataSystem, typeName, options, decompilationOptions, true);
                    }
                }
                catch (final Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    private static BytecodeOutputOptions createBytecodeFormattingOptions(final CommandLineOptions options) {
        if (options.isVerbose()) {
            return BytecodeOutputOptions.createVerbose();
        }

        final BytecodeOutputOptions bytecodeOptions = BytecodeOutputOptions.createDefault();

        bytecodeOptions.showTypeAttributes = options.getShowTypeAttributes();
        bytecodeOptions.showConstantPool = options.getShowConstantPool();
        bytecodeOptions.showLineNumbers = options.getIncludeLineNumbers();
        bytecodeOptions.showLocalVariableTables = options.getShowLocalVariableDetails();
        bytecodeOptions.showMethodsStack = options.getShowLocalVariableDetails();

        return bytecodeOptions;
    }

    private static void configureLogging(final CommandLineOptions options) {
        final Logger globalLogger = Logger.getGlobal();
        final Logger rootLogger = Logger.getAnonymousLogger().getParent();

        for (final Handler handler : globalLogger.getHandlers()) {
            globalLogger.removeHandler(handler);
        }

        for (final Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        final Level verboseLevel;

        switch (options.getLogLevel()) {
            case 0:
                verboseLevel = Level.SEVERE;
                break;
            case 1:
                verboseLevel = Level.FINE;
                break;
            case 2:
                verboseLevel = Level.FINER;
                break;
            case 3:
            default:
                verboseLevel = Level.FINEST;
                break;
        }

        globalLogger.setLevel(verboseLevel);
        rootLogger.setLevel(verboseLevel);

        final ConsoleHandler handler = new ConsoleHandler();

        handler.setLevel(verboseLevel);
        handler.setFormatter(new BriefLogFormatter());

        globalLogger.addHandler(handler);
        rootLogger.addHandler(handler);
    }

    private static void decompileJar(
        final String jarFilePath,
        final CommandLineOptions options,
        final DecompilationOptions decompilationOptions) throws IOException {

        final File jarFile = new File(jarFilePath);

        if (!jarFile.exists()) {
            throw new FileNotFoundException("File not found: " + jarFilePath);
        }

        final DecompilerSettings settings = decompilationOptions.getSettings();
        final JarFile jar = new JarFile(jarFile);
        final Enumeration<JarEntry> entries = jar.entries();

        final boolean oldShowSyntheticMembers = settings.getShowSyntheticMembers();
        final ITypeLoader oldTypeLoader = settings.getTypeLoader();

        settings.setShowSyntheticMembers(false);
        settings.setTypeLoader(new CompositeTypeLoader(new JarTypeLoader(jar), oldTypeLoader));

        try {
            MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());

            metadataSystem.setEagerMethodLoadingEnabled(options.isEagerMethodLoadingEnabled());

            int classesDecompiled = 0;

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();

                if (!name.endsWith(".class")) {
                    continue;
                }

                final String internalName = StringUtilities.removeRight(name, ".class");

                try {
                    decompileType(metadataSystem, internalName, options, decompilationOptions, false);

                    if (++classesDecompiled % 100 == 0) {
                        metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
                    }
                }
                catch (final Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        finally {
            settings.setShowSyntheticMembers(oldShowSyntheticMembers);
            settings.setTypeLoader(oldTypeLoader);
        }
    }

    private static void decompileType(
        final MetadataSystem metadataSystem,
        final String typeName,
        final CommandLineOptions commandLineOptions,
        final DecompilationOptions options,
        final boolean includeNested) throws IOException {

        final TypeReference type;
        final DecompilerSettings settings = options.getSettings();

        if (typeName.length() == 1) {
            //
            // Hack to get around classes whose descriptors clash with primitive types.
            //

            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(typeName);

            type = metadataSystem.resolve(reference);
        }
        else {
            type = metadataSystem.lookupType(typeName);
        }

        final TypeDefinition resolvedType;

        if (type == null || (resolvedType = type.resolve()) == null) {
            System.err.printf("!!! ERROR: Failed to load class %s.\n", typeName);
            return;
        }

        DeobfuscationUtilities.processType(resolvedType);

        if (!includeNested && (resolvedType.isNested() || resolvedType.isAnonymous() || resolvedType.isSynthetic())) {
            return;
        }

        final Writer writer = createWriter(resolvedType, settings);
        final boolean writeToFile = writer instanceof FileOutputWriter;
        final PlainTextOutput output;

        if (writeToFile) {
            output = new PlainTextOutput(writer);
        }
        else {
            output = new AnsiTextOutput(
                writer,
                commandLineOptions.getUseLightColorScheme() ? AnsiTextOutput.ColorScheme.LIGHT
                                                            : AnsiTextOutput.ColorScheme.DARK
            );
        }

        output.setUnicodeOutputEnabled(settings.isUnicodeOutputEnabled());

        if (settings.getLanguage() instanceof BytecodeLanguage) {
            output.setIndentToken("  ");
        }

        if (writeToFile) {
            System.out.printf("Decompiling %s...\n", typeName);
        }

        final TypeDecompilationResults results = settings.getLanguage().decompileType(resolvedType, output, options);

        writer.flush();

        if (writeToFile) {
            writer.close();
        }

        // If we're writing to a file and we were asked to include line numbers in any way,
        // then reformat the file to include that line number information.
        final List<LineNumberPosition> lineNumberPositions = results.getLineNumberPositions();

        if ((commandLineOptions.getIncludeLineNumbers() || commandLineOptions.getStretchLines()) && (writer instanceof FileOutputWriter)) {
            final EnumSet<LineNumberOption> lineNumberOptions = EnumSet.noneOf(LineNumberOption.class);

            if (commandLineOptions.getIncludeLineNumbers()) {
                lineNumberOptions.add(LineNumberOption.LEADING_COMMENTS);
            }

            if (commandLineOptions.getStretchLines()) {
                lineNumberOptions.add(LineNumberOption.STRETCHED);
            }

            final LineNumberFormatter lineFormatter = new LineNumberFormatter(
                ((FileOutputWriter) writer).getFile(),
                lineNumberPositions,
                lineNumberOptions
            );

            lineFormatter.reformatFile();
        }
    }

    private static Writer createWriter(final TypeDefinition type, final DecompilerSettings settings) throws IOException {
        final String outputDirectory = settings.getOutputDirectory();

        if (StringUtilities.isNullOrWhitespace(outputDirectory)) {
            return new OutputStreamWriter(
                System.out,
                settings.isUnicodeOutputEnabled() ? Charset.forName("UTF-8")
                                                  : Charset.defaultCharset()
            );
        }

        final String outputPath;
        final String fileName = type.getName() + settings.getLanguage().getFileExtension();
        final String packageName = type.getPackageName();

        if (StringUtilities.isNullOrWhitespace(packageName)) {
            outputPath = PathHelper.combine(outputDirectory, fileName);
        }
        else {
            outputPath = PathHelper.combine(
                outputDirectory,
                packageName.replace('.', PathHelper.DirectorySeparator),
                fileName
            );
        }

        final File outputFile = new File(outputPath);
        final File parentFile = outputFile.getParentFile();

        if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalStateException(
                String.format(
                    "Could not create output directory for file \"%s\".",
                    outputPath
                )
            );
        }

        if (!outputFile.exists() && !outputFile.createNewFile()) {
            throw new IllegalStateException(
                String.format(
                    "Could not create output file \"%s\".",
                    outputPath
                )
            );
        }

        return new FileOutputWriter(outputFile, settings);
    }
}

final class FileOutputWriter extends OutputStreamWriter {
    private final File file;

    FileOutputWriter(final File file, final DecompilerSettings settings) throws IOException {
        super(
            new FileOutputStream(file),
            settings.isUnicodeOutputEnabled() ? Charset.forName("UTF-8")
                                              : Charset.defaultCharset()
        );
        this.file = file;
    }

    /**
     * Returns the file to which 'this' is writing.
     *
     * @return the file to which 'this' is writing
     */
    public File getFile() {
        return this.file;
    }
}

final class BriefLogFormatter extends Formatter {
    private static final DateFormat format = new SimpleDateFormat("h:mm:ss");
    private static final String lineSep = System.getProperty("line.separator");

    /**
     * A Custom format implementation that is designed for brevity.
     */
    public String format(@NotNull final LogRecord record) {
        String loggerName = record.getLoggerName();

        if (loggerName == null) {
            loggerName = "root";
        }

        return format.format(new Date(record.getMillis())) +
               " [" + record.getLevel() + "] " +
               loggerName + ": " + record.getMessage() + ' ' + lineSep;
    }
}

final class NoRetryMetadataSystem extends MetadataSystem {
    private final Set<String> _failedTypes = new HashSet<>();

    NoRetryMetadataSystem() {
    }

//    NoRetryMetadataSystem(final String classPath) {
//        super(classPath);
//    }

    NoRetryMetadataSystem(final ITypeLoader typeLoader) {
        super(typeLoader);
    }

    @Override
    protected TypeDefinition resolveType(final String descriptor, final boolean mightBePrimitive) {
        if (_failedTypes.contains(descriptor)) {
            return null;
        }

        final TypeDefinition result = super.resolveType(descriptor, mightBePrimitive);

        if (result == null) {
            _failedTypes.add(descriptor);
        }

        return result;
    }
}