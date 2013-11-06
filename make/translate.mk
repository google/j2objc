# Defines the "translate" target.
#
# The including makefile must define the variables:
#   TRANSLATE_JAVA_FULL
#   TRANSLATE_JAVA_RELATIVE
# And optional variables:
#   TRANSLATE_ARGS
#
# Author: Keith Stanger

TRANSLATE_SOURCES = $(TRANSLATE_JAVA_RELATIVE:%.java=$(GEN_OBJC_DIR)/%.m)
TRANSLATE_HEADERS = $(TRANSLATE_SOURCES:.m=.h)
TRANSLATE_OBJC = $(TRANSLATE_SOURCES) $(TRANSLATE_HEADERS)
TRANSLATE_TARGET = $(BUILD_DIR)/.translate_mark

translate: $(TRANSLATE_TARGET)
	@:

$(TRANSLATE_TARGET): $(TRANSLATE_JAVA_FULL)
	@echo Translating sources.
	@mkdir -p $(GEN_OBJC_DIR)
	@$(DIST_DIR)/j2objc -d $(GEN_OBJC_DIR) $(TRANSLATE_ARGS) $?
	@touch $@

$(TRANSLATE_OBJC): $(TRANSLATE_TARGET)
	@:
