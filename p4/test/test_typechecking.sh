#!/bin/bash

SCRIPT="./compiler"

TESTS_OK=type_checker/*ok.tgr
TESTS_BAD=type_checker/*bad.tgr
ADDITIONAL_TESTS_OK=parser/additional_tests/*ok.tgr
ADDITIONAL_TESTS_BAD=parser/additional_tests/*bad.tgr
FEEDBACK_OK=feedback_ok.txt
FEEDBACK_BAD=feedback_bad.txt
FEEDBACK=feedback.txt

cp ../compiler .
cp ../compiler.jar .


# 2>filename
#   # Redirect and append stderr to file "filename."
# &>filename
#   # Redirect both stdout and stderr to file "filename."

# echo -n suppresses the newline
echo -n "" > $FEEDBACK_OK # for ok tests
echo -n "" > $FEEDBACK_BAD # for bad tests
echo -n "" > $FEEDBACK # all combined
echo "Running typechecking tests"

# First check *_ok.tgr files for false positives
for f in $TESTS_OK
do
  echo -n "-----$f: "
  $SCRIPT $f &> ${f%.tgr}_fb.txt
  # -s: if file is not zero size
  if [ -s ${f%.tgr}_fb.txt ]
  then
    cat ${f%.tgr}_fb.txt
    echo "-----${f%.tgr}_fb.txt:" >> $FEEDBACK_OK
  else
    echo "passed"
  fi
  cat ${f%.tgr}_fb.txt >> $FEEDBACK_OK
  rm ${f%.tgr}_fb.txt
done

if ! [ -s $FEEDBACK_OK ]
then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "!!!!!!!!!!!! No False Positives !!!!!!!!!!!!!"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!!!!!! No False Positives !!!!!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
else
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!! There Are False Positives !!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
fi

echo
echo "***** Running bad tests that are expected to fail. *****"
echo

# Then check *_bad.tgr files for false negatives
# Still need to make sure true positives are
# for the right reason
for f in $TESTS_BAD
do
  echo -n "-----$f: "
  $SCRIPT $f &> ${f%.tgr}_fb.txt
  # -s: if file is not zero size
  if [ -s ${f%.tgr}_fb.txt ]
  then
    cat ${f%.tgr}_fb.txt
    echo "-----${f%.tgr}_fb.txt:" >> $FEEDBACK_BAD
  else
    echo "false negative"
    echo "-----${f%.tgr}_fb.txt: false negative" >> $FEEDBACK_BAD
  fi
  cat ${f%.tgr}_fb.txt >> $FEEDBACK_BAD
  rm ${f%.tgr}_fb.txt
done

cat $FEEDBACK_OK >> $FEEDBACK
echo "" >> $FEEDBACK
echo "***** Running bad tests that are expected to fail. *****" >> $FEEDBACK
echo >> $FEEDBACK
cat $FEEDBACK_BAD >> $FEEDBACK
rm $FEEDBACK_OK
rm $FEEDBACK_BAD

echo
echo "************************************"
echo "********* ADDITIONAL TESTS *********"
echo "************************************"
echo

echo >> $FEEDBACK
echo "************************************" >> $FEEDBACK
echo "********* ADDITIONAL TESTS *********" >> $FEEDBACK
echo "************************************" >> $FEEDBACK
echo >> $FEEDBACK

# echo -n suppresses the newline
echo -n "" > $FEEDBACK_OK # for ok tests
echo -n "" > $FEEDBACK_BAD # for bad tests
echo "Running typechecking tests"

for f in $ADDITIONAL_TESTS_OK
do
  echo -n "-----$f: "
  $SCRIPT $f &> ${f%.tgr}_fb.txt
  # -s: if file is not zero size
  if [ -s ${f%.tgr}_fb.txt ]
  then
    cat ${f%.tgr}_fb.txt
    echo "-----${f%.tgr}_fb.txt:" >> $FEEDBACK_OK
  else
    echo "passed"
  fi
  cat ${f%.tgr}_fb.txt >> $FEEDBACK_OK
  rm ${f%.tgr}_fb.txt
done

if ! [ -s $FEEDBACK_OK ]
then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "!!!!!!!!!!!! No False Positives !!!!!!!!!!!!!"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!!!!!! No False Positives !!!!!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
else
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!! There Are False Positives !!!!!!!!!!" >> $FEEDBACK
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" >> $FEEDBACK
fi

echo
echo "***** Running bad tests that are expected to fail. *****"
echo

# Then check *_bad.tgr files for false negatives
# Still need to make sure true positives are
# for the right reason
for f in $ADDITIONAL_TESTS_BAD
do
  echo -n "-----$f: "
  $SCRIPT $f &> ${f%.tgr}_fb.txt
  # -s: if file is not zero size
  if [ -s ${f%.tgr}_fb.txt ]
  then
    cat ${f%.tgr}_fb.txt
    echo "-----${f%.tgr}_fb.txt:" >> $FEEDBACK_BAD
  else
    echo "false negative"
    echo "-----${f%.tgr}_fb.txt: false negative" >> $FEEDBACK_BAD
  fi
  cat ${f%.tgr}_fb.txt >> $FEEDBACK_BAD
  rm ${f%.tgr}_fb.txt
done

cat $FEEDBACK_OK >> $FEEDBACK
echo "" >> $FEEDBACK
echo "***** Running bad tests that are expected to fail. *****" >> $FEEDBACK
echo >> $FEEDBACK
cat $FEEDBACK_BAD >> $FEEDBACK
rm $FEEDBACK_OK
rm $FEEDBACK_BAD

rm compiler 
rm compiler.jar