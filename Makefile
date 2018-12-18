JAVAC = javac
JAVA = java

SRC_DIR = ./src ./src/constant
SRCS = $(foreach dir, $(SRC_DIR), $(wildcard $(dir)/*.java))
# SRCS = $(SRC_DIR)/*.java
LIB_DIR = ./lib/jbotsim
# LIB_SRCS = $(foreach dir, $(LIB_DIR), $(wildcard $(dir)/*.jar))
LIB_SRCS = $(LIB_DIR)/*
CLASS_DIR = ./bin
# CLASS_SRCS = $(foreach dir, $(CLASS_DIR), $(wildcard $(dir)/*.class))
CLASS_SRCS = $(CLASS_DIR)/*.class
ICON_DIR = ./src/icon
ICON_SRC = $(ICON_DIR)/node.png

CLASSPATH = $(foreach file, $(LIB_SRCS), $(file):)$(CLASS_DIR)

MAIN = Main

LOG_DIR = ./log
LOG_FILE = $(LOG_DIR)/log

.PHONY: all clean

all: compile execution
compile:
	mkdir -p $(CLASS_DIR)/icon
	cp $(ICON_SRC) $(CLASS_DIR)/icon
	$(JAVAC) -cp $(CLASSPATH) -d $(CLASS_DIR) $(SRCS)

execution:
	mkdir -p $(LOG_DIR)
	$(JAVA) -cp $(CLASSPATH) $(MAIN) > $(LOG_FILE)

clean:
	rm -rf $(CLASS_DIR)
