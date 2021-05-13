H=$(dirname $0)
SCALA_LIBS=$H/lib/scala-library-2.12.0.jar:$H/lib/scala-stm_2.12-0.8.jar
CLASSES_DIR=$H/classes

java -cp $SCALA_LIBS:$CLASSES_DIR $*

