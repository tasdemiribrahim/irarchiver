/*
 * Keiron Liddle, Aftex Software<keiron@aftexsw.com>
 */
package external.org.apache.tools.bzip2;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Baska bir stream'e BZip2 formatinda arsivleme yapan bir stream.
 */
public class CBZip2OutputStream extends OutputStream implements BZip2Constants {
    protected static final int SETMASK = (1 << 21);                             // 00000000001000000000000000000000
    protected static final int CLEARMASK = (~SETMASK);                          // 11111111110111111111111111111111
    protected static final int GREATER_ICOST = 15;
    protected static final int LESSER_ICOST = 0;
    protected static final int SMALL_THRESH = 20;
    protected static final int DEPTH_THRESH = 10;
    /**
     * Eğer siralama yaparken yigin tasmasi yasiyacak kadar sanssizsan
     * (neredeyse imkansız) asagidaki sabiti artirip tekrar dene. Pratikte en 
     * fazla 27 eleman goruldu, yani asagidaki sinir bayagi fazla.
    */
    protected static final int QSORT_STACK_SIZE = 1000;
////////////////////////////////////////////////////////////////////////////////
    /**
     * Hata olmasi durumunda sadece yazdirma yapilir. Dogrusu hata firlatmaktir.
     */
    private static void panic() {
        System.out.println("panic");
        //throw new CError();
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Kullanilan karakterleri ile karakter(seqToUnseq) ve indis(unseqToSeq)
     * dizilerini haritalar. Kullanilan karakter sayisini bulur.
     */
    private void makeMaps() {
        int i;
        nInUse = 0;
        for (i = 0; i < 256; i++) {
            if (inUse[i]) {
                seqToUnseq[nInUse] = (char) i;
                unseqToSeq[i] = (char) nInUse;
                nInUse++;
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Frekanslarla tablolari yeniden hesaplar
     * 
     * @param len : Agacin masrafi
     * @param freq : Frekanslar
     * @param alphaSize : Yaprak sayisi
     * @param maxLen : Paranoya sabiti
     */
    protected static void hbMakeCodeLengths(char[] len, int[] freq,
                                            int alphaSize, int maxLen) {
        int nNodes, nHeap, n1, n2, i, j, k;
        boolean  tooLong;

        int[] heap = new int[MAX_ALPHA_SIZE + 2];
        int[] weight = new int[MAX_ALPHA_SIZE * 2];
        int[] parent = new int[MAX_ALPHA_SIZE * 2];

        for (i = 0; i < alphaSize; i++) {
            weight[i + 1] = (freq[i] == 0 ? 1 : freq[i]) << 8;
        }

        while (true) {
            nNodes = alphaSize;
            nHeap = 0;

            heap[0] = 0;
            weight[0] = 0;
            parent[0] = -2;

            for (i = 1; i <= alphaSize; i++) {
                parent[i] = -1;                                                 // Baslangic degerleri ebeveyn=-1
                nHeap++;                                                        // yaprak=i
                heap[nHeap] = i;
                {
                    int zz, tmp;
                    zz = nHeap;
                    tmp = heap[zz];
                    while (weight[tmp] < weight[heap[zz >> 1]]) {               // Agirliklara gore yapraklari siralama
                        heap[zz] = heap[zz >> 1];
                        zz >>= 1;
                    }
                    heap[zz] = tmp;
                }
            }
            if (!(nHeap < (MAX_ALPHA_SIZE + 2))) {
                panic();
            }

            while (nHeap > 1) {                                                 // Ilk yaprak n1
                n1 = heap[1];
                heap[1] = heap[nHeap];
                nHeap--;
                {
                    int zz = 0, yy = 0, tmp = 0;
                    zz = 1;
                    tmp = heap[zz];
                    while (true) {                                              // Yapraklari tekrar siralama
                        yy = zz << 1;
                        if (yy > nHeap) {
                            break;
                        }
                        if (yy < nHeap
                            && weight[heap[yy + 1]] < weight[heap[yy]]) {
                            yy++;
                        }
                        if (weight[tmp] < weight[heap[yy]]) {
                            break;
                        }
                        heap[zz] = heap[yy];
                        zz = yy;
                    }
                    heap[zz] = tmp;
                }
                n2 = heap[1];
                heap[1] = heap[nHeap];                                          // Ikinci yaprak n2
                nHeap--;
                {
                    int zz = 0, yy = 0, tmp = 0;
                    zz = 1;
                    tmp = heap[zz];
                    while (true) {
                        yy = zz << 1;                                           // Yapraklari tekrar sirala
                        if (yy > nHeap) {
                            break;
                        }
                        if (yy < nHeap
                            && weight[heap[yy + 1]] < weight[heap[yy]]) {
                            yy++;
                        }
                        if (weight[tmp] < weight[heap[yy]]) {
                            break;
                        }
                        heap[zz] = heap[yy];
                        zz = yy;
                    }
                    heap[zz] = tmp;
                }
                nNodes++;
                parent[n1] = parent[n2] = nNodes;

                weight[nNodes] = ((weight[n1] & 0xffffff00)                     // n1 ve n2 nin ebeveynini ata
                                  + (weight[n2] & 0xffffff00))
                    | (1 + (((weight[n1] & 0x000000ff)
                            > (weight[n2] & 0x000000ff))
                            ? (weight[n1] & 0x000000ff)
                            : (weight[n2] & 0x000000ff)));

                parent[nNodes] = -1;                                            // Ebeveynin agirligi
                nHeap++;
                heap[nHeap] = nNodes;
                {
                    int zz = 0, tmp = 0;
                    zz = nHeap;
                    tmp = heap[zz];
                    while (weight[tmp] < weight[heap[zz >> 1]]) {               // Yapraklari tekrar sirala
                        heap[zz] = heap[zz >> 1];
                        zz >>= 1;
                    }
                    heap[zz] = tmp;
                }
            }
            if (!(nNodes < (MAX_ALPHA_SIZE * 2))) {
                panic();
            }

            tooLong = false;
            for (i = 1; i <= alphaSize; i++) {
                j = 0;
                k = i;
                while (parent[k] >= 0) {
                    k = parent[k];
                    j++;
                }
                len[i - 1] = (char) j;                                          // Agac sinir kontrolu
                if (j > maxLen) {                                               // Agacin boyutu
                    tooLong = true;
                }
            }

            if (!tooLong) {
                break;
            }

            for (i = 1; i < alphaSize; i++) {                                   // Agirlik guncellenir
                j = weight[i] >> 8;
                j = 1 + (j / 2);
                weight[i] = j << 8;
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
    int last;                                                                   // Bloktaki son karakterin indisi, yani blok boyutu=last+1
    int origPtr;                                                                // Siralama sonrasi orijinal dizinin zptr[]'deki indisi
    int blockSize100k;                                                          // Suanki blok boyutu=100000 * bu sayi
    boolean blockRandomised;                                                    // Blok rastgelelenmis ise neredeyse imkansız
    int bytesOut;                                                               // Yazilan byte sayisi
    int bsBuff;                                                                 // Karakter tamponu
    int bsLive;                                                                 // Tampon boyutu
    CRC mCrc = new CRC();                                                       // CRC degeri
    private boolean[] inUse = new boolean[256];                                 // Kullanilan karakter isaretcisi
    private int nInUse;                                                         // Kullanilan karakter dizisi
    private char[] seqToUnseq = new char[256];                                  // Kullanilan karakter haritasi
    private char[] unseqToSeq = new char[256];                                  // Kullanilan karakter indis haritasi
    private char[] selector = new char[MAX_SELECTORS];                          // Tablo secici
    private char[] selectorMtf = new char[MAX_SELECTORS];                       // Tablo secici MTF'lenmis
    private char[] block;                                                       // Blok
    private int[] quadrant;                                                     // Kullanilan her karakterin quadrant degeri
    private int[] zptr;                                                         // Siralanmis karakter indisleri
    private short[] szptr;                                                      // MTF degerlerini tutar
    private int[] ftab;                                                         // Artis kayitlarini tutar
    private int nMTF;                                                           // Kac karakterin MTF'lendigini tutar. Tekrarlar da dahil
    private int[] mtfFreq = new int[MAX_ALPHA_SIZE];                            // MTF frekansini tutar
    private int workFactor;                                                     // std = 50
    private int workDone;                                                       // Siralama deneme sayisi
    private int workLimit;                                                      // Siralama deneme siniri
    private boolean firstAttempt;                                               // Ilk siralama denemesi
    private int nBlocksRandomised;                                              // Rastgelenmis blok sayisi
    private int currentChar = -1;                                               // Suanki karakter
    private int runLength = 0;                                                  // Tekrar sayisi
////////////////////////////////////////////////////////////////////////////////
    /**
     * Sabit 9 blok boyutlu kurucu
     * 
     * @param inStream
     * @throws java.io.IOException
     */
    public CBZip2OutputStream(OutputStream inStream) throws IOException {
        this(inStream, 9);
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Kullanici tanimli blok boyutunda kurucu
     * 
     * @param inStream
     * @param inBlockSize
     * @throws java.io.IOException
     */
    public CBZip2OutputStream(OutputStream inStream, int inBlockSize)
        throws IOException {
        block = null;
        quadrant = null;
        zptr = null;
        ftab = null;

        bsSetStream(inStream);

        workFactor = 50;
        if (inBlockSize > 9) {
            inBlockSize = 9;
        }
        if (inBlockSize < 1) {
            inBlockSize = 1;
        }
        blockSize100k = inBlockSize;
        allocateCompressStructures();
        initialize();
        initBlock();
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Bytelar yazar. write(int b) metodunu cagirir.
     * 
     * @param b : Yazilacak bytelar
     * @param off : Offset
     * @param len : Kac byte yazilacagi
     */
    public void write(byte b[], int off, int len) throws IOException {
	if (b == null) {
	    throw new IOException( "empty 'b' byte set ");
	} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IOException( "request to write '" + len + "' bytes exceeds size of 'b' byte set '" + b.length + "' bytes" );
	} else if (len == 0) {
	    return;
	}
	for (int i = 0 ; i < len ; i++) {
	    write(b[off + i]);
	}
    }    
////////////////////////////////////////////////////////////////////////////////
    /**
     * Oliver Merkel tarafindan gelistirildi. Gelen karakterin tekrarini kontrol
     * eder ve yazar.
     * 
     * @param bv : Yazilacak karakter
     * @throws java.io.IOException
     */
    public void write(int bv) throws IOException {
        int b = (256 + bv) % 256;
        if (currentChar != -1) {
            if (currentChar == b) {                                             // Karakter tekrari varsa
                runLength++;
                if (runLength > 254) {                                          // 254den fazla tekrar varsa yaz
                    writeRun();
                    currentChar = -1;
                    runLength = 0;
                }
            } else {
                writeRun();                                                     // Tekrar yoksa yaz
                runLength = 1;
                currentChar = b;
            }
        } else {
            currentChar = b;
            runLength++;
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Blok doluncaya kadar yazar. RLE degerlerini de yazar.
     * 
     * @throws java.io.IOException
     */
    private void writeRun() throws IOException {
        if (last < allowableBlockSize) {                                        // Blok dolmadiysa
            inUse[currentChar] = true;                                          // Kullanilan karakter isaretlenir
            for (int i = 0; i < runLength; i++) {                               // CRC guncellenir
                mCrc.updateCRC((char) currentChar);
            }
            switch (runLength) {                                                // Tekrar sayisi kadar
            case 1:                                                             // Karakter bloga atilir
                last++;
                block[last + 1] = (char) currentChar;
                break;
            case 2:
                last++;
                block[last + 1] = (char) currentChar;
                last++;
                block[last + 1] = (char) currentChar;
                break;
            case 3:
                last++;
                block[last + 1] = (char) currentChar;
                last++;
                block[last + 1] = (char) currentChar;
                last++;
                block[last + 1] = (char) currentChar;
                break;
            default:
                inUse[runLength - 4] = true;
                last++;
                block[last + 1] = (char) currentChar;
                last++;
                block[last + 1] = (char) currentChar;
                last++;
                block[last + 1] = (char) currentChar;
                last++;
                block[last + 1] = (char) currentChar;
                last++;
                block[last + 1] = (char) (runLength - 4);
                break;                                                          // RLE adiminda 3 tekrardan fazlasi isaretlenir
            }
        } else {
            endBlock();
            initBlock();
            writeRun();
        }
    }
    boolean closed = false;
////////////////////////////////////////////////////////////////////////////////
    /**
     * Arsivleme sonlardirma yapar.
     * 
     * @throws java.lang.Throwable
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Arsivleme sonlandirma yapar. Genellikle bu metod kullanilir.
     * 
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        if (closed) {
            return;
        }

        if (runLength > 0) {
            writeRun();
        }
        currentChar = -1;
        endBlock();
        endCompression();
        closed = true;
        super.close();
        bsStream.close();
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Stream bosaltir
     * 
     * @throws java.io.IOException
     */
    public void flush() throws IOException {
        super.flush();
        bsStream.flush();
    }

    private int blockCRC, combinedCRC;
////////////////////////////////////////////////////////////////////////////////
    /**
     * Arsiv tipi magic kontrolu yapar ve blok boyutunu alir.
     * 
     * @throws java.io.IOException
     */
    private void initialize() throws IOException {
        bytesOut = 0;
        nBlocksRandomised = 0;
        bsPutUChar('B');
        bsPutUChar('Z');
        bsPutUChar('h');
        bsPutUChar('0' + blockSize100k);

        combinedCRC = 0;
    }

    private int allowableBlockSize;
////////////////////////////////////////////////////////////////////////////////
    /**
     * Blok degiskenlerinin baslangic degerlerini atar
     */
    private void initBlock() {
        //        blockNo++;
        mCrc.initialiseCRC();
        last = -1;
        //        ch = 0;

        for (int i = 0; i < 256; i++) {
            inUse[i] = false;
        }

        /* 20 paranoya sabiti. */
        allowableBlockSize = baseBlockSize * blockSize100k - 20;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Blok bittiginde islem yapar.
     * 
     * @throws java.io.IOException
     */
    private void endBlock() throws IOException {
        blockCRC = mCrc.getFinalCRC();                                          // Blok CRC alir
        combinedCRC = (combinedCRC << 1) | (combinedCRC >>> 31);                // Arsiv CRC hesaplar
        combinedCRC ^= blockCRC;                                                // Blok CRC ilk bittir
        doReversibleTransformation();                                           // Blogu sirala ve orijinal dizinin konumunu ayarla
        bsPutUChar(0x31);                                                       // Baslangic isaretcisi (pi)
        bsPutUChar(0x41);
        bsPutUChar(0x59);
        bsPutUChar(0x26);
        bsPutUChar(0x53);
        bsPutUChar(0x59);
        
        bsPutint(blockCRC);                                                     // Blok CRC yazilir

        if (blockRandomised) {                                                  // Rastgelelenme
            bsW(1, 1);
            nBlocksRandomised++;
        } else {
            bsW(1, 0);
        }

        moveToFrontCodeAndSend();                                               // En son blogun icerigi
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Arsiv sonu magic karekok(pi) degeri ve CRC yazar.
     * 
     * @throws java.io.IOException
     */
    private void endCompression() throws IOException {
        
        bsPutUChar(0x17);
        bsPutUChar(0x72);
        bsPutUChar(0x45);
        bsPutUChar(0x38);
        bsPutUChar(0x50);
        bsPutUChar(0x90);

        bsPutint(combinedCRC);

        bsFinishedWithStream();
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Tablolar icin esas kodlari atar
     * 
     * @param code : Kod tablosu bos gelir
     * @param length : Masraf
     * @param minLen : En az masraf
     * @param maxLen : En fazla masraf
     * @param alphaSize : Yaprak sayisi
     */
    private void hbAssignCodes (int[] code, char[] length, int minLen,
                                int maxLen, int alphaSize) {
        int n, vec, i;

        vec = 0;
        for (n = minLen; n <= maxLen; n++) {
            for (i = 0; i < alphaSize; i++) {
                if (length[i] == n) {
                    code[i] = vec;
                    vec++;
                }
            };
            vec <<= 1;
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Stream icin baslangic degerlerini sifirlar.
     * 
     * @param f : Output Stream
     */
    private void bsSetStream(OutputStream f) {
        bsStream = f;
        bsLive = 0;
        bsBuff = 0;
        bytesOut = 0;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Tamponda kalan karakterleri yazar.
     * 
     * @throws java.io.IOException
     */
    private void bsFinishedWithStream() throws IOException {
        while (bsLive > 0) {
            int ch = (bsBuff >> 24);
            try {
                bsStream.write(ch);
            } catch (IOException e) {
                throw  e;
            }
            bsBuff <<= 8;
            bsLive -= 8;
            bytesOut++;
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Tamponda kalan karakteri yazar. n boyutundaki v degerini tampona atar.
     * 
     * @param n : Bit sayisi
     * @param v : Tampona atilacak karakter
     * @throws java.io.IOException
     */
    private void bsW(int n, int v) throws IOException {
        while (bsLive >= 8) {
            int ch = (bsBuff >> 24);
            try {
                bsStream.write(ch);
            } catch (IOException e) {
                throw e;
            }
            bsBuff <<= 8;
            bsLive -= 8;
            bytesOut++;
        }
        bsBuff |= (v << (32 - bsLive - n));
        bsLive += n;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * 8 bit c karakterini tampona atar
     * 
     * @param c
     * @throws java.io.IOException
     */
    private void bsPutUChar(int c) throws IOException {
        bsW(8, c);
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * u tam sayisini tampona atar.
     * 
     * @param u
     * @throws java.io.IOException
     */
    private void bsPutint(int u) throws IOException {
        bsW(8, (u >> 24) & 0xff);
        bsW(8, (u >> 16) & 0xff);
        bsW(8, (u >>  8) & 0xff);
        bsW(8,  u        & 0xff);
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * c degerini numBits kadar bit ile tampona atar
     * 
     * @param numBits
     * @param c
     * @throws java.io.IOException
     */
    private void bsPutIntVS(int numBits, int c) throws IOException {
        bsW(numBits, c);
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Huffman tablolarini olusturur ve yazar.
     * 
     * @throws java.io.IOException
     */
    private void sendMTFValues() throws IOException {
        char len[][] = new char[N_GROUPS][MAX_ALPHA_SIZE];

        int v, t, i, j, gs, ge, totc, bt, bc, iter;
        int nSelectors = 0, alphaSize, minLen, maxLen, selCtr;
        int nGroups, nBytes;

        alphaSize = nInUse + 2;
        for (t = 0; t < N_GROUPS; t++) {
            for (v = 0; v < alphaSize; v++) {
                len[t][v] = (char) GREATER_ICOST;
            }
        }
        
        if (nMTF <= 0) {                                                        // Kac kodlama tablosunun kullanilacagi
            panic();
        }

        if (nMTF < 200) {
            nGroups = 2;
        } else if (nMTF < 600) {
            nGroups = 3;
        } else if (nMTF < 1200) {
            nGroups = 4;
        } else if (nMTF < 2400) {
            nGroups = 5;
        } else {
            nGroups = 6;
        }

        {
            int nPart, remF, tFreq, aFreq;                                      // Kodlama tablolarinin baslangic setini olusturur

            nPart = nGroups;
            remF  = nMTF;
            gs = 0;
            while (nPart > 0) {
                tFreq = remF / nPart;                                           // Tablo icin frekans = kalan frekans/kalan tablo
                ge = gs - 1;
                aFreq = 0;
                while (aFreq < tFreq && ge < alphaSize - 1) {                   // Tablo icin MTF frekanslari toplami ve kac karakter kodlanacak
                    ge++;
                    aFreq += mtfFreq[ge];
                }

                if (ge > gs && nPart != nGroups && nPart != 1                   // Sayi tam bolunemiyorsa iki tablodan biri az karakter tutar
                    && ((nGroups - nPart) % 2 == 1)) {
                    aFreq -= mtfFreq[ge];
                    ge--;
                }

                for (v = 0; v < alphaSize; v++) {
                    if (v >= gs && v <= ge) {                                   // Tablo alfa kadar karakter tutuyorsa
                        len[nPart - 1][v] = (char) LESSER_ICOST;                // Masrafi 0
                    } else {                                                    // Fazla karakterlerin masrafi 15
                        len[nPart - 1][v] = (char) GREATER_ICOST;
                    }
                }

                nPart--;                                                        // Kalan tablo sayisi guncellenir
                gs = ge + 1;                                                    // Karakter offseti guncellenir
                remF -= aFreq;                                                  // Kalan frekans guncellenir
            }
        }

        int[][] rfreq = new int[N_GROUPS][MAX_ALPHA_SIZE];
        int[] fave = new int[N_GROUPS];
        short[] cost = new short[N_GROUPS];
        for (iter = 0; iter < N_ITERS; iter++) {                                // Tabloyu N_ITERS kadar gelistir
            for (t = 0; t < nGroups; t++) {
                fave[t] = 0;
            }

            for (t = 0; t < nGroups; t++) {
                for (v = 0; v < alphaSize; v++) {
                    rfreq[t][v] = 0;
                }
            }

            nSelectors = 0;
            totc = 0;
            gs = 0;
            while (true) {                                                      // Grup baslangic ve bitis izlerini ata

                if (gs >= nMTF) {
                    break;
                }
                ge = gs + G_SIZE - 1;                                           // 50 karakteri kodlamak icin
                if (ge >= nMTF) {
                    ge = nMTF - 1;
                }

                for (t = 0; t < nGroups; t++) {                                 // Grubun masrafini kodlama tablolarinin her biri ile kodlanmis 
                    cost[t] = 0;                                                // olarak hesapla
                }

                if (nGroups == 6) {
                    short cost0, cost1, cost2, cost3, cost4, cost5;
                    cost0 = cost1 = cost2 = cost3 = cost4 = cost5 = 0;
                    for (i = gs; i <= ge; i++) {
                        short icv = szptr[i];                                   // MTF degerini alir
                        cost0 += len[0][icv];                                   // 1. Tablo icin masraf
                        cost1 += len[1][icv];                                   // 2.   "       "
                        cost2 += len[2][icv];                                   // 3.   "       "
                        cost3 += len[3][icv];
                        cost4 += len[4][icv];
                        cost5 += len[5][icv];
                    }
                    cost[0] = cost0;
                    cost[1] = cost1;
                    cost[2] = cost2;
                    cost[3] = cost3;
                    cost[4] = cost4;
                    cost[5] = cost5;
                } else {
                    for (i = gs; i <= ge; i++) {
                        short icv = szptr[i];
                        for (t = 0; t < nGroups; t++) {
                            cost[t] += len[t][icv];                             // Her tablo icin masrafi hesaplar
                        }
                    }
                }

                bc = 999999999;
                bt = -1;
                for (t = 0; t < nGroups; t++) {                                 // Bu grup icin en iyi kodlama tablosunu bul ve secici tablosuna
                    if (cost[t] < bc) {                                         // kaydet
                        bc = cost[t];
                        bt = t;
                    }
                };
                totc += bc;
                fave[bt]++;
                selector[nSelectors] = (char) bt;                               // Secici tabloya eklendi
                nSelectors++;

                for (i = gs; i <= ge; i++) {
                    rfreq[bt][szptr[i]]++;                                      // Secili tablo icin sembol frekanslarini artir
                }

                gs = ge + 1;
            }

            for (t = 0; t < nGroups; t++) {                                     // Eklenmis frekanslarla tablolari yeniden hesapla
                hbMakeCodeLengths(len[t], rfreq[t], alphaSize, 20);
            }
        }

        rfreq = null;
        fave = null;
        cost = null;

        if (!(nGroups < 8)) {
            panic();
        }
        if (!(nSelectors < 32768 && nSelectors <= (2 + (900000 / G_SIZE)))) {
            panic();
        }
        {
            char[] pos = new char[N_GROUPS];                                    // Seciciler icin MTF degerlerini hesapla
            char ll_i, tmp2, tmp;
            for (i = 0; i < nGroups; i++) {
                pos[i] = (char) i;
            }
            for (i = 0; i < nSelectors; i++) {
                ll_i = selector[i];
                j = 0;
                tmp = pos[j];
                while (ll_i != tmp) {
                    j++;
                    tmp2 = tmp;
                    tmp = pos[j];
                    pos[j] = tmp2;
                }
                pos[0] = tmp;
                selectorMtf[i] = (char) j;
            }
        }

        int[][] code = new int[N_GROUPS][MAX_ALPHA_SIZE];
        
        for (t = 0; t < nGroups; t++) {                                         // Tablolar icin esas kodlari ata
            minLen = 32;
            maxLen = 0;
            for (i = 0; i < alphaSize; i++) {
                if (len[t][i] > maxLen) {
                    maxLen = len[t][i];
                }
                if (len[t][i] < minLen) {
                    minLen = len[t][i];
                }
            }
            if (maxLen > 20) {
                panic();
            }
            if (minLen < 1) {
                panic();
            }
            hbAssignCodes(code[t], len[t], minLen, maxLen, alphaSize);
        }
        {
            boolean[] inUse16 = new boolean[16];                                // Haritalama tablosunu olustur
            for (i = 0; i < 16; i++) {
                inUse16[i] = false;
                for (j = 0; j < 16; j++) {
                    if (inUse[i * 16 + j]) {
                        inUse16[i] = true;
                    }
                }
            }

            nBytes = bytesOut;                                                  // 16 parca
            for (i = 0; i < 16; i++) {
                if (inUse16[i]) {
                    bsW(1, 1);
                } else {
                    bsW(1, 0);
                }
            }

            for (i = 0; i < 16; i++) {
                if (inUse16[i]) {                                               // Kullanilan semboller haritasi
                    for (j = 0; j < 16; j++) {
                        if (inUse[i * 16 + j]) {
                            bsW(1, 1);
                        } else {
                            bsW(1, 0);
                        }
                    }
                }
            }

        }
        
        nBytes = bytesOut;                                                      // Simdi seciciler
        bsW (3, nGroups);                                                       // Huffman tablo sayisi
        bsW (15, nSelectors);                                                   // Secici degerleri
        for (i = 0; i < nSelectors; i++) {
            for (j = 0; j < selectorMtf[i]; j++) {                              // MTFlenmis seciciler
                bsW(1, 1);
            }
            bsW(1, 0);
        }

        nBytes = bytesOut;                                                      // Simdi kodlama tablolari

        for (t = 0; t < nGroups; t++) {
            int curr = len[t][0];
            bsW(5, curr);
            for (i = 0; i < alphaSize; i++) {
                while (curr < len[t][i]) {
                    bsW(2, 2);
                    curr++;                                                     // 10 : Artir
                }
                while (curr > len[t][i]) {
                    bsW(2, 3);
                    curr--;                                                     // 11 : Azalt
                }
                bsW (1, 0);                                                     // Sonraki sembol
            }
        }

        nBytes = bytesOut;                                                      // En son blok verisi
        selCtr = 0;
        gs = 0;
        while (true) {
            if (gs >= nMTF) {
                break;
            }
            ge = gs + G_SIZE - 1;
            if (ge >= nMTF) {
                ge = nMTF - 1;
            }
            for (i = gs; i <= ge; i++) {                                        // Masraf ile kodun yazilmasi
                bsW(len[selector[selCtr]][szptr[i]],
                    code[selector[selCtr]][szptr[i]]);
            }

            gs = ge + 1;
            selCtr++;
        }
        if (!(selCtr == nSelectors)) {
            panic();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * MTF islemlerini yapar. Baslangic isaretcisini yazar.
     * 
     * @throws java.io.IOException
     */
    private void moveToFrontCodeAndSend () throws IOException {
        bsPutIntVS(24, origPtr);
        generateMTFValues();
        sendMTFValues();
    }

    private OutputStream bsStream;
////////////////////////////////////////////////////////////////////////////////
    /**
     * lo indisinden baslayarak hi indisine kadar degerlere d sabiti eklenmis
     * durumlarina gore siralar
     * 
     * @param lo : Siralama baslangici
     * @param hi : Siralama sonu
     * @param d : Siralama fazlalik sabiti
     */
    private void simpleSort(int lo, int hi, int d) {
        int i, j, h, bigN, hp;
        int v;

        bigN = hi - lo + 1;
        if (bigN < 2) {
            return;
        }

        hp = 0;
        while (incs[hp] < bigN) {                                               // Knuth degerleri aliniyor
            hp++;
        }
        hp--;

        for (; hp >= 0; hp--) {                                                 // Knuth degerleri yardimiyla
            h = incs[hp];                                                       // Siralama

            i = lo + h;                                                         // 1. Kopya
            while (true) {
                
                if (i > hi) {
                    break;
                }
                v = zptr[i];                                                    // v sirasi belirlenecek deger
                j = i;
                while (fullGtU(zptr[j - h] + d, v + d)) {                       // Degeri h alta alir
                    zptr[j] = zptr[j - h];
                    j = j - h;
                    if (j <= (lo + h - 1)) {
                        break;
                    }
                }
                zptr[j] = v;                                                    // Deger uygun siraya yerlestirilir
                i++;                                                            // Sonraki degere gecilir

                if (i > hi) {                                                   // 2. Kopya
                    break;
                }
                v = zptr[i];
                j = i;
                while (fullGtU(zptr[j - h] + d, v + d)) {
                    zptr[j] = zptr[j - h];
                    j = j - h;
                    if (j <= (lo + h - 1)) {
                        break;
                    }
                }
                zptr[j] = v;
                i++;

                if (i > hi) {                                                   // 3. Kopya
                    break;
                }
                v = zptr[i];
                j = i;
                while (fullGtU(zptr[j - h] + d, v + d)) {
                    zptr[j] = zptr[j - h];
                    j = j - h;
                    if (j <= (lo + h - 1)) {
                        break;
                    }
                }
                zptr[j] = v;
                i++;

                if (workDone > workLimit && firstAttempt) {
                    return;
                }
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * n kadar karakteri p1 ve p2 den baslayarak zptr degerlerini swaplar
     * 
     * @param p1
     * @param p2
     * @param n
     */
    private void vswap(int p1, int p2, int n) {
        int temp = 0;
        while (n > 0) {
            temp = zptr[p1];
            zptr[p1] = zptr[p2];
            zptr[p2] = temp;
            p1++;
            p2++;
            n--;
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Orta buyuklukteki karakteri dondurur
     * 
     * @param a : Ilk karakter
     * @param b : Ikinci karakter
     * @param c : Ucuncu karakter
     * @return orta buyuklukteki karakter
     */
    private char med3(char a, char b, char c) {
        char t;
        if (a > b) {
            t = a;
            a = b;
            b = t;
        }
        if (b > c) {
            t = b;
            b = c;
            c = t;
        }
        if (a > b) {
            b = a;
        }
        return b;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Yigin elemanlarini tutar
     */
    private static class StackElem {
        int ll;
        int hh;
        int dd;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Hizli siralama yapan metod
     * 
     * @param loSt : Baslangic
     * @param hiSt : Bitis
     * @param dSt : Siralama farki
     */
    private void qSort3(int loSt, int hiSt, int dSt) {
        int unLo, unHi, ltLo, gtHi, med, n, m;
        int sp, lo, hi, d;
        StackElem[] stack = new StackElem[QSORT_STACK_SIZE];
        for (int count = 0; count < QSORT_STACK_SIZE; count++) {
            stack[count] = new StackElem();
        }

        sp = 0;

        stack[sp].ll = loSt;
        stack[sp].hh = hiSt;
        stack[sp].dd = dSt;
        sp++;

        while (sp > 0) {
            if (sp >= QSORT_STACK_SIZE) {
                panic();
            }

            sp--;
            lo = stack[sp].ll;
            hi = stack[sp].hh;
            d = stack[sp].dd;

            if (hi - lo < SMALL_THRESH || d > DEPTH_THRESH) {
                simpleSort(lo, hi, d);
                if (workDone > workLimit && firstAttempt) {
                    return;
                }
                continue;
            }

            med = med3(block[zptr[lo] + d + 1],
                       block[zptr[hi            ] + d  + 1],
                       block[zptr[(lo + hi) >> 1] + d + 1]);

            unLo = ltLo = lo;
            unHi = gtHi = hi;

            while (true) {
                while (true) {
                    if (unLo > unHi) {
                        break;
                    }
                    n = ((int) block[zptr[unLo] + d + 1]) - med;
                    if (n == 0) {
                        int temp = 0;
                        temp = zptr[unLo];
                        zptr[unLo] = zptr[ltLo];
                        zptr[ltLo] = temp;
                        ltLo++;
                        unLo++;
                        continue;
                    };
                    if (n >  0) {
                        break;
                    }
                    unLo++;
                }
                while (true) {
                    if (unLo > unHi) {
                        break;
                    }
                    n = ((int) block[zptr[unHi] + d + 1]) - med;
                    if (n == 0) {
                        int temp = 0;
                        temp = zptr[unHi];
                        zptr[unHi] = zptr[gtHi];
                        zptr[gtHi] = temp;
                        gtHi--;
                        unHi--;
                        continue;
                    };
                    if (n <  0) {
                        break;
                    }
                    unHi--;
                }
                if (unLo > unHi) {
                    break;
                }
                int temp = 0;
                temp = zptr[unLo];
                zptr[unLo] = zptr[unHi];
                zptr[unHi] = temp;
                unLo++;
                unHi--;
            }

            if (gtHi < ltLo) {
                stack[sp].ll = lo;
                stack[sp].hh = hi;
                stack[sp].dd = d + 1;
                sp++;
                continue;
            }

            n = ((ltLo - lo) < (unLo - ltLo)) ? (ltLo - lo) : (unLo - ltLo);
            vswap(lo, unLo - n, n);
            m = ((hi - gtHi) < (gtHi - unHi)) ? (hi - gtHi) : (gtHi - unHi);
            vswap(unLo, hi - m + 1, m);

            n = lo + unLo - ltLo - 1;
            m = hi - (gtHi - unHi) + 1;

            stack[sp].ll = lo;
            stack[sp].hh = n;
            stack[sp].dd = d;
            sp++;

            stack[sp].ll = n + 1;
            stack[sp].hh = m - 1;
            stack[sp].dd = d + 1;
            sp++;

            stack[sp].ll = m;
            stack[sp].hh = hi;
            stack[sp].dd = d;
            sp++;
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Karakterlerin kac kez kullanildigina gore siralama yapar
     */
    private void mainSort() {
        int i, j, ss, sb;
        int[] runningOrder = new int[256];
        int[] copy = new int[256];
        boolean[] bigDone = new boolean[256];
        int c1, c2;
        int numQSorted;

        for (i = 0; i < NUM_OVERSHOOT_BYTES; i++) {                             // Genel blok yapilarinda veri 0 ile last+NUM_OVERSHOOT_BYTES
            block[last + i + 2] = block[(i % (last + 1)) + 1];                  // arasinda bulunur. Ilk olarak overshoot alani kurulur.
        }                                                                       // Ilk 20 karakteri kopyalar
        for (i = 0; i <= last + NUM_OVERSHOOT_BYTES; i++) {
            quadrant[i] = 0;
        }

        block[0] = (char) (block[last + 1]);

        if (last < 4000) {                                                      // Fazla karakter yoksa
            for (i = 0; i <= last; i++) {                                       // simpleSort() kullanilir
                zptr[i] = i;
            }
            firstAttempt = false;
            workDone = workLimit = 0;
            simpleSort(0, last, 0);
        } else {                                                                // Knuth yardimiyla kucukten buyuge siralama
            numQSorted = 0;                                                     // 256 karakter icin siralama isaretcisi
            for (i = 0; i <= 255; i++) {
                bigDone[i] = false;
            }

            for (i = 0; i <= 65536; i++) {
                ftab[i] = 0;
            }

            c1 = block[0];
            for (i = 0; i <= last; i++) {
                c2 = block[i + 1];                                              // ftab, blok degerlerinin ikili birlestirilmis halini tutar
                ftab[(c1 << 8) + c2]++;                                         // Blok degerlerinin gosterdigi indis bir artirilir
                c1 = c2;                                                        // ftab[blok[i]blok[i+1]]++
            }
            for (i = 1; i <= 65536; i++) {                                      // Sayi artmasi olan yerler bloklarin gosterdigi indisler
                ftab[i] += ftab[i - 1];                                         // Yani kullanilan karakterler. ftab kademeli olarak toplanir
            }                                                                   // Boylece tum ftab degerleri bir deger almis olur
            c1 = block[1];
            for (i = 0; i < last; i++) {                                        // ftab[blok[0]blok[1]] disindaki bloklarin gosterdigi indisler
                c2 = block[i + 2];                                              // 1 eksiltilir.
                j = (c1 << 8) + c2;                                             
                c1 = c2;                                                        // Simdi kullanilan karakter ciftleri artistan bir onceki.
                ftab[j]--;                                                      // zptr ftab degeri indisinde i. karakter tutar
                zptr[ftab[j]] = i;                                              
            }                                                                   
            j = ((block[last + 1]) << 8) + (block[1]);                          // ftab[blok[0]blok[1]] degeri azaltilir
            ftab[j]--;                                                          // Simdi ftab tum karakter ciftlerinin izini tutar.
            zptr[ftab[j]] = last;                                               // Son karakteri gosterir
              
            for (i = 0; i <= 255; i++) {
                runningOrder[i] = i;
            }

            {
                int vv;
                int h = 1;
                do {
                    h = 3 * h + 1;                                              // Knuth degerleri
                }
                while (h <= 256);
                do {
                    h = h / 3;
                    for (i = h; i <= 255; i++) {
                        vv = runningOrder[i];
                        j = i;
                        while ((ftab[((runningOrder[j - h]) + 1) << 8]          // Karakterin kac defa kullanildigina gore siralama
                                - ftab[(runningOrder[j - h]) << 8])             // Karakterin gosterdigi indisin degeri ile bir sonraki indis
                                > (ftab[((vv) + 1) << 8] - ftab[(vv) << 8])) {  // degeri arasindaki fark karakterin kac defa kullanildigi
                            runningOrder[j] = runningOrder[j - h];
                            j = j - h;
                            if (j <= (h - 1)) {
                                break;
                            }
                        }
                        runningOrder[j] = vv;                                   // runningOrder karakterin kacinci sirada islenecegi
                    }
                } while (h != 1);
            }

            for (i = 0; i <= 255; i++) {                                        // Ana siralama dongusu

                ss = runningOrder[i];                                           // En kucukten islemeye basla

                for (j = 0; j <= 255; j++) {                                    // Onceki islemler siralamanin cogunu yaptigindan kucuk parcalar 
                    sb = (ss << 8) + j;                                         // halinde [ss,j] quicksort ile siralamamiz yeterlidir.
                    if (!((ftab[sb] & SETMASK) == SETMASK)) {
                        int lo = ftab[sb] & CLEARMASK;                          // Bu karakterden onceki karakterler sayisi
                        int hi = (ftab[sb + 1] & CLEARMASK) - 1;                // + bu sayinin tekrar sayisi
                        if (hi > lo) {
                            qSort3(lo, hi, 2);
                            numQSorted += (hi - lo + 1);
                            if (workDone > workLimit && firstAttempt) {
                                return;
                            }
                        }
                        ftab[sb] |= SETMASK;                                    // Siralandigina dair 21. bite isaret konur
                    }
                }

                bigDone[ss] = true;                                             // ss siralama tamamlandi.
                if (i < 255) {                                                  // Bu sart son islem icin overshoot alani guncellemeyi atlar.
                    int bbStart  = ftab[ss << 8] & CLEARMASK;                   // Karakterin indisi
                    int bbSize   = (ftab[(ss + 1) << 8] & CLEARMASK) - bbStart; // Karakter tekrar sayisi
                    int shifts   = 0;
                    while ((bbSize >> shifts) > 65534) {                        // Tekrar siniri
                        shifts++;
                    }
                    for (j = 0; j < bbSize; j++) {
                        int a2update = zptr[bbStart + j];                       // Karakterin ftab indisinden kendisi bulunur
                        int qVal = (j >> shifts);                               // Karakterin degeri
                        quadrant[a2update] = qVal;                              // quadrant kayit edilir
                        if (a2update < NUM_OVERSHOOT_BYTES) {                   // overshoot quadrant hesaplama
                             quadrant[a2update + last + 1] = qVal;
                        }
                    }

                    if (!(((bbSize - 1) >> shifts) <= 65535)) {
                        panic();
                    }
                }

                for (j = 0; j <= 255; j++) {                                    // t!=ss, [t,ss] icin siralanmis parcalari tarar
                    copy[j] = ftab[(j << 8) + ss] & CLEARMASK;
                }

                for (j = ftab[ss << 8] & CLEARMASK;                             // Siralanmamis parcalari bir onceki degere esitler
                     j < (ftab[(ss + 1) << 8] & CLEARMASK); j++) {
                    c1 = block[zptr[j]];
                    if (!bigDone[c1]) {
                        zptr[copy[c1]] = zptr[j] == 0 ? last : zptr[j] - 1;     
                        copy[c1]++;
                    }
                }

                for (j = 0; j <= 255; j++) {
                    ftab[(j << 8) + ss] |= SETMASK;                             // ftab siralanmis olarak isaretlenir
                }
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Blok siralama istenilen denemede basarili olamazsa rastgelelenir
     */
    private void randomiseBlock() {
        int i;
        int rNToGo = 0;
        int rTPos  = 0;
        for (i = 0; i < 256; i++) {
            inUse[i] = false;                                                   // Kullanilan karakterler sifirlanir
        }
        for (i = 0; i <= last; i++) {
            if (rNToGo == 0) {
                rNToGo = (char) rNums[rTPos];                                   // Rastgeleleme sayisi alinir
                rTPos++;                                                        // Rastgele sayi pozisyonu guncellenir
                if (rTPos == 512) {
                    rTPos = 0;
                }
            }
            rNToGo--;
            block[i + 1] ^= ((rNToGo == 1) ? 1 : 0);                            // 16 bit isaretli sayilarla calisir
            block[i + 1] &= 0xFF;                                               // Bloktaki verinin ilk biti genellikle 0 ile XOR lanir
            inUse[block[i + 1]] = true;                                         // Karakter kullaniliyor isaretlenir
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Blogu Burrows-Wheeler Donusumu yapar.
     */
    private void doReversibleTransformation() {
        int i;

        workLimit = workFactor * last;
        workDone = 0;
        blockRandomised = false;
        firstAttempt = true;

         mainSort();                                                             // Ana siralama metodu
        if (workDone > workLimit && firstAttempt) {
            randomiseBlock();                                                   // Siralama yapilamazsa rasgeleleme
            workLimit = workDone = 0;
            blockRandomised = true;
            firstAttempt = false;
            mainSort();                                                         // Siralama tekrar denenir
        }
        origPtr = -1;
        for (i = 0; i <= last; i++) {
            if (zptr[i] == 0) {
                origPtr = i;                                                    // Baslangic indisi alinir
                break;
            }
        };
        if (origPtr == -1) {
            panic();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Iki karakteri karsilastirir
     * 
     * @param i1 : Ilk karakter indisi
     * @param i2 : Ikinci karakter indisi
     * @return i1>i2
     */
    private boolean fullGtU(int i1, int i2) {
        int k;
        char c1, c2;
        int s1, s2;

        c1 = block[i1 + 1];                                                     // Iki karakteri alir
        c2 = block[i2 + 1];
        if (c1 != c2) {                                                         // Ayni karakterler degilse karsilastirma
            return (c1 > c2);
        }
        i1++;
        i2++;
        c1 = block[i1 + 1];                                                     // Ikinci deneme
        c2 = block[i2 + 1];
        if (c1 != c2) {
            return (c1 > c2);
        }
        i1++;
        i2++;
        c1 = block[i1 + 1];                                                     // Ucuncu deneme
        c2 = block[i2 + 1];
        if (c1 != c2) {
            return (c1 > c2);
        }
        i1++;
        i2++;
        c1 = block[i1 + 1];                                                     // Dorduncu deneme
        c2 = block[i2 + 1];
        if (c1 != c2) {
            return (c1 > c2);
        }
        i1++;
        i2++;
        c1 = block[i1 + 1];                                                     // Besinci deneme
        c2 = block[i2 + 1];
        if (c1 != c2) {
            return (c1 > c2);
        }
        i1++;
        i2++;
        c1 = block[i1 + 1];                                                     // Altinci deneme
        c2 = block[i2 + 1];
        if (c1 != c2) {
            return (c1 > c2);
        }
        i1++;
        i2++;
        k = last + 1;
        do {                                                                    // Alti denemede farkli degerler bulunamazsa
            c1 = block[i1 + 1];                                                 // Donguye girer ve hem deger hem quandrant karsilastirma yapar
            c2 = block[i2 + 1];
            if (c1 != c2) {
                return (c1 > c2);
            }
            s1 = quadrant[i1];
            s2 = quadrant[i2];
            if (s1 != s2) {
                return (s1 > s2);
            }
            i1++;
            i2++;

            c1 = block[i1 + 1];
            c2 = block[i2 + 1];
            if (c1 != c2) {
                return (c1 > c2);
            }
            s1 = quadrant[i1];
            s2 = quadrant[i2];
            if (s1 != s2) {
                return (s1 > s2);
            }
            i1++;
            i2++;

            c1 = block[i1 + 1];
            c2 = block[i2 + 1];
            if (c1 != c2) {
                return (c1 > c2);
            }
            s1 = quadrant[i1];
            s2 = quadrant[i2];
            if (s1 != s2) {
                return (s1 > s2);
            }
            i1++;
            i2++;

            c1 = block[i1 + 1];
            c2 = block[i2 + 1];
            if (c1 != c2) {
                return (c1 > c2);
            }
            s1 = quadrant[i1];
            s2 = quadrant[i2];
            if (s1 != s2) {
                return (s1 > s2);
            }
            i1++;
            i2++;

            if (i1 > last) {                                                    // Blok sonuna gelirse basa doner
                i1 -= last;
                i1--;
            };
            if (i2 > last) {
                i2 -= last;
                i2--;
            };

            k -= 4;
            workDone++;
        } while (k >= 0);

        return false;
    }

    private int[] incs = {1, 4, 13, 40, 121, 364, 1093, 3280,
                           9841, 29524, 88573, 265720,
                           797161, 2391484};
////////////////////////////////////////////////////////////////////////////////
    /**
     * Arsivleme icin bellek ayrimi yapar
     */
    private void allocateCompressStructures () {
        int n = baseBlockSize * blockSize100k;
        block = new char[(n + 1 + NUM_OVERSHOOT_BYTES)];
        quadrant = new int[(n + NUM_OVERSHOOT_BYTES)];
        zptr = new int[n];
        ftab = new int[65537];

        if (block == null || quadrant == null || zptr == null
            || ftab == null) {
             // Toplam ayrilan bellek alani bellek boyutunu geciyorsa, gunumuzde 
            // pek mumkun degil
            // int totalDraw = (n + 1 + NUM_OVERSHOOT_BYTES) + (n + NUM_OVERSHOOT_BYTES) + n + 65537;
            // compressOutOfMemory ( totalDraw, n );
        }
        /*
         * Kodlama tablolarini hesaplarken MTF degerlerini saklayacak bir yere 
         * ihtiyac vardir. zptr dizisine koyabiliriz. Ancak bir short diziye 
         * sigarlar, bu yuzden szptr'yi kullaniriz. MTF degerlerinin islemleri 
         * sirasinda cache kullanimini azaltiriz. %1 oraninda hiz kazandirir.
         * szptr = zptr;
         */
        szptr = new short[2 * n];
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * MTF degerlerini uretir ve ikinci RLE adimini uygular.
     */
    private void generateMTFValues() {
        char[] yy = new char[256];
        int  i, j;
        char tmp;
        char tmp2;
        int zPend;
        int wr;
        int EOB;

        makeMaps();                                                             // Haritalari yaratir
        EOB = nInUse + 1;

        for (i = 0; i <= EOB; i++) {
            mtfFreq[i] = 0;
        }

        wr = 0;
        zPend = 0;
        for (i = 0; i < nInUse; i++) {
            yy[i] = (char) i;
        }


        for (i = 0; i <= last; i++) {
            char ll_i;

            ll_i = unseqToSeq[block[zptr[i]]];                                  // Karakterin sirasi

            j = 0;
            tmp = yy[j];
            while (ll_i != tmp) {                                               // Karakteri dizinin basina al
                j++;
                tmp2 = tmp;
                tmp = yy[j];
                yy[j] = tmp2;
            };
            yy[0] = tmp;

            if (j == 0) {                                                       // Ayni karakter ardarda gelirse
                zPend++;                                                        // Ikinci RLE adimi
            } else {                                                            // Tekrar sayisi tutulur
                if (zPend > 0) {                                                // Farkli bir karakter geldiyse
                    zPend--;                                                    
                    while (true) {
                        switch (zPend % 2) {
                        case 0:                                                 // Bir tekrar varsa
                            szptr[wr] = (short) RUNA;                           // 0 isareti konur
                            wr++;
                            mtfFreq[RUNA]++;                                    // MTF frekansi 0 indisi bir artar
                            break;
                        case 1:
                            szptr[wr] = (short) RUNB;                           // Iki tekrar varsa
                            wr++;                                               // 1 isareti konur
                            mtfFreq[RUNB]++;
                            break;                                              // MTF frekansi 1 indisi bir artar
                        };
                        if (zPend < 2) {
                            break;
                        }
                        zPend = (zPend - 2) / 2;
                    };
                    zPend = 0;
                }
                szptr[wr] = (short) (j + 1);                                    // Indis degeri yazilir
                wr++;
                mtfFreq[j + 1]++;                                               // MTF frekansi bir artirilir
            }
        }

        if (zPend > 0) {                                                        // Son karakter tekrar ise
            zPend--;
            while (true) {
                switch (zPend % 2) {
                case 0:
                    szptr[wr] = (short) RUNA;
                    wr++;
                    mtfFreq[RUNA]++;
                    break;
                case 1:
                    szptr[wr] = (short) RUNB;
                    wr++;
                    mtfFreq[RUNB]++;
                    break;
                }
                if (zPend < 2) {
                    break;
                }
                zPend = (zPend - 2) / 2;
            }
        }

        szptr[wr] = (short) EOB;
        wr++;
        mtfFreq[EOB]++;

        nMTF = wr;
    }
}


