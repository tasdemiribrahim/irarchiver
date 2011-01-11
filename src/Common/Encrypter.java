package Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import Gui.StatusDialog;
import java.net.SocketException;

/**
 * Dosyaların şifrelenmesinde ve deşifre edilmesinde kullanılan sınıf.
 */
public class Encrypter implements MainVocabulary {

    String className = Encrypter.class.getName();
    static final String RANDOM_ALG = "SHA1PRNG";
    static final String DIGEST_ALG = "SHA-256";
    static final String HMAC_ALG = "HmacSHA256";
    static final String CRYPT_ALG = "AES";
    static final String CRYPT_TRANS = "AES/CBC/NoPadding";
    static final byte[] DEFAULT_MAC = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef};
    static final int KEY_SIZE = 32;
    static final int BLOCK_SIZE = 16;
    static final int SHA_SIZE = 32;
    byte[] password;
    Cipher cipher;
    Mac hmac;
    SecureRandom random;
    MessageDigest digest;
    IvParameterSpec ivSpec1;
    SecretKeySpec aesKey1;
    IvParameterSpec ivSpec2;
    SecretKeySpec aesKey2;
    StatusDialog encryptDialog;

    /**
     * Verilen şifre ile encrypt ve decrypt işlemlerinin yapılacağı, StatusDialog arayüzüne sahip bir 
     * Common.Encrypter nesnesinin yaratıldığı kurucu methodu.
     * @throws GeneralSecurityException eğer platform gereken şifreleme methodlarını desteklemiyorsa.
     * @throws UnsupportedEncodingException UTF-16 kodlaması desteklenmiyorsa.
     */
    public Encrypter(String password, StatusDialog dialog) {
        try {

            setPassword(password);
            random = SecureRandom.getInstance(RANDOM_ALG);  //random number generator algoritması belirleniyor.
            digest = MessageDigest.getInstance(DIGEST_ALG); //MessageDigest(Hash) algoritması belirleniyor.
            cipher = Cipher.getInstance(CRYPT_TRANS); //şifreleme yöntemi olarak AES seçiliyor.
            hmac = Mac.getInstance(HMAC_ALG);   //Message Authentication Code algoritması belirleniyor.

            encryptDialog = dialog;
        } catch (GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException ex) {
            JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Random bir byte dizisinin oluşturulduğu method.
     * @param len Oluşturulan byte dizisinin uzunluğu.
     * @return len Uzunlukta bir random byte dizisi. 
     */
    private byte[] generateRandomBytes(int len) {
        byte[] bytes = new byte[len];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Gelen byte dizisi ve random bytelar ile SHA-256 algoritmasına göre bir hash değerinin oluşturulduğu 
     * method. (bytes.length x num) adet byte hash değerine eklenir. Daha sonra oluşturulan bu hash değeri 
     * orjinal byte dizisine kopyalanıyor.
     * @param bytes Hash değerinin tutulacağı byte dizisini gösteren değişken.
     * @param num Döngü sayısını gösteren değişken.
     */
    private void digestRandomBytes(byte[] bytes, int num) {
        assert bytes.length <= SHA_SIZE;

        digest.reset();
        digest.update(bytes);
        for (int i = 0; i < num; i++) {
            random.nextBytes(bytes);
            digest.update(bytes);
        }
        System.arraycopy(digest.digest(), 0, bytes, 0, bytes.length);
    }

    /**
     * Bigisayarın MAC adresine ve o anki zamana göre random bir IV(Initialization Vector) in yaratıldığı
     * method. Bu IV dosyadaki IV 2 ve AES key 2 yi şifrelemekte kullanılır.
     * @return IV.
     */
    private byte[] generateIv1() throws SocketException {
        byte[] iv = new byte[BLOCK_SIZE];
        long time = System.currentTimeMillis();
        byte[] mac = null;

        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();

        while (mac == null && ifaces.hasMoreElements()) {
            mac = ifaces.nextElement().getHardwareAddress();
        }

        if (mac == null) {
            mac = DEFAULT_MAC;
        }

        for (int i = 0; i < 8; i++) {
            iv[i] = (byte) (time >> (i * 8));
        }

        System.arraycopy(mac, 0, iv, 8, mac.length);
        digestRandomBytes(iv, 256);
        return iv;
    }

    /**
     * IV kullanılarak oluşturulan AES key in kullanıcının girmiş olduğu password ile bir döngü vasıtasıyla 
     * güncellenerek son değerinin bulunduğu method.
     * Bu AES key  IV 2 ve AES key 2 yi şifrelemekte kullanılır.
     * @return KEY_SIZE Büyüklüğündeki AES byte dizisi.
     */
    private byte[] generateAESKey1(byte[] iv, byte[] password) {
        byte[] aesKey = new byte[KEY_SIZE];
        System.arraycopy(iv, 0, aesKey, 0, iv.length);
        for (int i = 0; i < 8192; i++) {
            digest.reset();
            digest.update(aesKey);
            digest.update(password);
            aesKey = digest.digest();
        }
        return aesKey;
    }

    /**
     * Dosya içeriğini şifrelemek için random bir IV nin üretildiği method.
     * @return IV2.
     */
    private byte[] generateIV2() {
        byte[] iv = generateRandomBytes(BLOCK_SIZE);
        digestRandomBytes(iv, 256);
        return iv;
    }

    /**
     * Dosya içeriğini şifrelemek için random bir AES key in üretildiği method.
     * @return KEY_SIZE Büyüklüğünde bir AES key.
     */
    private byte[] generateAESKey2() {
        byte[] aesKey = generateRandomBytes(KEY_SIZE);
        digestRandomBytes(aesKey, 32);
        return aesKey;
    }

    /**
     * bytes Dizisi doldurulana kadar stream den okuma yapan method.
     * @throws IOException Array dolmaz ise.
     */
    private void readBytes(InputStream in, byte[] bytes) throws IOException {
        if (in.read(bytes) != bytes.length) {
            throw new IOException(EOFError);
        }
    }

    /**
     * Kullanıcı tarafından girilen passwordun kodlamasını UTF-16 olacak şekilde değiştiren method.
     * @throws UnsupportedEncodingException UTF-16 kodlaması desteklenmiyorsa.
     */
    private void setPassword(String password) throws UnsupportedEncodingException {
        this.password = password.getBytes("UTF-16LE");
    }

    /**
     * inFilePath adresinde bulunan verinin şifrelenerek outFilePath adresine yazıldığı method.
     * Şifrelemede kullanılan AES versiyonu 1 ya da 2 olabilir.
     * @throws IOException I/O hataları olursa.
     * @throws GeneralSecurityException Platform şifreleme methodlarını desteklemiyorsa.
     */
    public void encrypt(int version, String inFilePath, String outFilePath) throws IOException, GeneralSecurityException {

        encryptDialog.setStateToEncrypt();

        long totalSize = 0, comletedSize = 0;
        File outFile = null;
        InputStream in = null;
        OutputStream out = null;
        byte[] text = null;

        try {

            ivSpec1 = new IvParameterSpec(generateIv1());
            aesKey1 = new SecretKeySpec(generateAESKey1(ivSpec1.getIV(), password), CRYPT_ALG);
            ivSpec2 = new IvParameterSpec(generateIV2());
            aesKey2 = new SecretKeySpec(generateAESKey2(), CRYPT_ALG);

            in = new FileInputStream(inFilePath);
            out = new FileOutputStream(outFilePath);
            outFile = new File(outFilePath);
            totalSize = new File(inFilePath).length();

            out.write("AES".getBytes());	// Header bitleri
            out.write(version);	// Versiyon.
            out.write(0);	// Reserve edilmiş bitler.
            if (version == 2) {	// Extension bitinin olmadığını gösterir.
                out.write(0);
                out.write(0);
            }
            out.write(ivSpec1.getIV());	// Initialization Vektörü.

            text = new byte[BLOCK_SIZE + KEY_SIZE];
            cipher.init(Cipher.ENCRYPT_MODE, aesKey1, ivSpec1);
            cipher.update(ivSpec2.getIV(), 0, BLOCK_SIZE, text);
            cipher.doFinal(aesKey2.getEncoded(), 0, KEY_SIZE, text, BLOCK_SIZE);
            out.write(text);	// Şifrelenmiş IV ve key.

            hmac.init(new SecretKeySpec(aesKey1.getEncoded(), HMAC_ALG));
            text = hmac.doFinal(text);
            out.write(text);	// önceki cyphertextin HMAC değeri.

            cipher.init(Cipher.ENCRYPT_MODE, aesKey2, ivSpec2);
            hmac.init(new SecretKeySpec(aesKey2.getEncoded(), HMAC_ALG));
            text = new byte[BLOCK_SIZE];
            int len, last = 0;

            while ((len = in.read(text)) > 0 && !encryptDialog.isCanceled()) {
                cipher.update(text, 0, BLOCK_SIZE, text);
                hmac.update(text);
                out.write(text);	// Şifrelenmiş veri bloğu.
                last = len;

                comletedSize += len;
                encryptDialog.setStatus(comletedSize, totalSize);
            }

            last &= 0x0f;
            out.write(last);	// 4 bitlik mod 16 değeri.

            text = hmac.doFinal();
            out.write(text);	// önceki cyphertextin HMAC değeri.

        } catch (Exception ex) {
            encryptDialog.cancelDialog();
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            outFile.delete();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (encryptDialog.isCanceled()) {
                outFile.delete();
            }
        }
    }

    /**
     * inFiePath adresindeki şifrelenmiş dosyanın deşifre edilerek outFilePath adresine yazıldığı method..
     * Kaynak dosyanın decrypt edilebilmesi için AES versiyon 1 veya 2 ile şifrelenmiş olması gerekmektedir. 
     * @throws IOException I/O hataları olursa.
     * @throws GeneralSecurityException Platform gerekn şifreleme methodlarını desteklemiyorsa.
     */
    public void decrypt(String inFilePath, String outFilePath) throws IOException, GeneralSecurityException {

        encryptDialog.setStateToDencrypt();

        InputStream in = null;
        OutputStream out = null;
        File outFile = null;
        byte[] text = null, backup = null;
        long total = 3 + 1 + 1 + BLOCK_SIZE + BLOCK_SIZE + KEY_SIZE + SHA_SIZE + 1 + SHA_SIZE;
        int version;
        try {

            in = new FileInputStream(inFilePath);
            out = new FileOutputStream(outFilePath);
            outFile = new File(outFilePath);

            text = new byte[3];
            readBytes(in, text);	// sihirli bitler.
            
            if (!new String(text).equals("AES")) {
                throw new Exception("Exception Throwed From:" + className + "\n" + aesHeaderError);
            }

            version = in.read();	// Versiyon.
            if (version < 1 || version > 2) {
                throw new Exception("Exception Throwed From:" + className + "\n" + aesUnsopportedVersionError);
            }

            in.read();	// Reserve.

            if (version == 2) {	// Extension bitleri.

                text = new byte[2];
                int len;
                do {
                    readBytes(in, text);
                    len = ((0xff & (int) text[0]) << 8) | (0xff & (int) text[1]);
                    if (in.skip(len) != len) {
                        throw new Exception("Exception Throwed From:" + className + "\n" + aesExtensionError);
                    }
                    total += 2 + len;
                } while (len != 0);
            }

            text = new byte[BLOCK_SIZE];
            readBytes(in, text);	// başlatma.
            ivSpec1 = new IvParameterSpec(text);
            aesKey1 = new SecretKeySpec(generateAESKey1(ivSpec1.getIV(), password), CRYPT_ALG);

            cipher.init(Cipher.DECRYPT_MODE, aesKey1, ivSpec1);
            backup = new byte[BLOCK_SIZE + KEY_SIZE];
            readBytes(in, backup);	// dosya içeriğini decrypt edecek IV2 and aesKey2 değeri.

            text = cipher.doFinal(backup);
            ivSpec2 = new IvParameterSpec(text, 0, BLOCK_SIZE);
            aesKey2 = new SecretKeySpec(text, BLOCK_SIZE, KEY_SIZE, CRYPT_ALG);

            hmac.init(new SecretKeySpec(aesKey1.getEncoded(), HMAC_ALG));
            backup = hmac.doFinal(backup);
            text = new byte[SHA_SIZE];
            readBytes(in, text);	// HMAC ve doğruluk testi.

            if (!Arrays.equals(backup, text)) {
                throw new Exception(aesPasswordError);
            }

            total = new File(inFilePath).length() - total;	// Payload boyutu.

            if (total % BLOCK_SIZE != 0) {
                throw new Exception("Exception Throwed From:" + className + "\n" + aesCorruptedFileError);
            }

            cipher.init(Cipher.DECRYPT_MODE, aesKey2, ivSpec2);
            hmac.init(new SecretKeySpec(aesKey2.getEncoded(), HMAC_ALG));
            backup = new byte[BLOCK_SIZE];
            text = new byte[BLOCK_SIZE];

            long completedSize = 0;
            int temp = 0;
            for (int block = (int) (total / BLOCK_SIZE); block > 0; block--) {

                if (encryptDialog.isCanceled()) {
                    break;
                }
                int len = BLOCK_SIZE;
                if (in.read(backup, 0, len) != len) {	// Cyphertext bloğu.    
                    throw new Exception("Exception Throwed From:" + className + "\n" + aesUnexpectedEOFError);
                }
                cipher.update(backup, 0, len, text);
                hmac.update(backup, 0, len);
                if (block == 1) {
                    temp = len;
                    len = in.read();	// 4 bits size mod 16.
                }

                if (len != 0) {
                    out.write(text, 0, len);
                } else {
                    out.write(text, 0, temp);
                }
                completedSize += len;
                encryptDialog.setStatus(completedSize, total);
            }

            if (!encryptDialog.isCanceled()) {

                out.write(cipher.doFinal());

                backup = hmac.doFinal();
                text = new byte[SHA_SIZE];
                readBytes(in, text);	// HMAC ve doğruluk testi..

                if (!Arrays.equals(backup, text)) {
                    throw new Exception(aesPasswordError);
                }
            }

        } catch (Exception ex) {
            encryptDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            outFile.delete();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

            if (encryptDialog.isCanceled()) {
                outFile.delete();
            }
        }
    }
}
