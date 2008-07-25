/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tetris;

/**
 *
 * @author gm
 */
public class TetrisConstants {
	public static final int UNINITIALIZED = 0;	// used to indicate an int variable that should hold a constant hasn't been initialized
	
	public static final int TITLE_STATE	  = 1;	// app running, game not started
	public static final int RUNNING_STATE = 2;	// app started, game running
	public static final int PAUSED_STATE  = 3;	// app paused during game
	
	public static final int WIDTH  = 10;		// number of grid blocks horizontally
	public static final int HEIGHT = 20;		// number of grid blocks vertically, + number of rows off the screen at the top
	
	// invisible rows off the top of the screen allow immediate rotation, rather than having to wait for the piece to drop a couple of rows
	public static final int TOP_VISIBLE_ROW = 2;							// index of the first visible row
	public static final int VIEWABLE_ROWS 	= HEIGHT - TOP_VISIBLE_ROW;		// number of visible blocks (not off the top of the screen)
	
	// starting coordinates of new pieces, see TetrisPiece implementation to see exactly how new pieces positioned
	// if WIDTH changed, START_X should probably be changed
	public static final int START_X = 4;				// x position of new piece (rows indexed from 0)
	public static final int START_Y = TOP_VISIBLE_ROW;	// y position of new piece (rows indexed from 0)
	
	public static final int NUM_PIECE_TYPES = 7;		// number of difference piece types
	
	public static final int BLOCK_ACTIVE		= -2;	// block is part of the active piece
	public static final int BLOCK_EMPTY			= -1;	// block is empty
												// 0;	// UNINITIALIZED 
	public static final int I_PIECE				=  1;	// part of an I piece
	public static final int O_PIECE				=  2;	// part of an O piece 
	public static final int T_PIECE				=  3;	// part of a T piece
	public static final int S_PIECE				=  4;	// part of an S piece
	public static final int Z_PIECE				=  5;	// part of a Z piece
	public static final int L_PIECE				=  6;	// part of an L piece
	public static final int J_PIECE				=  7;	// part of a J piece
	
	public static final int ROTATION_TYPE_NONE   = 1;
	public static final int ROTATION_TYPE_TOGGLE = 2;
	public static final int ROTATION_TYPE_FREE   = 3;
	
	public static final int FOUR_BLOCKS = 4;	// number of blocks in a piece, just so we don't have 4's all over
	public static final int PIVOT_INDEX = 1;	// index in a TetrisPiece's block array of the block a piece pivots around

	// predefined RGB colors, hex
	public static final int COLOR_BLACK		 = 0x00000000;
	public static final int COLOR_WHITE		 = 0x00ffffff;
	public static final int COLOR_LIGHT_GREY = 0x00bbbbbb;
	public static final int COLOR_GREY		 = 0x00999999;
	public static final int COLOR_DARK_GREY  = 0x00555555;
	public static final int COLOR_RED		 = 0x00ff0000;
	public static final int COLOR_ORANGE	 = 0x00ff9933;
	public static final int COLOR_YELLOW	 = 0x00ffff00;
	public static final int COLOR_GREEN		 = 0x0000ff00;
	public static final int COLOR_DARK_GREEN = 0x00009900;
	public static final int COLOR_CYAN		 = 0x0000ffff;
	public static final int COLOR_BLUE		 = 0x000000ff;
	
	// color assignments for block states
	public static final int BG_COLOR	  = COLOR_GREY;
	public static final int EMPTY_COLOR   = COLOR_WHITE;
	public static final int I_PIECE_COLOR = COLOR_RED;
	public static final int O_PIECE_COLOR = COLOR_YELLOW;
	public static final int T_PIECE_COLOR = COLOR_ORANGE;
	public static final int S_PIECE_COLOR = COLOR_GREEN;
	public static final int Z_PIECE_COLOR = COLOR_DARK_GREEN;
	public static final int L_PIECE_COLOR = COLOR_BLUE;
	public static final int J_PIECE_COLOR = COLOR_CYAN;
	
	public static final int MAX_LEVEL = 20;		// level doesn't increase after MAX_LEVEL
	
	public static final int LEVEL_UNIT = 10;	// how many lines to clear before advancing level
	
	public static final int BASE_SPEED = 1000;	// tick speed at level 0
	// use explicit numerator and denominator, so we can use integer math
	public static final int SPEED_INCREASE_NUMERATOR	= 6;	// numerator of fraction to multiply tick speed by at new level
	public static final int SPEED_INCREASE_DENOMINATOR	= 7;	// denominator of fraction to multiply tick speed by at new level
	
	// formula for scores is (ROW_SCORE * level) + ROW_SCORE
	public static final int ONE_ROW_SCORE = 40;		// score for 1 line cleared
	public static final int TWO_ROW_SCORE = 100;	// score for 2 lines cleared
	public static final int THREE_ROW_SCORE = 300;	// score for 3 lines cleared
	public static final int FOUR_ROW_SCORE = 1200;	// score for 4 lines cleared (tetris)
	
	public static final String TETRIS_RECORD_STORE = "TetrisStore";		// name of record store to use (for hi score);
}
