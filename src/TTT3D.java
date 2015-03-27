import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Random;

/*
 * This application is an implementation of the game Tic-Tac-Toe however modified to be played in 3 dimensions. It uses a minimax
 * method with alpha-beta pruning for its move decision tree. Both scores for the computer and for the human are displayed and
 * updated when either has won. A tie counter does not need to be implemented as, with the unique nature of  3D Tic-Tac-Toe, a
 * tie is impossible to achieve. This game implements the ability for the user to choose who goes first (Human by default), the if they would like to
 * be 'X' or 'O' (X by default), and what difficulty they would like to select (Medium by default). The difficulty setting directly sets how many looks ahead the computer
 * will perform, up to 6. Any more and the computer wins too often (every time). It is integrated with a full GUI that is very user
 * friendly.
 */

public class TTT3D extends JFrame implements ActionListener{

	//Global variables that will be accessed or changed by the game
	private JButton newGameBtn;
	private JPanel boardPanel, textPanel, buttonPanel;
	private JLabel status, score;
	private JRadioButton oRadButton, xRadButton, cpuFirstButton, humanFirstButton, easyButton, mediumButton, hardButton;

	private boolean humanFirst = true;

	private int difficulty = 2;				//Variable that changes the amount of looks ahead and the intelligence used if computer goes first
	private int totalLooksAhead = 2;		//Variable that contains the amount of looks ahead the minimax algorithm will do
	private int lookAheadCounter = 0;		//Variable that keeps track of the looks ahead that have been done through recursion

	private int humanScore = 0;				//Total score for human
	private int computerScore = 0;			//Total score for CPU
	int[] finalWin = new int[3];			//Final winning combination
	TicTacToeButton[] finalWinButton = new TicTacToeButton[3];	//Final winning combination

	public boolean win = false;				//Variable to tell if a winning move has been achieved

	char humanPiece = 'X';
	char computerPiece = 'O';

	private char config[][][];				//Configuration of the board that is manipulated in the minimax algorithm
	private TicTacToeButton[][][] boardConfig;	//Button array allows direct access to all buttons on the GUI itself

	public static void main(String a[]){
	       new TTT3D();
	}

	/*
	 * TicTacToeButton is a private inner class that extends JButton and adds information vital to determine the location
	 * to send back to the main array
	 */
	private class TicTacToeButton extends JButton
    {
    	public int boxRow;
    	public int boxColumn;
    	public int boxBoard;
    }

	/*
	 * OneMove is a class that holds information for one potential move. This is used to check if a certain
	 * move is a win
	 */
	public class OneMove
    {
		int board;
    	int row;
    	int column;
    }


	/*
	 * constructor
	 */
    public TTT3D()
    {
    	super("3D Tic-Tac-Toe!");
    	setSize(380,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupBoard();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
	}

    /*
     * BoardPanel extends JPanel and allows the game board to be drawn behind the TicTacToeButtons without
     * interference.
     */
    public class BoardPanel extends JPanel
    {
    	protected void paintComponent(Graphics g) {
    	      super.paintComponent(g);

    	      Graphics2D g2 = (Graphics2D) g;
    	      g2.setStroke(new BasicStroke(2));

    	      //Board 0
    	      g2.drawLine(50, 87, 200, 87);
    	      g2.drawLine(25, 129, 175, 129);
    	      g2.drawLine(120, 60, 45, 160);
    	      g2.drawLine(170, 60, 95, 160);

    	      //Board 1
    	      g2.drawLine(50, 212, 200, 212);
    	      g2.drawLine(25, 254, 175, 254);
    	      g2.drawLine(120, 185, 45, 285);
    	      g2.drawLine(170, 185, 95, 285);

    	      //Board 2
    	      g2.drawLine(50, 337, 200, 337);
    	      g2.drawLine(25, 379, 175, 379);
    	      g2.drawLine(120, 310, 45, 410);
    	      g2.drawLine(170, 310, 95, 410);

    	      //Draws red line through the first and last winning position, always going through the second, indicating the location
    	      //of the win
    	      if(win)
    	      {
    	    	  g2.setColor(Color.RED);
        	      g2.drawLine(finalWinButton[0].getBounds().x + 27, finalWinButton[0].getBounds().y + 20,
        	    		  finalWinButton[2].getBounds().x + 27, finalWinButton[2].getBounds().y + 20);
    	      }


    	}
    }

    /*
     * setupBoard is the methd that builds the GUI
     */
    public void setupBoard()
    {
    	//Creating the 2 arrays to represent the game
    	config = new char[3][3][3];
    	boardConfig = new TicTacToeButton[3][3][3];

    	boardPanel = new BoardPanel();
    	buttonPanel = new JPanel();
    	textPanel = new JPanel();

    	//New Game Button
    	newGameBtn = new JButton("New Game");
    	newGameBtn.setBounds(230, 370, 120, 30);
        newGameBtn.addActionListener(new NewButtonListener());
        newGameBtn.setName("newGameBtn");

        //X/O Radio Button
    	xRadButton = new JRadioButton("X", true);
    	oRadButton = new JRadioButton("O");
    	xRadButton.setBounds(250, 320, 40, 40);
    	oRadButton.setBounds(300, 320, 40, 40);

    	ButtonGroup xoSelect = new ButtonGroup();
    	xoSelect.add(xRadButton);
    	xoSelect.add(oRadButton);

    	PieceListener xoListener = new PieceListener();
    	xRadButton.addActionListener(xoListener);
    	oRadButton.addActionListener(xoListener);

    	//First move radio buttons
    	humanFirstButton = new JRadioButton("Human First", true);
    	cpuFirstButton = new JRadioButton("CPU First");
    	humanFirstButton.setBounds(250, 110, 150, 40);
    	cpuFirstButton.setBounds(250, 80, 150, 40);

    	ButtonGroup firstSelect = new ButtonGroup();
    	firstSelect.add(cpuFirstButton);
    	firstSelect.add(humanFirstButton);

    	FirstListener firstListener = new FirstListener();
    	cpuFirstButton.addActionListener(firstListener);
    	humanFirstButton.addActionListener(firstListener);

    	//Difficulty radio buttons
    	easyButton = new JRadioButton("Easy");
    	mediumButton = new JRadioButton("Medium", true);
    	hardButton = new JRadioButton("Hard");
    	easyButton.setBounds(250, 190, 150, 40);
    	mediumButton.setBounds(250, 220, 150, 40);
    	hardButton.setBounds(250, 250, 150, 40);

    	ButtonGroup difficultyGroup = new ButtonGroup();
    	difficultyGroup.add(easyButton);
    	difficultyGroup.add(mediumButton);
    	difficultyGroup.add(hardButton);

    	DifficultyListener difficultyListener = new DifficultyListener();
    	easyButton.addActionListener(difficultyListener);
    	mediumButton.addActionListener(difficultyListener);
    	hardButton.addActionListener(difficultyListener);


    	//Welcome title
    	status = new JLabel("       Welcome to 3D Tic-Tac-Toe!");
    	status.setFont(new Font("Tahoma", Font.PLAIN, 12));

    	//Current score panel
    	score = new JLabel("               You: " + humanScore + "   Me: " + computerScore);
    	score.setFont(new Font("Tahoma", Font.BOLD, 15));

    	//Variables that determine the locations of the TicTacToeButtons as they are placed within loops
    	int rowShift = 28;
    	int rowStart = 58;

    	int xPos = 58;
    	int yPos = 47;
    	int width = 50;
    	int height = 40;

    	//Variables to keep track of the current button being placed
    	int boardNum = 0;
    	int rowNum = 0;
    	int columnNum = 0;

    	int boxCounter = 0;

    	//Board loop
    	for (int i = 0; i <= 2; i++)
		{
    		//Row loop
			for (int j = 0; j <= 2; j++)
			{
				//Column loop
				for(int k = 0; k <= 2; k++)
				{
					//Creating the new button, setting it to be empty in both arrays
					config[i][j][k] = '-';
					boardConfig[i][j][k] = new TicTacToeButton();
					boardConfig[i][j][k].setFont(new Font("Arial Rounded MT Bold", Font.ITALIC, 20));
	                boardConfig[i][j][k].setText("");
	                //Making it transparent and add
	                boardConfig[i][j][k].setContentAreaFilled(false);
	                boardConfig[i][j][k].setBorderPainted(false);
	                boardConfig[i][j][k].setFocusPainted(false);
	                //Placing the button
	                boardConfig[i][j][k].setBounds(xPos, yPos, width, height);
	                //Setting information variables
	                boardConfig[i][j][k].setName(Integer.toString(boxCounter));
	                boardConfig[i][j][k].boxBoard = boardNum;
	                boardConfig[i][j][k].boxRow = rowNum;
	                boardConfig[i][j][k].boxColumn = columnNum;
	                //Adding action listener
	                boardConfig[i][j][k].addActionListener(this);

	                //Bump the column number 1, move the position that the next button will be placed to the right, and add the current button to the panel
	                columnNum++;
	                boxCounter++;
	                xPos += 52;
	                getContentPane().add(boardConfig[i][j][k]);
				}
				//Reset the column number, bump the row number one, move the position that the next button will be placed down and skew it so it matches with the game board
				columnNum = 0;
            	rowNum++;
            	xPos = rowStart -= rowShift;
                yPos += 41;

			}

			//Reset row numbers and row shifts
			rowNum = 0;
			rowShift = 28;
			rowStart = 58;
			boardNum++;
			xPos = rowStart;
			yPos += 2;
		}


    	//Panel setup
    	boardPanel.setVisible(true);
    	textPanel.setVisible(true);
    	buttonPanel.setVisible(true);
    	status.setVisible(true);

    	textPanel.setLayout(new GridLayout(2,1));
        textPanel.add(status);
        textPanel.add(score);
        textPanel.setBounds(80, 0, 380, 30);


        add(xRadButton);
        add(oRadButton);
        add(humanFirstButton);
        add(cpuFirstButton);
        add(easyButton);
        add(mediumButton);
        add(hardButton);
        add(newGameBtn);
        add(textPanel);
        add(boardPanel);


        setVisible(true);

    }

    /*
     * FirstListener is an ActionListener that sets the starting player based on the players input. If clicked it simply clears the board, sets the
     * starting player, sets the title text, and checks the current difficulty. If the computer is selected to go first and the difficulty is not hard, the computer will
     * play in a random spot to allow for a more competitive game
     */
    class FirstListener implements ActionListener
    {

		public void actionPerformed(ActionEvent arg0) {
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");

			if(cpuFirstButton.isSelected())
			{
				humanFirst = false;
				if(!hardButton.isSelected())
				{
					computerPlayRandom();
				}
				else
				{
					computerPlays();
				}
			}
			else
			{
				humanFirst = true;
			}
		}

    }

    /*
     * NewButtonListener is an ActionListener that clears the board, sets the text, and starts a new game, and will go first if the radio button for the CPU
     * to go first is selected
     */
    class NewButtonListener implements ActionListener
    {

		public void actionPerformed(ActionEvent arg0) {
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");
			if(!humanFirst)
			{
				if(difficulty == 3)
				{
					computerPlays();
				}
				else
				{
					computerPlayRandom();
				}

			}

		}

    }
    /*
     * PieceListener is an ActionListener that changes the human and computer piece variables based on input from the user. It then clears the board
     * and starts a new game
     */
    class PieceListener implements ActionListener
    {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");

			if(xRadButton.isSelected())
			{
				humanPiece = 'X';
				computerPiece = 'O';
			}
			else
			{
				humanPiece = 'O';
				computerPiece = 'X';
			}

			if(!humanFirst)
			{
				if(difficulty == 3)
				{
					computerPlays();
				}
				else
				{
					computerPlayRandom();
				}
			}
		}


	}

    /*
     * DifficultyListener is an ActionListener that manipulates the difficulty variable and allows the user to change how aggresive or smart the
     * computer will play. The class itself just changes the global difficulty variable, and then starts a new game
     */
    class DifficultyListener implements ActionListener
    {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			clearBoard();
			status.setForeground(Color.BLACK);
			status.setText("                      Good luck!");

			if(easyButton.isSelected())
			{
				difficulty = 1;
				totalLooksAhead = 1;
			}
			else if(mediumButton.isSelected())
			{
				difficulty = 2;
				totalLooksAhead = 2;
			}
			else
			{
				difficulty = 3;
				totalLooksAhead = 6;
			}

			if(!humanFirst)
			{
				if(difficulty == 3)
				{
					computerPlays();
				}
				else
				{
					computerPlayRandom();
				}
			}
		}

    }

    /*
     * actionPerformed is the listener for all the buttons within the GUI. It takes in the input from the user if he/she clicks on a space, writes that information
     * to both the internal board and the GUI board, and simply checks if that move was a win. If it was, end the game and display the winning move/winning message.
     * If it wasn't, call computerPlays()
     */
	public void actionPerformed(ActionEvent e) {

		//Getting the button clicked's information and setting the arrays accordingly
		TicTacToeButton button = (TicTacToeButton)e.getSource();
		config[button.boxBoard][button.boxRow][button.boxColumn] = humanPiece;
        boardConfig[button.boxBoard][button.boxRow][button.boxColumn].setText(Character.toString(humanPiece));
        boardConfig[button.boxBoard][button.boxRow][button.boxColumn].setEnabled(false);

        OneMove newMove = new OneMove();
        newMove.board = button.boxBoard;
        newMove.row = button.boxRow;
        newMove.column = button.boxColumn;

        if(checkWin(humanPiece, newMove))
        {
        	status.setText("You beat me! Press New Game to play again.");
        	status.setForeground(Color.RED);
        	humanScore++;
        	win = true;
        	disableBoard();
        	updateScore();
        }
        else
        {
        	computerPlays();
        }
	}

	/*
	 * updateScore() is used to update the score panel with the correct score when a win has occurred
	 */
	public void updateScore()
	{
		score.setText("               You: " + humanScore + "   Me: " + computerScore);
	}

	/*
	 * clearBoard() is used to reset the board when the new game button has been pressed. It also
	 * repaints the board, clearing the winning move's line
	 */
	public void clearBoard()
	{
		repaint();
		win = false;
		lookAheadCounter = 0;
		for (int i = 0; i <= 2; i++)
		{
			for (int j = 0; j <= 2; j++)
			{
				for(int k = 0; k <= 2; k++)
				{
	        		config[i][j][k] = '-';
	        		boardConfig[i][j][k].setText("");
	        		boardConfig[i][j][k].setEnabled(true);
				}

			}
		}

		finalWin = new int[3];
	}

	/*
	 * disableBoard() is used to disable the board when a win has occurred, not allowing for any unintended
	 * clicks in the game space
	 */

	public void disableBoard()
	{
		int index = 0;
		for (int i = 0; i <= 2; i++)
		{
			for (int j = 0; j <= 2; j++)
			{
				for(int k = 0; k <= 2; k++)
				{

					if(contains(finalWin, Integer.parseInt(boardConfig[i][j][k].getName())))
					{

						boardConfig[i][j][k].setEnabled(true);
						boardConfig[i][j][k].setForeground(Color.RED);
						finalWinButton[index] = boardConfig[i][j][k];
						index++;

					}
					else
					{
						boardConfig[i][j][k].setEnabled(false);
					}

				}

			}
		}

		repaint();
	}

	/*
	 * Private method contains() is used in the process of checking the contents of the finalWin int array and
	 * changing the appropriate boxes to show the winning combination
	 */
	private boolean contains(int[] a, int k)
	{
		//Step through array
		for(int i : a)
		{	//Compare elements
			if(k == i)
				return true;
		}
		return false;
	}


	/*
	 * The method computerPlayRandom() is used when the difficulty setting is easy or medium and the computer is selected to go first.
	 * This is implemented because if the computer is allowed to move first using the minimax method, it is almost impossible for a
	 * human to win. Since the setting is on easy or medium, showing that the player might actually want to win, putting the first
	 * move in a random spot allows the game to be more competitive and fun. This method is not called when the setting is hard
	 * allowing for very aggressive play as the difficulty setting would suggest.
	 */
	private void computerPlayRandom()
	{
		Random random = new Random();
		int row = random.nextInt(3);
		int column = random.nextInt(3);
		int board = random.nextInt(3);
		config[board][row][column] = computerPiece;
		boardConfig[board][row][column].setText(Character.toString(computerPiece));

		boardConfig[board][row][column].setEnabled(false);
	}

	/*
	 * computerPlays() is the main method used in the A.I. implementation of this game. It walks through each available move in the
	 * current game board, then creates branches off of those moves using lookAhead(), judging what the player will do in response to
	 * that potential move, and makes a move that is most promising in response to the possible humans most promising move.
	 */
	private void computerPlays()
	{
		int bestScore;
		int hValue;
		OneMove nextMove;
		int bestScoreBoard = -1;
		int bestScoreRow = -1;
		int bestScoreColumn = -1;

		//Low number so the first bestScore will be the starting bestScore
		bestScore = -1000;
		//Walk through the entire game board
		check:
		for (int i = 0; i <= 2; i++)
		{
			for (int j = 0; j <= 2; j++)
			{
				for(int k = 0; k <= 2; k++)
				{
					if(config[i][j][k] == '-')
					{
						//Creating a new move on every empty position
						nextMove = new OneMove();
						nextMove.board = i;
						nextMove.row = j;
						nextMove.column = k;

						if(checkWin(computerPiece, nextMove))
						{
							//Leave the piece there if it is a win and end the game
							config[i][j][k] = computerPiece;
							boardConfig[i][j][k].setText(Character.toString(computerPiece));
							status.setText("   I win! Press New Game to play again.");
							status.setForeground(Color.RED);
							win = true;
							computerScore++;

							disableBoard();
							updateScore();
							break check;
						}
						else
						{
							//This is where the method generates all the possible counter moves potentially made
							//by the human player
							if(difficulty != 1)
							{
								hValue = lookAhead(humanPiece, -1000, 1000);
							}
							else
							{
								//If the player is on easy, just calculate the heuristic value for every current possible move, no looking ahead
								hValue = heuristic();
							}
							lookAheadCounter = 0;

							//CPU chooses the best hValue out of every move
							if(hValue >= bestScore)
							{
								bestScore = hValue;
								bestScoreBoard = i;
								bestScoreRow = j;
								bestScoreColumn = k;
								config[i][j][k] = '-';
							}
							else
							{
								config[i][j][k] = '-';
							}
						}
					}
				}
			}
		}

		//If there is no possible winning move, make the move in the calculated best position.
		if(!win)
		{
			config[bestScoreBoard][bestScoreRow][bestScoreColumn] = computerPiece;
			boardConfig[bestScoreBoard][bestScoreRow][bestScoreColumn].setText(Character.toString(computerPiece));

			boardConfig[bestScoreBoard][bestScoreRow][bestScoreColumn].setEnabled(false);
		}
	}

	/*
	 * lookAhead() generates all the possible moves in the available spaces based on the current board in response
	 * to the possible move made by the computer in computerPlays(). This method returns a heuristic value that is calculated
	 * using the heuristic() function. This function also implements the alpha beta pruning technique since the search
	 * tree can become quite large when playing on hard difficulty
	 */
	private int lookAhead(char c, int a, int b)
	{
		//Alpha and beta values that get passed in
		int alpha = a;
		int beta = b;

		//If you still want to look ahead
		if(lookAheadCounter <= totalLooksAhead)
		{

			lookAheadCounter++;
			//If you are going to be placing the computer's piece this time
			if(c == computerPiece)
			{
				int hValue;
				OneMove nextMove;

				for (int i = 0; i <= 2; i++)
				{
					for (int j = 0; j <= 2; j++)
					{
						for(int k = 0; k <= 2; k++)
						{

							if(config[i][j][k] == '-')
							{

								nextMove = new OneMove();
								nextMove.board = i;
								nextMove.row = j;
								nextMove.column = k;

								if(checkWin(computerPiece, nextMove))
								{
									config[i][j][k] = '-';
									return 1000;
								}
								else
								{
									//Recursive look ahead, placing human pieces next
									hValue = lookAhead(humanPiece, alpha, beta);
									if(hValue > alpha)
									{
										alpha = hValue;
										config[i][j][k] = '-';
									}
									else
									{
										config[i][j][k] = '-';
									}
								}

								//Break out of the look if the alpha value is larger than the beta value, going down no further
								if (alpha >= beta)
									break;
							}
						}
					}
				}

				return alpha;
			}

			//If you are going to be placing the human's piece this time
			else
			{
				int hValue;
				OneMove nextMove;

				for (int i = 0; i <= 2; i++)
				{
					for (int j = 0; j <= 2; j++)
					{
						for(int k = 0; k <= 2; k++)
						{

							if(config[i][j][k] == '-')
							{

								nextMove = new OneMove();
								nextMove.board = i;
								nextMove.row = j;
								nextMove.column = k;

								if(checkWin(humanPiece, nextMove))
								{
									config[i][j][k] = '-';
									return -1000;
								}
								else
								{
									//Recursive look ahead, placing computer pieces next
									hValue = lookAhead(computerPiece, alpha, beta);
									if(hValue < beta)
									{
										beta = hValue;
										config[i][j][k] = '-';
									}
									else
									{
										config[i][j][k] = '-';
									}
								}

								//Break out of the look if the alpha value is larger than the beta value, going down no further
								if (alpha >= beta)
									break;
							}
						}
					}
				}
				return beta;
			}
		}
		//If you are at the last level of nodes you want to check
		else
		{
			return heuristic();
		}

	}

	/*
	 * heuristic() simply uses the checkAvailable method for both the computer and human on the current board, and subtracts them
	 * making a higher value more promising for the computer.
	 */
	private int heuristic()
	{
		return (checkAvailable(computerPiece) - checkAvailable(humanPiece));
	}

	/*
	 * checkWin() takes in a character that will be checked for a win and a move that checks if that creates a win. It uses
	 * a 2-dimensional array that holds every possible winning combination and a 1-dimensional array that represents all the
	 * spaces on the game board.
	 */
	private boolean checkWin(char c, OneMove pos)
	{
		config[pos.board][pos.row][pos.column] = c;

		//Win table
		int[][] wins = {
				//Rows on single board
				{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, {18, 19, 20},
				{21, 22, 23}, {24, 25, 26},

				//Columns on single board
				{0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {9, 12, 15}, {10, 13, 16}, {11, 14, 17}, {18, 21, 24},
				{19, 22, 25}, {20, 23, 26},

				//Diagonals on single board
				{0, 4, 8}, {2, 4, 6}, {9, 13, 17}, {11, 13, 15},
				{18, 22, 26}, {20, 22, 24},

				//Straight down through boards
				{0, 9, 18}, {1, 10, 19}, {2, 11, 20}, {3, 12, 21}, {4, 13, 22}, {5, 14, 23}, {6, 15, 24},
				{7, 16, 25}, {8, 17, 26},

				//Diagonals through boards
				{0, 12, 24}, {1, 13, 25}, {2, 14, 26}, {6, 12, 18}, {7, 13, 19}, {8, 14, 20}, {0, 10, 20},
				{3, 13, 23}, {6, 16, 26},{2, 10, 18}, {5, 13, 21}, {8, 16, 24}, {0, 13, 26}, {2, 13, 24},
				{6, 13, 20}, {8, 13, 18},
		};

		//Array that indicates all the spaces on the game board
		int[] gameBoard = new int[27];

		//Counter from 0 to 49, one for each win combo
		int counter = 0;

		//If the space on the board is the same as the input char, set the corresponding location
		//in gameBoard to 1.
		for (int i = 0; i <= 2; i++)
		{
			for (int j = 0; j <= 2; j++)
			{
				for(int k = 0; k <= 2; k++)
				{
					if(config[i][j][k] == c)
					{
						gameBoard[counter] = 1;
					}
					else
					{
						gameBoard[counter] = 0;
					}
					counter++;
				}
			}
		}

		//For each possible win combination
		for (int i = 0; i <= 48; i++)
		{
			//Resetting counter to see if all 3 locations have been used
			counter = 0;
			for (int j = 0; j <= 2; j++)
			{
				//For each individual winning space in the current combination
				if(gameBoard[wins[i][j]] == 1)
				{
					counter++;

					finalWin[j] = wins[i][j];
					//If all 3 moves of the current winning combination are occupied by char c
					if(counter == 3)
					{
						return true;
					}
				}
			}
		}

		return false;
	}


	/*
	 * checkAvailable is very similar to checkWin(), however instead of returning a boolean if the input
	 * move is a win or not, this method returns an int corresponding to the amount of possible wins available
	 * on the current board for input char c ('X' or 'O')
	 */
	private int checkAvailable(char c)
	{
		int winCounter = 0;

		//Win table
		int[][] wins = {
				//Rows on single board
				{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, {18, 19, 20},
				{21, 22, 23}, {24, 25, 26},

				//Columns on single board
				{0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {9, 12, 15}, {10, 13, 16}, {11, 14, 17}, {18, 21, 24},
				{19, 22, 25}, {20, 23, 26},

				//Diagonals on single board
				{0, 4, 8}, {2, 4, 6}, {9, 13, 17}, {11, 13, 15},
				{18, 22, 26}, {20, 22, 24},

				//Straight down through boards
				{0, 9, 18}, {1, 10, 19}, {2, 11, 20}, {3, 12, 21}, {4, 13, 22}, {5, 14, 23}, {6, 15, 24},
				{7, 16, 25}, {8, 17, 26},

				//Diagonals through boards
				{0, 12, 24}, {1, 13, 25}, {2, 14, 26}, {6, 12, 18}, {7, 13, 19}, {8, 14, 20}, {0, 10, 20},
				{3, 13, 23}, {6, 16, 26},{2, 10, 18}, {5, 13, 21}, {8, 16, 24}, {0, 13, 26}, {2, 13, 24},
				{6, 13, 20}, {8, 13, 18},
		};

		//Array that indicates all the spaces on the game board
		int[] gameBoard = new int[27];

		//Counter from 0 to 49, one for each win combo
		int counter = 0;

		//If the space on the board is the same as the input char, set the corresponding location
		//in gameBoard to 1.
		for (int i = 0; i <= 2; i++)
		{
			for (int j = 0; j <= 2; j++)
			{
				for(int k = 0; k <= 2; k++)
				{
					if(config[i][j][k] == c || config[i][j][k] == '-')
					{
						gameBoard[counter] = 1;
					}
					else
					{
						gameBoard[counter] = 0;
					}
					counter++;
				}
			}
		}

		//For each possible win combination
		for (int i = 0; i <= 48; i++)
		{
			//Resetting counter to see if all 3 locations have been used
			counter = 0;
			for (int j = 0; j <= 2; j++)
			{
				//For each individual winning space in the current combination
				if(gameBoard[wins[i][j]] == 1)
				{
					counter++;

					finalWin[j] = wins[i][j];
					//If all 3 moves of the current winning combination are occupied by char c
					if(counter == 3)
					{
						winCounter++;
					}
				}
			}
		}

		return winCounter;
	}



}
