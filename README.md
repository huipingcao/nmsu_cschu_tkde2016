This is the code repository for paper:
Chuan Hu, Huiping Cao: Aspect-level Influence Discovery from Graphs, IEEE Transactions on Knowledge and Data Engineering (TKDE) vol.PP, no.99, pp.1-1 (March 2016). http://dx.doi.org/10.1109/TKDE.2016.2538223

Data link: TODO

Data format: please refer to https://github.com/hannahcao/nmsu_cs_chu_sdm2014

Command usage:
java -cp infdetection.jar sampling.MainInfDetection 
-model <model: oaim, laim or cim> 
-chainNum <# of chains> 
-graphfile <path to graph file>
-datafile <path to object profile data file>
-samplerId <sampler id>
-znum <# of topics>
-anum <# of latent aspects>
-burnin <# of burnin iterations>
-numIter <# of total iterations>
-concurrent <y or n. indicate parallel Gibbs sampling or not> 
-numThread <# of threads>
-checkConsistence <y or n. whether check consistence of counts. mainly used in debug> 
-debug <y or n. debug on sampling process>
-printThread <y or n. whether print thread running time details>
-access <iterator or index>

Example shell files:
1. cim_efficience.sh. run CIM model on Citeseerx data set
2. twitter50000_oaim_parallel.sh. run OAIM model on Twitter50000 data set
