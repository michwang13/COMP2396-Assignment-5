import javax.swing.*;

import java.util.ArrayList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.awt.Image;


/**
 * 
 */

/**
 * The BigTwoTable class implements the CardGameTable
 * interface. It is used to build a GUI for the Big Two
 * card game and handle all user actions.
 * @author michael
 *
 */
public class BigTwoTable implements CardGameTable {
	private BigTwoClient game;				// a card game associated with this table
	private boolean[] selected;			// a boolean array indicating which cards are being selected
	private int activePlayer;			// an integer specifying the index of the active player
	private JFrame frame;				// the main window of the application
	private JPanel bigTwoPanel;			// a panel for showing the cards of each player and the cards played on the table
	private JButton playButton;			// a "Play" button for the active player to play the selected cards
	private JButton passButton;			// a "Pass" button for the active player to pass his/her turn to the next player
	private JTextArea msgArea;			// a text area for showing the current game status
	private JTextArea chatMsgArea;      // a text area for showing the game messages by players 
	private JTextField chatMsgField;	// a text field for receiving player's messages
	private Image[][] cardImages;		// a 2D array storing the images for the faces of the cards
	private Image cardBackImage;		// an image for the backs of the cards
	private Image[] avatars;			// an array storing the images for the avatars
	
	
	/**
	 * A method to assign images to avatars
	 */
	private void assignAvatars() {
		avatars = new Image[4];
		ImageIcon imageIcon0 = new ImageIcon("images/captain_america.jpg");
		ImageIcon imageIcon1 = new ImageIcon("images/iron_man.jpg");
		ImageIcon imageIcon2 = new ImageIcon("images/black_widow.jpg");
		ImageIcon imageIcon3 = new ImageIcon("images/thor.jpg");
		
		Image scaledImage0 = imageIcon0.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
		Image scaledImage1 = imageIcon1.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
		Image scaledImage2 = imageIcon2.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
		Image scaledImage3 = imageIcon3.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
		
		avatars[0] = scaledImage0;
		avatars[1] = scaledImage1;
		avatars[2] = scaledImage2;
		avatars[3] = scaledImage3;
	}
	
	/**
	 * A method to assign images to card images
	 */
	private void assignCardImages() {
		cardImages = new Image[4][13];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 13; j++) {
				String suit = new String();
				String rank = new String();
				if (i == 0) {
					suit = "d";
				}
				else if (i == 1) {
					suit = "c";
				}
				else if (i == 2) {
					suit = "h";
				}
				else if (i == 3) {
					suit = "s";
				}
				
				if (j == 0) {
					rank = "a"; 
				}
				else if (j >= 1 && j <= 8) {
					rank = Integer.toString(j+1);
				}
				else if (j == 9) {
					rank = "t";
				}
				else if (j == 10) {
					rank = "j";
				}
				else if (j == 11) {
					rank = "q";
				}
				else if (j == 12) {
					rank = "k";
				}
				
				ImageIcon imageIcon = new ImageIcon("images/" + rank + suit + ".gif");
				Image scaledImage = imageIcon.getImage().getScaledInstance(50, 70, Image.SCALE_SMOOTH);
				cardImages[i][j] = scaledImage;
			}
		}		
	}
	
	/**
	 * A constructor for creating a BigTwoTable. The parameter
	 * game is a reference to a card game associated with this table
	 * @param game card game associated with this table
	 */
	public BigTwoTable(CardGame game) {
		this.game = (BigTwoClient) game;
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Assign Images
		assignAvatars();
		assignCardImages();
		ImageIcon cardBackImageIcon = new ImageIcon("images/b.gif");
		Image scaledCardBackImage = cardBackImageIcon.getImage().getScaledInstance(50, 70, Image.SCALE_SMOOTH);
		cardBackImage = scaledCardBackImage;
		
		// Set Menu Bar
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");
		JMenuItem connectMenuItem = new JMenuItem("Connect");
		JMenuItem quitMenuItem = new JMenuItem("Quit");
		connectMenuItem.addActionListener(new ConnectMenuItemListener());
		quitMenuItem.addActionListener(new QuitMenuItemListener());
		gameMenu.add(connectMenuItem);
		gameMenu.add(quitMenuItem);
		JMenu messageMenu = new JMenu("Message");
		JMenuItem testMenuItem = new JMenuItem("Test");
		//jgn lupa tambahin actionlistener, jangan lupa remove(?)
		messageMenu.add(testMenuItem);
		menuBar.add(gameMenu);
		menuBar.add(messageMenu);
		frame.setJMenuBar(menuBar);
		
		// Set Bottom Panel
		JPanel bottomPanel = new JPanel();
		JLabel messageFieldLabel = new JLabel("Message: ");
		chatMsgField = new JTextField(20);
		chatMsgField.addActionListener(new ChatMsgFieldListener());
		playButton = new JButton("Play");
		passButton = new JButton("Pass");
		bottomPanel.add(playButton);
		bottomPanel.add(passButton);
		bottomPanel.add(messageFieldLabel);
		bottomPanel.add(chatMsgField);
		frame.add(bottomPanel, BorderLayout.SOUTH);
		
		// Set Right Panel
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		msgArea = new JTextArea(5,27);
		msgArea.setLineWrap(true);
		JScrollPane msgAreaScroller = new JScrollPane(msgArea);
		chatMsgArea = new JTextArea(5,27);
		chatMsgArea.setLineWrap(true);
		JScrollPane chatMsgAreaScroller = new JScrollPane(chatMsgArea);
		rightPanel.add(msgAreaScroller);
		rightPanel.add(chatMsgAreaScroller);
		frame.add(rightPanel, BorderLayout.EAST);	
		

		// Set BigTwoPanel
		bigTwoPanel = new BigTwoPanel();
		frame.add(bigTwoPanel);
		
//		bigTwoPanel.setSize(600, 700);
		
		frame.setSize(800, 700);
		frame.setVisible(true);
	}
	
	/**
	 * A method for setting the index of the active 
	 * player (i.e., the current player).
	 * @param activePlayer the active player
	 */
	public void setActivePlayer(int activePlayer) {
		this.activePlayer = activePlayer;
		
	}
	
	/**
	 * A method for getting an array of indices of the cards selected.
	 * @return an array indices of the cards selected
	 */
	public int[] getSelected() {
		ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
	
		for (int i = 0; i < selected.length; i++) {
			if (selected[i] == true) {
				selectedIndices.add(i);
			}
		}
		
		int selectedIndicesArr[] = new int[selectedIndices.size()];
		for (int i = 0; i < selectedIndicesArr.length; i++) {
			selectedIndicesArr[i] = selectedIndices.get(i);
		}
		
		return selectedIndicesArr;

	}
	
	/**
	 * A method for resetting the list of selected cards.
	 */
	public void resetSelected() {
		int currentPlayerNumOfCards = game.getPlayerList().get(activePlayer).getNumOfCards();
		selected = new boolean[currentPlayerNumOfCards];
		for (int i = 0; i < currentPlayerNumOfCards; i++) {
			selected[i] = false;
		}
	}
	
	/**
	 * A method for repainting the GUI
	 */
	public void repaint() {
//		frame.removeMouseListener((MouseListener) bigTwoPanel);
//		for (MouseListener ml : frame.getMouseListeners()) {
//			frame.removeMouseListener(ml);
//		}
//		bigTwoPanel = null;
//		bigTwoPanel = new BigTwoPanel();
//		bigTwoPanel.setSize(600, 700);
//		
//		frame.addMouseListener((MouseListener) bigTwoPanel);
//		frame.add(bigTwoPanel);
		System.out.println("weng");
		frame.repaint();
		frame.setSize(900, 700);
		frame.setVisible(true);
	}
	
	/**
	 * A method for printing the specified string to 
	 * the message area of the GUI.
	 * @param msg string to be printed
	 */
	public void printMsg(String msg) {
		msgArea.append(msg);
		msgArea.append("\n");
	}
	
	/**
	 * A method for clearing the message area of the GUI
	 */
	public void clearMsgArea() {
		msgArea.setText("");
	}
	
	/**
	 * A method for printing the specified string to 
	 * the chat message area of the GUI.
	 * @param msg string to be printed
	 */
	public void printChatMsg(String msg) {
		chatMsgArea.append(msg);
		chatMsgArea.append("\n");
	}
	
	/**
	 * A method for clearing the c=hat message area of the GUI
	 */
	public void clearChatMsgArea() {
		chatMsgArea.setText("");
	}
	
	/**
	 * A method for resetting the GUI. Reset the list of selected cards,
	 * clear the message area, and enable user interactions
	 */
	public void reset() {
		resetSelected();
		clearMsgArea();
		enable();
	}
	
	/**
	 * A method for enabling user interactions with the GUI. Enable
	 * the "Play" button and "Pass" button, and enable the BigTwoPanel
	 * for selection of cards through mouse clicks
	 */
	public void enable() {
		playButton.setEnabled(true);
		passButton.setEnabled(true);
		playButton.addActionListener(new PlayButtonListener());
		passButton.addActionListener(new PassButtonListener());
		frame.addMouseListener((MouseListener) bigTwoPanel);
		
		//TODO: PERLU ADD ACTION LISTENER BUAT CHATMSGAREA GAK?
	}
	
	/**
	 * A method for disabling user interactions with the GUI. Disable
	 * the "Play" button and "Pass" button, and disable the BigTwoPanel
	 * for selection of cards through mouse clicks
	 */
	public void disable() {
		playButton.setEnabled(false);
		passButton.setEnabled(false);
		for (ActionListener al : playButton.getActionListeners()) {
			playButton.removeActionListener(al);
		}
		for (ActionListener al : passButton.getActionListeners()) {
			passButton.removeActionListener(al);
		}
		frame.removeMouseListener((MouseListener) bigTwoPanel);
	
		//TODO: PERLU REMOVE ACTION LISTENER BUAT CHATMSGAREA GAK?
	}
	
	/**
	 * A method do draw avatars on the bigTwoPanel
	 * @param g Graphics to be printed on
	 * @param playerIdx index of the player of the avatar
	 * @param x x-coordinate of where to draw the avatar
	 * @param y y-coordinate of where to draw the avatar
	 * @param observer the image observer
	 */
	private void drawAvatar(Graphics g, int playerIdx, int x, int y, ImageObserver observer) {
		g.setColor(Color.BLACK);
		String playerName = game.getPlayerList().get(playerIdx).getName();
		g.drawString(playerName, x, y);
		Image image = avatars[playerIdx];
		g.drawImage(image, x, y+10, observer);

	}

	/**
	 * A method do draw player's cards on the bigTwoPanel
	 * @param g Graphics to be printed on
	 * @param playerIdx index of the player of the avatar
	 * @param x x-coordinate of where to draw the avatar
	 * @param y y-coordinate of where to draw the avatar
	 * @param observer the image observer
	 */
	private void drawPlayerCards(Graphics g, int playerIdx, int x, int y, ImageObserver observer) {
		CardList playerCards = game.getPlayerList().get(playerIdx).getCardsInHand();
		if (playerIdx != activePlayer) {
			for (int i = 0; i < playerCards.size(); i++) {
				g.drawImage(cardBackImage, x+25*i, y, observer);
			}
		}
		else {
			for (int i = 0; i < playerCards.size(); i++) {
				int playerCardSuit = playerCards.getCard(i).getSuit();
				int playerCardRank = playerCards.getCard(i).getRank(); 
				if (selected[i]) {
					g.drawImage(cardImages[playerCardSuit][playerCardRank], x + 25*i, y-20, observer);					
				}
				else {
					g.drawImage(cardImages[playerCardSuit][playerCardRank], x + 25*i, y, observer);
				}
			}
		}
	}	

	/**
	 * A method do draw last hand on table on the bigTwoPanel
	 * @param g Graphics to be printed on
	 * @param playerIdx index of the player of the avatar
	 * @param x x-coordinate of where to draw the avatar
	 * @param y y-coordinate of where to draw the avatar
	 * @param observer the image observer
	 */
	private void drawLastHand(Graphics g, Hand lastHandOnTable, int x, int y, ImageObserver observer) {
		if (lastHandOnTable == null) {
			return;
		}
		
		CardGamePlayer lastHandPlayer = lastHandOnTable.getPlayer();
		g.setColor(Color.BLACK);
		g.drawString("Last Hand On Table", 60, 550);
		g.drawString("Played by " + lastHandPlayer.getName(), 60, 565);
		
		for (int i = 0; i < lastHandOnTable.size(); i++) {
			Card lastHandCard = lastHandOnTable.getCard(i);
			int cardSuit = lastHandCard.getSuit();
			int cardRank = lastHandCard.getRank();
			g.drawImage(cardImages[cardSuit][cardRank], x + 25*i, y, observer);
		}

	}
	
	
	/**
	 * An inner class that extends the JPanel class and implements the
	 * MouseListener interface. Overrides the paintComponent() method 
	 * inherited from the JPanel class to draw the card game table. 
	 * Implements the mouseClicked() method from the MouseListener
	 * interface to handle mouse click events. 
	 * @author michael
	 *
	 */
	public class BigTwoPanel extends JPanel implements MouseListener{
		
		/**
		 * A method of BigTwoPanel class to the paint avatars, player's cards,
		 * and last hand on table.
		 * @param g Graphics to be printed on
		 */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.GREEN);
			g.fillRect(0, 0, 600, 700);
//			drawAvatar(g, 0, 60, 20, this);
//			drawAvatar(g, 1, 60, 150, this);
//			drawAvatar(g, 2, 60, 280, this);
//			drawAvatar(g, 3, 60, 410, this);
//			drawPlayerCards(g, 0, 180, 40, this);
//			drawPlayerCards(g, 1, 180, 170, this);
//			drawPlayerCards(g, 2, 180, 300, this);
//			drawPlayerCards(g, 3, 180, 430, this);
//			
			System.out.println("printingigoisd");

			if (game.getPlayerList().get(0).getName() != null) {
				System.out.println(game.getPlayerList().get(0).getName());
			}
			//			for (int i = 0; i < 4; i++) {
//				if (game.getPlayerList().get(i).getName() != ""){
//					System.out.println(game.getPlayerList().get(i).getName());
//					drawAvatar(g, i, 60, 20+130*i, this);
//					drawPlayerCards(g, i, 180, 40+130*i, this);
//				}
//			}
//			
//			
//			Hand lastHandOnTable = (game.getHandsOnTable().isEmpty()) ? null : game.getHandsOnTable()
//					.get(game.getHandsOnTable().size() - 1);
//			drawLastHand(g, lastHandOnTable, 180, 520, this);			

		}
		
		/**
		 * A method of BigTwoPanel class to determine the card from the mouse click's
		 * x-coordinate and y-coordinate and repaint the panel to indicate
		 * if the card is selected or not. 
		 * @param x x-coordinate of the mouse click
		 * @param y y-coordinate of the mouse click
		 * @param currentPlayer current player that is playing
		 * @param currentPlayerNumOfCards number of cards of the current player that is playing
		 */
		public void checkCardSelected(int x, int y, CardGamePlayer currentPlayer, int currentPlayerNumOfCards) {
			x -= 190;
			y = y - 75 - 20 - (activePlayer*130);
			int cardIdx = x/25;
			
			if (cardIdx == currentPlayerNumOfCards) {
				cardIdx -= 1;										// the horizontal hitbox of the last card is twice the hitbox of other cards
			}
			
			if (cardIdx == currentPlayerNumOfCards-1) {
				// Handle last card
				if (y <= 20) {
					// The card is selected already
					if (selected[cardIdx] == true) {
						selected[cardIdx] = false;
					}
				}
				else {
					selected[cardIdx] = !selected[cardIdx];
				}
				repaint();
				return;
			}
			
			if (y <= 20) {
				// The card is selected already
				if (selected[cardIdx] == true) {
					selected[cardIdx] = false;
				}
				else if (selected[cardIdx] == false) {
					if (cardIdx != 0 && selected[cardIdx-1] == true) {
						// De-select the card immediately to the left
						selected[cardIdx - 1] = false;
					}
				}
			}
			else {
				selected[cardIdx] = !selected[cardIdx];
			}
			repaint();
		}
		
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			int x = e.getX();
			int y = e.getY();

			CardGamePlayer currentPlayer = game.getPlayerList().get(activePlayer);
			int currentPlayerNumOfCards = currentPlayer.getNumOfCards();

			if (y >= 75+(activePlayer*130) && y <= 165+(activePlayer*130)) {
				if (x >= 190 && x <= 190+(25*(currentPlayerNumOfCards+1))) {
					checkCardSelected(x, y, currentPlayer, currentPlayerNumOfCards);
				}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * An inner class that implements the ActionListener interface. 
	 * Implements the actionPerformed() method from the ActionListener 
	 * interface to handle button-click events for the “Play” button. 
	 * When the “Play” button is clicked, call the makeMove() 
	 * method of CardGame object to make a move.
	 * @author michael
	 *
	 */
	class PlayButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if (getSelected().length != 0) {
				game.makeMove(activePlayer, getSelected());
			}
		}
	}
	
	/**
	 * An inner class that implements the ActionListener interface. 
	 * Implements the actionPerformed() method from the ActionListener 
	 * interface to handle button-click events for the “Pass” button.
	 * When the “Pass” button is clicked, call the makeMove() 
	 * method of your CardGame object to make a move.
	 * @author michael
	 *
	 */
	class PassButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			resetSelected();
			game.makeMove(activePlayer, getSelected());
		}
	}
	
	/**
	 * An inner class that implements the ActionListener interface. 
	 * Implements the actionPerformed() method from the ActionListener 
	 * interface to handle menu-item-click events for the “Connect” menu item. 
	 * When the “Connect” menu item is selected, should (i) create a 
	 * new BigTwoDeck object and call its shuffle() method; and (ii) call the 
	 * start() method of CardGame object with the BigTwoDeck object as an argument.
	 * @author michael
	 *
	 */
	class ConnectMenuItemListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			game.makeConnection();
		}
	}
	
	/**
	 * An inner class that implements the ActionListener interface. 
	 * Implements the actionPerformed() method from the ActionListener interface 
	 * to handle menu-item-click events for the “Quit” menu item. When the “Quit”
	 * menu item is selected, you should terminate your application. 
	 * @author michael
	 *
	 */
	class QuitMenuItemListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	class ChatMsgFieldListener implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e) {
			CardGameMessage newChatMsg = new CardGameMessage(CardGameMessage.MSG, -1, chatMsgField.getText());
			game.sendMessage(newChatMsg);
			chatMsgField.setText("");
		}
	}
	
}


