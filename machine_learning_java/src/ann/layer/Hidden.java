package ann.layer;

import ann.Layer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Hidden extends Layer {
    protected float[][] w;//weights
    protected float[] b;//biases

    protected float learningRate;

    public Hidden(int size) {
        super(size);
        inputSize = size;
        outputSize = size;
        name = "HIDDEN";
        learningRate = 0.01f;

        w = new float[outputSize][inputSize];
        b = new float[outputSize];

        //random weights and biases
        for (int j = 0; j < outputSize; j++) {
            for (int i = 0; i < inputSize; i++) {
                w[j][i] = (float) random.nextFloat(-2,2);
            }
            b[j] = (float) 0;
        }
    }

    public Hidden(int inputSize, int outputSize) {
        super(inputSize, outputSize);
        name = "HIDDEN";
        learningRate = 0.01f;

        w = new float[outputSize][inputSize];
        b = new float[outputSize];

        //random weights and biases
        for (int j = 0; j < outputSize; j++) {
            for (int i = 0; i < inputSize; i++) {
                w[j][i] = (float) random.nextFloat(-2,2);
            }
            b[j] = (float) 0;
        }
    }

    public Hidden(String filename) {
        super(filename);
        name = "HIDDEN";
    }

    public Hidden setLearningRate(float learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    @Override
    public float[][] output(float[]... inputs) {
        //y_j = sum_i(w_ji*x_i)+b_j
        x = new float[inputs.length][inputSize];
        float[][] y = new float[inputs.length][outputSize];
        for (int t = 0; t < inputs.length; t++) {
            for (int j = 0; j < outputSize; j++) {
                y[t][j] = 0;
                for (int i = 0; i < inputSize; i++) {
                    x[t][i] = inputs[t][i];
                    y[t][j] += w[j][i]*x[t][i];
                }
                y[t][j] += b[j];
            }
        }
        return y;
    }

    @Override
    public float[][] gradient(float[]... gradients) {
        //dw_ji -= dy_j*x_i
        //db_j -= dy_j;

        float[][] dw = new float[outputSize][inputSize];
        float[] db = new float[outputSize];
        for (int t = 0; t < gradients.length; t++) {
            for (int j = 0; j < outputSize; j++) {
                for (int i = 0; i < inputSize; i++) {
                    dw[j][i] +=gradients[t][j]*x[t][i];
                }
                db[j] +=gradients[t][j];
            }
        }

        //dx_i = sum_j(dy_j*w_ji)
        float[][] dx = new float[gradients.length][inputSize];
        for (int t = 0; t < gradients.length; t++) {
            for (int i = 0; i < inputSize; i++) {
                dx[t][i] = 0;
                for (int j = 0; j < outputSize; j++) {
                    dx[t][i] += gradients[t][j]*w[j][i];
                }
            }
        }

        //Update
        //w_ji -=learning * dw_ji
        //b_j -=learning * db_j
        for (int j = 0; j < outputSize; j++) {
            for (int i = 0; i < inputSize; i++) {
                w[j][i] -=learningRate * dw[j][i]/ gradients.length;
            }
            b[j] -= learningRate * db[j]/ gradients.length;
        }

        return dx;
    }

    @Override
    public float[] predict(float... inputs) {
        //y_j = sum_i(w_ji*x_i)+b_j
        float[] y = new float[outputSize];
        for (int j = 0; j < outputSize; j++) {
            y[j] = 0;
            for (int i = 0; i < inputSize; i++) {
                y[j] += w[j][i]*inputs[i];
            }
            y[j] += b[j];
        }
        return y;
    }

    @Override
    public void save(String filename){
        try {
            FileWriter writer = new FileWriter(filename+".layer");

            writer.write(inputSize+"\n");
            writer.write(outputSize+"\n");
            writer.write(name+"\n");
            writer.write(learningRate+"\n");

            for (int j = 0; j < outputSize; j++) {
                for (int i = 0; i < inputSize; i++) {
                    writer.write( w[j][i]+"\n");
                }
                writer.write(b[j]+"\n");
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void read(String filename){
        try {
            Scanner reader = new Scanner(new File(filename+".layer"));

            inputSize = reader.nextInt();
            outputSize = reader.nextInt();
            name = reader.next();
            learningRate = reader.nextFloat();

            w = new float[outputSize][inputSize];
            b = new float[outputSize];
            for (int j = 0; j < outputSize; j++) {
                for (int i = 0; i < inputSize; i++) {
                    w[j][i] = reader.nextFloat();
                }
                b[j] = reader.nextFloat();
            }

            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
