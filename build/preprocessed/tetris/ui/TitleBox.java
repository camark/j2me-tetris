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

/**
 * Shows a title screen.
 * Shows the info, current hi score, and prompt to select a level.
 */
public class TitleBox extends DisplayBox {

	private Font font;	 // the font to write in
	private int hiScore; // the current hi score
	
	private static final String TITLE_STRING = "tetris";
	private static final String AUTHOR_STRING = "jordan kiang";
	private static final String HI_SCORE_PREFIX_STRING = "hi score: ";
	private static final String LEVEL_STRING = "select level 0-9";

	/**
	 * Make a new TitleBox.
	 * 
	 * @param x x coordinate of upper left of the box
	 * @param y y coordinate of upper left of the box
	 * @param width width of the box (pixels)
	 * @param height height of the box (pixels);
	 * @param fgColor foreground color, used in this class just for border
	 * @param bgColor background color
	 * @param font the font to print in
	 */
	public TitleBox(int x, int y, int width, int height,
					int fgColor, int bgColor,
					Font font) {
		super(x, y, width, height, fgColor, bgColor);
		
		this.font = font;
	}
	

	/**
	 * Paint the box contents on the given Graphics object.
	 * @param g the Graphics object on which to paint
	 */
	protected void paintBoxContents(Graphics g) {
		// horizontal center of the box
		int centerX = this.x + (this.width / 2);
		
		// start rows at the bottom of the box, move up from there
		int dy = this.height / 4;		// get space between rows
		int y = this.y + this.height;	// get initial y, initial at bottom of the box
				
		// prompt to choose a level
		g.drawString(LEVEL_STRING, centerX, y, Graphics.BOTTOM | Graphics.HCENTER);
		
		// current hi score
		y -= dy;
		g.drawString(HI_SCORE_PREFIX_STRING + this.hiScore, centerX, y, Graphics.BOTTOM | Graphics.HCENTER);
		
		// my name!
		y -= dy;
		g.drawString(AUTHOR_STRING, centerX, y, Graphics.BOTTOM | Graphics.HCENTER);

		// name of the game
		y -= dy;
		g.drawString(TITLE_STRING, centerX, y, Graphics.BOTTOM | Graphics.HCENTER);
	}
	
	/**
	 * Update the hi score used.
	 * @param hiScore to update to
	 * @return if the new hi score is different from the existing hi score.
	 */
	public boolean setHiScore(int hiScore) {
		if(this.hiScore != hiScore) {
			this.hiScore = hiScore;
			return true;
		}
		
		return false;
	}
}
