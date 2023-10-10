cd .. &&
rm *.class ;
javac ChatClient.java &&
javac ChatServer.java &&
mv *.class tests &&
cd tests
