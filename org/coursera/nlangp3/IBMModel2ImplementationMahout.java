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

import org.apache.mahout.math.function.ObjectFloatProcedure;
import org.apache.mahout.math.map.OpenIntFloatHashMap;
import org.apache.mahout.math.map.OpenObjectFloatHashMap;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

/**
 * @author rpuduppully
 *
 */
public class IBMModel2ImplementationMahout {
	
	private static final String T_FILE = "/home/arya/nlp/h3/tValues.txt";
//	private static final String ENGLISH_FILE = "D:/project/study/nlp/h3/corpus.en";
//	private static final String SPANISH_FILE = "D:/project/study/nlp/h3/corpus.es";
	private static final String ENGLISH_FILE = "/home/arya/nlp/h3/corpus.en";
	private static final String SPANISH_FILE = "/home/arya/nlp/h3/corpus.es";

//	private static final String ENGLISH_FILE_TEST = "D:/project/study/nlp/h3/corpus_test.en";
//	private static final String SPANISH_FILE_TEST = "D:/project/study/nlp/h3/corpus_test.es";

	
	private static final String TEST_ENGLISH_FILE = "D:/project/study/nlp/h3/test.en";
	private static final String TEST_SPANISH_FILE = "D:/project/study/nlp/h3/test.es";
	private static final String TEST_KEY_FILE = "D:/project/study/nlp/h3/alignment_test.p2.out";
	private static final String DEV_KEY_FILE = "/home/arya/nlp/h3/dev.key2.out";

//	private static final String DEV_ENGLISH_FILE = "D:/project/study/nlp/h3/dev.en";
//	private static final String DEV_SPANISH_FILE = "D:/project/study/nlp/h3/dev.es";

	private static final String DEV_ENGLISH_FILE = "/home/arya/nlp/h3/dev.en";
	private static final String DEV_SPANISH_FILE = "/home/arya/nlp/h3/dev.es";
	
	private static final String COMMA = ",";
	private static final String ATTHERATE = "@";
	private static final String SPACE = " ";
	private static final String NULL = "null";
	private static final String PIPE = "|";
	
	
	private OpenObjectIntHashMap<String> ejIndex = new OpenObjectIntHashMap<String>();
	private OpenObjectIntHashMap<String> fiIndex = new OpenObjectIntHashMap<String>();
	private OpenObjectFloatHashMap<WordPairIndex> t = new OpenObjectFloatHashMap<WordPairIndex>(1000000);
	
//	private Map<String,Float> q = new HashMap<String, Float>();
//	private Map<String,Map<String,Float>> qNested = new HashMap<String, Map<String,Float>>();
//	private Map<Jilm, Float> qObj = new HashMap<Jilm, Float>();
	
	private OpenObjectFloatHashMap<Jilm> qObj = new OpenObjectFloatHashMap<Jilm>(2000000); 
	
	public static void main(String[] args) {
		IBMModel2ImplementationMahout ibmModel2Implementation = new IBMModel2ImplementationMahout();
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
			System.out.println("Step "+(5+i));
		}
		System.out.println("Step n");
//		System.out.println(ejIndex);
//		System.out.println(fiIndex);
//		System.out.println(qObj);
//		System.out.println(t);
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
			BufferedReader fileReaderEnglish = new BufferedReader(new FileReader(DEV_ENGLISH_FILE));
			BufferedReader fileReaderSpanish = new BufferedReader(new FileReader(DEV_SPANISH_FILE));
			
			BufferedWriter fileWriterAlighment = new BufferedWriter(new FileWriter(DEV_KEY_FILE));
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
					int position = -1;
					for(int j=0; j<englishPlusNullTokens.length; j++){
//						String figivenej = add(argsSpanish[i],PIPE,argsEnglish[j]);
//						String jilm = new StringBuilder().append(j).append(PIPE).append(i+1).append(COMMA).append(l).append(COMMA).append(m).toString();
						Jilm jilm = new Jilm((short)j, (short)(i+1), (short)l, (short)m);
						WordPairIndex wpi = new WordPairIndex(ejIndex.get(englishPlusNullTokens[j]), fiIndex.get(argsSpanish[i]));
						float tq = t.get(wpi) * qObj.get(jilm);
						if(max < tq){
							max = tq;
							position = j;
						}
					}
					if(position >-1){
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
//			int k=0;
			while((inputLineEnglish = fileReaderEnglish.readLine()) != null){
				inputLineSpanish = fileReaderSpanish.readLine();
				String[] spanishTokens = inputLineSpanish.split(SPACE);
				String[] englishTokens = inputLineEnglish.split(SPACE);
				int l = englishTokens.length;
				int m = spanishTokens.length;
//				k++;
//				if(k%50 == 0){
//					System.out.println(k+ SPACE + qObj.size());
//				}
				
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
			final OpenObjectFloatHashMap<Jilm> cJilm = new OpenObjectFloatHashMap<Jilm>(6500000);
			final OpenObjectFloatHashMap<Ilm> cIlm = new OpenObjectFloatHashMap<Ilm>();
			final OpenObjectFloatHashMap<WordPairIndex> cEjFi = new OpenObjectFloatHashMap<WordPairIndex>(2000000);
			final OpenIntFloatHashMap cEj = new OpenIntFloatHashMap();
//			int k= 0;
			String inputLineEnglish = null;
			String inputLineSpanish = null;
			while((inputLineEnglish = fileReaderEnglish.readLine()) != null){
//				k++;
				inputLineSpanish = fileReaderSpanish.readLine();
				String[] spanishTokens = inputLineSpanish.split(SPACE);
				String[] englishTokens = inputLineEnglish.split(SPACE);
				String[] englishPlusNullTokens = new String[englishTokens.length +1];
				englishPlusNullTokens[0] = NULL;
				System.arraycopy(englishTokens, 0, englishPlusNullTokens, 1, englishTokens.length);
				
				int l = englishTokens.length;
				int m = spanishTokens.length;
				
//				if(k%50 ==0){
//					System.out.println(k);
//				}
				for(int iIndex=0; iIndex< spanishTokens.length; iIndex++){
					boolean sumComputed = false;
					float sum = 0.0f;
					float delta = 0.0f;
					for(int jIndex=0; jIndex<englishPlusNullTokens.length; jIndex++){
						int j = jIndex;
						int i = iIndex + 1;
						
						String ej = englishPlusNullTokens[jIndex];
						String fi = spanishTokens[iIndex];
//						String figivenej = add(fi,PIPE,ej);
						int fiValue = fiIndex.get(fi);
						if(!sumComputed){
							sumComputed = true;
							for(int jSumIndex = 0; jSumIndex < englishPlusNullTokens.length; jSumIndex++){
//								String jilm = new StringBuilder().append(jSumIndex).append(PIPE).append(i).append(COMMA).append(l).append(COMMA).append(m).toString();
								Jilm jilm = new Jilm((short)jSumIndex, (short)i, (short)l, (short)m);
								WordPairIndex wpi = new WordPairIndex(ejIndex.get(englishPlusNullTokens[jSumIndex]), fiValue);
								sum += qObj.get(jilm) * t.get(wpi);
							}
						}
//						String jilm = new StringBuilder().append(j).append(PIPE).append(i).append(COMMA).append(l).append(COMMA).append(m).toString();
						Jilm jilm = new Jilm((short)j, (short)i, (short)l, (short)m);
//						String ilm = new StringBuilder().append(i).append(COMMA).append(l).append(COMMA).append(m).toString();
						Ilm ilm = new Ilm((short)i, (short)l, (short)m);
						int ejValue = ejIndex.get(ej);
						WordPairIndex wpi = new WordPairIndex(ejValue, fiValue);
						delta = qObj.get(jilm)* t.get(wpi)/sum;
						
//						if(cJilm.get(jilm) == null){
//							cJilm.put(jilm, 0.0f);
//						}
//						if(cIlm.get(ilm) == null){
//							cIlm.put(ilm, 0.0f);
//						}
						Integer ejvalue = ejValue;
//						if(cEj.get(ejvalue) == null){
//							cEj.put(ejvalue, 0.0f);
//						}
						
						cEj.put(ejvalue, cEj.get(ejvalue)+delta);
						cJilm.put(jilm, cJilm.get(jilm)+delta);
						cIlm.put(ilm, cIlm.get(ilm)+delta);
//						T tValue = t.get(wpi);
//						tValue.setC(tValue.getC()+delta);
						cEjFi.put(wpi, cEjFi.get(wpi)+delta);
					}
				}
			}
			
			
//			for(Map.Entry<WordPairIndex, T> tEntry: t.entrySet()){
//				int ej = tEntry.getKey().getEj();
//				float tValue = tEntry.getValue().getC()/cEj.get(ej);
//				tEntry.setValue(new T(tValue));
//			}
			
			t.forEachPair(new ObjectFloatProcedure<WordPairIndex>() {
				
				@Override
				public boolean apply(WordPairIndex wpi, float value) {
					t.put(wpi, cEjFi.get(wpi)/cEj.get(wpi.getEj()));
					return true;
				}
			});
			
			qObj.forEachPair(new ObjectFloatProcedure<Jilm>() {
				
				@Override
				public boolean apply(Jilm jilm, float second) {
					Ilm ilm = new Ilm(jilm.getI(), jilm.getL(), jilm.getM());
					qObj.put(jilm, cJilm.get(jilm)/ cIlm.get(ilm));
					return true;
				}
			});
//			for(Map.Entry<Jilm, Float> qEntry: qObj.entrySet()){
//				Jilm jilm = qEntry.getKey();
////				String [] args = jilm.split("\\|");
////				String ilm = args[1];
//				Ilm ilm = new Ilm(jilm.getI(), jilm.getL(), jilm.getM());
//				qEntry.setValue(cJilm.get(jilm)/ cIlm.get(ilm));
//			}
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
//				if(ejIndex.get(ej) == null || fiIndex.get(fi)==null){
//					System.out.println(inputLine);
//				}else{
//				t.put(new WordPairIndex(ejIndex.get(ej), fiIndex.get(fi)), new T(Float.valueOf(args[1])));
				t.put(new WordPairIndex(ejIndex.get(ej), fiIndex.get(fi)), Float.valueOf(args[1]));
//				}
			}
			fileReaderAlignment.close();
			System.out.println("t size "+t.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}

