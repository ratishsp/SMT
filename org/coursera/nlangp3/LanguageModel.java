/**
 * 
 */
package org.coursera.nlangp3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rpuduppully
 *
 */
public class LanguageModel {

	private static final String TRAINING_FILE = "/home/arya/nlp/h3/dev.en";
	private static final int START = 1;
	private static final int STOP = 2;
	private static final String STAR = "@#!@#!@#!";
	private static final String STOP_SYMBOL ="^&%$%$^$&^&";
	private static final String SPACE = " "; 
	private Map<Uvw,Integer> cuvw = new HashMap<Uvw,Integer>();
	private Map<Uv,Integer> cuv = new HashMap<Uv,Integer>();
	private Map<U,Integer> cu = new HashMap<U,Integer>();
	private Map<Uvw,Float> qwgivenuv = new HashMap<Uvw,Float>();
	private Map<Uv,Float> qwgivenv = new HashMap<Uv,Float>();
	private Map<U,Float> qw = new HashMap<U,Float>();
	private int total =0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LanguageModel languageModel = new LanguageModel();
		languageModel.readTrainingData();
		languageModel.computeq();
	}
	
	public Map<U, Integer> getCu() {
		return cu;
	}
	
	public Map<Uvw, Integer> getCuvw() {
		return cuvw;
	}
	
	public Map<Uv, Integer> getCuv() {
		return cuv;
	}
	
	private void computeq() {
		System.out.println(cuvw);
//		System.out.println(cuv);
		System.out.println(cu);
	}
	public void readTrainingData() {
		try {
//			f.put
			BufferedReader br1 = new BufferedReader(new FileReader(TRAINING_FILE));
			String inputLine = null;
			while((inputLine = br1.readLine())!=null){
				String[] args = inputLine.split(SPACE);
				
//				Uv uv = new Uv(STAR, args[0]);
//				incrementValueCuv(cuv, uv);
//				Uvw uvw = new Uvw(STAR, STAR, args[0]);
//				incrementValueCuvw(cuvw, uvw);
//				uvw = new Uvw(STAR, args[1], args[2]);
//				incrementValueCuvw(cuvw, uvw);
				total += args.length;
				String [] tokens = new String[args.length +3];
				tokens[0] = STAR;
				tokens[1] = STAR;
				tokens[tokens.length -1] = STOP_SYMBOL;
				System.arraycopy(args, 0, tokens, 2, args.length);
				for (int i = 2; i < tokens.length; i++) {
					String w = tokens[i];
					String u = tokens[i-2];
					String v = tokens[i-1];
					Uvw uvw = new Uvw(u, v, w);
					incrementValueCuvw(cuvw, uvw);
					
					Uv uv = new Uv(v, w);
					incrementValueCuv(cuv, uv);
					
					U uobj = new U(w);
					incrementValueCu(cu,uobj);
				}
			}
			br1.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void incrementValueCu(Map<U, Integer> map, U key) {
		if(map.containsKey(key)){
			map.put(key, map.get(key)+1);
		}else{
			map.put(key, 1);
		}
	}
	private void incrementValueCuvw(Map<Uvw, Integer> map, Uvw key) {
		if(map.containsKey(key)){
			map.put(key, map.get(key)+1);
		}else{
			map.put(key, 1);
		}
	}
	private void incrementValueCuv(Map<Uv,Integer> map,Uv key){
		if(map.containsKey(key)){
			map.put(key, map.get(key)+1);
		}else{
			map.put(key, 1);
		}
	}

	public float findq(String u, String v, String w){
		Uvw uvw = new Uvw(u, v, w);
		Uv uv = new Uv(v, w);
		U uObj = new U(w);
		Integer countUvw = cuvw.get(uvw);
		Integer countUv = cuv.get(uv);
		Integer countU = cu.get(uObj);
		
		float q = 0;
		if(countUv != null && countUvw != null  && countUv !=0){
			q+= (float)countUvw/countUv;
		}
		
		if(countU != null && countUv != null && countU !=0){
			q+= (float)countUv/countU;
		}
		
		q+= (float)countU/total;
		
		return q/3;
	}
}


class Uvw{
	private String u;
	private String v;
	private String w;
	
	
	@Override
	public String toString() {
		return "Uvw [u=" + u + ", v=" + v + ", w=" + w + "]";
	}
	public Uvw(String u, String v, String w) {
		super();
		this.u = u;
		this.v = v;
		this.w = w;
	}
	public String getU() {
		return u;
	}
	public void setU(String u) {
		this.u = u;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	public String getW() {
		return w;
	}
	public void setW(String w) {
		this.w = w;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		result = prime * result + ((w == null) ? 0 : w.hashCode());
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
		Uvw other = (Uvw) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		if (w == null) {
			if (other.w != null)
				return false;
		} else if (!w.equals(other.w))
			return false;
		return true;
	}
	
	
}

class Uv{
	private String u;
	private String v;
	
	
	@Override
	public String toString() {
		return "Uv [u=" + u + ", v=" + v + "]";
	}
	public Uv(String u, String v) {
		super();
		this.u = u;
		this.v = v;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
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
		Uv other = (Uv) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}
	
	public String getU() {
		return u;
	}
	public void setU(String u) {
		this.u = u;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	
}

class U{
	private String u;

	
	@Override
	public String toString() {
		return "U [u=" + u + "]";
	}

	public U(String u) {
		super();
		this.u = u;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
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
		U other = (U) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		return true;
	}

	public String getU() {
		return u;
	}

	public void setU(String u) {
		this.u = u;
	}

	
	
	

}