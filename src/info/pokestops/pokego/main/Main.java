package info.pokestops.pokego.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import POGOProtos.Map.Fort.FortDataOuterClass.FortData;
import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.auth.PtcLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

public class Main {
	
	private static int alt = 0;
	private static int width = 9;
	private static int maxErrors = 10;
	private static int maxNoLocs = 10;
	private static int refreshLocs = 3;
	private static double coordConst = 0.017;
	
	private static String dir = "C:/Users/User/Desktop/Locations/";
	
	private static List<Pokestop> stops = new ArrayList<Pokestop>();
	private static List<FortData> gyms = new ArrayList<FortData>();
	
	public static final void main(String[] args) throws LoginFailedException, RemoteServerException, InterruptedException{
		scan(new Bounds("42.46424606945876,-82.8812026977539", "42.23995623127169,-83.28907012939453"), "Detroit", PopulationType.URBAN);
	}
	
	public static final void scan(Bounds bounds, String city, PopulationType type) throws InterruptedException, LoginFailedException{
		System.out.println("Scanning " + city);
		
		Main.dir = "C:/Users/Joey/Desktop/Locations/" + city + "/";
		maxNoLocs = type.getMaxNoLocations();
		refreshLocs = type.getRefreshLocations();
		
		System.out.println("Logging into account...");
		OkHttpClient httpClient = new OkHttpClient();
		AuthInfo auth = new PtcLogin(httpClient).login("user", "pass");
		
		double minLat = bounds.getMinLat();
		double maxLat = bounds.getMaxLat();
		double minLng = bounds.getMinLng();
		double maxLng = bounds.getMaxLng();
		
		int errors = 0;
		int nolocs = 0;
		
		int scan = 1;
		int total = calculateLoops(minLat, maxLat, minLng, maxLng);
		for(double x = minLat; x <= maxLat; x += coordConst){
			for(double z = minLng; z <= maxLng; z += coordConst){
				int locs = 0;
				try{
					PokemonGo go = new PokemonGo(auth,httpClient);
					go.setLocation(x, z, alt);
					
					System.out.println("Getting nearby locations...");
					
					Map map = go.getMap();
					map.setUseCache(false);
					
					System.out.println("CHECKING (" + scan + "/" + total +"): " + go.getLatitude() + ", " + go.getLongitude());
					
					MapObjects objs = map.getMapObjects(width);
					for(Iterator<Pokestop> iterator = objs.getPokestops().iterator(); iterator.hasNext();){
						Pokestop stop = iterator.next();
						addNewPokestop(stop);
						locs++;
					}
					
					for(Iterator<FortData> iterator = objs.getGyms().iterator(); iterator.hasNext();){
						FortData gym = iterator.next();
						addNewGym(gym);
						locs++;
					}
					
					scan++;
					if((scan % 80) == 0){
						dumpSearch();
						Thread.sleep(3000);
					}else if((scan % 150) == 0){
						System.out.println("Sleeping for 3 seconds...");
						Thread.sleep(3000);
						System.out.println("\nCreate new HTTP Client");
						httpClient = new OkHttpClient();
					}else{
						Thread.sleep(900);
					}
					
					if(locs == 0){
						nolocs++;
					}else{
						nolocs = 0;
					}
					if(nolocs < maxNoLocs && nolocs > 0 && (nolocs % refreshLocs) == 0){
						System.out.println("Sleeping for 15 seconds...");
						Thread.sleep(15000);
						System.out.println("\nCreate new HTTP Client");
						httpClient = new OkHttpClient();
					}else if(nolocs > maxNoLocs){
						System.out.println("Sleeping for 60 seconds...");
						nolocs = 0;
						Thread.sleep(60000);
						System.out.println("\nCreate new HTTP Client");
						httpClient = new OkHttpClient();
					}
				}catch (Exception ex){
					ex.printStackTrace();
					if(errors < maxErrors){
						errors++;
						System.out.println("\nError discovered, retry in 10 seconds.");
						Thread.sleep(10000);
						System.out.println("\nCreate new HTTP Client");
						httpClient = new OkHttpClient();
					}else{
						dumpSearch();
						System.out.println("\nMax errors reached, end task.");
						break;
					}
				}
			}
		}
		dumpSearch();
		stops.clear();
		gyms.clear();
		System.out.println("Scanning next city... wait 120 seconds");
		Thread.sleep(120000);
	}
	
	public static final void dumpSearch(){
		System.out.println("\n\nFound " + (stops.size() + gyms.size()) + " locations.\n");
		
		List<String> stopCoords = new ArrayList<String>();
		List<String> gymCoords = new ArrayList<String>();
		
		for(Pokestop s : stops){
			stopCoords.add(s.getLatitude() + ", " + s.getLongitude());
		}
		for(FortData g : gyms){
			gymCoords.add(g.getLatitude() + ", " + g.getLongitude());
		}
		
		System.out.println("Pokestops dumped to Desktop/pokestops.txt");
		System.out.println("Gyms dumped to Desktop/gyms.txt");
		appendToFile(LocationType.POKESTOP, stopCoords);
		appendToFile(LocationType.GYM, gymCoords);
	}
	
	public static final void appendToFile(LocationType type, List<String> coords){
		try{
			String filename = type.toString().toLowerCase() + "s.txt";
			
			File cityDir = new File(dir);
			if(!cityDir.exists()){
				cityDir.mkdir();
			}
			
			List<String> lines = new ArrayList<String>();
			
			File file = new File(dir + filename);

			if(!file.exists()){
				file.createNewFile();
			}else{
				lines = Files.readAllLines(FileSystems.getDefault().getPath(dir, filename));
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			int exists = 0;
			for(String c : coords){
				String line = c + "|";
				if(!lines.contains(line)){
					bw.write(line);
					bw.newLine();
				}else{
					exists++;
				}
			}
			
			bw.close();
			
			System.out.println(exists + " coordinates already existed and were not added.");
		}catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public static void log(String message) {
		try{
			PrintWriter out = new PrintWriter(new FileWriter(dir + "log.txt", true), true);
			out.write(message);
			out.close();
		}catch (IOException ex){
			
		}
	}
	
	public static final int calculateLoops(double minLat, double maxLat, double minLng, double maxLng){
		int total = 0;
		for(double x = minLat; x <= maxLat; x += coordConst){
			for(double z = minLng; z <= maxLng; z += coordConst){
				total++;
			}
		}
		return total;
	}
	
	public static final void addNewPokestop(Pokestop s){
		boolean add = true;
		for(Pokestop ps : stops){
			if(ps.getLatitude() == s.getLatitude() && ps.getLongitude() == s.getLongitude()){
				add = false;
			}
		}
		if(add){
			System.out.println("Found stop at " + s.getLatitude() + ", " + s.getLongitude());
			stops.add(s);
		}
	}
	
	public static final void addNewGym(FortData g){
		boolean add = true;
		for(FortData fd : gyms){
			if(fd.getLatitude() == g.getLatitude() && fd.getLongitude() == g.getLongitude()){
				add = false;
			}
		}
		if(add){
			System.out.println("Found gym at " + g.getLatitude() + ", " + g.getLongitude());
			gyms.add(g);
		}
	}

}
