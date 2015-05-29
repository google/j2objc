---
layout: default
---

# Using J2ObjC with Make

Here's a simple Makefile that demonstrates building with j2objc and j2objcc:

```make
.SUFFIXES: .java .m

BUILD_DIR = build
SOURCE_DIR = src

# Change to where distribution was unzipped.
J2OBJC_DISTRIBUTION = /your/distribution/dir
J2OBJC = $(J2OBJC_DISTRIBUTION)/j2objc
J2OBJCC = $(J2OBJC_DISTRIBUTION)/j2objcc

OBJECTS = \
  $(BUILD_DIR)/foo/MainClass.o \
  $(BUILD_DIR)/foo/bar/Support.o \
  $(BUILD_DIR)/foo/bar/Utils.o
OBJC_SOURCES = $(OBJECTS:.o=.m)
RESULT = mainclass

default: translate $(OBJECTS)
	$(J2OBJCC) -o $(RESULT) $(OBJECTS)

translate: $(BUILD_DIR) $(OBJC_SOURCES)

clean:
	@rm -rf $(RESULT) $(BUILD_DIR)

$(BUILD_DIR)/%.m $(BUILD_DIR)/%.h: $(SOURCE_DIR)/%.java
	$(J2OBJC) -sourcepath $(SOURCE_DIR) -d $(BUILD_DIR) $?

$(BUILD_DIR)/%.o: $(BUILD_DIR)/%.m
	$(J2OBJCC) -I$(BUILD_DIR) -c $? -o $@

$(BUILD_DIR):
	@mkdir $(BUILD_DIR)
```

This works, but translation is slow because each source file is separately translated.
Like javac, it's faster to translate all related files together, since parsing the 
source only needs to be done once.  The following changes improves the build time significantly:

```make
JAVA_SOURCES = $(TMPDIR).sources.list

translate: pre_translate $(OBJC_SOURCES)
	@if [ `cat $(JAVA_SOURCES) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(SOURCE_DIR) -d $(BUILD_DIR) \
	    `cat $(JAVA_SOURCES)` ; \
	fi

pre_translate: $(BUILD_DIR)
	@rm -f $(JAVA_SOURCES)
	@touch $(JAVA_SOURCES)

$(BUILD_DIR)/%.m $(BUILD_DIR)/%.h: $(SOURCE_DIR)/%.java
	@echo $? >> $(JAVA_SOURCES)
```