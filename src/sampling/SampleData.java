package sampling;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.*;

import util.*;
import preprocess.DataParsed;

/**
 *
 * Influencing objects: that have the potential to estTemporalInf others
 * Influenced objects: that have the potential to be influenced by others
 *
 * Example: o1->o2, o2->o3, o2->o4
 * Influencing objects: o1,o2
 * Influenced objects: o2,o3,o4
 *
 * @author Huiping Cao
 */
public class SampleData {
    public CmdOption cmdOption;

    public int chainId = -1;

    public Map<Integer, Map<Integer, Map<Integer, Double>>> in_influencing_wAup;
    public Map<Integer, Map<Integer, Map<Integer, Double>>> in_influenced_wAu;

    public Set<Integer> influenced_userset;
    public Set<Integer> influencing_userset;

    public Set<Integer> testSet; //citing:influenced test set

    public Map<Integer, List<Integer>> in_userGraph;

    /**
     * influencing obj id -> obj sample chain
     */
    public Map<Integer, List<SampleElementInfluencing>> influencingObjChain;
    int influencingCount = 0;
    /**
     * influenced obj id -> obj sample chain
     */
    public Map<Integer, List<SampleElementInfluenced>> influencedObjChain;
    int influencedCount = 0;

    //multi thread sampling
    public ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
    //

    //influenced count
    /**
     * N_u
     */
    public Map<Integer, Double> N_u_influenced;
    /**
     * N_{u,b}
     */
    public Map<Integer, Map<Integer, Double>> N_ub_influenced;
    /**
     * N_{u,z,b=0}
     */
    public Map<Integer, Map<Integer, Double>> N_uz_innov_influenced;
    /**
     * N_{u',b=1}
     */
    public Map<Integer, Double> N_up_inher_influenced;
    /**
     * N_{u',z',b=1}
     */
    public Map<Integer, Map<Integer, Double>> N_upz_inher_influenced;
    /**
     * N_{u,u',b=1}
     */
    public Map<Integer, Map<Integer, Double>> N_uup_inher_influenced;

    // count for OAIM model
    /**
     * N_{u,ta,up,b=1}
     */
    public Map<Integer, Map<Integer, Map<Integer, Double>>> N_utaup_inher_influenced;

    /**
     * N_{u,ta,b=1}
     */
    public Map<Integer, Map<Integer, Double>> N_uta_inher_influenced;

    // count for LAIM model
    /**
     * N_{u,a,up,b=1}
     */
    public Map<Integer, Map<Integer, Map<Integer, Double>>> N_uaup_inher_influenced;

    /**
     * N_{u,a,b=1}
     */
    public Map<Integer, Map<Integer, Double>> N_ua_inher_influenced;
    /**
     * N_{a,ta}
     */
    public Map<Integer, Map<Integer, Double>> N_taa_inher_influenced;
    /**
     * N_a
     */
    public Map<Integer, Double> N_a_influenced;


    //influencing count
    /**
     * N_{u'}
     */
    public Map<Integer, Double> N_up_influencing;
    /**
     * N_{u',z'}
     */
    public Map<Integer, Map<Integer, Double>> N_upz_influencing;

    //    shared count
    /**
     * N_{z,w} + N_{z',w'}
     */
    public Map<Integer, Map<Integer, Double>> N_wz_all;
    /**
     * N_z + N_{z'}
     */
    public Map<Integer, Double>  N_z_all;

    private MiniDistribution map_z_dist = null;
    private  MiniDistribution map_bopza_dist = null;

    /**
     * log likelihood of this model
     */
    public double llh = 0;
    /**
     * perplexity of this model
     */
    public double perplexity;

    Map<Integer, InfluencingGibbsSampler> influencingThreadMap = new HashMap<Integer, InfluencingGibbsSampler>();
    Map<Integer, InfluencedGibbsSampler> influencedThreadMap = new HashMap<Integer, InfluencedGibbsSampler>();

    Probability probability;

    public SampleData(final DataParsed parsedData,
                      CmdOption _option, Set<Integer> testSet, Sampler sampler)
    {
        this.cmdOption = _option;

        this.in_influencing_wAup = parsedData.influencing_wAup;
        this.in_influenced_wAu = parsedData.influenced_wAu;

        this.influenced_userset = parsedData.influenced_userset;
        this.influencing_userset = parsedData.influencing_userset;

        this.in_userGraph = parsedData.in_userGraph;
        this.testSet = testSet;
        this.chainId = sampler.runnableChainId;

        System.out.println(Debugger.getCallerPosition()+" initial chain id "+this.chainId);

        init();

        probability = new Probability();
    }


    /**
     * Initialize the data structure for Gibbs sampling
     */
    private void init()
    {
        ////////////////////////////////////////
        //1. w count for influencing and influenced documents

        this.influencingObjChain = new TreeMap<>();
        this.influencedObjChain = new TreeMap<>();

        /////////////////////////////////////////////////////

        //3. Initialize sampling counts
        //Sampling counts
        if (cmdOption.concurrent.equals("y")){ //concurrent sampling
            N_u_influenced = new ConcurrentSkipListMap<>();
            N_ub_influenced = new ConcurrentSkipListMap<>();
            N_uz_innov_influenced = new ConcurrentSkipListMap<>();
            N_up_inher_influenced = new ConcurrentSkipListMap<>();
            N_upz_inher_influenced = new ConcurrentSkipListMap<>();
            N_uup_inher_influenced = new ConcurrentSkipListMap<>();

            //OAIM
            N_utaup_inher_influenced = new ConcurrentSkipListMap<>();
            N_uta_inher_influenced = new ConcurrentSkipListMap<>();

            //LAIM
            N_uaup_inher_influenced = new ConcurrentSkipListMap<>();
            N_ua_inher_influenced = new ConcurrentSkipListMap<>();
            N_taa_inher_influenced = new ConcurrentSkipListMap<>();
            N_a_influenced = new ConcurrentSkipListMap<>();

            N_up_influencing = new ConcurrentSkipListMap<>();
            N_upz_influencing = new ConcurrentSkipListMap<>();

            N_wz_all = new ConcurrentSkipListMap<>();
            N_z_all = new ConcurrentSkipListMap<>();
        }
        else if (cmdOption.concurrent.equals("n")){//serial sampling
            N_u_influenced = new TreeMap<>();
            N_ub_influenced = new TreeMap<>();
            N_uz_innov_influenced = new TreeMap();
            N_up_inher_influenced = new TreeMap<>();
            N_upz_inher_influenced = new TreeMap<>();
            N_uup_inher_influenced = new TreeMap<>();

            //OAIM
            N_utaup_inher_influenced = new TreeMap<>();
            N_uta_inher_influenced = new TreeMap<>();

            //LAIM
            N_uaup_inher_influenced = new TreeMap<>();
            N_ua_inher_influenced = new TreeMap<>();
            N_taa_inher_influenced = new TreeMap<>();
            N_a_influenced = new TreeMap<>();

            N_up_influencing = new TreeMap<>();
            N_upz_influencing = new TreeMap<>();

            N_wz_all = new TreeMap<>();
            N_z_all = new TreeMap<>();
        }

//      enable thread contention monitoring
        if ( !tmxb.isThreadContentionMonitoringEnabled() )
            tmxb.setThreadContentionMonitoringEnabled(true);
        if (!tmxb.isCurrentThreadCpuTimeSupported())
            tmxb.setThreadCpuTimeEnabled(true);

    }

    /**
     * For object "uid", get the number of objects that influece it.
     * @param uid
     * @return he number of objects that influece the given object "uid".
     */
    public int getUprimeNumber(int uid) {
        return in_userGraph.get(uid).size();
    }


    /**
     * Draw the initial sample for influencing and influenced objects
     * @param trainOrTest true: training; false: test
     */
    public void drawInitialSample(boolean trainOrTest, ExecutorService pool)
    {
        //draw initial sample for influencing graph object profiles
        drawInitialSampleInfluencing(trainOrTest, pool);

//        if (cmdOption.concurrent.equals("y"))
//            this.rebuild_global_count();
//
//        System.out.println(Debugger.getCallerPosition() + " after initial influencing sampling, checking count consistent.");
//        this.checkSampleCountConsistency();
//        System.out.println(Debugger.getCallerPosition()+" after initial influencing sampling, count is consistent.");

        //draw initial sample for influenced graph object profiles
        drawInitialSampleInfluenced(trainOrTest, pool);

        if (cmdOption.concurrent.equals("y"))
            this.rebuild_global_count();

        if (cmdOption.checkConsistence.equals("y")){
            System.out.println(Debugger.getCallerPosition() + " after initial sampling, checking count consistent.");
            this.checkSampleCountConsistency();
            System.out.println(Debugger.getCallerPosition()+" after initial sampling, count is consistent.");
        }

        System.out.println(Debugger.getCallerPosition()+" after initial and free resource memUsed="+
          +Debugger.getMemoryUsed());
    }

    /**
     * free wAup 
     */
    public void clearInputDataCount(){
        //
        if (this.in_influencing_wAup!=null){
            this.in_influencing_wAup.clear();
            this.in_influencing_wAup = null;
        }
        if (this.in_influencing_wAup!=null){
            this.in_influenced_wAu.clear();
            this.in_influenced_wAu = null;
            System.gc();
        }
    }

    private void draw_sample_influencing_multithread(boolean trainOrTest, ExecutorService pool){
        Map<Integer, Future<String>> futureMap = new HashMap<>();

        Date beginSubmit = new Date();

        System.out.println(Debugger.getCallerPosition() + " submit threads");
        for (Map.Entry<Integer, List<SampleElementInfluencing>> entry : influencingObjChain.entrySet()) {
//        for each group of tokens, start a thread
            int upIdx = entry.getKey();
            List<SampleElementInfluencing> list = entry.getValue();

//           start gibbs sampling threads
            InfluencingGibbsSampler gs = influencingThreadMap.get(upIdx);
            if (gs == null) {
                gs = new InfluencingGibbsSampler(upIdx, list, this, trainOrTest);
                influencingThreadMap.put(upIdx, gs);
            }

            gs.setTrainOrTest(trainOrTest);
//                gs.setThreadId(upIdx);
            futureMap.put(upIdx, pool.submit(gs));
        }
        Date endSubmit = new Date();

        Date beginRunning = new Date();

        try {
            Thread.sleep(1000*cmdOption.numThread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Debugger.getCallerPosition() + " waiting for influencing threads to finish");
        for (Map.Entry<Integer, Future<String>> entry : futureMap.entrySet()) {
            try {
                Future<String> f = entry.getValue();
                String message = f.get();
//                System.out.println(Debugger.getCallerPosition()+message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
//        compute the averaged influencing thread running aspect
        int n = 0;
        double avg_b_time = 0;
        double avg_w_time = 0;
        double avg_c_time = 0;
        for (Map.Entry<Integer, List<SampleElementInfluencing>> entry : influencingObjChain.entrySet()) {
            int upIdx = entry.getKey();
            InfluencingGibbsSampler gs = influencingThreadMap.get(upIdx);
            long b_time = gs.blocked_time;
            long w_time = gs.waiting_time;
            long c_time = gs.cpu_time;
            avg_b_time += b_time;
            avg_w_time += w_time;
            avg_c_time += c_time;
            n++;
        }
        avg_b_time /= n;
        avg_w_time /= n;
        avg_c_time /= n;
        System.out.println(Debugger.getCallerPosition() + " chain-" + chainId + " influencing chains avg blocking time=" + avg_b_time
          + " avg waiting time=" + avg_w_time + " avg cpu time=" + avg_c_time);

        Date endRunning = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition() + " chain-" + this.chainId + " influencing thread submission", beginSubmit, endSubmit);
        Util.printMsTimeBetween(Debugger.getCallerPosition() + " chain-" + this.chainId + " influencing all threads from submit to end running", beginSubmit, endRunning);
        Util.printMsTimeBetween(Debugger.getCallerPosition() + " chain-" + this.chainId + " influencing all threads running", beginRunning, endRunning);
    }

    private void draw_sample_influenced_multithread(boolean trainOrTest, ExecutorService pool) {
//        for each group of tokens, start a thread
        Map<Integer, Future<String>> futureMap = new HashMap<>();

        Date beginSubmit = new Date();
        for (Map.Entry<Integer, List<SampleElementInfluenced>> entry : influencedObjChain.entrySet()){
            int uIdx = entry.getKey();
            List<SampleElementInfluenced> list = entry.getValue();

//           start gibbs sampling thread
            InfluencedGibbsSampler gs = influencedThreadMap.get(uIdx);
            if (gs==null) {
                gs = new InfluencedGibbsSampler(uIdx, list, this, trainOrTest);
                influencedThreadMap.put(uIdx, gs);
            }

            gs.setTrainOrTest(trainOrTest);
            futureMap.put(uIdx, pool.submit(gs));
        }

        //invoke all block.
//            try {
//                pool.invokeAll(influencedThreadMap.values());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

        Date endSubmit = new Date();

        Date beginRunning = new Date();
        try {
            Thread.sleep(cmdOption.numThread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//            //check begin aspect stamp
//            Map<Long, long[]> beginStamp = new HashMap<Long, long[]>();
//            for (Map.Entry<Integer, InfluencedGibbsSampler> entry : influencedThreadMap.entrySet()){
//                InfluencedGibbsSampler sampler = entry.getValue();
//                long[] tmp = sampler.printSamplerInfo(tmxb);
//                beginStamp.put(sampler.getThreadId(), tmp);
//            }

//          wait for all thread to finish
        for (Map.Entry<Integer, Future<String >> entry : futureMap.entrySet()){
            try {
                Future<String> f =  entry.getValue();
                String message = f.get();
//                    System.out.println(Debugger.getCallerPosition()+" "+message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //        compute the averaged influencing thread running aspect
        int n=0;
        double avg_b_time = 0;
        double avg_w_time = 0;
        double avg_c_time = 0;
        for (Map.Entry<Integer, List<SampleElementInfluenced>> entry : influencedObjChain.entrySet()){
            int uIdx = entry.getKey();
            InfluencedGibbsSampler gs = influencedThreadMap.get(uIdx);

            long b_time = gs.blocked_time;
            long w_time = gs.waiting_time;
            long c_time = gs.cpu_time;
            avg_b_time += b_time;
            avg_w_time += w_time;
            avg_c_time += c_time;
            n++;
        }
        avg_b_time /= n;
        avg_w_time /= n;
        avg_c_time /= n;
        System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" influenced chains avg blocking time="+avg_b_time
          +" avg waiting time="+avg_w_time+" avg cpu time="+avg_c_time);

        Date endRunning = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influenced thread submission", beginSubmit, endSubmit);
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influenced all threads from submit to end running", beginSubmit, endRunning);
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId+" influenced all threads running", beginRunning, endRunning);
    }

    /**
     * Draw initial samples for influencing objects
     * @param trainOrTest true: training; false: test
     */
    private void drawInitialSampleInfluencing(boolean trainOrTest, ExecutorService pool)
    {
        //map loop initial
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Double>>> w2Aup_entry : in_influencing_wAup.entrySet()){
            int wid = w2Aup_entry.getKey();
            for (Map.Entry<Integer, Map<Integer, Double>> a2up_entry : w2Aup_entry.getValue().entrySet()) {
                int aspectId = a2up_entry.getKey();

                for (Map.Entry<Integer, Double> up2count_entry : a2up_entry.getValue().entrySet()) {
                    //Get the number of tokens in object "opIndex"
                    int upIndex = up2count_entry.getKey();
                    final double tokenCount = up2count_entry.getValue();

                    if (testSet.contains(upIndex) && trainOrTest)//in training step and opIndex in testSet. Continue
                        continue;
                    if (!testSet.contains(upIndex) && !trainOrTest)//in test step and opIndex is not in testSet.  Continue
                        continue;

                    //w occurs "tokenCount" times in the profile of object "opIndex"
                    for (int occ = 0; occ < tokenCount; occ++) {
                        int newZ = cmdOption.concurrent.equals("y") ? 0 : Util.initialLatentState(cmdOption.znum);
//                        int newZ = Util.initialLatentState(cmdOption.znum);

                        //1.add the sample
                        SampleElementInfluencing e = new SampleElementInfluencing(upIndex, aspectId, wid, newZ);
                        List<SampleElementInfluencing> objChain = this.influencingObjChain.get(upIndex);
                        if(objChain==null){
                            objChain = new ArrayList<>();
                            this.influencingObjChain.put(upIndex, objChain);
                        }
                        objChain.add(e);

                        //2. update sample count
                        if (cmdOption.concurrent.equals("n"))
                            updCountInfluencing(upIndex, newZ, wid, aspectId, +1);
                        influencingCount++;
                    }
                }
            }
        }

        if (cmdOption.concurrent.equals("y"))
            draw_sample_influencing_multithread(trainOrTest, pool);

        System.out.println(Debugger.getCallerPosition()+ " influencing object count="+ influencingObjChain.size()+
          " influencing sample chain size="+influencingCount+" memory usage="+Debugger.getMemoryUsed());
    }

    /**
     * draw initial sample for influenced documents.
     * @param trainOrTest true: training; false: test
     */
    private void drawInitialSampleInfluenced(boolean trainOrTest, ExecutorService pool)
    {
        //map loop initial
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Double>>> w2A2u_entry : in_influenced_wAu.entrySet()){//non zero w
            int wid = w2A2u_entry.getKey();
            for (Map.Entry<Integer, Map<Integer, Double>> a2u_entry : w2A2u_entry.getValue().entrySet()) {// non zero ta
                int aspect = a2u_entry.getKey();

                for (Map.Entry<Integer, Double> u2count_entry : a2u_entry.getValue().entrySet()) {
                    int uid = u2count_entry.getKey();

                    if (testSet.contains(uid) && trainOrTest)//in training step and obj in testSet. Continue
                        continue;
                    if (!testSet.contains(uid) && !trainOrTest)//in test step and obj is not in testSet.  Continue
                        continue;

                    final double tokenCount = u2count_entry.getValue();

                    for (int occ = 0; occ < tokenCount; occ++) {
                        int newB = cmdOption.concurrent.equals("y") ? 0:Util.initialLatentState(2);
                        int newZ = cmdOption.concurrent.equals("y") ? 0 :Util.initialLatentState(cmdOption.znum);
                        int newUprime = cmdOption.concurrent.equals("y") ? 0 : Util.initiaUprime(in_userGraph.get(uid));
                        int newLA =cmdOption.concurrent.equals("y") ? 0 : Util.initialLatentState(cmdOption.anum);
                      /*  int newB = Util.initialLatentState(2);
                        int newZ = Util.initialLatentState(cmdOption.znum);
                        int newUprime = Util.initiaUprime(in_userGraph.get(uid));
                        int newLA = Util.initialLatentState(cmdOption.anum);*/

                        SampleElementInfluenced e = new SampleElementInfluenced(uid, aspect, wid, newZ, newLA, newB, newUprime);

                        List<SampleElementInfluenced> objChain = this.influencedObjChain.get(uid);
                        if (objChain==null){
                            objChain = new ArrayList<>();
                            this.influencedObjChain.put(uid, objChain);
                        }
                        objChain.add(e);

                        if (cmdOption.concurrent.equals("n"))
                            updCountInfluenced(uid, aspect, wid, newZ, newLA, newB, newUprime, 1);
                        influencedCount++;
                    }
                }
            }
        }

        if (cmdOption.concurrent.equals("y"))
            draw_sample_influenced_multithread(trainOrTest, pool);

        System.out.println(Debugger.getCallerPosition()+ " influenced object count="+ influencedObjChain.size()+
          " influenced sample chain size="+influencedCount+" memory usage="+Debugger.getMemoryUsed());
    }

    /**
     *   Update the sample for object.  synchronize the whole method.
     * @param upIndex
     * @param word
     * @param z
     * @param value
     */
    public void updCountInfluencing(int upIndex, int z, int word, int aspect, int value) {

        Util.update1MapIncreamental(N_up_influencing, upIndex, value);
        Util.update2MapIncreamental(N_upz_influencing, upIndex, z, value);

        //shared counts.  do not update topic count after each token sampling.  rebuild the topic word count after one iteration
        if (cmdOption.concurrent.equals("n")) {
            Util.update2MapIncreamental(N_wz_all, word, z, value);
            Util.update1MapIncreamental(N_z_all, z, value);
        }
    }
    /**
     * udpate count of influenced object.  synchronize the whole method.
     * @param u
     * @param aspect
     * @param word
     * @param z
     * @param latent_aspect
     * @param b
     * @param uprime
     * @param value
     */
    public void updCountInfluenced(
      int u, int aspect, int word, int z, int latent_aspect, int b, int uprime, int value){

        Util.update2MapIncreamental(N_ub_influenced, u, b, value);
        Util.update1MapIncreamental(N_u_influenced, u, value);

        if(b==Constant.INHERITANCE){ //b=1; use the inherited data
            if (cmdOption.concurrent.equals("n")){
                Util.update1MapIncreamental(N_up_inher_influenced, uprime, value);
                Util.update2MapIncreamental(N_upz_inher_influenced, uprime, z, value);
            }
            Util.update2MapIncreamental(N_uup_inher_influenced, u, uprime, value);
            if (cmdOption.model.equals("oaim")){
                Util.update3MapIncreamental(N_utaup_inher_influenced, u, aspect, uprime, value);
                Util.update2MapIncreamental(N_uta_inher_influenced, u, aspect, value);
            }
            else if (cmdOption.model.equals("laim")){
                Util.update3MapIncreamental(N_uaup_inher_influenced, u, latent_aspect, uprime, value);
                Util.update2MapIncreamental(N_ua_inher_influenced, u, latent_aspect, value);
                if (cmdOption.concurrent.equals("n")){
                    Util.update2MapIncreamental(N_taa_inher_influenced, aspect, latent_aspect, value);
                    Util.update1MapIncreamental(N_a_influenced, latent_aspect, value);
                }
            }
        }
        else if (b==Constant.INNOTVATION){//b=0 innovative
            Util.update2MapIncreamental(N_uz_innov_influenced, u, z, value);
        }

        //shared counts.  if concurrent: do not update topic count after each token sampling.  rebuild the topic word count after one iteration
        if (cmdOption.concurrent.equals("n")){
            Util.update2MapIncreamental(N_wz_all, word, z, value);
            Util.update1MapIncreamental(N_z_all, z, value);
        }
    }

    /**
     * rebuild global count (N_zw, N_z, N_taa_inher_influenced, N_a_influenced, N_up_inher_influenced, N_upz_inher_influenced) table after one iteration
     */
    private void rebuild_global_count(){
        if (!cmdOption.concurrent.equals("y"))
            return;

        N_wz_all.clear();
        N_z_all.clear();
        N_taa_inher_influenced.clear();
        N_a_influenced.clear();
        N_up_inher_influenced.clear();
        N_upz_inher_influenced.clear();

        N_wz_all = null;
        N_z_all = null;
        N_taa_inher_influenced = null;
        N_a_influenced = null;
        N_up_inher_influenced = null;
        N_upz_inher_influenced = null;
        if (cmdOption.concurrent.equals("y")) { //concurrent sampling
            N_wz_all = new ConcurrentSkipListMap<>();
            N_z_all = new ConcurrentSkipListMap<>();
            N_taa_inher_influenced = new ConcurrentSkipListMap<>();
            N_a_influenced = new ConcurrentSkipListMap<>();
            N_up_inher_influenced = new ConcurrentSkipListMap<>();
            N_upz_inher_influenced = new ConcurrentSkipListMap<>();
        }
        else if (cmdOption.concurrent.equals("n")){//serial sampling
            N_wz_all = new TreeMap<>();
            N_z_all = new TreeMap<>();
            N_taa_inher_influenced = new TreeMap<>();
            N_a_influenced = new TreeMap<>();
            N_up_inher_influenced = new TreeMap<>();
            N_upz_inher_influenced = new TreeMap<>();
        }
        System.gc();//clear unused memory

        for (Map.Entry<Integer, List<SampleElementInfluencing>> entry : this.influencingObjChain.entrySet())
            for (SampleElementInfluencing e : entry.getValue()){
                Util.update2MapIncreamental(N_wz_all, e.w, e.z, +1);
                Util.update1MapIncreamental(N_z_all, e.z, +1);
            }
        for (Map.Entry<Integer, List<SampleElementInfluenced>> entry : this.influencedObjChain.entrySet())
            for (SampleElementInfluenced e : entry.getValue()){
                Util.update2MapIncreamental(N_wz_all, e.w, e.z, +1);
                Util.update1MapIncreamental(N_z_all, e.z, +1);
                if (e.b==Constant.INHERITANCE){
                    if (cmdOption.model.equals("laim")){
                        Util.update2MapIncreamental(N_taa_inher_influenced, e.aspect, e.latent_aspect, +1);
                        Util.update1MapIncreamental(N_a_influenced, e.latent_aspect, +1);
                    }

                    Util.update1MapIncreamental(N_up_inher_influenced, e.uprime, +1);
                    Util.update2MapIncreamental(N_upz_inher_influenced, e.uprime, e.z, +1);
                }
            }
    }

    public void checkSampleCountConsistency(){
        double tmpsum =0 ;

//        check shared counts
        //1. total N_z_all[i] = \sum_j N_wz_all[i][j]
        for(int z=0;z<cmdOption.znum;z++){
            tmpsum = 0;

            for(int word : N_wz_all.keySet())
                tmpsum += Util.get2Map(N_wz_all, word, z);

            assert(tmpsum == Util.get1Map(N_z_all, z)):"ERROR N_z_all["+z+"]="+Util.get1Map(N_z_all, z)
              +" sum over N_wz_all["+z+"]="+tmpsum;
        }

//        check influenced counts
        //2. influenced: N_u_influenced[i] = \sum_b N_ub_influenced[i][b]
        for(int uid : in_userGraph.keySet()){
            tmpsum = 0;
            for(int b=0;b<2;b++)
                tmpsum+=Util.get2Map(N_ub_influenced, uid, b);

            assert(tmpsum == Util.get1Map(N_u_influenced, uid)):"ERROR N_u_influenced["+uid+"]="
              +Util.get1Map(N_u_influenced, uid)+" tmpsum="+tmpsum;
        }

        //3. influenced b0 and b1
        //get b0sample sum and b1 sample sum
        double[] bsum = new double[2];
        bsum[0] = bsum[1] = 0;
        for(int b : new int[]{Constant.INNOTVATION, Constant.INHERITANCE} )
            for(int uid : influenced_userset)
                bsum[b]+= Util.get2Map(N_ub_influenced, uid, b);

        //4. influenced b innovative
        //N_uz_innov_influenced sum should be equal to bsum[innovative]
        tmpsum=0;
        for(int uid : in_userGraph.keySet()){
            for(int z=0;z<cmdOption.znum;z++)
                tmpsum+=Util.get2Map(N_uz_innov_influenced, uid, z);
        }
        assert(tmpsum == bsum[Constant.INNOTVATION]):
          "bsum["+Constant.INNOTVATION+"]="+bsum[Constant.INNOTVATION]
          +" sum over u z N_uz_innov[][]"+tmpsum;

        //5. influenced b inheritance
        tmpsum=0;
        // N_up_influencing sum should be equal to bsum[inheritance]
        for (int uid : influenced_userset)
            for(int upId : influencing_userset)
                tmpsum += Util.get2Map(N_uup_inher_influenced, uid, upId);

        assert(tmpsum == bsum[Constant.INHERITANCE]):
          "bsum["+Constant.INHERITANCE+"]="+bsum[Constant.INHERITANCE]
          +" sum over u up N_uup[][]="+tmpsum;

        tmpsum = 0;
        for (int uprime : influencing_userset)
            tmpsum += Util.get1Map(N_up_inher_influenced, uprime);

        assert(tmpsum == bsum[Constant.INHERITANCE]):
          "bsum["+Constant.INHERITANCE+"]="+bsum[Constant.INHERITANCE]
            +" sum over up N_up_inher[]="+tmpsum;

        //6. influenced N_ub_influenced and N_uup_inher_influenced
        for(int uid : influenced_userset){
            tmpsum = 0;
            for(int upId : influencing_userset) //in_userGraph.get(uid)
                tmpsum+=Util.get2Map(N_uup_inher_influenced, uid, upId);

            assert(tmpsum == Util.get2Map(N_ub_influenced, uid, Constant.INHERITANCE)):
              "N_ub_influenced["+uid+"]["+Constant.INHERITANCE+"]="+Util.get2Map(N_ub_influenced, uid, Constant.INHERITANCE)
                      +" sum over up N_uup_inher="+tmpsum;
        }

//        7. N_uup_inher_influenced and N_uup_inher_influenced
        for (int uid : N_uup_inher_influenced.keySet()){
            for (int upid : N_uup_inher_influenced.get(uid).keySet()){
                tmpsum = 0;
                if (cmdOption.model.equals("oaim")){
                    for (int ta : N_utaup_inher_influenced.get(uid).keySet()){
                        tmpsum += Util.get3Map(N_utaup_inher_influenced, uid, ta, upid);
                    }
                    assert (tmpsum == Util.get2Map(N_uup_inher_influenced, uid, upid)):
                      "N_uup_inher_influenced["+uid+"]["+upid+"]!="+tmpsum;
                } else if (cmdOption.model.equals("laim")){
                    for (int a : N_uaup_inher_influenced.get(uid).keySet()){
                        tmpsum += Util.get3Map(N_uaup_inher_influenced, uid, a, upid);
                    }
                    assert (tmpsum == Util.get2Map(N_uup_inher_influenced, uid, upid)):
                      "N_uup_inher_influenced["+uid+"]["+upid+"]!="+tmpsum;
                }
            }
        }

        // 8. N_ata_inher_influenced and N_ua_inher_influenced
        for (int la=0; la < cmdOption.anum; la++){
            //TODO
        }

//       influencing counts
        //9. influencing N_up_influencing and N_upz_influencing
        for(int upid : N_up_influencing.keySet()){
            tmpsum=0;
            for(int z=0;z<cmdOption.znum;z++){
                tmpsum+=Util.get2Map(N_upz_influencing, upid, z);
            }
            assert(tmpsum == Util.get1Map(N_up_influencing, upid)):
              "N_up_influencing["+upid+"]="+Util.get1Map(N_up_influencing, upid)+
                " sum over N_upz_influencing["+upid+"][]="+tmpsum;
        }

        System.out.println(Debugger.getCallerPosition()+"Counts are CONSISTENT.");
    }

    /**
     * Draw sample for one iteration
     */
    public void drawOneIterationSample(boolean trainOrTest,  ExecutorService pool) {
        llh = 0;
        Date beginPoint = new Date();
        Date endPoint = new Date();

        if (cmdOption.concurrent.equals("y")) {
            beginPoint = new Date();
            this.rebuild_global_count();
            endPoint = new Date();
            Util.printMsTimeBetween(Debugger.getCallerPosition() + " chain-" + this.chainId
              + " time used in rebuilding global counts", beginPoint, endPoint);
        }

        if (cmdOption.checkConsistence.equals("y")){
            //check the consistency of the sample counts
            this.checkSampleCountConsistency();
            System.out.println(Debugger.getCallerPosition()
              + " before sampling, count is consistent");
        }

        beginPoint = new Date();
        drawOneIterationInfluencingSample(trainOrTest, pool);
        endPoint = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId
          +" time used in influencing sampling", beginPoint, endPoint);

//        if (cmdOption.concurrent.equals("y")){
//            beginPoint = new Date();
//            this.rebuild_global_count();
//            endPoint = new Date();
//            Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId
//              +" time used in rebuiling rebuilding global counts", beginPoint, endPoint);
//        }
//
//        if (cmdOption.checkConsistence.equals("y")){
//            //check the consistency of the sample counts
//            this.checkSampleCountConsistency();
//            System.out.println(Debugger.getCallerPosition()
//              +" after one iteration influenced sampling, count is consistent");
//        }

        beginPoint = new Date();
        drawOneIterationInfluencedSample(trainOrTest, pool);
        endPoint = new Date();
        Util.printMsTimeBetween(Debugger.getCallerPosition()+" chain-"+this.chainId
          +" time used in influenced sampling", beginPoint, endPoint);

    }

    /**
     * Draw samples for one influencing document, update related counts
     * Tested, checked the first 10 element's update, correct
     */
    private void drawOneIterationInfluencingSample(boolean trainOrTest, ExecutorService pool) {
        if (cmdOption.concurrent.equals("y")) {
            this.draw_sample_influencing_multithread(trainOrTest, pool);
        }
        else {
            if (map_z_dist==null)
                map_z_dist = new MiniDistribution(cmdOption.znum);

            for (Map.Entry<Integer, List<SampleElementInfluencing>> chain_entry : influencingObjChain.entrySet()) {
                for (SampleElementInfluencing e : chain_entry.getValue()){
                    updCountInfluencing(e.u, e.z, e.w, e.aspect, -1); //update count
                    int uprime = e.u; int w = e.w; int aspect = e.aspect;

                    double[] result = Probability.drawInfluencingSample(map_z_dist, uprime, w, aspect, this, cmdOption);
                    e.z = (int)result[0];
                    llh += result[1];

                    updCountInfluencing(e.u, e.z, e.w, e.aspect, +1); //update count
                }

                //TODO debug negative llh
                /*System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" DEBUG llh="+llh);
                if (llh>=0){
                    System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" DEBUG llh>=0 after sampling influencing object "+chain_entry.getKey());
                }*/
            }
        }
    }

    /**
     * Draw samples for one influenced document, update related counts
     */
    private void drawOneIterationInfluencedSample(boolean trainOrTest, ExecutorService pool) {
        if (cmdOption.concurrent.equals("y")){
           draw_sample_influenced_multithread(trainOrTest, pool);
        }
        else {
            if (map_bopza_dist ==null){
                int Rup = 0;
                for (List<Integer> up_list : in_userGraph.values())
                    if (up_list.size() > Rup) Rup = up_list.size();

                int distrSize = cmdOption.model.equals("oaim") ? cmdOption.znum*(Rup+1) : cmdOption.znum*(Rup+1)*cmdOption.anum;
                System.out.println(Debugger.getCallerPosition()+" map_bopza_dist size="+distrSize);
                map_bopza_dist = new MiniDistribution(distrSize);
            }

            for (Map.Entry<Integer, List<SampleElementInfluenced>> chain_entry : influencedObjChain.entrySet()) {
                for (SampleElementInfluenced e : chain_entry.getValue()){
                    updCountInfluenced(e.u, e.aspect, e.w, e.z, e.latent_aspect, e.b, e.uprime, -1);
                    int uid = e.u; int w = e.w; int aspect = e.aspect;

                    double[] result = Probability.drawInfluencedSample(map_bopza_dist, uid, w, aspect, this, cmdOption);
                    e.b = (int)result[0];
                    e.z = (int)result[1];
                    e.uprime = (int)result[2];
                    if (cmdOption.model.equals("oaim"))
                        llh += result[3];
                    else if (cmdOption.model.equals("laim")) {
                        e.latent_aspect = (int)result[3];
                        llh += result[4];
                    }

                    updCountInfluenced(e.u, e.aspect, e.w, e.z, e.latent_aspect, e.b, e.uprime, +1);
                }
                //TODO debug negative llh
                /*System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" DEBUG llh="+llh);
                if (llh>=0){
                    System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" DEBUG llh>=0 after sampling influenced object "+chain_entry.getKey());
                }*/
            }
        }
    }

    public String toString() {
        final StringBuffer str = new StringBuffer();

        str.append(Debugger.getCallerPosition()+"****Sample**** \n");

        return str.toString();
    }

    /**
     * calculate log likelihood of this model
     */
    public void calculate_llh(){
        System.out.println(Debugger.getCallerPosition()+" calculating llh");

        if (cmdOption.concurrent.equals("y")){
            llh = 0;
            for (InfluencingGibbsSampler gs : influencingThreadMap.values()) {
                gs.calculate_llh();
                llh += gs.llh;
            }
            for (InfluencedGibbsSampler gs : influencedThreadMap.values()) {
                gs.calculate_llh();
                llh += gs.llh;
            }
        }
        else {
            //TODO[DONE] llh calculation is done during sampling
            ;
        }
    }

    /**
     * calculate perplexity of this model.
     */
    public void calculate_perplexity(){
        perplexity = Math.exp( -1.0*(llh) / (influencingCount+influencedCount) );
    }
}
