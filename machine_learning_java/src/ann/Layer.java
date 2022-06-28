package ann;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Layer {
    protected float[][] x;
    protected int inputSize;
    protected int outputSize;
    protected String name;

    protected static Random random = new Random();

    public  Layer(int size){
        this.inputSize = size;
        this.outputSize = size;
        name = "LAYER";
    }

    public Layer(int inputSize,int outputSize){
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        name = "LAYER";
    }

    public Layer(String filename){
        name = "LAYER";
        read(filename);
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public String getName() {
        return name;
    }

    public float[][] output(float[]...inputs){
        x = new float[inputs.length][inputSize];
        for (int t = 0; t < x.length; t++) {
            System.arraycopy(inputs[t], 0, x[t], 0, x[t].length);
        }
        return x;
    }

    public float[][] gradient(float[]...gradients){
        return gradients;
    }

    public float[] predict(float...inputs){
        return inputs;
    }

    public void save(String filename){
        try {
            FileWriter writer = new FileWriter(filename+".layer");

            writer.write(inputSize+"\n");
            writer.write(outputSize+"\n");
            writer.write(name+"\n");

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read(String filename){
        try {
            Scanner reader = new Scanner(new File(filename+".layer"));

            inputSize = reader.nextInt();
            outputSize = reader.nextInt();
            name = reader.next();

            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return name + "[" + inputSize + "," + outputSize + "]";
    }




}
