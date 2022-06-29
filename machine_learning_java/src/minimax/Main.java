package minimax;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args){
        MiniMax miniMax = new MiniMax(0,0,0,0);
        miniMax.setAvailableActions(currentEncodedState -> {
            if(currentEncodedState[3]==1)
                return new ArrayList<>();

            List<Integer> actions = new ArrayList<>();
            for (int i = 0; i < currentEncodedState.length; i++)
                if(currentEncodedState[i]==0)
                    actions.add(i);
            return actions;
        });
        miniMax.setUpdateState((currentEncodedState,action) -> {
            float[] newEncodedState = new float[currentEncodedState.length];
            System.arraycopy(currentEncodedState,0,newEncodedState,0,newEncodedState.length);
            newEncodedState[action] = 1;
            return newEncodedState;
        });
        miniMax.setUpdateValue(currentEncodedState -> {
            float score = 0;
            if(currentEncodedState[3]==1){
                for(float f:currentEncodedState)
                    if(f==0)
                        score++;
            }
            return score;
        });
        miniMax.setMaximizingConditions(currentEncodedState -> true);

        int bestAction = miniMax.bestAction();
        System.out.println(bestAction);
    }
}
