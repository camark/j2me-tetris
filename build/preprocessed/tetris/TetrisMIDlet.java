

package tetris;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Random;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import tetris.model.TetrisBoard;
import tetris.model.TetrisPiece;
import tetris.ui.TetrisCanvas;

/**
 * Maintains game state.  Handles input.
 */
public class TetrisMIDlet extends MIDlet implements CommandListener {
	
	private TetrisCanvas gameCanvas;		// canvas on which the game is painted	
	
	private TetrisBoard board;				// holds the game state
	private TetrisPiece activePiece;		// holds the state of the active piece
	
	private int score;						// the current game score
	private int level;						// the current level
	private int lineCount;					// the current number of lines cleared
	private int nextPieceType;				// the next piece
	private int tickSpeed;					// the speed in milliseconds between drops

	private int hiScore;					// the current hi score
	private RecordStore tetrisStore;		// the RecordStore holding the hi score
	private int hiScoreRecordId;			// the record id in the RecordStore of the hi score
	
	private boolean[] completedRows;		// array of booleans indicating the rows that have been cleared
											// store as an instance variable so we reuse without reallocating
	
	private Random rand;					// generates pseudo random numbers to choose the next piece
	
	private Command exitCommand;			// Command to exit the app
	private Command pauseCommand;			// Command to pause the app
	private Command resumeCommand;			// Command to resume the app after a pause
	
	private int gameState = TetrisConstants.UNINITIALIZED;	// mark as unitialized at first, can check in startApp to see if init necessary

	private DropThread dropThread;			// the thread that drops the active piece one row per tick
	
	/**
	 * Start the app.
	 * @see MIDlet#startApp()
	 */
	protected void startApp() throws MIDletStateChangeException {
		if(TetrisConstants.UNINITIALIZED == this.gameState) {
			// game is just starting up, need to initialize
			this.init();
		} else if(TetrisConstants.RUNNING_STATE == this.gameState) {
			// game is resuming from an external suspend
			// this puts it in the internal pause state
			this.resumeGame();
		} else {
			// resumed from the title screen, just repaint
			this.gameCanvas.reset();
		}
		
		Display.getDisplay(this).setCurrent(this.gameCanvas);
	}
	
	/**
	 * Suspend the app.
	 * @see MIDlet#pauseApp()
	 */
	protected void pauseApp() {
		// put the game in its internal paused state before suspending the app
		if(TetrisConstants.RUNNING_STATE == this.gameState) {
			this.pauseGame();
		}
	}
	
	/**
	 * Exit.
	 * @see MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		if(null != this.tetrisStore) {
			// write out the hi score before exitting
			this.writeAndCloseHiScore(this.hiScore);
		}
	}
	
	/**
	 * Initalization on app startup.
	 */
	private void init() {
		this.board = new TetrisBoard();
		this.gameCanvas = new TetrisCanvas(this);
		
		this.activePiece = new TetrisPiece();		
		this.completedRows = new boolean[TetrisConstants.HEIGHT];
		this.hiScore = this.openAndReadHiScore();	// get currently saved hi score from rms
		this.nextPieceType = TetrisConstants.UNINITIALIZED;
		
		this.rand = new Random();
		
		// setup exit/pause/resume commands
		this.setupCommands();
		this.gameCanvas.addCommand(this.exitCommand);
		
		// put the app in a state to show the title screen
		this.setGameState(TetrisConstants.TITLE_STATE);
	}
	
	/**
	 * Set up the game state so that a new game is started.
	 * @param level the initial level at which to start the game
	 */
	private void startNewGame(int level) {
		this.gameCanvas.addCommand(this.pauseCommand);	// during running, pause command should be available
		
		this.score = 0;
		this.lineCount = 0;
		this.level = level;
		this.nextPieceType = this.getRandomPieceType();
		this.tickSpeed = this.getInitialTickSpeed(this.level);
		
		this.board.clearBoard();
		this.tryAddNewPiece();
		
		this.setGameState(TetrisConstants.RUNNING_STATE);
		
		this.runDropThread();
	}
	
	/**
	 * Put the app in an end game state.  Should redisplay the title screen.
	 */
	private void endGame() {
		// no commands needed, don't need to pause when not playing
		this.gameCanvas.removeCommand(this.pauseCommand);
		
		this.hiScore = Math.max(this.hiScore, this.score);		// set hi score if current score is higher
		this.nextPieceType = TetrisConstants.UNINITIALIZED;
		this.setGameState(TetrisConstants.TITLE_STATE);							// show the title screen
		
		this.dropThread.stopThread();
		this.dropThread = null;		// will have to replace it anyway, might as well gc as soon as possible
	}
	
	/**
	 * Put the game into a paused state during running.
	 */
	private void pauseGame() {
		// replace the pause command with a resume command
		this.gameCanvas.removeCommand(this.pauseCommand);
		this.gameCanvas.addCommand(this.resumeCommand);
		
		// put in paused state and stop dropping
		this.setGameState(TetrisConstants.PAUSED_STATE);
		this.dropThread.stopThread();
	}
	
	/**
	 * Resume the game from a paused state.
	 */
	private void resumeGame() {
		// replace the resume command with a pause command
		this.gameCanvas.removeCommand(this.resumeCommand);
		this.gameCanvas.addCommand(this.pauseCommand);
	
		// put in running state and resume dropping
		this.setGameState(TetrisConstants.RUNNING_STATE);
		this.runDropThread();
	}
	
	/**
	 * Run the drop thread.  In MIDP 1.0 it doesn't seem possible to restart a thread.
	 * Instead just create a new one and start it.
	 */
	private void runDropThread() {
		this.dropThread = new DropThread(this);
		this.dropThread.start();
	}
	
	/**
	 * Set the initial tick speed according to the given level.
	 * 
	 * @param level the initial level
	 * @return the initial tick speed
	 */
	private int getInitialTickSpeed(int level) {
		int initialTickSpeed = TetrisConstants.BASE_SPEED;
		
		// multiply by fraction for each level
		for(int i = 0; i < level; i++) {
			initialTickSpeed = (initialTickSpeed * TetrisConstants.SPEED_INCREASE_NUMERATOR) / TetrisConstants.SPEED_INCREASE_DENOMINATOR;
		}
		
		return initialTickSpeed;
	}

	/**
	 * The MIDlet handles the key input.
	 * This is called by the Canvas' listening keyPressed.
	 * 
	 * @param keyCode the keyCode from Canvas' keyPressed
	 */
	public void keyPressed(int keyCode) {
		if(TetrisConstants.RUNNING_STATE == this.gameState) {
			// if the app is in a running state, then we want the game actions

			keyCode = this.gameCanvas.getGameAction(keyCode);	
		
			if(Canvas.DOWN == keyCode) {
				this.tryMoveDown();
			} else if(Canvas.UP == keyCode) {
				this.tryRotateLeft();
			} else if(Canvas.LEFT == keyCode) {
				this.tryMoveLeft();
			} else if(Canvas.RIGHT == keyCode) {
				this.tryMoveRight();
			} else if(Canvas.FIRE == keyCode) {
				this.quickDrop();
			}
		} else if(TetrisConstants.TITLE_STATE == this.gameState) {
			// if we're at the title screen, get the level number from input
			
			if(Canvas.KEY_NUM0 <= keyCode && Canvas.KEY_NUM9 >= keyCode) {
				int level = keyCode - Canvas.KEY_NUM0;	
				this.startNewGame(level);
			}
		}	
	}
	
	/**
	 * Quick drop the active piece as far as it will go
	 */
	private synchronized void quickDrop() {
		int dropScore = 0;	// 1 point for each line dropped
		while(this.tryMoveDown()) {
			dropScore++;
		}
		
		this.score += dropScore;
		
		if(null != this.dropThread) {
			// if the piece has been quick dropped, then the piece has been instantly dropped to the bottom.
			// since the new piece is immediately added, it will drop a row at the end of the current tick.
			// we specify to skip the next tick, the player gets the remainder of the current tick, plus
			// the whole next tick before the piece drops a row.
			this.dropThread.skipNextTick();
		}
	}
	
	/**
	 * Set the active piece as a new piece and choose the next piece
	 * 
	 * @return the active piece, updated as a new piece
	 */
	private TetrisPiece newPiece() {
		int pieceType = this.nextPieceType;
		this.nextPieceType = this.getRandomPieceType();
		
		TetrisPiece activePiece = this.getActivePiece();
		activePiece.setAsNewPiece(pieceType, TetrisConstants.START_X, TetrisConstants.START_Y);	
		
		return activePiece;
	}
	
	/**
	 * @return a pseudo random piece type
	 */
	private int getRandomPieceType() {
		// MIDP 1.0 doesn't have Random.nextInt(int n)
		// this is a bad substitute, but good enough for us...
		return Math.abs(rand.nextInt() % TetrisConstants.NUM_PIECE_TYPES) + 1;
	}
	
	/**
	 * Clear the completed rows from the board.
	 * Wipes the filled rows and drops the rows above them.
	 * 
	 * @param piece the piece in its final position
	 * @return number of rows cleared
	 */
	private int clearCompletedRows(TetrisPiece piece) {
		TetrisBoard board = this.getBoard();
		
		// check each row that the piece includes, see if completed
		for(int i = 0; i < TetrisConstants.FOUR_BLOCKS; i++) {
			int rowY = piece.getBlockY(i);
			
			// mark rows completed that are filled
			if(board.checkRowCompleted(rowY)) {
				this.markRowCompleted(rowY, true);
			}
		}
		
		int numClearedRows = 0;
		for(int y = TetrisConstants.HEIGHT - 1; y >= 0; y--) { // iterate from bottom up
			// each row should be dropped the current tally of completed rows
			if(numClearedRows > 0) {
				board.dropRow(y, numClearedRows);
			}
			
			if(this.isRowCompleted(y)) {
				numClearedRows++;
				this.markRowCompleted(y, false);	// reset for next time
			}
		}
		
		// clear the top number of rows completed, these are new empty rows
		for(int i = 0; i < numClearedRows; i++) {
			board.clearRow(i);
		}
		
		return numClearedRows;
	}
	
	/**
	 * Mark a row as completed, a setter accessor to the completedRows.
	 * We use the instance completedRows to reuse the array without reallocating.
	 * 
	 * @param row the index of the row, lower indices at the top of the board
	 * @param isCompleted true if the row is completed false otherwise
	 */
	private void markRowCompleted(int row, boolean isCompleted) {
		this.completedRows[row] = isCompleted;
	}
	
	/**
	 * Check if a row is completed, a getter accessor to the completedRows.
	 * @param row the index of the row, lower indices at the top of the board
	 * @return true if the row is completed false otherwise.
	 */
	private boolean isRowCompleted(int row) {
		return this.completedRows[row];
	}
	
	/**
	 * Update the game state to reflect the completed rows (adjust score, etc).
	 * @param completedRows the number of completed rows
	 */
	private void updateRowState(int completedRows) {
		this.lineCount += completedRows;	// increment the line count
		
		// formula for scores is (ROW_SCORE * level) + ROW_SCORE
		if(1 == completedRows) {
			this.score += (this.level * TetrisConstants.ONE_ROW_SCORE) + TetrisConstants.ONE_ROW_SCORE;
		} else if(2 == completedRows) {
			this.score += (this.level * TetrisConstants.TWO_ROW_SCORE) + TetrisConstants.TWO_ROW_SCORE;
		} else if(3 == completedRows) {
			this.score += (this.level * TetrisConstants.THREE_ROW_SCORE) + TetrisConstants.THREE_ROW_SCORE;
		} else if(4 == completedRows) {
			this.score += (this.level *TetrisConstants. FOUR_ROW_SCORE) + TetrisConstants.FOUR_ROW_SCORE;
		}
		
		// integer division gets the level
		int level = this.lineCount / TetrisConstants.LEVEL_UNIT;
		if(level > this.level) {
			this.level = level;
		
			// level increase, adjust tick speed
			this.tickSpeed = (this.tickSpeed * TetrisConstants.SPEED_INCREASE_NUMERATOR) / TetrisConstants.SPEED_INCREASE_DENOMINATOR;
		}
	}
	
	/**
	 * Try to add a new piece.  Check that there is room on the board.
	 * If we can't add the piece, the game is over.
	 * 
	 * @return true if the piece was added, false if couldn't add and the game is over.
	 */
	private synchronized boolean tryAddNewPiece() {
		TetrisPiece newPiece = this.newPiece();		// reset the active piece
		TetrisBoard board = this.getBoard();
		if(board.canAddNewPiece(newPiece)) {
			board.addNewPiece(newPiece);
		
			// added successfully
			return true;
		}
		
		// no room to add, game over
		this.endGame();
		return false;
	}
	
	/**
	 * Try to move the current piece down one row.
	 * If we can't drop the piece any more, we try to add a new one.
	 * 
	 * @return true if the piece was dropped, false otherwise
	 */
	private synchronized boolean tryMoveDown() {
		TetrisPiece activePiece = this.getActivePiece();
		TetrisBoard board = this.getBoard();
		
		if(board.canMoveDown(activePiece)) {
			board.moveDown(activePiece);
			this.gameCanvas.repaint();
			
			// piece moved down
			return true;
		}
		
		// couldn't move down
		board.lockPiece(activePiece);
		int numClearedRows = this.clearCompletedRows(activePiece);
		this.updateRowState(numClearedRows);
		this.tryAddNewPiece();
		this.gameCanvas.repaint();
		
		return false;
	}
	
	/**
	 * Try to move the current piece left.
	 * 
	 * @return true if we could move the piece left, false if couldn't
	 */
	private synchronized boolean tryMoveLeft() {
		if(this.getBoard().canMoveLeft(this.getActivePiece())) {
			this.board.moveLeft(this.getActivePiece());
			this.gameCanvas.repaint();
			
			// piece moved left
			return true;
		}
		
		// couldn't move left
		return false;
	}
	
	/**
	 * Try to move the current piece right.
	 * 
	 * @return true if we could move the piece right, false if couldn't
	 */
	private synchronized boolean tryMoveRight() {
		if(this.getBoard().canMoveRight(this.getActivePiece())) {
			this.board.moveRight(this.getActivePiece());
			this.gameCanvas.repaint();
			
			// piece moved right
			return true;
		}
		
		// couldn't move right
		return false;
	}
	
	/**
	 * Try to rotate the piece left (counter-clockwise)
	 * 
	 * @return true if we could rotate left, false if couldn't
	 */
	private synchronized boolean tryRotateLeft() {
		if(this.getBoard().canRotateLeft(this.getActivePiece())) {
			this.board.rotateLeft(this.getActivePiece());
			this.gameCanvas.repaint();
			
			// rotated left
			return true;
		}
		
		// couldn't rotate
		return false;
	}
	
	/**
	 * Try to rotate the piece right (clockwise)
	 *
	 * @return true if we could rotate right, false if couldn't
	 */
	private synchronized boolean tryRotateRight() {
		if(this.getBoard().canRotateRight(this.getActivePiece())) {
			this.board.rotateRight(this.getActivePiece());
			this.gameCanvas.repaint();
			
			// rotated right
			return true;
		}
		
		// couldn't rotate
		return false;
	}
	
	/**
	 * Handle commands.  Implementation for CommandListener.
	 * 
	 * @param c the Command input
	 * @param d the originating Displayable, not needed, since we only have the Canvass
	 */
	public void commandAction(Command c, Displayable d) {
		if(c == this.exitCommand) {
			try {
				// on exit just destroy the app
				this.destroyApp(true);
				this.notifyDestroyed();
			} catch(MIDletStateChangeException msce) {
				// unconditional
			}
		} else if(c == this.pauseCommand) {
			this.pauseGame();
		} else if(c == this.resumeCommand) {
			this.resumeGame();
		}
	}
	
	/**
	 * Setup the commands.
	 * Once they're set up, add and remove pause/resume depending on the state.
	 */
	private void setupCommands() {
		this.exitCommand = new Command("exit", Command.EXIT, 1);
		this.pauseCommand = new Command("pause", Command.ITEM, 1);
		this.resumeCommand = new Command("resume", Command.ITEM, 1);
				
		this.gameCanvas.setCommandListener(this);
	}
	
	/**
	 * Drop thread periodically calls this method.
	 * We try to move down the active piece on each tick.
	 */
	public void tick() {
		this.tryMoveDown();
	}

	/**
	 * @return the board state object
	 */
	public TetrisBoard getBoard() {
		return this.board;
	}
	
	/**
	 * @return the active piece state object
	 */
	public TetrisPiece getActivePiece() {
		return this.activePiece;
	}
	
	/**
	 * @return the current game score
	 */
	public int getScore() {
		return this.score;
	}
	
	/**
	 * @return the current hi score
	 */
	public int getHiScore() {
		return this.hiScore;
	}
	
	/**
	 * @return the current line count
	 */
	public int getLineCount() {
		return this.lineCount;
	}
	
	/**
	 * @return the current level
	 */
	public int getLevel() {
		return this.level;
	}
	
	/**
	 * @return the upcoming piece once the current piece is dropped
	 */
	public int getNextPieceType() {
		return this.nextPieceType;
	}
	
	/**
	 * @return the current time between ticks (milliseconds)
	 */
	public int getTickSpeed() {
		return this.tickSpeed;
	}
	
	/**
	 * @return the current game state, should be a constant defined in the constants file
	 */
	public int getGameState() {
		return this.gameState;
	}
	
	/**
	 * Set the game state.
	 * Additionally resets the canvas so it will repaint one-time painting.
	 * 
	 * @param gameState the new game state
	 */
	public void setGameState(int gameState) {
		this.gameState = gameState;
		
		if(null != this.gameCanvas) {
			this.gameCanvas.reset();
			this.gameCanvas.repaint();
		}
	}

	/**
	 * Open the record store and try to read a hi score out of it.
	 * 
	 * @return the hi score persisted in rms
	 */
	private int openAndReadHiScore() {
		int hiScore = 0;
		
		try {
			this.tetrisStore = RecordStore.openRecordStore(TetrisConstants.TETRIS_RECORD_STORE, true);
			if(this.tetrisStore.getNumRecords() > 0) {
				// should be only one record, the hi score
				
				RecordEnumeration recordEnum = this.tetrisStore.enumerateRecords(null, null, false);
				this.hiScoreRecordId = recordEnum.nextRecordId();
				byte[] hiScoreBytes = this.tetrisStore.getRecord(this.hiScoreRecordId);
				
				// wrap the bytes in a DataInputStream so we can readInt
				ByteArrayInputStream byteIn = new ByteArrayInputStream(hiScoreBytes);
				DataInputStream dataIn = new DataInputStream(byteIn);
				
				hiScore = dataIn.readInt();
				
				// don't think this is necessary
				dataIn.close();
				byteIn.close();
			}
		} catch(Exception e) {
			// nothing we can do, hiScore remains 0
		}
			
		return hiScore;
	}
	
	/**
	 * Try to write the current hi score to the record store and close it.
	 * 
	 * @param score the current hi score
	 */
	private void writeAndCloseHiScore(int score) {
		try {
			// use DataOutputStream so we can writeInt
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(4);	// a single int, should be 4 bytes
			DataOutputStream dataOut = new DataOutputStream(byteOut);
			dataOut.writeInt(this.hiScore);
			
			// get the bytes for the int
			byte[] hiScoreBytes = byteOut.toByteArray();
			
			if(this.tetrisStore.getNumRecords() == 0) {
				// no previously stored record, created a new one
				this.tetrisStore.addRecord(hiScoreBytes, 0, hiScoreBytes.length);
			} else {
				// overwrite the existing hi score record
				this.tetrisStore.setRecord(this.hiScoreRecordId, hiScoreBytes, 0, hiScoreBytes.length);
			}
			
			this.tetrisStore.closeRecordStore();
			
			dataOut.close();
			byteOut.close();
			
		} catch(Exception e) {
			// oh well...
		}
	}
}
