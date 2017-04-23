package sampling;

import util.*;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Gibbs sampling sampler thread.
 * User: chu
 * Date: 11/5/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class InfluencingGibbsSampler implements Callable<String>{

    List<SampleElementInfluencing> obj_list;
    SampleData call_sample_data;
    private boolean trainOrTest;
    double llh = 0;
    Probability probability;
    private MiniDistribution map_z_dist = null;
    private boolean init = true;
    /**
     * groupId is the partition id.
     */
    private long groupId;
    /**
     * after the thread begin to run, it is set to thread id.
     */
    private long threadId;

    long blocked_time;
    long waiting_time;
    long cpu_time;


    public InfluencingGibbsSampler(int groupId, List<SampleElementInfluencing> obj_list, SampleData call_sample_data, boolean trainOrTest) {
        setGroupId(groupId);
        this.obj_list = obj_list;
        this.call_sample_data = call_sample_data;
        this.setTrainOrTest(trainOrTest);
        this.probability = new Probability();
    }

    public void runSampling(){
//        Map<String, List<Long>> timeMap = new HashMap<String, List<Long>>();
        llh = 0;
        CmdOption cmdOption = call_sample_data.cmdOption;
        if (map_z_dist==null)
            map_z_dist = new MiniDistribution(cmdOption.znum);

        for (SampleElementInfluencing e : obj_list){
//            System.out.println(Debugger.getCallerPosition()+" sample:"+e.toString());
            call_sample_data.updCountInfluencing(e.u, e.z, e.w, e.aspect, -1); //update count

            int uprime = e.u; int w = e.w; int aspect = e.aspect;

            double[] result = Probability.drawInfluencingSample(map_z_dist, uprime, w, aspect, call_sample_data, cmdOption);
            e.z = (int)result[0];
            llh += result[1];

            call_sample_data.updCountInfluencing(e.u, e.z, e.w, e.aspect, +1); //update count
        }
    }

    /**
     * TODO debug null pointer here
     */
    public void drawInitSampling(){
        CmdOption cmdOption = call_sample_data.cmdOption;
        for (SampleElementInfluencing e : obj_list) {
            e.z = Util.initialLatentState(cmdOption.znum);
//            System.out.println(Debugger.getCallerPosition()+" call_sample_data==null? "+call_sample_data==null);
            call_sample_data.updCountInfluencing(e.u, e.z, e.w, e.aspect, +1); //update count
        }
    }

    public void calculate_llh(){

    }

    @Override
    public String call() throws Exception {
        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
              +" group-id="+getGroupId()+" thread-id="+getThreadId()+" influencing begin to run");

        //set thread id
        setThreadId(Thread.currentThread().getId());

        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition() + "chain-" + this.call_sample_data.chainId
              + " group-id=" + getGroupId() + " thread-id=" + getThreadId() + " influencing reset thread id");

        long[] beforeStamp = null;
        long[] afterStamp;

        if (this.call_sample_data.cmdOption.printThread.equals("y")){
            beforeStamp = this.printSamplerInfo(call_sample_data.tmxb);
        }

        if (init) {
            init = false;
            this.drawInitSampling();
        }
        else
            this.runSampling();

        if (this.call_sample_data.cmdOption.printThread.equals("y")){
           afterStamp = this.printSamplerInfo( call_sample_data.tmxb);

            blocked_time = (afterStamp[0]-beforeStamp[0]);
            waiting_time = (afterStamp[1]-beforeStamp[1]);
            cpu_time = (afterStamp[2]-beforeStamp[2]);

//            print withing thread running aspect
            System.out.println(Debugger.getCallerPosition()+" influencing chain-"+call_sample_data.chainId+" obj-"+this.groupId
              +" thread_id="+this.threadId
              +" blocked_time="+blocked_time+" waiting_time="+waiting_time+" cpu_time="+cpu_time);
        }

//        setGroupId(0);
        return "influencing object "+getGroupId()+" sampling finish";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isTrainOrTest() {
        return trainOrTest;
    }

    public void setTrainOrTest(boolean trainOrTest) {
        this.trainOrTest = trainOrTest;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public int getChainSize(){
        return this.obj_list.size();
    }

    public long[] printSamplerInfo(ThreadMXBean tmxb){
//        if (getGroupId()==0)
//            setGroupId(Thread.currentThread().getId());

//        pin check whether this thread started or not.

//        if (call_sample_data.cmdOption.debug.equals("y"))
//            System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
//              +"group-id="+getGroupId()+" thread-id="+getThreadId()
//              +" enter influencing printSamplerInfo");
//
//        while (getThreadId()==0){
////            if (call_sample_data.cmdOption.debug.equals("y"))
////                System.out.println("chain-"+this.call_sample_data.chainId
////                  +"group-id="+getGroupId()+" influencing pin check thread id.");
//        }
//
//        if (call_sample_data.cmdOption.debug.equals("y"))
//            System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
//              +"group-id="+getGroupId()+" thread-id="+getThreadId()
//              +" finish influencing thread id check");

        long[] time = new long[3];

        ThreadInfo threadInfo = tmxb.getThreadInfo(getThreadId());

        time[0] =  threadInfo.getBlockedTime();
        time[1] =  threadInfo.getWaitedTime();
        time[2] =  tmxb.getThreadUserTime(getThreadId())/1000000;

        return time;
    }
}
