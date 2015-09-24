package master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;


public class MastersCopyofConfigFile {
	
	private static MastersCopyofConfigFile instance = null;
	static HashMap<String, String> masterConfigurationFileHashMap; 
	
	public static synchronized MastersCopyofConfigFile getInstance()
	{
		if(instance == null)
		{
			instance  = new MastersCopyofConfigFile();
			masterConfigurationFileHashMap = new HashMap<String, String>();
		}
		return instance;
	}

	public HashMap<String, String> getMasterConfigDetails(){
		return masterConfigurationFileHashMap;
	}
	
	public void updateMasterHashMap(String key, String value){
		masterConfigurationFileHashMap.put(key, value);
	}
	
	public HashMap<String, String> readSetupValuesFromMasterConfigFile(String fileName){
			
		BufferedReader reader;
		masterConfigurationFileHashMap = new HashMap<String, String>();
		try {
		reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if(line.contains("[")) continue;
			if(line.isEmpty()) continue;
			String[] tokens = line.split("=");
		    masterConfigurationFileHashMap.put(tokens[0], tokens[1]);		    
		}
		reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return masterConfigurationFileHashMap;
	} 
	

}
