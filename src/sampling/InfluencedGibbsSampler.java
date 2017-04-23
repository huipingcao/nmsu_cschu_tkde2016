package sampling;

import util.*;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: chu
 * Date: 11/5/13
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class InfluencedGibbsSampler implements Callable<String>{
    List<SampleElementInfluenced> obj_list;
    double llh = 0;
    SampleData call_sample_data;
    private boolean trainOrTest;
    Probability probability;
    private  MiniDistribution map_bopza_dist = null;
    private boolean init = true;
    /**
     * groupId is the partition id.
     */
    private long groupId;
    /**
     * after the thread begin to run, it is set to thread id.
     */
    private long threadId;

    public int avgOp;

    long blocked_time;
    long waiting_time;
    long cpu_time;

    public InfluencedGibbsSampler(int groupId, List<SampleElementInfluenced> obj_list, SampleData call_sample_data, boolean trainOrTest) {
        this.groupId = groupId;
        this.obj_list = obj_list;
        this.call_sample_data = call_sample_data;
        this.setTrainOrTest(trainOrTest);

        this.avgOp = 0;

        for (SampleElementInfluenced e : this.obj_list){
            int uid = e.u;
            this.avgOp += this.call_sample_data.getUprimeNumber(uid);
        }

        this.avgOp = this.avgOp / this.obj_list.size();
        this.probability = new Probability();
    }

    public void runSampling(){
        llh = 0;
        CmdOption cmdOption = call_sample_data.cmdOption;
        if (map_bopza_dist ==null){
            int Rup = call_sample_data.in_userGraph.get((int)groupId).size();
            int distrSize = cmdOption.model.equals("oaim") ? cmdOption.znum*(Rup+1) : cmdOption.znum*(Rup+1)*cmdOption.anum;
            System.out.println(Debugger.getCallerPosition()+" map_bopza_dist size="+distrSize);
            map_bopza_dist = new MiniDistribution(distrSize);
        }

        for (SampleElementInfluenced e : obj_list){
            call_sample_data.updCountInfluenced(e.u, e.aspect, e.w, e.z, e.latent_aspect, e.b, e.uprime, -1);

            double[] result = Probability.drawInfluencedSample(map_bopza_dist, e.u, e.w, e.aspect, call_sample_data, cmdOption);
            e.b = (int)result[0];
            e.z = (int)result[1];
            e.uprime = (int)result[2];
            if (cmdOption.model.equals("oaim"))
                llh += result[3];
            else if (cmdOption.model.equals("laim")) {
                e.latent_aspect = (int)result[3];
                llh += result[4];
            }

            call_sample_data.updCountInfluenced(e.u, e.aspect, e.w, e.z, e.latent_aspect, e.b, e.uprime, +1);
        }
    }

    /**
     * TODO to debug
     */
    public void drawInitSampling(){
        CmdOption cmdOption = call_sample_data.cmdOption;
        for (SampleElementInfluenced e : obj_list) {
            e.b = Util.initialLatentState(2);
            e.z = Util.initialLatentState(cmdOption.znum);
            e.uprime = Util.initiaUprime(call_sample_data.in_userGraph.get(e.u));
            e.latent_aspect = Util.initialLatentState(cmdOption.anum);
            call_sample_data.updCountInfluenced(e.u, e.aspect, e.w, e.z, e.latent_aspect, e.b, e.uprime, +1);
        }
    }

    public void calculate_llh(){

    }

    @Override
    public String call() throws Exception {
        //set thread id
        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
              +" group-id="+getGroupId()+" thread-id="+getThreadId()+" influenced begin to run");

        setThreadId(Thread.currentThread().getId());

        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition() + "chain-" + this.call_sample_data.chainId
              + " group-id=" + getGroupId() + " thread-id=" + getThreadId() + " influenced reset thread id");

        long[] beforeStamp = null;
        long[] afterStamp;

        if (this.call_sample_data.cmdOption.printThread.equals("y")){
            beforeStamp = this.printSamplerInfo(  call_sample_data.tmxb);
        }

        if (init) {
            init = false;
            this.drawInitSampling();
        }
        else
            this.runSampling();

        if (this.call_sample_data.cmdOption.printThread.equals("y")){
            afterStamp = this.printSamplerInfo(call_sample_data.tmxb);

            blocked_time = (afterStamp[0]-beforeStamp[0]);
            waiting_time = (afterStamp[1]-beforeStamp[1]);
            cpu_time = (afterStamp[2]-beforeStamp[2]);

//            print withing thread running aspect
            System.out.println(Debugger.getCallerPosition()+" influenced chain-"+call_sample_data.chainId+" obj-"+this.groupId
              +" thread_id="+this.threadId
              +" blocked_time="+blocked_time+" waiting_time="+waiting_time+" cpu_time="+cpu_time);
        }

        return "TIM model influenced object "+getGroupId()+" sampling finish"; //To change body of implemented methods use File | Settings | File Templates.
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
//        pin check whether thread start or not

        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition()+" chain-"+this.call_sample_data.chainId
              +" group-id="+getGroupId()+" thread-id="+getThreadId()
              +" enter influenced printSamplerInfo");

        while (getThreadId()==0){
//            if (call_sample_data.cmdOption.debug.equals("y"))
//                System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
//                  +"group-id="+getGroupId()+" influenced pin check thread id");
        }

        if (call_sample_data.cmdOption.debug.equals("y"))
            System.out.println(Debugger.getCallerPosition()+"chain-"+this.call_sample_data.chainId
              +"group-id="+getGroupId()+" thread-id="+getThreadId()
              +" finish influenced thread id check");

//        setThreadId(Thread.currentThread().getId());

        long[] time = new long[3];

        ThreadInfo threadInfo = tmxb.getThreadInfo(getThreadId());

        time[0] =  threadInfo.getBlockedTime();
        time[1] =  threadInfo.getWaitedTime();
        time[2] =  tmxb.getThreadUserTime(getThreadId())/1000000;

        return time;
    }
}
