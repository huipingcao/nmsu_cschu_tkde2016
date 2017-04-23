package util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Minimal implementation of a Distribution. Designed for reuse and high performance.
 * Chuan add keys5 to hold aspect
 */
public class MiniDistribution {
	//Invariants: (added by Huiping Cao June 5, 2012)
	//(1) keys.length = keys2.length = keys3.length = keys4.length = vals.length 
	//(2) Number of real values in keys, keys2, keys3, keys4, vals is "endposition"
	//(3) vals[i] = probability (key1 = keys[i], key2 = keys2[i], ...)

	/**
	 * latent state
	 */
	private int[] keys1;
	/**
	 * b
	 */
	private int[] keys2;
	/**
	 * oprime
	 */
	private int[] keys3;
	/**
	 * aspect
	 */
	private int[] keys4;
	private int[] keys5;//useless now
//	sum of distributions
	private double[] vals;

	int endposition = 0;
	private int key2Draw = 0;
	private int key3Draw = 0;
	private int key4Draw = 0;
	private int key5Draw = 0;
	
	public double totalSum = 0;

	public MiniDistribution(int maxEntries) {
		keys1 = new int[maxEntries];
		keys2 = new int[maxEntries];
		keys3 = new int[maxEntries];
		keys4 = new int[maxEntries];
		keys5 = new int[maxEntries];
		vals = new double[maxEntries];
	}

	public String toString(){
		String str = "keys="+Arrays.toString(keys1) +"\n"
				+ "keys2="+Arrays.toString(keys2) +"\n"
				+ "keys3="+Arrays.toString(keys3) +"\n"
				+ "keys4="+Arrays.toString(keys4) +"\n"
				+ "keys5="+Arrays.toString(keys5) +"\n"
				+ "vals="+Arrays.toString(vals) +"\n"
				+ "endposition = "+endposition;

		return str;
	}

	public void add(int key1, double val) {
		add(key1, 0, val);
	}
	
	public void add(int key1, int key2, double val) {
		add(key1, key2, 0, val);
	}

	public void add(int key1, int key2, int key3, double val) {
		add(key1, key2, key3,0, val);
	}
	
	public void add(int key1, int key2, int key3, int key4, double val){
		add(key1, key2, key3, key4, 0, val);
	}
	
	public void add(int key1, int key2, int key3, int key4,  int key5, double val) {
		assert (!Double.isNaN(val)) : key1 + " " + key2 + " " + key3 + " " + key4+" "+key5;
//		assert (val < 0) : key1 + " " + key2 + " " + key3 + " " + key4+" "+key5+" "+val;
        assert (endposition < this.keys1.length) : "out of boundary endposition="+endposition+" array length="+this.keys1.length;
		totalSum+=val;
		keys1[endposition] = key1;
		keys2[endposition] = key2;
		keys3[endposition] = key3;
		keys4[endposition] = key4;
		keys5[endposition] = key5;
		vals[endposition] = val;
//		tmpSum+=val;
//		
//		int numOfInterval = this.intervalMark.length-1;
//		double lengthOfInterval = MiniDistribution.priSum/numOfInterval;
//		if (tmpSum > lengthOfInterval && intervalIndex<interval.length){
//			this.intervalMark[intervalIndex] =  endposition;
//			this.interval[intervalIndex] = totalSum;
//			this.intervalIndex++;
//			tmpSum = 0.0;
//		}

		endposition++;
	}

    public void put(int key1, double val) { put(key1, 0, val); }

	public void put(int key1, int key2, double val) {
		put(key1, key2, 0, val);
	}

	public void put(int key1, int key2, int key3, double val) {
		add(key1, key2, key3, 0, val);
	}

	public void put(int key1, int key2, int key3, int key4, double val) {
		add(key1, key2, key3, key4, val);
	}

	public void put(int key1, int key2, int key3, int key4, int key5, double val) {
		add(key1, key2, key3, key4, key5, val);
	}



    public void clear() {
		endposition = 0;
        totalSum = 0;
	}

	public boolean isEmpty() {
		return endposition == 0;
	}

	public double sum() {
		return totalSum;
	}

	/**
	 * Draw a key from the distribution
	 * The corresponding key2 can be retrieved by a subsequent call to getKey2Draw().
	 *
	 * @return drawn key
	 */
	 public int draw() {
		 if (isEmpty()) {
			 throw new RuntimeException(Debugger.getCallerPosition()+" distribution is Empty!");
		 }
		 double sum = totalSum;
		 //next aspect prior sum
//		 MiniDistribution.priSum = sum;
		 
//		 System.out.println(Debugger.getCallerPosition()+"sum= "+sum+" size "+endposition);
		 
		 assert (sum > 0) : sum + " \n" + Arrays.toString(keys1) + " \n" + Arrays.toString(vals);
		 assert (!Double.isNaN(sum));
		 if (Double.isInfinite(sum)) {
			 System.err.println("Too large values. Sums to infinity. " + Arrays.toString(vals));
		 }
		 
		 double rnd = Math.random(); //a random number in [0.0, 1.0)
		 double rdmValue = rnd * sum;
		 
		 //binary search here
//		 int startPoint = Util.binarySearch(vals, rdmValue, 0, vals.length-1);
//		 if(startPoint > vals.length)
//			 throw new RuntimeException(Debugger.getCallerPosition()+"something went wrong here.. remaining seed=" + rdmValue+" total sum "+sum);
//
//		 else if(startPoint == vals.length-1){
//			 key2Draw = keys2[startPoint];
//			 key3Draw = keys3[startPoint];
//			 key4Draw = keys4[startPoint];
//			 key5Draw = keys5[startPoint];
//			 return keys1[startPoint];
//		 }
//		 else{
//			 key2Draw = keys2[startPoint+1];
//			 key3Draw = keys3[startPoint+1];
//			 key4Draw = keys4[startPoint+1];
//			 key5Draw = keys5[startPoint+1];
//			 return keys1[startPoint+1];
//		 }

         //iteration draw
         double tmpSum = 0;
		 for (int i = 0; i < endposition; i++) {
			 assert (!Double.isNaN(rdmValue));
			 assert (!Double.isNaN(vals[i]));
//			 seed -= vals[i];
			 //if (Double.isNaN(seed)) {
			 //    int bla = 1;
			 //}
             tmpSum += vals[i];
			 if (rdmValue <= tmpSum) {
				 key2Draw = keys2[i];
				 key3Draw = keys3[i];
				 key4Draw = keys4[i];
				 key5Draw = keys5[i];
				 return keys1[i];
			 }
		 }
         return -1;
	 }
	 
	 public int getKey2Draw() {
		 return key2Draw;
	 }


	 public int getKey3Draw() {
		 return key3Draw;
	 }

	 public int getKey4Draw() {
		 return key4Draw;
	 }

	 public int getKey5Draw() {
		 return key5Draw;
	 }

	 public void initializeEqualDistribution(int maxKey) {
		 clear();
		 for (int key = 0; key < maxKey; key++) {
			 add(key, 0, 0, 1.0);
		 }
	 }

    public int size(){return this.keys1.length;}
}