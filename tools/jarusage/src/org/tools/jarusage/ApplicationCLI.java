package org.tools.jarusage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationCLI 
{

	public static void main(String[] args) 
	{
		
		Properties prop = new Properties();
		try (InputStream input = new FileInputStream(args[0])) 
		{
            prop.load(input);

        } catch (IOException ex) {
            System.out.println("Error reading properties file");
            System.exit(0);
        }

		validate(prop);
		
		String[] dependantJarArray = getDepJarList(prop);
		
		JarUsage jarUsage = new JarUsage(prop.getProperty("codeFolder"),dependantJarArray,prop.getProperty("jarFile"));
        try
        {
            jarUsage.search();
        }
        catch (ClassNotFoundException | IOException  e1)
        {
            e1.printStackTrace();
        }   
		

	}

	//TODO. Have a util between swing and CLI app. Leaving it out for now.
	private static void validate(Properties prop) {
		
		
	}

	private static String[] getDepJarList(Properties prop) {
		String commaSeperatedDepJars = prop.getProperty("dependantJars");
		String[] dependantJarArray = null;
		if(null != commaSeperatedDepJars && !commaSeperatedDepJars.isEmpty()) {
			dependantJarArray = commaSeperatedDepJars.split(",");
		}
		return dependantJarArray;
	}

}
