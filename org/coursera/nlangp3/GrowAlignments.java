/**
 * 
 */
package org.coursera.nlangp3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author rpuduppully
 *
 */
public class GrowAlignments {

	private static final String FILE1 = "/home/arya/nlp/h3/dev.key.out";
	private static final String FILE2 = "/home/arya/nlp/h3/dev.key_ej.out";
	private static final String OUTPUT_FILE = "/home/arya/nlp/h3/dev.key_combined.out";
	private static final String GROWN_FILE = "/home/arya/nlp/h3/dev.key_grown.out";
	private static final String INTERSECT_FILE = "/home/arya/nlp/h3/dev.key_intersect.out";
	private static final String DEV_EN = "/home/arya/nlp/h3/dev.en";
	private static final String DEV_ES = "/home/arya/nlp/h3/dev.es";
	private static final String SPACE = " ";
	private List<String> alignmentsList = new ArrayList<String>();
	private List<String> alignmentsIntersectList = new ArrayList<String>();
	private List<String> alignmentsGrownList = new ArrayList<String>();
	private int [] devenLen = new int [200];
	private int [] devesLen = new int [200];
	private Map<RowAndHead, Byte> rowE = new HashMap<RowAndHead,Byte>();
	private Map<RowAndHead, Byte> rowF = new HashMap<RowAndHead,Byte>();
	
	public static void main(String[] args) {
		GrowAlignments growAlignments = new GrowAlignments();
		growAlignments.compute();
		growAlignments.grow();
	}

	private void grow() {
		try{
			BufferedReader br1 = new BufferedReader(new FileReader(DEV_EN));
			BufferedReader br2 = new BufferedReader(new FileReader(DEV_ES));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(GROWN_FILE));

			String readLine = null;
			int index =0;
			while((readLine = br1.readLine())!=null){
				devenLen[index] = readLine.split(SPACE).length;
				index++;
			}
			
			index =0;
			while((readLine = br2.readLine())!=null){
				devesLen[index] = readLine.split(SPACE).length;
				index++;
			}
		
			for (Iterator<String> intersectIterator = alignmentsIntersectList.iterator(); intersectIterator.hasNext();) {
				String row = intersectIterator.next();
				String [] elements = row.split(SPACE);
				
				int rowNum = Integer.parseInt(elements[0]);
				int i = Integer.parseInt(elements[1]);
				int j = Integer.parseInt(elements[2]);
				
				rowE.put(new RowAndHead(rowNum,i), (byte)1);
				rowF.put(new RowAndHead(rowNum,j), (byte)1);
			}
				
			alignmentsGrownList.addAll(alignmentsIntersectList);
			for (Iterator<String> intersectIterator = alignmentsIntersectList.iterator(); intersectIterator.hasNext();) {
				String row = intersectIterator.next();
				String [] elements = row.split(SPACE);
				
				int rowNum = Integer.parseInt(elements[0]);
				int i = Integer.parseInt(elements[1]);
				int j = Integer.parseInt(elements[2]);

				//and alignment
				for(int iIndex =i-1; iIndex < i+1; iIndex++){
					for(int jIndex= j-1; jIndex<j+1; jIndex++){
						if(alignmentsList.contains(""+rowNum + SPACE+ iIndex+SPACE+jIndex) && (!rowE.containsKey(new RowAndHead(rowNum,iIndex)) && !rowF.containsKey(new RowAndHead(rowNum, jIndex)))){
							alignmentsGrownList.add(""+rowNum + SPACE+ iIndex+SPACE+jIndex);
							rowE.put(new RowAndHead(rowNum,iIndex), (byte)1);
							rowF.put(new RowAndHead(rowNum,jIndex), (byte)1);
						}
					}
				}
			}
			
			//or alignment
			for(int rowIndex = 0; rowIndex <200; rowIndex++){
				for(int iIndex =0; iIndex< devenLen[rowIndex]; iIndex++){
					for(int jIndex=0; jIndex< devesLen[rowIndex]; jIndex++){
						String key = (rowIndex+1) + SPACE + (iIndex+1)+ SPACE+ (jIndex +1);
						if(alignmentsList.contains(key) && !(rowE.containsKey(new RowAndHead(rowIndex+1, iIndex+1)) && rowF.containsKey(new RowAndHead(rowIndex+1, jIndex+1)))){
							alignmentsGrownList.add(key);
							rowE.put(new RowAndHead(rowIndex+1,iIndex+1), (byte)1);
							rowF.put(new RowAndHead(rowIndex+1,jIndex+1), (byte)1);							
						}
					}
				}
			}
			
			Collections.sort(alignmentsGrownList, new IntegerComparator());
			for (Iterator<String> alignmentsGrownIterator = alignmentsGrownList.iterator(); alignmentsGrownIterator.hasNext();) {
				bw1.write(alignmentsGrownIterator.next());
				bw1.newLine();
			}
			br1.close();
			br2.close();
			bw1.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void compute() {
		Set<String> alignments = new HashSet<String>();
		Set<String> alignmentsEj = new HashSet<String>();
		Set<String> alignmentsFi = new HashSet<String>();

		
		try{
			BufferedReader br1 = new BufferedReader(new FileReader(FILE1));
			BufferedReader br2 = new BufferedReader(new FileReader(FILE2));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(OUTPUT_FILE));
			
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(INTERSECT_FILE));
			String readLine = null;
			while((readLine = br1.readLine())!=null){
				alignments.add(readLine);
				alignmentsEj.add(readLine);
			}
			
			while((readLine = br2.readLine())!= null){
				alignments.add(readLine);
				alignmentsFi.add(readLine);
			}
		
			alignmentsList.addAll(alignments);
		
			
			Collections.sort(alignmentsList, new IntegerComparator());
			for (Iterator<String> iterator = alignmentsList.iterator(); iterator.hasNext();) {
				bw1.write(iterator.next());
				bw1.newLine();
			}
			
			alignmentsEj.retainAll(alignmentsFi);
			
			alignmentsIntersectList.addAll(alignmentsEj);
			Collections.sort(alignmentsIntersectList, new IntegerComparator());
			for (Iterator<String> iterator = alignmentsIntersectList.iterator(); iterator.hasNext();) {
				bw2.write(iterator.next());
				bw2.newLine();
			}
			
			br1.close();
			br2.close();
			bw1.close();
			bw2.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
}
