

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	public static Properties getConfig(){
		return getConfigFromFile("config/config.properties");
	}
	
	public static Properties getConfigFromFile(String filepath) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(filepath);
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}
}
