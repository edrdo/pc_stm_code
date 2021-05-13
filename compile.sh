H=$(dirname $0)
SCALA_LIBS=$H/lib/scala-library-2.12.0.jar:$H/lib/scala-stm_2.12-0.8.jar
CLASSES_DIR=$H/classes
mkdir -p $CLASSES_DIR
javac -cp $SCALA_LIBS -d $CLASSES_DIR $SOURCES $H/src/pc/stm/*.java

