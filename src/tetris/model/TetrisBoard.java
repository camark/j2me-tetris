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
 * Defines the state of the grid of blocks.
 * 
 * Has logic for manipulating supplied pieces' state on the board.
 * Whenever the board adjusts its state to reflect that a piece has changed,
 * it should also ensure that the piece's state is updated as well.
 */
public class TetrisBoard {
	
	private int[][] boardBlocks = new int[TetrisConstants.WIDTH][TetrisConstants.HEIGHT];	// the grid of blocks
	
	/**
	 * Make a new board.
	 */
	public TetrisBoard() {
		this.clearBoard();
	}
	
	/**
	 * Clear the board so that all blocks on the board are empty
	 *
	 */
	public void clearBoard() {
		for(int y = 0; y < TetrisConstants.HEIGHT; y++) {
			this.clearRow(y);
		}
	}
	
	/**
	 * Clear the row at the given row index so that all blocks are empty
	 * 
	 * @param rowY the index of the row to clear, top of the board is lower, bottom higher
	 */
	public void clearRow(int rowY) {
		for(int x = 0; x < TetrisConstants.WIDTH; x++) {
			this.setBlockType(x, rowY, TetrisConstants.BLOCK_EMPTY);
		}
	}
	
	////////////////////
	
	/**
	 * Use to lock down pieces once they can't move down further.
	 * Adjusts the board blocks that are part of the active piece so that are set as their piece types instead of active.
	 * 
	 * @param piece the piece to lock down
	 */
	public void lockPiece(TetrisPiece piece) {
		int pieceType = piece.getPieceType();
		
		this.updatePieceBlocks(piece, pieceType);
	}
	
	/**
	 * Checks that all pieces in the give row are filled in.
	 * 
	 * @param rowY the y index of the row on the board, lower indexes at the top, higher at the bottom
	 * @return true if the row is completely filled in, false otherwise
	 */
	public boolean checkRowCompleted(int rowY) {
		for(int x = 0; x < TetrisConstants.WIDTH; x++) {
			if(TetrisConstants.BLOCK_EMPTY == this.getBlockType(x, rowY)) {
				// empty block, so the row is not filled
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Translate all blocks in the given row down the given number of rows.
	 * Note that since this is meant to be called sequentially from the bottom up,
	 * an isolated call of this method will leave the original row in a stale state.
	 * A dropRow called on an above row should overwrite the stale data. 
	 * 
	 * @param rowY the y index of the row to drop, lower indexes at the top, higher at the bottom
	 * @param numRows the number of rows to translate the row blocks down
	 */
	public void dropRow(int rowY, int numRows) {
		for(int x = 0; x < TetrisConstants.WIDTH; x++) {
			int blockType = this.getBlockType(x, rowY);
			
			this.setBlockType(x, rowY + numRows, blockType);
		}
	}
	
	////////////////////
	
	/**
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the block/piece type at the given coordinates
	 */
	public int getBlockType(int x, int y) {
		return this.boardBlocks[x][y];
	}
	
	/**
	 * @param x the x coordinate of the block to set
	 * @param y the y coordinate of the block to set
	 * @param pieceType the type of the piece to set the block to
	 */
	private void setBlockType(int x, int y, int pieceType) {
		this.boardBlocks[x][y] = pieceType;
	}
	
	/**
	 * Check that a block from the active piece can be moved into this block.
	 * This means that it should be empty, or already part of the active piece since,
	 * a part of the block can move into a spot currently occupied by a different block from the same piece.
	 * 
	 * @param x the x coordinate to check
	 * @param y the y coordinate to check
	 * @return true if a block is open to be moved in to
	 */
	private boolean checkBlockMove(int x, int y) {
		if(this.isOnBoard(x, y)) { // first check that the coordinates are on the board
			
			int blockType = this.getBlockType(x, y);
			
			return TetrisConstants.BLOCK_EMPTY  == blockType  || // block is empty OR
			TetrisConstants.BLOCK_ACTIVE == blockType;    // already part of the piece
		}
		
		return false; // off the board
	}
	
	/**
	 * Check that the given block coordinates are on the board.
	 * 
	 * @param x the x coordinate to check
	 * @param y the y coordinate to check
	 * @return true if the coordinates are on the board, false otherwise
	 */
	private boolean isOnBoard(int x, int y) {
		return x >= 0 		&&		// not off the left edge
		  	   x <  TetrisConstants.WIDTH	&&		// not off the right edge
			   y >= 0 		&&		// not off the bottom
			   y <  TetrisConstants.HEIGHT;			// not already at the top? can't move up anyway...
	}
	
	/**
	 * Update all blocks on the board that are part of a so that they have the piece's block type.
	 * @param piece the piece to update
	 * @param blockType the block/piece type to update the piece to
	 */
	private void updatePieceBlocks(TetrisPiece piece, int blockType) {
		// set each of the four blocks
		for(int i = 0; i < TetrisConstants.FOUR_BLOCKS; i++) {
			int blockX = piece.getBlockX(i);
			int blockY = piece.getBlockY(i);
			
			this.setBlockType(blockX, blockY, blockType);
		}
	}
	
	////////////////////
	
	/**
	 * Check the given piece can be placed on the board, ie that its blocks are currently empty.
	 * 
	 * @param piece the piece to check
	 * @return true if the piece fits on the board, false otherwise 
	 */
	public boolean canAddNewPiece(TetrisPiece piece) {
		// check each of the four blocks
		for(int i = 0; i < TetrisConstants.FOUR_BLOCKS; i++) {
			int blockX = piece.getBlockX(i);
			int blockY = piece.getBlockY(i);
			
			if(!this.checkBlockMove(blockX, blockY)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Add the new piece to the board by marking its position blocks as in an active state.
	 * @param piece the piece to add to the board
	 */
	public void addNewPiece(TetrisPiece piece) {
		this.updatePieceBlocks(piece, TetrisConstants.BLOCK_ACTIVE);
	}
	
	////////////////////
	
	/**
	 * Check that the given piece can move down one row, (ie the below blocks are empty)
	 * 
	 * @param piece the piece to check to move down
	 * @return true if the piece can move down, false otherwise
	 */
	public boolean canMoveDown(TetrisPiece piece) {
		return this.canTranslatePiece(piece, 0, 1);
	}
	
	/**
	 * Check that the given piece can move left one column, (ie the left blocks are empty)
	 * 
	 * @param piece the piece to check to move left
	 * @return true if the piece can move left, false otherwise
	 */
	public boolean canMoveLeft(TetrisPiece piece) {
		return this.canTranslatePiece(piece, -1, 0);
	}
	
	/**
	 * Check that the given piece can move right one column, (ie the right blocks are empty)
	 * 
	 * @param piece the piece to check to move right
	 * @return true if the piece can move right, false otherwise
	 */
	public boolean canMoveRight(TetrisPiece piece) {
		return this.canTranslatePiece(piece, 1, 0);
	}
	
	/**
	 * Check that the given piece can be translated.
	 * 
	 * @param piece the piece to translate
	 * @param dx the blocks to move horizontally positive moves right, negative moves left
	 * @param dy the blocks to move vertically, positive moves down, negative moves ?up?
	 * @return true if the block can be translated, false otherwise
	 */
	private boolean canTranslatePiece(TetrisPiece piece, int dx, int dy) {
		
		// find each of the new blocks that would be occupied, check each one of them
		for(int i = 0; i < TetrisConstants.FOUR_BLOCKS; i++) {
			int blockX = piece.getBlockX(i);
			int blockY = piece.getBlockY(i);
			
			int moveX = blockX + dx;
			int moveY = blockY + dy;
			
			if(!this.checkBlockMove(moveX, moveY)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Move the given piece down one row.
	 * @param piece the piece to move down.
	 */
	public void moveDown(TetrisPiece piece) {
		this.translatePiece(piece, 0, 1);
	}
	
	
	/**
	 * Move the given piece left one column.
	 * @param piece the piece to move left.
	 */
	public void moveLeft(TetrisPiece piece) {
		this.translatePiece(piece, -1, 0);
	}
	
	/**
	 * Move the given piece right one column.
	 * @param piece the piece to move right.
	 */
	public void moveRight(TetrisPiece piece) {
		this.translatePiece(piece, 1, 0);
	}
	
	/**
	 * Translate the piece.  A helper called by exposed translate methods.
	 * Updates the state on the board as well as in the piece.
	 * 
	 * @param piece the piece to translate
	 * @param dx the number of blocks to move horizontally, positive for right, negative for left
	 * @param dy the number of blocks to move vertically, positive for down, negative for ?up?
	 */
	public void translatePiece(TetrisPiece piece, int dx, int dy) {
		// clear the current position of the piece on the board
		this.updatePieceBlocks(piece, TetrisConstants.BLOCK_EMPTY);
		
		// rotate the piece, this adjusts the state of the piece
		piece.translatePiece(dx, dy);

		// update the board with the new position of the piece
		this.updatePieceBlocks(piece, TetrisConstants.BLOCK_ACTIVE);
	}
	
	////////////////////
	
	/**
	 * Check that the piece can be rotated left (counter-clockwise).
	 * 
	 * @param piece the piece to rotate left
	 * @return true if the piece can be rotated left, false otherwise
	 */
	public boolean canRotateLeft(TetrisPiece piece) {
		return this.canRotatePiece(piece,
				   piece.getBlockX(TetrisConstants.PIVOT_INDEX), piece.getBlockY(TetrisConstants.PIVOT_INDEX),
				   true);
	}
	
	/**
	 * Check that the piece can be rotated right (clockwise).
	 * @param piece the piece to rotate right
	 * @return true if the piece can be rotated right, false otherwise.
	 */
	public boolean canRotateRight(TetrisPiece piece) {
		return this.canRotatePiece(piece,
								   piece.getBlockX(TetrisConstants.PIVOT_INDEX), piece.getBlockY(TetrisConstants.PIVOT_INDEX),
								   false);
	}
	
	/**
	 * Check that the piece can be rotated, ie check that the new position is empty.
	 * 
	 * @param piece the piece to rotate
	 * @param pivotX the x coordinate pivot around which to rotate the piece
	 * @param pivotY the y coordinate pivot around which to rotate the piece
	 * @param rotateDirection true if rotate left, false rotate right
	 * @return true if can rotate, false othwerwise
	 */
	private boolean canRotatePiece(TetrisPiece piece, int pivotX, int pivotY, boolean rotateDirection) {
		
		int rotationType = piece.getRotationType();
		if(TetrisConstants.ROTATION_TYPE_NONE == rotationType) {
			// piece can't rotate at all no matter what, this should be an O piece.
			return false;
		} else if(TetrisConstants.ROTATION_TYPE_TOGGLE == rotationType) {
			// an I, S, or Z piece, can only has two flip positions
			rotateDirection = piece.getRotationToggle();
		} // else ROTATION_TYPE_FREE, just use the given rotate direction
			
		for(int i = 0; i < TetrisConstants.FOUR_BLOCKS; i++) {
			int blockX = piece.getBlockX(i);
			int blockY = piece.getBlockY(i);
			
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
			
			if(!this.checkBlockMove(rotateX, rotateY)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Rotate the piece left (counter-clockwise).
	 * @param piece the piece to rotate.
	 */
	public void rotateLeft(TetrisPiece piece) {
		this.rotatePiece(piece,
				   		 piece.getBlockX(TetrisConstants.PIVOT_INDEX), piece.getBlockY(TetrisConstants.PIVOT_INDEX),
						 true);
	}
	
	/**
	 * Rotate the piece right (clockwise).
	 * @param piece the piece to rotate.
	 */
	public void rotateRight(TetrisPiece piece) {
		this.rotatePiece(piece,
				   		 piece.getBlockX(TetrisConstants.PIVOT_INDEX), piece.getBlockY(TetrisConstants.PIVOT_INDEX),
						 false);
	}
	
	/**
	 * Rotate the piece around the given pivot.  A helper called by exposed rotate methods.
	 * Updates the state on the board as well as in the piece.
	 * 
	 * @param piece the piece to rotate
	 * @param pivotX the x coordinate around which to rotate
	 * @param pivotY the y coordinate around which to rotate
	 * @param direction true if rotate right, false if rotate left
	 */
	private void rotatePiece(TetrisPiece piece, int pivotX, int pivotY, boolean direction) {
		// clear the current position of the piece on the board
		this.updatePieceBlocks(piece, TetrisConstants.BLOCK_EMPTY);
		
		// rotate the piece, this adjusts the state of the piece
		piece.rotate(pivotX, pivotY, direction);

		// update the board with the new position of the piece
		this.updatePieceBlocks(piece, TetrisConstants.BLOCK_ACTIVE);
	}
}
