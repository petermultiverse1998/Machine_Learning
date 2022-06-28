package graph;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public Main(){
        Graph graph = new Graph();
        List<Float> y = new ArrayList<>();
        for (int i = -360; i <= 360; i++) {
//            y.add((float) i*i);
            //graph.plotXY((float) i, (float) Math.sin(i*Math.PI/180));
            //graph.plotXY((float) i, i*i);
            graph.plotXY((float) i, Math.min(i,100));

//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }
//        System.out.println(y);
//        graph.plotY(y);
    }
}
