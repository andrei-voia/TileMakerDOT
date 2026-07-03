package data;

import core.ItemType;

public class Selection {
	
	private ItemType type;
    private int index;

    public Selection(ItemType type, int index) {
        this.type = type;
        this.index = index;
    }
    
    @Override
    public String toString() {
    	return type + " / " + index;
    }
    
    public void set(ItemType type, int index) {
    	this.type = type;
    	this.index = index;
    }

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setEraseMode() {
		this.type = ItemType.ERASE_MODE;
		this.index = -2;
	}
	
	public void setNotesTool() {
		this.type = ItemType.NOTES_TOOL;
		this.index = -1;
	}
	
	public void setNpcWalkAreaMode() {
		this.type = ItemType.NPC_WALK_AREA_MODE;
		this.index = 1;
	}
	
	public void setBrushTool() {
		this.type = ItemType.BRUSH_TOOL;
		this.index = -1;
	}
	
	public void setChunkSelectionTool() {
		this.type = ItemType.CHUNK_SELECTION_TOOL;
		this.index = -1;
	}
	
	public void setNone() {
		this.type = ItemType.NONE;
		this.index = -1;
	}
	
	public boolean isTileMode() {
		return this.type == ItemType.TILE;
	}
	
	public boolean isObjectMode() {
		return this.type == ItemType.OBJECT;
	}
	
	public boolean isNpcMode() {
		return this.type == ItemType.NPC;
	}
	
	public boolean isEraseMode() {
		return this.type == ItemType.ERASE_MODE;
	}
	
	public boolean isNotesTool() {
		return this.type == ItemType.NOTES_TOOL;
	}
	
	public boolean isNpcWalkAreaMode() {
		return this.type == ItemType.NPC_WALK_AREA_MODE;
	}
	
	public boolean isBrushTool() {
		return this.type == ItemType.BRUSH_TOOL;
	}
	
	public boolean isChunkSelectionTool() {
		return this.type == ItemType.CHUNK_SELECTION_TOOL;
	}
}
