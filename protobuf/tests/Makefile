J2OBJC_ROOT = ../..
include $(J2OBJC_ROOT)/make/common.mk
include $(J2OBJC_ROOT)/make/j2objc_deps.mk
include $(J2OBJC_ROOT)/make/translate_macros.mk
include $(J2OBJC_ROOT)/java_deps/jars.mk

SRC_ROOT = $(realpath ../../../../../..)

.NOTPARALLEL:

JAVA_TESTS = \
  ByteStringTest.java \
  CompatibilityTest.java \
  EnumsTest.java \
  MapsTest.java \
  MessagesTest.java \
  OneofTest.java \
  PrimitivesTest.java \
  StringsTest.java
OTHER_JAVA_SOURCES = \
  MemoryBenchmarks.java \
  PerformanceBenchmarks.java \
  ProtobufTest.java

JAVA_TESTS_ARC = \
  ByteStringTest.java \
  CompatibilityTest.java \
  EnumsTest.java \
  MapsTest.java \
  MessagesTest.java \
  OneofTest.java \
  PrimitivesTest.java \
  StringsTest.java
OTHER_JAVA_SOURCES_ARC = \
  MemoryBenchmarks.java \
  PerformanceBenchmarks.java \
  ProtobufTest.java

PROTOS = \
  conflicting_class_name.proto \
  empty_file.proto \
  enum_fields.proto \
  funny_names.proto \
  map_fields.proto \
  message_fields.proto \
  oneof_fields.proto \
  package_prefix.proto \
  prefix_option.proto \
  primitives.proto \
  single_file.proto \
  size_test.proto \
  string_fields.proto \
  typical.proto

TESTDATA = \
  testdata/largeproto \
  testdata/randombytes

$(call check_protobuf_dir)

DESCRIPTOR_INCLUDE_DIR = ../compiler/src
DESCRIPTOR_PROTO = $(DESCRIPTOR_INCLUDE_DIR)/google/protobuf/j2objc-descriptor.proto

TESTS_TO_RUN = $(JAVA_TESTS:%.java=%)
TESTS_TO_RUN_ARC = $(JAVA_TESTS_ARC:%.java=%)

JAVA_SOURCES = $(JAVA_TESTS) $(OTHER_JAVA_SOURCES)
JAVA_SOURCES_ARC = $(JAVA_TESTS_ARC) $(OTHER_JAVA_SOURCES_ARC)

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

LIB_DEPS = $(DIST_LIB_MACOSX_DIR)/libjunit.a $(DIST_LIB_MACOSX_DIR)/libprotobuf_runtime.a

J2OBJC_PROTOS_NAME = testprotos
J2OBJC_PROTOS_INPUTS = $(PROTOS:%=protos/%) $(DESCRIPTOR_PROTO)
J2OBJC_PROTOS_PATHS = protos $(DESCRIPTOR_INCLUDE_DIR) $(PROTOBUF_INCLUDE_PATH)
J2OBJC_PROTOS_PREFIX_FILES = j2objc_prefixes
J2OBJC_PROTOS_GENERATE_CLASS_MAPPINGS = YES
include $(J2OBJC_ROOT)/make/j2objc_protos.mk

CREATE_JAR_NAME = protobuf_tests
CREATE_JAR_SOURCES = $(JAVA_SOURCES) $(J2OBJC_PROTOS_JAVA)
CREATE_JAR_RESOURCES = $(TESTDATA)
CREATE_JAR_JAVAC_ARGS = -cp $(CLASSPATH) -encoding UTF-8
CREATE_JAR_DEPENDENCIES = annotations_dist junit_java protobuf_runtime_java
include $(J2OBJC_ROOT)/make/create_jar.mk

TRANSLATE_ARGS = -cp $(CLASSPATH):$(CREATE_JAR_RESULT) \
  --prefixes j2objc_prefixes \
  -encoding UTF-8 \
  $(J2OBJC_PROTOS_CLASS_MAPPING_FILES:%=--mapping %)
TRANSLATED_SOURCES = $(JAVA_SOURCES:%.java=$(GEN_OBJC_DIR)/%.m)
TRANSLATED_SOURCES_ARC = $(JAVA_SOURCES_ARC:%.java=$(GEN_OBJC_DIR)/arc/%.m)

TRANSLATE_ARTIFACT := $(call emit_translate_rule,\
  protobuf_tests,\
  $(GEN_OBJC_DIR),\
  $(JAVA_SOURCES),\
  $(J2OBJC_PROTOS_CLASS_MAPPING_FILES),\
  $(TRANSLATE_ARGS))

TRANSLATE_ARTIFACT_ARC := $(call emit_translate_rule,\
  protobuf_tests_arc,\
  $(GEN_OBJC_DIR)/arc,\
  $(JAVA_SOURCES_ARC),\
  $(J2OBJC_PROTOS_CLASS_MAPPING_FILES),\
  $(TRANSLATE_ARGS) -use-arc)

$(TRANSLATED_SOURCES): $(TRANSLATE_ARTIFACT)
	@:

$(TRANSLATED_SOURCES_ARC): $(TRANSLATE_ARTIFACT_ARC)
	@:

translate_dependencies: $(CREATE_JAR_RESULT)

OBJS = $(JAVA_SOURCES:%.java=$(OBJS_DIR)/%.o) $(OBJS_DIR)/my_malloc.o \
  $(J2OBJC_PROTOS_SOURCES:$(GEN_OBJC_DIR)/%.m=$(OBJS_DIR)/%.o)
OBJS_ARC = $(JAVA_SOURCES_ARC:%.java=$(OBJS_DIR)/arc/%.o) $(OBJS_DIR)/my_malloc.o \
  $(J2OBJC_PROTOS_SOURCES:$(GEN_OBJC_DIR)/%.m=$(OBJS_DIR)/%.o)
BIN = $(BUILD_DIR)/protobuf_tests
BIN_ARC = $(BUILD_DIR)/protobuf_tests_arc

test_java: $(CREATE_JAR_RESULT)
	$(JAVA) -classpath $(CLASSPATH):$(CREATE_JAR_RESULT) \
	  org.junit.runner.JUnitCore $(TESTS_TO_RUN)

test_objc: $(BIN)
	@$(BIN) org.junit.runner.JUnitCore $(TESTS_TO_RUN)

test_objc_arc: $(BIN_ARC)
	@$(BIN_ARC) org.junit.runner.JUnitCore $(TESTS_TO_RUN_ARC)

memory_benchmarks: $(BIN)
	@$(BIN) MemoryBenchmarks

memory_benchmarks_arc: $(BIN)
	@$(BIN_ARC) MemoryBenchmarks

performance_benchmarks: $(BIN)
	@$(BIN) PerformanceBenchmarks

performance_benchmarks_arc: $(BIN)
	@$(BIN_ARC) PerformanceBenchmarks

test: test_java test_objc test_objc_arc

clean:
	@rm -rf $(BUILD_DIR)

$(BUILD_DIR):
	@mkdir -p $(BUILD_DIR)

# This ARC rule needs to be declared first, since it uses a more specific
# pattern than the following rule.
$(OBJS_DIR)/arc/%.o: $(GEN_OBJC_DIR)/arc/%.m \
  | jre_emul_dist junit_dist protobuf_runtime_dist $(J2OBJC_PROTOS_OBJC)
	@mkdir -p $(@D)
	$(TEST_J2OBJCC) -Werror -c $(INCLUDE_DIRS:%=-I%) $(C_FLAGS) \
      -fobjc-arc -fobjc-arc-exceptions -o $@ $<

$(OBJS_DIR)/%.o: $(GEN_OBJC_DIR)/%.m \
  | jre_emul_dist junit_dist protobuf_runtime_dist $(J2OBJC_PROTOS_OBJC)
	@mkdir -p $(@D)
	$(TEST_J2OBJCC) -Werror -c $(INCLUDE_DIRS:%=-I%) $(C_FLAGS) -o $@ $<

$(OBJS_DIR)/my_malloc.o: my_malloc.m | jre_emul_dist
	@mkdir -p $(@D)
	$(TEST_J2OBJCC) -c -o $@ $<

$(BIN): $(OBJS) $(LIB_DEPS)
	$(TEST_J2OBJCC) -ObjC -Werror -l c++ -ljre_emul -ljunit -lprotobuf_runtime -o $@ $(OBJS)

$(BIN_ARC): $(OBJS_ARC) $(LIB_DEPS)
	$(TEST_J2OBJCC) -ObjC -Werror -l c++ -ljre_emul -ljunit -lprotobuf_runtime \
       -fobjc-arc -fobjc-arc-exceptions -o $@ $(OBJS_ARC)
