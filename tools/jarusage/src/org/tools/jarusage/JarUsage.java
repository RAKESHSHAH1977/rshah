package org.tools.jarusage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class searches the usage of classes bundled in the jar to the given
 * source directory.
 * 
 * @author rakeshks
 *
 */
public class JarUsage
{
    
    /**
     * Classloader to load the classes of selected jar. 
     */
    private URLClassLoader classLoader;
    
    /** Java source location */
    private String sourceFile;
    
    /**Jar file */
    private String jar; 
    
    /** To enable debug */
    private boolean isDebug = false;

    private Object[] files;
    
    
    /**
     * Constructor 
     * 
     * @param sourceFile - java source folder
     * @param jarFile - selected jar File.
     */
    public JarUsage(String sourceFile, Object[] files, String jarFile) 
    {
        this.sourceFile = sourceFile;
        this.jar = jarFile;
        this.files = files;
    }

    private void initClassLoader() throws MalformedURLException
    {
        URL[] urls = new URL[1];
        if(files != null && files.length > 0)
        {
            int i = 0;
            urls = new URL[files.length+1]; 
            for(Object url : files)
            {
                urls[i++] = new URL("file:\\"+url);
            }
            urls[i] = new URL("file:\\"+jar);
        }
        else {
            urls[0] = new URL("file:\\"+jar);
        }
        System.out.println("Total No of jars loaded in class loader "+urls.length);
        classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
    }

    /**
     * This is a brute-force search.
     * This method loads the jar in the classloader.  It reads all the jar entries of class. 
     * It looks up the class, in the classloader.It uses reflection to get all the methods from 
     * the class. 
     * 
     * 
     * @throws IOException - throws when jar and source file are not correct.
     * @throws ClassNotFoundException - in case class is not loaded in the classloader.
     */
    
    public void search() throws IOException, ClassNotFoundException
    {
        initClassLoader();
        JarFile jarFile = new JarFile(jar);
        List<JarEntry> entries = getJarEntries(jarFile);
        List<File> files = new ArrayList<File>();
        
        long l1 = System.currentTimeMillis();
        if(isDebug) 
        {
            System.out.println("Collecting Files from Source");
        }
        collectFiles(new File(sourceFile),files);
        
        long l2 = System.currentTimeMillis();
        if(isDebug)
        {
            System.out.println("Total Files are "+files.size() + " Total Time taken  "+ (l2-l1) + " miliseconds");
        }
        System.out.println("Stared Searching...");
        for(File file: files)
        {
            if(isDebug)
            {
                System.out.println("Searching in "+ file.getName());
            }
            List<String> lines = new ArrayList<>();
            try {
                lines.addAll(Files.readAllLines(Paths.get(file.getAbsolutePath())));
            }
            catch (MalformedInputException exception)
            {
                System.out.println(file.getAbsolutePath() + " is skipped.");
                continue;
            }
            for(JarEntry jarEntry : entries)
            {
                if (jarEntry.getName().endsWith(".class"))
                {
                    String className = jarEntry.getName().replace('/', '.');
                    int length = className.length();
                    className = className.substring(0, length - 6);
                    if (!searchPackage(lines,className))
                    {
                        continue;
                    }
                    Class<?> clazz = classLoader.loadClass(className);
                    System.out.println("Usage of class: "+ className + " follows.");
                    searchClassUsage(file, lines, clazz.getSimpleName());
                    System.out.println("Usage of class:"+ className + " ends.");

                    System.out.println("\tMethod usage of class:"+ className + " follows. +++++");
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    Set<String> methods = new HashSet<>();
                    for (Method m : declaredMethods)
                    {
                        String methodName = m.getName();
                        if(methods.add(methodName))
                        {
                            searchMethodUsage(file,lines,m.getModifiers(),methodName,clazz.getSimpleName());
                        }
                    }
                    System.out.println("\tMethod usage of "+ className + " ends. ");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
                }
            }
        }
        jarFile.close();
        System.out.println("Search Completed " + files.size());
    }

    private List<JarEntry> getJarEntries(JarFile jarFile)
    {
        List<JarEntry> jarEntries = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements())
        {
            JarEntry jarEntry = entries.nextElement();
            jarEntries.add(jarEntry);
        }
        return jarEntries;
    }

    private void collectFiles(File file,List<File> files)
    {
        if(file.isFile())
        {
            files.add(file);
        }
        else 
        {
            File[] listFiles = file.listFiles(path -> path.isDirectory()?true:path.getName().endsWith(".java"));
            for(File f: listFiles) 
            {
                collectFiles(f, files);
            }
        }
    }

    private boolean searchPackage(List<String> lines, String className) throws IOException
    {
        boolean found = false;
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext())
        {
            String line = iterator.next();

            if (line.startsWith("import"))
            {
                if (line.contains(className))
                {
                    found = true;
                    break;
                }
            }
            else if(line.contains("class"))
            {
                break;
            }
        }
        return found;
    }

    private void searchMethodUsage(File file, List<String> lines,int modifier,String methodName,String className)
                throws IOException
    {
        methodName = methodName+"(";
        if(Modifier.isStatic(modifier))
        {
            methodName = className+"."+methodName;
        }
        int lineNo = 1;
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext())
        {
            String line = iterator.next();
            if (line.contains(methodName))
            {
                System.out.println("\t"+lineNo + ": \""+ methodName + "\" is found in "+file.getAbsolutePath());
            }
            lineNo++;
        }
    }
    

    private void searchClassUsage(File file, List<String> lines,String className)
                throws IOException
    {
        int lineNo = 1;
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext())
        {
            String line = iterator.next();
            if (line.contains(className))
            {
                System.out.println(lineNo + ": \""+ className + "\" is found in "+file.getAbsolutePath());
            }
            lineNo++;
        }
    }


    /**
     * Testing method.
     * @param args args
     * @throws IOException - exception
     * @throws ClassNotFoundException - exception
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        String sourceFile = "C:\\dev-teamsite\\main\\javautils\\src\\javautil\\sharedutils\\";
        String jarName = "C:\\workspace\\libraries\\jars\\commons-lang3-3.8.1.jar";
       /* JarUsage jarUsage = new JarUsage(sourceFile,jarName);
        jarUsage.search();*/
        
    }
   
}
