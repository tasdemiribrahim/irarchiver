
package Gui;

import Common.MainVocabulary;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * Dosya sıkıştırma, açma; dosya şifreleme, deşifreleme; dosya parçalama, dosya birleştirme işlemleri 
 * esnasında işlemin o anki durumunu gösteren arayüzü oluşturan sınıf.
 */
public class StatusDialog extends JFrame implements MainVocabulary, ActionListener {

    String className = StatusDialog.class.getName();
        
    String cancelMessage, succeedMessage;
    public boolean cancel = false, isIndeterminate = false, isEditable = true;
    JProgressBar statusProgressBar;
    JLabel stateLabel;
    JButton cancelButton, okButton;

    /**
     * Gui.StatusDialog sınıfının nesnesini oluşturan kurucu methodu.
     */
    public StatusDialog() {

        initComponents();
        if (populateGui())
            initiateActions();
    }

     /**
     * Arayüz bileşenlerinin tanımlandığı method.
     */
    private void initComponents() {
        stateLabel = new JLabel("Compressing...");
        statusProgressBar = new JProgressBar();
        cancelButton = new JButton("Cancel");
        okButton = new JButton("Ok");
    }

    /**
     * Arayüz ve arayüz bileşenlerinin özelliklerinin belirlendeği method. Arayüz bileşenlerinin
     * arayüz içerisindeki konumlarını belirlemek için GridBagLayout() nesnesi kullanılmıştır.
     * @return işlemler sırasında herhangi bir hata olursa false, olmazsa true
     */
    private boolean populateGui() {
        try {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.weightx = .5;

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            add(stateLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            add(statusProgressBar, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            add(cancelButton, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            add(okButton, constraints);

            statusProgressBar.setStringPainted(true);
            stateLabel.setHorizontalAlignment(JLabel.CENTER);
            okButton.setVisible(false);
            okButton.setEnabled(false);

            setSize(250, 125);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension form = getSize();
            setLocation((screen.width - form.width) / 2, (screen.height - form.height) / 2);
            setResizable(false);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setVisible(true);

        } catch (final Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, populateGuiError +" From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

     /**
     * Arayüz bileşenlerine uygun interface atamalarının yapıldığı method.
     */
    private void initiateActions() {
        cancelButton.addActionListener(this);
        okButton.addActionListener(this);
    }

    /**
     * ActionListener interface inin bir methodudur. Herhangi bir ActionEvent meydana 
     * gelmesi durumunda ActionEventi gönderen bileşene ait fonksiyonun çağırıldığı method.
     * @param e Meydana gelen ActionEventinin hangi arayüz bileşeni tarafından oluşturulduğunu 
     * belirlemekte kullanılan değişken. 
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(cancelButton)) {
            cancelButtonActionPerformed();
        } else if (e.getSource().equals(okButton)) {
            this.dispose();
        }
    }

    /**
     * cancelButton buttonuna basılması durumunda çağırılan method.
     */
    private void cancelButtonActionPerformed() {
        cancel = true;
        stateLabel.setText(cancelMessage);
        statusProgressBar.setValue(0);
        
        if(isIndeterminate)
            setIndeterminate(false);
        statusProgressBar.setStringPainted(false);
        
        cancelButton.setVisible(false);
        okButton.setVisible(true);
        okButton.setEnabled(true);
    }

    /**
     * StatusDialog arayüzünün durumunu şifreleme durumuna getiren ve statusProggressBar ı 
     * sıfırlayan method.
     */
    public void setStateToEncrypt () {
        stateLabel.setText("Encrypting...");
        statusProgressBar.setValue(0);
        setIndeterminate(false);
    }
    
    /**
     * StatusDialog arayüzünün durumunu şifre çözme durumuna getiren ve statusProggressBar ı 
     * sıfırlayan method.
     */
    public void setStateToDencrypt () {
        stateLabel.setText("Dencrypting...");
        statusProgressBar.setValue(0);
        setIndeterminate(false);
    }
    /**
     * StatusDialog arayüzünün durumunu arşivleme durumuna getiren ve statusProggressBar ı 
     * sıfırlayan method.
     */
    public void setStateToCompress () {
        setTitle(compressionDialogTitle);
        cancelMessage = compressionCanceledMessage;
        succeedMessage = compressionSucceedMessage;
        stateLabel.setText("Compressing...");
        statusProgressBar.setValue(0);
    }
    
    /**
     * StatusDialog arayüzünün durumunu arşiv açma durumuna getiren ve statusProggressBar ı 
     * sıfırlayan method.
     */
    public void setStateToDecompress () {
        setTitle(decompressionDialogTitle);
        cancelMessage = decompressionCanceledMessage;
        succeedMessage = decompressionSucceedMessage;
        stateLabel.setText("Decompressing...");
        statusProgressBar.setValue(0);
    }
    
    /**
     * StatusDialog arayüzünün durumunu dosya parçalama durumuna getiren ve statusProggressBar ı 
     * belirsiz duruma getiren method.
     */
    public void setStateToSplitFile() {
        setIndeterminate(true);
        stateLabel.setText("Spliting Files...");
    }
    
    /**
     * StatusDialog arayüzünün durumunu dosya birleştirme durumuna getiren ve statusProggressBar ı 
     * belirsiz duruma getiren method.
     */
    public void setStateToJoinFile() {
        setIndeterminate(true);
        stateLabel.setText("Joining Files...");
    }
    
    /**
     * statusProgressBar ın değerini güncelleyen method.
     * @param completed statusProgressBarın o anki konumunu gösteren değişken.
     * @param total statusProgressBarın toplam kaç birimden oluştuğunu gösteren değişken.
     */
    public void setStatus(long completed , long total ) {
        int milestone = (int)(((double)completed / (double)total) * 100);
        if (milestone <= 100) {
            statusProgressBar.setValue(milestone);
        }
    }

    /**
     * StatusDialog arayüzünün cancel değerini döndürerek yapılan işlemin iptal 
     * edilip edilmediğini gösteren method.
     * @return işlem iptal edilmişse false, aksi taktirde true
     */
    public boolean isCanceled() {
        return cancel;
    }
    
    /**
     * statusProgressBar ın durumunun değiştirilip değiştirilemeyeceğinin belirlendiği method.
     * @param choise true ise statusProgressBar ın durumu değiştirilebilir, false ise değiştirilemez.
     */
    public void setEditable(boolean choise) {
        isEditable = choise;
    }

    /**
     * statusProgressBar ın belirsiz modda görünüp görünmeyeceğini belirleyen method.
     * @param choice true ise statusProgressBar belirsiz modda, false ise normal
     * modda görünür. 
     */
    public void setIndeterminate(boolean choice) {
        
        if(isEditable) {
            if(choice == true){
                statusProgressBar.setIndeterminate(choice);
                statusProgressBar.setStringPainted(false);
                isIndeterminate = true;
            }else {
                statusProgressBar.setIndeterminate(choice);
                statusProgressBar.setStringPainted(true);
                isIndeterminate = false;
            }
        }
    }

    /**
     * Yapılan işlemin başarıyla tamamlanması durumunda çağırılan method.
     */
    public void completeDialog() {
        stateLabel.setText(succeedMessage);
       
        if(isIndeterminate)
            setIndeterminate(false);
        statusProgressBar.setValue(100);
        
        cancelButton.setVisible(false);
        okButton.setVisible(true);
        okButton.setEnabled(true);
    }
    
    /**
     * Yapılan işlemi sonlandırarak StatusDialog arayüzünü kapatan method.
     */
    public void cancelDialog() {
        cancel = true;
        setVisible(false);
    }
}
