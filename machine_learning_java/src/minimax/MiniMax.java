package minimax;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MiniMax {
    public static float INFINITY = 1e30f;

    public static final Random random = new Random();

    private float[] rootEncodedState;
    private AvailableActions availableActions;
    private UpdateState updateState;
    private Reward reward;
    private MaximizingConditions maximizingConditions;

    public MiniMax(){
       rootEncodedState = null;
    }

    public MiniMax(float...encodeStates){
        init(encodeStates);
    }

    public MiniMax init(float...encodeStates){
        rootEncodedState = new float[encodeStates.length];
        System.arraycopy(encodeStates,0, rootEncodedState,0, rootEncodedState.length);
        return this;
    }

    public MiniMax setAvailableActions(AvailableActions availableActions) {
        this.availableActions = availableActions;
        return this;
    }

    public MiniMax setUpdateState(UpdateState updateState) {
        this.updateState = updateState;
        return this;
    }

    public MiniMax setUpdateValue(Reward reward) {
        this.reward = reward;
        return this;
    }

    public MiniMax setMaximizingConditions(MaximizingConditions maximizingConditions) {
        this.maximizingConditions = maximizingConditions;
        return this;
    }


    private float minimax(float[] currentEncodedState, int action,boolean isMaximizing){
        float[] newEncodedState = updateState.newState(currentEncodedState,action);
        List<Integer> actions = availableActions.availableActions(newEncodedState);
        if(actions.isEmpty())
            return reward.reward(newEncodedState);

        if(isMaximizing){
            float maxReward = -INFINITY;
            for (Integer integer : actions) {
                float reward = minimax(newEncodedState,integer,maximizingConditions.isMaximizing(newEncodedState));
                maxReward = Math.max(maxReward,reward);
            }
            return maxReward;
        }else{
            float minReward = INFINITY;
            for (Integer integer : actions) {
                float reward = minimax(newEncodedState,integer,maximizingConditions.isMaximizing(newEncodedState));
                minReward = Math.min(minReward,reward);
            }
            return minReward;
        }
    }

    private float minimax(float[] currentEncodedState,float alpha,float beta, int action,boolean isMaximizing){
        float[] newEncodedState = updateState.newState(currentEncodedState,action);
        List<Integer> actions = availableActions.availableActions(newEncodedState);
        if(actions.isEmpty())
            return reward.reward(newEncodedState);

        if(isMaximizing){
            float maxReward = -INFINITY;
            for (Integer integer : actions) {
                float reward = minimax(newEncodedState,alpha,beta,integer,maximizingConditions.isMaximizing(newEncodedState));
                maxReward = Math.max(maxReward,reward);
                alpha = Math.max(alpha,reward);
                if(beta<=alpha)
                    break;
            }
            return maxReward;
        }else{
            float minReward = INFINITY;
            for (Integer integer : actions) {
                float reward = minimax(newEncodedState,alpha,beta,integer,maximizingConditions.isMaximizing(newEncodedState));
                minReward = Math.min(minReward,reward);
                beta = Math.min(beta,reward);
                if(beta<=alpha)
                    break;
            }
            return minReward;
        }
    }

    public int bestActionAlphaBetaPruning(){
        List<Integer> rootActions = availableActions.availableActions(rootEncodedState);
        List<Float> scores = new ArrayList<>();
        for (Integer rootAction : rootActions)
            scores.add(minimax(rootEncodedState,-INFINITY,INFINITY,rootAction,false));
        List<Integer> bestActions = bestActions(rootActions,scores);
        return bestActions.get(random.nextInt(bestActions.size()));
    }

    public int bestAction(){
        List<Integer> rootActions = availableActions.availableActions(rootEncodedState);
        List<Float> scores = new ArrayList<>();
        for (Integer rootAction : rootActions)
            scores.add(minimax(rootEncodedState,rootAction,false));
        List<Integer> bestActions = bestActions(rootActions,scores);
        return bestActions.get(random.nextInt(bestActions.size()));
    }
    private List<Integer> bestActions(List<Integer> rootActions,List<Float> scores){
        float maxScore = -INFINITY;
        for (float score : scores)
            maxScore = Math.max(maxScore,score);
        List<Integer> bestActions = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++)
            if(maxScore==scores.get(i))
                bestActions.add(rootActions.get(i));
        return bestActions;
    }

    ////////////////////////INTERFACES////////////////////////////////////////

    @FunctionalInterface
    public interface UpdateState{
        float[] newState(float[] currentEncodedState, int action);
    }

    @FunctionalInterface
    public interface Reward {
        float reward(float[] currentEncodedState);
    }

    @FunctionalInterface
    public interface AvailableActions{
        List<Integer> availableActions(float[] currentEncodedState);
    }

    @FunctionalInterface
    public interface MaximizingConditions{
        boolean isMaximizing(float[] prevEncodedState);
    }

}
