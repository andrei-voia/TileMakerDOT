package data;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JOptionPane;

import localization.LocalizationManager;

public class HistoryData {

	private static int MAX_UNDO_STATES = 20;
	
	//history undo / redo actions inside tool
	private Deque<MapData> undoStack = new ArrayDeque<>();
	private Deque<MapData> redoStack = new ArrayDeque<>();
	
	protected MapState mapState;
	
	public HistoryData(MapState mapState) {
		this.mapState = mapState;
	}
	
	public void saveState() {
		//clear redo stack on any new action
		redoStack.clear();
		
		try {
			//deep copy the current state
			MapData state = new MapData(mapState.getData());
	
			//if max states are reached then always remove the oldest ones
			if (undoStack.size() > 0 && undoStack.size() >= MAX_UNDO_STATES) {
				undoStack.removeLast();
			}
	
			undoStack.push(state);
			
		} catch (OutOfMemoryError e) {
			//in case the memory ran out very early in the stack then set it to 0 because system is very low on RAM
			if(undoStack.size() <= 3) {
				MAX_UNDO_STATES = 0;
			}
			//cut the max undo states to half to save memory for the tool
			else {
				MAX_UNDO_STATES = (int) (undoStack.size() * 0.5f);
			}
			
	        //clear the rest of the undo history to free up the RAM
	        undoStack.clear();
			redoStack.clear();
			
	        //force the JVM to run garbage collection
	        System.gc();
	        
	        //inform the user about the RAM usage and how it was solved
	        JOptionPane.showMessageDialog(
	            null,
	            LocalizationManager.getInstance().getFormattedString("dialog_memory_warning_message", MAX_UNDO_STATES),
	            LocalizationManager.getInstance().getString("dialog_memory_warning"),
	            JOptionPane.ERROR_MESSAGE
	        );
	    }
	}

	public void resetSaveState() {
		undoStack.clear();
		redoStack.clear();
	}

	public void undo() {
		if (undoStack.size() > 1) {
			MapData currentState = undoStack.pop();
			redoStack.push(currentState);

			MapData previousState = undoStack.peek();
			mapState.getData().applyState(previousState);
		}
	}

	public void redo() {
		if (!redoStack.isEmpty()) {
			MapData nextState = redoStack.pop();
			undoStack.push(nextState);
			mapState.getData().applyState(nextState);
		}
	}
}
