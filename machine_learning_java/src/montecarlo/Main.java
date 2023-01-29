package montecarlo;

import tools.EncoderDecoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    record StateHolder(int[] board) implements Serializable{}

    public static void main(String[] args) throws InterruptedException {
        int[] board = new int[]{0,0,0,0};

        MonteCarlo monteCarlo = new MonteCarlo(EncoderDecoder.encode(new StateHolder(board)));
        monteCarlo.setAvailableActions(currentEncodedState -> {
            StateHolder stateHolder =(StateHolder) EncoderDecoder.decode(currentEncodedState);
            if(stateHolder.board[3]==1)
                return new ArrayList<>();

            List<Integer> actions = new ArrayList<>();
            for (int i = 0; i < stateHolder.board.length; i++)
                if(stateHolder.board[i]==0)
                    actions.add(i);
            return actions;
        });
        monteCarlo.setUpdateState((currentEncodedState,action,isRollOut) -> {
            StateHolder stateHolder =(StateHolder) EncoderDecoder.decode(currentEncodedState);
            stateHolder.board[action] = 1;
            return EncoderDecoder.encode(stateHolder);
        });
        monteCarlo.setRollOutAction(currentEncodedState -> {
            StateHolder stateHolder =(StateHolder) EncoderDecoder.decode(currentEncodedState);
            List<Integer> actions = new ArrayList<>();
            for (int i = 0; i < stateHolder.board.length; i++)
                if(stateHolder.board[i]==0)
                    actions.add(i);
            return actions.get(new Random().nextInt(actions.size()));
        });
        monteCarlo.setUpdateValue(currentEncodedState -> {
            StateHolder stateHolder =(StateHolder) EncoderDecoder.decode(currentEncodedState);
            float score = 0;
            if(stateHolder.board[3]==1){
                for(float f:stateHolder.board)
                    if(f==0)
                        score++;
            }
            return score;
        });
        monteCarlo.setMaximizingConditions(currentEncodedState -> true);


        int bestAction = monteCarlo.bestAction(1000);
        System.out.println(monteCarlo);
        System.out.println(bestAction);

    }

}
