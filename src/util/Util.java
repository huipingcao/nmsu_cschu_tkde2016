package util;

import net.didion.jwnl.data.Exc;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.relationship.SymmetricRelationship;
import sampling.SampleData;
import sampling.SampleDataSer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.DoubleAccumulator;


/**
 * 
 * @author Huiping Cao
 * @date Apr. 2, 2012
 */
public class Util{
	private static final int PRECISION = 2;
	
    public static String getStatFileName(String samplerId)
    {
        String fname = Constant.DefaultResultOutputFolder+samplerId + ".stat.xls";
        return fname;
    }
	/**
	 * A generic function to print an array
	 * @param array
	 */
	public static <E> void printArray(E[] array)
	{
		for(int i=0;i<array.length;i++)
			System.out.print(" "+array[i]);
		System.out.println();
	}
	/**
	 * print 2D array to string (matrix)
	 * @param array
	 * @return
	 */
	public static String Array2DToString(double[][] array)
	{
		final StringBuffer str = new StringBuffer();
		int i=0, j=0;  
        for(i=0;i<array.length;i++){
        	for(j=0;j<array[i].length;j++){
        		 str.append(" "+array[i][j]);
        	}
        	str.append("\n");
        }
        str.append("\n");
        
        return str.toString();
	}
	
	/**
	 * Convert a 2D array to string
	 * @param data
	 * @return
	 */
	public static String toString(double[][] data) {
        StringBuffer buf = new StringBuffer();
        
        //added by Huiping
        buf.append("row num = word num="+data.length+"\n");
        if(data.length>0) buf.append("col num = doc num = "+data[0].length+"\n");
        //end of adding
        
        for (int c0 = 0; c0 < data.length; c0++) {
            double[] row = data[c0];
            buf.append(Arrays.toString(row)).append("\n");
        }
        return buf.toString();
        //return Arrays.deepToString(data);
    }
	
	/**
	 * add the number of elements in one dimension together
	 * E.g., from 2D array
	 * [[1],[2]]
	 * [[3],[4]]
	 * [[5],[6]] dim0size = 3, dim1size=2
	 * sumArrayDim(array,0) should return array with size dim1size 2  
	 * [9=1+3+5, 12=2+4+6]
	 * sumArrayDim(array,1) should return array with size dim1size 3
	 * [3=1+2, 7=3+4, 11=5+6]
	 * @param array
	 * @param dimToSummarize
	 * @return
	 */
	public static double[] sumArrayDim(double [][] array,int dimToSummarize)
	{
		double[] sum = null; 
		int dim0size = array.length;
		int dim1size = array[0].length;
		
		if(dimToSummarize==0) sum = new double[dim1size];
		else 				sum = new double[dim0size];
	
		for(int i=0;i<dim0size;i++){
			for(int j=0;j<dim1size;j++){
				if(dimToSummarize==0) sum[j] += array[i][j];
				else 				sum[i] += array[i][j];
			}
		}
		return sum;
	}

    public static double[] avgArrayDim(double [][] array,int dimToAvg){
        double[] sum = null;
        int dim0size = array.length;
        int dim1size = array[0].length;

        if(dimToAvg==0) sum = new double[dim1size];
        else 				sum = new double[dim0size];

        for(int i=0;i<dim0size;i++){
            for(int j=0;j<dim1size;j++){
                if(dimToAvg==0) sum[j] += array[i][j] / dim0size ;
                else 				sum[i] += array[i][j] / dim1size;
            }
        }

        return sum;
    }
	
	/**
	 * Add the number of the elements inside the array
	 * @param array
	 * @return
	 */
	public static double sumArray(double[] array)
	{
		double sum=0.0;
		for(int i=0;i<array.length;i++)
			sum+=array[i];
		return sum;
	}
	
	/**
	 * randomly generate the the latent state
	 * @param numLatentState
	 * @return
	 */
	public static int initialLatentState(int numLatentState) {
        return (int) Math.floor(Math.random() * numLatentState);
    } 
	
	public static int initiaUprime(List<Integer> bibliography) {
        assert ( bibliography!=null ) : "bibliograph is null" ;
        assert ( !bibliography.isEmpty()) : "bibliograph is empty";

        int index = (int) Math.floor(Math.random() * bibliography.size());
        return bibliography.get(index);
    }

    /**
     * print 4 dimension map to file
     * @param map
     * @param writer
     */
    public static void print4Map(Map<Integer, Map<Integer, Map<Integer, Map<Integer, Double>>>> map, Writer writer){
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Map<Integer, Double>>>> entry : map.entrySet()){
            Integer key = entry.getKey();
            print3Map(entry.getValue(), key.toString(), writer);
        }
    }

    /**
     * print 3 dimension map to file
     * @param map
     * @param offset
     * @param writer
     */
    public static void print3Map(Map<Integer, Map<Integer, Map<Integer, Double>>> map, String offset, Writer writer){
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Double>>> entry : map.entrySet()){
            int key = entry.getKey();
            print2Map(entry.getValue(), offset + (offset == null || offset.length() == 0 ? "" : "\t") + key, writer);
        }
    }

    /**
     * print 2 dimension map to file
     * @param map
     * @param offset
     * @param writer
     */
    public static void print2Map(Map<Integer, Map<Integer, Double>> map, String offset, Writer writer){
        for (Map.Entry<Integer, Map<Integer, Double>> entry : map.entrySet()){
            int key = entry.getKey();
            print1Map(entry.getValue(), offset+(offset==null||offset.length()==0?"":"\t")+key, writer);
        }
    }

    /**
     * print 1 dimension map to file
     * @param map
     * @param offset
     * @param writer
     */
    public static void print1Map(Map<Integer, Double> map, String offset, Writer writer){
        for (Map.Entry<Integer, Double> entry : map.entrySet()){
            int key = entry.getKey();
            try {
                writer.append(offset+(offset==null||offset.length()==0?"":"\t")+key+"\t"+entry.getValue()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * update 3 dimension Hash map
     * @param map
     * @param key1
     * @param key2
     * @param key3
     * @param value
     */
    public static void update4MapIncreamental(Map<Integer, Map<Integer, Map<Integer, Map<Integer, Double>>>> map,
                                              int key1,
                                              int key2,
                                              int key3,
                                              int key4,
                                              double value){
        if(value==0 && get4Map(map, key1, key2, key3, key4)==0)
            return ;

        Map<Integer, Map<Integer, Map<Integer, Double>>> map1 = map.get(key1);
        if(map1==null){
            if (map instanceof ConcurrentMap) {
                if (map instanceof ConcurrentHashMap)
                    map1 = new ConcurrentHashMap<Integer, Map<Integer, Map<Integer, Double>>>();
                else if (map instanceof ConcurrentSkipListMap)
                    map1 = new ConcurrentSkipListMap<Integer, Map<Integer, Map<Integer, Double>>>();

                if (((ConcurrentMap) map).replace(key1, map1) == null)
                    ((ConcurrentMap) map).putIfAbsent(key1, map1);
            }
            else {
                if (map instanceof HashMap) {
                    map1 = new HashMap<Integer, Map<Integer, Map<Integer, Double>>>();
                    ((HashMap) map).put(key1, map1);
                } else if (map instanceof TreeMap) {
                    map1 = new TreeMap<Integer, Map<Integer, Map<Integer, Double>>>();
                    ((TreeMap) map).put(key1, map1);
                }
            }
        }
        update3MapIncreamental(map1, key2, key3, key4, value);

        if(map1.isEmpty()){   // if map1 is emptry remove it from map to save memory usage
            if (map instanceof ConcurrentMap)
                ((ConcurrentMap)map).remove(key1, map1);
            else
                map.remove(key1);
        }
    }

    /**
     * update 3 dimension Hash map
     * @param map
     * @param key1
     * @param key2
     * @param key3
     * @param value
     */
    public static void update3MapIncreamental(Map<Integer, Map<Integer, Map<Integer, Double>>> map,
                                              int key1,
                                              int key2,
                                              int key3,
                                              double value){
        if(value==0 && get3Map(map, key1, key2, key3)==0)
            return ;

        Map<Integer, Map<Integer, Double>> map1 = map.get(key1);

        if(map1==null){
            if (map instanceof ConcurrentMap) {
                if (map instanceof ConcurrentHashMap)
                    map1 = new ConcurrentHashMap<>();
                else if (map instanceof ConcurrentSkipListMap)
                    map1 = new ConcurrentSkipListMap<>();

                if (((ConcurrentMap) map).replace(key1, map1) == null)
                    ((ConcurrentMap) map).putIfAbsent(key1, map1);
            }
            else {
                if (map instanceof HashMap) {
                    map1 = new HashMap<>();
                    ((HashMap) map).put(key1, map1);
                } else if (map instanceof TreeMap) {
                    map1 = new TreeMap<>();
                    ((TreeMap) map).put(key1, map1);
                }
            }
        }
        update2MapIncreamental(map1, key2, key3, value);

        if(map1.isEmpty()){   // if map1 is emptry remove it from map to save memory usage
            if (map instanceof ConcurrentMap)
                ((ConcurrentMap)map).remove(key1, map1);
            else
                map.remove(key1);
        }
    }

    /**
     * update 2 dimension map
     * @param map
     * @param key1
     * @param key2
     * @param value
     */
    public static void update2MapIncreamental(Map<Integer, Map<Integer, Double>> map,
                                              int key1,
                                              int key2,
                                              double value){

        if(value==0 && get2Map(map, key1, key2)==0)
            return;

        Map<Integer, Double> map1 = map.get(key1);

        if(map1==null){
            if (map instanceof ConcurrentMap) {
                if (map instanceof ConcurrentHashMap)
                    map1 = new ConcurrentHashMap<>();//
                else if (map instanceof ConcurrentSkipListMap)
                    map1 = new ConcurrentSkipListMap<>();//

                if ( ((ConcurrentMap)map).replace(key1, map1)==null) {
                    ((ConcurrentMap)map).putIfAbsent(key1, map1);
                }
            }
            else {
                if (map instanceof HashMap) {
                    map1 = new HashMap<>();
                      ((HashMap) map).put(key1, map1);
                } else if (map instanceof TreeMap) {
                    map1 = new TreeMap<>();
                    ((TreeMap) map).put(key1, map1);
                }
            }
        }

        update1MapIncreamental(map1, key2, value);
        if (map1.isEmpty()){ // if map1 is emptry remove it from map to save memory usage
            if (map instanceof ConcurrentMap)
                ((ConcurrentMap)map).remove(key1, map1);
            else
                map.remove(key1);
        }
    }

    public static void update1MapIncreamental(Map<Integer, Double> map, int key, double value){
        //influencing counts
//       TODO debug null pointer exception here
        double oldFreq = 0;
        try{
//            TODO comment to debug
//            oldFreq = map.containsKey(key)? map.get(key) : 0;
            if (map.containsKey(key)) {
                if (map.get(key)==null){ //null pointer exception happens here
                    System.out.println(Debugger.getCallerPosition()+" map.get(key) is null here key="+key);
                    for (Map.Entry<Integer, Double> entry : map.entrySet())
                        System.out.println(entry.getKey()+" "+entry.getValue());
                    System.exit(0);
                }
                else
                    oldFreq = map.get(key);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }

        double newValue = oldFreq + value;

        if (newValue == 0){ // if newValue=0 remove it from map to save memory usage
            if (map instanceof ConcurrentMap)
                ((ConcurrentMap) map).remove(key, oldFreq);
            else
                map.remove(key);
        }
        else {     //else update the value in map
            if (map instanceof ConcurrentMap) {
                if (((ConcurrentMap) map).replace(key, newValue) == null)
                    ((ConcurrentMap) map).putIfAbsent(key, newValue);
            } else
                map.put(key, newValue);
        }
    }

    /**
     * read value from 4 dimension map
     * @param map
     * @param key1
     * @param key2
     * @param key3
     * @return
     */
    public static double get4Map(Map<Integer, Map<Integer, Map<Integer, Map<Integer, Double>>>> map,
                                 int key1, int key2, int key3, int key4){
        Map<Integer, Map<Integer, Map<Integer, Double>>> map1 = map.get(key1);
        if(map1==null)
            return 0;
        else{
            return get3Map(map1, key2, key3, key4);
        }
    }
    /**
     * read value from 3 dimension map
     * @param map
     * @param key1
     * @param key2
     * @param key3
     * @return
     */
    public static double get3Map(Map<Integer, Map<Integer, Map<Integer, Double>>> map,
                                 int key1, int key2, int key3){
        Map<Integer, Map<Integer, Double>> map1 = map.get(key1);
    	if(map1==null)
    		return 0;
    	else{
            return get2Map(map1, key2, key3);
    	}
    }

    /**
     * read value from 2 dimension map
     * @param map
     * @param key1
     * @param key2
     * @return
     */
    public static double get2Map(Map<Integer, Map<Integer, Double>> map,
                                 int key1, int key2){
        Map<Integer, Double> map1 = map.get(key1);
    	if(map1==null)
    		return 0;
    	else{
    		return get1Map(map1, key2);
    	}
    }

    /**
     * read value from 1 dimension map
     * @param map
     * @param key
     * @return
     */
    public static double get1Map(Map<Integer, Double> map, int key){
        Double value = map.get(key);
        if(value==null)
            return 0;
        else{
            return value;
        }
    }

    /**
     * print 2 dimension map to screeen
     * @param map
     */
    public static void print2Map(Map<Integer, Map<Integer, Double>> map){
    	for(Map.Entry<Integer, Map<Integer, Double>> entry : map.entrySet())
    		for(Map.Entry<Integer, Double> entry2 : entry.getValue().entrySet())
    			System.out.println(entry.getKey()+"\t"+entry2.getKey()+"\t"+entry2.getValue());
    	
    }
    /**
     * Add by Chuan
     * binary search
     * @param arr sorted array to be searched
     * @param value target search value
     * @param p begin index
     * @param q end index
     * @return the biggest index b such that arr[b]<value; -1 if value < arr[0]
     */
    public static int binarySearch(double[] arr, double value, int p, int q){
    	if(p>q)
    		return p-1;
    	
    	int r = (p+q)/2;
    	if(arr[r] == value)
    		return r;
    	else if(arr[r] < value)
    		return binarySearch(arr, value, r+1, q);
    	else
    		return binarySearch(arr, value, p, r-1);
    }

    public static void printMsTimeBetween(String text, Date beginPoint, Date endPoint){
    	System.out.println(text+"\t time(ms) = "+ ( endPoint.getTime()-beginPoint.getTime() ) );
    }

    /**
     * begin record aspect for operation at iter iteration
     * @param operation
     */
    public static void beginRecordTime(Map<String, List<Long>> map, String operation){
        Date date = new Date();
        List<Long> list = map.get(operation);
        if (list==null) {
            list = new ArrayList<Long>();
            map.put(operation, list);
        }

        list.add(date.getTime());
    }
    /**
     * end record aspect for operation at iter iteration
     * @param operation
     */
    public static void endRecordTime(Map<String, List<Long>> map, String operation){
        Date date = new Date();

        List<Long> list = map.get(operation);

        long beginTime = list.get(list.size()-1);
        long endTime = date.getTime();

        list.set(list.size()-1, endTime - beginTime);
    }

    public static void printTimeMap(Map<String, List<Long>> map) {
//        System.out.println(Debugger.getCallerPosition()+" print running aspect detail");
        for (Map.Entry<String, List<Long>> entry : map.entrySet()){
            String operation = entry.getKey();
            List<Long> list = entry.getValue();

            for (int i=0; i<list.size(); i++)
                System.out.println(operation+" at iteration "+i+" use aspect "+list.get(i));
        }
    }

    public static Map<String, Long> sumTimeMap(Map<String, List<Long>> map) {
        Map<String, Long> sumMap = new HashMap<String, Long>();

        for (Map.Entry<String, List<Long>> entry : map.entrySet()){
            String operation = entry.getKey();
            List<Long> list = entry.getValue();

            long sum = 0;
            for (int i=0; i<list.size(); i++)
                sum += list.get(i);

            sumMap.put(operation, sum);
        }

        return sumMap;
    }

    /**
     * print the gamma_oaim values
     *
     */
    public static void prtResult2File(CmdOption option, SampleData sampleData, int totalIter, long totalTime)
    {
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//        Date date = new Date();

        String outputfile_prefix = Constant.DefaultResultOutputFolder+option.SAMPLER_ID+"_chain-"+
          sampleData.chainId+"_iter-"+totalIter;
        System.out.println(Debugger.getCallerPosition()+"print parameter values to file =" + outputfile_prefix);

        try {
//            //write sampleData objects
//            System.out.println(Debugger.getCallerPosition()+" write SampleData serialized object to "+outputfile_prefix+".ser");
//            FileOutputStream fout = new FileOutputStream(outputfile_prefix+".ser");
//            ObjectOutputStream oos = new ObjectOutputStream(fout);
//            oos.writeObject(new SampleDataSer(sampleData));
//            oos.close();

            //write counts in sampleData

//            N_u
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".Nu")));
            bw.append("U\n");
            Util.print1Map(sampleData.N_u_influenced, "", bw);
            bw.flush();
            bw.close();

//            N_ub
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".beta")));
            bw.append("U \t B \t freq\n");
            Util.print2Map(sampleData.N_ub_influenced, "", bw);
            bw.flush();
            bw.close();

//          N_uzb=0
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".theta")));
            bw.append("U \t Z(b=0) \t freq\n");
            Util.print2Map(sampleData.N_uz_innov_influenced, "", bw);
            bw.flush();
            bw.close();

            //            N_uup influenced
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".gamma_oaim")));
            bw.append("U \t Up influenced\t freq\n");
            Util.print2Map(sampleData.N_uup_inher_influenced, "", bw);
            bw.flush();
            bw.close();

            //            N_utaup influenced
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".psy")));
            bw.append("U \t ta\t up\t freq\n");
            Util.print3Map(sampleData.N_utaup_inher_influenced, "", bw);
            bw.flush();
            bw.close();

//          N_uaup
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".N_uaup")));
            bw.append("U \t a\t up\t freq\n");
            Util.print3Map(sampleData.N_uaup_inher_influenced, "", bw);
            bw.flush();
            bw.close();

            // N_uta
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".N_uta")));
            bw.append("U \t ta\t freq\n");
            Util.print2Map(sampleData.N_uta_inher_influenced, "", bw);
            bw.flush();
            bw.close();

            // N_ua
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".N_ua")));
            bw.append("U \t a\t freq\n");
            Util.print2Map(sampleData.N_ua_inher_influenced, "", bw);
            bw.flush();
            bw.close();

            // N_ua
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".N_a")));
            bw.append("a\t freq\n");
            Util.print1Map(sampleData.N_a_influenced, "", bw);
            bw.flush();
            bw.close();

            // N_taa
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".eta")));
            bw.append("ta \t a\t freq\n");
            Util.print2Map(sampleData.N_taa_inher_influenced, "", bw);
            bw.flush();
            bw.close();

//            N_up
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".Nup")));
            bw.append("Up influencing \t freq \n");
            Util.print1Map(sampleData.N_up_influencing, "", bw);
            bw.flush();
            bw.close();

//            N_up influenced
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".Nup_inher")));
            bw.append("Up influenced\t freq\n");
            Util.print1Map(sampleData.N_up_inher_influenced, "", bw);
            bw.flush();
            bw.close();

//            N_upz influenced
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".theta_prime_inher")));
            bw.append("Up \t Z influenced\t freq\n");
            Util.print2Map(sampleData.N_upz_inher_influenced, "", bw);
            bw.flush();
            bw.close();

//            N_upz influencing
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".theta_prime")));
            bw.append("Up \t Z influencing  \t freq\n");
            Util.print2Map(sampleData.N_upz_influencing, "", bw);
            bw.flush();
            bw.close();

//            N_wz_all.  Print z -> word index-> word string
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".phi")));
            bw.append("W_index \t Z \t freq\n");
            Util.print2Map(sampleData.N_wz_all, "", bw);
            bw.flush();
            bw.close();

//            N_up
            bw = new BufferedWriter(new FileWriter(new File(outputfile_prefix+".Nz")));
            bw.append("Z \t freq \n");
            Util.print1Map(sampleData.N_z_all, "", bw);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Map<Integer, Double> count = new HashMap<>();
        for (int i=0;i<10; i++)
            Util.update1MapIncreamental(count, Util.initialLatentState(2), 1.0);

        for (Map.Entry<Integer, Double> entry : count.entrySet())
            System.out.println(entry.getKey()+"\t"+entry.getValue());

        count = new ConcurrentSkipListMap<>();
        System.out.println(count instanceof ConcurrentMap) ;
    }
}