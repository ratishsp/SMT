/**
 * 
 */
package org.coursera.nlangp3;

import java.util.Comparator;

/**
 * @author rpuduppully
 *
 */
public class IntegerComparator implements Comparator<String> {

	private static final String SPACE = " ";
	@Override
	public int compare(String o1, String o2) {
		String[] o1row = o1.split(SPACE);
		String[] o2row = o2.split(SPACE);
		int comparison = Integer.compare(Integer.parseInt(o1row[0]),Integer.parseInt(o2row[0]));
		
		if(comparison ==0){
			comparison = Integer.compare(Integer.parseInt(o1row[1]),Integer.parseInt(o2row[1]));
		}
		
		return comparison;
	}
}
