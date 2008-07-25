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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import tetris.TetrisConstants;

/**
 * A DisplayBox that shows the next piece.
 */
public class NextPieceBox extends DisplayBox {
	
	private int pieceType;		// the type of the piece to display
	private Font captionFont;	// the font to write the caption in
	
	private int leftBlockX;		// left most position of the mini-grid we display the next piece
	private int blockSize;		// the dimension of a block to fit into the display box.
	
	/**
	 * Make a new NextPieceBox.
	 * 
	 * @param x x coordinate of upper left of the box
	 * @param y y coordinate of upper left of the box
	 * @param width width of the box (pixels)
	 * @param height height of the box (pixels);
	 * @param fgColor foreground color, used in this class just for border
	 * @param bgColor background color
	 * @param captionFont the font to write the "next" caption in
	 */
	public NextPieceBox(int x, int y, int width, int height,
						int fgColor, int bgColor,
						Font captionFont) {
		super(x, y, width, height, fgColor, bgColor);
		
		this.captionFont = captionFont;
	
		this.setupLayout();
	}
	
	/**
	 * One time method to set up the layout in the box, ie how big the blocks are, positioning, etc...
	 */
	private void setupLayout() {
		// as if the box was composed of a 6x6 grid, will fit any piece with a block on either side
		final int gridSize = 6;
		
		// blocksize is minimum of the grid we can display given our size
		this.blockSize = Math.min(this.width / gridSize, this.height / gridSize);
		
		// center the grid in the display box by specifying its left most position
		this.leftBlockX = this.x + ((this.width - (this.blockSize * gridSize)) / 2);
	}
	
	/**
	 * Update the piece type.
	 * Its return value can be used to decide whether repainting is necessary.
	 * 
	 * @param pieceType the type of the piece to display
	 * @return true if the piece type was changed, false it matches the currently set piece type.
	 */
	public boolean setPieceType(int pieceType) {
		if(this.pieceType != pieceType) {
			this.pieceType = pieceType;
			return true;
		}

		return false;
	}
	
	/**
	 * Paint the NextPieceBox contents on the given Graphics object.
	 * @param g the Graphics object on which to paint
	 */
	protected void paintBoxContents(Graphics g) {
		// write the "next" caption in the foreground color
		g.setColor(this.fgColor);
		
		int xCenter = this.x + (this.width / 2);
		
		g.setFont(this.captionFont);
		g.drawString("next", xCenter, this.y, Graphics.TOP | Graphics.HCENTER);
		
		// paint the piece in the box
		switch(this.pieceType) {
			case TetrisConstants.I_PIECE:
				this.paintIPiece(g);
				break;
			case TetrisConstants.O_PIECE:
				this.paintOPiece(g);
				break;
			case TetrisConstants.T_PIECE:
				this.paintTPiece(g);
				break;
			case TetrisConstants.S_PIECE:
				this.paintSPiece(g);
				break;
			case TetrisConstants.Z_PIECE:
				this.paintZPiece(g);
				break;
			case TetrisConstants.L_PIECE:
				this.paintLPiece(g);
				break;
			case TetrisConstants.J_PIECE:
				this.paintJPiece(g);
				break;
		}
	}
	
	/**
	 * Paint a block, paints a 1 pixel drop shadow.
	 * 
	 * @param x the x coordinate to paint the block at
	 * @param y the y coordinate to paint the block at
	 * @param blockSize the size of a block to print in the display box
	 * @param color the color to paint in
	 * @param g the Graphics object on which to paint
	 */
	private void paintBlock(int x, int y, int blockSize, int color, Graphics g) {
		g.setColor(TetrisConstants.COLOR_BLACK);
		g.fillRect(x + 1, y + 1, blockSize - 1, blockSize - 1);
		g.setColor(color);
		g.fillRect(x, y, blockSize - 1, blockSize - 1);
	}
	
	/**
	 * ####
	 * 
	 * @param g a Graphics object on which to paint an I piece
	 */
	private void paintIPiece(Graphics g) {
		int color = TetrisConstants.I_PIECE_COLOR;
		
		int x = this.leftBlockX + this.blockSize;
		int y = (this.y + this.height) - (this.blockSize * 2);
		
		this.paintBlock(x, y, this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y, this.blockSize, color, g);
		this.paintBlock(x + (2 *this. blockSize), y, this.blockSize, color, g);
		this.paintBlock(x + (3 * this.blockSize), y, this.blockSize, color, g);
	}
	
	/**
	 * ##
	 * ##
	 * 
	 * @param g a Graphics object on which to paint an O piece
	 */
	private void paintOPiece(Graphics g) {
		int color = TetrisConstants.O_PIECE_COLOR;
		
		int x = this.leftBlockX + (this.blockSize * 2);
		int y = (this.y + this.height) - (this.blockSize * 3);
		
		this.paintBlock(x, y, this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y, this.blockSize, color, g);
		this.paintBlock(x, y + this.blockSize, this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y + this.blockSize, this.blockSize, color, g);
	}
	
	/**
	 *  #
	 * ###
	 * 
	 * @param g a Graphics object on which to paint an T piece
	 */
	private void paintTPiece(Graphics g) {
		int color = TetrisConstants.T_PIECE_COLOR;
		
		int x = this.leftBlockX + (this.blockSize * 3);
		int y = (this.y + this.height) - (this.blockSize * 3);
		
		this.paintBlock(x, y, this.blockSize, color, g);
		this.paintBlock(x - this.blockSize, y + this.blockSize, this.blockSize, color, g);
		this.paintBlock(x, y + this.blockSize, this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y + this.blockSize, this.blockSize, color, g);
	}
	
	/**
	 *  ##
	 * ##
	 * 
	 * @param g a Graphics object on which to paint an S piece
	 */
	private void paintSPiece(Graphics g) {
		int color = TetrisConstants.S_PIECE_COLOR;
		
		int x = this.leftBlockX + (this.blockSize * 2);
		int y = (this.y + this.height) - (this.blockSize * 3);
		
		this.paintBlock(x, y, this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y, this.blockSize, color, g);
		this.paintBlock(x - this.blockSize, y + this.blockSize, this.blockSize, color, g);
		this.paintBlock(x, y + this.blockSize, this.blockSize, color, g);
	}
	
	/**
	 * ##
	 *  ##
	 *  
	 * @param g a Graphics object on which to paint a Z piece
	 */
	private void paintZPiece(Graphics g) {
		int color = TetrisConstants.Z_PIECE_COLOR;
		
		int x = this.leftBlockX + this.blockSize;
		int y = (this.y + this.height) - (this.blockSize * 3);
		
		this.paintBlock(x, y, this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y, this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y + this.blockSize, this.blockSize, color, g);
		this.paintBlock(x + (this.blockSize * 2), y + this.blockSize, this.blockSize, color, g);
	}
	
	/**
	 * #
	 * #
	 * ##
	 *  
	 * @param g a Graphics object on which to paint a L piece
	 */
	private void paintLPiece(Graphics g) {
		int color = TetrisConstants.L_PIECE_COLOR;
		
		int x = this.leftBlockX + (this.blockSize * 2);
		int y = (this.y + this.height) - (this.blockSize * 4);
		
		this.paintBlock(x, y, this.blockSize, color, g);
		this.paintBlock(x, y + this.blockSize, this.blockSize, color, g);
		this.paintBlock(x, y + (this.blockSize * 2), this.blockSize, color, g);
		this.paintBlock(x + this.blockSize, y + (this.blockSize * 2), this.blockSize, color, g);
	}
	
	/**
	 *  #
	 *  #
	 * ##
	 *  
	 * @param g a Graphics object on which to paint a L piece
	 */
	private void paintJPiece(Graphics g) {
		int color = TetrisConstants.J_PIECE_COLOR;
		
		int x = this.leftBlockX + (this.blockSize * 3);
		int y = (this.y + this.height) - (this.blockSize * 4);
		
		this.paintBlock(x, y, this.blockSize, color, g);
		this.paintBlock(x, y + this.blockSize, this.blockSize, color, g);
		this.paintBlock(x, y + (this.blockSize * 2), this.blockSize, color, g);
		this.paintBlock(x - this.blockSize, y + (this.blockSize * 2), this.blockSize, color, g);
	}
}
