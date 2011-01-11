/*
** Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
 *
*/

package external.publicDomain.tar;

import java.io.*;
/**
 * UNIX tar arsivini bir outputstream olarak yazar. write() kullanarak girdi 
 * iceriklerini yazar.
 * 
 * Kerry Menzel <kmenzel@cfl.rr.com>
 */
public class TarOutputStream extends FilterOutputStream {
	protected boolean			debug;
	protected long				currSize;
	protected long				currBytes;
	protected byte[]			oneBuf;
	protected byte[]			recordBuf;
	protected int				assemLen;
	protected byte[]			assemBuf;
	protected TarBuffer			buffer;
////////////////////////////////////////////////////////////////////////////////
    /**
     * Standart blok ve kayit boyutu ile kurucu
     * 
     * @param os
     */
	public TarOutputStream( OutputStream os ) {
		this( os, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Verilen blok ve standart kayit boyutu ile kurucu
     *
     * @param os
     * @param blockSize
     */
	public TarOutputStream( OutputStream os, int blockSize ) {
		this( os, blockSize, TarBuffer.DEFAULT_RCDSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Verilen blok ve kayit boyutu ile kurucu
     *
     * @param os
     * @param blockSize
     * @param recordSize
     */
	public TarOutputStream( OutputStream os, int blockSize, int recordSize ) {
		super( os );
		this.buffer = new TarBuffer( os, blockSize, recordSize );
		this.debug = false;
		this.assemLen = 0;
		this.assemBuf = new byte[ recordSize ];
		this.recordBuf = new byte[ recordSize ];
		this.oneBuf = new byte[1];
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
	 * Tamponda debugging bayragini atar
	 *
	 * @param debugF : Yeni debug ayari
	 */
	public void setBufferDebug( boolean debug ) {
		this.buffer.setDebug( debug );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Streami kapatmadan tari sonlandirir
	 */
	public void finish() throws IOException {
		this.writeEOFRecord();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Streami kapatir ve tari sonlandirir
	 */
	public void close() throws IOException {
		this.finish();
		this.buffer.close();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Kayit boyutunu alir
         * 
	 * @return recordSize
	 */
	public int getRecordSize() {
		return this.buffer.getRecordSize();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Streame bir girdi koyar. Girdinin basligini yazar ve streami girdinin
         * icerigini yazmak icin ayarlar. Bu metod cagirilinca write() ile yazma 
         * yapilabilir. Icerik yazilinca tum tamponlanmis veriyi streame yazmak
         * closeEntry() cagirilmak zorundadir.
         * 
	 * @param entry 
	 */
	public void putNextEntry( TarEntry entry ) throws IOException {
		StringBuffer name = entry.getHeader().name;
		if ( ( entry.isUnixTarFormat() && name.length() > TarHeader.NAMELEN ) || ( ! entry.isUnixTarFormat()
                && name.length() > (TarHeader.NAMELEN + TarHeader.PREFIXLEN) )) {   // Formata gore isim boyutu kontrolu yapar
			throw new InvalidHeaderException( "file name '" + name + "' is too long ( " + name.length() + " > "
                                + ( entry.isUnixTarFormat() ? TarHeader.NAMELEN : (TarHeader.NAMELEN + TarHeader.PREFIXLEN) ) + " bytes )" );
			}
		entry.writeEntryHeader( this.recordBuf );                       // Basligi yazar
		this.buffer.writeRecord( this.recordBuf );                      // Kayiti yazar
		this.currBytes = 0;
		if ( entry.isDirectory() )
			this.currSize = 0;
		else
			this.currSize = entry.getSize();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Girdiyi kapatir. Tum veri iceren girdiler icin cagirilmalidir.
         */
	public void closeEntry() throws IOException {
		if ( this.assemLen > 0 ) {
			for ( int i = this.assemLen ; i < this.assemBuf.length ; ++i )
				this.assemBuf[i] = 0;
			this.buffer.writeRecord( this.assemBuf );
			this.currBytes += this.assemLen;
			this.assemLen = 0;
			}
		if ( this.currBytes < this.currSize )
			throw new IOException ( "entry closed at '" + this.currBytes + "' before the '" + this.currSize
                    + "' bytes specified in the header were written" );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Suanki girdiye bir byte yazar
         * 
	 * @param b : Yazilcacak byte
	 */
	public void write( int b ) throws IOException {
		this.oneBuf[0] = (byte) b;
		this.write( this.oneBuf, 0, 1 );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Suanki girdiye bytelar yazar
         * 
	 * @param wBuf : Yazilacak bytelar
	 */
	public void write( byte[] wBuf ) throws IOException {
		this.write( wBuf, 0, wBuf.length );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Tar arsivine bytelar yazar. Diger yazma metodlari bu metodu cagirir.
         * 
	 * @param wBuf : Yazilacak bytelar
	 * @param wOffset : Offset
	 * @param numToWrite : Kac byte yazilacagi
	 */
	public void write( byte[] wBuf, int wOffset, int numToWrite ) throws IOException {
		if ( (this.currBytes + numToWrite) > this.currSize )            // Yazilmak istenen veri boyutu asiyorsa
			throw new IOException( "request to write '" + numToWrite + "' bytes exceeds size in header of '"
                                + this.currSize + "' bytes" );
		if ( this.assemLen > 0 ) {                                      // Ikincil tampon
			if ( (this.assemLen + numToWrite ) >= this.recordBuf.length ) {
				int aLen = this.recordBuf.length - this.assemLen;
				System.arraycopy( this.assemBuf, 0, this.recordBuf, 0, this.assemLen ); // Once ikincil tampondan 
				System.arraycopy( wBuf, wOffset, this.recordBuf, this.assemLen, aLen ); // Sonra verilen bytelar yazilir
				this.buffer.writeRecord( this.recordBuf );
				this.currBytes += this.recordBuf.length;
				wOffset += aLen;
				numToWrite -= aLen;
				this.assemLen = 0;
				}
			else // ( (this.assemLen + numToWrite ) < this.recordBuf.length )
				{
				System.arraycopy( wBuf, wOffset, this.assemBuf,	this.assemLen, numToWrite );
				wOffset += numToWrite;                          // Verilen byte tamamen yazilir
				this.assemLen += numToWrite; 
				numToWrite -= numToWrite;
				}
			}
		for ( ; numToWrite > 0 ; ) {                                    // Simdi ya ikincil tampon bosaldi ya da tum veriler yazildi.
			if ( numToWrite < this.recordBuf.length ) {             // Veriler ikincil tampona atilir
				System.arraycopy( wBuf, wOffset, this.assemBuf, this.assemLen, numToWrite );
				this.assemLen += numToWrite;
				break;
				}
			this.buffer.writeRecord( wBuf, wOffset );               // Yazilmak istenen veriler kayiti dolduruyorsa tampona yazilir
			long num = this.recordBuf.length;
			this.currBytes += num;
			numToWrite -= num;
			wOffset += num;
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
         * Tar arsivine EOF yazar. EOF tamamen sifir dolu olan kayittir
	 */
	private void writeEOFRecord() throws IOException {
		for ( int i = 0 ; i < this.recordBuf.length ; ++i )
			this.recordBuf[i] = 0;
		this.buffer.writeRecord( this.recordBuf );
		}
	}