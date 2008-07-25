/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tetris;

/**
 *
 * @author gm
 */
public class DropThread extends Thread {
	
	private TetrisMIDlet game;	// the game using this thread
	
	private boolean running;		// is the thread running
	private boolean skipNextTick;	// whether the thread should skip dropping the piece on the next tick
									// this is used after a quick drop so that the new piece doesn't just
									// drop after the remaining period of time in the current tick
	
	/**
	 * Make a new drop thread.
	 * @param game the game midlet
	 */
	public DropThread(TetrisMIDlet game) {
		this.game = game;
		this.running = true;
		this.skipNextTick = true;	// skip the first drop, because ticking is at the start of the loop,
									// and we don't want an immediate drop when the thread is started
	}
	
	/**
	 * Stop the thread.  This will cause this thread to exit its running loop.
	 * Apparently it can't be restarted, and will instead a new instance needs to be used in the future.
	 */
	public void stopThread() {
		this.running = false;
	}
	
	/**
	 * Skip the next tick, no dropping action will be taken at the next tick.
	 */
	public void skipNextTick() {
		this.skipNextTick = true;
	}
	
	/**
	 * The drop running loop.
	 */
	public void run() {
	    while(this.running) {			
			// drop the active piece
			if(this.skipNextTick) {
				this.skipNextTick = false;
			} else {
				this.game.tick();
			}
			
			try {
				Thread.sleep(this.game.getTickSpeed());	// sleep between tick
			} catch(InterruptedException ie) {
			    // who's interrupting us? just drop out of running
			}
		}
	}
}

