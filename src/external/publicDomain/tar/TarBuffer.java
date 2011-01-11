/*
** Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
*/
package external.publicDomain.tar;

import java.io.*;
/**
 * Tamponlanmis InputStream'i gercekler. Bu sistem blok teypler ve ozel IO
 * araclari gunlerine kadar gider. Bu sinifin yaptigi tek gercek islem
 * dosyalarin dogru blok boyutu oldugunu garantilemek.
 *
 * Bu sinifa asla direk erismenize gerek yoktur.
 *
 * Tar tamponlari Tar IOStreamleri tarafindan yaratilir.
 */
public class TarBuffer extends Object {
	public static final int		DEFAULT_RCDSIZE = ( 512 );
	public static final int		DEFAULT_BLKSIZE = ( DEFAULT_RCDSIZE * 20 );
	private InputStream		inStream;
	private OutputStream	outStream;
	private byte[]	blockBuffer;
	private int		currBlkIdx;
	private int		currRecIdx;
	private int		blockSize;
	private int		recordSize;
	private int		recsPerBlock;
	private boolean	debug;
////////////////////////////////////////////////////////////////////////////////
    /**
     * InputStream tabanli standart blok ve kayit boyutlu kurucu.
     *
     * @param inStream
     */
	public TarBuffer( InputStream inStream ) {
		this( inStream, TarBuffer.DEFAULT_BLKSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * InputStream tabanli verilen blok ve standart kayit boyutlu kurucu
     *
     * @param inStream
     * @param blockSize
     */
	public TarBuffer( InputStream inStream, int blockSize ) {
		this( inStream, blockSize, TarBuffer.DEFAULT_RCDSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * InputStream tabanli verilen blok ve kayit boyutlu kurucu
     *
     * @param inStream
     * @param blockSize
     * @param recordSize
     */
	public TarBuffer( InputStream inStream, int blockSize, int recordSize ) {
		this.inStream = inStream;
		this.outStream = null;
		this.initialize( blockSize, recordSize );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * OutputStream tabanli standart blok ve kayit boyutlu kurucu
     *
     * @param outStream
     */
	public TarBuffer( OutputStream outStream ) {
		this( outStream, TarBuffer.DEFAULT_BLKSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * OutputStream tabanli verilen blok ve standart kayit boyutlu kurucu
     *
     * @param outStream
     * @param blockSize
     */
	public TarBuffer( OutputStream outStream, int blockSize ) {
		this( outStream, blockSize, TarBuffer.DEFAULT_RCDSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * OutputStream tabanli verilen blok ve kayit boyutlu kurucu.
     *
     * @param outStream
     * @param blockSize
     * @param recordSize
     */
	public TarBuffer( OutputStream outStream, int blockSize, int recordSize ) {
		this.inStream = null;
		this.outStream = outStream;
		this.initialize( blockSize, recordSize );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Baslangic degerlerini atar
	 */
	private void initialize( int blockSize, int recordSize ) {
		this.debug = false;
		this.blockSize = blockSize;
		this.recordSize = recordSize;
		this.recsPerBlock = ( this.blockSize / this.recordSize );
		this.blockBuffer = new byte[ this.blockSize ];
		if ( this.inStream != null ) {
			this.currBlkIdx = -1;
			this.currRecIdx = this.recsPerBlock;
			}
		else {
			this.currBlkIdx = 0;
			this.currRecIdx = 0;
			}
		}
////////////////////////////////////////////////////////////////////////////////
         /**
         * Tamponun blok boyutunu alir. Blok pekcok kayittan olusur
         *
         * @return blockSize
	 */
	public int getBlockSize() {
		return this.blockSize;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Tamponun kayit boyutunu alir
     *
     * @return recordSize
	 */
	public int getRecordSize() {
		return this.recordSize;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Debugging bayragini atar
	 *
	 * @param debug : Yeni debug ayari
	 */
	public void setDebug( boolean debug ) {
		this.debug = debug;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Kayit arsiv sonunu mu gosteriyor kontrol eder. Arsiv sonu tamamen null
     * byte kayit ile gosterilir
     *
	 * @param record : Kontrol edilecek kayit
	 */
	public boolean isEOFRecord( byte[] record ) {
		for ( int i = 0, sz = this.getRecordSize() ; i < sz ; ++i )
			if ( record[i] != 0 )
				return false;
		return true;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * InputStreamdeki bir kayiti atlar
	 */
	public void	skipRecord() throws IOException {
		if ( this.debug ) {
			System.err.println( "SkipRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx );
			}
		if ( this.inStream == null )
			throw new IOException ( "reading (via skip) from an output buffer" );
		if ( this.currRecIdx >= this.recsPerBlock ) {
			if ( ! this.readBlock() )
				return;
			}
		this.currRecIdx++;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * InputStreamden bir kayit okur ve veriyi dondurur
     *
	 * @return kayit verisi
	 */
	public byte[] readRecord() throws IOException {
		if ( this.debug ) {
			System.err.println ( "ReadRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx );
			}
		if ( this.inStream == null )
			throw new IOException( "reading from an output buffer" );
		if ( this.currRecIdx >= this.recsPerBlock ) {
			if ( ! this.readBlock() )
				return null;
			}
		byte[] result = new byte[ this.recordSize ];
		System.arraycopy( this.blockBuffer, (this.currRecIdx * this.recordSize),	result, 0, this.recordSize );
		this.currRecIdx++;
		return result;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * InputStreamden bir blok okur ve tampona atar
     *
	 * @return false EOF ise, yoksa true
	 */
	private boolean readBlock() throws IOException {
		if ( this.debug ) {
			System.err.println( "ReadBlock: blkIdx = " + this.currBlkIdx );
			}
		if ( this.inStream == null )
			throw new IOException( "reading from an output buffer" );
		this.currRecIdx = 0;
		int offset = 0;
		int bytesNeeded = this.blockSize;
		for ( ; bytesNeeded > 0 ; ) {                                           // Blok boyutu kadar okuma yapilir
			long numBytes = this.inStream.read( this.blockBuffer, offset, bytesNeeded );
			//'Yohann.Roussel@alcatel.fr' tarafindan gelistirme:
			// Eger EOF gelirse ve blok tam degilse bu bozuk arsivdir. Bu
			// durumda hatayi gormez ve tum blok okunmus gibi devam ederiz. Bu
			// ileride bir hataya neden olmaz ve bu islem sonucunda false doner.
			if ( numBytes == -1 )
				break;
			offset += numBytes;                                                 // Offset guncellenir
			bytesNeeded -= numBytes;
			if ( numBytes != this.blockSize ) {
				if ( this.debug ) {
					System.err.println( "ReadBlock: INCOMPLETE READ " + numBytes+ " of " + this.blockSize + " bytes read." );
					}
				}
			}
		this.currBlkIdx++;                                                      // Blok sayaci artar
		return true;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Blok sayacini alir.
     *
	 * @return currBlkIdx
	 */
	public int getCurrentBlockNum() {
		return this.currBlkIdx;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Bloktaki kayit numarasini alir
     *
	 * @return currRecIdx - 1
	 */
	public int getCurrentRecordNum() {
		return this.currRecIdx - 1;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsive bir kayit yazar
     *
	 * @param record
	 */
	public void writeRecord( byte[] record ) throws IOException {
		if ( this.debug ) {
			System.err.println( "WriteRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx );
			}
		if ( this.outStream == null )
			throw new IOException( "writing to an input buffer" );
		if ( record.length != this.recordSize )
			throw new IOException( "record to write has length '" + record.length + "' which is not the record size of '"
                    + this.recordSize + "'" );
		if ( this.currRecIdx >= this.recsPerBlock ) {                           // Blok dolduysa yeni blok
			this.writeBlock();
			}
		System.arraycopy( record, 0, this.blockBuffer, (this.currRecIdx * this.recordSize), this.recordSize );
		this.currRecIdx++;                                                      // Blok dolduysa yeni blok
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsive gonderilen kayittan offsetten baslayarak okunan veri yazilir
	 *
	 * @param buf : Veriyi tutan kayit
	 * @param offset : Tamponda okumaya baslanacak offset
	 */
	public void	writeRecord( byte[] buf, int offset ) throws IOException {
		if ( this.debug ) {
			System.err.println( "WriteRecord: recIdx = " + this.currRecIdx + " blkIdx = " + this.currBlkIdx );
			}
		if ( this.outStream == null )
			throw new IOException( "writing to an input buffer" );
		if ( (offset + this.recordSize) > buf.length )
			throw new IOException( "record has length '" + buf.length + "' with offset '" + offset
                    + "' which is less than the record size of '" + this.recordSize + "'" );
		if ( this.currRecIdx >= this.recsPerBlock ) {                           // Blok dolduysa yeni blok
			this.writeBlock();
			}
		System.arraycopy( buf, offset, this.blockBuffer, (this.currRecIdx * this.recordSize), this.recordSize );
		this.currRecIdx++;                                                      // Veri yazilir ve kayit guncellenir
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Arsive tampondaki blogu yazar
	 */
	private void writeBlock() throws IOException {
		if ( this.debug ) {
			System.err.println( "WriteBlock: blkIdx = " + this.currBlkIdx );
			}
		if ( this.outStream == null )
			throw new IOException( "writing to an input buffer" );
		this.outStream.write( this.blockBuffer, 0, this.blockSize );
		this.outStream.flush();
		this.currRecIdx = 0;
		this.currBlkIdx++;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Suanki blogu bosaltir.
	 */
	private void flushBlock() throws IOException {
		if ( this.debug ) {
			System.err.println( "TarBuffer.flushBlock() called." );
			}
		if ( this.outStream == null )
			throw new IOException( "writing to an input buffer" );
		if ( this.currRecIdx > 0 ) {
			this.writeBlock();
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tamponu kapatir. Kapatmadan once blogu bosaltir
	 */
	public void close() throws IOException {
		if ( this.debug ) {
			System.err.println( "TarBuffer.closeBuffer()." );
			}
		if ( this.outStream != null ) {
			this.flushBlock();
			if ( this.outStream != System.out && this.outStream != System.err ) {
				this.outStream.close();
				this.outStream = null;
				}
			}
		else if ( this.inStream != null ) {
			if ( this.inStream != System.in ) {
				this.inStream.close();
				this.inStream = null;
				}
			}
		}
	}