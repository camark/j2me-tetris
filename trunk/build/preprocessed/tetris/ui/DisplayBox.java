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

import javax.microedition.lcdui.Graphics;

/**
 * A simple box that can paint itself on the screen.  Has a one-pixel border.
 * Mainly a base class to extend off of.
 */
public class DisplayBox {

	protected int x;		// x coordinate of upper left of the box
	protected int y;		// y coordinate of upper left of the box
	protected int width;	// width of the box (pixels)
	protected int height;	// height of the box (pixels);
	
	protected int fgColor;	// foreground color, used in this class just for border
	protected int bgColor;	// background color
	
	/**
	 * Make a new box.
	 * 
	 * @param x x coordinate of upper left of the box
	 * @param y y coordinate of upper left of the box
	 * @param width width of the box (pixels)
	 * @param height height of the box (pixels);
	 * @param fgColor foreground color, used in this class just for border
	 * @param bgColor background color
	 */
	public DisplayBox(int x, int y, int width, int height,
					  int fgColor, int bgColor) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		this.fgColor = fgColor;
		this.bgColor = bgColor;
	}
	
	/**
	 * Paint this box on the given Graphics object.
	 * @param g the Graphics on which to paint.
	 */
	public final void paint(Graphics g) {
		g.setColor(this.bgColor);
		g.fillRect(x, y, width, height);	// the background of the box
		g.setColor(this.fgColor);
		g.drawRect(x, y, width, height);	// the border
		
		this.paintBoxContents(g);
	}
	
	protected void paintBoxContents(Graphics g) {}
}
