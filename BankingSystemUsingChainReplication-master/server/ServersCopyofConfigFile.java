package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class ServersCopyofConfigFile {
	
	private static ServersCopyofConfigFile instance = null;
	
	static ConcurrentHashMap<String, String> serverConfigurationFileHashMap; 
	
	public static synchronized ServersCopyofConfigFile getInstance()
	{
		if(instance == null)
		{
			instance  = new ServersCopyofConfigFile();
			serverConfigurationFileHashMap = new ConcurrentHashMap<String, String>();
		}
		return instance;
	}

	public static ConcurrentHashMap<String, String> getServerConfigDetails(){
		return serverConfigurationFileHashMap;
	}
	
	public static void updateServerHashMap(String key, String value){
		serverConfigurationFileHashMap.put(key, value);
	}
	
	public static ConcurrentHashMap<String, String> readSetupValuesFromServerConfigFile(String fileName){
			
		BufferedReader reader;
		serverConfigurationFileHashMap = new ConcurrentHashMap<String, String>();
		try {
		reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if(line.contains("[")) continue;
			if(line.isEmpty()) continue;
			String[] tokens = line.split("=");
		    serverConfigurationFileHashMap.put(tokens[0], tokens[1]);		    
		}
		reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return serverConfigurationFileHashMap;
	} 
	

}
