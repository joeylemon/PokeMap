package info.pokestops.pokego.main;

public class Scan {
	
	private Bounds bounds;
	private String city;
	private PopulationType pop;
	public Scan(Bounds bounds, String city, PopulationType pop){
		this.bounds = bounds;
		this.city = city;
		this.pop = pop;
	}
	
	public Bounds getBounds(){
		return bounds;
	}
	
	public String getCity(){
		return city;
	}
	
	public PopulationType getPopulationType(){
		return pop;
	}

}
