package tools;

import java.util.ArrayList;
import java.util.List;

import data.MapNote;
import data.NoteColor;

public class AnnotatedNotesTool {

	private List<MapNote> mapNotes = new ArrayList<>();
	
	//go through every map notes and find if it already exists
	public MapNote findNoteAt(int col, int row) {
		for(MapNote note: mapNotes) {
			if(note.getCol() == col && note.getRow() == row) {
				return note;
			}
		}
		return null;
	}
	
	public void addNote(int col, int row, String text, NoteColor color) {
		mapNotes.add(new MapNote(col, row, text, color));
	}

	public void removeMapNote(MapNote mapNote) {
		mapNotes.remove(mapNote);
	}
	
	public List<MapNote> getMapNotes() {
		return mapNotes;
	}
}
