package montecarlo;

import neat.Genome;
import neat.NEAT;
import neat.NeatGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        MonteCarlo monteCarlo = new MonteCarlo(0,0,0,0);
        monteCarlo.setAvailableActions(currentEncodedState -> {
            if(currentEncodedState[3]==1)
                return new ArrayList<>();

            List<Integer> actions = new ArrayList<>();
            for (int i = 0; i < currentEncodedState.length; i++)
                if(currentEncodedState[i]==0)
                    actions.add(i);
            return actions;
        });
        monteCarlo.setUpdateState((currentEncodedState,action,isRollOut) -> {
            float[] newEncodedState = new float[currentEncodedState.length];
            System.arraycopy(currentEncodedState,0,newEncodedState,0,currentEncodedState.length);
            newEncodedState[action] = 1;
            return newEncodedState;
        });
        monteCarlo.setRollOutAction(currentEncodedState -> {
            List<Integer> actions = new ArrayList<>();
            for (int i = 0; i < currentEncodedState.length; i++)
                if(currentEncodedState[i]==0)
                    actions.add(i);
            return actions.get(new Random().nextInt(actions.size()));
        });
        monteCarlo.setUpdateValue(currentEncodedState -> {
            float score = 0;
            if(currentEncodedState[3]==1){
                for(float f:currentEncodedState)
                    if(f==0)
                        score++;
            }
            return score;
        });
        monteCarlo.setMaximizingConditions(currentEncodedState -> true);


        int bestAction = monteCarlo.bestAction(100);
        System.out.println(monteCarlo);
        System.out.println(bestAction);

    }

}
