package org.tools.jarusage;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

/**
 * 
 * @author Rakesh Shah
 *
 */
public class Application extends JFrame
{
    private Container panel;
    private JLabel selectJar;
    private JTextField selectJarText;
    private JTextField selectFolderText;
    private JButton b1;
    private JButton b2;
    private JButton search;
    private JLabel errorLabel;
    private JTextArea textArea;
    private JList<String> listBox; 
    private GridBagLayout layout = new GridBagLayout();
    
    public Application() 
    {
        setTitle("Search Jar Usage");
        init();
    }
    
    private void init()
    {
        panel = getContentPane();
        panel.setLayout(layout);
        errorLabel = new JLabel();
        JarFileBrowser jarFileBrowser = new JarFileBrowser();
       
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=0;
        c.gridwidth = 3;
        c.fill= GridBagConstraints.HORIZONTAL;
        c.insets= new Insets(6, 6, 0, 0);
        errorLabel.setVisible(false);
        errorLabel.setForeground(Color.red);
        panel.add(errorLabel, c);
        
        //----------------- first row ends ---------------------
        selectJar = new JLabel("Jar File :",SwingConstants.LEFT);
        c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=1;
        c.anchor= GridBagConstraints.LINE_START;
        c.fill= GridBagConstraints.NONE;
        c.insets= new Insets(6, 6, 0, 0);
        panel.add(selectJar, c);
        
        selectJarText = new JTextField();
        selectJarText.setText("C:/workspace/libraries/jars/commons-lang3-3.8.1.jar");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets= new Insets(6, 6, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        selectJarText.setPreferredSize(new Dimension(-1, 26));
        panel.add(selectJarText, c);
        
        b1 = new JButton("Select Jar");
        b1.setMnemonic('j');
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.insets= new Insets(6, 6, 0, 6);
        c.fill= GridBagConstraints.NONE;
        c.anchor= GridBagConstraints.NORTHWEST;
        panel.add(b1, c);
        
        //---------------------- Second Row Ends ------------------
        
        JLabel selectFolder = new JLabel("Add Dependency Jar:",SwingConstants.LEFT);
        c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=2;
        c.fill= GridBagConstraints.NONE;
        c.insets= new Insets(6, 6, 0, 0);
        panel.add(selectFolder, c);
        
        listBox = new JList<String>();
        listBox.setModel(new DefaultListModel<>());
        c = new GridBagConstraints();
        c.gridx=1;
        c.gridy=2;
        c.fill= GridBagConstraints.NONE;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.anchor= GridBagConstraints.LINE_START;
        c.insets= new Insets(6, 6, 0, 0);
        c.weighty = 0.05;
        panel.add(new JScrollPane(listBox), c);
        
        JButton plus = new JButton("+");
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.insets= new Insets(6, 6, 0, 6);
        c.anchor= GridBagConstraints.NORTHWEST;
        c.fill= GridBagConstraints.NONE;
        c.weighty=0.0;
        panel.add(plus, c);
        
        
        JButton minus = new JButton("-");
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 3;
        c.insets= new Insets(6, 6, 0, 6);
        c.anchor= GridBagConstraints.NORTHWEST;
        c.fill= GridBagConstraints.NONE;
        minus.setPreferredSize(plus.getPreferredSize());
        panel.add(minus, c);
        
       //---------------------- Third Row Ends ------------------
        
        JLabel selectJarFolder = new JLabel("Java Source Folder:",SwingConstants.LEFT);
        c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=4;
        c.insets= new Insets(6, 6, 0, 0);
        c.fill= GridBagConstraints.HORIZONTAL;
        panel.add(selectJarFolder,c);
        
        
        selectFolderText = new JTextField();
        selectFolderText.setText("C:/dev-teamsite/main/javautils/src/javautil/sharedutils");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.insets= new Insets(6, 6, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        //selectFolderText.setFont(selectFolderText.getFont().deriveFont(14f));
        selectFolderText.setPreferredSize(new Dimension(-1, 26));
        panel.add(selectFolderText, c);
        
        JButton b3 = new JButton("Source Folder");
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        c.insets= new Insets(6, 6, 0, 6);
        c.fill = GridBagConstraints.NONE;
        c.anchor= GridBagConstraints.NORTHWEST;
        panel.add(b3, c);
        
        //---------------------- Fourth Row Ends ------------------
        
        search = new JButton("Search");
        search.setMnemonic('s');
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.CENTER;
        c.insets= new Insets(6, 6, 0, 0);
        panel.add(search,c);
        
        //--------------------------- 5th row --------------
        
        textArea = new JTextArea();
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets= new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.BOTH;
        
        //textArea.setFont(textArea.getFont().deriveFont(14f)); 
        panel.add(new JScrollPane(textArea), c);
        
        redirectLog();
        
        search.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                 if(!isValidInput())
                 {
                     return;
                 }
                
                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Object[] files= ((DefaultListModel<String>)listBox.getModel()).toArray();
                        JarUsage jarUsage = new JarUsage(selectFolderText.getText(),files,selectJarText.getText());
                        try
                        {
                            jarUsage.search();
                        }
                        catch (ClassNotFoundException | IOException  e1)
                        {
                            e1.printStackTrace();
                        }        
                    }
                }) ;
                t.start();
            }

            private boolean isValidInput()
            {
                boolean isValid = true;
                String s1 = selectJarText.getText();
                String s2 = selectFolderText.getText();
                if(s1 == null || s1.isEmpty())
                {
                    showError("Select jar file");
                    hideError();
                    isValid = false;
                } else if(s2 == null || s2.isEmpty())
                {
                    showError("Select Source folder");        
                    hideError();
                    isValid = false;
                }
                if(isValid) 
                {
                    File jarFile = new File(s1);
                    if(!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
                        showError(jarFile + " invalid jar file");
                        hideError();
                        isValid = false;
                    }
                }
                if(isValid) 
                {
                    File sourceFolder = new File(s2);
                    if(!sourceFolder.exists() || !sourceFolder.isDirectory()) {
                        showError(sourceFolder + " invalid source directory");
                        hideError();
                        isValid = false;
                    }
                }
                return isValid;
            }
            
            private void showError(String errorMessage)
            {
                errorLabel.setText(errorMessage);
                errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                errorLabel.setVisible(true);
            }

            private void hideError()
            {
                SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(6000); 
                        errorLabel.setVisible(false);
                    }catch(InterruptedException ie) {}
                    });
            }
        });
        b1.addActionListener(jarFileBrowser);
        plus.addActionListener(jarFileBrowser);
        minus.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<String> selectedValuesList = listBox.getSelectedValuesList();
                DefaultListModel<String> model = (DefaultListModel<String>) listBox.getModel();
                for(String file: selectedValuesList)
                {
                    model.removeElement(file);
                }
            }
        });
        //b2.addActionListener(new DirectoryBrowser());
        b3.addActionListener(new DirectoryBrowser());
        setSize(new Dimension(1000, 800));
        setVisible(true);//making the frame visible
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        center(this);
    }

    
   private void center(JFrame frame) {
        // get the size of the screen, on systems with multiple displays,
        // the primary display is used
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        // calculate the new location of the window
        int w = frame.getSize().width;
        int h = frame.getSize().height;
 
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        // moves this component to a new location, the top-left corner of
        // the new location is specified by the x and y
        // parameters in the coordinate space of this component's parent
        frame.setLocation(x, y);
    }
 
    private void redirectLog()
    {
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);
    }


    private class JarFileBrowser implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JButton button = (JButton) e.getSource();
            String btnName = button.getText();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(new FileFilter()
            {
                @Override
                public String getDescription()
                {
                    return "*.jar";
                }
                
                @Override
                public boolean accept(File f)
                {
                    return f.getName().endsWith(".jar");
                }
            });
            fileChooser.setDialogTitle("Select Jar file");
            fileChooser.showSaveDialog(Application.this);
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if(selectedFiles != null && selectedFiles.length > 0)
            {
                if(btnName.equals("+"))
                {
                    for(File file: selectedFiles)
                    {
                        DefaultListModel<String> model = (DefaultListModel<String>) listBox.getModel();
                        model.addElement(file.getAbsolutePath());
                    }
                }
                else
                {
                    Application.this.selectJarText.setText(selectedFiles[0].getAbsolutePath());
                }
            }
            
        }
    }
    
    private class DirectoryBrowser implements ActionListener
    {
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Directory");
            fileChooser.showSaveDialog(Application.this);
            File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile != null)
            {
                Application.this.selectFolderText.setText(selectedFile.getAbsolutePath());
            }
            
        }
    }
    
    private static class TextAreaOutputStream extends OutputStream
    {
        private JTextArea textArea;

        public TextAreaOutputStream(JTextArea text) 
        {
            this.textArea = text;
        }
        
        @Override
        public void write(int b) throws IOException
        {
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
    
    
    public static void main(String[] args)
    {
        Application application = new Application();           
    }
}

