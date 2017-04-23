package util;

import org.kohsuke.args4j.Option;

import java.io.Serializable;

public class CmdOption implements Serializable{

	@Option(name="-help", usage="Print this help info")
	public boolean help = false;
	
	@Option(name="-graphfile", usage="Input graph file name (default: empty)")
	public String graphfile = "";

	@Option(name="-datafile", usage="Input paper folder (default: empty)")
	public String datafile = "";

	@Option(name="-duplicate", usage="Input paper folder (default: empty)")
	public String duplicate = "yes";

	@Option(name="-model", usage="laim or oaim (default:oaim)")
	public String model = "oaim";

	@Option(name="-znum", usage="Number of latent topics (default: 10)")
	public int znum = 10;

	@Option(name="-anum", usage="Number of latent aspects (default: 10)")
	public int anum = 10;
	
	@Option(name="-numIter", usage="Number of Gibbs sampling iterations  (default: 1000)")
	public int numIter = 1000;
	
	@Option(name="-burnin", usage="BURN IN iterations for Gibbs Sampling (default: 10)")
	public int burnin = 10;
	
	@Option(name="-chainNum", usage="The number of chains used to judge convergence (default: 2)")
	public int chainNum = 2;
	
	@Option(name="-rhat", usage="RHAT value for convergence (default: 1.01)")
	public double R_HAT_THRESH = 1.01;

	@Option(name="-samplerId", usage="The sampler id string")
	public String SAMPLER_ID = "";
	
    //Parameters for distributions p(w|latent-state)
	@Option(name="-alphaPhi", usage="Dirichlet parameter alphaPhi for latent state variables (Default: 0.01)")
	public double alphaPhi=0.01;
	
	//Dirichlet for p(latent-state|object)
	@Option(name="-alphaTheta", usage="Dirichlet parameter alphaTheta (Default: 0.1)")
	public double alphaTheta=0.1;
	
	@Option(name="-alphaGamma", usage="Dirichlet parameter alphaGamma for object interaction mixture (Default: 1.0)")
	public double alphaGamma=0.01;

	@Option(name = "-alphaPsi", usage = "Dirichlet parameter alphaPsi for latent aspect to observed aspect mixture")
	public double alphaPsi = 0.1;

	@Option(name = "-alphaEta", usage = "Dirichlet parameter alphaPsi for latent aspect distribution")
	public double alphaEta = 0.1;

	@Option(name="-alphaLambdaInherit", usage="Dirichlet parameter inherit percentage (Default: 0.5)")
	public double alphaLambdaInherit=50;
	
	@Option(name="-alphaLambdaInnov", usage="Dirichlet parameter innovative percentage (Default: 0.5)")
	public double alphaLambdaInnov=50;
	
    @Option(name = "-numThread", usage = "number of thread during sampling.")
    public int numThread = -1;

    @Option(name = "-concurrent", usage = "use single thread or multi thread to do Gibbs sampling.  y or n (Default y)")
    public String concurrent = "n";

    @Option(name = "-checkConsistence", usage = "whether turning on consistence check. y or n (Default y)")
    public String checkConsistence = "y";

	@Option(name = "-checkConvergence", usage = "whether check convergence.  Default y")
	public String checkConvergence = "y";

	@Option(name = "-printThread", usage = "whether print thread running information in each iteration.  y or n (Default n")
    public String  printThread = "n";

    @Option(name = "-debug", usage = "whether print debug information.  y or n (By default n)")
    public String debug = "n";

    @Option(name = "-access", usage = "count access pattern: index or iterator")
    public String access = "iterator";
}
