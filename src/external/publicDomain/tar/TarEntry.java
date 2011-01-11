/*
** Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
*/
package external.publicDomain.tar;

import java.io.*;
import java.util.Date;
/**
 * Bu sinif Tar arsivindeki bir girdiyi temsil eder. Girdi dosya ile girdi
 * basligindan olusur. Nasil kullanilacagina bagli olarak girdiler uc sekilde
 * baslatilabilirler.
 * Bir arsivden okunan baslik bytelarindan yaratilan girdiler TarEntry( byte[] )
 * tarafindan baslatilir. Basliklari okunann bytelar ile doldurulur. Dosya
 * kismi null atanir.
 * Arsive yazilacak dosyalardan yaratilan girdiler TarEntry( File ) tarafindan
 * baslatilir. Baslik dosyanin bilgileri ile doldurulur. Ayrica dosyaya referans
 * tutarlar.
 * Son olarak sadece isim ile yaratilan girdiler. Bu programciya elle girdi
 * yaratmaya izin verir. Ornegin arsive yazilacak sadece bir InputStream varsa
 * ve baslik baska bilgilerden olusturulduysa. Bu durumda baslik bilgileri
 * standart atanir vr dosya null atanir.
 *
 * Orijinal Unix Tar Baslik:
 *
 * Alan   Alan      Alan
 * Boyutu Adi       Anlami
 * -----  --------- ---------------------------
 *   100  name      dosya adi
 *     8  mode      dosya modu
 *     8  uid       kullanici ID
 *     8  gid       grup ID
 *    12  size      Byte dosya boyutu
 *    12  mtime     dosya guncelleme zamani
 *     8  chksum    baslik icin kontrol toplami
 *     1  link      bag gosterici
 *   100  linkname  baglanmis dosya adi
 *
 * POSIX "ustar" Tipi Tar Baslik:
 *
 * Alan   Alan      Alan
 * Boyutu Adi       Anlami
 * -----  --------- ---------------------------
 *   100  name      dosya adi
 *     8  mode      dosya modu
 *     8  uid       kullanici ID
 *     8  gid       grup ID
 *    12  size      Byte dosya boyutu
 *    12  mtime     dosya guncelleme zamani
 *     8  chksum    baslik icin kontrol toplami
 *     1  typeflag  dosya tipi
 *   100  linkname  baglanmis dosya adi
 *     6  magic     USTAR gosterici
 *     2  version   USTAR versiyon
 *    32  uname     kullanici adi
 *    32  gname     grup adi
 *     8  devmajor  ana aygit numarasi
 *     8  devminor  ikincil aygit numarasi
 *   155  prefix    dosya adi icin ontaki(prefix)
 *
 * struct posix_header
 *   {                     byte offset
 *   char name[100];            0
 *   char mode[8];            100
 *   char uid[8];             108
 *   char gid[8];             116
 *   char size[12];           124
 *   char mtime[12];          136
 *   char chksum[8];          148
 *   char typeflag;           156
 *   char linkname[100];      157
 *   char magic[6];           257
 *   char version[2];         263
 *   char uname[32];          265
 *   char gname[32];          297
 *   char devmajor[8];        329
 *   char devminor[8];        337
 *   char prefix[155];        345
 *   };                       500
 *
 * Sinif GNU formatli basliklari gosterse de desteklemez.
 */
public class TarEntry extends Object implements Cloneable {
	protected File				file;                                           // Girdi bir dosya temsil ediyorsa
	protected TarHeader			header;                                         // Girdinin baslik bilgisi
	protected boolean			unixFormat;                                     // UNIX format bayragi
	protected boolean			ustarFormat;                                    // USTAR format bayragi
	protected boolean			gnuFormat;                                      // GNU format bayragi
////////////////////////////////////////////////////////////////////////////////
	/**
     * Sadece altsiniflarin kullanimi icin korunan kurucu
	 */
	protected TarEntry(){ }
////////////////////////////////////////////////////////////////////////////////
	/**
     * Sadece isim ile kurucu
     *
     * @param name
	 */
	public TarEntry( String name ) {
		this.initialize();
		this.nameTarHeader( this.header, name );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Dosya ile kurucu
	 *
	 * @param file
	 */
	public TarEntry( File file ) throws InvalidHeaderException {
		this.initialize();
		this.getFileTarHeader( this.header, file );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Baslik ile kurucu
	 *
	 * @param headerBuf
	 */
	public TarEntry( byte[] headerBuf ) throws InvalidHeaderException {
		this.initialize();
		this.parseTarHeader( this.header, headerBuf );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Baslangic degerlerini atar
	 */
	private void initialize() {
		this.file = null;
		this.header = new TarHeader();
		this.gnuFormat = false;
		this.ustarFormat = true;
		this.unixFormat = false;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdiyi klonla
	 */
	public Object clone() {
		TarEntry entry = null;
		try {
			entry = (TarEntry) super.clone();
			if ( this.header != null ) {
				entry.header = (TarHeader) this.header.clone();                 // Baslik klonu
				}
			if ( this.file != null ) {
				entry.file = new File( this.file.getAbsolutePath() );           // Dosya klonu
				}
			}
		catch ( CloneNotSupportedException ex ) {
			ex.printStackTrace( System.err );
			}
		return entry;                                                           // Klonu gonderir
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * USTAR format kontrolu
	 *
	 * @return ustarFormat
	 */
	public boolean isUSTarFormat() {
		return this.ustarFormat;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * USTAR formati atar
	 */
	public void setUSTarFormat() {
		this.ustarFormat = true;
		this.gnuFormat = false;
		this.unixFormat = false;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * GNU format kontrolu
	 *
	 * @return gnuFormat
	 */
	public boolean isGNUTarFormat() {
		return this.gnuFormat;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * GNU formati atar
	 */
	public void setGNUTarFormat() {
		this.gnuFormat = true;
		this.ustarFormat = false;
		this.unixFormat = false;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * UNIX format kontrolu
	 *
	 * @return unixFormat
	 */
	public boolean isUnixTarFormat() {
		return this.unixFormat;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * UNIX format atar
	 */
	public void setUnixTarFormat() {
		this.unixFormat = true;
		this.ustarFormat = false;
		this.gnuFormat = false;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Iki girdinin denkligini kontrol eder. DEnklik baslik isimler ile
     * belirlenir.
     *
	 * @return it : Simdiki girdi ile kontrol edilecek girdi
	 * @return denkse TRUE
	 */
	public boolean equals( TarEntry it ) {
		return this.header.name.toString().equals( it.header.name.toString() );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Alt girdi kontrolu. Verilen girdinin ismi suanki girdininki ile basliyor.
	 *
	 * @param desc : Kontrol edilecek girdi
	 * @return altgirdi ise TRUE
	 */
	public boolean isDescendent( TarEntry desc ) {
		return desc.header.name.toString().startsWith( this.header.name.toString() );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin basligini alir
	 *
	 * @return header
	 */
	public TarHeader getHeader() {
		return this.header;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin ismini alir
	 *
	 * @return name
	 */
	public String getName() {
		return this.header.name.toString();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin ismini atar
	 *
	 * @param name
	 */
	public void setName( String name ) {
		this.header.name = new StringBuffer( name );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin kullanici ID alir
	 *
	 * @return userID
	 */
	public int getUserId() {
		return this.header.userId;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin kullanici ID atar
	 *
	 * @param userId
	 */
	public void setUserId( int userId ) {
		this.header.userId = userId;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin grup ID alir
	 *
	 * @return groupId
	 */
	public int getGroupId() {
		return this.header.groupId;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin grup ID atar
	 *
	 * @param groupId
	 */
	public void setGroupId( int groupId ) {
		this.header.groupId = groupId;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin kullanici ismini alir
	 *
	 * @return userName
	 */
	public String getUserName() {
		return this.header.userName.toString();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin kullanici ismini atar
	 *
	 * @param userName
	 */
	public void setUserName( String userName ) {
		this.header.userName = new StringBuffer( userName );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin grup ismini alir
	 *
	 * @return groupName
	 */
	public String getGroupName() {
		return this.header.groupName.toString();
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin grup ismini atar
	 *
	 * @param groupName
	 */
	public void setGroupName( String groupName ) {
		this.header.groupName = new StringBuffer( groupName );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin ID lerini atar
	 *
	 * @param userId
	 * @param groupId
	 */
	public void setIds( int userId, int groupId ) {
		this.setUserId( userId );
		this.setGroupId( groupId );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin isimlerini atar
	 *
	 * @param userName
	 * @param groupName
	 */
	public void setNames( String userName, String groupName ) {
		this.setUserName( userName );
		this.setGroupName( groupName );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Java zamani ile girdinin guncelleme zamani atar
	 *
	 * @param time
	 */
	public void setModTime( long time ) {
		this.header.modTime = time / 1000;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin guncelleme zamani atar
	 *
	 * @param time
	 */
	public void setModTime( Date time ) {
		this.header.modTime = time.getTime() / 1000;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdinin guncelleme zamani alir
	 *
	 * @return modTime
	 */
	public Date getModTime() {
		return new Date( this.header.modTime * 1000 );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdi dosyasini alir
	 *
	 * @return file
	 */
	public File getFile() {
		return this.file;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdi dosya boyutunu alir
	 *
	 * @return size
	 */
	public long getSize() {
		return this.header.size;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Girdi dosya boyutunu atar
	 *
	 * @param size
	 */
	public void setSize( long size ) {
		this.header.size = size;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Dizin kontrolu
	 *
	 * @return dizin ise TRUE
	 */
	public boolean isDirectory() {
		if ( this.file != null )                                                // Dosya kontrolu
			return this.file.isDirectory();
		if ( this.header != null ) {                                            // Baslik
			if ( this.header.linkFlag == TarHeader.LF_DIR )                     // Bayrak kontrolu
				return true;
			if ( this.header.name.toString().endsWith( "/" ) )                  // Dosya ismi kontrolu
				return true;
			}
		return false;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Bir dosyanin bilgileri ile basligi doldurur
	 *
	 * @param hdr : Baslik
	 * @param file : Dosya
	 */
	public void getFileTarHeader( TarHeader hdr, File file ) throws InvalidHeaderException {
		this.file = file;
		String name = file.getPath();                                           // Patrick Beard <beard@netscape.com> tarafindan yapilmistir
		String osname = System.getProperty( "os.name" );                        // Isletim sistemi kontrolu
		if ( osname != null ) {                                                 // Surucu karakterleri silinir
			String Win32Prefix = "windows";
			if ( osname.toLowerCase().startsWith( Win32Prefix ) ) {
				if ( name.length() > 2 ) {
					char ch1 = name.charAt(0);
					char ch2 = name.charAt(1);
					if ( ch2 == ':'	&& ( (ch1 >= 'a' && ch1 <= 'z')	|| (ch1 >= 'A' && ch1 <= 'Z') ) ) {
						name = name.substring( 2 );
						}
					}
				}
			}
		name = name.replace( File.separatorChar, '/' );                         // Dizin ayraclari degistirilir
		for ( ; name.startsWith( "/" ) ; )                                      // Yol baslangicindaki ayraclar silinir
			name = name.substring( 1 );
 		hdr.linkName = new StringBuffer( "" );
		hdr.name = new StringBuffer( name );                                    // Dosya ismi yazilir
		if ( file.isDirectory() ) {                                             // Dizin icin
			hdr.size = 0;                                                       // Boyut
			hdr.mode = 040755;                                                  // Erisimler
			hdr.linkFlag = TarHeader.LF_DIR;                                    // Bag bayragi
			if ( hdr.name.charAt( hdr.name.length() - 1 ) != '/' )              // Sonuna ayrac eklenir
				hdr.name.append( "/" );
			}
		else {                                                                  // Dosya icin
			hdr.size = file.length();                                           // Boyut
			hdr.mode = 0100644;                                                 // Erisimler
			hdr.linkFlag = TarHeader.LF_NORMAL;                                 // Bag bayragi
			}
		hdr.modTime = file.lastModified() / 1000;                               // Guncelleme zamani
		hdr.checkSum = 0;
		hdr.devMajor = 0;
		hdr.devMinor = 0;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Girdi dizin ise icindeki dosyalarin listesini dondurur
	 *
	 * @return icerik dizini
	 */
	public TarEntry[] getDirectoryEntries()	throws InvalidHeaderException {
		if ( this.file == null || ! this.file.isDirectory() ) {
			return new TarEntry[0];
			}
		String[] list = this.file.list();
		TarEntry[] result = new TarEntry[ list.length ];
		for ( int i = 0 ; i < list.length ; ++i ) {
			result[i] =	new TarEntry( new File( this.file, list[i] ) );
			}
		return result;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Basligin kontrol toplamini hesaplar
	 *
	 * @param buf : Baslik
	 * @return checksum
	 */
	public long computeCheckSum( byte[] buf ) {
		long sum = 0;
		for ( int i = 0 ; i < buf.length ; ++i ) {
			sum += 255 & buf[ i ];                                              // Tum icerigin deger olarak toplami
			}
		return sum;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Girdinin baslik bilgilerini bir baslik tamponuna yazar.
	 *
	 * @param outbuf 
	 * @throws InvalidHeaderException : Isim basliga sigmazsa
	 */
	public void writeEntryHeader( byte[] outbuf ) throws InvalidHeaderException {
		int offset = 0;
		if ( this.isUnixTarFormat() ) {
			if ( this.header.name.length() > 100 )
				throw new InvalidHeaderException( "file path is greater than 100 characters, " + this.header.name );
			}
		offset = TarHeader.getFileNameBytes( this.header.name.toString(), outbuf ); // Isim tampona yazilir
		offset = TarHeader.getOctalBytes( this.header.mode, outbuf, offset, TarHeader.MODELEN );    // Mod yazilir
		offset = TarHeader.getOctalBytes( this.header.userId, outbuf, offset, TarHeader.UIDLEN );   // Kullanici ID
		offset = TarHeader.getOctalBytes( this.header.groupId, outbuf, offset, TarHeader.GIDLEN );  // Grup ID
		long size = this.header.size;
		offset = TarHeader.getLongOctalBytes( size, outbuf, offset, TarHeader.SIZELEN );    // Boyut
		offset = TarHeader.getLongOctalBytes( this.header.modTime, outbuf, offset, TarHeader.MODTIMELEN );  // Guncelleme zamani
		int csOffset = offset;
		for ( int c = 0 ; c < TarHeader.CHKSUMLEN ; ++c )                       // Kontrol toplami alani
			outbuf[ offset++ ] = (byte) ' ';
		outbuf[ offset++ ] = this.header.linkFlag;                              // Bag gosterici
		offset = TarHeader.getNameBytes( this.header.linkName, outbuf, offset, TarHeader.NAMELEN ); // Bag ismi
		if ( this.unixFormat ) {
			for ( int i = 0 ; i < TarHeader.MAGICLEN ; ++i )                    // UNIX formati bosluk doldurma
				outbuf[ offset++ ] = 0;
			}
		else {
			offset = TarHeader.getNameBytes( this.header.magic, outbuf, offset, TarHeader.MAGICLEN );   // Format yazilir
			}
		offset = TarHeader.getNameBytes( this.header.userName, outbuf, offset, TarHeader.UNAMELEN );    // Kullanici ismi
		offset = TarHeader.getNameBytes( this.header.groupName, outbuf, offset, TarHeader.GNAMELEN );   // Grup ismi
		offset = TarHeader.getOctalBytes( this.header.devMajor, outbuf, offset, TarHeader.DEVLEN );     // Ana aygit
		offset = TarHeader.getOctalBytes( this.header.devMinor, outbuf, offset, TarHeader.DEVLEN );     // Ikincil aygit
		for ( ; offset < outbuf.length ; )                                                              // Bosluk doldurma
			outbuf[ offset++ ] = 0;
		long checkSum = this.computeCheckSum( outbuf );
		TarHeader.getCheckSumOctalBytes( checkSum, outbuf, csOffset, TarHeader.CHKSUMLEN );             // Kontrol toplami
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Baslik tamponundan girdi bilgilerini inceler ve basliga atar.
	 *
	 * UNIX stili kodu David Mehringer <dmehring@astro.uiuc.edu> tarafindan
     * yapilmistir.
	 *
	 * @param hdr : Tampondan doldurulacak baslik
	 * @param header : Tampon
	 */
	public void parseTarHeader( TarHeader hdr, byte[] headerBuf ) throws InvalidHeaderException {
		int offset = 0;
		if ( headerBuf[257] == 0	&& headerBuf[258] == 0 && headerBuf[259] == 0 && headerBuf[260] == 0 && headerBuf[261] == 0 ) {
			this.unixFormat = true;
			this.ustarFormat = false;                                           // UNIX formati
			this.gnuFormat = false;
			}
		else if ( headerBuf[257] == 'u' && headerBuf[258] == 's' && headerBuf[259] == 't' && headerBuf[260] == 'a' && headerBuf[261] == 'r'
				&& headerBuf[262] == 0 ) {
			this.ustarFormat = true;
			this.gnuFormat = false;                                             // USTAR formati
			this.unixFormat = false;
			}
		else if (  headerBuf[257] == 'u' && headerBuf[258] == 's' && headerBuf[259] == 't' && headerBuf[260] == 'a' && headerBuf[261] == 'r'
				&& headerBuf[262] != 0 && headerBuf[263] != 0 ) {
			this.gnuFormat = true;
			this.unixFormat = false;                                            // GNU formati
			this.ustarFormat = false;
			}
		else {
			StringBuffer buf = new StringBuffer( 128 );
			buf.append( "header magic is not 'ustar' or unix-style zeros, it is '" );
			buf.append( headerBuf[257] );
			buf.append( headerBuf[258] );
			buf.append( headerBuf[259] );
			buf.append( headerBuf[260] );                                       // Bilinmeyen format
			buf.append( headerBuf[261] );
			buf.append( headerBuf[262] );
			buf.append( headerBuf[263] );
			buf.append( "', or (dec) " );
			buf.append( (int)headerBuf[257] );
			buf.append( ", " );
			buf.append( (int)headerBuf[258] );
			buf.append( ", " );
			buf.append( (int)headerBuf[259] );
			buf.append( ", " );
			buf.append( (int)headerBuf[260] );
			buf.append( ", " );
			buf.append( (int)headerBuf[261] );
			buf.append( ", " );
			buf.append( (int)headerBuf[262] );
			buf.append( ", " );
			buf.append( (int)headerBuf[263] );
			throw new InvalidHeaderException( buf.toString() );
			}
		hdr.name = TarHeader.parseFileName( headerBuf );                        // Dosya ismi yazilir
		offset = TarHeader.NAMELEN;
		hdr.mode = (int)TarHeader.parseOctal( headerBuf, offset, TarHeader.MODELEN );   // Erisim modu
		offset += TarHeader.MODELEN;
		hdr.userId = (int)TarHeader.parseOctal( headerBuf, offset, TarHeader.UIDLEN );  // Kullanici ID
		offset += TarHeader.UIDLEN;
		hdr.groupId = (int)TarHeader.parseOctal( headerBuf, offset, TarHeader.GIDLEN ); // Grup ID
		offset += TarHeader.GIDLEN;
		hdr.size = TarHeader.parseOctal( headerBuf, offset, TarHeader.SIZELEN );        // Boyut
		offset += TarHeader.SIZELEN;
		hdr.modTime = TarHeader.parseOctal( headerBuf, offset, TarHeader.MODTIMELEN );  // Guncelleme zamani
		offset += TarHeader.MODTIMELEN;
		hdr.checkSum = (int)TarHeader.parseOctal( headerBuf, offset, TarHeader.CHKSUMLEN ); // KOntrol toplami
		offset += TarHeader.CHKSUMLEN;
		hdr.linkFlag = headerBuf[ offset++ ];                                   // Bag gosterici
		hdr.linkName = TarHeader.parseName( headerBuf, offset, TarHeader.NAMELEN ); // Bag ismi
		offset += TarHeader.NAMELEN;
		if ( this.ustarFormat ) {
			hdr.magic = TarHeader.parseName( headerBuf, offset, TarHeader.MAGICLEN );   // USTAR formati
			offset += TarHeader.MAGICLEN;
			hdr.userName = TarHeader.parseName( headerBuf, offset, TarHeader.UNAMELEN );    // Kullanici ismi
			offset += TarHeader.UNAMELEN;
			hdr.groupName =	TarHeader.parseName( headerBuf, offset, TarHeader.GNAMELEN );   // Grup ismi
			offset += TarHeader.GNAMELEN;
			hdr.devMajor = (int)TarHeader.parseOctal( headerBuf, offset, TarHeader.DEVLEN );    // Ana cihaz
			offset += TarHeader.DEVLEN;
			hdr.devMinor = (int)TarHeader.parseOctal( headerBuf, offset, TarHeader.DEVLEN );    // Ikincil cihaz
			}
		else {
			hdr.devMajor = 0;
			hdr.devMinor = 0;
			hdr.magic = new StringBuffer( "" );
			hdr.userName = new StringBuffer( "" );
			hdr.groupName = new StringBuffer( "" );
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Sadece girdi ismi verilen baslik doldur
     *
	 * @param hdr : Baslik
	 * @param name : Girdi ismi
	 */
	public void nameTarHeader( TarHeader hdr, String name ) {
		boolean isDir = name.endsWith( "/" );
		this.gnuFormat = false;
		this.ustarFormat = true;
		this.unixFormat = false;
		hdr.checkSum = 0;
		hdr.devMajor = 0;
		hdr.devMinor = 0;
		hdr.name = new StringBuffer( name );
		hdr.mode = isDir ? 040755 : 0100644;
		hdr.userId = 0;
		hdr.groupId = 0;
		hdr.size = 0;
		hdr.checkSum = 0;
		hdr.modTime = (new java.util.Date()).getTime() / 1000;
		hdr.linkFlag = isDir ? TarHeader.LF_DIR : TarHeader.LF_NORMAL;
		hdr.linkName = new StringBuffer( "" );
		hdr.userName = new StringBuffer( "" );
		hdr.groupName = new StringBuffer( "" );
		hdr.devMajor = 0;
		hdr.devMinor = 0;
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Tum baslik bilgilerini bir katar olarak dondurur
     *
     * @return TarEntry
     */
	public String toString() {
		StringBuffer result = new StringBuffer( 128 );
		return result. append( "[TarEntry name=" ).append( this.getName() ).append( ", isDir=" ).append( this.isDirectory() ).
			append( ", size=" ).append( this.getSize() ).append( ", userId=" ).append( this.getUserId() ).append( ", user=" ).
			append( this.getUserName() ).append( ", groupId=" ).append( this.getGroupId() ).append( ", group=" ).append( this.getGroupName() ).
			append( "]" ).toString();
		}
	}