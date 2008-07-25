/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tetris.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import tetris.TetrisConstants;
import tetris.TetrisMIDlet;
import tetris.model.TetrisBoard;

/**
 * Handles all of the UI and painting for the app.
 * Delegates input to the Midlet instance itself.
 * Makes an effort to adjust itself to the available screen dimensions.
 * Also makes an effort to repaint only what is necessary.
 * Uses manual double buffering if the Canvas doesn't automatically double buffer.
 */
public class TetrisCanvas extends Canvas {

	private TetrisMIDlet game;			// the midlet

	private int boardX;					// x coordinate of the upper left corner of the playing grid on the canvas
	private int boardY;					// y coordinate of the upper left corner of the playing grid on the canvas
	private int boardWidth;				// width (pixels) of the playing grid on the canvas
	private int boardHeight;			// height (pixels) of the playing grid on the canvas
	
	private int screenWidth;			// total screen width (pixels)
	private int screenHeight;			// total screen height (pixels)
	private int viewableWidth;			// used width once layout calculations are made (pixels)
	private int viewableHeight;			// used height onece layout calculations are made (pixels)
	
	private int blockSize;				// the dimension of a grid block, same for width and height
	
	private int infoPanelX;				// the x coordinate of the left edge of the right info panel (scores, level, etc.)
	private int infoPanelWidth;			// the width (pixels) of the info panel
	
	private InfoBox scoreBox;			// box showing the score
	private InfoBox lineCountBox;		// box showing the number of lines cleared
	private InfoBox levelBox;			// box showing the current level
	private NextPieceBox nextPieceBox;	// box showing the next piece
	private TitleBox titleBox;			// box showing the title screen, displayed when no game running
	
	private Font font;					// font to use throughout display
	
	private Image doubleBuffer;			// if Canvas isn't double buffered, use Image to manually double buffer
	private boolean paintedOnce;		// a flag to indicate whether things that only need to be drawn once have been
	private int[][] lastBoardState;		// the board state last time the screen was painted, used to avoid unecessary painting
	
	/**
	 * Make a new canvas.
	 * 
	 * @param game the Midlet instance the canvas is attached to.
	 */
	public TetrisCanvas(TetrisMIDlet game) {
		this.game = game;
		this.lastBoardState = new int[TetrisConstants.WIDTH][TetrisConstants.VIEWABLE_ROWS];	// only need to save state for visible rows
		
		if(!this.isDoubleBuffered()) {
			// use a double buffer if Canvas doesn't automatically support
			// can use a null check on this.doubleBuffer to see if double buffering
			this.doubleBuffer = Image.createImage(this.getWidth(), this.getHeight());
		}
		
		// initializes instance variables that set the relative layout
		this.setupLayout();
	}

	/**
	 * Passes on single key press to the game instance that handles input.
	 */
	protected void keyPressed(int keyCode) {
		this.game.keyPressed(keyCode);
	}
	
	/**
	 * Paint the canvas.
	 */
	public void paint(Graphics g) {
		if(null == this.doubleBuffer) {
			// direct support for double buffering, so we just paint directly to the canvas
			this.paintScreen(g);
		} else {
			// no automatic double buffering, so we paint on an buffer Image and then draw it
			Graphics bufferG = this.doubleBuffer.getGraphics();
			this.paintScreen(bufferG);
			g.drawImage(this.doubleBuffer, 0 , 0, Graphics.LEFT | Graphics.TOP);
		}
	}

	/**
	 * Helper paint method that paints on the supplied Graphics object.
	 * Exists so that a generic Graphics object can be passed in, whether it is on the canvas or a double buffer.
	 * 
	 * @param g a Graphics object on which to paint
	 */
	private void paintScreen(Graphics g) {
		TetrisBoard board = this.game.getBoard();

		if(!this.paintedOnce) {
			// need to paint one time things that will not be updated during the normal course of the game
			this.paintOnce(g);
			this.paintedOnce = true;
			
		} else if(TetrisConstants.RUNNING_STATE == this.game.getGameState()) {
			// otherwise if the game is running we need to update the board/info
			
			this.paintInfoBoxes(g);
			this.paintBoard(g);
		}
	}
	
	/**
	 * Paint once.  Paints one time things that will not be overwritten during the normal course of the game.
	 * Will usually need to call this method once per state change of the game.
	 * 
	 * @param g the graphics object on which to paint it
	 */
	private void paintOnce(Graphics g) {
		
		// cover the entire canvas with the background color
		// everything else will be painted over it
		g.setColor(TetrisConstants.BG_COLOR);
		g.fillRect(0, 0, this.screenWidth, this.screenHeight);
		
		// paint the info boxes
		this.scoreBox.paint(g);
		this.lineCountBox.paint(g);
		this.levelBox.paint(g);
		this.nextPieceBox.paint(g);
		
		if(TetrisConstants.TITLE_STATE == this.game.getGameState()) {
			// paint the title screen over the board
			this.paintBoard(g);
			this.titleBox.setHiScore(this.game.getHiScore());
			this.titleBox.paint(g);
			
		} else if(TetrisConstants.RUNNING_STATE == this.game.getGameState()) {
			// just need to paint the game board
			this.paintBoard(g);
			
		} else if(TetrisConstants.PAUSED_STATE == this.game.getGameState()) {
			// we paint a paused message and hide the board
			this.paintPausedBoard(g);
		}
	}
	
	/**
	 * Paint the info boxes of the right panel.
	 * 
	 * @param g the Graphics object on which to paint
	 */
	private void paintInfoBoxes(Graphics g) {
		
		// paint the score box only if its value has changed
		if(this.scoreBox.updateValue(this.game.getScore())) {
			this.scoreBox.paint(g);
		}
		
		// paint the line count box only if its value has changed
		if(this.lineCountBox.updateValue(this.game.getLineCount())) {
			this.lineCountBox.paint(g);
		}
		
		// paint the level box only if its value has changed
		if(this.levelBox.updateValue(this.game.getLevel())) {
			this.levelBox.paint(g);
		}
		
		// paint the next piece box only if the next piece has changed
		if(this.nextPieceBox.setPieceType(this.game.getNextPieceType())) {
			this.nextPieceBox.paint(g);
		}
	}
	
	/**
	 * Paint the board area in its paused state.
	 * We hide the board and print a paused message.
	 * 
	 * @param g the Graphics object to paint on
	 */
	private void paintPausedBoard(Graphics g) {
		// fill the whole board area to hide it
		g.setColor(TetrisConstants.EMPTY_COLOR);
		g.fillRect(this.boardX, this.boardY, this.boardWidth, this.boardHeight);
		
		// paint paused message at the center of the board
		int boardCenterX = this.boardX + (this.boardWidth / 2);
		int boardCenterY = this.boardY + (this.boardHeight / 2);
		
		g.setColor(TetrisConstants.COLOR_BLACK);
		g.drawString("paused", boardCenterX, boardCenterY, Graphics.HCENTER | Graphics.BOTTOM);
	}
	
	/**
	 * Paint the board state.  Paints each grid block individually if it needs to be painted.
	 * 
	 * @param g the Graphics object to paint on 
	 */
	private void paintBoard(Graphics g) {
		TetrisBoard board = this.game.getBoard();
		
		for(int x = 0; x < TetrisConstants.WIDTH; x++) {
			for(int y = TetrisConstants.TOP_VISIBLE_ROW; y < TetrisConstants.HEIGHT; y++) {
				int blockType = board.getBlockType(x, y);
				
				// check if the state of the block is different from the last time we painted
				if(blockType != this.getLastBoardState(x, y) || TetrisConstants.BLOCK_ACTIVE == blockType) {
					// repaint the block
					this.paintBlock(x, y, blockType, g);
					
					// update the saved state
					this.setLastBoardState(x, y, blockType);
				}
			}
		}
	}
	
	private int getLastBoardState(int x, int y) {
		return this.lastBoardState[x][y - TetrisConstants.TOP_VISIBLE_ROW];
	}
	
	private void setLastBoardState(int x, int y, int blockType) {
		this.lastBoardState[x][y - TetrisConstants.TOP_VISIBLE_ROW] = blockType;
	}
	
	/**
	 * Paint an individual block of the given type at the given location.
	 * 
	 * @param blockX x coordinate of the upper left of the block
	 * @param blockY y coordinate of the upper left of the block
	 * @param blockType the piece/block type of the block
	 * @param g the Graphics object to paint on
	 */
	private void paintBlock(int x, int y, int blockType, Graphics g) {
		int blockX = this.boardX + (this.blockSize * x);
		int blockY = this.boardY + (this.blockSize * (y - TetrisConstants.TOP_VISIBLE_ROW));
		
		if(TetrisConstants.BLOCK_EMPTY != blockType) {
			// draw a drop shadow
			g.setColor(TetrisConstants.COLOR_BLACK);
			g.fillRect(blockX + 1, blockY + 1, this.blockSize - 1, this.blockSize - 1);
			
			// then draw the actual block over the shadow
			this.setColor(blockType, g);
			g.fillRect(blockX, blockY, this.blockSize - 1, this.blockSize - 1);
			
		}  else {
			// block is empty, paint it the empty color
			g.setColor(TetrisConstants.COLOR_WHITE);
			g.fillRect(blockX, blockY, this.blockSize, this.blockSize);
		}	
	}
	
	/**
	 * Reset the board state so that one-time painting will be done with next painting.
	 */
	public void reset() {
		this.paintedOnce = false;
		
		// reset the board cached board state too
		for(int x = 0; x < TetrisConstants.WIDTH; x++) {
			for(int y = TetrisConstants.TOP_VISIBLE_ROW; y < TetrisConstants.HEIGHT; y++) {
				this.setLastBoardState(x, y, TetrisConstants.UNINITIALIZED);
			}	
		}
		
		this.repaint();
	}
	
	/**
	 * Set the color of the given Graphics object according to the block/piece type.
	 * 
	 * @param blockType the block/piece type to set to
	 * @param g the Graphics object to set the color on
	 */
	private void setColor(int blockType, Graphics g) {
		switch(blockType) {
			case TetrisConstants.BLOCK_EMPTY:
				g.setColor(TetrisConstants.EMPTY_COLOR);
				break;
			case TetrisConstants.BLOCK_ACTIVE:
				// block is active, set the color to the type of the active piece
				this.setColor(this.game.getActivePiece().getPieceType(), g);
				break;
			case TetrisConstants.I_PIECE:
				g.setColor(TetrisConstants.I_PIECE_COLOR);
				break;
			case TetrisConstants.O_PIECE:
				g.setColor(TetrisConstants.O_PIECE_COLOR);
				break;
			case TetrisConstants.T_PIECE:
				g.setColor(TetrisConstants.T_PIECE_COLOR);
				break;
			case TetrisConstants.S_PIECE:
				g.setColor(TetrisConstants.S_PIECE_COLOR);
				break;
			case TetrisConstants.Z_PIECE:
				g.setColor(TetrisConstants.Z_PIECE_COLOR);
				break;
			case TetrisConstants.L_PIECE:
				g.setColor(TetrisConstants.L_PIECE_COLOR);
				break;
			case TetrisConstants.J_PIECE:
				g.setColor(TetrisConstants.J_PIECE_COLOR);
				break;
		}
	}
	
	/**
	 * A lot of one-time ugliness.  Don't look in here...
	 * This method inspects the available screen/font, etc... and sets variables that determine the layout.
	 * The attempt here is to make things relative so that it will appear OK on different platforms.
	 */
	private void setupLayout() {
		// screen dimensions
		this.screenWidth = this.getWidth();
		this.screenHeight = this.getHeight();
		
		// get a simple font to use throughout
		this.font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		
		// get the minimum width for the info panel by using the width of a sample score
		int sampleLabelWidth = this.font.stringWidth("10000000");
		this.infoPanelWidth = sampleLabelWidth + (sampleLabelWidth / 4);  // 1.25 * sampleLabelWidth so box is a little wider 
		this.infoPanelX = screenWidth - this.infoPanelWidth;
		int infoBoxHeight = this.font.getHeight() * 2;					  // make the box twice has high as the font
		
		// get the width remaining on the screen minus the info panel
		int remainderWidth = screenWidth - this.infoPanelWidth;
		
		// initial settings
		this.boardX = 0;
		this.boardY = 0;
		this.viewableWidth = screenWidth;
		this.viewableHeight = screenHeight;
		
		// blocks should be a square, so we get the minimum possible dimension and use it for both
		int blockWidth = remainderWidth / TetrisConstants.WIDTH;
		int blockHeight = screenHeight / TetrisConstants.VIEWABLE_ROWS;	// only use visible rows in calculation
		this.blockSize = Math.min(blockWidth, blockHeight);
		
		// dimension of the board (grid area) is the size of the block * the dimension
		this.boardWidth = this.blockSize * TetrisConstants.WIDTH;
		this.boardHeight = this.blockSize * TetrisConstants.VIEWABLE_ROWS;
		
		// get the used dimensions of the screen
		this.viewableWidth = this.boardWidth + this.infoPanelWidth;
		this.viewableHeight = this.boardHeight;
		
		// center the board in the available space
		this.boardX = (this.screenWidth - this.viewableWidth) / 2;
		this.boardY = (this.screenHeight - this.viewableHeight) / 2;
		this.infoPanelX = this.boardX + this.boardWidth;
		
		// info boxes stacked on top of each other, get their total height (NextPieceBox is 1.5 height)
		int infoBoxTotalHeight = (infoBoxHeight * 9) / 2; // 4.5 * height of one box
		int remainderHeight = this.viewableHeight - infoBoxTotalHeight;
		int infoBoxY = this.boardY + (remainderHeight / 2);
		
		// build score box on top
		this.scoreBox = new InfoBox(this.infoPanelX, infoBoxY, this.infoPanelWidth, infoBoxHeight,
									TetrisConstants.COLOR_BLACK, TetrisConstants.COLOR_LIGHT_GREY,
									this.font, "score", this.game.getScore());

		// level box under score box
		infoBoxY += infoBoxHeight;
		this.levelBox = new InfoBox(this.infoPanelX, infoBoxY, this.infoPanelWidth, infoBoxHeight,
									TetrisConstants.COLOR_BLACK, TetrisConstants.COLOR_LIGHT_GREY,
									this.font, "level", this.game.getLevel());
		
		// line box under level box
		infoBoxY += infoBoxHeight;
		this.lineCountBox = new InfoBox(this.infoPanelX, infoBoxY, this.infoPanelWidth, infoBoxHeight,
										TetrisConstants.COLOR_BLACK, TetrisConstants.COLOR_LIGHT_GREY,
										this.font, "lines", this.game.getLineCount());
		
		// next piece box under level box
		infoBoxY += infoBoxHeight;
		this.nextPieceBox = new NextPieceBox(this.infoPanelX, infoBoxY, this.infoPanelWidth, (infoBoxHeight * 3) / 2,
											 TetrisConstants.COLOR_BLACK, TetrisConstants.COLOR_LIGHT_GREY, this.font);

		// longest string in the title box is possibly the hi score line
		// so we ensure that it's wide enough to accomodate an impossibly high score
		int titleBoxWidth = this.font.stringWidth("hi score: 1000000000000");
		
		// title box half the viewable height, or the height of four lines, whichever is more
		int titleBoxHeight = (this.viewableHeight / 2);
		titleBoxHeight = Math.max(titleBoxHeight, this.font.getHeight() * 4);
		
		// center the title box int he available space
		int titleBoxX = this.boardX + ((boardWidth - titleBoxWidth) / 2);
		titleBoxX = Math.max(titleBoxX, 0);
		int titleBoxY = this.boardY + ((boardHeight - titleBoxHeight) / 2);
		titleBoxY = Math.max(titleBoxY, 0);

		this.titleBox = new TitleBox(titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight,
									 TetrisConstants.COLOR_BLACK, TetrisConstants.COLOR_LIGHT_GREY,
									 this.font);
	}
}
