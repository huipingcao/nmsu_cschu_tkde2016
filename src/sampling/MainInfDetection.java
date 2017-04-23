package sampling;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import util.CmdOption;
import util.Debugger;
import util.Util;

import java.io.*;
import java.util.*;

public class MainInfDetection {

    public static void showHelp(CmdLineParser parser){
        System.out.println("infdetection [options ...] [arguments...]");
        parser.printUsage(System.out);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        CmdOption option = new CmdOption();
        CmdLineParser parser = new CmdLineParser(option);

        //1. get command line parameters
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println(Debugger.getCallerPosition()+"Command line error: " + e.getMessage());
            showHelp(parser);
            return;
        }

        if(option.help==true){
            showHelp(parser);
            return;
        }

        System.out.println(Debugger.getCallerPosition()+"graphfile="+option.graphfile
          +"\ndatafile="+option.datafile);
        //System.exit(0);


        //2. do Gibbs sampling to estimate parameters on all data.
        Set<Integer> emptyTestSet = new HashSet<Integer>();
        option.alphaTheta = 50.0/option.znum;

        SamplerChain samplerChain = new SamplerChain(option, emptyTestSet);
        samplerChain.doGibbs();

        Date beginCheckPoint = new Date();

        //3. split the data set to training and test.  Use a 10-fold cross-validation and average the likelihood
        //on test data


        double likelihood = samplerChain.getLogLikelihoodAll();
        System.out.println(Debugger.getCallerPosition()+" whole log likelihood of model is "+likelihood);

        Util.prtResult2File(option, samplerChain.samplerToGetResult.getSampleData(),
          samplerChain.samplerToGetResult.totalIter, samplerChain.samplerToGetResult.totalTime);

        Date endCheckPoint = new Date();

        System.out.println("\n"+Debugger.getCallerPosition()+" result_output_time=" +
          ( endCheckPoint.getTime()-beginCheckPoint.getTime() ) + "Finish program\n\n");
    }
}
