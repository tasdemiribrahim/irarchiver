package Gui;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import Common.MainVocabulary;
import java.awt.Frame;
import javax.swing.plaf.metal.MetalTheme;

public class FrameOperations implements MainVocabulary
{
    static final String className = Main.class.getName();
    
    protected boolean isAvailableLookAndFeel(String laf) 
    {
         try 
         { 
             Class lnfClass = Class.forName(laf);
             LookAndFeel newLAF = (LookAndFeel)(lnfClass.newInstance());
             return newLAF.isSupportedLookAndFeel();
         } 
         catch(Exception e) 
         {
             return false;
         }
     }
    
    protected void repaintTheme(String newTheme) throws Exception
    { 
        try
        {
           Class lnfClass = Class.forName(newTheme);
           MetalLookAndFeel.setCurrentTheme((MetalTheme)lnfClass.newInstance());
           repaintGUI(props.getProperty("style"));
        }
        catch (Exception ex) 
        { 
            trayIcon.setToolTip("repaint theme error");
            throw new Exception("repaint theme error " + "at " + className + newline + ex.getMessage());
        } 
    }
    
    protected void repaintGUI(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, Exception 
    {
        try 
        {
           UIManager.setLookAndFeel(lookAndFeel);
        } 
        catch (ClassNotFoundException ex) 
        {
            throw new ClassNotFoundException(ex.getMessage());
        }
        catch (InstantiationException ex) 
        {
            throw new InstantiationException(ex.getMessage());
        } 
        catch (IllegalAccessException ex) 
        {
            throw new IllegalAccessException(ex.getMessage());
        } 
        catch (UnsupportedLookAndFeelException ex) 
        {
            throw new UnsupportedLookAndFeelException(ex.getMessage());
        }
        for(int i=0;i<Gui.Main.getFramesLenght();i++)
            SwingUtilities.updateComponentTreeUI(frames[i]);
        frames[0].pack();
    }
    
    public void setVisiblity(boolean b) 
    {
       for(int i=0;i<Gui.Main.getFramesLenght();i++)
       {
           if(b)
               frames[i].setState(Frame.NORMAL);
           frames[i].setVisible(b);
       }
    }
    
    public void setFocus(boolean b)
    {
       for(int i=0;i<Gui.Main.getFramesLenght();i++)
       {
           if(b)
               frames[i].setState(Frame.NORMAL);
           frames[i].toFront();
       }
    }
    
    public static void deleteFrame(String oldFrame,boolean close) throws Exception
    {
        try
        {
            for(int i=0;i<Gui.Main.getFramesLenght();i++)
               if(frames[i].getClass().toString().equals(oldFrame))
               {
                   if(close)
                       frames[i].dispose();
                   for(int j=i;j<Gui.Main.getFramesLenght()-1;j++)
                       frames[j]=frames[j+1];
                   Gui.Main.setFramesLenght(Gui.Main.getFramesLenght()-1);
                   break;
               }
        }
        catch (Exception ex) 
        { 
            trayIcon.setToolTip("Frame error");
            throw new Exception("Frame error " + "at " + className + newline + ex.getMessage());
        }
    }
}