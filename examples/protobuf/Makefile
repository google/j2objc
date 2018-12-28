# See README.txt.

.PHONY: all clean

ifndef J2OBJC_DIST
J2OBJC_DIST = ../../dist
endif

JAVA_SOURCES = AddPerson.java ListPeople.java
PROTO_SOURCE = addressbook.proto
PROTO_PACKAGE = com/example/tutorial
SCRIPTS = add_person list_people

BUILD_DIR = build_result

all: $(SCRIPTS)

# Generate Java and Objective C proto files.
JAVA_DIR = $(BUILD_DIR)/java
PROTO_JAVA = $(JAVA_DIR)/$(PROTO_PACKAGE)/AddressBookProtos.java
OBJC_DIR = $(BUILD_DIR)/objc
PROTO_OBJC = $(OBJC_DIR)/$(PROTO_PACKAGE)/AddressBookProtos.m

$(PROTO_JAVA) $(PROTO_OBJC): $(JAVA_DIR) $(OBJC_DIR)
	$(J2OBJC_DIST)/j2objc_protoc --java_out=$(JAVA_DIR) \
	    --j2objc_out=$(OBJC_DIR) $(PROTO_SOURCE)

# Compile Java proto file.
PROTO_JAVA_CLASS = $(PROTO_JAVA:%.java=%.class)
PROTOBUF_RUNTIME = $(J2OBJC_DIST)/lib/protobuf_runtime.jar
JRE_RUNTIME = $(J2OBJC_DIST)/lib/jre_emul.jar

$(PROTO_JAVA_CLASS): $(PROTO_JAVA)
	javac -source 1.8 -target 1.8 -bootclasspath $(JRE_RUNTIME) \
	    -cp $(PROTOBUF_RUNTIME) -d $(JAVA_DIR) $(PROTO_JAVA)

# Translate Java sources, adding compiled Java proto to the classpath.
OBJC_SOURCES = $(JAVA_SOURCES:%.java=$(OBJC_DIR)/%.m)

$(OBJC_SOURCES): $(PROTO_JAVA_CLASS)
	$(J2OBJC_DIST)/j2objc -cp $(JAVA_DIR):$(PROTOBUF_RUNTIME) \
	    -d $(OBJC_DIR) $(JAVA_SOURCES)

# Build shared executable
EXECUTABLE = $(BUILD_DIR)/addressbook

$(EXECUTABLE): $(OBJC_SOURCES) $(PROTO_OBJC)
	$(J2OBJC_DIST)/j2objcc -I$(OBJC_DIR) -lprotobuf_runtime -o $@ \
	    $^

# Create scripts.
$(SCRIPTS): $(EXECUTABLE)
	@echo "Writing scripts"
	@echo '#! /bin/sh' > add_person
	@echo $(EXECUTABLE) 'AddPerson "$$@"' >> add_person
	@echo '#! /bin/sh' > list_people
	@echo $(EXECUTABLE) 'ListPeople "$$@"' >> list_people
	@chmod +x add_person list_people


$(JAVA_DIR) $(OBJC_DIR):
	@mkdir -p $@

clean:
	@rm -rf $(BUILD_DIR) $(SCRIPTS)
