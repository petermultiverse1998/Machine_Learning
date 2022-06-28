package neat;

import java.util.Arrays;
import java.util.HashMap;

public class GlobalInnovationNumbers {
    /////////////////////////////PRIVATE VARIABLES///////////////////////////////
    private final HashMap<Integer,int[]> innovationNumbers;   //innovation_number->[from_node,to_node]

    /////////////////////////////CONSTRUCTORS///////////////////////////////////
    public GlobalInnovationNumbers(){
        innovationNumbers = new HashMap<>();
    }

    ////////////////////////////GETTER//////////////////////////////////////////
    /**
     * @param from  :   from node
     * @param to    :   to node
     * @return      : return new innovation number if there is no existing innovation number
     */
    public int getInnovationNumber(int from,int to){
        for (int innovationNumber: innovationNumbers.keySet()) {
            int[] n = innovationNumbers.get(innovationNumber);
            if (n[0] == from && n[1] == to)
                return innovationNumber;
        }
        int innovationNumber = innovationNumbers.size()+1;
        innovationNumbers.put(innovationNumber,new int[]{from,to});
        return innovationNumber;
    }

    /**
     * @return  :   (reference) innovation numberse
     */
    public HashMap<Integer,int[]> getInnovationNumbers(){
        return innovationNumbers;
    }

    ////////////////////////////TO STRING///////////////////////////////////////
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 1; i <= innovationNumbers.size(); i++) {
            str.append(i).append("->").append(Arrays.toString(innovationNumbers.get(i))).append("\n");
        }
        return  str.toString();
    }
}
