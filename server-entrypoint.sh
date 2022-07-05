# This command is needed to build/update the H2 database
# Include --fail-never because some client packages fail to build
# This is fine because Mage.Client is built separately in its own container
mvn install -DskipTests -pl -Mage.Client

# Compile the server app
cd Mage.Server || echo >> "Failed to cd into Mage.Server" && exit
mvn assembly:assembly

# cd into the newly compiled server app, unzip it, and run it
cd target || echo >> "Failed to cd into Mage.Server/target" && exit
unzip mage-server.zip
./startServer.sh
