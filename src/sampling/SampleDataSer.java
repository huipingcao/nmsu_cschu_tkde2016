package sampling;

import util.CmdOption;

import java.io.Serializable;
import java.util.*;

/**
 * Created by chu on 8/26/14.
 * a wrapper for serializable SamplerData
 */
public class SampleDataSer implements Serializable{
    public CmdOption cmdOption;

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
    /**
     * N_{u,ta,up,b=1}
     */
    public Map<Integer, Map<Integer, Map<Integer, Double>>> N_utaup_inher_influenced;
    /**
     * N_{u,ta,b=1}
     */
    public Map<Integer, Map<Integer, Double>> N_uta_inher_influenced;


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

    /**
     * log likelihood of this model
     */
    public double llh = 0;
    /**
     * perplexity of this model
     */
    public double perplexity;

    public Map<Integer, List<Integer>> in_userGraph;

    public SampleDataSer(SampleData sampleData){
        this.cmdOption = sampleData.cmdOption;
        this.N_u_influenced = sampleData.N_u_influenced;
        this.N_ub_influenced = sampleData.N_ub_influenced;
        this.N_uz_innov_influenced = sampleData.N_uz_innov_influenced;
        this.N_up_inher_influenced = sampleData.N_up_inher_influenced;
        this.N_upz_inher_influenced = sampleData.N_upz_inher_influenced;
        this.N_uup_inher_influenced = sampleData.N_uup_inher_influenced;
        this.N_utaup_inher_influenced = sampleData.N_utaup_inher_influenced;
        this.N_uta_inher_influenced = sampleData.N_uta_inher_influenced;

        this.N_up_influencing = sampleData.N_up_influencing;
        this.N_upz_influencing = sampleData.N_upz_influencing;
        this.N_wz_all = sampleData.N_wz_all;
        this.N_z_all = sampleData.N_z_all;
        this.llh = sampleData.llh;
        this.perplexity = sampleData.perplexity;
        this.in_userGraph = sampleData.in_userGraph;
    }
}
