package ann;

import ann.layer.Activation;
import ann.layer.Hidden;
import ann.layer.activation.Relu;
import ann.layer.activation.Sigmoid;
import ann.layer.activation.Softmax;
import ann.layer.activation.Tanh;
import ann.layer.hidden.HiddenAdam;
import ann.loss.BinaryEntropy;
import ann.loss.Entropy;
import ann.loss.MeanSquare;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Network {
    protected List<Layer> layers;
    protected Loss loss;
    protected Map<FromTo,Layer> connections;
    protected float forceLearningRate=-1;

    public Network(){
        layers = new ArrayList<>();
        loss = new Loss();

        connections = new HashMap<>();
    }

    public Network(String filename){
        loss = new Loss();
        read(filename);
    }

    public Network addLayer(Layer layer){
        layers.add(layer);
        return this;
    }

    public Network addLayers(Layer...layers){
        this.layers.addAll(Arrays.asList(layers));
        return this;
    }

    public Network addConnection(int from,int to,Layer layer){
        connections.put(new FromTo(from,to),layer);
        return this;
    }

    public Network forceLearningRate(float learningRate){
        for(Layer layer:layers)
            if(layer instanceof Hidden)
                ((Hidden) layer).setLearningRate(learningRate);
        for(Layer layer:connections.values())
            if(layer instanceof Hidden)
                ((Hidden) layer).setLearningRate(learningRate);
        forceLearningRate = learningRate;
        return this;
    }

    public Network setLoss(Loss loss){
        this.loss = loss;
        return this;
    }

    public float getLoss() {
        return loss.getLoss();
    }

    //training
    public void train(float[][]x,float[][] y_ref){
        //forward
        float[][] y = x;
        Map<Integer,float[][]> fromY = new HashMap<>();
        for(FromTo fromTo: connections.keySet())
            if(!fromY.containsKey(fromTo.from()))
                fromY.put(fromTo.from(), new float[x.length][connections.get(fromTo).getInputSize()]);
        int l=0;
        for (Layer layer:layers){
            if(fromY.containsKey(l))
                System.arraycopy(y,0,fromY.get(l),0,y.length);
            y = layer.output(y);
            List<FromTo> fromTos = FromTo.getAllTo(l,connections.keySet().stream().toList());
            for(FromTo fromTo:fromTos)
                y = sum(y,connections.get(fromTo).output(fromY.get(fromTo.from())));
            l++;
        }

        //loss
        float[][] dy = loss.gradient(y,y_ref);

        //backpropagation
        l = layers.size();
        Map<Integer,float[][]> toDy = new HashMap<>();
        for(FromTo fromTo: connections.keySet())
            if(!toDy.containsKey(fromTo.to()))
                toDy.put(fromTo.to(), new float[x.length][connections.get(fromTo).getOutputSize()]);
        Collections.reverse(layers);
        for (Layer layer:layers){
            if(toDy.containsKey(l))
                System.arraycopy(dy,0,toDy.get(l),0,dy.length);
            dy = layer.gradient(dy);
            List<FromTo> fromTos = FromTo.getAllFrom(l,connections.keySet().stream().toList());
            for(FromTo fromTo:fromTos)
                dy = sum(dy,connections.get(fromTo).gradient(toDy.get(fromTo.to)));
            l--;
        }
        Collections.reverse(layers);
    }
    private static float[][] sum(float[][]...ys){
        float[][] y = new float[ys[0].length][ys[0][0].length];
        for (int i = 0; i < y.length; i++)
            for (int j = 0; j < y[0].length; j++)
                for (float[][] floats : ys) y[i][j] += floats[i][j];
        return y;
    }

    //predict
    public float[] predict(float...x){
        float[] y = x;
        Map<Integer,float[]> fromY = new HashMap<>();
        for(FromTo fromTo: connections.keySet())
            if(!fromY.containsKey(fromTo.from()))
                fromY.put(fromTo.from(), new float[connections.get(fromTo).getInputSize()]);
        int l=0;
        for (Layer layer:layers){
            if(fromY.containsKey(l))
                System.arraycopy(y,0,fromY.get(l),0,y.length);
            y = layer.predict(y);
            List<FromTo> fromTos = FromTo.getAllTo(l,connections.keySet().stream().toList());
            for(FromTo fromTo:fromTos)
                y = sum(y,connections.get(fromTo).predict(fromY.get(fromTo.from())));
            l++;
        }
        return y;
    }
    private static float[] sum(float[]...ys){
        float[] y = new float[ys[0].length];
        for (int i = 0; i < y.length; i++)
            for (float[] floats : ys) y[i] += floats[i];
        return y;
    }

    public void save(String filename){
        try {
            FileWriter writer = new FileWriter(filename+".ann");
            writer.write(layers.size()+"\n");
            for(Layer layer:layers) {
                writer.write(layer.getName() + "\n");
                layer.save(filename+"_"+layers.indexOf(layer));
            }
            writer.write(connections.size()+"\n");
            for(FromTo fromTo: connections.keySet()) {
                Layer layer = connections.get(fromTo);
                writer.write(layer.getName() + " ");
                writer.write(fromTo.from + " ");
                writer.write(fromTo.to + "\n");
                layer.save(filename+"_conn_"+connections.keySet().stream().toList().indexOf(fromTo));
            }
            writer.write(loss.getName()+"\n");
            if(forceLearningRate>0)
                writer.write(forceLearningRate+"\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read(String filename){
        try {
            Scanner reader = new Scanner(new File(filename+".ann"));
            int size = reader.nextInt();
            layers = new ArrayList<>(size);
            for (int i = 0; i < size; i++){
                String name = reader.next();
                switch (name.trim().toUpperCase()) {
                    case "LAYER" -> layers.add(new Layer(filename + "_" + i));
                        case "HIDDEN" -> layers.add(new Hidden(filename + "_" + i));
                            case "HIDDEN_ADAM" -> layers.add(new HiddenAdam(filename + "_" + i));
                    case "ACTIVATION" -> layers.add(new Activation());
                        case "RELU" -> layers.add(new Relu());
                        case "SIGMOID" -> layers.add(new Sigmoid());
                        case "SOFTMAX" -> layers.add(new Softmax());
                        case "TANH" -> layers.add(new Tanh());
                }
            }
            connections = new HashMap<>();
            if(reader.hasNextInt()){
                size = reader.nextInt();
                for (int i = 0; i < size; i++){
                    String name = reader.next();
                    int from = reader.nextInt();
                    int to = reader.nextInt();
                    switch (name.trim().toUpperCase()) {
                        case "LAYER" -> connections.put(new FromTo(from,to),new Layer(filename + "_conn_" + i));
                            case "HIDDEN" -> connections.put(new FromTo(from,to),new Hidden(filename + "_conn_" + i));
                                case "HIDDEN_ADAM" -> connections.put(new FromTo(from,to),new HiddenAdam(filename + "_conn_" + i));
                        case "ACTIVATION" -> connections.put(new FromTo(from,to),new Activation());
                            case "RELU" -> connections.put(new FromTo(from,to),new Relu());
                            case "SIGMOID" -> connections.put(new FromTo(from,to),new Sigmoid());
                            case "SOFTMAX" -> connections.put(new FromTo(from,to),new Softmax());
                            case "TANH" -> connections.put(new FromTo(from,to),new Tanh());
                    }
                }
            }
            String lossName = reader.next();
            switch (lossName.trim().toUpperCase()) {
                case "LOSS" -> loss = new Loss();
                    case "MEAN_SQUARE" -> loss = new MeanSquare();
                    case "BINARY_CROSS_ENTROPY" -> loss = new BinaryEntropy();
                    case "CATEGORICAL_CROSS_ENTROPY" -> loss = new Entropy();
            }
            if(reader.hasNextFloat()){
                float learningRate = reader.nextFloat();
                if(learningRate>0)
                    forceLearningRate(learningRate);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(Layer layer:layers)
            builder.append(layer).append("\n");
        for(FromTo fromTo: connections.keySet())
            builder.append(connections.get(fromTo))
                .append("(").append(fromTo.from).append("=>").append(fromTo.to).append(")")
                    .append("\n");

        builder.append(loss);
        return builder.toString();
    }

    private static record FromTo(int from,int to){
        @Override
        public boolean equals(Object obj) {
            FromTo fromTo = (FromTo) obj;
            return from==fromTo.from && to==fromTo.to;
        }

        public static List<FromTo> getAllFrom(int from,List<FromTo> fromTos){
            return fromTos.stream().filter(fromTo -> fromTo.from==from).toList();
        }

        public static List<FromTo> getAllTo(int to,List<FromTo> fromTos){
            return fromTos.stream().filter(fromTo -> fromTo.to==to).toList();
        }

        public static List<Integer> getAllFromInAscendingOrder(List<FromTo> fromTos){
            List<Integer> froms = new ArrayList<>();
            fromTos.forEach(fromTo -> {if(!froms.contains(fromTo.from))froms.add(fromTo.from);});
            return froms;
        }
    }

}
