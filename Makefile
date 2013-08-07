PROG=RabbitAsyncEventListener.jar
JC = javac
IP = 172.16.81.133
PWD = $(shell pwd)
JAVAC_OPTS = -Xlint:deprecation

all: jar/$(PROG)

jar/$(PROG): build/$(PROG:%.jar=%.class)
	cd $(PWD)/build; \
	jar cvfe $(PWD)/jar/RabbitAsyncEventListener.jar $(PROG:%.jar=%) sqlfire/callbacks/*.class

build/$(PROG:%.jar=%.class):
	cd $(PWD)/src/main/java; \
	javac $(JAVAC_OPTS) sqlfire/callbacks/RabbitAsyncEventListener.java -d $(PWD)/build/

install:
	test -f jar/$(PROG)
	sqlf run -client-bind-address=$(IP) -file=sql/install.sql

uninstall:
	sqlf run -client-bind-address=$(IP) -file=sql/uninstall.sql

clean: uninstall
	rm -rf jar/*
	rm -rf build/*
