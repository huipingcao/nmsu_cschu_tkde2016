package preprocess;

import java.io.*;
import java.util.*;

import util.CmdOption;
import util.Constant;
import util.Util;


/**
 * Class which represents a raw data.
 * @author convergence
 *
 */
public class DataParsed {

    /**
     * Influenced word document count
     * w -> o -> freq
     */
    public Map<Integer,  Map<Integer, Map<Integer, Double>>> influencing_wAup = new HashMap<>();

    /**
     * influenced word timestamp document count
     */
    public Map<Integer, Map<Integer, Map<Integer, Double>>> influenced_wAu = new HashMap<>();

    public Map<Integer, List<Integer>> in_userGraph = new HashMap<>();

    public Set<Integer> influenced_userset = new HashSet<>();
    public Set<Integer> influencing_userset = new HashSet<>();

    /**
     * Initialize the sample's internal structure.
     * Read the input graph, and the documents for the nodes
     * If the passed graphFileName is empty, the program will set a manual set of data
     * otherwise, the sampler will read the file and get the internal structures
     * Create the internal data structure that would be used in the sampling process.
     *
     * These internal structures include
     * (1) topological structure of estTemporalInf
     * (2) Vocabulary
     */
    public void init(CmdOption cmdOption)
    {
//        read graph edges
        try {
            BufferedReader bw = new BufferedReader(new FileReader(new File(cmdOption.graphfile)));
            String line = bw.readLine();
            while (line!=null){
                String[] tokens = line.split("\\s");
                int influenced_id = Integer.parseInt(tokens[0]);
                int influencing_id = Integer.parseInt(tokens[1]);
                // influenced and influencing id can not be the same
                if (influenced_id!=influencing_id){
                    influenced_userset.add(influenced_id);
                    influencing_userset.add(influencing_id);

                    if (!in_userGraph.containsKey(influenced_id))
                        in_userGraph.put(influenced_id, new ArrayList<>());

                    if (!in_userGraph.get(influenced_id).contains(influencing_id))
                        in_userGraph.get(influenced_id).add(influencing_id);
                }

                line = bw.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Set<Integer> vocabSet = new HashSet<>();
        Set<Integer> aspectSet = new HashSet<>();
//        read uid aspect word:count data
        try {
            BufferedReader bw = new BufferedReader(new FileReader(new File(cmdOption.datafile)));
            String line = bw.readLine();
            while (line!=null){
                String[] tokens = line.split("\\s");
                int uid = Integer.parseInt(tokens[0]);
                int aspectId = Integer.parseInt(tokens[1]);
                aspectSet.add(aspectId);

                for (int i=2; i<tokens.length; i++){
                    String[] items = tokens[i].split(":");
                    int wid = Integer.parseInt(items[0]);
                    vocabSet.add(wid);

                    int count = Integer.parseInt(items[1]);
                    if(influencing_userset.contains(uid))
                        Util.update3MapIncreamental(influencing_wAup, wid, aspectId, uid, count);
                    if(influenced_userset.contains(uid))
                        Util.update3MapIncreamental(influenced_wAu, wid, aspectId, uid, count);
                }

                line = bw.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Constant.wordNum = vocabSet.size();
        Constant.aspectNum = aspectSet.size();
    }

    public static void cleanTweetJson(String tweetPath){
        FileReader fr;
        BufferedReader br;
        File currentDir = new File(tweetPath);
        File[] jsonFiles = currentDir.listFiles((File f) -> f.getName().endsWith(".json"));
        for (File jsonFile : jsonFiles){
            try {
                fr = new FileReader(jsonFile);
                br = new BufferedReader(fr);
                String line = br.readLine();
                String jsonString = "";
                while(line!=null){
                    jsonString+=line;
                    line = br.readLine();
                }
                br.close();

                // //TODO clean non unicode text.  remove later
                System.out.println(jsonString.replaceAll("[^\\x00-\\x7F]", ""));
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                      new FileOutputStream(jsonFile.getAbsolutePath().replace("tweet", "clean")), "UTF-8"));
                    bw.write(jsonString.replaceAll("[^\\x00-\\x7F]", ""));
                    bw.flush(); bw.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] arg){
        DataParsed.cleanTweetJson(arg[0]);
    }
}

