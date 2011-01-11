/*
** Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
*/

package external.publicDomain.tar;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.*;
/**
 * Bir UNIX tar arsivini InputStream olarak okur.
 *
 * Kerry Menzel <kmenzel@cfl.rr.com>
 */
public class TarInputStream extends FilterInputStream {
	protected boolean			debug;
	protected boolean			hasHitEOF;
	protected long				entrySize;
	protected long				entryOffset;
	protected byte[]			oneBuf;
	protected byte[]			readBuf;
	protected TarBuffer			buffer;
	protected TarEntry			currEntry;
	protected long                          mark;
	protected int                           markLim;
	protected EntryFactory                  eFactory;
////////////////////////////////////////////////////////////////////////////////
    /**
     * Standart blok ve kayit boyutlu kurucu
     *
     * @param is
     */
    public TarInputStream( InputStream is ) {
		this( is, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Verilen blok ve standart kayit boyutlu kurucu
     *
     * @param is
     * @param blockSize
     */
	public TarInputStream( InputStream is, int blockSize ) {
		this( is, blockSize, TarBuffer.DEFAULT_RCDSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Verilen blok ve kayit boyutlu kurucu
     *
     * @param is
     * @param blockSize
     * @param recordSize
     */
	public TarInputStream( InputStream is, int blockSize, int recordSize ) {
		super( is );
		this.buffer = new TarBuffer( is, blockSize, recordSize );
		this.readBuf = null;
		this.oneBuf = new byte[1];
		this.debug = false;
		this.hasHitEOF = false;
		this.eFactory = null;
                this.mark=-1;
                this.markLim=-1;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Debugging bayragini atar
	 *
	 * @param debugF : Yeni debug ayari
     */
	public void setDebug( boolean debugF ) {
		this.debug = debugF;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Kullanici tanimli girdi olusturur
	 *
	 * @param factory
	 */
	public void setEntryFactory( EntryFactory factory ) {
		this.eFactory = factory;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Tampondaki debugging bayragini atar
	 *
	 * @param debug : Yeni debug ayari
	 */
	public void setBufferDebug( boolean debug ) {
		this.buffer.setDebug( debug );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Bu streami kapatir. Tamponun close() metodunu cagirir
	 */
	public void close() throws IOException {
		this.buffer.close();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Kayit boyutunu alir
	 *
	 * @return recordSize
	 */
	public int getRecordSize(){
		return this.buffer.getRecordSize();
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Kalan veri boyutunu alir. Tum arsivdeki degil sadece su anki girdinin
     * verisine bakar.
     *
     * @return boyut - offset
     */
    public int available() throws IOException {
            return (int)(this.entrySize - this.entryOffset);
    }
////////////////////////////////////////////////////////////////////////////////
	/**
         * Girdi tamponundan verileri atlar. Tum arsivdeki degil sadece su anki
         * girdinin verisine bakar. Eger girdi sonuna ulasirsa durur.
	 *
	 * @param numToSkip : Atlanacak boyut
	 * @return atlanan byte sayisi
	 */
	public long skip( long numToSkip ) throws IOException {
		byte[] skipBuf = new byte[ 8 * 1024 ];
                long num = numToSkip;
		for ( ; num > 0 ; ) {
			int numRead = this.read( skipBuf, 0, ( num > skipBuf.length ? skipBuf.length : (int) num ) );
			if ( numRead == -1 )
				break;
			num -= numRead;
			}
		return ( numToSkip - num );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Isaretleme destekleniyor
	 *
	 * @return false
	 */
	public boolean markSupported() {
		return true;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Isaretleme yapar
	 *
	 * @param markLimit
	 */
	public void mark( int markLimit ) {
            if(markSupported()){
                    markLim = markLimit;
                    mark = entryOffset;
                } 
        }
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Isaretlemeyi eski haline getirir
	 */
	public void reset() throws IOException {
            if(markSupported()){
                if( mark !=-1 && markLim >= ( entryOffset-mark ) ) {
                    entryOffset = mark;
                }
                else throw new IOException( " the method mark has not been called since the stream was created, or the number of bytes read " +
                        "from the stream since mark was last called (" + (char)(entryOffset-mark) + ") is larger than the argument to mark " +
                        "at that last call (" + (char)markLim + ")" );
            }
            else throw new IOException("mark/reset not supported");
        }
////////////////////////////////////////////////////////////////////////////////
	/**
         * Girdiden simdiye kadar okunan veri sayisini, offset, dondurur.
	 *
	 * @returns offset
	 */
	public long getEntryPosition() {
		return this.entryOffset;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Streamdeki konumumuzu verir. Suan kacinci byte'tayiz.
	 *
	 * @returns suanki isaretci
	 */
	public long getStreamPosition() {
		return ( buffer.getBlockSize() * buffer.getCurrentBlockNum() ) + buffer.getCurrentRecordNum();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsivdeki siradaki girdiyi alir. Suanki girdide kalan veriyi atlar.
	 *
	 * @return TarEntry
	 */
	public TarEntry getNextEntry() throws IOException {
		if ( this.hasHitEOF )                                                   // Sirada girdi yoksa null dondurur
			return null;
		if ( this.currEntry != null ) {
			long numToSkip = (this.entrySize - this.entryOffset);               // Atlanan veri boyutu
			if ( this.debug )
			System.err.println( "TarInputStream: SKIP currENTRY '" + this.currEntry.getName() + "' SZ "
                    + this.entrySize + " OFF " + this.entryOffset + "  skipping " + numToSkip + " bytes" );
			if ( numToSkip > 0 ) {
				this.skip( numToSkip );                                         // Veri atlanir
				}
			this.readBuf = null;
			}
		byte[] headerBuf = this.buffer.readRecord();                            // Kayiti tampona okur
		if ( headerBuf == null ) {
			if ( this.debug ) {
				System.err.println( "READ NULL RECORD" );
				}
			this.hasHitEOF = true;
			}
		else if ( this.buffer.isEOFRecord( headerBuf ) ) {
			if ( this.debug ) {
				System.err.println( "READ EOF RECORD" );
				}
			this.hasHitEOF = true;
			}
		if ( this.hasHitEOF ) {
			this.currEntry = null;
			}
		else {
			try {
				if ( this.eFactory == null ) {
					this.currEntry = new TarEntry( headerBuf );                 // Yeni girdiyi yaratir
					}
				else {
					this.currEntry = this.eFactory.createEntry( headerBuf );    // Kullanici tanimli girdi yaratir
					}
				if ( this.debug )
				System.err.println( "TarInputStream: SET CURRENTRY '" + this.currEntry.getName()	+ "' size = " + this.currEntry.getSize() );
				this.entryOffset = 0;
				this.entrySize = this.currEntry.getSize();
				}
			catch ( InvalidHeaderException ex ) {
				this.entrySize = 0;
				this.entryOffset = 0;
				this.currEntry = null;
				throw new InvalidHeaderException( "bad header in block " + this.buffer.getCurrentBlockNum() + " record "
                        + this.buffer.getCurrentRecordNum() + ", " + ex.getMessage() );
				}
			}
		return this.currEntry;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Suanki girdiden bir byte okur
     *
	 * @return okunan deger
	 */
	public int read() throws IOException {
		int num = this.read( this.oneBuf, 0, 1 );
		if ( num == -1 )
			return num;
		else
			return (int) this.oneBuf[0];
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Suanki girdiden veriler okur
     *
	 * @param buf : Okunan degerlerin konacagi tampon
	 * @return ne kadar veri okundu
	 */
	public int read( byte[] buf ) throws IOException {
		return this.read( buf, 0, buf.length );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Suanki girdiden veriler okur.
         *
         * Diger okuma metodlari bu metodu cagirarak okuma yapar. Girdi sinirlarina
         * gore islem yapar.
         *
         * @param buf : Verilerin koyulacagi tampon
         * @param offset
         * @param numToRead : Okunacak veri sayisi
	 * @return okunan veri sayisi
	 */
	public int read( byte[] buf, int offset, int numToRead ) throws IOException {
		int totalRead = 0;
		if ( this.entryOffset >= this.entrySize )
			return -1;
		if ( (numToRead + this.entryOffset) > this.entrySize ) {                // Okuma siniri belirlenir
			numToRead = (int) (this.entrySize - this.entryOffset);
			}
		if ( this.readBuf != null ) {                                           // Okuma tamponunda veri varsa
			int sz = ( numToRead > this.readBuf.length ) ? this.readBuf.length : numToRead;
			System.arraycopy( this.readBuf, 0, buf, offset, sz );               // Tampondan veri kopyalanir
			if ( sz >= this.readBuf.length ) {                                  // Tampondaki tum veri okunduysa tampon bosaltilir
				this.readBuf = null;
				}
			else {                                                              // Tamponda veri kaldiysa
				int newLen = this.readBuf.length - sz;                          // Okunan veri tampondan cikarilir
				byte[] newBuf = new byte[ newLen ];
				System.arraycopy( this.readBuf, sz, newBuf, 0, newLen );
				this.readBuf = newBuf;                                          // Okuma tamponu guncellenir
				}
			totalRead += sz;                                                    // Simdiye kadar okunan deger
			numToRead -= sz;                                                    // Geriye kalan deger
			offset += sz;
			}
		for ( ; numToRead > 0 ; ) {                                             // Istenilen miktar okununcaya kadar devam et
			byte[] rec = this.buffer.readRecord();                              // Tampondan kayit oku
			if ( rec == null ) {
				throw new IOException( "unexpected EOF with " + numToRead + " bytes unread" );
				}
			int sz = numToRead;
			int recLen = rec.length;                                            // Kayit boyutu al
			if ( recLen > sz ) {                                                // Kayit boyutu istenilen miktardan fazlaysa
				System.arraycopy( rec, 0, buf, offset, sz );                    // Istenilen miktari kopyala
				this.readBuf = new byte[ recLen - sz ];
				System.arraycopy( rec, sz, this.readBuf, 0, recLen - sz );      // Geri kalan veriyi okuma tamponuna at
				}
			else {                                                              // Kayit boyutu istenilen miktardan fazla degilse
				sz = recLen;                                                    // Kayit tamamen kopyalanir
				System.arraycopy( rec, 0, buf, offset, recLen );
				}
			totalRead += sz;                                                    // Simdiye kadar okunan deger
			numToRead -= sz;                                                    // Geriye kalan deger
			offset += sz;
			}
		this.entryOffset += totalRead;                                          // Girdi offseti guncellenir
		return totalRead;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsiv girdisi icerigini direk outputStream'e kopyalar
	 *
	 * @param out
	 */
	public void copyEntryContents( OutputStream out ) throws IOException {
		byte[] buf = new byte[ 32 * 1024 ];
		for ( ; ; ) {
			int numRead = this.read( buf, 0, buf.length );
			if ( numRead == -1 )
				break;
			out.write( buf, 0, numRead );
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Programciya kendi kendi TarEntry altsiniflarini yaratmaya izin verir.
	 */
	public interface EntryFactory {
		public TarEntry createEntry( String name );
		public TarEntry createEntry( File path ) throws InvalidHeaderException;
		public TarEntry createEntry( byte[] headerBuf ) throws InvalidHeaderException;
		}
	public class EntryAdapter implements EntryFactory {
		public TarEntry createEntry( String name ) {
			return new TarEntry( name );
			}
		public TarEntry createEntry( File path ) throws InvalidHeaderException {
			return new TarEntry( path );
			}
		public TarEntry createEntry( byte[] headerBuf ) throws InvalidHeaderException {
			return new TarEntry( headerBuf );
			}
		}
	}