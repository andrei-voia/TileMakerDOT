package view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import core.TileObject;
import data.MapRegistry;

public class MapStatistics {
	
	private MapRegistry mapRegistry;
	private Map<String, Integer> objectAssets = new HashMap<>();
	private Map<String, Integer> npcAssets = new HashMap<>();
	
	public MapStatistics(MapRegistry mapRegistry) {
		this.mapRegistry = mapRegistry;
	}

	public void calculateAssetDensity() {
        //refresh the map list for objects and NPCs
        objectAssets.clear();
        npcAssets.clear();
        
        for(TileObject obj: mapRegistry.getAllSortedItems()) {
        	String assetName = obj.getName();
        	//add to objects map count
        	if(obj.isObject()) {
        		objectAssets.put(assetName, objectAssets.getOrDefault(assetName, 0) + 1);
        	}
        	//add to NPCs map count
        	else {
        		npcAssets.put(assetName, npcAssets.getOrDefault(assetName, 0) + 1);
        	}
        }
    }
	
	public List<Entry<String, Integer>> getObjectAssetsList() {
        return getAssetsList(objectAssets);
	}
	
	public List<Entry<String, Integer>> getNpcAssetsList() {
        return getAssetsList(npcAssets);
	}
	
	private List<Entry<String, Integer>> getAssetsList(Map<String, Integer> assets) {
        //sort the map by its values (counts) in descending order (highest density first)
        return assets.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
	}
}
