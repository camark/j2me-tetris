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
 * An InfoBox has a caption and an integer value to display.
 * It displays the caption on one line and the value below it, each centered horizontally.
 */
public class InfoBox extends DisplayBox {

	private String caption;	// the caption to print
	private int value;		// the value to print, will be updated throughout the game
	
	private Font font;		// the font to print in
	
	/**
	 * 
	 * 
	 * @param x x coordinate of upper left of the box
	 * @param y y coordinate of upper left of the box
	 * @param width width of the box (pixels)
	 * @param height height of the box (pixels);
	 * @param fgColor foreground color, used in this class just for border
	 * @param bgColor background color
	 * @param font the font to print in
	 * @param caption the caption to print
	 * @param initialValue the initial value to print
	 * 
	 */
	public InfoBox(int x, int y, int width, int height,
				   int fgColor, int bgColor,
				   Font font, String caption,  int initialValue) {
		super(x, y, width, height, fgColor, bgColor);
		
		this.caption = caption;
		this.font = font;
		this.value = initialValue;
	}
	
	/**
	 * Update the value to print in this box.
	 * Indicates whether the value is different that what is currently set.
	 * This can be used to decide if it needs to be repainted or not.
	 * 
	 * @param value the value to update the box to
	 * @return true if the value is different from the current setting, false otherwise
	 */
	public boolean updateValue(int value) {
		if(this.value != value) {
			this.value = value;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Pain the InfoBox contents on the given Graphics.
	 * @param g the Graphics
	 */
	protected void paintBoxContents(Graphics g) {
		g.setColor(this.fgColor);
		
		// find the horizontal center of the box.
		int xCenter = this.x + (this.width / 2);
		
		// write the caption in the set font.
		g.setFont(this.font);
		g.drawString(this.caption, xCenter, this.y, Graphics.TOP | Graphics.HCENTER);
	
		// write the value in the set font.
		g.setFont(this.font);
		g.drawString(Integer.toString(this.value), xCenter, this.y + this.height, Graphics.HCENTER | Graphics.BOTTOM);
	}
}
