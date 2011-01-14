package Gui;

import javax.swing.JFrame;
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
           for(JFrame frame:frames)
               SwingUtilities.updateComponentTreeUI(frame);
           frames.get(0).pack();
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
        catch (Exception ex) 
        {
            throw new Exception(populateGuiError+" at "+ className + " at repaintGUI " + newline +ex.getMessage());
        }
    }
    
    public void setVisiblity(boolean b) 
    {
        for(JFrame frame:frames)
       {
           if(b)
               frame.setState(Frame.NORMAL);
           frame.setVisible(b);
       }
    }
    
    public void setFocus(boolean b)
    {
        for(JFrame frame:frames)
       {
           if(b)
               frame.setState(Frame.NORMAL);
           frame.toFront();
       }
    }
    
    public static void deleteFrame(String oldFrame,boolean close) throws Exception
    {
        try
        {
            for(int i=0;i<frames.size();i++)
               if(frames.get(i).getClass().toString().equals(oldFrame))
               {
                   if(close)
                       frames.get(i).dispose();
                   frames.remove(i);
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