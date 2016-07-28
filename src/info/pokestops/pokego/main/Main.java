package info.pokestops.pokego.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import POGOProtos.Map.Fort.FortDataOuterClass.FortData;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

public class Main {
	
	
	private static int alt = 0;
	private static int width = 9;
	private static int maxNoLocs = 10;
	private static int refreshLocs = 3;
	private static double coordConst = 0.021;
	
	private static String city = "Default";
	private static String dir = "C:/Users/***/Desktop/Locations/";
	
	private static PokemonGo go;
	public static String refresh_token;
	private static OkHttpClient httpClient;
	
	private static List<Pokestop> stops = new ArrayList<Pokestop>();
	private static List<FortData> gyms = new ArrayList<FortData>();
	private static List<String> log = new ArrayList<String>();
	
	public static final void main(String[] args) throws LoginFailedException, RemoteServerException, InterruptedException{
		log("Logging into account...");
		httpClient = new OkHttpClient();
		try{
			go = new PokemonGo(new PtcCredentialProvider(httpClient, "***", "***"), httpClient);
		}catch (LoginFailedException ex){
			catchException(ex);
		}catch (RemoteServerException ex) {
			catchException(ex);
		}
		
		List<Scan> scans = new ArrayList<Scan>();
		scans.add(new Scan(new Bounds("52.52697915988631,13.195610046386719", "52.50613909894948,13.45301628112793"), "Berlin", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("40.44955971899028,-3.6002540588378906", "40.47555014671345,-3.7789535522460938"), "Madrid", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("44.41416430998939,26.013050079345703", "44.376140479200124,26.103858947753906"), "Bucharest", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("48.21094727794909,16.292381286621094", "48.22856083588024,16.37460708618164"), "Vienna", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("53.55632008870665,9.955329895019531", "53.50908051674147,10.036182403564453"), "Hamburg", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("47.50850487150151,18.96514892578125", "47.48345284065516,19.238948822021484"), "Budapest", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("52.489679436549324,-1.8962574005126953", "52.465213701916504,-1.8106842041015625"), "Birmingham UK", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("50.92346159685735,6.986403465270996", "50.90151722768345,6.901988983154297"), "Cologne", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("40.87886710569574,14.22429084777832", "40.85692857386187,14.318962097167969"), "Naples", PopulationType.URBAN));
		
		scans.add(new Scan(new Bounds("44.99879594361408,7.5208282470703125", "45.110361752910514,7.761325836181641"), "Turin", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("43.36612409315011,5.3009033203125", "43.23394781150972,5.4512786865234375"), "Marseille", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("52.4275475424737,4.77630615234375", "52.28811257899827,5.004615783691406"), "Amsterdam", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("45.848651708793966,15.852584838867188", "45.75243291804482,16.09222412109375"), "Zagreb", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("39.538469286383034,-0.48168182373046875", "39.38765194764911,-0.3179168701171875"), "Valencia", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("35.49981018711523,139.57717895507812", "35.8217105820067,139.99740600585938"), "Tokyo", PopulationType.URBAN));
		scans.add(new Scan(new Bounds("40.637664715596856,-74.08561706542969", "40.87821814104651,-73.78520965576172"), "New York", PopulationType.URBAN));
		scanList(scans);
	}
	
	public static final void scanList(List<Scan> scans){
		int count = 0;
		for(Scan scan : scans){
			scan(scan);
			count++;
			if(count < scans.size()){
				log("Scanning next city... sleeping 60 seconds.");
				Main.delay(60000);
			}
		}
	}
	
	public static final void scan(Scan scan){
		Bounds bounds = scan.getBounds();
		String city = scan.getCity();
		PopulationType type = scan.getPopulationType();
		
		log("Scanning " + city);
		
		Main.city = city;
		Main.dir = "C:/Users/Joey/Desktop/Locations/" + city + "/";
		maxNoLocs = type.getMaxNoLocations();
		refreshLocs = type.getRefreshLocations();
		
		double minLat = bounds.getMinLat();
		double maxLat = bounds.getMaxLat();
		double minLng = bounds.getMinLng();
		double maxLng = bounds.getMaxLng();
		
		int nolocs = 0;
		
		int count = 1;
		int total = calculateLoops(minLat, maxLat, minLng, maxLng);
		for(double x = minLat; x <= maxLat; x += coordConst){
			for(double z = minLng; z <= maxLng; z += coordConst){
				int locs = 0;
				try{
					go.setLocation(x, z, alt);
					
					log("Getting nearby locations...");
					
					Map map = go.getMap();
					map.setUseCache(false);
					
					log("CHECKING (" + count + "/" + total +"): " + go.getLatitude() + ", " + go.getLongitude());
					
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
					
					count++;
					if((count % 25) == 0){
						dumpSearch();
						Main.delay(3000);
					}else if((count % 150) == 0){
						log("Refreshing. Sleeping for 5 seconds...");
						Main.delay(5000);
					}else{
						Main.delay(900);
					}
					
					if(locs == 0){
						nolocs++;
					}else{
						nolocs = 0;
					}
					if(nolocs < maxNoLocs && nolocs > 0 && (nolocs % refreshLocs) == 0){
						log("Sleeping for 15 seconds...");
						Main.delay(15000);
					}else if(nolocs > maxNoLocs){
						log("Sleeping for 120 seconds...");
						nolocs = 0;
						Main.delay(120000);
						createNewClient();
					}
				}catch (Exception ex){
					catchException(ex);
				}
			}
		}
		dumpSearch();
		stops.clear();
		gyms.clear();
	}
	
	public static final void delay(long time){
		try{
			Thread.sleep(time);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static final void catchException(Exception ex){
		ex.printStackTrace();
		dumpSearch();
		log("\nError discovered, retry in 30 seconds.");
		Main.delay(30000);
		createNewClient();
	}
	
	public static final void createNewClient(){
		try{
			log("\nCreate new HTTP Client");
			httpClient = new OkHttpClient();
			go = new PokemonGo(new PtcCredentialProvider(httpClient, "***", "***"), httpClient);
		}catch (Exception ex){
			catchException(ex);
		}
	}
	
	public static final void dumpSearch(){
		try{
			log("\n\nFound " + (stops.size() + gyms.size()) + " locations (" + stops.size() + " stops, " + gyms.size() + " gyms)\n");
			
			List<String> stopCoords = new ArrayList<String>();
			List<String> gymCoords = new ArrayList<String>();
			
			for(Pokestop s : stops){
				String id = s.getId();
				
				stopCoords.add(s.getLatitude() + "->" + s.getLongitude() + "->" + id);
			}
			for(FortData g : gyms){
				gymCoords.add(g.getLatitude() + ", " + g.getLongitude());
			}
			
			log("Locations dumped to " + dir);
			int existingStops = appendToFile(LocationType.POKESTOP, stopCoords);
			int existingGyms = appendToFile(LocationType.GYM, gymCoords);
			log((existingStops + existingGyms) + " coordinates already existed and were not added (" + existingStops + " stops, " + existingGyms + " gyms)\n");
			
			log("Dumping log\n");
			dumpLog();
		}catch (Exception ex){
			catchException(ex);
		}
	}
	
	public static final int appendToFile(LocationType type, List<String> coords){
		int exists = 0;
		try{
			String filename = city + "-" + type.toString().toLowerCase() + "s.txt";
			
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
		}catch (IOException ex){
			ex.printStackTrace();
		}
		return exists;
	}
	
	public static final void log(String message){
		String fullmsg = "[" + getTime() + "] " +  message;
		System.out.println(fullmsg);
		log.add(fullmsg);
	}
	
	public static final void dumpLog(){
		try{
			String filename = city + " log.txt";
			
			File cityDir = new File(dir);
			if(!cityDir.exists()){
				cityDir.mkdir();
			}
			
			File file = new File(dir + filename);

			if(file.exists()){
				file.delete();
			}
			file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(String s : log){
				bw.write(s);
				bw.newLine();
			}
			
			bw.close();
			log.clear();
		}catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public static final String getTime(){
		long millis = System.currentTimeMillis();
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		
		hour -= 4;
		if(hour < 0){
			hour = 12 + hour;
		}
		if(hour > 12){
			hour -= 12;
		}

		return String.format("%02d:%02d:%02d", hour, minute, second);
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
			log("Found stop at " + s.getLatitude() + ", " + s.getLongitude());
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
			log("Found gym at " + g.getLatitude() + ", " + g.getLongitude());
			gyms.add(g);
		}
	}

}
