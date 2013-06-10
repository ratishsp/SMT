/**
 * 
 */
package org.coursera.nlangp3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rpuduppully
 *
 */
public class IBMModel2Implementation {
	
	private static final String T_FILE = "D:/project/study/nlp/h3/tValues.txt";
	private static final String ENGLISH_FILE = "D:/project/study/nlp/h3/corpus.en";
	private static final String SPANISH_FILE = "D:/project/study/nlp/h3/corpus.es";
	
	private static final String TEST_ENGLISH_FILE = "D:/project/study/nlp/h3/test.en";
	private static final String TEST_SPANISH_FILE = "D:/project/study/nlp/h3/test.es";
	private static final String TEST_KEY_FILE = "D:/project/study/nlp/h3/test.key2.out";

	private static final String COMMA = ",";
	private static final String ATTHERATE = "@";
	private static final String SPACE = " ";
	private static final String NULL = "null";
	private static final String PIPE = "|";
	
	private Map<String, Integer> ejIndex = new HashMap<String, Integer>();
	private Map<String, Integer> fiIndex = new HashMap<String, Integer>();
	private Map<WordPairIndex,T> t = new HashMap<WordPairIndex,T>();
//	private Map<String,Float> q = new HashMap<String, Float>();
//	private Map<String,Map<String,Float>> qNested = new HashMap<String, Map<String,Float>>();
	private Map<Jilm, Float> qObj = new HashMap<Jilm, Float>();
	
	private Map<Jilm,Float> cJilm = new HashMap<Jilm, Float>();
	private Map<Ilm,Float> cIlm = new HashMap<Ilm, Float>();
	private Map<Integer,Float> cEj = new HashMap<Integer, Float>();
	
	public static void main(String[] args) {
		IBMModel2Implementation ibmModel2Implementation = new IBMModel2Implementation();
		ibmModel2Implementation.execute();
	}

	private void execute() {
		System.out.println("Step 1");
		initializeWordPairIndex();
		System.out.println("Step 2");
		readTValues();
		System.out.println("Step 3");
		initializeQ();
		
		

		System.out.println("Step 4");
		for(int i=0; i<5; i++){
			computeParameters();
			System.out.println("Step 5+i");
		}
		System.out.println("Step n");
		computeAlignments();
	}

	private void initializeWordPairIndex() {
		try {
			BufferedReader fileReaderEnglish = new BufferedReader(new FileReader(ENGLISH_FILE));
			BufferedReader fileReaderSpanish = new BufferedReader(new FileReader(SPANISH_FILE));
			
			int j = 0;
			int i = 0;
			String inputLineEnglish = null;
			String inputLineSpanish = null;
			ejIndex.put(NULL, 0);
			while((inputLineEnglish = fileReaderEnglish.readLine()) != null){
				inputLineSpanish = fileReaderSpanish.readLine();
				String[] spanishTokens = inputLineSpanish.split(SPACE);
				String[] englishTokens = inputLineEnglish.split(SPACE);
				
				for(String englishToken: englishTokens){
					if(!ejIndex.containsKey(englishToken)){
						ejIndex.put(englishToken,++j);
					}
				}
				for(String spanishToken: spanishTokens){
					if(!fiIndex.containsKey(spanishToken)){
						fiIndex.put(spanishToken, ++i);
					}
				}
			}
			fileReaderEnglish.close();
			fileReaderSpanish.close();
			System.out.println("ejIndex size" +ejIndex.size());
			System.out.println("fiIndex size" +fiIndex.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void computeAlignments() {
		try {
			BufferedReader fileReaderEnglish = new BufferedReader(new FileReader(TEST_ENGLISH_FILE));
			BufferedReader fileReaderSpanish = new BufferedReader(new FileReader(TEST_SPANISH_FILE));
			
			BufferedWriter fileWriterAlighment = new BufferedWriter(new FileWriter(TEST_KEY_FILE));
			String inputEnglishLine = null;
			int k=0;
			while((inputEnglishLine = fileReaderEnglish.readLine())!= null){
				k++;
				String inputSpanishLine = fileReaderSpanish.readLine();
				String [] argsSpanish = inputSpanishLine.split(SPACE);
				String [] argsEnglish = inputEnglishLine.split(SPACE);
				String[] englishPlusNullTokens = new String[argsEnglish.length +1];
				englishPlusNullTokens[0] = NULL;
				System.arraycopy(argsEnglish, 0, englishPlusNullTokens, 1, argsEnglish.length);
				
				int m = argsSpanish.length;
				int l = argsEnglish.length;
				for(int i=0; i<argsSpanish.length; i++){
					float max = 0.0f;
					int position = 0;
					for(int j=0; j<englishPlusNullTokens.length; j++){
						String figivenej = add(argsSpanish[i],PIPE,argsEnglish[j]);
//						String jilm = new StringBuilder().append(j).append(PIPE).append(i+1).append(COMMA).append(l).append(COMMA).append(m).toString();
						Jilm jilm = new Jilm((short)j, (short)(i+1), (short)l, (short)m);
						float tq = t.get(figivenej).getT() * qObj.get(jilm);
						if(max < tq){
							max = tq;
							position = j;
						}
					}
					if(position >0){
						fileWriterAlighment.write(k+SPACE+position + SPACE+ (i+1));
						fileWriterAlighment.newLine();
					}					
				}

			}
			
			fileReaderEnglish.close();
			fileReaderSpanish.close();
			fileWriterAlighment.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void initializeQ() {
		try {
			BufferedReader fileReaderEnglish = new BufferedReader(new FileReader(ENGLISH_FILE));
			BufferedReader fileReaderSpanish = new BufferedReader(new FileReader(SPANISH_FILE));
			
			String inputLineEnglish = null;
			String inputLineSpanish = null;
			int k=0;
			while((inputLineEnglish = fileReaderEnglish.readLine()) != null){
				inputLineSpanish = fileReaderSpanish.readLine();
				String[] spanishTokens = inputLineSpanish.split(SPACE);
				String[] englishTokens = inputLineEnglish.split(SPACE);
				int l = englishTokens.length;
				int m = spanishTokens.length;
				k++;
				if(k%50 == 0){
					System.out.println(k+ SPACE + qObj.size());
					
				}
				
				for(int iIndex=0; iIndex< spanishTokens.length; iIndex++){
					for(int jIndex=0; jIndex<englishTokens.length + 1; jIndex++){
						int j = jIndex;
						int i = iIndex + 1;
//						String jilm = new StringBuilder().append(j).append(PIPE).append(i).append(COMMA).append(l).append(COMMA).append(m).toString();
//						String lm = new StringBuilder().append(l).append(COMMA).append(m).toString();
//						String ji = new StringBuilder().append(j).append(COMMA).append(i).toString();
//						if(q.get(jilm) == null){
//							q.put(jilm,(float)1/(l+1));
//						}
//						if(qNested.get(lm) == null){
//							qNested.put(lm, new HashMap<String,Float>());
//						}
//						qNested.get(lm).put(ji, (float)1/(l+1));
						qObj.put(new Jilm((short)j, (short)i, (short)l, (short)m), (float)1/(l+1));
					}
				}
			}
			fileReaderEnglish.close();
			fileReaderSpanish.close();
		
			System.out.println("q size "+qObj.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void computeParameters() {
		try {
			BufferedReader fileReaderEnglish = new BufferedReader(new FileReader(ENGLISH_FILE));
			BufferedReader fileReaderSpanish = new BufferedReader(new FileReader(SPANISH_FILE));
			
			int k= 0;
			String inputLineEnglish = null;
			String inputLineSpanish = null;
			while((inputLineEnglish = fileReaderEnglish.readLine()) != null){
				k++;
				inputLineSpanish = fileReaderSpanish.readLine();
				String[] spanishTokens = inputLineSpanish.split(SPACE);
				String[] englishTokens = inputLineEnglish.split(SPACE);
				String[] englishPlusNullTokens = new String[englishTokens.length +1];
				englishPlusNullTokens[0] = NULL;
				System.arraycopy(englishTokens, 0, englishPlusNullTokens, 1, englishTokens.length);
				
				int l = englishTokens.length;
				int m = spanishTokens.length;
				
				if(k%50 ==0){
					System.out.println(k);
				}
				for(int iIndex=0; iIndex< spanishTokens.length; iIndex++){
					boolean sumComputed = false;
					float sum = 0.0f;
					float delta = 0.0f;
					for(int jIndex=0; jIndex<englishPlusNullTokens.length; jIndex++){
						int j = jIndex;
						int i = iIndex + 1;
						
						String ej = englishPlusNullTokens[jIndex];
//						String figivenej = add(spanishTokens[iIndex],PIPE,ej);
						int fiValue = fiIndex.get(spanishTokens[iIndex]);
						if(!sumComputed){
							sumComputed = true;
							for(int jSumIndex = 0; jSumIndex < englishPlusNullTokens.length; jSumIndex++){
//								String jilm = new StringBuilder().append(jSumIndex).append(PIPE).append(i).append(COMMA).append(l).append(COMMA).append(m).toString();
								Jilm jilm = new Jilm((short)jSumIndex, (short)i, (short)l, (short)m);
								WordPairIndex wpi = new WordPairIndex(ejIndex.get(englishPlusNullTokens[jSumIndex]), fiValue);
								sum += qObj.get(jilm) * t.get(wpi).getT();
							}
						}
//						String jilm = new StringBuilder().append(j).append(PIPE).append(i).append(COMMA).append(l).append(COMMA).append(m).toString();
						Jilm jilm = new Jilm((short)j, (short)i, (short)l, (short)m);
//						String ilm = new StringBuilder().append(i).append(COMMA).append(l).append(COMMA).append(m).toString();
						Ilm ilm = new Ilm((short)i, (short)l, (short)m);
						
						WordPairIndex wpi = new WordPairIndex(ejIndex.get(englishPlusNullTokens[jIndex]), fiValue);
						delta = qObj.get(jilm)* t.get(wpi).getT()/sum;
						if(cJilm.get(jilm) == null){
							cJilm.put(jilm, 0.0f);
						}
						if(cIlm.get(ilm) == null){
							cIlm.put(ilm, 0.0f);
						}
						Integer ejvalue = ejIndex.get(ej);
						if(cEj.get(ejvalue) == null){
							cEj.put(ejvalue, 0.0f);
						}
						
						cEj.put(ejvalue, cEj.get(ejvalue)+delta);
						cJilm.put(jilm, cJilm.get(jilm)+delta);
						cIlm.put(ilm, cIlm.get(ilm)+delta);
						T tValue = t.get(wpi);
						tValue.setC(tValue.getC()+delta);
					}
				}
			}
			
			
			for(Map.Entry<WordPairIndex, T> tEntry: t.entrySet()){
//				String[]args =tEntry.getKey().split("\\|");
				int ej = tEntry.getKey().getEj();
//				if(args.length >1){
//					ej = args[1];
//				}
//				ej = tEntry.getKey().getEj();
				float tValue = tEntry.getValue().getC()/cEj.get(ej);
				tEntry.setValue(new T(tValue));
			}
			
			for(Map.Entry<Jilm, Float> qEntry: qObj.entrySet()){
				Jilm jilm = qEntry.getKey();
//				String [] args = jilm.split("\\|");
//				String ilm = args[1];
				Ilm ilm = new Ilm(jilm.getI(), jilm.getL(), jilm.getM());
				qEntry.setValue(cJilm.get(jilm)/ cIlm.get(ilm));
			}
			fileReaderEnglish.close();
			fileReaderSpanish.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String add(String ... args) {
		StringBuilder sb = new StringBuilder();
		for(String arg:args){
			sb.append(arg);
		}
		return sb.toString();
	}
	private void readTValues() {
		try {
			BufferedReader fileReaderAlignment = new BufferedReader(new FileReader(T_FILE));
			
			String inputLine = null;
			while((inputLine = fileReaderAlignment.readLine())!= null){
				String[] args = inputLine.split(ATTHERATE);
				String[] ejfi = args[0].split("\\|");
				
//				if(ejfi.length == 1){
//					System.out.println(inputLine);
//				}
				String ej = (ejfi.length == 1 || ejfi[1] == null) ? "": ejfi[1];
				String fi = ejfi[0] == null ? "": ejfi[0];
				if(ejIndex.get(ej) == null || fiIndex.get(fi)==null){
//					System.out.println(inputLine);
				}else{
					t.put(new WordPairIndex(ejIndex.get(ej), fiIndex.get(fi)), new T(Float.valueOf(args[1])));
				}
			}
			fileReaderAlignment.close();
			System.out.println("t size "+t.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}

class Ilm{
	private short i;
	private short l;
	private short m;
	public short getI() {
		return i;
	}
	public void setI(short i) {
		this.i = i;
	}
	public short getL() {
		return l;
	}
	public void setL(short l) {
		this.l = l;
	}
	public short getM() {
		return m;
	}
	public void setM(short m) {
		this.m = m;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		result = prime * result + l;
		result = prime * result + m;
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
		Ilm other = (Ilm) obj;
		if (i != other.i)
			return false;
		if (l != other.l)
			return false;
		if (m != other.m)
			return false;
		return true;
	}
	public Ilm(short i, short l, short m) {
		super();
		this.i = i;
		this.l = l;
		this.m = m;
	}

	
}

class Jilm{
	private short j;
	private short i;
	private short l;
	private short m;
	
	
	public Jilm(short j, short i, short l, short m) {
		super();
		this.j = j;
		this.i = i;
		this.l = l;
		this.m = m;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		result = prime * result + j;
		result = prime * result + l;
		result = prime * result + m;
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
		Jilm other = (Jilm) obj;
		if (i != other.i)
			return false;
		if (j != other.j)
			return false;
		if (l != other.l)
			return false;
		if (m != other.m)
			return false;
		return true;
	}
	public short getJ() {
		return j;
	}
	public void setJ(short j) {
		this.j = j;
	}
	public short getI() {
		return i;
	}
	public void setI(short i) {
		this.i = i;
	}
	public short getL() {
		return l;
	}
	public void setL(short l) {
		this.l = l;
	}
	public short getM() {
		return m;
	}
	public void setM(short m) {
		this.m = m;
	}

	@Override
	public String toString() {
		return "Jilm [j=" + j + ", i=" + i + ", l=" + l + ", m=" + m + "]";
	}
	
	
}

class WordPair{
	private String ej;
	private String fi;
	public String getEj() {
		return ej;
	}
	public void setEj(String ej) {
		this.ej = ej;
	}
	public String getFi() {
		return fi;
	}
	public void setFi(String fi) {
		this.fi = fi;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ej == null) ? 0 : ej.hashCode());
		result = prime * result + ((fi == null) ? 0 : fi.hashCode());
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
		WordPair other = (WordPair) obj;
		if (ej == null) {
			if (other.ej != null)
				return false;
		} else if (!ej.equals(other.ej))
			return false;
		if (fi == null) {
			if (other.fi != null)
				return false;
		} else if (!fi.equals(other.fi))
			return false;
		return true;
	}
	
	public WordPair(String ej, String fi) {
		super();
		this.ej = ej;
		this.fi = fi;
	}
	
	
	
}

class WordPairIndex{
	private short ej;
	private short fi;
	
	public int getEj() {
		return ej;
	}
	public void setEj(short ej) {
		this.ej = ej;
	}
	public int getFi() {
		return fi;
	}
	public void setFi(short fi) {
		this.fi = fi;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ej;
		result = prime * result + fi;
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
		WordPairIndex other = (WordPairIndex) obj;
		if (ej != other.ej)
			return false;
		if (fi != other.fi)
			return false;
		return true;
	}
	
	public WordPairIndex(int ej, int fi) {
		super();
		this.ej = (short)ej;
		this.fi = (short)fi;
	}
	@Override
	public String toString() {
		return "WordPairIndex [ej=" + ej + ", fi=" + fi + "]";
	}
	
	
	
}
