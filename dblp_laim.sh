#PBS -l nodes=1:ppn=48
#PBS -l walltime=240:00:00

DATA_SIZE='put_the_path_of_data_here'
DATE='put_date_here'

NUM_THREAD=20

ZNUM=10
for ANUM in 10 20 50 100 200 
do
    SAMPLER_ID='dblp-laim-z'$ZNUM'-thread'$NUM_THREAD-$DATE
    java -Xmx220G -cp infdetection.jar sampling.MainInfDetection -model laim -chainNum 2 -graphfile $DATA_PATH'/cite.txt' -datafile $DATA_PATH'/inf_data.txt'  -samplerId $SAMPLER_ID -znum $ZNUM -anum $ANUM  -burnin 10 -numIter 30 -duplicate yes  -concurrent y -numThread $NUM_THREAD -checkConsistence n -debug n -printThread n -access iterator > ${SAMPLER_ID}.log
done

for ANUM in 10 20
do
    for ZNUM in 10 20 30 40 50
    do
        SAMPLER_ID='dblp-laim-z'$ZNUM'-thread'$NUM_THREAD-$DATE
        java -Xmx220G -cp infdetection.jar sampling.MainInfDetection -model laim -chainNum 2 -graphfile $DATA_PATH'/cite.txt' -datafile $DATA_PATH'/inf_data.txt'  -samplerId $SAMPLER_ID -znum $ZNUM -anum $ANUM  -burnin 10 -numIter 30 -duplicate yes  -concurrent y -numThread $NUM_THREAD -checkConsistence n -debug n -printThread n -access iterator > ${SAMPLER_ID}.log
    done
done
