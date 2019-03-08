#!/bin/sh
#
# Clone github.com/google/jarjar and update local copy for Java 11.
#
# Author: alan.pincus@decafsoftware.com
#   Date: 3/2019
#
ASM7=http://central.maven.org/maven2/org/ow2/asm

git clone https://github.com/google/jarjar

curl ${ASM7}/asm/7.0/asm-7.0.jar                                               \
     -o jarjar/lib/asm-7.0.jar
curl ${ASM7}/asm-commons/7.0/asm-commons-7.0.jar                               \
     -o jarjar/lib/asm-commons-7.0.jar

rm jarjar/lib/asm-6.0.jar
rm jarjar/lib/asm-commons-6.0.jar

cd jarjar
git apply ../0001-Update-to-Java-11-using-asm-7.0.patch
ant
