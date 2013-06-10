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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author rpuduppully
 *
 */
public class TranslationAlignments {
	
	private static final String COMMA = ",";
	private static final String NULL = "null";
	private static final String PIPE = "|";
	private static final String ATTHERATE = "@";
	private static final String SPACE = " ";
	private static final String ENGLISH_FILE_TEST = "D:/project/study/nlp/h3/corpus_test.en";
	private static final String SPANISH_FILE_TEST = "D:/project/study/nlp/h3/corpus_test.es";
//	private static final String DEV_ENGLISH_FILE = "D:/project/study/nlp/h3/dev.en";
//	private static final String DEV_SPANISH_FILE = "D:/project/study/nlp/h3/dev.es";
	private static final String DEV_ENGLISH_FILE = "/home/arya/nlp/h3/dev.en";
	private static final String DEV_SPANISH_FILE = "/home/arya/nlp/h3/dev.es";

//	private static final String DEV_KEY_FILE = "D:/project/study/nlp/h3/dev.key.out";
	private static final String DEV_KEY_FILE = "/home/arya/nlp/h3/dev.key.out";

	private static final String TEST_ENGLISH_FILE = "D:/project/study/nlp/h3/test.en";
	private static final String TEST_SPANISH_FILE = "D:/project/study/nlp/h3/test.es";
	private static final String TEST_KEY_FILE = "D:/project/study/nlp/h3/test.key.out";

	
//	private static final String ENGLISH_FILE = "D:/project/study/nlp/h3/corpus.en";
//	private static final String SPANISH_FILE = "D:/project/study/nlp/h3/corpus.es";
	private static final String ENGLISH_FILE = "/home/arya/nlp/h3/corpus.en";
	private static final String SPANISH_FILE = "/home/arya/nlp/h3/corpus.es";
	private static final String T_FILE = "/home/arya/nlp/h3/tValues.txt";
	private Map<String,T> tInitial;
//	private Map<String,Map<String,Float>> t;
	public static void main(String[] args) {
		TranslationAlignments translationAlignments = new TranslationAlignments();
		System.out.println("Step 1");
		translationAlignments.initialize();
		
		System.out.println("Step 2");
		for(int i=0; i<5; i++){
			translationAlignments.executeEM();
			System.out.println("Step "+ (i+3));
		}
		translationAlignments.assignAlighments();
		translationAlignments.writeTToFile();
		
	}

	private void writeTToFile() {
		try {
			BufferedWriter fileWriterAlignment = new BufferedWriter(new FileWriter(T_FILE));
			for(Map.Entry<String, T> entry: tInitial.entrySet()){
				fileWriterAlignment.write(entry.getKey() + ATTHERATE+ entry.getValue().getT());
				fileWriterAlignment.newLine();
			}
			fileWriterAlignment.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void assignAlighments() {

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
				 
				for(int i=0; i<argsSpanish.length; i++){
					float max = 0.0f;
					int position = 0;
					for(int j=0; j<argsEnglish.length; j++){
						String figivenej = add(argsSpanish[i],PIPE,argsEnglish[j]);
						if(tInitial.get(figivenej)!= null && max < tInitial.get(figivenej).getT()){
							max = tInitial.get(figivenej).getT();
							position = j+1;
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
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	private void executeEM() {
		try {
			System.out.println("a");
			BufferedReader fileReaderEnglish = new BufferedReader(new FileReader(ENGLISH_FILE));
			BufferedReader fileReaderSpanish = new BufferedReader(new FileReader(SPANISH_FILE));
//			Map<String, Float> delta = new HashMap<String,Float>();
//			Map<String,Float> countEjFi = new HashMap<String,Float>(1<<22);
			Map<String,Float> countEj = new HashMap<String,Float>();
			
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
//				if(k%50 ==0){
//					System.out.println(k);
////					System.out.println("size of ejfi "+countEjFi.size());
////					System.out.println("size of ej "+countEj.size());
//				}
				for(int iIndex=0; iIndex< spanishTokens.length; iIndex++){
					boolean sumComputed = false;
					float sum = 0.0f;
					for(int jIndex=0; jIndex<englishPlusNullTokens.length; jIndex++){
//						int i = iIndex+1;
//						int j = jIndex;
//						System.out.println(englishPlusNullTokens[jIndex]+SPACE+ spanishTokens[iIndex]);
//						System.out.println("jIndex "+jIndex);
						if(!sumComputed){
							for(int jSumIndex=0; jSumIndex<englishPlusNullTokens.length; jSumIndex++){
								sum = sum + tInitial.get(add(spanishTokens[iIndex],PIPE,englishPlusNullTokens[jSumIndex])).getT();
							}
							sumComputed = true;
//							System.out.println(sumComputed);
						}
//						String kij = new StringBuilder(k).append(COMMA).append(i).append(COMMA).append(j).toString();
						String ej = englishPlusNullTokens[jIndex];
						String figivenej = add(spanishTokens[iIndex],PIPE,ej);
//						delta.put(kij, tInitial.get(figivenej)/sum);
						float delta = tInitial.get(figivenej).getT()/sum;
//						String ejfi = add(ej,COMMA,spanishTokens[iIndex]);
//						if(countEjFi.get(ejfi) == null){
//							countEjFi.put(ejfi, 0f);
//						}
						if(countEj.get(ej) == null){
							countEj.put(ej, 0f);
						}
//						countEjFi.put(ejfi, countEjFi.get(ejfi)+delta);
						countEj.put(ej, countEj.get(ej)+delta);
						T t = tInitial.get(figivenej);
						t.setC(t.getC()+delta);
					}
				}
				
			}
			System.out.println("b");
//			Iterator<Entry<String,T>> it = tInitial.entrySet().iterator();
			for(Map.Entry<String, T> tEntry: tInitial.entrySet()){
				String key = tEntry.getKey();
				String [] args = key.split("\\|");
//				String fi = args[0];
				
				String ej = "";
				if(args.length >1){
					ej = args[1];
				}
//				tInitial.put(add(fi,PIPE, ej), new T(tEntry.getValue().getC()/ countEj.get(ej)));
				tEntry.setValue(new T(tEntry.getValue().getC()/ countEj.get(ej)));
			}
//			it.
			System.out.println("c");
//			System.out.println("delta "+delta);
//			System.out.println("countEj "+countEj);
//			System.out.println("countEjFi "+countEjFi);
//			System.out.println("tInitial "+tInitial);
			fileReaderEnglish.close();
			fileReaderSpanish.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
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

	private void initialize() {
		try {
			String inputLineEnglish = null;
			String inputLineSpanish = null;
			BufferedReader fileReaderEnglish = new BufferedReader(new FileReader(ENGLISH_FILE));
			BufferedReader fileReaderSpanish = new BufferedReader(new FileReader(SPANISH_FILE));
			Map<String, Set<String>> englishToSpanishWords = new HashMap<String,Set<String>>();
			Set<String> distinctSpanishWords = new HashSet<String>();
			int length= 0;
			while((inputLineEnglish = fileReaderEnglish.readLine()) != null){
				length++;
				inputLineSpanish = fileReaderSpanish.readLine();
				String[] spanishTokens = inputLineSpanish.split(SPACE);
				String[] englishTokens = inputLineEnglish.split(SPACE);				 
				for(String englishToken: englishTokens){
					if(englishToSpanishWords.get(englishToken) == null){
						englishToSpanishWords.put(englishToken, new HashSet<String>(Arrays.asList(spanishTokens)));
					}else{
						englishToSpanishWords.get(englishToken).addAll((Arrays.asList(spanishTokens)));
					}
				}
				distinctSpanishWords.addAll(Arrays.asList(spanishTokens));
			}
			
			int nullSize =  distinctSpanishWords.size();
			tInitial = new HashMap<String,T>();
//			t = new HashMap<String,Map<String,Float>>();
			for(Map.Entry<String, Set<String>> englishToSpanishWord: englishToSpanishWords.entrySet()){
				int size = englishToSpanishWord.getValue().size();
//				Map<String,Float> fValues = new HashMap<String, Float>();
				for(String spanishWord : englishToSpanishWord.getValue()){
					tInitial.put(add(spanishWord,PIPE,englishToSpanishWord.getKey()), new T((float)1/size));
					tInitial.put(add(spanishWord,PIPE,NULL), new T((float)1/nullSize));
//					fValues.put(spanishWord, (float)1/size);					
				}
//				t.put(englishToSpanishWord.getKey(), fValues);
			}
			
			/*Map<String,Float> fValues = new HashMap<String, Float>();
			for(String distinctSpanishWord: distinctSpanishWords){
				fValues.put(distinctSpanishWord, (float)1/nullSize);
			}
			t.put(NULL, fValues);*/
			fileReaderEnglish.close();
			fileReaderSpanish.close();
			System.out.println(length + SPACE+nullSize);
			System.out.println("size "+tInitial.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			
	}
}
