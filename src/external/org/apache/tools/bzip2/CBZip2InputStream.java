/*
 * Keiron Liddle, Aftex Software<keiron@aftexsw.com>
 */
package external.org.apache.tools.bzip2;

import java.io.InputStream;
import java.io.IOException;

/**
 * Bzip2 formatindan herhangi bir stream gibi okuma yapmak icin arsiv acan bir 
 * input sterami
 * 
 * Acma buyuk miktarda bellek alani ihtiyac duyar. Bu yuzden close() metodunu 
 * CBZip2InputStreame ayrilmis belleği biraktirmak en kısa surede cagirmaliyiz.
 *
 * CBZip2InputStream tek byte read() metodu ile arsivlenmis kaynak streamden 
 * okuma yapar. Bu yuzden bir tamponlanmis kaynak stream kullanimina gidilir.
 */
public class CBZip2InputStream extends InputStream implements BZip2Constants {
    /**
     * Bozuk arsiv CRC de hata yazdirir
     * 
     * @throws java.io.IOException
     */
    private static void reportCRCError() throws IOException {
       // Dogru yolu bir hata firlatmaktir.
        //throw new IOException("crc error");

        // Bu sinifin eski versiyonu gibi sadece mesaj yazdirir
        System.err.println("BZip2 CRC error");
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Verilen deger kadar byte tampona okur ve atlar
     * 
     * @param n : Atlanacak boyut
     * @return
     * @throws java.io.IOException
     */
    public long skips(long n) throws IOException {
	long remaining = n;
	if (skipBuf == null)
	    skipBuf = new byte[skipBufferSize];
        for ( ; remaining > 0 ; ) {
                int numRead = this.read( skipBuf, 0, ( remaining > skipBuf.length ? skipBuf.length : (int) remaining ) );
                if ( numRead == -1 )
                        break;
                remaining -= numRead;
                }
        return ( n - remaining );
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Kalan byte miktarini dondurur
     * 
     * @return
     * @throws java.io.IOException
     */
    public int available() throws IOException {
	return 0 ;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Isaretleme yapar. Offset ayarlanmasini bekliyor
     * 
     * @param readlimit
     */
    public synchronized void mark(int readlimit) {}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Isaretlemeyi eski haline cevirir. Offset ayarlanmasini bekliyor
     * 
     * @throws java.io.IOException
     */
    public synchronized void reset() throws IOException {
	throw new IOException("mark/reset not supported");
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Isaretleme destekleniyor mu? Henuz yapilmadi
     *  
     * @return
     */
    public boolean markSupported() {
	return false;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Kullanilan karakterlerin haritasindan daha iyi bir harita yaratir
     */
    private void makeMaps() {
        final boolean[] inUse   = this.data.inUse;
        final byte[] seqToUnseq = this.data.seqToUnseq;
        int nInUseShadow = 0;
        for (int i = 0; i < 256; i++) {                                         // Eger i karakteri kullaniliyorsa
            if (inUse[i])                                                       // Suandaki indise i karakteri atilir
                seqToUnseq[nInUseShadow++] = (byte) i;                          // sonraki indise gecilir
        }
        this.nInUse = nInUseShadow;
    }
////////////////////////////////////////////////////////////////////////////////
    private final int skipBufferSize = 2048;
    private byte[] skipBuf;
    private int  last;                                                          // Bloktaki son karakterin indisi, yani blok buyukulugu = last + 1
    private int  origPtr;                                                       // Siralama sonrasi orijinal dizinin zptr[] icindeki indisi
    private int blockSize100k;                                                  // Her zaman 0...9 arasinda olmali. Suanki blok boyutu 100000 * bu sayi
    private boolean blockRandomised;                                            // Blok rastgelelenmis mi
    private int bsBuff;                                                         // Karakter tamponu
    private int bsLive;                                                         // Karakter tamponu boyutu
    private final CRC crc = new CRC();                                          // CRC değeri
    private int nInUse;                                                         // Kullanilan karakter sayisi
    private InputStream in;                                                     // InputStream
    private int currentChar = -1;                                               // Suanki karakter
    private static final int EOF                  = 0;                          // Hangi alt sinifin cagirilacagini gosteren sabitler
    private static final int START_BLOCK_STATE = 1;
    private static final int RAND_PART_A_STATE = 2;
    private static final int RAND_PART_B_STATE = 3;
    private static final int RAND_PART_C_STATE = 4;
    private static final int NO_RAND_PART_A_STATE = 5;
    private static final int NO_RAND_PART_B_STATE = 6;
    private static final int NO_RAND_PART_C_STATE = 7;
    private int currentState = START_BLOCK_STATE;                               // Suanki durum usttekilerden birisidir
    private int storedBlockCRC, storedCombinedCRC;                              // Tek ve toplam blok CRC
    private int computedBlockCRC, computedCombinedCRC;                          // Tek ve toplam hesaplanmis blok CRC
    private int su_count;                                                       // Degiskenler setup* metodlari tarafindan kullanilir
    private int su_ch2;
    private int su_chPrev;
    private int su_i2;
    private int su_j2;
    private int su_rNToGo;
    private int su_rTPos;
    private int su_tPos;
    private char su_z;
    private CBZip2InputStream.Data data;                                        // Tum bellek yiyen kisim. initBlock() tarafindan baslatilir
////////////////////////////////////////////////////////////////////////////////
    /**
     * Belli streamden okunan bytelari acan yeni bir CBZip2InputStream yaratir
     * 
     * BZip2 basliklari "BZ" magic değerleri ile isaretlendiginden kurucu 
     * streamdeki sonraki byte'in magicden sonraki ilk byte olmasini bekler
     * Bu yuzden cagiricilar ilk iki byte atlamak zorundadir. Yoksa hata atilir
     * 
     * @throws IOException : Eğer stream icerigi yanlis formattaysa veya bir I/O hatasi varsa
     * @throws NullPointerException : Eğer inputStream bossa
     */
    public CBZip2InputStream(final InputStream in) throws IOException {
        super();
        this.in = in;
        init();
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * InputStream bos degilse okuma yapan metodu cagirir
     * 
     * @return okuma yapan metodun gonderdigi deger
     * @throws java.io.IOException : Stream bossa
     */
    public int read() throws IOException {
        if (this.in != null) {
            return read0();
        } else {
            throw new IOException("stream closed");
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * len degeri kadar okuma yapilip offs indisinden baslayarak dest dizinine 
     * yazilir
     * 
     * @param dest : Yazilacak dizin
     * @param offs : Yazmaya baslanacak indis
     * @param len : Okuma miktari
     * @return istenen degerde okuma olduysa -1, yoksa okunan miktar
     * @throws java.io.IOException
     */
    public int read(final byte[] dest, final int offs, final int len) 
            throws IOException {
        if (offs < 0) {                                                         // Offset sifirdan kucuk olamaz
            throw new IndexOutOfBoundsException("offs(" + offs + ") < 0.");
        }
        if (len < 0) {                                                          // Uzunluk sifirdan kucuk olamaz
            throw new IndexOutOfBoundsException("len(" + len + ") < 0.");
        }
        if (offs + len > dest.length) {                                         // Offset ve uzunluk toplami dizinin boyutunu gecemez
            throw new IndexOutOfBoundsException("offs(" + offs + ") + len("
                                                + len + ") > dest.length("
                                                + dest.length + ").");
        }
        if (this.in == null) {                                                  // Stream bossa 
            throw new IOException("stream closed");
        }

        final int hi = offs + len;                                              // Okuma siniri
        int destOffs = offs;                                                    // Okuma baslangici
        for (int b; (destOffs < hi) && ((b = read0()) >= 0);) {                 // Sinira kadar okuma yapilir
            dest[destOffs++] = (byte) b;                                        // Okunan deger diziye atilir
        }

        return (destOffs == offs) ? -1 : (destOffs - offs);                     // Okuma tamsa -1, yoksa okunan miktar 
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Suanki duruma gore uygun metod ile okuma yapar.
     * 
     * @return EOF ise -1, yoksa karakter
     * @throws java.io.IOException
     */
    private int read0() throws IOException {
        final int retChar = this.currentChar;

        switch (this.currentState) {                                            // Suanki durum isaretcisine gore bir metod cagirir
        case EOF:
            return -1;

        case START_BLOCK_STATE:                                                 // Bu durumlar icin buraya hic gelmemesi gerekli 
            throw new IllegalStateException();

        case RAND_PART_A_STATE:
            throw new IllegalStateException();

        case RAND_PART_B_STATE:                                                 // Rastgelelenmis karakter cozulmus ve tekrar kontrolu icin 
            setupRandPartB();                                                   // B parcasina gidilir
            break;

        case RAND_PART_C_STATE:                                                 // Tekrar degeri kadar veri yazilincaya kadar C parcasina gidilir
            setupRandPartC();                                                   // Yani RLE kodu cozulur
            break;

        case NO_RAND_PART_A_STATE:                                              
            throw new IllegalStateException();

        case NO_RAND_PART_B_STATE:                                              // Karakter cozulmus ve tekrar kontrolu icin B parcasina gidilir
            setupNoRandPartB();
            break;

        case NO_RAND_PART_C_STATE:                                              // Tekrar degeri kadar ceri yazilincaya kadar C parcasina gidilir
            setupNoRandPartC();
            break;

        default:
            throw new IllegalStateException();
        }

        return retChar;                                                         // Durumlarda okunan deger yazilmak icin dondurulur
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Arsivleme yontemi ve blok boyutu magic degerleri kontrolu yapar. 
     * 
     * @throws java.io.IOException
     */
    private void init() throws IOException {
        int magic2 = this.in.read();
        if (magic2 != 'h') {
            throw new IOException("Stream is not BZip2 formatted: expected 'h'"
                                  + " as first byte but got '" + (char) magic2
                                  + "'");
        }

        int blockSize = this.in.read();
        if ((blockSize < '1') || (blockSize > '9')) {
            throw new IOException("Stream is not BZip2 formatted: illegal "
                                  + "blocksize " + (char) blockSize);
        }

        this.blockSize100k = blockSize - '0';

        initBlock();
        setupBlock();
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Baslangic(pi) ve bitis(karekok(pi)) magic degerleri kontrolu yapar.
     * Bellek ayirir.
     * 
     * @throws java.io.IOException
     */
    private void initBlock() throws IOException {
        char magic0 = bsGetUByte();
        char magic1 = bsGetUByte();
        char magic2 = bsGetUByte();
        char magic3 = bsGetUByte();
        char magic4 = bsGetUByte();
        char magic5 = bsGetUByte();

        if (magic0 == 0x17 &&
            magic1 == 0x72 &&
            magic2 == 0x45 &&
            magic3 == 0x38 &&
            magic4 == 0x50 &&
            magic5 == 0x90) {
            complete();                                                         // Dosya sonu
        } else if (magic0 != 0x31 || 
                   magic1 != 0x41 || 
                   magic2 != 0x59 || 
                   magic3 != 0x26 || 
                   magic4 != 0x53 || 
                   magic5 != 0x59   
                   ) {
            this.currentState = EOF;
            throw new IOException("bad block header");
        } else {
            this.storedBlockCRC = bsGetInt();                                   // CRC degeri alinir
            this.blockRandomised = bsR(1) == 1;                                 // Rasgeleleme degeri alinir
            if (this.data == null) {                                            // Bellek ayrimi yapici yerine burada yapilir. 
                this.data = new Data(this.blockSize100k);                       // Boylece eger girdi bossa bellek ayrilmaz
            }

            //currBlockNo++;
            getAndMoveToFrontDecode();                                          // Karakterler 50 parçalar halinde okunur ve yazilir

            this.crc.initialiseCRC();
            this.currentState = START_BLOCK_STATE;
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Bit bazinda CRC islemleri ile hesaplanan ve kayitli CRC kontrolu yapar.
     * 
     * @throws java.io.IOException : Hatali CRC varsa
     */
    private void endBlock() throws IOException {
        this.computedBlockCRC = this.crc.getFinalCRC();
        if (this.storedBlockCRC != this.computedBlockCRC) {                     // Hesaplanan CRC kayitli CRC ile esit degilse hata
            this.computedCombinedCRC                                            // 32. bit ile 1. bit veyalanir
                = (this.storedCombinedCRC << 1)                                 // XOR lanip aralarindaki farklar alinir
                | (this.storedCombinedCRC >>> 31);                              // Hata bildirilir
            this.computedCombinedCRC ^= this.storedBlockCRC;

            reportCRCError();
        }

        this.computedCombinedCRC
            = (this.computedCombinedCRC << 1)
            | (this.computedCombinedCRC >>> 31);
        this.computedCombinedCRC ^= this.computedBlockCRC;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Dosya sonuna ulasilirsa kayitli CRC alir, suanki durumu EOF ayarlar, 
     * veriyi bosaltir.
     * 
     * @throws java.io.IOException : Hatali CRC varsa
     */
    private void complete() throws IOException {
        this.storedCombinedCRC = bsGetInt();
        this.currentState = EOF;
        this.data = null;

        if (this.storedCombinedCRC != this.computedCombinedCRC) {
            reportCRCError();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Stream kapatma ve veri bosaltma islemleri yapilir
     * 
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        InputStream inShadow = this.in;
        if (inShadow != null) {
            try {
                if (inShadow != System.in) {
                    inShadow.close();
                }
            } finally {
                this.data = null;
                this.in = null;
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Verilen değer kadar okama yapar.
     *  
     * @param n : Okunacak bit sayisi
     * @return okunan deger
     * @throws java.io.IOException
     */
    private int bsR(final int n) throws IOException {
        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        if (bsLiveShadow < n) {
            final InputStream inShadow = this.in;                               // Istenen deger kadar okuma yapilir
            do {
                int thech = inShadow.read();                                    // Streamden veri okunur

                if (thech < 0) {
                    throw new IOException("unexpected end of stream");
                }

                bsBuffShadow = (bsBuffShadow << 8) | thech;                     // Veri tampona atilir
                bsLiveShadow += 8;                                              // Tampon boyutu artar
            } while (bsLiveShadow < n);

            this.bsBuff = bsBuffShadow;
        }

        this.bsLive = bsLiveShadow - n;                                         // Okunan veri boyutu tampon boyutundan cikar
        return (bsBuffShadow >> (bsLiveShadow - n)) & ((1 << n) - 1);           // Bit bazinda okuma yapilir
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Bir 8 bit okuma yapar, bir bit dondurur
     * 
     * @return okunan bit degeri 0 veya 1
     * @throws java.io.IOException
     */
    private boolean bsGetBit() throws IOException {
        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        if (bsLiveShadow < 1) {
            int thech = this.in.read();

            if (thech < 0) {
                throw new IOException("unexpected end of stream");
            }

            bsBuffShadow = (bsBuffShadow << 8) | thech;
            bsLiveShadow += 8;
            this.bsBuff = bsBuffShadow;
        }

        this.bsLive = bsLiveShadow - 1;
        return ((bsBuffShadow >> (bsLiveShadow - 1)) & 1) != 0;
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * 8 bit okuma yapar.
     * 
     * @return Okunan bitlerin temsil ettigi karakter
     * @throws java.io.IOException
     */
    private char bsGetUByte() throws IOException {
        return (char) bsR(8);
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * 32 bit okuma yapar. 
     * 
     * @return Okunan bitlerin temsil ettigi sayi
     * @throws java.io.IOException
     */
    private int bsGetInt() throws IOException {
        return (((((bsR(8) << 8) | bsR(8)) << 8) | bsR(8)) << 8) | bsR(8);
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * createHuffmanDecodingTables() tarafindan cagirilir. Alfa degerlerini 
     * kullanarak base degerlerinin hesaplar. Karakterin huffman tablosuna
     * katkisini tutar.
     * 
     * @param limit : base hesabinda kullanilir (bos gelir)
     * @param base : Huffman tablosu (bos gelir)
     * @param perm : Sirali alfa degerlerinin indisleri (bos gelir)
     * @param length : Tablonun alfa degerleri
     * @param minLen : Alfa alt siniri
     * @param maxLen : Alfa ust siniri
     * @param alphaSize : Alfa boyutu
     */
    private static void hbCreateDecodeTables(final int[] limit,
                                             final int[] base,
                                             final int[] perm,
                                             final char[] length,
                                             final int minLen,
                                             final int maxLen,
                                             final int alphaSize) {
        for (int i = minLen, pp = 0; i <= maxLen; i++) {                        // Alfa degerlerinin siralanmis indis degerlerini tutar
            for (int j = 0; j < alphaSize; j++) {                               // perm[0] en dusuk alfa degerinin indisi
                if (length[j] == i) {                                           // perm[alphaSize-1] en yuksek alfa degerinin indisi
                    perm[pp++] = j;                                             // length dizi icerigi degismez
                }
            }
        }

        for (int i = MAX_CODE_LEN; --i > 0;) {
            base[i] = 0;
            limit[i] = 0;
        }

        for (int i = 0; i < alphaSize; i++) {                                   // base dizisinin alfa degeri+1 indisteki degeri 1 aratar
            base[length[i] + 1]++;
        }

        for (int i = 1, b = base[0]; i < MAX_CODE_LEN; i++) {                   // base dizisi kademeli olarak toplanir
            b += base[i];
            base[i] = b;
        }

        for (int i = minLen, vec = 0, b = base[i]; i <= maxLen; i++) {          // vec = baseler arasi fark katari
            final int nb = base[i + 1];
            vec += nb - b;
            b = nb;
            limit[i] = vec - 1;                                                 // karakterin agirligi
            vec <<= 1;
        }

        for (int i = minLen + 1; i <= maxLen; i++) {
            base[i] = ((limit[i - 1] + 1) << 1) - base[i];                      // base[i] = i-1. an icin vec degeri - base[i]
        }
    }
///////////////////////////////////////////////////////////////////////////
    /**
     * Huffman haritalama ve secici bilgilerini alir. Alfa degerlerinin hesaplar
     * 
     * @throws java.io.IOException
     */
    private void recvDecodingTables() throws IOException {
        final Data dataShadow     = this.data;
        final boolean[] inUse     = dataShadow.inUse;
        final byte[] pos          = dataShadow.recvDecodingTables_pos;
        final byte[] selector     = dataShadow.selector;
        final byte[] selectorMtf  = dataShadow.selectorMtf;

        int inUse16 = 0;
        for (int i = 0; i < 16; i++) {                                          // Huffman haritalama tablosunu elde eder
            if (bsGetBit()) {                                                   // 16 parca icin 16 bit
                inUse16 |= 1 << i;
            }
        }

        for (int i = 256; --i >= 0;) {
            inUse[i] = false;
        }

        for (int i = 0; i < 16; i++) {
            if ((inUse16 & (1 << i)) != 0) {
                final int i16 = i << 4;
                for (int j = 0; j < 16; j++) {
                    if (bsGetBit()) {
                        inUse[i16 + j] = true;                                  // Kullanilan sembollerin haritasi
                    }
                }
            }
        }

        makeMaps();                                                             // Daha iyi bir harita yaratilir
        final int alphaSize = this.nInUse + 2;                                  // Alfa boyutu = kullanilan karakter sayisi+2

        final int nGroups = bsR(3);                                             // Simdi seciciler, once kac farkli huffman tablosu kullanildigi
        final int nSelectors = bsR(15);                                         // Sonra huffman tablarinin swap bilgileri

        for (int i = 0; i < nSelectors; i++) {                                  // Seciciler kadar
            int j = 0;
            while (bsGetBit()) {                                                // MTFlenmis huffman tablolarinin bit uzunlugu
                j++;
            }
            selectorMtf[i] = (byte) j;                                          // Secici degeri kayit edilir
        }
        for (int v = nGroups; --v >= 0;) {                                      // Seciciler icin MTF kod cozme
            pos[v] = (byte) v;                                                  // Simdilik pos = indis degeri - 1
        }

        for (int i = 0; i < nSelectors; i++) {                                  // Huffman tablosu secilir
            int v = selectorMtf[i] & 0xff;                                      // MTF isleminin sonucu olarak
            final byte tmp = pos[v];                                            // Neredeyse herzaman v sifirdir
            while (v > 0) {
                pos[v] = pos[v - 1];
                v--;
            }
            pos[0] = tmp;                                                       // Deger basa alinir
            selector[i] = tmp;                                                  // MTF kod cozme sonucu 
        }

        final char[][] len  = dataShadow.temp_charArray2d;                      // Iki boyutlu char dizi
        for (int t = 0; t < nGroups; t++) {
            int curr = bsR(5);                                                  // Simdi kodlama tablolari
            final char[] len_t = len[t];                                        // Huffman deltalari icin baslangic uzunlugu
            for (int i = 0; i < alphaSize; i++) {                               // Vertorel gosterim
                while (bsGetBit()) {                                            // Alfa boyutu kadar
                    curr += bsGetBit() ? -1 : 1;                                // Guncellenecek mi
                }                                                               // 0=> azalt, 1=> artir
                len_t[i] = (char) curr;                                         // Alfayi kaydet
            }
        }
        createHuffmanDecodingTables(alphaSize, nGroups);                        // En son olarak Huffman tablolarini yarat
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * recvDecodingTables() tarafindan cagirilir. Herbir huffman tablosu icin 
     * alfaya gore limitleri ayarlar, tablo yaratan metod cagirilir.
     * 
     * @param alphaSize : Alfa boyutu
     * @param nGroups : Kullanilan Huffman tablosu sayisi
     */
    private void createHuffmanDecodingTables(final int alphaSize,
                                             final int nGroups) {
        final Data dataShadow = this.data;
        final char[][] len  = dataShadow.temp_charArray2d;
        final int[] minLens = dataShadow.minLens;
        final int[][] limit = dataShadow.limit;
        final int[][] base  = dataShadow.base;
        final int[][] perm  = dataShadow.perm;

        for (int t = 0; t < nGroups; t++) {                                     // Her bir tablo icin
            int minLen = 32;                                                    // Boyut sinirlari
            int maxLen = 0;
            final char[] len_t = len[t];                                        // Vektor alinir
            for (int i = alphaSize; --i >= 0;) {                                // Enson alfadan baslanir
                final char lent = len_t[i];                                     // Alfa alinir
                if (lent > maxLen) {
                    maxLen = lent;
                }
                if (lent < minLen) {
                    minLen = lent;                                              // en kucuk karakter indisi alfaya ayarlanir
                }
            }
            hbCreateDecodeTables(limit[t], base[t], perm[t], len[t], minLen,    // Tablo yaratma cagirilir
                                 maxLen, alphaSize);
            minLens[t] = minLen;                                                // Bu tablonun tabani kaydedilir
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Baslangic indisini alir. Huffman agacini olusturur. RLE kodlari cozulur. 
     * Karakterler okunur ve yazilir.
     * 
     * @throws java.io.IOException
     */
    private void getAndMoveToFrontDecode() throws IOException {
        this.origPtr = bsR(24);                                                 // Baslangic indisini alir
        recvDecodingTables();                                                   // Huffman agaclari olusturulur

        final InputStream inShadow = this.in;
        final Data dataShadow   = this.data;
        final byte[] ll8        = dataShadow.ll8;
        final int[] unzftab     = dataShadow.unzftab;
        final byte[] selector   = dataShadow.selector;
        final byte[] seqToUnseq = dataShadow.seqToUnseq;
        final char[] yy         = dataShadow.getAndMoveToFrontDecode_yy;
        final int[] minLens     = dataShadow.minLens;
        final int[][] limit     = dataShadow.limit;
        final int[][] base      = dataShadow.base;
        final int[][] perm      = dataShadow.perm;
        final int limitLast     = this.blockSize100k * 100000;
        
        for (int i = 256; --i >= 0;) {                                          
            yy[i] = (char) i;
            unzftab[i] = 0;
        }

        int groupNo     = 0;
        int groupPos    = G_SIZE - 1;                                           // Her elli karakter icin bir tablo
        final int eob   = this.nInUse + 1;
        int nextSym     = getAndMoveToFrontDecode0(0);                          // Ilk alfa degeri alinir
        int bsBuffShadow      = this.bsBuff;
        int bsLiveShadow      = this.bsLive;
        int lastShadow        = -1;
        int zt          = selector[groupNo] & 0xff;                             // Tablo numarasi alinir
        int[] base_zt   = base[zt];                                             // Tablo bilgileri alinir
        int[] limit_zt  = limit[zt];
        int[] perm_zt   = perm[zt];
        int minLens_zt  = minLens[zt];

        while (nextSym != eob) {                                                // Okuma dongusu
            if ((nextSym == RUNA) || (nextSym == RUNB)) {                       // Sembol RLE degeriyse
                int s = -1;

                for (int n = 1; true; n <<= 1) {                                // RUNA ise bir tekrar
                    if (nextSym == RUNA) {
                        s += n;
                    } else if (nextSym == RUNB) {                               // RUNB ise iki tekrar
                        s += n << 1;
                    } else {                                                    // Okunan deger RLE degilse donguden cik
                        break;
                    }

                    if (groupPos == 0) {                                        // Elli karakter olduysa yeni tabloya gecer
                        groupPos    = G_SIZE - 1;                               // Yeni tablo bilgileri alinir
                        zt          = selector[++groupNo] & 0xff;
                        base_zt     = base[zt];
                        limit_zt    = limit[zt];
                        perm_zt     = perm[zt];
                        minLens_zt  = minLens[zt];
                    } else {
                        groupPos--;                                             // Kalan karakter sayisi guncellenir
                    }

                    int zn = minLens_zt;

                    while (bsLiveShadow < zn) {                                 // Veri okunur
                        final int thech = inShadow.read();
                        if (thech >= 0) {
                            bsBuffShadow = (bsBuffShadow << 8) | thech;
                            bsLiveShadow += 8;
                            continue;
                        } else {
                            throw new IOException("unexpected end of stream");
                        }
                    }                                                           // Okunan veri zvec'e atilir
                    int zvec = (bsBuffShadow >> (bsLiveShadow - zn)) & ((1 << zn) - 1);
                    bsLiveShadow -= zn;

                    while (zvec > limit_zt[zn]) {                               // Huffman kodunun okunmasi
                        zn++;
                        while (bsLiveShadow < 1) {
                            final int thech = inShadow.read();
                            if (thech >= 0) {
                                bsBuffShadow = (bsBuffShadow << 8) | thech;
                                bsLiveShadow += 8;
                                continue;
                            } else {
                                throw new IOException("unexpected end of stream");
                            }
                        }
                        bsLiveShadow--;                                         // Huffman kod zvec'e atilir
                        zvec = (zvec << 1) | ((bsBuffShadow >> bsLiveShadow) & 1);
                    }
                    nextSym = perm_zt[zvec - base_zt[zn]];                      // Yeni deger alinir. Buyuk ihtimalle alfa degeri
                }

                final byte ch = seqToUnseq[yy[0]];                              // Kullanilan karakter alinir
                unzftab[ch & 0xff] += s + 1;                                    // Karakterin tekrar sayisi atanir

                while (s-- >= 0) {
                    ll8[++lastShadow] = ch;                                     // Karakter kayit edilir
                }

                if (lastShadow >= limitLast) {                                  // Blok asilirsa
                    throw new IOException("block overrun");
                }
            } else {
                if (++lastShadow >= limitLast) {                                // Hata firlatir
                    throw new IOException("block overrun");
                }

                final char tmp = yy[nextSym - 1];                               // Yeni karakter kayit edilir
                unzftab[seqToUnseq[tmp] & 0xff]++;
                ll8[lastShadow] = seqToUnseq[tmp];

                if (nextSym <= 16) {                                            // Bu dongu acma sirasinda islenir, bu yuzden sisteme yuklenmemek
                    for (int j = nextSym - 1; j > 0;) {                         // Icin cok kucuk boyutta kopyalama icin cagirilir
                        yy[j] = yy[--j];
                    }
                } else {
                    System.arraycopy(yy, 0, yy, 1, nextSym - 1);                // yy dizisi 1 kaydirilir.
                }

                yy[0] = tmp;                                                    // Yeni karakter 0 indisine atilir

                if (groupPos == 0) {                                            // Elli karakter olduysa yeni tabloya gecer
                    groupPos    = G_SIZE - 1;
                    zt          = selector[++groupNo] & 0xff;
                    base_zt     = base[zt];
                    limit_zt    = limit[zt];
                    perm_zt     = perm[zt];
                    minLens_zt  = minLens[zt];
                } else {
                    groupPos--;                                                 // Kalan karakter sayisi guncellenir
                }

                int zn = minLens_zt;

                while (bsLiveShadow < zn) {                                     // Veri okunur
                    final int thech = inShadow.read();
                    if (thech >= 0) {
                        bsBuffShadow = (bsBuffShadow << 8) | thech;
                        bsLiveShadow += 8;
                        continue;
                    } else {
                        throw new IOException("unexpected end of stream");
                    }
                }
                int zvec = (bsBuffShadow >> (bsLiveShadow - zn)) & ((1 << zn) - 1);
                bsLiveShadow -= zn;                                             // Okunan veri zvec'e atilir

                while (zvec > limit_zt[zn]) {
                    zn++;                                                       // Huffman kodunun okunmasi
                    while (bsLiveShadow < 1) {
                        final int thech = inShadow.read();
                        if (thech >= 0) {
                            bsBuffShadow = (bsBuffShadow << 8) | thech;
                            bsLiveShadow += 8;
                            continue;
                        } else {
                            throw new IOException("unexpected end of stream");
                        }
                    }
                    bsLiveShadow--;
                    zvec = (zvec << 1) | ((bsBuffShadow >> bsLiveShadow) & 1);
                }
                nextSym = perm_zt[zvec - base_zt[zn]];                          // Yeni deger alinir. Buyuk ihtimalle yeni karakter
            }
        }

        this.last = lastShadow;                                                 // Bloktaki son karakter
        this.bsLive = bsLiveShadow;                                             // Tampon boyutu
        this.bsBuff = bsBuffShadow;                                             // Tampon guncellenir
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Huffman kodlanmis sıradaki veriyi okur.
     * 
     * @param groupNo : Secici indisi
     * @return verinin alfa degeri
     * @throws java.io.IOException
     */
    private int getAndMoveToFrontDecode0(final int groupNo)
        throws IOException {
        final InputStream inShadow  = this.in;
        final Data dataShadow  = this.data;
        final int zt          = dataShadow.selector[groupNo] & 0xff;            // Hangi tablo
        final int[] limit_zt  = dataShadow.limit[zt];                           // Tablo agirliklari
        int zn = dataShadow.minLens[zt];                                        // Tablo tabani
        int zvec = bsR(zn);                                                     // Veriyi okur
        int bsLiveShadow = this.bsLive;                                         // Tampon boyutunu alir
        int bsBuffShadow = this.bsBuff;                                         // Tamponu alir

        while (zvec > limit_zt[zn]) {                                           // Okunan veri huffman kodu bulunur
            zn++;                                                               // En kucuk agirliktan baslanarak hepsi denenir
            while (bsLiveShadow < 1) {
                final int thech = inShadow.read();

                if (thech >= 0) {
                    bsBuffShadow = (bsBuffShadow << 8) | thech;
                    bsLiveShadow += 8;
                    continue;
                } else {
                    throw new IOException("unexpected end of stream");          // Huffman kod zvec'e atilir.
                }
            }
            bsLiveShadow--;
            zvec = (zvec << 1) | ((bsBuffShadow >> bsLiveShadow) & 1);          // Karakter alfa degeri dondurulur
        }

        this.bsLive = bsLiveShadow;
        this.bsBuff = bsBuffShadow;

        return dataShadow.perm[zt][zvec - dataShadow.base[zt][zn]];
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Yeni blok icin verileri yukler
     * 
     * @throws java.io.IOException
     */
    private void setupBlock() throws IOException {
        if (this.data == null) {
            return;
        }

        final int[] cftab = this.data.cftab;
        final int[] tt    = this.data.initTT(this.last + 1);
        final byte[] ll8  = this.data.ll8;
        cftab[0] = 0;
        System.arraycopy(this.data.unzftab, 0, cftab, 1, 256);                  // Karakter tekrar sayilari cftab'a kopyalanir

        for (int i = 1, c = cftab[0]; i <= 256; i++) {
            c += cftab[i];
            cftab[i] = c;                                                       // Tekrarlar kademeli olarak toplanir
        }

        for (int i = 0, lastShadow = this.last; i <= lastShadow; i++) {
            tt[cftab[ll8[i] & 0xff]++] = i;                                     // Bloktaki tum karakterler icin
        }                                                                       // Kayitli karakter isaretlenir

        if ((this.origPtr < 0) || (this.origPtr >= tt.length)) {
            throw new IOException("stream corrupted");
        }

        this.su_tPos = tt[this.origPtr];
        this.su_count = 0;
        this.su_i2 = 0;
        this.su_ch2 = 256;

        if (this.blockRandomised) {                                             // Blok rastgelelenmis ise
            this.su_rNToGo = 0;
            this.su_rTPos = 0;
            setupRandPartA();
        } else {
            setupNoRandPartA();                                                 // Blok normalse
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Rastgelelenmis kodun cozumu birinci kisim. Tek bir karakter okur ve 
     * gercek haline cevirir. Sonra B parcasina gecer.
     * 
     * @throws java.io.IOException
     */
    private void setupRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {                                          // Blok sonuna ulasilmamissa
            this.su_chPrev = this.su_ch2;                                       // Bir onceki veri saklanir
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;              // Rastgelelenmis veri alinir
            this.su_tPos = this.data.tt[this.su_tPos];                          // Sonraki veri pozisyonu alinir
            if (this.su_rNToGo == 0) {
                this.su_rNToGo = BZip2Constants.rNums[this.su_rTPos] - 1;       // Rastgeleleme sayisi alinir
                if (++this.su_rTPos == 512) {                                   // Rastgele sayi pozisyonu guncellenir
                    this.su_rTPos = 0;
                }
            } else {
                this.su_rNToGo--;                                               // Rastgeleleme sayisi bir azaltilir 
            }
            this.su_ch2 = su_ch2Shadow ^= (this.su_rNToGo == 1) ? 1 : 0;        // Veri gercek haline cevrilir
            this.su_i2++;                                                       // Sayac artirilir
            this.currentChar = su_ch2Shadow;                                    // Suanki karakter saklanir
            this.currentState = RAND_PART_B_STATE;                              // B parcasina gecilir
            this.crc.updateCRC(su_ch2Shadow);                                   // CRC degeri guncellenir
        } else {
            endBlock();
            initBlock();
            setupBlock();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Rastgelelenmemis kodun cozumu birinci kisim. Tek bir karakter okur, 
     * kaydeder ve B parcasina gecer.
     * 
     * @throws java.io.IOException
     */
    private void setupNoRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {                                          // Blok sonuna ulasilmamissa
            this.su_chPrev = this.su_ch2;                                       // Bir onceki veri saklanir
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;              // Veri alinir
            this.su_ch2 = su_ch2Shadow;                                         // Veri saklanir
            this.su_tPos = this.data.tt[this.su_tPos];                          // Sonraki veri pozisyonu alinir
            this.su_i2++;                                                       // Sayac artar
            this.currentChar = su_ch2Shadow;                                    // Suanki karakter saklanir
            this.currentState = NO_RAND_PART_B_STATE;                           // B parcasina gecilir
            this.crc.updateCRC(su_ch2Shadow);                                   // CRC guncellenir
        } else {
            this.currentState = NO_RAND_PART_A_STATE;
            endBlock();
            initBlock();
            setupBlock();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Rastgelelenmis kodun cozumu ikinci kisim. Okunan iki karakteri 
     * karsilastirir. Farkli karakterse karakter sayacini 1 yapar, A parcasina 
     * gecer. Karakterler esitse karakter sayacini artirir. Tekrar ucten 
     * fazlaysa siradaki veri alinir, ikinci bir sayac baslatilir ve C parcasina
     * gecer. Tekrar ucten azsa A parcasina gecer.
     * 
     * @throws java.io.IOException
     */
    private void setupRandPartB() throws IOException {
        if (this.su_ch2 != this.su_chPrev) {                                    // Simdiki karakter oncekiyle ayni degilse ise
            this.currentState = RAND_PART_A_STATE;                              // Birinci kisma geri doner
            this.su_count = 1;                                                  // Karakterin bir kez gectigine dair isaret konur
            setupRandPartA();
        } else if (++this.su_count >= 4) {                                      // Karakter sayaci artirilir. Eger 4 veya daha fazla tekrar varsa
            this.su_z = (char) (this.data.ll8[this.su_tPos] & 0xff);            // Veri alinir
            this.su_tPos = this.data.tt[this.su_tPos];                          // Sonraki veri pozisyonu alinir
            if (this.su_rNToGo == 0) {
                this.su_rNToGo = BZip2Constants.rNums[this.su_rTPos] - 1;       // Rastgeleleme sayisi alinir
                if (++this.su_rTPos == 512) {                                   // Rastgele sayi pozisyonu guncellenir
                    this.su_rTPos = 0;
                }
            } else {
                this.su_rNToGo--;                                               // Rastgeleleme sayisi bir azaltilir 
            }
            this.su_j2 = 0;                                                     // İkinci sayac sifirlanir
            this.currentState = RAND_PART_C_STATE;
            if (this.su_rNToGo == 1) {                                          // Veri gercek haline cevirilir
                this.su_z ^= 1;
            }
            setupRandPartC();
        } else {                                                                // Suanki karakter onceki karakterle ayniysa ama sayac 4 den azsa
            this.currentState = RAND_PART_A_STATE;
            setupRandPartA();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Rastgelelenmis kodun cozumu ucuncu kisim. B de okunan veri kadar 
     * ayni karakter yazilir. Yani birinci kisim RLE kod cozumu yapilir.
     * 
     * @throws java.io.IOException
     */
    private void setupRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {                                           // Eger sayac iki B de okunan veriden kucukse 
            this.currentChar = this.su_ch2;                                     // Suanki karakter saklanir
            this.crc.updateCRC(this.su_ch2);                                    // CRC guncellenir
            this.su_j2++;                                                       // Ikinci sayac artar
        } else {
            this.currentState = RAND_PART_A_STATE;                              // A parcasina gecilir
            this.su_i2++;                                                       // Birinci sayac artar
            this.su_count = 0;                                                  // Karakter sayaci sifirlanir
            setupRandPartA();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Rastgelelenmemis kodun cozumu ikinci kisim. Okunan iki karakteri 
     * karsilastirir. Farkli karakterse A parcasina gecer. Karakterler esitse ve
     * tekrar ucten fazlaysa siradaki veri alinir, ikinci bir sayac baslatilir 
     * ve C parcasina gecer. Tekrar ucten azsa A parcasina gecer.
     *
     * @throws java.io.IOException
     */
    private void setupNoRandPartB() throws IOException {
        if (this.su_ch2 != this.su_chPrev) {                                    // Simdiki karakter oncekiyle ayni degilse ise
            this.su_count = 1;                                                  // Karakterin bir kez gectigine dair isaret konur
            setupNoRandPartA();                                                 // Birinci kisma geri doner
        } else if (++this.su_count >= 4) {                                      // Karakter sayaci artirilir. Eger 4 veya daha fazla tekrar varsa
            this.su_z = (char) (this.data.ll8[this.su_tPos] & 0xff);            // Veri alinir
            this.su_tPos = this.data.tt[this.su_tPos];                          // Sonraki veri pozisyonu alinir
            this.su_j2 = 0;                                                     // Ikinci sayac sifirlanir
            setupNoRandPartC();                                                 // Ucuncu kisima gecilir
        } else {                                                                // Karakterler farkli ise
            setupNoRandPartA();                                                 // Birinci kisima gecer
        }
    }
////////////////////////////////////////////////////////////////////////////////
    /**
     * Rastgelelenmemis kodun cozumu ucuncu kisim. B de okunan veri kadar 
     * ayni karakter yazilir. Yani birinci kisim RLE kod cozumu yapilir.
     * 
     * @throws java.io.IOException
     */
    private void setupNoRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {                                           // Eger sayac iki B de okunan veriden kucukse 
            int su_ch2Shadow = this.su_ch2;
            this.currentChar = su_ch2Shadow;                                    // Suanki karakter saklanir
            this.crc.updateCRC(su_ch2Shadow);                                   // CRC guncellenir
            this.su_j2++;                                                       // Ikinci sayac artar
            this.currentState = NO_RAND_PART_C_STATE;                           // C parcasinda devam edilir
        } else {
            this.su_i2++;
            this.su_count = 0;
            setupNoRandPartA();
        }
    }
////////////////////////////////////////////////////////////////////////////////
    private static final class Data extends Object {                            // 900000 Blok boyutu ile
        final boolean[] inUse   = new boolean[256];                             //      256 byte : Kullanilan karakter gosterici
        final byte[] seqToUnseq   = new byte[256];                              //      256 byte : Kullanilan karakter dizisi
        final byte[] selector     = new byte[MAX_SELECTORS];                    //    18002 byte : Tablo secici
        final byte[] selectorMtf  = new byte[MAX_SELECTORS];                    //    18002 byte : Tablo secici MTF'lenmis
                                                                                // Acma sirasinda verinin gecisini saglamak amaciyla Freq tablolari toplandi
        final int[] unzftab = new int[256];                                     //     1024 byte : Karakterlerin artarda tekrar sayilari
        final int[][] limit = new int[N_GROUPS][MAX_ALPHA_SIZE];                //     6192 byte : Huffman agaci kodlari
        final int[][] base  = new int[N_GROUPS][MAX_ALPHA_SIZE];                //     6192 byte : Sade karakterin agaca katkisi
        final int[][] perm  = new int[N_GROUPS][MAX_ALPHA_SIZE];                //     6192 byte : Sirali alfa indisleri
        final int[] minLens = new int[N_GROUPS];                                //       24 byte : En kucuk huffman kod
        final int[]     cftab     = new int[257];                               //     1028 byte : Artis kayitlarini tutar
        final char[]    getAndMoveToFrontDecode_yy = new char[256];             //      512 byte : SeqToUnseq ile karakter tespitine yarar
        final char[][]  temp_charArray2d  = new char[N_GROUPS][MAX_ALPHA_SIZE]; //     3096 byte : Alfa degerleri
        final byte[] recvDecodingTables_pos = new byte[N_GROUPS];               //        6 byte : Tablo pozisyonlari
                                                                                //---------------
                                                                                //    60798 byte
        int[] tt;                                                               //  3600000 byte : Veri pozisyonlarini tutar
        byte[] ll8;                                                             //   900000 byte : Tum blok
                                                                                //---------------
                                                                                //  4560782 byte
                                                                                //===============
        Data(int blockSize100k) {
            super();

            this.ll8 = new byte[blockSize100k * BZip2Constants.baseBlockSize];
        }

        /**
         * tt dizisini baslatir
         * 
         * Bu metod gerekli dizi uzunlugu bilindiginde cagirilir. Kucuk 
         * dosyalari arsivlerken gereksiz bellek ayirmaktan kacinmak icin 
         * kurulum zamaninda kullanilmaz.
         * 
         * @param length : Dizi uzunlugu
         */
        final int[] initTT(int length) {
            int[] ttShadow = this.tt;

            // tt.length herzaman lenghtten buyuk veya esit olmali. Eğer arsivci 
            // kucuk ve buyuk bloklari karistirdiysa boyle olmayabilir. Normalde 
            // sadece son blok digerlerinden kucuk olacaktir.
             if ((ttShadow == null) || (ttShadow.length < length)) {
                this.tt = ttShadow = new int[length];
            }

            return ttShadow;
        }

    }
}