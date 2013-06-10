/**
 * 
 */
package org.coursera.nlangp3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author rpuduppully
 *
 */
public class PhrasalLexicon {
	private static final String GROWN_FILE = "/home/arya/nlp/h3/dev.key_grown.out";
	private static final String DEV_EN = "/home/arya/nlp/h3/dev.en";
	private static final String DEV_ES = "/home/arya/nlp/h3/dev.es";
	private static final String SPACE = " ";
	private static final int DISTORTION_LIMIT = 4;
	private static final int ETA = -4;	//TODO confirm
	private static final double BEAMWIDTH = 2;
	private List<Ef> lexicon = new ArrayList<Ef>();
	private Map<String,List<String>> lexiconMap = new HashMap<String, List<String>>();
	private Map<Ef,Integer> cEf = new HashMap<Ef,Integer>();
	private Map<String, Integer> cE = new HashMap<String,Integer>();
	private Map<Ef,Double> feg = new HashMap<Ef,Double>();
	private Map<Integer,Object> aij = new HashMap<Integer,Object>();
	private LanguageModel languageModel;
	private Map<State,Qp> bp= new LinkedHashMap<State,Qp>();
	
	public static void main(String[] args) {
		PhrasalLexicon phrasalLexicon = new PhrasalLexicon();
		phrasalLexicon.build();
//		State q = phrasalLexicon.decode("creo en lucha pobreza");
		State q = phrasalLexicon.decode("paz deben aceptar");
//		State q = phrasalLexicon.decode("caso ha un efecto positivo");  // this has a positive effect
		phrasalLexicon.printTranslation(q);
	
		
	}

	private void printTranslation(State q) {
		while(bp.get(q) != null){
			Qp qp = bp.get(q);
			System.out.println(qp.getP());
			q = qp.getQ();
		}
	}

	private void build() {
		try{
			languageModel = new LanguageModel();
			languageModel.readTrainingData();
			
			BufferedReader br1 = new BufferedReader(new FileReader(DEV_EN));
			BufferedReader br2 = new BufferedReader(new FileReader(DEV_ES));
			BufferedReader br3 = new BufferedReader(new FileReader(GROWN_FILE));
			List<String> devEn = new ArrayList<String>();
			List<String> devEs = new ArrayList<String>();
			String readLine = null;
			while((readLine = br1.readLine())!=null){
				devEn.add(readLine);
			}
			
			while((readLine = br2.readLine())!=null){
				devEs.add(readLine);
			}
			
			int rowNum = 0;
			List<String> tempList = new ArrayList<String>();
			while((readLine = br3.readLine())!= null){
				String [] args = readLine.split(SPACE);
				if(Integer.parseInt(args[0]) - rowNum ==1){
					tempList.add(readLine);
				}else{
					rowNum++;
					createAij(rowNum,tempList);
					tempList = new ArrayList<String>();
					tempList.add(readLine); 
				}
			}
			rowNum++;
			createAij(rowNum,tempList);
			
			for(int k=0; k<200; k++){
				String english = devEn.get(k);
				String foreign = devEs.get(k);
				String[] englishArgs = english.split(SPACE);
				String[] foreignArgs = foreign.split(SPACE);
				int mk = foreign.split(SPACE).length;
				int lk = english.split(SPACE).length;

				Map<Integer, List<Integer>> jAdashj = new HashMap<Integer, List<Integer>>();
				Map<Integer, List<Integer>> iAi = new HashMap<Integer, List<Integer>>();
				int [][] aijEntry = (int[][]) aij.get(k+1);
				for (int i = 0; i < aijEntry.length; i++) {
					int[] js = aijEntry[i];
					incrementA(jAdashj, js[0], js[1]);
					incrementA(iAi, js[1], js[0]);
				}
				
				for(int s=0; s<mk; s++){
					for(int t=s; t<mk; t++){
						for(int sdash=0; sdash<lk; sdash++){
							for(int tdash=sdash; tdash<lk; tdash++ ){
								if(consistent(iAi, jAdashj, k,s,t,sdash,tdash)){
									StringBuilder sb = new StringBuilder();
									for(int sSub= s; sSub<t; sSub++){					//TODO < or <=
										sb.append(foreignArgs[sSub]).append(SPACE);
									}
									sb.append(foreignArgs[t]);
									String f = sb.toString();
									
									sb = new StringBuilder();
									for(int sdashSub= sdash; sdashSub< tdash; sdashSub++){
										sb.append(englishArgs[sdashSub]).append(SPACE);		//TODO manipulate space
									}
									sb.append(englishArgs[tdash]);
									String e = sb.toString();
									
									Ef ef = new Ef(e, f);
									increment(cEf, ef);
									incrementE(cE,e);
									
									lexicon.add(ef);
									addE(lexiconMap, f, e);
									System.out.println(ef);
								}
							}
						}
					}
				}
				System.out.println(k);
			}
			
			for (Ef ef : lexicon) {
				feg.put(ef, Math.log((float)cEf.get(ef)/cE.get(ef.getE())));
			}
			
			
			
			br1.close();
			br2.close();
			br3.close();
//			System.out.println(lexicon);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void createAij(int rowNum, List<String> tempList) {
		int [][] aijentry = new int [tempList.size()][2];
		for(int index=0; index< tempList.size(); index++){
			String [] args = tempList.get(index).split(SPACE);
			aijentry[index][0] = Integer.parseInt(args[1]);
			aijentry[index][1] = Integer.parseInt(args[2]);
		}
		aij.put(rowNum, aijentry);
	}

	private void incrementA(Map<Integer, List<Integer>> map, Integer key, Integer value){
		if(map.containsKey(key)){
			map.get(key).add(value);
		}else{
			List<Integer> values = new ArrayList<Integer>();
			values.add(value);
			map.put(key,values);
		}
	}
	
	private void addE(Map<String, List<String>> map, String key, String value){
		if(map.containsKey(key)){
			map.get(key).add(value);
		}else{
			List<String> values = new ArrayList<String>();
			values.add(value);
			map.put(key,values);
		}
	}
	
	private boolean consistent(Map<Integer,List<Integer>> iAi, Map<Integer,List<Integer>> jAdashj,int k,int sIn, int tIn, int sdashIn, int tdashIn) {
		int s = sIn+1;
		int t = tIn+1;
		int sdash = sdashIn+1;
		int tdash = tdashIn+1;
		int [][] aijEntry = (int[][]) aij.get(k+1);
		
		//A(i) is set of all english words to which foreign word i is aligned to
		//A'(j) is set of all foreign words aligned to english word j
		//for each i in {s,t}   A(i) in {s',t'}
		//for each j in (sdash,tdash) A(j) in {s,t}
		List<Integer> Ai = new ArrayList<Integer>();
		for(int index = s; index<= t; index++){
			//TODO check <=
			Ai = iAi.get(index);
			if(Collections.max(Ai).compareTo(tdash) >0){
				return false;
			}
			if(Collections.min(Ai).compareTo(sdash) <0){
				return false;
			}
			
		}
		List<Integer> Adashj = new ArrayList<Integer>();
		for(int index = sdash; index<=tdash; index++){
			Adashj = jAdashj.get(index);
			if(Collections.max(Adashj).compareTo(t)>0){
				return false;
			}
			if(Collections.min(Adashj).compareTo(s) <0){
				return false;
			}
		}
		//there is atleast one (i,j) pair such that i in {s,t} and j in {s',t'} and A(i,j) = 1;
		boolean exists = false;
		for (int index = 0; index < aijEntry.length; index++) {
			int[] js = aijEntry[index];
			
			if(js[0] >= sdash && js[0] <= tdash && js[1]>= s && js[1]<=t){
				exists = true;
			}
			
		}
		
		return exists;
	}

	private void increment(Map<Ef, Integer> map, Ef key){
		if(map.containsKey(key)){
			map.put(key, map.get(key)+1);
		}else{
			map.put(key, 1);
		}
	}
	
	private void incrementE(Map<String, Integer> map, String key){
		if(map.containsKey(key)){
			map.put(key, map.get(key)+1);
		}else{
			map.put(key, 1);
		}
	}

	private void add(List<State> Q, State q, State qdash, Phrase p){
		ListIterator<State> iterator = Q.listIterator();
		boolean entryExists = false;
		while(iterator.hasNext()){
			State qEntry = iterator.next();
			if(eq(qEntry, qdash)){
				entryExists = true;
				if(qdash.getAlpha() > qEntry.getAlpha()){
					iterator.remove();
					iterator.add(qdash);
					bp.put(qdash, new Qp(q, p));
				}
			}
		}
		
		if(!entryExists){
			Q.add(qdash);
			bp.put(qdash, new Qp(q,p));
		}
	}

	private List<State> beam(List<State> Q){
		double score = 0;
		if(Q.size() >0){
			score = Collections.max(Q, new QComparator()).getAlpha();
		}
		List<State> outputStates = new ArrayList<State>();
		for(State qEntry: Q){
			if(qEntry.getAlpha() > score - BEAMWIDTH){
				outputStates.add(qEntry);
			}
		}
		return outputStates;
	}
	
	private State decode(String f){
		int bitstringLength =  f.split(SPACE).length;
		char [] c = new char[bitstringLength];
		for(int index = 0; index< bitstringLength; index++){
			c[index] = '0';
		}
		//initialize q0
		State q0 = new State("","",c,0,0);
		List<List<State>> QList = new ArrayList<List<State>>();
		List<State> Q0 = new ArrayList<State>();
		Q0.add(q0);
		QList.add(Q0);
		
		for(int index =1; index<=bitstringLength; index++){
			QList.add(new ArrayList<State>());
		}
		
		List<Phrase> phrases = getPhrases(f);
		//for i in 0 to n-1
		//for each state q in beam(Qi) for each phrase p in ph(q)
		//q' = next(q,p)
		//Add q' to Qj where j= len(q')
		
		for(int index=0; index <bitstringLength; index++ ){
			List<State> qStates = beam(QList.get(index));
			for(State q: qStates){
				List<Phrase> compatiblePhrases = ph(q,phrases);
				for(Phrase p: compatiblePhrases){
					State qdash = next(q,p);
					char[] bitstring = qdash.getB();
					int len=0;
					for(int bitIndex=0; bitIndex<bitstring.length; bitIndex++){
						if(bitstring[bitIndex] == '1'){
							len++;
						}
					}
						
					add(QList.get(len),q,qdash, p);
				}
			}
		}
	
//		System.out.println(bp);
//		for(int index =0; index< QList.size(); index++){
//			System.out.println(index + " "+ QList.get(index));
//		}
		return Collections.max(QList.get(bitstringLength), new QComparator());
	}
	
	private List<Phrase> getPhrases(String f){
		List<Phrase> phrases = new ArrayList<Phrase>();
		String [] args = f.split(SPACE);
		for(int l=0; l<= args.length-1; l++){
			for(int i=0; i<= args.length -1- l; i++){
				int j= i+l;
				StringBuilder sb = new StringBuilder();
				for(int index=i; index<j; index++){
					sb.append(args[index]).append(SPACE);
				}
				sb.append(args[j]);
				String sub = sb.toString();
				if(lexiconMap.containsKey(sub)){
					List<String> eValues = lexiconMap.get(sub);
					for(String eValue: eValues){
						phrases.add(new Phrase(i+1, j+1, eValue, sub));
					}
				}
			}
		}
		return phrases;	
	}
	
	private State next(State q, Phrase p){
		//e1 e2 b r alpha
		//return e1' e2' b' r' alpha'
		//phrase p has three components s, t and e
		
		char[] c = new char[q.getB().length];
//		c = q.getB();
		for(int index = 0; index< q.getB().length; index++){
			c[index] = q.getB()[index];
		}
		
		for(int index=p.getS(); index<= p.getT(); index++){
			c[index-1] = '1';
		}
		
		String expanded = q.getE1() + SPACE + q.getE2()+SPACE+p.getE();
		String [] args = expanded.split(SPACE);
		
		float qValue = 0.0f;
		for(int index = 2; index< args.length; index++){
			qValue += Math.log((languageModel.findq( args[index-2], args[index-1], args[index])));
		}
		
		int distortion = Math.abs(q.getR()+1- p.getS())* ETA;
		
		Ef ef = new Ef(p.getE(), p.getF());
		Double gef = feg.get(ef);
		
		//alpha' = alpha + g(p)+ qValue + eta(r+1-s)
		Double alphadash = q.getAlpha() + gef + qValue + distortion;
		
		State qdash = new State();
		qdash.setAlpha(alphadash);
		qdash.setE1(args[args.length-2]);
		qdash.setE2(args[args.length-1]);
		qdash.setB(c);
		qdash.setR(p.getT());
		return qdash;
	}
	
	private boolean eq(State q, State qdash){
		return (q.getB()== qdash.getB() && q.getR()== qdash.getR() && q.getE1().equals(qdash.getE1()) && q.getE2().equals(qdash.getE2()));
	}
	
	private List<Phrase> ph(State q, List<Phrase> P){
		List<Phrase> compatiblePhrases = new ArrayList<Phrase>();
		for(Phrase p: P){
			if(compatible(q,p)){
				compatiblePhrases.add(p);
			}
		}
		return compatiblePhrases;
	}

	private boolean compatible(State q, Phrase p) {
		//p must not overlap with bitstring b
		boolean compatible = true;
		for(int index=p.getS(); index<= p.getT(); index++){
			if(q.getB()[index -1] == '1'){
				compatible = false;
			}
		}
		
		//distortion limit is obeyed
		//r and s and not too distant
		if(Math.abs(q.getR()+1- p.getS()) > DISTORTION_LIMIT){
			compatible = false;
		}
		
		return compatible;
	}
}

class Ef{
	private String e;
	private String f;
	
	
	public Ef(String e, String f) {
		super();
		this.e = e;
		this.f = f;
	}
	
	public String getE() {
		return e;
	}
	
	public String getF() {
		return f;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e == null) ? 0 : e.hashCode());
		result = prime * result + ((f == null) ? 0 : f.hashCode());
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
		Ef other = (Ef) obj;
		if (e == null) {
			if (other.e != null)
				return false;
		} else if (!e.equals(other.e))
			return false;
		if (f == null) {
			if (other.f != null)
				return false;
		} else if (!f.equals(other.f))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Ef [e=" + e + ", f=" + f + "]";
	}
	
	
	
}

class Phrase{
	//tuple s,t,e
	private int s;
	private int t;
	private String f;
	private String e;
	
	public Phrase(int s, int t, String e, String f) {
		super();
		this.s = s;
		this.t = t;
		this.e = e;
		this.f = f;
	}
	public int getS() {
		return s;
	}
	public void setS(int s) {
		this.s = s;
	}
	public int getT() {
		return t;
	}
	public void setT(int t) {
		this.t = t;
	}
	public String getE() {
		return e;
	}
	public void setE(String e) {
		this.e = e;
	}
	
	public void setF(String f) {
		this.f = f;
	}
	
	public String getF() {
		return f;
	}
	@Override
	public String toString() {
		return "Phrase [s=" + s + ", t=" + t + ", f=" + f + ", e=" + e + "]";
	}
	
	
	
	
	
}
class State{
	//tuple e1,e2, b, r ,alpha
	
	private String e1;
	private String e2;
	private char[] b;
	private int r;
	private double alpha;
	
	public State() {
		// TODO Auto-generated constructor stub
	}
	
	public State(String e1, String e2, char[] b, int r, double alpha) {
		super();
		this.e1 = e1;
		this.e2 = e2;
		this.b = b;
		this.r = r;
		this.alpha = alpha;
	}
	public String getE1() {
		return e1;
	}
	public void setE1(String e1) {
		this.e1 = e1;
	}
	public String getE2() {
		return e2;
	}
	public void setE2(String e2) {
		this.e2 = e2;
	}
	public char[] getB() {
		return b;
	}
	public void setB(char[] b) {
		this.b = b;
	}
	public int getR() {
		return r;
	}
	public void setR(int r) {
		this.r = r;
	}
	public double getAlpha() {
		return alpha;
	}
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	@Override
	public String toString() {
		return "State [e1=" + e1 + ", e2=" + e2 + ", b=" + Arrays.toString(b)
				+ ", r=" + r + ", alpha=" + alpha + "]";
	}
	
	
}

class Qp{
	private State q;
	private Phrase p;
	
	public Qp() {
		// TODO Auto-generated constructor stub
	}
	
	public Qp(State q, Phrase p){
		this.q = q;
		this.p = p;
	}
	
	public void setP(Phrase p) {
		this.p = p;
	}
	
	public void setQ(State q) {
		this.q = q;
	}
	
	public Phrase getP() {
		return p;
	}
	
	public State getQ() {
		return q;
	}

	@Override
	public String toString() {
		return "Qp [q=" + q + ", p=" + p + "]";
	}
	
	
	
}

