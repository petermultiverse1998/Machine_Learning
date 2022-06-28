package ann.network;

import ann.Network;

import java.util.Random;

public class QNetwork extends Network {
    protected float discountFactor  = 0.9f;
    protected float exploreFactor   = 0f;//0.9
    protected float exploreDecay    = 0.99f;
    protected float expectedReward  = 0;

    protected static Random random = new Random();

    public QNetwork() {
        super();
    }

    public QNetwork(String filename) {
        super(filename);
    }

    //Action for play
    public int predictAction(float...state){
        float[] qValue = qValue(state);
        float largest = qValue[0];
        int index = 0;
        for (int i = 0; i < qValue.length; i++) {
            if(qValue[i]<=largest)
                continue;
            largest = qValue[i];
            index = i;
        }
        return index;
    }

    ///////////////////////////////////FOR TRAINING///////////////////////////
    private float[] prevState;
    private int actionTaken;
    //initialize parameters
    public void initParameters(float discountFactor,float exploreFactor,float exploreDecay){
        this.discountFactor  = discountFactor;
        this.exploreFactor   = exploreFactor ;
        this.exploreDecay    = exploreDecay  ;
    }
    /**
     * Initialize state vector
     * @param stateSize : size of state
     */
    public void initState(int stateSize){
        prevState = new float[stateSize];
        actionTaken = -1;
        expectedReward = 0;
    }
    /**
     * if action to be taken is limited set the action
     * @param action : action to be set
     */
    public void forcedAction(int action){
        actionTaken = action;
    }
    /**
     * @param currentState  : current state
     * @param currentReward : current reward
     * @param trainNumber   : number of training
     * @return              : action for future state
     */
    public int reinforce(float[] currentState,float currentReward,int trainNumber){
        if(actionTaken ==-1){
            //beginning
            System.arraycopy(currentState,0,prevState,0,currentState.length);
            actionTaken = action(qValue(currentState));
            return actionTaken;
        }

        //If it is not beginning
        float[] qValues = qValue(currentState);
        float maxQValue = max(qValues);
        float targetReward = currentReward + discountFactor * maxQValue;
        expectedReward += maxQValue;
        float[] targetQ = new float[qValues.length];
        targetQ[actionTaken] = targetReward;

        //train
        for (int i = 0; i < trainNumber; i++)
            train(new float[][]{prevState},new float[][]{targetQ});

        //store state and action
        System.arraycopy(currentState,0,prevState,0,currentState.length);
        actionTaken = action(qValue(currentState));
        return actionTaken;
    }
    public float getCurrentExpectedReward(){
        return expectedReward;
    }

    /**
     * this is called after winning,loosing or end of game
     * @param currentReward : reward at end of game
     * @param trainNumber   : training number
     */
    public void reinforceLast(float currentReward,int trainNumber){
        //If it is not beginning
        float[] targetQ = new float[prevState.length];
        targetQ[actionTaken] = currentReward;
        expectedReward+=currentReward;

        //train
        for (int i = 0; i < trainNumber; i++)
            train(new float[][]{prevState},new float[][]{targetQ});
    }

    private int action(float[] qValue){

        //Exploration
        exploreFactor *=(1-exploreDecay);
        if(random.nextFloat()<=exploreFactor)
            return random.nextInt(qValue.length);

        //Exploitation
        float prob          = random.nextFloat();
        float cum_prob      = 0;
        float total_prob    = 0;
        for(float f:qValue)
            total_prob+=f;
        for (int i = 0; i < qValue.length; i++) {
            cum_prob += qValue[0]/total_prob;
            if(prob>cum_prob)
                continue;
            return i;
        }
        return qValue.length-1;
    }
    private float[] qValue(float[] state){
        return predict(state);
    }
    private static float max(float[] qValue){
        float largest = qValue[0];
        for (int i = 1; i < qValue.length; i++) {
            if(largest>=qValue[i])
                continue;
            largest = qValue[i];
        }
        return largest;
    }
}
