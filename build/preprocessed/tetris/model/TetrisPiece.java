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

package tetris.model;

import tetris.TetrisConstants;

/**
 * An instance of this defines the active piece that is being dropped.
 * Rather than instantiating a new one all the time, we use the same object and just reset its type and blocks.
 * 
 * Using a single instance saves in heap operations.  This is really fairly negligible, so may consider
 * refactoring with an abstract parent and concrete implementations for each piece type.
 * This would allow custom rotating behavior, like two position rotating for I, S, and Z types, and no rotating for O.
 * 
 * Note that this class has no knowledge of the board.  Anything updating the piece state will also want to
 * adjust the board.  The adjusting logic can adjust the piece, and then examine the results to see how to
 * adjust the board, however.
 */
public class TetrisPiece {
	
	// defines the blocks that are part of this piece
	private int[][] blocks = new int[TetrisConstants.FOUR_BLOCKS][2];	// 2nd dim: [0] is x, [1] is y
	
	private int pieceType;			// the type of the piece
	
	private int rotationType;		// the rotation type of the piece, should be one of defined constants, same until set as new piece
	private boolean rotationToggle;	// the current rotation position, changes with each rotation

	////////////////////
	
	/**
	 * Reset this piece to reuse it as a new piece.
	 * 
	 * @param pieceType the type, should be a constant defined constants file
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	public void setAsNewPiece(int pieceType, int x, int y) {
		switch(pieceType) {
			case TetrisConstants.I_PIECE:
				this.setAsNewIPiece(TetrisConstants.START_X, TetrisConstants.START_Y);
				break;
			case TetrisConstants.O_PIECE:
				this.setAsNewOPiece(TetrisConstants.START_X, TetrisConstants.START_Y);
				break;
			case TetrisConstants.T_PIECE:
				this.setAsNewTPiece(TetrisConstants.START_X, TetrisConstants.START_Y);
				break;
			case TetrisConstants.S_PIECE:
				this.setAsNewSPiece(TetrisConstants.START_X, TetrisConstants.START_Y);
				break;
			case TetrisConstants.Z_PIECE:
				this.setAsNewZPiece(TetrisConstants.START_X, TetrisConstants.START_Y);
				break;
			case TetrisConstants.L_PIECE:
				this.setAsNewLPiece(TetrisConstants.START_X, TetrisConstants.START_Y);
				break;
			case TetrisConstants.J_PIECE:
				this.setAsNewJPiece(TetrisConstants.START_X, TetrisConstants.START_Y);
		}
	}
	
	////////////////////
	// for the below definitions, see the piece diagrams for the type
	// @ means that block is the pivot block for the piece
	// pivot point is also the starting location of the new piece
	
	/**
	 * 	##@#
	 * 	
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	private void setAsNewIPiece(int x, int y) {
		this.pieceType = TetrisConstants.I_PIECE;
		
		this.rotationType = TetrisConstants.ROTATION_TYPE_TOGGLE;	// only has two rotation positions
		this.rotationToggle = true;					// only necessary to reset for consistent behavior with new pieces
		
		this.setBlockCoords(0, x - 1, y);		
		this.setBlockCoords(1, x    , y);
		this.setBlockCoords(2, x + 1, y);
		this.setBlockCoords(3, x + 2, y);
	}
	
	/**
	 * 	@#
	 * 	##
	 * 
	 * pivot only used for initial placement, O's can't rotate
	 * 
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	private void setAsNewOPiece(int x, int y) {
		this.pieceType = TetrisConstants.O_PIECE;
		
		this.rotationType = TetrisConstants.ROTATION_TYPE_NONE;		// can't rotate at all
		
		this.setBlockCoords(0, x,     y);
		this.setBlockCoords(1, x + 1, y);
		this.setBlockCoords(2, x,     y + 1);
		this.setBlockCoords(3, x + 1, y + 1);
	}
	
	/**
	 *	#@#
	 * 	 #
	 * 
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	private void setAsNewTPiece(int x, int y) {
		this.pieceType = TetrisConstants.T_PIECE;
		
		this.rotationType = TetrisConstants.ROTATION_TYPE_FREE;		// rotates freely, four possible positions
		
		this.setBlockCoords(0, x - 1, y);
		this.setBlockCoords(1, x,     y);
		this.setBlockCoords(2, x + 1, y);
		this.setBlockCoords(3, x,     y + 1);
	}
	
	/**
	 *	 @#
	 *  ##
	 * 
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	private void setAsNewSPiece(int x, int y) {
		this.pieceType = TetrisConstants.S_PIECE;
		
		this.rotationType = TetrisConstants.ROTATION_TYPE_TOGGLE;	// only has two rotation positions
		this.rotationToggle = true;					// only necessary to reset for consistent behavior with new pieces
		
		this.setBlockCoords(0, x + 1, y);
		this.setBlockCoords(1, x,     y);
		this.setBlockCoords(2, x,	  y + 1);
		this.setBlockCoords(3, x - 1, y + 1);
	}
	
	/**
	 *	#@  
	 *   ##
	 * 
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	private void setAsNewZPiece(int x, int y) {
		this.pieceType = TetrisConstants.Z_PIECE;
		
		this.rotationType = TetrisConstants.ROTATION_TYPE_TOGGLE;	// only has two rotation positions
		this.rotationToggle = true;					// only necessary to reset for consistent behavior with new pieces
		
		this.setBlockCoords(0, x - 1, y);
		this.setBlockCoords(1, x,     y);
		this.setBlockCoords(2, x,     y + 1);
		this.setBlockCoords(3, x + 1, y + 1);
	}
	
	/**
	 *	#@#
	 *	#
	 *
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	private void setAsNewLPiece(int x, int y) {
		this.pieceType = TetrisConstants.L_PIECE;
		
		this.rotationType = TetrisConstants.ROTATION_TYPE_FREE;		// rotates freely, four possible positions
		
		this.setBlockCoords(0, x - 1, y);
		this.setBlockCoords(1, x,     y);
		this.setBlockCoords(2, x + 1, y);
		this.setBlockCoords(3, x - 1, y + 1);
	}
	
	/**
	 *	#@#
	 * 	  #
	 * 
	 * @param x the x coordinate around which the new piece will start
	 * @param y the y coordinate around which the new piece will start
	 */
	private void setAsNewJPiece(int x, int y) {
		this.pieceType = TetrisConstants.J_PIECE;
		
		this.rotationType = TetrisConstants.ROTATION_TYPE_FREE;		// rotates freely, four possible positions
		
		this.setBlockCoords(0, x - 1, y);
		this.setBlockCoords(1, x,     y);
		this.setBlockCoords(2, x + 1, y);
		this.setBlockCoords(3, x + 1, y + 1);
	}
	
	////////////////////
	
	/**
	 * @param a block index in the piece, should be 0-3 (four blocks per piece
	 * @return the x coordinate of the block at that index of the piece
	 */
	public int getBlockX(int blockIndex) {
		return this.blocks[blockIndex][0];
	}

	/**
	 * @param a block index in the piece, should be 0-3 (four blocks per piece
	 * @return the y coordinate of the block at that index of the piece
	 */
	public int getBlockY(int blockIndex) {
		return this.blocks[blockIndex][1];	
	}
	
	/** 
	 * @param a block index in the piece, should be 0-3 (four blocks per piece
	 * @param x the x coordinate to set the block at the block index to
	 * @param y the y coordinate to set the block at the block index to
	 */
	public void setBlockCoords(int blockIndex, int x, int y) {
		this.blocks[blockIndex][0] = x;	// x is index 0 in the 2nd dim
		this.blocks[blockIndex][1] = y;	// y is index 1 in the 2nd dim
	}
	
	/**
	 * Translate the piece on the board.  Adjusts all four block pieces.
	 * 
	 * @param dx the number of blocks to move horizontally positive right, negative left
	 * @param dy the number of blocks to move vertically, positive down, negative ?up?
	 */
	public void translatePiece(int dx, int dy) {
		
		// iterate over the four blocks, translate each
		for(int i = 0; i < TetrisConstants.FOUR_BLOCKS; i++) {
			int moveX = this.getBlockX(i) + dx;
			int moveY = this.getBlockY(i) + dy;
		
			this.setBlockCoords(i, moveX, moveY);
		}
	}
	
	/**
	 * Rotate the piece around the supplied pivot.
	 * 
	 * @param pivotX x coordinate of the block to rotate around
	 * @param pivotY y coordinate of the block to rotate around
	 * @param rotateDirection true if rotate left, false if rotate right
	 */
	public void rotate(int pivotX, int pivotY, boolean rotateDirection) {
		
		if(TetrisConstants.ROTATION_TYPE_TOGGLE == this.rotationType) {
			// if this is a toggling rotation piece (I, S, or Z), we just ignore
			// the requested rotation direction, and just toggle back and forth
			rotateDirection = this.rotationToggle;
			this.rotationToggle = !this.rotationToggle; // toggle should be after use, so it will match the get in canRotate
		}
		
		// iterate over the four blocks, rotate each
		for(int i = 0; i < TetrisConstants.FOUR_BLOCKS; i++) {
			int blockX = this.getBlockX(i);
			int blockY = this.getBlockY(i);
			
			// rotate left: swap the x and y, and flip sign of x
			// rotate right: swap the x and y, and flip sign of y 
			int dx = blockY - pivotY;
			int dy = blockX - pivotX;
			
			if(rotateDirection) {
				// rotate left
				dx *= -1;
			} else {
				// rotate right
				dy *= -1;
			}
			
			int rotateX = pivotX + dx;
			int rotateY = pivotY + dy;
			
			this.setBlockCoords(i, rotateX, rotateY);
		}
	}
	
	/**
	 * @return the currently set type of this piece, should be a constant defined in constants
	 */
	public int getPieceType() {
		return this.pieceType;
	}

	/**
	 * @return the rotation type of the current piece, should be a constant defined in constants
	 */
	public int getRotationType() {
		return this.rotationType;
	}
	
	/**
	 * @return get the current rotation toggle, useful when piece is one of those with only two rotations, (I, S, Z)
	 */
	public boolean getRotationToggle() {
		return this.rotationToggle;
	}
}
