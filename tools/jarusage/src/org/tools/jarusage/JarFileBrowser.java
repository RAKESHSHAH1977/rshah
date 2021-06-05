package org.tools.jarusage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class JarFileBrowser implements ActionListener
{
    private JTextField selectJarText;

    public JarFileBrowser(JTextField selectJarText) {

        this.selectJarText = selectJarText;

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
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
        fileChooser.showSaveDialog(selectJarText.getRootPane());
        File[] selectedFiles = fileChooser.getSelectedFiles();
        if (selectedFiles != null && selectedFiles.length > 0)
        {
            selectJarText.setText(selectedFiles[0].getAbsolutePath());
        }

    }

}
