#!/bin/bash
find . -name *.java > sources.txt
javac @sources.txt -d classes
rm sources.txt

