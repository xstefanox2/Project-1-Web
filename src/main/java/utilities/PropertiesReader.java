package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
	
	private static PropertiesReader instance;
	private Properties props = new Properties();
	
	private PropertiesReader() {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")){
			props.load(input);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getValue(String key) {
		return props.getProperty(key);
	}
	
	public static PropertiesReader getInstance() {
		if (instance == null) {
			instance = new PropertiesReader();
		}
		return instance;
	}
}