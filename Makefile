JAVAC = javac
JAVA = java

SRC_DIR = ./src ./src/constant
SRCS = $(foreach dir,$(SRC_DIR),$(wildcard $(dir)/*.java))
LIB_SRCS = ./lib/jbotsim/*:./lib/poi-4.0.1/*:./lib/poi-4.0.1/lib/*:./lib/poi-4.0.1/ooxml-lib/*
CLASS_DIR = ./bin
ICON_DIR = ./src/icon
ICON_SRC = $(ICON_DIR)/node.png

CLASSPATH = $(LIB_SRCS):$(CLASS_DIR)

MAIN = Main

LOG_DIR = ./log
LOG_FILE = $(LOG_DIR)/log
DATA_FILES = $(LOG_DIR)/data_raw $(LOG_DIR)/data_summary

.PHONY: all clean

all: compile execution
compile:
	mkdir -p $(CLASS_DIR)/icon
	cp $(ICON_SRC) $(CLASS_DIR)/icon
	$(JAVAC) -cp $(CLASSPATH) -d $(CLASS_DIR) $(SRCS)

execution:
	mkdir -p $(LOG_DIR)
	$(foreach file,$(DATA_FILES),eval rm -f $(file))
	$(JAVA) -cp $(CLASSPATH) $(MAIN) > $(LOG_FILE)

clean:
	rm -rf $(CLASS_DIR)
