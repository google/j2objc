#!/bin/bash
#
# Execute javac command, filtering out the non-suppressable warnings.

JAVAC=$1
shift
${JAVAC} $* 2> >(grep -v '^Note:')
