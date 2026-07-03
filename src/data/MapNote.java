package data;

public class MapNote {
	
	private int col;
    private int row;
    private String text;
    private NoteColor color;

    public MapNote(int col, int row, String text, NoteColor color) {
        this.col = col;
        this.row = row;
        this.text = text;
        this.color = color;
    }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public NoteColor getColor() {
		return color;
	}

	public void setColor(NoteColor color) {
		this.color = color;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}
}
