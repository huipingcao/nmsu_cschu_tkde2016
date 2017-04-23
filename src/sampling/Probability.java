package sampling;


import util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Probability {
    /**
     * draw an influencing sample by Gibbs sampling.
     * @param uprime
     * @param w
     * @param aspect
     * @param data
     * @param cmdOption
     * @return latent topic z, sample probability of the input token.
     */
    public static double[] drawInfluencingSample(MiniDistribution map_z_dist, int uprime, int w, int aspect, final SampleData data, final CmdOption cmdOption){
        map_z_dist.clear();

        double sample_prob = 0;

        if (cmdOption.access.equals("index")){
            // each latent state, calculate its posterior probability p(z|others) by HashMap key access
            for (int z = 0; z < cmdOption.znum; z++) {
                double prob = Probability.influencingPosterior(uprime, w, z, aspect, data, cmdOption);
                map_z_dist.put(z, prob);
                sample_prob += prob;
            }
        } else if (cmdOption.access.equals("iterator")){
            double[] phi_dist = new double[cmdOption.znum];
            //        numerator of phi
            Map<Integer, Double> N_w_z = data.N_wz_all.get(w);
            if (N_w_z!=null)
                for (Map.Entry<Integer, Double> entry : N_w_z.entrySet())
                    phi_dist[entry.getKey()] = entry.getValue()+cmdOption.alphaPhi;

            //        denominator of phi
            for (Map.Entry<Integer, Double> entry : data.N_z_all.entrySet())
                phi_dist[entry.getKey()] /= entry.getValue() + cmdOption.alphaPhi*Constant.wordNum;

            for (int z=0; z<cmdOption.znum; z++)
                if (phi_dist[z]==0)
                    phi_dist[z] = 1.0/Constant.wordNum;

            //        theta prime
            double[] theta_prime_dist = new double[cmdOption.znum];
            double theta_prime_den = Util.get1Map(data.N_up_inher_influenced, uprime)
                    +Util.get1Map(data.N_up_influencing, uprime)+cmdOption.znum*cmdOption.alphaTheta;

            if (data.N_upz_influencing.containsKey(uprime))
                for (Map.Entry<Integer, Double> entry : data.N_upz_influencing.get(uprime).entrySet())
                    theta_prime_dist[entry.getKey()] = entry.getValue();

            if (data.N_upz_inher_influenced.containsKey(uprime))
                for (Map.Entry<Integer, Double> entry : data.N_upz_inher_influenced.get(uprime).entrySet())
                    theta_prime_dist[entry.getKey()] += entry.getValue()+cmdOption.alphaTheta;

            try { //TODO debug negative probability
                for (int z=0; z<cmdOption.znum; z++) {
                    if (theta_prime_dist[z] == 0)
                        theta_prime_dist[z] = cmdOption.alphaTheta;
                    theta_prime_dist[z] /= theta_prime_den;

                    double sample_prob_z = theta_prime_dist[z] * phi_dist[z];
                    sample_prob += sample_prob_z;

                    map_z_dist.put(z, sample_prob_z);
                }
            }
            catch (AssertionError error){
                error.printStackTrace();
                System.out.println(Debugger.getCallerPosition()+" phi="+ Arrays.toString(phi_dist)+"\ntheta="+
                  Arrays.toString(theta_prime_dist));
                System.exit(0);
            }
        }

        return new double[]{map_z_dist.draw(), Math.log(sample_prob)};
    }

    public static double[] drawInfluencedSample(MiniDistribution map_bopza_dist, int u, int w, int aspect, final SampleData data, final CmdOption cmdOption){

        map_bopza_dist.clear();

        double sample_prob0=0; double sample_prob1=0;

        // direct draw sampling.  copied from Probability.getMAPInfluenced_zbaop_blocked. 20150303
        if (cmdOption.access.equals("index")){
//            index access to hashmap
            for (int z = 0; z < cmdOption.znum; z++) {
                double probGj = Probability.influencedPosterior(
                        u, w, aspect, z, -1, Constant.INNOTVATION, -1, data, cmdOption);
                sample_prob0 += probGj;

                map_bopza_dist.put(Constant.INNOTVATION, z, -1, probGj);
            }
            //calculate the probability of p(z=*,oprime=*|b=innovative) when b is innovative
            //index access to hashmap
            for (int uprime : data.in_userGraph.get(u)) {
                for (int z = 0; z < cmdOption.znum; z++) {//for each latent state
                    if ((uprime == u) || (data.testSet.contains(uprime) ))
                        continue;

                    double probGj = 0;
                    if (cmdOption.model.equals("oaim")){
                        probGj = Probability.influencedPosterior(
                                u, w, aspect, z, -1, Constant.INHERITANCE, uprime, data, cmdOption);
                        map_bopza_dist.put(Constant.INHERITANCE, z, uprime, probGj);
                        sample_prob1 += probGj;
                    }
                    else if (cmdOption.model.equals("laim")){
                        for (int la=0; la<cmdOption.anum; la++){
                            probGj = Probability.influencedPosterior(
                                    u, w, aspect, z, la, Constant.INHERITANCE, uprime, data, cmdOption);
                            map_bopza_dist.put(Constant.INHERITANCE, z, uprime, la, probGj);
                            sample_prob1 += probGj;
                        }
                    }
                }
            }
        } else if (cmdOption.access.equals("iterator")){
            double[] phi_dist = new double[cmdOption.znum];
//        numerator of phi
            Map<Integer, Double> N_w_z = data.N_wz_all.get(w);
            for (Map.Entry<Integer, Double> entry : N_w_z.entrySet())
                phi_dist[entry.getKey()] = entry.getValue() + cmdOption.alphaPhi;

//        denominator of phi
            for (Map.Entry<Integer, Double> entry : data.N_z_all.entrySet())
                phi_dist[entry.getKey()] /= entry.getValue() + cmdOption.alphaPhi * Constant.wordNum;

            for (int z = 0; z < cmdOption.znum; z++)
                if (phi_dist[z] == 0)
                    phi_dist[z] = 1.0 / Constant.wordNum;

//        numerator of theta
            double[] theta_dist = new double[cmdOption.znum];
            // denominator of theta
            double theta_den = Util.get2Map(data.N_ub_influenced, u, Constant.INNOTVATION) + cmdOption.znum * cmdOption.alphaTheta;
            if (data.N_uz_innov_influenced.containsKey(u))
                for (Map.Entry<Integer, Double> entry : data.N_uz_innov_influenced.get(u).entrySet())
                    theta_dist[entry.getKey()] = (entry.getValue() + cmdOption.alphaTheta) / theta_den;

            double beta0 = (Util.get2Map(data.N_ub_influenced, u, Constant.INNOTVATION) + cmdOption.alphaLambdaInnov)/(Util.get1Map(data.N_u_influenced, u)+cmdOption.alphaLambdaInnov+cmdOption.alphaLambdaInherit);
            for (int z = 0; z < cmdOption.znum; z++) {
                if (theta_dist[z] == 0)
                    theta_dist[z] = 1.0 / cmdOption.znum;
                double sample_prob_z = beta0 * theta_dist[z] * phi_dist[z];
                sample_prob0 += sample_prob_z;
                map_bopza_dist.put(Constant.INNOTVATION, z, -1, sample_prob_z);
            }

            double beta1 = (Util.get2Map(data.N_ub_influenced, u, Constant.INHERITANCE) + cmdOption.alphaLambdaInherit)/(Util.get1Map(data.N_u_influenced, u)+cmdOption.alphaLambdaInnov+cmdOption.alphaLambdaInherit);
            if (cmdOption.model.equals("cim")) {
                //begin gamma_cim calculation
                double[] gamma_cim = new double[data.getUprimeNumber(u)];
                double gamma_oaim_den = Util.get2Map(data.N_ub_influenced, u, Constant.INHERITANCE) +
                  data.getUprimeNumber(u) * cmdOption.alphaGamma;
                //up index
                ArrayList<Integer> up_index_list = new ArrayList<>();
                int i = 0;      // up -> count
                if (data.N_uup_inher_influenced.containsKey(u))
                    for (Map.Entry<Integer, Double> entry : data.N_uup_inher_influenced.get(u).entrySet()) {
                        up_index_list.add(entry.getKey());
                        gamma_cim[i] = (entry.getValue() + cmdOption.alphaGamma) / gamma_oaim_den;
                        i++;
                    }
                double gamma_cim_default = cmdOption.alphaGamma / gamma_oaim_den;

                // N_uup = 0 up index
                List<Integer> zero_up_index = new ArrayList<>(data.in_userGraph.get(u));
                zero_up_index.removeAll(up_index_list);

                up_index_list.addAll(zero_up_index);

                for (; i < gamma_cim.length; i++)
                    gamma_cim[i] = gamma_cim_default;
                //end gamma_cim calculation

                assert gamma_cim.length == up_index_list.size() : "gamma_cim.length=" + gamma_cim.length + " up_index size="+up_index_list.size()+" uprime set="+data.in_userGraph.get(u).stream().map(a -> a.toString()).collect(Collectors.joining(", "));

                // gamma factor for CIM model
                double gamma_cim_den = Util.get2Map(data.N_ub_influenced, u, Constant.INHERITANCE)
                  +data.getUprimeNumber(u)*cmdOption.alphaGamma;


                for (int g = 0; g < gamma_cim.length; g++) {
                    int uprime = up_index_list.get(g);

                    double[] theta_prime_dist = new double[cmdOption.znum];
                    double theta_prime_den = Util.get1Map(data.N_up_inher_influenced, uprime)
                      + Util.get1Map(data.N_up_influencing, uprime) + cmdOption.znum * cmdOption.alphaTheta;
                    if (data.N_upz_influencing.containsKey(uprime))
                        for (Map.Entry<Integer, Double> entry : data.N_upz_influencing.get(uprime).entrySet())
                            theta_prime_dist[entry.getKey()] = entry.getValue();

                    if (data.N_upz_inher_influenced.containsKey(uprime))
                        for (Map.Entry<Integer, Double> entry : data.N_upz_inher_influenced.get(uprime).entrySet())
                            theta_prime_dist[entry.getKey()] += entry.getValue() + cmdOption.alphaTheta;

                    for (int z = 0; z < cmdOption.znum; z++) {
                        if (theta_prime_dist[z] == 0)
                            theta_prime_dist[z] = cmdOption.alphaTheta;
                        theta_prime_dist[z] /= theta_prime_den;

                        double sample_prob_gz =  beta1 * gamma_cim[g] * theta_prime_dist[z] * phi_dist[z];

                        sample_prob1 += sample_prob_gz;
                        map_bopza_dist.put(Constant.INHERITANCE, z, uprime, sample_prob_gz);
                    }
                }
            }
            else if (cmdOption.model.equals("oaim")) {
                //begin gamma_oaim calculation
                double[] gamma_oaim = new double[data.getUprimeNumber(u)];
                double gamma_oaim_den = Util.get2Map(data.N_uta_inher_influenced, u, aspect) +
                        data.getUprimeNumber(u) * cmdOption.alphaGamma;
                //up index
                ArrayList<Integer> up_index_list = new ArrayList<>();
                int i = 0;      // up -> count
                if (data.N_utaup_inher_influenced.containsKey(u) && data.N_utaup_inher_influenced.get(u).containsKey(aspect))
                    for (Map.Entry<Integer, Double> entry : data.N_utaup_inher_influenced.get(u).get(aspect).entrySet()) {
                        up_index_list.add(entry.getKey());
                        gamma_oaim[i] = (entry.getValue() + cmdOption.alphaGamma) / gamma_oaim_den;
                        i++;
                    }
                double gamma_oaim_default = cmdOption.alphaGamma / gamma_oaim_den;

                // N_utaup = 0 up index
                List<Integer> zero_up_index = new ArrayList<>(data.in_userGraph.get(u));
                zero_up_index.removeAll(up_index_list);

                up_index_list.addAll(zero_up_index);

                for (; i < gamma_oaim.length; i++)
                    gamma_oaim[i] = gamma_oaim_default;
                //end gamma_oaim calculation

                assert gamma_oaim.length == up_index_list.size() : "gamma_oaim.length=" + gamma_oaim.length + " up_index size="+up_index_list.size()+" uprime set="+data.in_userGraph.get(u).stream().map(a -> a.toString()).collect(Collectors.joining(", "));


                for (int g = 0; g < gamma_oaim.length; g++) {
                    int uprime = up_index_list.get(g);

                    double[] theta_prime_dist = new double[cmdOption.znum];
                    double theta_prime_den = Util.get1Map(data.N_up_inher_influenced, uprime)
                            + Util.get1Map(data.N_up_influencing, uprime) + cmdOption.znum * cmdOption.alphaTheta;
                    if (data.N_upz_influencing.containsKey(uprime))
                        for (Map.Entry<Integer, Double> entry : data.N_upz_influencing.get(uprime).entrySet())
                            theta_prime_dist[entry.getKey()] = entry.getValue();

                    if (data.N_upz_inher_influenced.containsKey(uprime))
                        for (Map.Entry<Integer, Double> entry : data.N_upz_inher_influenced.get(uprime).entrySet())
                            theta_prime_dist[entry.getKey()] += entry.getValue() + cmdOption.alphaTheta;

                    for (int z = 0; z < cmdOption.znum; z++) {
                        if (theta_prime_dist[z] == 0)
                            theta_prime_dist[z] = cmdOption.alphaTheta;
                        theta_prime_dist[z] /= theta_prime_den;

                        double sample_prob_gz =  beta1 * gamma_oaim[g] * theta_prime_dist[z] * phi_dist[z];

                        sample_prob1 += sample_prob_gz;
                        map_bopza_dist.put(Constant.INHERITANCE, z, uprime, sample_prob_gz);
                    }
                }
            }
            else if (cmdOption.model.equals("laim")){    //TODO refactor
                double[] eta_laim = new double[cmdOption.anum];
                double eta_laim_den = Util.get2Map(data.N_ub_influenced, u, Constant.INHERITANCE)+cmdOption.alphaPsi;

                double[] psi_laim = new double[cmdOption.anum];

                if (data.N_taa_inher_influenced.containsKey(aspect))
                    for (int latent_aspect : data.N_taa_inher_influenced.get(aspect).keySet()) {
                        psi_laim[latent_aspect] =
                                (Util.get2Map(data.N_taa_inher_influenced, aspect, latent_aspect)+cmdOption.alphaPsi)
                                        / (Util.get1Map(data.N_a_influenced, latent_aspect)+cmdOption.anum*cmdOption.alphaPsi);
                    }
                if (data.N_ua_inher_influenced.containsKey(u))
                    for (int latent_aspect : data.N_ua_inher_influenced.get(u).keySet()){
                        eta_laim[latent_aspect] = Util.get2Map(data.N_ua_inher_influenced, u, latent_aspect) / eta_laim_den;
                    }

                for (int latent_aspect=0; latent_aspect<cmdOption.anum; latent_aspect++){
                    if (psi_laim[latent_aspect]==0)
                        psi_laim[latent_aspect] = cmdOption.alphaPsi / (Util.get1Map(data.N_a_influenced, latent_aspect)+cmdOption.anum*cmdOption.alphaPsi);
                    if (eta_laim[latent_aspect]==0)
                        eta_laim[latent_aspect] = cmdOption.alphaEta / eta_laim_den;
                }

                for (int latent_aspect=0; latent_aspect<cmdOption.anum; latent_aspect++) {
                    double eta = eta_laim[latent_aspect];
                    double psi = psi_laim[latent_aspect];

                    //begin gamma_laim calculation
                    double[] gamma_laim = new double[data.getUprimeNumber(u)];
                    double gamma_laim_den = Util.get2Map(data.N_ua_inher_influenced, u, latent_aspect) +
                            data.getUprimeNumber(u) * cmdOption.alphaGamma;
                    //up index
                    ArrayList<Integer> up_index = new ArrayList<>();
                    int i = 0;      // up -> count
                    if (data.N_uaup_inher_influenced.containsKey(u) && data.N_uaup_inher_influenced.get(u).containsKey(aspect))
                        for (Map.Entry<Integer, Double> entry : data.N_uaup_inher_influenced.get(u).get(aspect).entrySet()) {
                            up_index.add(entry.getKey());
                            gamma_laim[i] = (entry.getValue() + cmdOption.alphaGamma) / gamma_laim_den;
                            i++;
                        }
                    double gamma_oaim_default = cmdOption.alphaGamma / gamma_laim_den;

                    // N_utaup = 0 up index
                    List<Integer> zero_up_index = new ArrayList<>(data.in_userGraph.get(u));
                    zero_up_index.removeAll(up_index);

                    up_index.addAll(zero_up_index);

                    for (; i < gamma_laim.length; i++)
                        gamma_laim[i] = gamma_oaim_default;
                    //end gamma_laim calculation

                    assert gamma_laim.length == up_index.size() : "gamma_laim.length=" + gamma_laim.length + " up_index size=" + up_index.size();

                    for (int g = 0; g < gamma_laim.length; g++) {
                        int uprime = up_index.get(g);
                        double[] theta_prime_dist = new double[cmdOption.znum];
                        double theta_prime_den = Util.get1Map(data.N_up_inher_influenced, uprime)
                                + Util.get1Map(data.N_up_influencing, uprime) + cmdOption.znum * cmdOption.alphaTheta;
                        if (data.N_upz_influencing.containsKey(uprime))
                            for (Map.Entry<Integer, Double> entry : data.N_upz_influencing.get(uprime).entrySet())
                                theta_prime_dist[entry.getKey()] = entry.getValue();

                        if (data.N_upz_inher_influenced.containsKey(uprime))
                            for (Map.Entry<Integer, Double> entry : data.N_upz_inher_influenced.get(uprime).entrySet())
                                theta_prime_dist[entry.getKey()] += entry.getValue() + cmdOption.alphaTheta;

                        for (int z = 0; z < cmdOption.znum; z++) {
                            if (theta_prime_dist[z] == 0)
                                theta_prime_dist[z] = cmdOption.alphaTheta;
                            theta_prime_dist[z] /= theta_prime_den;
                            double sample_prob_gz = beta1 * beta1 * gamma_laim[g] * theta_prime_dist[z] * phi_dist[z] * eta * psi;
                            sample_prob1 += sample_prob_gz;
                            map_bopza_dist.put(Constant.INHERITANCE, z, uprime, latent_aspect, sample_prob_gz);
                        }
                    }
                }
            }
        }

        double[] result = new double[cmdOption.model.equals("oaim")?4:5];

        //randomly draw new z, new b, new u' which follow the joint distribution calculated above
        result[0] = map_bopza_dist.draw(); //mapB
        result[1] = map_bopza_dist.getKey2Draw(); //mapZ
        result[2] = map_bopza_dist.getKey3Draw(); //mapUprime

        if (cmdOption.model.equals("oaim") || cmdOption.model.equals("cim")) {
            result[3] = Math.log(sample_prob0 + sample_prob1);
        }
        else if (cmdOption.model.equals("laim")){
            result[3] = map_bopza_dist.getKey4Draw(); //mapLatentAspect
            result[4] = Math.log(sample_prob0 + sample_prob1);
        }
        return result;
    }

/*    public static double[] drawInfluencedSample_seq(MiniDistribution map_bopza_dist, int u, int w, int aspect, final SampleData data, final CmdOption cmdOption) {

    }*/

        //p(z|...)= N1 *N2
    public static double influencingPosterior(int uprime, int w, int z, int aspect,
                                              final SampleData data, final CmdOption option) {//CitinfSampler.citedPosteriorT
		double phi = phi(z, w, data);
        double thetaPrime = thetaPrime(uprime, z, data);

		double prob =  phi * thetaPrime ; //should it be (N2*N3)*(N3*lambda)? TODO
        assert (prob > 0.0) : "probG0 must be positive but is " + prob +
        ". uprime=" + uprime + " w=" + w + " z=" + z+" phi="+phi+
          " thetaPrime="+thetaPrime;

        assert (!Double.isNaN(prob));
        assert (!Double.isInfinite(prob));
        
        return prob;
    }

    /**
     * computer joint probability of one position
     * @param uid
     * @param w
     * @param aspect
     * @param z
     * @param latent_aspect
     *@param b
     * @param uprime
     * @param data
     * @param cmdOption     @return
     */
	public static double influencedPosterior(int uid, int w, int aspect, int z, int latent_aspect, int b, int uprime,
                                             final SampleData data, final CmdOption cmdOption)
	{
		double prob = 0.0;
		if (b == Constant.INNOTVATION) {//for b=0, innovation 
			double beta = beta(uid, b, data); //p(b|.)
            double theta = theta(uid, z, data);// p(z|.)
            double phi = phi(z, w, data);

            prob = beta * theta * phi ;

            assert (beta > 0) : "b=0 beta must be positive but is"  + beta + ". uid=" + uid + " aspect=" + w + " z=" + z;
            assert (theta> 0) : "b=0 theta must be positive but is"  + theta + ". uid=" + uid + " aspect=" + w + " z=" + z;
            assert (phi> 0) : "b=0 phi must be positive but is"  + phi + ". uid=" + uid + " aspect=" + w + " z=" + z;

		} else {//b=1,
            if (cmdOption.model.equals("oaim")){

                double beta = beta(uid, b, data);//p(b|.)
                double thetaPrime = thetaPrime(uprime, z, data);
                double gamma = gamma_oaim(uid, aspect, uprime, data);
                double phi = phi(z, w, data);

                prob = beta * thetaPrime * gamma *  phi ;

                assert (beta > 0) : "b=1 beta must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (thetaPrime> 0) : "b=1 thetaPrime must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (phi > 0) : "b=1 phi must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (gamma > 0) : "b=1 gamma_oaim must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
            }
            else if (cmdOption.model.equals("laim")){
                double beta = beta(uid, b, data);//p(b|.)
                double thetaPrime = thetaPrime(uprime, z, data);
                double gamma = gamma_laim(uid, latent_aspect, uprime, data);
                double eta = eta(uid, latent_aspect, data);
                double psi = psi(latent_aspect, aspect, data);
                double phi = phi(z, w, data);

                prob = beta * thetaPrime * gamma * eta * psi * phi;

                assert (beta > 0) : "b=1 beta must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (thetaPrime> 0) : "b=1 thetaPrime must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (phi > 0) : "b=1 phi must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (gamma > 0) : "b=1 gamma_oaim must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (eta> 0) : "b=1 eta must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
                assert (psi > 0) : "b=1 psi must be positive but is"  + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
            }
		}
		
		assert (prob > 0.0) : "probG0 must be positive but is " + prob + ". uid=" + uid + " aspect=" + w + " z=" + z;
		assert (!Double.isNaN(prob));
		assert (!Double.isInfinite(prob));
		return prob;
    }

    /*
	  * Calculate the right part of formula (16) and (17)
	  * p(b|...)
	  */
	 public static double beta(int uid, int b, final SampleData data) {
         CmdOption cmdOption = data.cmdOption;

         double num;
         double den = Util.get1Map(data.N_u_influenced, uid)
           + cmdOption.alphaLambdaInnov
           + cmdOption.alphaLambdaInherit;

         if (b == Constant.INNOTVATION) {//b=0, p(bi=innovation| b not i, aspect, o', z), alpha1 = alphaLambdaInnov
             num = Util.get2Map(data.N_ub_influenced, uid, b) + cmdOption.alphaLambdaInnov;

         } else {//b=1,p(bi=inherit| b not i, aspect, o', z), alpha2 = alphaLambdaInherit
             num = Util.get2Map(data.N_ub_influenced, uid, b) + cmdOption.alphaLambdaInherit;
         }

         return num / den;
    }
	 
	 /**
	  * p(z|xxx) for both influenced (b=0)
	  * @return
	  */
	 private static double theta(int uid, int z, final SampleData data) {
         CmdOption cmdOption = data.cmdOption;

		 double num = Util.get2Map(data.N_uz_innov_influenced, uid, z) + cmdOption.alphaTheta;
		 double den = Util.get2Map(data.N_ub_influenced, uid, Constant.INNOTVATION) + cmdOption.znum*cmdOption.alphaTheta;

		 return num / den;
    }
    /**
     *  p(z|xxx) for both influenced (b=1) and influencing
     * @param uprime
     * @param z
     * @param data
     * @return
     */
	 private static double thetaPrime(int uprime, int z, final SampleData data) {
         CmdOption cmdOption = data.cmdOption;

		 double num = Util.get2Map(data.N_upz_inher_influenced, uprime, z)+
           Util.get2Map(data.N_upz_influencing, uprime, z) + cmdOption.alphaTheta;
		 double den = Util.get1Map(data.N_up_inher_influenced, uprime) +
           Util.get1Map(data.N_up_influencing, uprime) + cmdOption.znum*cmdOption.alphaTheta;

		 return num / den;
    }

    /**
     *
     * @param uid
     * @param uprime
     * @param data
     * @return
     */
    public static double gamma_oaim(int uid, int aspect, int uprime, SampleData data) {
        CmdOption cmdOption = data.cmdOption;

    	double num = Util.get3Map(data.N_utaup_inher_influenced, uid, aspect, uprime) + cmdOption.alphaGamma;
    	double den = Util.get2Map(data.N_uta_inher_influenced, uid, aspect) +
          data.getUprimeNumber(uid)*cmdOption.alphaGamma;

        return num / den;
    }


    public static double gamma_laim(int uid, int latent_aspect, int uprime, SampleData data) {
        CmdOption cmdOption = data.cmdOption;

        double num = Util.get3Map(data.N_uaup_inher_influenced, uid, latent_aspect, uprime) + cmdOption.alphaGamma;
        double den = Util.get2Map(data.N_ua_inher_influenced, uid, latent_aspect) +
          data.getUprimeNumber(uid)*cmdOption.alphaGamma ;

        return num / den;
    }

    public static double eta(int uid, int latent_aspect, SampleData data){
        CmdOption cmdOption = data.cmdOption;

        double num = Util.get2Map(data.N_ua_inher_influenced, uid, latent_aspect) + cmdOption.alphaEta;
        double den = Util.get2Map(data.N_ub_influenced, uid, Constant.INHERITANCE) + cmdOption.anum+cmdOption.alphaEta;

        return num / den;
    }

    public static double psi(int latent_aspect, int aspect, SampleData data){
        CmdOption cmdOption = data.cmdOption;

        double num = Util.get2Map(data.N_taa_inher_influenced, aspect, latent_aspect) + cmdOption.alphaPsi;
        double den = Util.get1Map(data.N_a_influenced, aspect) + Constant.aspectNum * cmdOption.alphaPsi;

        return  num / den;
    }

    /**
     * probability for word
     * @param z
     * @param w
     * @param data
     * @return
     */
    private static double phi(int z, int w, SampleData data){
        CmdOption cmdOption = data.cmdOption;

        double num = Util.get2Map(data.N_wz_all, w, z) + cmdOption.alphaPhi;
        double den = Util.get1Map(data.N_z_all, z) + Constant.wordNum * cmdOption.alphaPhi;

        return num / den;
    }

    public static void main(String[] args){

    }

}
