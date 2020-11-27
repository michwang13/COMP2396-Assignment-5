import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
/**
 * The BigTwoClient class implements the CardGame interface 
 * and NetworkGame interface. It is used to model a Big Two
 * card game that supports 4 players playing over the internet. 
 * @author michael
 *
 */
public class BigTwoClient implements CardGame, NetworkGame{
	private int numOfPlayers;							// an integer specifying the number of players
	private Deck deck;									// a deck of cards
	private ArrayList<CardGamePlayer> playerList;		// a list of player
	private ArrayList<Hand> handsOnTable;				// a list of hands played on the table
	private int playerID;								// an integer specifying the playerID (i.e. index) of the local player
	private String playerName;							// a string specifying the name of the local player
	private String serverIP;							// a string specifying the IP address of the game server 
	private int serverPort;								// an integer specifying the TCP port of the game server
	private Socket sock;								// a socket connection to the game server
	private ObjectOutputStream oos;						// an ObjectOutputStream for sending messages to the server
	private int currentIdx;								// an integer specifying the index of the current player
	private BigTwoTable table;							// a Big Two table which builds the GUI for the game and handles all user actions.

	
	/**
	 * A constructor for creating a Big Two card game. Create 
	 * 4 players and add them to the player list. Also, create
	 * a 'console' (a BigTwoConsole object) for providing the
	 * user interface
	 *
	 */
	public BigTwoClient() {
		handsOnTable = new ArrayList<Hand>();
		
		playerList = new ArrayList<CardGamePlayer>();
		CardGamePlayer player1 = new CardGamePlayer();
		CardGamePlayer player2 = new CardGamePlayer();
		CardGamePlayer player3 = new CardGamePlayer();
		CardGamePlayer player4 = new CardGamePlayer();
		player1.setName("");
		player2.setName("");
		player3.setName("");
		player4.setName("");
		playerList.add(player1);
		playerList.add(player2);
		playerList.add(player3);
		playerList.add(player4);
		
		table = new BigTwoTable(this);
		JFrame frameEnterName = new JFrame();
		playerName = new String();
		playerName = JOptionPane.showInputDialog(frameEnterName, "Enter name: ");	
//		while (playerName == null || playerName == "") {
//			playerName = JOptionPane.showInputDialog(frameEnterName, "Enter name: ");	
//		}
		makeConnection();
		
	}
	/**
	 * A method for starting a Big Two card game. It should 
	 * create a Big Two card game, create and shuffle a deck 
	 * of cards, and start the game with the deck of cards.
	 * 
	 */
	public static void main(String[] args) {
		BigTwoClient bigTwoClient = new BigTwoClient();
	}
	
	/**
	 * A method for getting the number of players
	 * @return number of players
	 */
	public int getNumOfPlayers() {
		return numOfPlayers;
	}
	
	/**
	 * A method for retrieving the deck of cards being used.
	 * @return deck of cards being used
	 *
	 */
	public Deck getDeck() {
		return deck;
	}

	/**
	 * A method for retrieving the list of players.
	 * @return list of players
	 *
	 */
	public ArrayList<CardGamePlayer> getPlayerList() {
		return playerList;
	}

	/**
	 * A method for retrieving the list of hands played on the table.
	 * @return list of hands played on the table
	 *
	 */
	public ArrayList<Hand> getHandsOnTable() {
		return handsOnTable;
	}

	/**
	 * A method for retrieving the index of the current player.
	 * @return index of the current player
	 *
	 */
	public int getCurrentIdx() {
		return currentIdx;
	}

	/**
	 * A method for dividing cards to 4 players and find which
	 * player starts first (for getting 3 of Diamonds)
	 * @return index of the player that will start first
	 *
	 */
	private int divideCards() {
		int firstPlayerIdx = 0;
		for (int i = 0; i < 4; i++) {
			CardGamePlayer playerToGetCard = playerList.get(i);
			for (int j = 0; j < 13; j++) {
				BigTwoCard cardToAdd = (BigTwoCard) deck.getCard(j + ((i) * 13));
				if (cardToAdd.getRank() == 2 && cardToAdd.getSuit() == 0) {
					firstPlayerIdx = i;	
				}
				playerToGetCard.addCard(cardToAdd);
			}
		}
		return firstPlayerIdx;
	}
	
	/**
	 * A method to sort all players' cards according to Big Two rules
	 * from weakest to strongest
	 */
	private void sortPlayersCards() {
		for (int i = 0; i < 4; i++) {
			playerList.get(i).getCardsInHand().sort();
		}
		
	}
	/**
	 * A method for getting the player's selected cards based on
	 * the selected indices.
	 * @param player current player that chooses the cards
	 * @param selectedIndices indices that the player selected
	 * @return the cards selected by the player
	 */
	private CardList getSelectedCards(CardGamePlayer player, int[] selectedIndices) {
		CardList selectedCards = new CardList();
		for (int i = 0; i < selectedIndices.length; i++) {
			int selectedIndex = selectedIndices[i];
			CardList currentPlayerCards = player.getCardsInHand();
			Card selectedCard = currentPlayerCards.getCard(selectedIndex);
			selectedCards.addCard(selectedCard);
		}	
		return selectedCards;
	}
	
	/**
	 * A method for checking if a cardlist contain three of diamonds
	 * @return true if contain three of diamonds, otherwise false
	 */
	private boolean isContainThreeOfDiamonds(CardList selectedCards) {
		Card threeOfDiamonds = new Card(0, 2);
		if (selectedCards.contains(threeOfDiamonds)) {

			return true;
		}
		return false;
	}
	
	/** 
	 * A method for checking whether a specified hand is valid	 
	 * @param hand hand to check whether it is valid
	 * @return the hand if it is valid, otherwise null
	 */
	public static Hand validHand(Hand hand) {
		if (hand.isValid()) {
			return hand;
		}
		else {
			return null;
		}
	}
	
	/** 
	 * A method for returning a valid hand from the specified 
	 * list of cards of the player.
	 * @param player player to retrieve the valid hand from
	 * @param cards cards to retrieve the valid hand from
	 * @return the valid hand if a valid hand can be composed from the specified list of cards, otherwise null
	 */
	public static Hand composeHand(CardGamePlayer player, CardList cards) {
		if (cards.size() == 1) {
			Single singleHand = new Single(player, cards);
			return validHand(singleHand);
		}
		else if (cards.size() == 2) {
			Double doubleHand = new Double(player, cards);
			return validHand(doubleHand);
		}
		else if (cards.size() == 3) {
			Triple tripleHand = new Triple(player, cards);
			return validHand(tripleHand);
		}
		else if (cards.size() == 5) {
			StraightFlush straightFlushHand = new StraightFlush(player, cards);
			if (validHand(straightFlushHand) != null) {
				return straightFlushHand;
			}
			
			Straight straightHand = new Straight(player, cards);
			if (validHand(straightHand) != null) {
				return straightHand;
			}
			
			Flush flushHand = new Flush(player, cards);
			if (validHand(flushHand) != null) {
				return flushHand;
			}
			
			FullHouse fullHouseHand = new FullHouse(player, cards);
			if (validHand(fullHouseHand) != null) {
				return fullHouseHand;
			}
			
			Quad quadHand = new Quad(player, cards);
			if (validHand(quadHand) != null) {
				return quadHand;
			}
			return null;
		}
		else {
			return null;
		}
	}	
	
	/**
	 * A method for making a move by a player with the specified playerID 
	 * using the cards specified by the list of indices. This method should 
	 * be called from the BigTwoTable when the active player presses either 
	 * the "Play" or "Pass" button. Call the checkMove() method from the 
	 * CardGame interface with the playerID and cardIdx as the arguments. 
	 * @param playerID id of the player making a move
	 * @param cardIdx the indices of cards selected by the player
	 * 
	 */
	public void makeMove(int playerID, int[] cardIdx) {
		CardGameMessage message = new CardGameMessage(CardGameMessage.MOVE, -1, cardIdx);
		sendMessage(message);
	}
	
	/**
	 * A method for checking a move made by a player. This method should 
	 * be called from the makeMove() method from the CardGame interface. 
	 * @param playerID id of the player making a move
	 * @param cardIdx the indices of cards selected by the player
	 */
	public void checkMove(int playerID, int[] cardIdx) {
		CardGamePlayer player = playerList.get(playerID);
		CardList selectedCards = new CardList();
		Hand composedHand = null;
		Hand lastHandOnTable = (handsOnTable.isEmpty()) ? null : handsOnTable
				.get(handsOnTable.size() - 1);
		boolean isNewRound = true;
		
		// Check if this is a newRound
		if (lastHandOnTable != null && lastHandOnTable.getPlayer() == player) {
			isNewRound = true;
		}
		else if (lastHandOnTable == null) {
			isNewRound = true;
		}
		else {
			isNewRound = false;
		}
		
		if (cardIdx.length == 0) {
			// Player passes his turn
			if (isNewRound) {
//				table.printMsg("{pass} <== Not a legal move!!");
				table.printMsg("Not a legal move!!");
			}
			else {
				table.printMsg("{Pass}");
				currentIdx = (currentIdx + 1) % 4;
				table.setActivePlayer(currentIdx);
				if (currentIdx == this.playerID) {
					table.printMsg("Your turn:");
					table.enable();
				}
				else {
					table.printMsg(playerList.get(currentIdx).getName() + "'s turn:");					
					table.disable();
				}
			}
			table.resetSelected();
			table.repaint();
			return;
		}
		else {
			selectedCards = getSelectedCards(player, cardIdx);
			composedHand = composeHand(player, selectedCards);
			
			// Validate chosen cards
			if (lastHandOnTable == null) {
				if (composedHand == null) {
					String msg = "Not a legal move!!";
					table.printMsg(msg);
					return;
				}
				else if (!isContainThreeOfDiamonds(composedHand)) {
					printTurn(composedHand, false);
					return;
				}
			}
			else {
				if (composedHand == null) {
					String msg = "Not a legal move!!";
					table.printMsg(msg);
					return;
				}
				else if (isNewRound) {
					// Player does not have to choose cards that beats and have the same
					// size as the last hand on table
					;
				}
				else if (composedHand.size() != lastHandOnTable.size()) {
					printTurn(composedHand, false);
					return;
				}
				else if (!composedHand.beats(lastHandOnTable)) {
					printTurn(composedHand, false);
					return;
				}
			}
		}
		
		player.removeCards(composedHand);
		handsOnTable.add(composedHand);
		printTurn(composedHand, true);
		
		if (endOfGame()) {
			String messageDialogBox = "";
			messageDialogBox += "Game ends\n";
			for (int i = 0; i < 4; i++) {
				CardGamePlayer tempPlayer = playerList.get(i);
				if (tempPlayer.getNumOfCards() == 0) {
					messageDialogBox = messageDialogBox + tempPlayer.getName() + " wins the game.\n";
					continue;
				}
				messageDialogBox = messageDialogBox + tempPlayer.getName() + " has " + tempPlayer.getNumOfCards() + " in hand.\n";
			}
			table.resetSelected();
			table.repaint();			
			
			JFrame frameEndOfGame = new JFrame();			
			JOptionPane.showMessageDialog(frameEndOfGame, messageDialogBox);
			CardGameMessage endOfGameMessage = new CardGameMessage(CardGameMessage.READY, -1, null);
			sendMessage(endOfGameMessage);

		}
		else {
			currentIdx = (currentIdx + 1) % 4;	
			table.setActivePlayer(currentIdx);
			if (currentIdx == this.playerID) {
				table.printMsg("Your turn:");
				table.enable();
			}
			else {
				table.printMsg(playerList.get(currentIdx).getName() + "'s turn:");					
				table.disable();
			}
			table.resetSelected();
			table.repaint();
		}
	}
	
	/**
	 * A method for printing turn to the right console
	 * @param composedHand
	 */
	private void printTurn(Hand composedHand, boolean isValid) {
		if (isValid) {
			String msg = "{" + composedHand.getType() + "} " + composedHand.toString();
			table.printMsg(msg);
		}
		else {
			String msg = "Not a legal move!!";
			table.printMsg(msg);
		}
	}
	
	
	
	/**
	 * A method for checking if the game ends
	 * @return true if the game ends, otherwise false 
	 */
	public boolean endOfGame() {
		Hand lastHandOnTable = (handsOnTable.isEmpty()) ? null : handsOnTable
				.get(handsOnTable.size() - 1);
		if (lastHandOnTable == null) {
			return false;
		}
		else if (lastHandOnTable.getPlayer().getCardsInHand().isEmpty()) {
				return true;
		}
		return false;
	}
	
	
	
	/**
	 * A method for starting the game with a given deck of 
	 * cards supplied as the argument. Remove all the cards from
	 * the players as well as from the table, distribute the cards to
	 * the players, identify the player who holds the 3 of Diamonds, set
	 * both the currentIdx of the BigTwo instance and the activePlayer of 
	 * the BigTwoTable instance to the index of the player who holds the 3
	 * of diamonds
	 * @param deck shuffled deck of cards
	 *
	 */
	public void start(Deck deck) {
		this.deck = deck;
		for (int i = 0; i < 4; i++) {
			playerList.get(i).removeAllCards();
		}
		handsOnTable.clear();
		
		currentIdx = divideCards();
		sortPlayersCards();
		
		table.disable();
		table.setActivePlayer(currentIdx);
		table.reset();		
		table.printMsg("All players are ready. Game starts.");
		if (currentIdx == this.playerID) {
			table.printMsg("Your turn:");
			table.enable();
		}
		else {
			table.printMsg(playerList.get(currentIdx).getName() + "'s turn:");					
			table.disable();
		}
		table.repaint();
	}
	
	@Override
	public int getPlayerID() {
		return playerID;
	}
	@Override
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}
	@Override
	public String getPlayerName() {
		return playerName;
	}
	@Override
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	@Override
	public String getServerIP() {
		return serverIP;
	}
	@Override
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	@Override
	public int getServerPort() {
		return serverPort;
	}
	@Override
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	@Override
	public void makeConnection() {
		try {
			sock = new Socket("127.0.0.1", 2396);	
			oos = new ObjectOutputStream(sock.getOutputStream());
			table.printMsg("Connected to server at /127.0.0.1:2396");
			Thread readerThread = new Thread(new ServerHandler());
			readerThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}		

	}
	@Override
	public void parseMessage(GameMessage message) {
		
		int messageType = message.getType();
		
		switch (messageType) {
			case CardGameMessage.MOVE:
				int movingPlayerID = message.getPlayerID();
				int[] selectedIndices = (int[]) message.getData();
				checkMove(movingPlayerID, selectedIndices);
				break;
				
			case CardGameMessage.PLAYER_LIST:
				// Set the playerID and 
				this.playerID = message.getPlayerID();			
				// Update the names in the player list
				
				String[] playerNames = (String[]) message.getData();
				for (int i = 0; i < playerNames.length; i++) {
					if (playerNames[i] != null) {
						playerList.get(i).setName(playerNames[i]);
					}
				}

				// Send join message
				CardGameMessage joinMessage = new CardGameMessage(CardGameMessage.JOIN, -1, playerName);
				sendMessage(joinMessage);

				break;
				
			case CardGameMessage.JOIN:
				// Update the names in the player list
				int joiningPlayerID = message.getPlayerID();
				String joiningPlayerName = (String) message.getData();
				playerList.get(joiningPlayerID).setName(joiningPlayerName);
				if (joiningPlayerID == playerID) {
					CardGameMessage readyMessage = new CardGameMessage(CardGameMessage.READY, -1, null);
					sendMessage(readyMessage);
				}
				else {
					table.printMsg(joiningPlayerName + " joins the game.");					
				}
				break;
				
			case CardGameMessage.FULL:
				table.printMsg("Server is full, cannot join the game.");
				break;
				
			case CardGameMessage.QUIT:
				int quittingPlayerID = message.getPlayerID();
				String quittingPlayerName = playerList.get(quittingPlayerID).getName();
				table.printMsg(quittingPlayerName + " leaves the game.");
				playerList.get(quittingPlayerID).setName("");
				for (int i = 0; i < 4; i++) {
					if (playerList.get(i).getName() != null && playerList.get(i).getName() != "") {
						table.printMsg(playerList.get(i).getName() + " is ready.");
					}
					playerList.get(i).removeAllCards();
				}
				
				CardGameMessage readyMessage = new CardGameMessage(CardGameMessage.READY, -1, null);
				sendMessage(readyMessage);
				table.repaint();
				break;
				
			case CardGameMessage.READY:
				int readyPlayerID = message.getPlayerID();
				table.printMsg(playerList.get(readyPlayerID).getName() + " is ready.");
				table.repaint();
				break;
			
			case CardGameMessage.START:
				BigTwoDeck playedDeck = (BigTwoDeck) message.getData();
				start(playedDeck);
				break;
			
			case CardGameMessage.MSG:
				String chatMsg = (String) message.getData();
				table.printChatMsg(chatMsg);
				break;
			
		}
	}

	
	@Override
	public void sendMessage(GameMessage message) {
		try {
			oos.writeObject(message);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	public class ServerHandler implements Runnable{
		private ObjectInputStream ois;
		
		@Override
		public void run() {
			CardGameMessage message;
			try {
			ois = new ObjectInputStream(sock.getInputStream());
				while ((message = (CardGameMessage) ois.readObject()) != null) {
					System.out.println("read " + message);
					parseMessage(message);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}

	


