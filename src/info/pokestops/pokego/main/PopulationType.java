package info.pokestops.pokego.main;

public enum PopulationType {
	
	URBAN(3, 1),
	SUBURBAN(12, 6),
	RURAL(20, 10);
	
	private int maxNoLocs;
	private int refreshLocs;
	PopulationType(int maxNoLocs, int refreshLocs){
		this.maxNoLocs = maxNoLocs;
		this.refreshLocs = refreshLocs;
	}
	
	public int getMaxNoLocations(){
		return maxNoLocs;
	}
	
	public int getRefreshLocations(){
		return refreshLocs;
	}
	
	public static final PopulationType getType(int value){
		PopulationType type = null;
		int current = 1;
		for(PopulationType t : PopulationType.values()){
			if(value == current){
				type = t;
				break;
			}else{
				current++;
			}
		}
		return type;
	}

}
