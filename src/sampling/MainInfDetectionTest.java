package sampling;

import util.CmdOption;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chu on 6/16/14.
 */
public class MainInfDetectionTest {
    public static void main(String[] args){
        CmdOption option = new CmdOption();
        option.chainNum = 2;
        option.graphfile = "/Users/chu/Documents/aspect_infdetect/data/citeseerx_data/cite.txt";
        option.datafile = "/Users/chu/Documents/aspect_infdetect/data/citeseerx_data/inf_data.txt";
        option.SAMPLER_ID = "citeseer_test";
        option.znum = 10;
        option.burnin = 10;
        option.numIter=20;
        option.concurrent = "y";
        option.numThread = 2;
        option.checkConsistence = "y";
        option.access = "iterator";

        //2. do Gibbs sampling to estimate parameters on all data.
        Set<Integer> emptyTestSet = new HashSet<Integer>();
        SamplerChain samplerChain = new SamplerChain(option, emptyTestSet);
        samplerChain.doGibbs();
    }
}