compile: src/*.java
	rm -rf ./out
	mkdir out
	javac -d out src/*.java -d out/

run:
	java -classpath out/ threadedpatternfinder.ProducerConsumer

clean:
	rm -rf ./out


