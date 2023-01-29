package minimax;

import tools.EncoderDecoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Main {
    record StateHolder(int[] board) implements Serializable{}

    public static void main(String[] args){
        int[] board = new int[]{0,0,0,0};

        MiniMax miniMax = new MiniMax(EncoderDecoder.encode(new StateHolder(board)));
        miniMax.setAvailableActions(currentEncodedState -> {
            StateHolder stateHolder =(StateHolder) EncoderDecoder.decode(currentEncodedState);
            if(stateHolder.board[3]==1)
                return new ArrayList<>();

            List<Integer> actions = new ArrayList<>();
            for (int i = 0; i < stateHolder.board.length; i++)
                if(stateHolder.board[i]==0)
                    actions.add(i);
            return actions;
        });
        miniMax.setUpdateState((currentEncodedState,action) -> {
            StateHolder stateHolder =(StateHolder) EncoderDecoder.decode(currentEncodedState);
            stateHolder.board[action] = 1;
            return EncoderDecoder.encode(stateHolder);
        });
        miniMax.setUpdateValue(currentEncodedState -> {
            StateHolder stateHolder =(StateHolder) EncoderDecoder.decode(currentEncodedState);

            float score = 0;
            if(stateHolder.board[3]==1){
                for(float f: stateHolder.board)
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
