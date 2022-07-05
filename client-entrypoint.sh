mvn install -pl -Mage.Server.Plugins,-Mage.Server.Console,-Mage.Server,-Mage.Tests,-Mage.Verify,-:mage-game-brawlduel,-:mage-player-ai,-:mage-game-brawlfreeforall,-:mage-game-commanderduel,-:mage-game-commanderfreeforall,-:mage-deck-limited,-:mage-game-freeforall,-:mage-game-freeformcommanderduel,-:mage-player-ai-draftbot,-:mage-player-ai-ma,-:mage-player-ai-mcts,-:mage-player-aiminimax,-:mage-game-pennydreadfulcommanderfreeforall,-:mage-game-tinyleadersduel

# Compile the client app
cd Mage.Client || echo >> "Failed to cd into Mage.Client" && exit
mvn assembly:assembly

# cd into the newly compiled client app, unzip it, and run it
cd target || echo >> "Failed to cd into Mage.Client/target" && exit
unzip mage-client.zip
./startClient.sh
