package ann;
import ann.layer.Hidden;
import ann.layer.activation.*;
import ann.layer.hidden.*;
import ann.loss.*;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
//        float[][] x = {{1,3},{2,4},{3,5},{4,3},{5,7},{6,8},{7,9},{8,10},{9,11}};
//        float[][] y = {{4},{6},{8},{10},{7},{14},{16},{18},{20}};
//        float[][] y = {{0},{0},{0},{1},{1},{0},{1},{1},{1}};
//        float[][] y = {{0,1},{0,1},{0,1},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0}};
//        float[][] y = {{1},{2},{3},{4},{5},{6},{7},{8},{9}};

        float[][] x = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        float[][] y = {{0}, {1}, {1}, {0}};

        Network network = new Network()
                .addLayer(new Hidden(2,2).setLearningRate(0.01f))
                .addLayer(new Sigmoid())
                .addLayer(new Hidden(2,1).setLearningRate(0.02f))
                .addLayer(new Sigmoid())
//                .addConnection(0,2,new Hidden(2,2))
                //.forceLearningRate(0.1f)
                .setLoss(new MeanSquare());
//
        for (int epoch = 0; epoch < 100000; epoch++) {
            network.train(x,y);
            System.out.println("epoch "+(epoch+1)+":"+network.getLoss());
        }
//
//        network.save("data/ann");
//////
//        network = new Network("data/ann");
        System.out.println(network);
//
        System.out.println(Arrays.toString(network.predict(0,0)));
        System.out.println(Arrays.toString(network.predict(1,0)));
        System.out.println(Arrays.toString(network.predict(1,1)));
        System.out.println(Arrays.toString(network.predict(0,1)));

//


    }


}
