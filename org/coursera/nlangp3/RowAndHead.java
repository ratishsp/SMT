/**
 * 
 */
package org.coursera.nlangp3;

/**
 * @author rpuduppully
 *
 */
public class RowAndHead {

	private int rowno;
	private int headno;
	
	public void setHeadno(int headno) {
		this.headno = headno;
	}
	
	public void setRowno(int rowno) {
		this.rowno = rowno;
	}
	
	public int getHeadno() {
		return headno;
	}
	
	public int getRowno() {
		return rowno;
	}
	
	public RowAndHead() {
		// TODO Auto-generated constructor stub
	}
	

	public RowAndHead(int rowno, int headno) {
		super();
		this.rowno = rowno;
		this.headno = headno;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + headno;
		result = prime * result + rowno;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RowAndHead other = (RowAndHead) obj;
		if (headno != other.headno)
			return false;
		if (rowno != other.rowno)
			return false;
		return true;
	}
	
	
	
}
