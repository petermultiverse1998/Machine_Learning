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
    private UpdateValue updateValue;

    private float breakingReward = -INFINITY;

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

    public MiniMax setBreakingReward(float breakingReward){
        this.breakingReward = breakingReward;
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

    public MiniMax setUpdateValue(UpdateValue updateValue) {
        this.updateValue = updateValue;
        return this;
    }

    private float reward(float[] currentEncodedState,int action){
        float[] newEncodedState = new float[currentEncodedState.length];
        updateState.updateState(newEncodedState,action,currentEncodedState);
        List<Integer> actions = availableActions.availableActions(newEncodedState);
        if(actions.isEmpty()){
            return updateValue.updateValue(newEncodedState);
        }

        float reward = INFINITY;
        for (Integer integer : actions) {
            reward = Math.min(reward, reward(newEncodedState, integer));
            if(reward<=breakingReward)
                break;
        }
        return reward;
    }

    public int bestAction(){
        List<Integer> rootActions = availableActions.availableActions(rootEncodedState);
        List<Float> scores = new ArrayList<>();
        for (Integer rootAction : rootActions)
            scores.add(reward(rootEncodedState,rootAction));
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
        void updateState(float[] newEncodedState,int action,float[] currentEncodedState);
    }

    @FunctionalInterface
    public interface UpdateValue{
        float updateValue(float[] currentEncodedState);
    }

    @FunctionalInterface
    public interface AvailableActions{
        List<Integer> availableActions(float[] currentEncodedState);
    }

}
