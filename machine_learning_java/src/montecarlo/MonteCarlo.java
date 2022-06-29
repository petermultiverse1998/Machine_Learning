package montecarlo;

import java.util.List;
import java.util.Random;

public class MonteCarlo{
    public static final Random random = new Random();

    private State root;
    private AvailableActions availableActions;
    private UpdateState updateState;
    private UpdateValue updateValue;
    private RollOutAction rollOutAction;

    public MonteCarlo(){
        root = null;
    }

    public MonteCarlo(float...encodeStates){
        init(encodeStates);
    }

    public MonteCarlo init(float...encodeStates){
//        if(root!=null)
//            root.removeAllChilds();
        root = new State(encodeStates);
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

    public MonteCarlo setUpdateValue(UpdateValue updateValue) {
        this.updateValue = updateValue;
        return this;
    }

    public MonteCarlo setRollOutAction(RollOutAction rollOutAction) {
        this.rollOutAction = rollOutAction;
        return this;
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
                currentState = currentState.maxUCB1Child();
                //continue;
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

            //System.out.println(root);

            //assign current state to root state
            currentState = root;
        }
        return root.bestActionChild();
    }

    public int bestActionSingleExpansion(int totalIteration) {
        //ROOT SELECTION
        State currentState = root;
        while (root.iteration() < totalIteration) {
            //check if state is leaf?
            if (!currentState.isLeafNode()) {
                //non leaf state
                //SELECTION
                //Select child with maximum UCB
                currentState = currentState.maxUCB1Child();
                //continue;
            }

            //leaf state
            //check if state is sampled?
//            if (currentState.isSampled() || currentState.isRoot()) {
            if (currentState.isSampled() && currentState.isRoot()) {
//            if (currentState.isSampled()) {
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

            //System.out.println(root);

            //assign current state to root state
            currentState = root;
        }
        return root.bestActionChild();
    }

    public int bestActionWithoutUCB(int totalIteration) {
        //ROOT SELECTION
        State currentState = root;
        int turn = 0;
        int totalActions = 1;
        int count = 0;
        float rate = 0.5f;
        int subIteration = (int) (rate*totalIteration);
        float subCount = 0;
        while (root.iteration() < totalIteration) {
            count++;
            subCount++;
            //check if state is leaf?
            if (!currentState.isLeafNode()) {
                //non leaf state
                //SELECTION
                //Select child with maximum UCB
//                currentState = currentState.maxUCB1Child();
                turn = (turn+1)%totalActions;
                currentState = currentState.childs().get(turn);
                //continue;
            }

            //leaf state
            //check if state is sampled?
//            if (currentState.isSampled() || currentState.isRoot()) {
            if (currentState.isSampled() && currentState.isRoot()) {
//            if (currentState.isSampled()) {
                //either not sampled or root node
                //EXPAND for each available actions
                if(!currentState.isTerminal()){
                    //not terminal state
                    List<Integer> actions = availableActions.availableActions(currentState.encodeStates());
                    if(actions.size()==1)
                        return actions.get(0);
                    totalActions = actions.size();
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

            //System.out.println(root);

            //assign current state to root state
            currentState = root;

            if(count>=totalIteration)
                break;
            if(subCount>=subIteration){
                int requiredSurvive = (int) Math.ceil(0.5*totalActions);
                while (totalActions>requiredSurvive) {
                    root.removeLowWinningChild();
                    totalActions--;
                    if(totalActions==1)
                        break;
                }
                if(root.childs().size()==1)
                    break;
                subCount=0;
                subIteration = (int) (rate*subIteration);
            }


        }
        return root.bestActionChild();
    }

    public int bestActionTime(int totalTimeInMillis) {
        long t0 = System.currentTimeMillis();

        //ROOT SELECTION
        State currentState = root;
        while ((System.currentTimeMillis()-t0) < totalTimeInMillis) {
            //check if state is leaf?
            if (!currentState.isLeafNode()) {
                //non leaf state
                //SELECTION
                //Select child with maximum UCB
                currentState = currentState.maxUCB1Child();
                continue;
            }

            //leaf state
            //check if state is sampled?
            if (currentState.isSampled() || currentState.isRoot()) {
//            if (currentState.isSampled()) {
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

            //System.out.println(root);

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
        return updateValue.updateValue(newState.encodeStates());
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
        void updateState(float[] newEncodedState,int action,float[] currentEncodedState,boolean isRollOut);
    }

    @FunctionalInterface
    public interface UpdateValue{
        float updateValue(float[] currentEncodedState);
    }

    @FunctionalInterface
    public interface AvailableActions{
        List<Integer> availableActions(float[] currentEncodedState);
    }

    @FunctionalInterface
    public interface RollOutAction{
        int rollOutAction(float[] currentEncodedState);
    }


}
