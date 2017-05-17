#PBS -l nodes=1:ppn=48
#PBS -l walltime=2400:00:00

# DATA_SIZE='put_the_path_of_data_here'
DATE='20170516'

for DATA_SIZE in 500 #100 500 1000 1500 2000 5000 10000 20000 30000 40000 50000
do
    DATA_PATH='/Users/hcao/DATA_HD2/local/TKDE_clean_data/twitter'$DATA_SIZE'/'

    ## serial Gibbs sampling
    # for ZNUM in 10 20 30 40 50 
    # do
    #     SAMPLER_ID='twitter'$DATA_SIZE'-z'$ZNUM'-cim-iterator-'$DATE
    # 
    #     java -Xmx40G -ea -cp infdetection.jar sampling.MainInfDetection -chainNum 2 -graphfile $DATA_PATH'/cite.txt' -datafile $DATA_PATH'/inf_data.txt' -samplerId $SAMPLER_ID-iterator -znum $ZNUM  -burnin 10 -numIter 100 -duplicate yes  -concurrent n -checkConsistence n -access iterator -model cim  > log/$SAMPLER_ID-serial-iterator.log  2>&1 
    # done

    ## parallel Gibbs sampling
    # # of thread
    NUM_THREAD=4
    for ZNUM in 10 # 20 30 40 50 #60 70 80 90 100
    do
        SAMPLER_ID='twitter'$DATA_SIZE'-z'$ZNUM'-cim-parallel-iterator-'$DATE

        java -Xmx8G -ea -cp infdetection.jar sampling.MainInfDetection -chainNum 2 -graphfile $DATA_PATH'/cite.txt' -datafile $DATA_PATH'/inf_data.txt' -samplerId $SAMPLER_ID-iterator -znum $ZNUM  -burnin 10 -numIter 100 -duplicate yes  -concurrent y -numThread $NUM_THREAD -checkConsistence n -access iterator -model cim  > log/$SAMPLER_ID.log  2>&1 
    done
done
