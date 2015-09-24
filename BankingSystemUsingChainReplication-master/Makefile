JCC = javac

# Compile all the files from the subdirectories
all: 
	$(JCC) common/*.java
	$(JCC) client/*.java
	$(JCC) server/*.java
	$(JCC) master/*.java


# Remove all the class files and the log files
clean: 
	rm -rf logs/*
	rm common/*.class
	rm client/*.class
	rm server/*.class
	rm master/*.class
