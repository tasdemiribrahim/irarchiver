/*
** Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
*/
package external.publicDomain.tar;

import java.io.*;
import javax.activation.*;
/**
 * Tar arsivi dosya nesnelerini temsil eden girdiler dizisidir. Arsivdeki her
 * girdi bir baslik kayidi tutar. Dizin girdileri sadece baslik kayidindan
 * olusur. Kayitlar 512 byte uzunlugundadir.
 * Tar arsivleri bir InputStream veya OutputStream ile cagrilmalarina gore okuma
 * veya yazma modunda baslatilir. Bir kez baslatilinca arsivin modu
 * degistirilemez
 * Tar arsivine rastgele erisim destegi yoktur. Ancak TarBuffer.
 * getCurrentRecordNum() ve TarBuffer.getCurrentBlockNum() kullanilarak bazi
 * islemler yapilabilir.
 */
public class TarArchive extends Object	{
	protected boolean			verbose;
	protected boolean			debug;
	protected boolean			keepOldFiles;
	protected boolean			asciiTranslate;
	protected int				userId;
	protected String			userName;
	protected int				groupId;
	protected String			groupName;
	protected String			rootPath;
	protected String			tempPath;
	protected String			pathPrefix;
	protected int				recordSize;
	protected byte[]			recordBuf;
	protected TarInputStream	tarIn;
	protected TarOutputStream	tarOut;
	protected TarTransFileTyper		transTyper;
	protected TarProgressDisplay	progressDisplay;
////////////////////////////////////////////////////////////////////////////////
	/**
     * InputStream tabanli standart blok ve kayit boyutlu kurucu.
     *
     * @param inStream
     */
	public	TarArchive( InputStream inStream ){
		this( inStream, TarBuffer.DEFAULT_BLKSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * InputStream tabanli verilen blok ve standart kayit boyutlu kurucu.
     *
     * @param inStream
     * @param blockSize
     */
	public	TarArchive( InputStream inStream, int blockSize ) {
		this( inStream, blockSize, TarBuffer.DEFAULT_RCDSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * InputStream tabanli verilen blok ve kayit boyutlu kurucu.
     *
     * @param inStream
     * @param blockSize
     * @param recordSize
     */
	public TarArchive( InputStream inStream, int blockSize, int recordSize ) {
		this.tarIn = new TarInputStream( inStream, blockSize, recordSize );
		this.initialize( recordSize );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * OutputStream tabanli standart blok ve kayit boyutlu kurucu.
     *
     * @param outStream
     */
	public	TarArchive( OutputStream outStream ) {
		this( outStream, TarBuffer.DEFAULT_BLKSIZE );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * OutputStream tabanli verilen blok ve standart kayit boyutlu kurucu.
     *
     * @param outStream
     * @param blockSize
     */
	public	TarArchive( OutputStream outStream, int blockSize )	{
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
	public	TarArchive( OutputStream outStream, int blockSize, int recordSize )	{
		this.tarOut = new TarOutputStream( outStream );
		this.initialize( recordSize );
		}
////////////////////////////////////////////////////////////////////////////////
    /**
     * Baslangic degerlerini atar
     *
     * @param recordSize
     */
	private void initialize( int recordSize ) {
		this.rootPath = null;
		this.pathPrefix = null;
		this.tempPath = System.getProperty( "user.dir" );
		this.userId = 0;
		this.userName = "";
		this.groupId = 0;
		this.groupName = "";
		this.debug = false;
		this.verbose = false;
		this.keepOldFiles = false;
		this.progressDisplay = null;
		this.recordBuf = new byte[ this.getRecordSize() ];
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Debugging bayragini atar
	 *
	 * @param debugF : Yeni debug ayari
	 */
	public void setDebug( boolean debugF ) {
		this.debug = debugF;
		if ( this.tarIn != null )
			this.tarIn.setDebug( debugF );
		else if ( this.tarOut != null )
			this.tarOut.setDebug( debugF );
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Uzunluk ayarini dondurur
	 *
	 * @return verbose
	 */
	public boolean isVerbose() {
		return this.verbose;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Uzunluk bayragini atar
	 *
	 * @param verbose : Yeni uzunluk ayari
	 */
	public void	setVerbose( boolean verbose ) {
		this.verbose = verbose;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsivleme islemi gelisimini goruntulemeye yarar
	 *
	 * @param display : Gelisim goruntuleme arayuzu
	 */
	public void	setTarProgressDisplay( TarProgressDisplay display )	{
		this.progressDisplay = display;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Ustune yazma bayragini atar
	 *
	 * @param keepOldFiles : Eski dosyalari saklama ayari
	 */
	public void	setKeepOldFiles( boolean keepOldFiles )	{
		this.keepOldFiles = keepOldFiles;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * ASCII dosya ceviri bayragini atar. Bayrak TRUE ise MIME dosya tipi
     * 'text/*' seklinde mi belirler. Eger MIME tipi bulunamazsa TransFileTyper
     * kullanilir. Eger ikisi de ASCII degil derse cevirme yapilir. Cevirme
     * isletim sistemi dosya ayraclarini UNIX ayracina cevirir. Bu islem bir tar
     * standartidir.
     *
	 * @param asciiTranslate : Cevirme ayari
	 */
	public void	setAsciiTranslation( boolean asciiTranslate ) {
		this.asciiTranslate = asciiTranslate;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * ASCII tipini belirleyecek nesneyi atar.
	 *
	 * @param transTyper : Yeni TransFileTyper nesnesi
	 */
	public void	setTransFileTyper( TarTransFileTyper transTyper ) {
		this.transTyper = transTyper;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Basliga koyulacak kullanici ve grup bilgilerini atar.
	 *
	 * @param userId
	 * @param userName
	 * @param groupId
	 * @param groupName
	 */
	public void	setUserInfo( int userId, String userName, int groupId, String groupName ) {
		this.userId = userId;
		this.userName = userName;
		this.groupId = groupId;
		this.groupName = groupName;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Kullanici ID bilgisini alir
     *
	 * @return userId.
	 */
	public int getUserId() {
		return this.userId;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Kullanici isim bilgisini alir
	 *
	 * @return userName
	 */
	public String getUserName()	{
		return this.userName;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Grup ID bilgisini alir.
     *
	 * @return groupId
	 */
	public int getGroupId() {
		return this.groupId;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Grup isim bilgisini alir
     *
	 * @return groupName
	 */
	public String getGroupName() {
		return this.groupName;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Gecici dizin yolunu alir. Java versiyon 1.2 ye kadar gecici dosyalari
     * desteklemediginden bu sistem gelistirilmistir. Gecici dizin 'user.dir'
     * sistem ozelligini kullanir.
	 *
	 * @return tempPath
	 */
	public String getTempDirectory() {
		return this.tempPath;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Gecici dizin yolunu atar.
     *
	 * @param path : Yeni gecici dizin yolu
	 */
	public void	setTempDirectory( String path )	{
		this.tempPath = path;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Kayit boyutunu alir. Kayit, blok, tamponlanmis IO kullanilir.
	 *
	 * @return kayit boyutu
	 */
	public int getRecordSize() {
		if ( this.tarIn != null ) {
			return this.tarIn.getRecordSize();
			}
		else if ( this.tarOut != null ) {
			return this.tarOut.getRecordSize();
			}
		return TarBuffer.DEFAULT_RCDSIZE;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Verilen dosya icin gecici dosya yolu alir. Gecici dosya yaratilmaz. Dosya
     * isim cakismalarini engellemeye calisir. Bu yuzden isim essizdir.
	 *
	 * @return gecici dosya yolu
	 */
	private String getTempFilePath( File eFile ) {
		String pathStr = this.tempPath + File.separator	+ eFile.getName() + ".tmp";
		for ( int i = 1 ; i < 5 ; ++i )	{
			File f = new File( pathStr );
			if ( ! f.exists() )
				break;
			pathStr = this.tempPath + File.separator + eFile.getName() + "-" + i + ".tmp";
			}
		return pathStr;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsivi kapatir. close() metodunu cagirir.
	 */
	public void closeArchive() throws IOException {
		if ( this.tarIn != null ) {
			this.tarIn.close();
			}
		else if ( this.tarOut != null )	{
			this.tarOut.close();
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * "list" komutunu yapar ve arsiv icerigini listeler. progressDisplay
     * kapaliysa hicbirsey yapmaz.
	 */
	public void	listContents() throws IOException, InvalidHeaderException {
		for ( ; ; )	{
			TarEntry entry = this.tarIn.getNextEntry();
			if ( entry == null ) {
				if ( this.debug ) {
					System.err.println( "READ EOF RECORD" );
					}
				break;
				}
			if ( this.progressDisplay != null )
				this.progressDisplay.showTarProgressMessage( entry.getName() );
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * "extract" komutunu isler ve arsiv icerigini acar. Her girdi icin
     * extractEntry() metodunu cagirir
     *
	 * @param destDir : Dosyalarin yazilacagi hedef dizin
	 */
	public void extractContents( File destDir )	throws IOException, InvalidHeaderException {
		for ( ; ; )	{
			TarEntry entry = this.tarIn.getNextEntry();
			if ( entry == null ) {
				if ( this.debug ) {
					System.err.println( "READ EOF RECORD" );
					}
				break;
				}
			this.extractEntry( destDir, entry );
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsivden bir girdi cikarir. getNextEntry() ile alinan girdiyi hedef
     * dizine yazar.
	 * 
	 * @param destDir : Dosyanin yazilacagi hedef dizin
	 * @param entry : tarIn.getNextEntry() tarafindan alinan girdi
	 */
	private void extractEntry( File destDir, TarEntry entry ) throws IOException {
		if ( this.verbose )	{
			if ( this.progressDisplay != null )
				this.progressDisplay.showTarProgressMessage( entry.getName() );
			}
		String name = entry.getName();
		name = name.replace( '/', File.separatorChar );
		File destFile = new File( destDir, name );
		if ( entry.isDirectory() ) {                                            // Girdi dizin ise
			if ( ! destFile.exists() ) {
				if ( ! destFile.mkdirs() ) {                                    // Dizin yaratilir
					throw new IOException( "error making directory path '" + destFile.getPath() + "'" );
					}
				}
			}
		else {
			File subDir = new File( destFile.getParent() );                     // Ebeveyn dizin alinir
			if ( ! subDir.exists() ) {
				if ( ! subDir.mkdirs() ) {                                      // Ebeveyn dizin yaratilir
					throw new IOException( "error making directory path '" + subDir.getPath() + "'" );
					}
				}
			if ( this.keepOldFiles && destFile.exists() ) {                     // Dosya varsa ve ustune yazma yapilmayacaksa
				if ( this.verbose ) {
					if ( this.progressDisplay != null )
                        this.progressDisplay.showTarProgressMessage( "not overwriting " + entry.getName() );
					}
				}
			else {
				boolean asciiTrans = false;
				FileOutputStream out = new FileOutputStream( destFile );        // OutputStream olusturulur
				if ( this.asciiTranslate ) {                                    // ASCII bayragi varsa
					MimeType mime = null;
					String contentType = null;
					try {
						contentType = FileTypeMap.getDefaultFileTypeMap().getContentType( destFile ); // Icerik tipi alinir
						mime = new MimeType( contentType );
						if ( mime.getPrimaryType().equalsIgnoreCase( "text" ) )	{ // MIME kontrolu
							asciiTrans = true;
							}
						else if ( this.transTyper != null ) {
							if ( this.transTyper.isAsciiFile( entry.getName() ) ) { // TransFileTyper kontrolu
								asciiTrans = true;
								}
							}
						}
					catch ( MimeTypeParseException ex ){ }
					if ( this.debug ) {
						System.err.println( "EXTRACT TRANS? '" + asciiTrans	+ "'  ContentType='" + contentType 
                                + "'  PrimaryType='" + mime.getPrimaryType() + "'" );
						}
					}
				PrintWriter outw = null;
				if ( asciiTrans ) {
					outw = new PrintWriter( out );
					}
				byte[] rdbuf = new byte[32 * 1024];                             // Okuma tamponu
				for ( ; ; ) {
					int numRead = this.tarIn.read( rdbuf );                     // Tampona okuma yapilir
					if ( numRead == -1 )
						break;
					if ( asciiTrans ) {
						for ( int off = 0, b = 0 ; b < numRead ; ++b ) {
							if ( rdbuf[ b ] == 10 ) {
								String s = new String( rdbuf, off, (b - off) ); // ASCII donusumu yapilir
								outw.println( s );                              // Degerler yazilir
								off = b + 1;
								}
							}
						}
					else {
						out.write( rdbuf, 0, numRead );                         // Okunan degerler yazilir
						}
					}
				if ( asciiTrans )                                               // Stream kapatilir
					outw.close();
				else
					out.close();
				}
			}
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Arsive bir girdi yazar. Bu metod putNextEntry() cagirir ve girdiyi yazar
     * ve enson closeEntry() cagirir. Dizinler icin putNextEntry() cagirir ve
     * recurse bayragi varsa dizinin icindeki girdileri isler.
     *
	 * @param entry : Arsive yazilacak girdi
	 * @param recurse : Dizin icerigi islenecek mi
	 */
	public void writeEntry( TarEntry oldEntry, boolean recurse ) throws IOException {
		boolean asciiTrans = false;
		boolean unixArchiveFormat = oldEntry.isUnixTarFormat();
		File tFile = null;
		File eFile = oldEntry.getFile();
		TarEntry entry = (TarEntry) oldEntry.clone();                           // Girdinin kopyasi alinir. Boylece istedigimiz gibi oynayabiliriz
		if ( this.verbose ) {
			if ( this.progressDisplay != null )
				this.progressDisplay.showTarProgressMessage( entry.getName() );
			}
		if ( this.asciiTranslate && ! entry.isDirectory() ) {                   // ASCII kontrol ve donusum
			MimeType mime = null;
			String contentType = null;
			try {
				contentType = FileTypeMap.getDefaultFileTypeMap().getContentType( eFile );  // Icerik tipi alinir
				mime = new MimeType( contentType );
				if ( mime.getPrimaryType().equalsIgnoreCase( "text" ) ) {       // MIME kontrol
					asciiTrans = true;
					}
				else if ( this.transTyper != null ) {
					if ( this.transTyper.isAsciiFile( eFile ) ) {               // TransFileTyper kontrolu
						asciiTrans = true;
						}
					}
				}
			catch ( MimeTypeParseException ex )	{ }
			if ( this.debug ) {
				System.err.println( "CREATE TRANS? '" + asciiTrans + "'  ContentType='" + contentType
						+ "'  PrimaryType='" + mime.getPrimaryType() + "'" );
				}
			if ( asciiTrans ) {
				String tempFileName = this.getTempFilePath( eFile );
				tFile = new File( tempFileName );                               // Gecici dosya ile streamler olusturulur
				BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( eFile ) ) );
				BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( tFile ) );
				for ( ; ; ) {
					String line = in.readLine();                                // Veri okunur
					if ( line == null )
						break;
					out.write( line.getBytes() );
					out.write( (byte)'\n' );                                    // Veri gecici dosyaya yazilir
					}
				in.close();                                                     // Kapatma yapilir
				out.flush();
				out.close();
				entry.setSize( tFile.length() );
				eFile = tFile;
				}
			}
		String newName = null;
		if ( this.rootPath != null ) {
			if ( entry.getName().startsWith( this.rootPath ) ) {                // Kok dizin isimden cikarilir
				newName = entry.getName().substring ( this.rootPath.length() + 1 );
				}
			}
		if ( this.pathPrefix != null ) {                                        // Isim ontakisi(dizin) eklenir
			newName = (newName == null) ? this.pathPrefix + "/" + entry.getName() : this.pathPrefix + "/" + newName;
			}
		if ( newName != null ) {                                                // Isim guncellenir
			entry.setName( newName );
			}
		this.tarOut.putNextEntry( entry );                                      // Girdi eklenir
		if ( entry.isDirectory() ) {                                            // Girdi dizin ise
			if ( recurse ) {
				TarEntry[] list = entry.getDirectoryEntries();                  // Dizin icerigi alinir
				for ( int i = 0 ; i < list.length ; ++i ) {
					TarEntry dirEntry = list[i];
					if ( unixArchiveFormat )
						dirEntry.setUnixTarFormat();
					this.writeEntry( dirEntry, recurse );                       // Dizin icerigi yazilir
					}
				}
			}
		else {                                                                  // Girdi dosya ise
			FileInputStream in = new FileInputStream( eFile );
			byte[] eBuf = new byte[ 32 * 1024 ];
			for ( ; ; ) {
				int numRead = in.read( eBuf, 0, eBuf.length );                  // Veri okunur
				if ( numRead == -1 )
					break;
				this.tarOut.write( eBuf, 0, numRead );                          // Veri yazilir
				}
			in.close();
			if ( tFile != null ) {                                              // Gecici dosya silinir
				tFile.delete();
				}
			this.tarOut.closeEntry();                                           // Yazma islemi biter
			}
		}
	}