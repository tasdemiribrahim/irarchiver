/*
** David M. Gaskin
*/
package external.publicDomain.tar;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * Bir tar dosyasinin icerigini sayar
 */
public class TarEntryEnumerator implements Enumeration {
	private TarInputStream	tis = null;
	private boolean			eof = false;
	private TarEntry		readAhead = null;
////////////////////////////////////////////////////////////////////////////////
	/**
     * Saymanin yapilacagi streami atar
	 */
	public TarEntryEnumerator( TarInputStream tis ) {
		this.tis      = tis;
		eof           = false;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Saymada siradaki elemani dondurur. java.util.Enumeration icin gereklidir
	 *
	 * @return siradaki eleman
	 * @exception NoSuchElementException : EOF sonrasini okuma cabasi
	 */
	public Object nextElement() throws NoSuchElementException {
		if ( eof && ( readAhead == null ) )
			throw new NoSuchElementException();
		TarEntry rc = null;
		if ( readAhead != null ) {
			rc        = readAhead;
			readAhead = null;
			}
		else {
			rc = getNext();
			}
		return rc;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Daha sayilacak eleman varsa true dondurur
	 */
	public boolean hasMoreElements() {
		if (eof)
			return false;
		boolean rc = false;
		readAhead = getNext();
		if ( readAhead != null )
			rc = true;
		return rc;
		}
////////////////////////////////////////////////////////////////////////////////
	/**
     * Siradaki elemani dondurur
	 *
	 * @return null siradaki eleman yoksa
	 */
	private TarEntry getNext() {
		TarEntry rc = null;
		try {
			rc = tis.getNextEntry();
			}
		catch ( IOException ex ) {
			ex.printStackTrace();
			}
		if ( rc == null )
			eof = true;
		return rc;
		}
	}