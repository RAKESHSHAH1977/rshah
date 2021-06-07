package org.tools.jarusage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    /** Java source location */
    private String sourceFile;
    
    /**Jar file */
    private String jar; 
    
    /** To enable debug */
    private boolean isDebug = false;

    private static Map<String,Integer> map;
    
    
    /**
     * Constructor 
     * 
     * @param sourceFile - java source folder
     * @param jarFile - selected jar File.
     */
    public JarUsage(String sourceFile, String jarFile) 
    {
        this.sourceFile = sourceFile;
        this.jar = jarFile;
        init();
    }

    private void init()
    {
        map = new HashMap<>();
        map.put("public", Modifier.PUBLIC);
        map.put("static", Modifier.STATIC);
        map.put("abstract", Modifier.ABSTRACT);
        map.put("final", Modifier.FINAL);
        map.put("synchronized", Modifier.SYNCHRONIZED);
        map.put("transient", Modifier.TRANSIENT);
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
    
    public void search() throws IOException
    {
        
        System.out.println("Started Searching..." );
        System.out.println("Java Source Folder:- "+sourceFile);
        System.out.println("Jar File:- "+jar);
        JarFile jarFile = new JarFile(jar);
        List<JarEntry> entries = getJarEntries(jarFile);
        List<File> files = new ArrayList<>();
        long l1 = System.currentTimeMillis();
        System.out.println("Collecting Files from Source - Keep Patience...");
        collectFiles(new File(sourceFile),files);
        long l2 = System.currentTimeMillis();
        System.out.println("\nTotal Files are "+files.size() + " Total Time taken  "+ (l2-l1)+ " miliseconds");
        
        Map<JarEntry,Collection<AttributeWrapper>> cache = new HashMap<>();
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
                    System.out.println("Usage of class: "+ className + " follows.");
                    searchClassUsage(file, lines, className);
                    System.out.println("Usage of class:"+ className + " ends.");
                    System.out.println("\tMethod usage of class:"+ className + " follows. +++++");
                    Set<String> methods = new HashSet<>();
                    if(!cache.containsKey(jarEntry)) {
                          String location = extractJarEntry(jarFile,jarEntry);
                          Collection<AttributeWrapper> attributes = getMethods(location);
                          cache.put(jarEntry, attributes);
                      }
                      Collection<AttributeWrapper> attributes  = cache.get(jarEntry);
                      String clazzName = className.substring(className.lastIndexOf('.')+1);
                      for(AttributeWrapper m : attributes)
                      {
                        String methodName = m.getName();
                        if(methods.add(methodName) && m.isMethod())
                        {
                            searchMethodUsage(file,lines,m.getModifiers(),methodName,clazzName);
                        }
                    }
                    System.out.println("\tMethod usage of "+ className + " ends. ");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
                }
            }
        }
        jarFile.close();
        long l3 = System.currentTimeMillis();
        System.out.println("Search Completed for " + files.size() + " in "+ (l3-l2) + " miliseconds");
        System.out.println("Total time Consumed  " + (l3-l1) + " miliseconds");
    }

    private static Collection<AttributeWrapper> getMethods(String location)
    {
        List<AttributeWrapper> list = new LinkedList<>();
        String javaHome = System.getProperty("java.home");
        try
        {
            Process process = Runtime.getRuntime().exec(new String[] {javaHome+"/bin/javap","-public",location});
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            int i = 0;
            while(line != null) {
                if(i != 0) {
                    AttributeWrapper wrapper = parseStatement(line.toCharArray(), 0, line.length());
                    if(wrapper != null) {
                        list.add(wrapper);
                    }
                }
                line = bufferedReader.readLine();
                i++;
            }
            bufferedReader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Valid modifiers for method and member variable. 
     * 
     * For method 
     *  <li>public
     *  <li>static
     *  <li>final
     *  <li>abstract
     *  <li>synchronized
     *  <li>transient
     *  
     * @param dataArray
     * @param beginToken
     * @param lastToken
     * @return methodName - method name;
     */
    private static AttributeWrapper parseStatement(char[] dataArray, int beginToken, int lastToken)
    {
        int modifiers = 0;
        StringBuilder sb = new StringBuilder();
        boolean isMethod = false;
        for(int i = beginToken,j = 0; i<lastToken; i++,j++) {
            if(dataArray[i] == ' ') {
                String m = sb.toString();
                if(map.containsKey(m))
                    modifiers = modifiers | map.get(sb.toString());
                sb.delete(0, j);
                j = 0;
            } 
            else if(dataArray[i] == '(') {
                isMethod = true;
                break;
            } else {
                sb.append(dataArray[i]);
            }
        }
        AttributeWrapper mWrapper = new AttributeWrapper(sb.toString(), modifiers,isMethod);
        return mWrapper;
    }
    
    
    private static class AttributeWrapper {
        
        private String methodName;
        private int modifiers;
        private boolean isMethod;
        
        public AttributeWrapper(String name,int modifiers,boolean isMethod) {
            this.methodName = name;
            this.modifiers = modifiers;
            this.isMethod = isMethod;
        }
        
        public boolean isMethod()
        {
            return isMethod;
        }
        
        public String getName() 
        {
            return methodName;
        }
        
        public int getModifiers() 
        {
         return modifiers;   
        }
        
        @Override
        public String toString()
        {
            return methodName;
        }
    }

    private static void printToken(String s)
    {
        System.out.println(s);
    }

    private static String extractJarEntry(JarFile jarFile, JarEntry jarEntry)
    {
        int lastIndex = jarEntry.getName().lastIndexOf('/');
        String fileName = jarEntry.getName().substring(lastIndex+1);
        String location = "";
        try(InputStream inputStream = jarFile.getInputStream(jarEntry);)
        {
            FileSystem fileSystem = FileSystems.getDefault();
            
            Path targetPath = fileSystem.getPath(System.getProperty("java.io.tmpdir"),fileName);
            Files.copy(inputStream,targetPath,StandardCopyOption.REPLACE_EXISTING);
            location = targetPath.toString();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return location;
    }

    private static List<JarEntry> getJarEntries(JarFile jarFile)
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

    private static void collectFiles(File file,List<File> files)
    {
        if(files.size() % 100 == 0)
            System.out.print("-");
        if(file.isFile())
        {
            files.add(file);
        }else 
        {
            File[] listFiles = file.listFiles(path -> path.isDirectory()?true:path.getName().endsWith(".java"));
            for(File f: listFiles) 
            {
                collectFiles(f, files);
            }
        }
    }

    private static boolean searchPackage(List<String> lines, String className) throws IOException
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
    
    private static void searchMethodUsage(File file, List<String> lines,int modifier,String methodName,String className)
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
     */
    public static void main(String[] args)
    {
        String sourceFile = "C:\\dev-teamsite\\main\\javautils\\src\\javautil\\sharedutils\\";
        String jarName = "C:\\workspace\\libraries\\jars\\commons-lang3-3.8.1.jar";
        
        if(args.length == 0) {
            printUsage();
        }
        
        if(args.length < 4) {
            
            printUsage();
        } else {
            
            if(args[0].equals("-src.dir")) 
            {
                sourceFile = args[1];
                File file = new File(sourceFile);
                if(!file.exists() || !file.isDirectory()) 
                {
                    System.out.println(args[1] + " is not a valid source directory.");
                    
                }
            } else {
                printUsage();
            }
            if(args[2].equals("-jar.file")) 
            {
                jarName = args[3];
                File file = new File(jarName);
                if(!file.exists() || !file.isFile() || !file.getName().endsWith(".jar")) 
                {
                    System.out.println(args[3] + " is not a valid source directory.");
                }
            } else {
                printUsage();
            }
        }
        
        JarUsage jarUsage = new JarUsage(sourceFile,jarName);
        try
        {
            jarUsage.search();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }

    private static void printUsage()
    {
        System.out.println("Usage\n"
                + "java -jar jarusaeclt.jar -src.dir <java source> -jar.file <jarfile>\n"
                + "Options\n"
                + "\t-src.dir  absolute java source folder\n"
                + "\t-jar.file absolute path of the jar file");
        System.exit(0);
    }
   
}
