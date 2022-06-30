package montecarlo;

import java.util.List;
import java.util.Random;

public class MonteCarlo{
    public static final Random random = new Random();

    private State root;
    private AvailableActions availableActions;
    private UpdateState updateState;
    private Reward reward;
    private RollOutAction rollOutAction;
    private MaximizingConditions maximizingConditions;

    public MonteCarlo(){
        root = null;
    }

    public MonteCarlo(float...encodeStates){
        init(encodeStates);
    }

    public MonteCarlo init(float...encodeStates){
        float[] tempState = new float[encodeStates.length];
        System.arraycopy(encodeStates,0,tempState,0,tempState.length);
        root = new State(tempState);
        return this;
    }

    public MonteCarlo setTemperatureParameter(float temperatureParameter){
        root.setTemperatureParameter(temperatureParameter);
        return this;
    }

    public MonteCarlo setAvailableActions(AvailableActions availableActions) {
        this.availableActions = availableActions;
        return this;
    }

    public MonteCarlo setUpdateState(UpdateState updateState) {
        this.updateState = updateState;
        return this;
    }

    public MonteCarlo setUpdateValue(Reward reward) {
        this.reward = reward;
        return this;
    }

    public MonteCarlo setRollOutAction(RollOutAction rollOutAction) {
        this.rollOutAction = rollOutAction;
        return this;
    }

    public MonteCarlo setMaximizingConditions(MaximizingConditions maximizingConditions) {
        this.maximizingConditions = maximizingConditions;
        return this;
    }

    public void adjustRoot(int...actionsPerformed){
        for (int action : actionsPerformed)
            root = root.child(action);
    }

    public int bestAction(int totalIteration) {
        //ROOT SELECTION
        State currentState = root;
        while (root.iteration() < totalIteration) {
            //check if state is leaf?
            if (!currentState.isLeafNode()) {
                //non leaf state
                //SELECTION
                //Select child with maximum UCB
                if(maximizingConditions.isMaximizing(currentState.encodeStates()))
                    currentState = currentState.maxUCB1Child();
                else
                    currentState = currentState.minUCB1Child();
                continue;
            }

            //leaf state
            //check if state is sampled?
            if (currentState.isSampled() || currentState.isRoot()) {
                //either not sampled or root node
                //EXPAND for each available actions
                if(!currentState.isTerminal()){
                    //not terminal state
                    List<Integer> actions = availableActions.availableActions(currentState.encodeStates());
                    currentState.expand(updateState,actions);
                    //Select the first child
                    currentState = currentState.firstChild();
                }
            }

            //already sampled
            //ROLLOUT
            float value = rollout(currentState);

            //BACK PROPAGATION
            currentState.update(value);

//            System.out.println(root);

            //assign current state to root state
            currentState = root;
        }
        return root.bestActionChild();
    }

    private float rollout(State currentState) {
        State newState = currentState;
        //break until state is terminal state
        while (!newState.isTerminal()) {
            //select random action from available actions
            List<Integer> actions = availableActions.availableActions(newState.encodeStates());
            if(actions.isEmpty()){
                newState.makeTerminal();
            }else{
                int selectedAction;
                if(rollOutAction==null)
                    selectedAction = actions.get(random.nextInt(0, actions.size()));
                else
                    selectedAction = rollOutAction.rollOutAction(newState.encodeStates());
                //find new state after action
                newState = new State(newState, selectedAction, updateState,true);
            }
        }
        return reward.reward(newState.encodeStates());
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public State getRoot() {
        return root;
    }

    ////////////////////////INTERFACES////////////////////////////////////////

    @FunctionalInterface
    public interface UpdateState{
        float[] updateState(float[] currentEncodedState,int action,boolean isRollOut);
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
    public interface RollOutAction{
        int rollOutAction(float[] currentEncodedState);
    }

    @FunctionalInterface
    public interface MaximizingConditions{
        boolean isMaximizing(float[] currentEncodedState);
    }


}
