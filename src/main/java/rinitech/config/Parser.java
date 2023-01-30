package rinitech.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Parser
{
	public static String parse(String s)
	{
		Properties prop = new Properties();
		try(InputStream input = new FileInputStream("app.config")) {
			prop.load(input);
			return prop.getProperty(s);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
