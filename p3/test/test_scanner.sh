#!/bin/bash

SCRIPT="./compiler"

FILES=scanner/*.tgr
ADDITIONAL_FILES=scanner/additional_tests/*.tgr

cp ../compiler .
cp ../compiler.jar .


let runs=0
let passed=0
let failed=0

echo "Generating tokens for tests"
for f in $FILES
do
  let runs++
  #echo "Processing $f to ${f%.tgr}.tokens"
  # generate tokens file
  $SCRIPT $f --tokens --no-type-check > ${f%.tgr}.tokens
  # generate diff file
  diff -iw ${f%.tgr}_correct.tokens ${f%.tgr}.tokens > ${f%.tgr}.diff
  # if diff file is non-empty (indicating a difference)
  if [ -s ${f%.tgr}.diff ]
  then
    echo "The tokens for ${f%.tgr} differs:"
    if which diffstat &>/dev/null ; then
        diffstat ${f%.tgr}.diff
    else
        cat ${f%.tgr}.diff
    fi
    let failed++
  else
    let passed++
  fi
  rm ${f%.tgr}.diff
  rm ${f%.tgr}.tokens
done

echo "passed: $passed, failed: $failed, your grade: $passed/$runs"

echo
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo "!!!!!!!!!!! SOLUTION GRADER TESTS !!!!!!!!!!!"
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo


let runs=0
let passed=0
let failed=0

echo "Generating tokens for tests"
for f in $ADDITIONAL_FILES
do
  let runs++
  #echo "Processing $f to ${f%.tgr}.tokens"
  # generate tokens file
  $SCRIPT $f --tokens --no-type-check > ${f%.tgr}.tokens
  # generate diff file
  diff -iw ${f%.tgr}_correct.tokens ${f%.tgr}.tokens > ${f%.tgr}.diff
  # if diff file is non-empty (indicating a difference)
  if [ -s ${f%.tgr}.diff ]
  then
    echo "The tokens for ${f%.tgr} differs:"
    if which diffstat &>/dev/null ; then
        diffstat ${f%.tgr}.diff
    else
        cat ${f%.tgr}.diff
    fi
    let failed++
  else
    let passed++
  fi
  rm ${f%.tgr}.diff
  rm ${f%.tgr}.tokens
done

echo "passed: $passed, failed: $failed, your grade: $passed/$runs"

rm compiler 
rm compiler.jar