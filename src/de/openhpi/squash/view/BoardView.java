package de.openhpi.squash.view;

import de.openhpi.squash.common.Display;

public class BoardView extends Drawable{

	public void set(){

	}
	
    public void draw(Display display){
        display.background(display.color(display.backgroundColor));
    }
}
