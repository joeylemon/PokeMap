package info.pokestops.pokego.main;

public class Bounds {
	
	private double lat1;
	private double lat2;
	private double lng1;
	private double lng2;
	public Bounds(String coord1, String coord2){
		String[] split1 = coord1.split(",");
		lat1 = Double.parseDouble(split1[0]);
		lng1 = Double.parseDouble(split1[1]);
		
		String[] split2 = coord2.split(",");
		lat2 = Double.parseDouble(split2[0]);
		lng2 = Double.parseDouble(split2[1]);
	}
	
	public double getMinLat(){
		double min = lat1;
		if(lat2 < lat1){
			min = lat2;
		}
		return min;
	}
	
	public double getMaxLat(){
		double max = lat1;
		if(lat2 > lat1){
			max = lat2;
		}
		return max;
	}
	
	public double getMinLng(){
		double min = lng1;
		if(lng2 < lng1){
			min = lng2;
		}
		return min;
	}
	
	public double getMaxLng(){
		double max = lng1;
		if(lng2 > lng1){
			max = lng2;
		}
		return max;
	}

}
