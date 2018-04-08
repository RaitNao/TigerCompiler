#!/bin/bash

SCRIPT="./compiler"

FILES=parser/*.tgr
TESTS_OK=parser/additional_tests/*ok.tgr
TESTS_BAD=parser/additional_tests/*bad.tgr

cp ../compiler .
cp ../compiler.jar .


let runs=0
let passed=0
let failed=0

echo "Generating ASTs for tests"
for f in $FILES
do
  let runs++
  #echo "Processing $f to ${f%.tgr}.ast"
  # generate ast file
  $SCRIPT $f --ast --no-type-check > ${f%.tgr}.ast
  # generate diff file
  diff -iw ${f%.tgr}_correct.ast ${f%.tgr}.ast > ${f%.tgr}.diff
  # if diff file is non-empty (indicating a difference)
  if [ -s ${f%.tgr}.diff ]
  then
    echo "The AST for ${f%.tgr} differs:"
    cat ${f%.tgr}.diff
    let failed++
  else
    let passed++
  fi
  rm ${f%.tgr}.diff
  rm ${f%.tgr}.ast
done

echo "passed: $passed, failed: $failed, your grade: $passed/$runs"

echo
echo
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo "!!!!!!!!!!! SOLUTION GRADER TESTS !!!!!!!!!!!"
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo

echo "Running parser tests"

# First check *_ok.tgr files to make sure they all parse and emit the right ast
for f in $TESTS_OK
do
  echo -----$f...
  eval $SCRIPT $f --ast --no-type-check &> ${f%.tgr}.out
  diff -b ${f%.tgr}.out ${f%.tgr}.ast  \
      || echo Generated AST differs! Please inspect.
  rm ${f%.tgr}.out
done

echo
echo "**********************************************************"
echo "***** Please manually inspect the following outputs. *****"
echo "***** Running bad tests that are expected to fail.   *****"
echo "**********************************************************"
echo

# Then check *_bad.tgr files to make sure they all fail to parse.
for f in $TESTS_BAD
do
    echo -----$f:
    if $SCRIPT $f --ast --no-type-check 1>/dev/null; then
        echo " ^^ test did not fail as expected, stdout is not empty"
    fi
done

rm compiler 
rm compiler.jar