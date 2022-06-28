package montecarlo;

import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

public class State{
    public static float INFINITY = 1e30f;

    private final State parent;
    private final int leadingAction;
    private final List<State> childs;
    private final int layer;//for printing purpose
    private final float[] encodedStates;

    private float totalValue = 0f;
    private int iteration = 0;
    private boolean isTerminal = false;
    public float temperatureParameter = (float) Math.sqrt(2);

    public State(float... encodedStates){
        //root state
        parent = null;
        leadingAction = -1;
        childs = new ArrayList<>();
        layer = 0;
        this.encodedStates = encodedStates;
    }

    public State(State parent, int leadingAction, MonteCarlo.UpdateState function,boolean isRollOut){
        this.parent = parent;
        this.leadingAction = leadingAction;
        childs = new ArrayList<>();
        layer = parent.layer+1;
        this.encodedStates = new float[parent.encodedStates.length];
        function.updateState(this.encodedStates,leadingAction, parent.encodedStates,isRollOut);
    }

    public void setTemperatureParameter(float temperatureParameter) {
        this.temperatureParameter = temperatureParameter;
    }

    public void expand(MonteCarlo.UpdateState function, List<Integer>availableActions){
        for(int action:availableActions)
            childs.add(new State(this, action,function,false));
    }

    public State parent(){
        return parent;
    }

    public List<State> childs(){
        return childs;
    }

    public State child(int childLeadingAction){
        List<State> states = childs.stream().filter(state -> state.leadingAction==childLeadingAction).toList();
        if(!states.isEmpty())
            return states.get(0);
        return null;
    }

    public boolean isLeafNode(){
        return childs.isEmpty();
    }

    public boolean isSampled(){
        return iteration!=0;
    }

    public void remove(State child){
        childs.remove(child);
    }

    public void remove(int childLeadingAction){
        for (State child : childs) {
            if (child.leadingAction == childLeadingAction) {
                childs.remove(child);
                return;
            }
        }
    }

    public void removeAllChilds(){
        while (!childs.isEmpty())
            this.remove(childs.get(0));
    }

    public void update(float value){
        totalValue += value;
        iteration++;
        if(this.parent!=null)
            this.parent.update(value);
    }

    public float UCB1(){
        if(iteration==0 || this.parent==null)
            return INFINITY;
        return (float) ((totalValue/iteration)+temperatureParameter*Math.sqrt(Math.log(this.parent.iteration)/iteration));
    }

    public State maxUCB1Child(){
        return childs.stream().max((state,state1)->Float.compare(state.UCB1(),state1.UCB1())).orElse(null);
    }

    public State firstChild(){
        return childs.get(0);
    }

    public int iteration() {
        return iteration;
    }

    public void makeTerminal(){
        isTerminal = true;
    }

    public void disableTerminal(){
        isTerminal = false;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public float value(){
        return totalValue;
    }

    public float[] encodeStates() {
        return encodedStates;
    }

    public int bestActionChild(){
//        State bestChild = childs.stream().max((state,state1)-> Float.compare(state.iteration,state1.iteration)).orElse(null);
        State bestChild = childs.stream().max((state,state1)-> Float.compare(state.value(),state1.value())).orElse(null);
        if(bestChild==null)
            return -1;
        return bestChild.leadingAction;
//        return maxUCB1Child().leadingAction;
    }

    public void removeLowWinningChild(){
        State weakChild = childs.stream().max((state,state1)-> Float.compare(state1.value(),state.value())).orElse(null);
        if(weakChild!=null){
            for (State child:childs){
                if(child.leadingAction!=weakChild.leadingAction)
                    continue;
                childs.remove(child);
                break;
            }
        }
    }

    public boolean isRoot(){
        return this.leadingAction==-1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        //parent
        builder.append(layer).append(": ");
        builder.append("[action=").append(leadingAction)
                .append(", value=").append(totalValue)
                .append(", iteration=").append(iteration)
                .append(", UCB1=").append(UCB1())
                .append(", player=").append(encodedStates[0])
                .append("]\n");
        for (State child:childs){
            builder.append("   ".repeat(layer));
            builder.append("").append(child);
        }
        return builder.toString();
    }
}
