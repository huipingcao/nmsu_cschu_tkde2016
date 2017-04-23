package convergence;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import util.CmdOption;
import util.Debugger;
import util.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * simple in-memory convergence diagnose class. only check convergence on log-likelihood and perplexity.
 */
public class ConvergenceDiagnose {

	private CmdOption cmdOption;

	//This is used for checking convergence
    private Map<Integer, ArrayList<Double>> llh_table = new ConcurrentSkipListMap<>();
    private Map<Integer, ArrayList<Double>> perplexity_table = new ConcurrentSkipListMap<>();

//    stores rhat values
    private Map<Integer, ArrayList<Double>> llh_rhat_table = new ConcurrentSkipListMap<>();
    private Map<Integer, ArrayList<Double>> perplexity_rhat_table = new ConcurrentSkipListMap<>();

	public ConvergenceDiagnose(List<Integer> _allChainIds, CmdOption _cmdOption){
        cmdOption = _cmdOption;
        for(int i : _allChainIds) {
            llh_table.put(i, new ArrayList<>());
            perplexity_table.put(i, new ArrayList<>());

            llh_rhat_table.put(i, new ArrayList<>());
            perplexity_rhat_table.put(i, new ArrayList<>());
        }
	}

    /**
     * check if all sampling chain started to run.
     * @return
     */
    public boolean allChainStarted(){
        for (Map.Entry<Integer, ArrayList<Double>> entry : llh_table.entrySet())
            if (entry.getValue().size()==0)
                return false;
        return true;
    }
	 /**
      *
     */
    public void addOneChainIteration(int chainId, double llh, double perplexity)
    {
        llh_table.get(chainId).add(llh);
        perplexity_table.get(chainId).add(perplexity);
    }
    /**
     * check if all chains converge
     * @return
     */
    public boolean checkAllChainConvergence(){
        boolean allChainConverged = true;
        for (int cid : llh_table.keySet()){
            System.out.println(Debugger.getCallerPosition() + "checking convergence for chain-id=" + cid);
            // TODO call checkConvergence() function first.  if allChainConverged==false the loop will terminate automatically.
            allChainConverged = checkConvergence(cid) && allChainConverged;
            System.out.println(Debugger.getCallerPosition()+"finish checking convergence for chain-id="+cid);
        }

        this.writeConvergenceHistory();

        return allChainConverged;
    }
    /**
     * Check whether the multiple chains converge or not
     * TODO[DONE] debug: it seems chain-1 never enter this method
     * @return
     */
    public boolean checkConvergence(int chainId)
    {
        System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" enter check convergence");

        double llh_rHat = get_llh_estimatedPotentialScaleReduction();
        double perplexity_rHat = get_perplexity_estimatedPotentialScaleReduction();

        System.out.println(Debugger.getCallerPosition()+" chain-"+chainId+" llh_rhat="+llh_rHat+" perplexity_rhat="+perplexity_rHat);

        llh_rhat_table.get(chainId).add(llh_rHat);
        perplexity_rhat_table.get(chainId).add(perplexity_rHat);

        return llh_rHat <= cmdOption.R_HAT_THRESH && perplexity_rHat <= cmdOption.R_HAT_THRESH;
    } 
    
    /**
     * From all chains llh calculate rHat value
     * @return
     */
    private double get_llh_estimatedPotentialScaleReduction() {
        double rHat = Double.MAX_VALUE;

        //min length of all chains
        int S = Integer.MAX_VALUE;
        for (List<Double> list : llh_table.values())
            if (list.size() < S)
                S = list.size();

        Map<Integer, Double> chaimMeans = new HashMap<>();
        double W = 0;
        double B = 0;
        double allChainMean = 0;

        for (int cid : llh_table.keySet()){
            List<Double> llh_list = llh_table.get(cid);
            double cmean = 0;
            double cvar = 0;
            for (int i=0; i<S; i++)
                cmean = ( cmean*i + llh_list.get(i) ) /(i+1);
            for (int i=0; i<S; i++)
                cvar += (llh_list.get(i)-cmean) * (llh_list.get(i)-cmean);
//            System.out.println(Debugger.getCallerPosition()+" chainId-"+cid+" chain mean="+cmean+" chain variance="+cvar);

            allChainMean += cmean;
            cvar /= S;
            W += cvar;
            chaimMeans.put(cid, cmean);
        }
        W /= llh_table.size();
        allChainMean /= llh_table.size();
        for (int cid : chaimMeans.keySet())
            B += (chaimMeans.get(cid) - allChainMean) * (chaimMeans.get(cid) - allChainMean);
        B *= ((double)S / (llh_table.size()-1));

        System.out.println(Debugger.getCallerPosition()+" S="+S+" W="+W+" B="+B);

        rHat = Math.sqrt( (double)(S-1)/(S) + B/(W*S) );
        return rHat;
    }
    /**
     * From all chains perplexity calculate rHat value
     * @return
     */
    private double get_perplexity_estimatedPotentialScaleReduction() {
        double rHat = Double.MAX_VALUE;

        //min length of all chains
        int S = Integer.MAX_VALUE;
        for (List<Double> list : perplexity_table.values())
            if (list.size() < S)
                S = list.size();

        Map<Integer, Double> chaimMeans = new HashMap<>();
        double W = 0;
        double B = 0;
        double allChainMean = 0;

        for (int cid : perplexity_table.keySet()){
            List<Double> ppl_list = perplexity_table.get(cid);
            double cmean = 0;
            double cvar = 0;
            for (int i=0; i<S; i++)
                cmean = ( cmean*i + ppl_list.get(i) ) /(i+1);
            for (int i=0; i<S; i++)
                cvar += (ppl_list.get(i)-cmean) * (ppl_list.get(i)-cmean);

            allChainMean += cmean;
            cvar /= S;
            W += cvar;
            chaimMeans.put(cid, cmean);
        }
        W /= perplexity_table.size();
        allChainMean /= perplexity_table.size();
        for (int cid : chaimMeans.keySet())
            B += (chaimMeans.get(cid) - allChainMean) * (chaimMeans.get(cid) - allChainMean);
        B *= ((double)S / (perplexity_table.size()-1));

        System.out.println(Debugger.getCallerPosition()+" S="+S+" W="+W+" B="+B);

        rHat = Math.sqrt( (double)(S-1)/(S) + B/(W*S) );
        return rHat;
    }

    /**
     * Sets this chain as finished. If all other chains are finished as well, their summaryfiles are removed
     * (To be precise: are marked for deletion once this VM is exited)
     *
     * @return true if this chain is the last chain
     */
    public boolean finish() {
        if (!checkAllChainConvergence()) {
            System.err.println(Debugger.getCallerPosition()+
            		"ConvergenceDiagnosis#finish: chain has not converged. Finish must only be called after convergence.");
        }

        writeConvergenceHistory();
        return true;
    }

    /**
     * write RHat value, llh, perplexity history to file
     */
    public void writeConvergenceHistory(){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Util.getStatFileName(this.cmdOption.SAMPLER_ID))));
            bw.write("chain-id\titeration\tllh\tllh rHat\tperplexity\tperplexity rhat\n");
            for (int cid : llh_table.keySet()){
                List<Double> llh_list = llh_table.get(cid);
                List<Double> llh_rhat_list = llh_rhat_table.get(cid);
                List<Double> perplexity_list = perplexity_table.get(cid);
                List<Double> perplexity_rhat_list = perplexity_rhat_table.get(cid);

                int l = Math.min(Math.min(llh_list.size(), llh_rhat_list.size()), Math.min(perplexity_list.size(), perplexity_rhat_list.size()));

                System.out.println(Debugger.getCallerPosition()+llh_list.size()+" "+llh_rhat_list.size()+" "+perplexity_list.size()+" "+perplexity_rhat_list.size());
                System.out.println(Debugger.getCallerPosition()+" writing convergence history for chain-"+cid+" chain length="+l);

                for (int i=0; i<l; i++)
                    bw.write(cid+"\t"+i+"\t"+llh_list.get(i)+"\t"+llh_rhat_list.get(i)+"\t"+perplexity_list.get(i)+"\t"+perplexity_rhat_list.get(i)+"\n");

                bw.write("\n");
            }
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        CmdOption option = new CmdOption();
        CmdLineParser parser = new CmdLineParser(option);

        //1. get command line parameters
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println(Debugger.getCallerPosition() + "Command line error: " + e.getMessage());
            return;
        }

        List<Integer> chain_id_list = new ArrayList<>();
        chain_id_list.add(0);
        chain_id_list.add(1);
        ConvergenceDiagnose covg = new ConvergenceDiagnose(chain_id_list, option);

        covg.addOneChainIteration(0, 0.1, 0.2);
        covg.addOneChainIteration(0, 0.11, 0.22);
        covg.addOneChainIteration(0, 0.12, 0.23);
        covg.addOneChainIteration(0, 0.12, 0.22);
        covg.addOneChainIteration(0, 0.12, 0.21);
        covg.addOneChainIteration(0, 0.12, 0.21);
        covg.addOneChainIteration(0, 0.11, 0.22);
        covg.addOneChainIteration(0, 0.10, 0.21);

        covg.addOneChainIteration(1, 0.1, 0.2);
        covg.addOneChainIteration(1, 0.101, 0.202);
        covg.addOneChainIteration(1, 0.12, 0.231);
        covg.addOneChainIteration(1, 0.112, 0.212);
        covg.addOneChainIteration(1, 0.112, 0.19);
        covg.addOneChainIteration(1, 0.12, 0.2);
        covg.addOneChainIteration(1, 0.1, 0.252);
        covg.addOneChainIteration(1, 0.9, 0.21);

        covg.checkAllChainConvergence();
    }
}
