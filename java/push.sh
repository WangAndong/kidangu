ant
RHOST=141.212.109.202
scp ~/studio/kidangu/java/kidangu.jar april@$RHOST:/home/april/jars
scp ~/studio/liblinear-java/liblinear.jar april@$RHOST:/home/april/jars
scp ~/april/java/april.jar april@$RHOST:/home/april/jars
scp ~/magic/java/magic.jar april@$RHOST:/home/april/jars
rsync -avz ~/studio/inria-person april@$RHOST:/home/april/

