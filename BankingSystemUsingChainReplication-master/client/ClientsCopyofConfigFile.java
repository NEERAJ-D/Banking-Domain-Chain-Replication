package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;


public class ClientsCopyofConfigFile {
	
	private static ClientsCopyofConfigFile instance = null;
	static HashMap<String, String> clientConfigurationFileHashMap; 
	
	public static synchronized ClientsCopyofConfigFile getInstance()
	{
		if(instance == null)
		{
			instance  = new ClientsCopyofConfigFile();
			clientConfigurationFileHashMap = new HashMap<String, String>();
		}
		return instance;
	}

	public HashMap<String, String> getClientConfigDetails(){
		return clientConfigurationFileHashMap;
	}
	
	public void updateClientHashMap(String key, String value){
		clientConfigurationFileHashMap.put(key, value);
	}
	
	public HashMap<String, String> readSetupValuesFromClientConfigFile(String fileName){
			
		BufferedReader reader;
		clientConfigurationFileHashMap = new HashMap<String, String>();
		try {
		reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if(line.contains("[")) continue;
			if(line.isEmpty()) continue;
			String[] tokens = line.split("=");
		    clientConfigurationFileHashMap.put(tokens[0], tokens[1]);		    
		}
		reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clientConfigurationFileHashMap;
	} 
	

}
