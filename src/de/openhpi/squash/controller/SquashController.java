package de.openhpi.squash.controller;

import java.util.List;
import java.util.ArrayList;

import de.openhpi.squash.common.Display;
import de.openhpi.squash.common.IObserver;
import de.openhpi.squash.model.BallModel;
import de.openhpi.squash.model.BoardModel;
import de.openhpi.squash.view.BallView;
import de.openhpi.squash.view.IDrawable;
import de.openhpi.squash.view.BoardView;

public class SquashController implements IObserver {
	private boolean modelChanged;
	private Display display;
	BoardView boardView;
	BoardModel boardModel;
	BallView ballView;
	BallModel ballModel;
	float frameTimeInSec = 0.0f;

	private List<IDrawable> shapes = new ArrayList<IDrawable>();
	
	public static void setup(Display display) {
		new SquashController(display);
	}

	// setup Views and Models
	private SquashController(Display display){
        this.frameTimeInSec = 1.0f / display.drawFrameRate;

		this.display = display;
		this.display.registerObserver(this);

		this.boardView = new BoardView();
		this.shapes.add(this.boardView);

		this.boardModel = new BoardModel(display.width, display.height);

		this.ballView = new BallView();
		this.shapes.add(this.ballView);
		
		this.ballModel = new BallModel(display.canvasUnit);
		this.ballModel.setDistancePerSecond(display.canvasUnit*4, display.canvasUnit*2);
	}

	// process messages from View and Model
	@Override
	public void update(String message){
		switch (message){
			case "Display.SetUpReady":
				this.copyModelAttributesToViews();
				this.display.update(this.shapes);
				break;

			case "Display.NextFrame":
			case "Display.MouseClicked":
				this.calculateNextFrameInModels();
				if (this.finalizeNextFrameInModels()){
					this.copyModelAttributesToViews();
					this.display.update(this.shapes);
				}
				break;
		}
	}

	private void calculateNextFrameInModels(){
		this.ballModel.calculateNextFrame(this.frameTimeInSec);
	}

	private boolean finalizeNextFrameInModels(){
		this.modelChanged = false;
		this.modelChanged = this.modelChanged || 
							this.ballModel.finalizeNextFrame();
		return modelChanged;
	}

	private void copyModelAttributesToViews(){
		this.ballView.set(this.ballModel.side, 
						this.ballModel.getPosition().x,
						this.ballModel.getPosition().y);
	}
}