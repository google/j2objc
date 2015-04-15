SUFFIXES:

J2OBJC_ROOT = ../..
include $(J2OBJC_ROOT)/make/common.mk
include $(J2OBJC_ROOT)/make/j2objc_deps.mk
include $(J2OBJC_ROOT)/java_deps/jars.mk

SRC_ROOT = $(realpath ../../../../../..)

.NOTPARALLEL:

JAVA_TESTS = \
  ByteStringTest.java \
  CompatibilityTest.java \
  EnumsTest.java \
  MessagesTest.java \
  PrimitivesTest.java \
  StringsTest.java
OTHER_JAVA_SOURCES = \
  MemoryBenchmarks.java \
  PerformanceBenchmarks.java \
  ProtobufTest.java

PROTOS = \
  empty_file.proto \
  enum_fields.proto \
  funny_names.proto \
  message_fields.proto \
  package_prefix.proto \
  primitives.proto \
  single_file.proto \
  size_test.proto \
  string_fields.proto \
  typical.proto


TESTS_TO_RUN = $(JAVA_TESTS:%.java=%)

JAVA_SOURCES = $(JAVA_TESTS) $(OTHER_JAVA_SOURCES)

OBJS_DIR = $(BUILD_DIR)/objs

CLASSPATH_LIST = \
  $(DIST_JAR_DIR)/$(JUNIT_JAR) \
  $(DIST_JAR_DIR)/$(HAMCREST_JAR) \
  $(DIST_JAR_DIR)/j2objc_annotations.jar \
  $(DIST_JAR_DIR)/protobuf_runtime.jar
CLASSPATH = $(subst $(eval) ,:,$(strip $(CLASSPATH_LIST)))

TEST_J2OBJCC = $(DIST_DIR)/j2objcc -g
C_FLAGS = -Werror -Wno-objc-redundant-literal-use -Wno-format -Wno-parentheses \
  -fobjc-abi-version=2 -fobjc-legacy-dispatch -stdlib=libc++

INCLUDE_DIRS = . $(GEN_OBJC_DIR)

LIB_DEPS = $(DIST_DIR)/lib/libjunit.a $(DIST_DIR)/lib/libprotobuf_runtime.a

J2OBJC_PROTOS_NAME = testprotos
J2OBJC_PROTOS_INPUTS = $(PROTOS:%=protos/%)
J2OBJC_PROTOS_PREFIX_FILES = j2objc_prefixes
J2OBJC_PROTOS_GENERATE_CLASS_MAPPINGS = YES
include $(J2OBJC_ROOT)/make/j2objc_protos.mk

CREATE_JAR_NAME = protobuf_tests
CREATE_JAR_SOURCES = $(JAVA_SOURCES) $(J2OBJC_PROTOS_JAVA)
CREATE_JAR_JAVAC_ARGS = -cp $(CLASSPATH) -encoding UTF-8
CREATE_JAR_DEPENDENCIES = annotations_dist junit_java protobuf_runtime_java
include $(J2OBJC_ROOT)/make/create_jar.mk

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES)
TRANSLATE_ARGS = -cp $(CLASSPATH):$(CREATE_JAR_RESULT) \
  --prefixes j2objc_prefixes \
  -encoding UTF-8 \
  $(J2OBJC_PROTOS_CLASS_MAPPING_FILES:%=--mapping %)
TRANSLATE_DEPENDENCIES = $(J2OBJC_PROTOS_CLASS_MAPPING_FILES)
include $(J2OBJC_ROOT)/make/translate.mk

translate_dependencies: $(CREATE_JAR_RESULT)

OBJS = $(JAVA_SOURCES:%.java=$(OBJS_DIR)/%.o) $(OBJS_DIR)/my_malloc.o \
  $(J2OBJC_PROTOS_SOURCES:$(GEN_OBJC_DIR)/%.m=$(OBJS_DIR)/%.o)
BIN = $(BUILD_DIR)/protobuf_tests

test_java: $(CREATE_JAR_RESULT)
	java -classpath $(CLASSPATH):$(CREATE_JAR_RESULT) \
	  org.junit.runner.JUnitCore $(TESTS_TO_RUN)

test_objc: $(BIN)
	@$(BIN) org.junit.runner.JUnitCore $(TESTS_TO_RUN)

memory_benchmarks: $(BIN)
	@$(BIN) MemoryBenchmarks

performance_benchmarks: $(BIN)
	@$(BIN) PerformanceBenchmarks

test: test_java test_objc

clean:
	@rm -rf $(BUILD_DIR)

$(BUILD_DIR):
	@mkdir -p $(BUILD_DIR)

$(OBJS_DIR)/%.o: $(GEN_OBJC_DIR)/%.m \
  | jre_emul_dist junit_dist protobuf_runtime_dist $(J2OBJC_PROTOS_OBJC)
	@mkdir -p $(@D)
	$(TEST_J2OBJCC) -c $(INCLUDE_DIRS:%=-I%) $(C_FLAGS) -o $@ $<

$(OBJS_DIR)/my_malloc.o: my_malloc.m | jre_emul_dist
	@mkdir -p $(@D)
	$(TEST_J2OBJCC) -c -o $@ $<

$(BIN): $(OBJS) $(LIB_DEPS)
	$(TEST_J2OBJCC) -ObjC -Werror -l c++ -l junit -l protobuf_runtime -o $@ $(OBJS)
