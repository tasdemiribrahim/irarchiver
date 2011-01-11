
package Gui;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFrame;
import Common.MainVocabulary;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

/**
 * Program hakkındaki genel bilgileri görüntüleyen sınıf.
 */
public class About extends JFrame implements MainVocabulary, ActionListener{

    String className = About.class.getName();
    JButton closeButton;
    JLabel nameLabel, projectNameLabel, versionLabel, projectVersionLabel, authorNameLabel,
            projectAuthorNameLabel1, projectAuthorNameLabel2, adviserNameLabel, projectAdviserNameLabel;
    
    /**
     * Program hakkındaki genel bilgileri görüntüleyen Gui.About sınıfının nesnesinin 
     * oluşturulduğu kurucu methodu.
     */
    public About() {
        initComponents();
        if(populateGui())       
            initiateActions();
    }
    
    /**
     * Arayüz bileşenlerinin tanımlandığı method.
     */
    public void initComponents() {
        nameLabel = new JLabel("Project Name:");
        projectNameLabel = new JLabel(projectName);
        versionLabel = new JLabel("Version:");
        projectVersionLabel = new JLabel(projectVersion);
        authorNameLabel = new JLabel("Authors:");
        projectAuthorNameLabel1 = new JLabel("İbrahim Taşdemir");
        projectAuthorNameLabel2 = new JLabel("Ramis Taşgın");
        closeButton = new JButton("Close");
        adviserNameLabel = new JLabel("Adviser:");
        projectAdviserNameLabel = new JLabel("Gürhan Gündüz");
    }
    
    /**
     * Arayüz ve arayüz bileşenlerinin özelliklerinin belirlendeği method. Arayüz bileşenlerinin
     * arayüz içerisindeki konumlarını belirlemek için GridBagLayout() nesnesi kullanılmıştır.
     * @return İşlemler sırasında herhangi bir hata olursa false, olmazsa true.
     */
    private boolean populateGui() {
        try {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();	   
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5,5,5,5);
            constraints.weightx = .5;
            
            constraints.gridx = 0;
            constraints.gridy = 0;
            add(nameLabel,constraints);      
            constraints.gridx = 1;
            constraints.gridy = 0;
            add(projectNameLabel,constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            add(versionLabel,constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            add(projectVersionLabel,constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            add(authorNameLabel,constraints);
            constraints.gridx = 1;
            constraints.gridy = 2;
            add(projectAuthorNameLabel1,constraints);
            constraints.gridx = 1;
            constraints.gridy = 3;
            add(projectAuthorNameLabel2,constraints);
            constraints.gridx = 0;
            constraints.gridy = 4;
            add(adviserNameLabel,constraints);
            constraints.gridx = 1;
            constraints.gridy = 4;
            add(projectAdviserNameLabel,constraints);
            constraints.gridx = 0;
            constraints.gridy = 5;
            constraints.gridwidth = 2;
            add(closeButton,constraints);
            
            setSize(250,200);
            setTitle(projectName + " - Info");
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); 
            Dimension form = getSize();
            setLocation((screen.width - form.width)/2, (screen.height - form.height)/2);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setResizable(false);
            setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, populateGuiError +" From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * ActionListener interface ine ait actionPerformed methodu. closeButton adlı JButton tarafından 
     * ActionEvent oluşturulması durumunda sınıfa ait dispose() methodu çağrılarak about arayüzü
     * kapatılır.
     * @param e Meydana gelen ActionEventinin hangi arayüz bileşeni tarafından oluşturulduğunu 
     * belirlemekte kullanılan değişken. 
     */
    public void actionPerformed(ActionEvent e) { 
        if(e.getSource().equals(closeButton))
            this.dispose();
    }

    /**
     * Arayüz bileşenlerine uygun interface atamalarının yapıldığı method.
     */
    private void initiateActions() {
        closeButton.addActionListener(this);
    }    
}
