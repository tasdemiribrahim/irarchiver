/*
** Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
*/

package external.publicDomain.tar;

/**
 * Bu sinif arsivde kullanilan basligi olusturur. Ayrica pek cok baslik sabitini
 * tutar.
 */
public class TarHeader extends Object implements Cloneable {
	public static final int		NAMELEN = 100;                                  // Baslik tamponundaki isim alani boyutu
	public static final int		NAMEOFFSET = 0;                                 // Baslik tamponundaki isim alani offseti
	public static final int		PREFIXLEN = 155;                                // Prefix boyutu
	public static final int		PREFIXOFFSET = 345;                             // Prefix offseti
	public static final int		MODELEN = 8;                                    // Mod boyutu
	public static final int		UIDLEN = 8;                                     // Kullanici ID boyutu
	public static final int		GIDLEN = 8;                                     // Grup ID boyutu
	public static final int		CHKSUMLEN = 8;                                  // KOntrol toplami boyutu
	public static final int		SIZELEN = 12;                                   // Boyut alani boyutu
	public static final int		MAGICLEN = 8;                                   // Magic boyutu
	public static final int		MODTIMELEN = 12;                                // Guncelleme zamani boyutu
	public static final int		UNAMELEN = 32;                                  // Kullanici adi boyutu
	public static final int		GNAMELEN = 32;                                  // Grup adi boyutu
	public static final int		DEVLEN = 8;                                     // Arac boyutu
	public static final byte	LF_OLDNORM	= 0;                                // LF sabitleri girdi tipini belirtir
	public static final byte	LF_NORMAL	= (byte) '0';                       // Normal dosya
	public static final byte	LF_LINK		= (byte) '1';                       // Bag dosya
	public static final byte	LF_SYMLINK	= (byte) '2';                       // Sembolik bag
	public static final byte	LF_CHR		= (byte) '3';                       // Karakter araci
	public static final byte	LF_BLK		= (byte) '4';                       // Blok araci
	public static final byte	LF_DIR		= (byte) '5';                       // Dizin
	public static final byte	LF_FIFO		= (byte) '6';                       // FIFO
	public static final byte	LF_CONTIG	= (byte) '7';                       // Bitisik
	public static final String	TMAGIC		= "ustar";                          // POSIX tar arsivi
	public static final String	GNU_TMAGIC	= "ustar  ";                        // GNU tar arsivi
	public StringBuffer		name;                                               // Girdinin adi
	public int				mode;                                               // Erisim modu
	public int				userId;                                             // Kullanici ID
	public int				groupId;                                            // Grup ID
	public long				size;                                               // Girdi boyutu
	public long				modTime;                                            // Guncelleme zamani
	public int				checkSum;                                           // Kontrol toplami
	public byte				linkFlag;                                           // Bag bayragi
	public StringBuffer		linkName;                                           // Bag adi
	public StringBuffer		magic;                                              // Magic eki
	public StringBuffer		userName;                                           // Kullanici adi
	public StringBuffer		groupName;                                          // Grup adi
	public int				devMajor;                                           // Ana arac numarasi
	public int				devMinor;                                           // Ikincil arac numarasi
////////////////////////////////////////////////////////////////////////////////
    /**
     * Baslik baslangic degerlerini atar
     */
	public TarHeader() {
		this.magic = new StringBuffer( TarHeader.TMAGIC );
		this.name = new StringBuffer();
		this.linkName = new StringBuffer();
		String user = System.getProperty( "user.name", "" );
		if ( user.length() > 31 )
			user = user.substring( 0, 31 );
		this.userId = 0;
		this.groupId = 0;
		this.userName = new StringBuffer( user );
		this.groupName = new StringBuffer( "" );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Basligin klonunu olusturur ve dondurur
	 */
	public Object clone() {
		TarHeader hdr = null;
		try {
			hdr = (TarHeader) super.clone();
			hdr.name = (this.name == null ) ? null : new StringBuffer( this.name.toString() );
			hdr.mode = this.mode;
			hdr.userId = this.userId;
			hdr.groupId = this.groupId;
			hdr.size = this.size;
			hdr.modTime = this.modTime;
			hdr.checkSum = this.checkSum;
			hdr.linkFlag = this.linkFlag;
			hdr.linkName = (this.linkName == null ) ? null : new StringBuffer( this.linkName.toString() );
			hdr.magic = (this.magic == null ) ? null : new StringBuffer( this.magic.toString() );
			hdr.userName = (this.userName == null ) ? null : new StringBuffer( this.userName.toString() );
			hdr.groupName = (this.groupName == null ) ? null : new StringBuffer( this.groupName.toString() );
			hdr.devMajor = this.devMajor;
			hdr.devMinor = this.devMinor;
			}
		catch ( CloneNotSupportedException ex ) {
			ex.printStackTrace( System.err );
			}
		return hdr;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Girdinin ismini alir
     *
	 * @return name
	 */
	public String getName() {
		return this.name.toString();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Basliktan sekizlik bir katari alir. Bu dosya erisim modunu almak icin
     * kullanilir
     *
	 * @param header
	 * @param offset
	 * @param length
	 * @return sekizlik katar
	 */
	public static long parseOctal( byte[] header, int offset, int length ) throws InvalidHeaderException {
		long result = 0;
		boolean stillPadding = true;
		int end = offset + length;
		for ( int i = offset ; i < end ; ++i ) {
			if ( header[i] == 0 )
				break;
			if ( header[i] == (byte) ' ' || header[i] == '0' ) {
				if ( stillPadding )
					continue;
				if ( header[i] == (byte) ' ' )
					break;
				}
			stillPadding = false;
			result = (result << 3) + (header[i] - '0');
			}
		return result;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Basliktan dosya ismini alir. Bu parseName() den farklidir. Bu 'ustar'
     * isimleri tanir ve "prefix" degerini ada ekler.
	 *
	 * Dmitri Tikhonov <dxt2431@yahoo.com>
	 *
	 * @param header
	 * @param offset
	 * @param length
	 * @return prefix + isim
	 */
	public static StringBuffer parseFileName( byte[] header ) {
		StringBuffer result = new StringBuffer( 256 );
		if ( header[345] != 0 ) {                                               // Eger header[345] sifira esit degilse, o zaman bu 'ustar' in
			for ( int i = 345 ; i < 500 && header[i] != 0 ; ++i ) {             // Gosterdigi "prefix" dir. Normal isim alanina eklenir. '/'
				result.append( (char)header[i] );                               // ile ayrilir.
				}
			result.append( "/" );
			}
		for ( int i = 0 ; i < 100 && header[i] != 0 ; ++i ) {
			result.append( (char)header[i] );
			}
		return result;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Basliktan dosya ismini alir
	 *
	 * @param header 
	 * @param offset 
	 * @param length 
	 * @return isim
	 */
	public static StringBuffer parseName( byte[] header, int offset, int length ) throws InvalidHeaderException {
		StringBuffer result = new StringBuffer( length );
		int end = offset + length;
		for ( int i = offset ; i < end ; ++i ) {
			if ( header[i] == 0 )
				break;
			result.append( (char)header[i] );
			}
		return result;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Bu metod getNameBytes() gibi baslik tamponuna isim atar. Ancak bu metod
     * uzun isimleri tanir. Prefix ve suffix olarak ikiye ayirir.
	 *
	 * @param outbuf 
	 * @param newName
	 * @return simdiki baslik offseti(her zaman TarHeader.NAMELEN).
	 * @throws InvalidHeaderException : Isim basliga sigmazssa
	 */
	public static int getFileNameBytes( String newName, byte[] outbuf ) throws InvalidHeaderException {
		if ( newName.length() > 100 ) {
			int index = newName.indexOf( "/", newName.length() - 100 );         // Kesmek icin yer belirle
			if ( index == -1 )
				throw new InvalidHeaderException( "file name is greater than 100 characters, " + newName );
			String name = newName.substring( index + 1 );                       // Suffix ismi al
			String prefix = newName.substring( 0, index );                      // Prefix ismi al
			if ( prefix.length() > TarHeader.PREFIXLEN )
				throw new InvalidHeaderException( "file prefix is greater than 155 characters" );
			TarHeader.getNameBytes( new StringBuffer( name ), outbuf, TarHeader.NAMEOFFSET, TarHeader.NAMELEN );
			TarHeader.getNameBytes( new StringBuffer( prefix ), outbuf, TarHeader.PREFIXOFFSET, TarHeader.PREFIXLEN );
			}
		else {
			TarHeader.getNameBytes( new StringBuffer( newName ), outbuf, TarHeader.NAMEOFFSET, TarHeader.NAMELEN );
			}
		return TarHeader.NAMELEN;                                               // Offset simdi isim alani sonu
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Isim tamponundan bytelari baslik tamponuna tasir
     *
	 * @param name
     * @param buf
	 * @param offset
	 * @param length
	 * @return yeni offset (offset + length).
	 */
	public static int getNameBytes( StringBuffer name, byte[] buf, int offset, int length ) {
		int i;
		for ( i = 0 ; i < length && i < name.length() ; ++i ) {
			buf[ offset + i ] = (byte) name.charAt( i );
			}
		for ( ; i < length ; ++i ) {
			buf[ offset + i ] = 0;
			}
		return offset + length;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Bir sekizlik tam sayiyi baslik tamponuna atar
     *
	 * @param value sekizlik deger
     * @param buf
	 * @param offset 
	 * @param length 
	 * @return yeni offset
	 */
	public static int getOctalBytes( long value, byte[] buf, int offset, int length ) {
		byte[] result = new byte[ length ];
		int idx = length - 1;
		buf[ offset + idx ] = 0;
		--idx;
		buf[ offset + idx ] = (byte) ' ';
		--idx;
		if ( value == 0 ) {                                                     // Deger sifirsa kaydet
			buf[ offset + idx ] = (byte) '0';
			--idx;
			}
		else {
			for ( long val = value ; idx >= 0 && val > 0 ; --idx ) {            // Degeri sekizlik parcalar halinde yazar
				buf[ offset + idx ] = (byte) ( (byte) '0' + (byte) (val & 7) );
				val = val >> 3;
				}
			}
		for ( ; idx >= 0 ; --idx ) {                                            // Bosluk doldurma
			buf[ offset + idx ] = (byte) ' ';
			}
		return offset + length;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Bir sekizlik long tam sayiyi baslik tamponuna yazar
	 *
	 * @param value
     * @param buf
	 * @param offset
     * @param length
	 * @return yeni offset
	 */
	public static int getLongOctalBytes( long value, byte[] buf, int offset, int length ) {
		byte[] temp = new byte[ length + 1 ];
		TarHeader.getOctalBytes( value, temp, 0, length + 1 );                  // Gecici degerleri alir
		System.arraycopy( temp, 0, buf, offset, length );                       // Gecici degerleri kopyalar
		return offset + length;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Kontrol toplamini baslik tamponuna yazar
     *
	 * @param header
	 * @param offset
	 * @param length
	 * @return yeni offset
	 */
	public static int getCheckSumOctalBytes( long value, byte[] buf, int offset, int length ) {
		TarHeader.getOctalBytes( value, buf, offset, length );                  // Degeri yazar
		buf[ offset + length - 1 ] = (byte) ' ';                                // Bitis isaretleri
		buf[ offset + length - 2 ] = 0;
		return offset + length;
		}
	}