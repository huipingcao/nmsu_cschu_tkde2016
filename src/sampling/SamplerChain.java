package sampling;

import convergence.ConvergenceDiagnose;
import util.CmdOption;
import util.Constant;
import util.Debugger;
import preprocess.DataParsed;

import java.util.*;

/**
 * @author convergence
 *
 */
class SamplerRunnable implements Runnable{
	
	int chainNo;
	SamplerChain samplerChain;
	Set<Integer> testSet;

	/**
	 * Create a runnable sampler
	 * 
	 * @param _chainNo: the chain no of this runnable sampler
	 * @param _samplerChain: the sample chain this runnable belong to
	 */
	public SamplerRunnable(int _chainNo, SamplerChain _samplerChain)
	{
		chainNo = _chainNo;
		samplerChain = _samplerChain;
		this.testSet = _samplerChain.testSet;
	}


	@Override
	public void run() {
		
		boolean train = true;
		boolean test = false;
		
		boolean takeSamplesFromThisChain = (chainNo==samplerChain.allChainIds.get(0));
		System.out.println(Debugger.getCallerPosition()+"Chain: "+ chainNo);
		
		//2. create a sampler (set the parameters) 
		Sampler sampler = new Sampler(takeSamplesFromThisChain, chainNo, samplerChain);
		
		//3. initialize all the initial structures for sampling 
		// (read and parse documents, create intermediate data structure, etc.) 
		//sampler.init(samplerChain.parsedData);
		
		//4. draw the train sample
		sampler.drawInitSample(train);
		
		sampler.doGibbs(train);

		//5. draw the test sample
        // TODO
        //do test only when test set is not empty.
		if (!sampler.getSampleData().testSet.isEmpty()){
            sampler.drawInitSample(test);
            sampler.doGibbs(test);
        }

		if (takeSamplesFromThisChain) {//sampler id==0
			samplerChain.samplerToGetResult = sampler;
		}
		
	}
}


public class SamplerChain {

	//shared between sample chains
	DataParsed parsedData; 		//the data structure after parsing the original data
	CmdOption cmdOption;		//command options (with all the parameters)
	Sampler samplerToGetResult;	//from this sampler to derive the final results
	
	List<Integer> allChainIds;	//the total number of chains used to test the sampler convergence
	
	Set<Integer> testSet;
    //  operation -> iteration -> aspect

	ConvergenceDiagnose convDiag;

	/**
	 * Initialize sample chain, read sample data
	 * 
	 * Debugged & correct Aug. 17, 2012
	 * 
	 * @param _cmdOption
	 */
	public SamplerChain(CmdOption _cmdOption, Set<Integer> testSet)
	{

        Date beginCheckPoint = new Date();
		cmdOption = _cmdOption;
		Constant.zNum = cmdOption.znum;
		parsedData = new DataParsed();
		parsedData.init(cmdOption);
		this.testSet = testSet;

		allChainIds = new ArrayList<>();

		for(int i=0;i<cmdOption.chainNum;i++){
			allChainIds.add(i);
		}

		convDiag = new ConvergenceDiagnose(allChainIds, cmdOption);

        Date endCheckPoint = new Date();

        System.out.println("\n"+ Debugger.getCallerPosition()+" input_loading_and_parsing_time=" +
          ( endCheckPoint.getTime()-beginCheckPoint.getTime()) );
	}
	
	/**
	 * Run two threads and do gibbs sample
	 */
	public void doGibbs()
    {
		System.out.println(Debugger.getCallerPosition()+"allChainids size="+ allChainIds.size()+":" +allChainIds);
		
		//Create the sampler threads 
		final List<Thread> threadlist = new ArrayList<Thread>();
		for (int chain : allChainIds) {
			Runnable runnableSampler = new SamplerRunnable(chain, this);//why pass this?  See line 45
			Thread thread = new Thread(runnableSampler,"train-" + chain);
			
			//if(chain.trim().equals("1")) break;//Huiping added, just use one chain
			threadlist.add(thread);
		}
     
		//Start the sampler threads
		for (Thread thread : threadlist) {
			System.out.println(Debugger.getCallerPosition()+"start thread" + thread.getId()+":" + thread.getName());
			thread.start();
		}
		
		for (Thread thread : threadlist) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println(Debugger.getCallerPosition()+"interrupted");
			}
		} 
    }

	/**
	 * calculate the whole likelihood of model
	 * @return
	 */
	public double getLogLikelihoodAll(){return this.samplerToGetResult.getSampleData().llh;}

}
