package de.openhpi.squash.controller;

import java.util.List;
import java.util.ArrayList;

import de.openhpi.squash.common.Display;
import de.openhpi.squash.common.IObserver;
import de.openhpi.squash.model.MovableRectangle;
import de.openhpi.squash.model.BoardModel;
import de.openhpi.squash.model.IMovableRectangle;
import de.openhpi.squash.model.IPositionableRectangle;
import de.openhpi.squash.model.Point;
import de.openhpi.squash.model.Speed;
import de.openhpi.squash.view.RectangleView;
import de.openhpi.squash.view.IDrawable;
import de.openhpi.squash.view.BoardView;

public class SquashController implements IObserver {
	private boolean modelChanged;
	private Display display;
	BoardView boardView;
	BoardModel boardModel;
	RectangleView ballView;
	MovableRectangle ballModel;
	float frameTimeInSec = 0.0f;
	private Speed movSpeed;    // reused by checkCollison()
	private Point movNewPos; // reused by checkCollison()

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

		this.ballView = new RectangleView();
		this.shapes.add(this.ballView);
		
		this.ballModel = new MovableRectangle(display.canvasUnit,
												display.canvasUnit,
												0,0,
												display.canvasUnit*4, 
												display.canvasUnit*2);
	}

	// process messages from Display
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
				this.processCollisonsInModels();
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

	private void processCollisonsInModels(){
		checkCollisonMovableInsideFixed(this.ballModel,this.boardModel);
	}

	private void checkCollisonMovableInsideFixed(IMovableRectangle movable, 
												IPositionableRectangle fixed) {
		movSpeed = movable.getDistancePerSecond();
		movNewPos = movable.getNewPosition();
		if (movSpeed.x>0 && (movable.top.isIntersectingWith(fixed.right)
						|| movable.bottom.isIntersectingWith(fixed.right))){
			movable.changeDistancePerSecond(-1, 1);
			movable.setNewPosition(movNewPos.x-(movable.right.pointA.x-fixed.right.pointA.x),
									movNewPos.y);
		}
		else if (movSpeed.x<0 && (movable.top.isIntersectingWith(fixed.left)
		 					|| movable.bottom.isIntersectingWith(fixed.left))) {
			movable.changeDistancePerSecond(-1, 1);
			movable.setNewPosition(fixed.left.pointA.x+(fixed.left.pointA.x-movNewPos.x),
									movNewPos.y);
		}

		if (movSpeed.y>0 && (movable.left.isIntersectingWith(fixed.bottom)
						|| movable.right.isIntersectingWith(fixed.bottom))){
			movable.changeDistancePerSecond(1, -1);
			movable.setNewPosition(movNewPos.x,
									movNewPos.y-(movable.bottom.pointA.y-fixed.bottom.pointA.y));
		}
		else if (movSpeed.y<0 && (movable.left.isIntersectingWith(fixed.top)
							|| movable.right.isIntersectingWith(fixed.top))){
			movable.changeDistancePerSecond(1, -1);
			movable.setNewPosition(movNewPos.x,
								fixed.top.pointA.y+(fixed.top.pointA.y-movNewPos.y));
		}
	}

	private void checkCollisonMovableOutsideFixed(IMovableRectangle movable, 
													IPositionableRectangle fixed) {
		movSpeed = movable.getDistancePerSecond();
		movNewPos = movable.getNewPosition();
		if (movSpeed.x>0 && (movable.top.isIntersectingWith(fixed.left)
						|| movable.bottom.isIntersectingWith(fixed.left))){
			movable.changeDistancePerSecond(-1, 1);
			movable.setNewPosition(movNewPos.x-(movable.right.pointA.x-fixed.left.pointA.x),
									movNewPos.y);
		}
		else if (movSpeed.x<0 && (movable.top.isIntersectingWith(fixed.right)
		 					|| movable.bottom.isIntersectingWith(fixed.right))) {
			movable.changeDistancePerSecond(-1, 1);
			movable.setNewPosition(fixed.right.pointA.x+(fixed.right.pointA.x-movNewPos.x),
									movNewPos.y);
		}

		if (movSpeed.y>0 && (movable.left.isIntersectingWith(fixed.top)
						|| movable.right.isIntersectingWith(fixed.top))){
			movable.changeDistancePerSecond(1, -1);
			movable.setNewPosition(movNewPos.x,
									movNewPos.y-(movable.bottom.pointA.y-fixed.top.pointA.y));
		}
		else if (movSpeed.y<0 && (movable.left.isIntersectingWith(fixed.bottom)
							|| movable.right.isIntersectingWith(fixed.bottom))){
			movable.changeDistancePerSecond(1, -1);
			movable.setNewPosition(movNewPos.x,
								fixed.bottom.pointA.y+(fixed.bottom.pointA.y-movNewPos.y));
		}
	}

	private boolean finalizeNextFrameInModels(){
		this.modelChanged = false;
		this.modelChanged = this.modelChanged || 
							this.ballModel.finalizeNextFrame();
		return modelChanged;
	}

	private void copyModelAttributesToViews(){
		this.ballView.set(this.ballModel.width, 
						  this.ballModel.height,
						this.ballModel.getNewPosition().x,
						this.ballModel.getNewPosition().y);
	}
}