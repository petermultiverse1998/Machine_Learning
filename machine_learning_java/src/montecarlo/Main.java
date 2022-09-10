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

//        Neat neat = new Neat();
//        NeatGraphics graphics = new NeatGraphics();
//        Genome fittest = null;
//        for (int i = 0; i < 100; i++) {
//            neat.evolve();
//            fittest = neat.fittestGenome();
//            graphics.draw(fittest);
//            Thread.sleep(10);
//        }
//
//        for (int i = 0; i < 10; i++) {
//            System.out.println(i+" : "+fittest.forward(1,i)[0]);
//        }

    }

    static class Neat extends NEAT{
        public Neat() {
            super(1000, 2, 1, 10, true, false, true);
        }

        @Override
        public float fitness(Genome genome) {
            float error = 0;
            for (int i = 0; i < 10; i++) {
                float f = genome.forward(1,i)[0];
                error += (3*i+1-f) *(3*i+1-f);
            }
            return -error;
        }
    }
}
