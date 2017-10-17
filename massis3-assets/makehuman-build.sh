#!/usr/bin/env bash

##################
# http://stackoverflow.com/a/246128/3315914
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
##################

if [[ "$1" != "" ]]; then
    nThreads="$1"
else
    nThreads=1
fi

cd "$DIR"
MAKEHUMAN_INPUT_DIR=$(realpath "./makehuman")
MAKEHUMAN_OUTPUT_DIR=$(realpath "./models")
ANIMATIONS_INPUT_DIR=$(realpath "./animations")
if [ ! -d "$MAKEHUMAN_INPUT_DIR" ]; then
  echo 'Directory "makehuman" does not exist. Exiting'
  exit -1
fi

if [ ! -d "$MAKEHUMAN_OUTPUT_DIR" ]; then
  echo "Directory $MAKEHUMAN_OUTPUT_DIR does not exist. Exiting"
  exit -1
fi

rm -rf "$MAKEHUMAN_OUTPUT_DIR/*"
docker run -t \
-v $MAKEHUMAN_INPUT_DIR:/input \
-v $MAKEHUMAN_OUTPUT_DIR:/output \
-v $ANIMATIONS_INPUT_DIR:/animations \
rpax/massis3-mkh /bin/run-converter
#docker run -t -v $MAKEHUMAN_INPUT_DIR:/input -v $MAKEHUMAN_OUTPUT_DIR:/output rpax/massis3-mkh /bin/run-converter "$nThreads"
