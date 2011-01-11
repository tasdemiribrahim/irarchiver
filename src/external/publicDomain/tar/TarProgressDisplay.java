/*
** Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
*/
package external.publicDomain.tar;
/**
 * Bu arayuz listeleme isleminin sonucunu gostermek icin gereklidir.
 */
public interface TarProgressDisplay {
	/**
     * Bir islem mesaji gosterir
	 *
	 * @param msg 
	 */
	public void showTarProgressMessage( String msg );
	}