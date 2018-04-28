#!/bin/bash
TESTS_OK=controlflow/*.tgr

cp ../compiler .
cp ../compiler.jar .

for f in $TESTS_OK
do
    echo -n "-----$f: "
    echo
    ./compiler $f 2>&1
done

rm compiler
rm compiler.jar