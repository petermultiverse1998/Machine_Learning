package ann.layer.hidden;

import ann.layer.Hidden;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class HiddenAdam extends Hidden {
    protected float[][] mw;//for weight momentum
    protected float[] mb;//for biases momentum
    protected float[][] vw;//for weight rms
    protected float[] vb;//for biases rms

    protected float momentum    = 0.9f;//beta1
    protected float rms         = 0.999f;//beta2
    protected long epoch         = 0;

    protected static float EPSILON = 1e-8f;

    public HiddenAdam(int size) {
        super(size);
        mw = new float[outputSize][inputSize];
        mb = new float[outputSize];
        vw = new float[outputSize][inputSize];
        vb = new float[outputSize];
        epoch = 0;
        name = "HIDDEN_ADAM";
        learningRate = 0.001f;
    }

    public HiddenAdam(int inputSize, int outputSize) {
        super(inputSize, outputSize);
        mw = new float[outputSize][inputSize];
        mb = new float[outputSize];
        vw = new float[outputSize][inputSize];
        vb = new float[outputSize];
        epoch = 0;
        name = "HIDDEN_ADAM";
        learningRate = 0.001f;
    }

    public HiddenAdam(String filename) {
        super(filename);
        mw = new float[outputSize][inputSize];
        mb = new float[outputSize];
        vw = new float[outputSize][inputSize];
        vb = new float[outputSize];
        epoch = 0;
        name = "HIDDEN_ADAM";
        learningRate = 0.001f;
    }

    public HiddenAdam setMomentum(float momentum) {
        this.momentum = momentum;
        return this;
    }

    public HiddenAdam setRms(float rms) {
        this.rms = rms;
        return this;
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
        //m = beta1*m + (1-beta1) * d
        //v = beta2*v + (1-beta2) * d^2
        //M = m/(1-beta1^t)
        //V = v/(1-beta2^t)
        //theta -=learning*M/sqrt((V)+epsilon)
        epoch++;
        for (int j = 0; j < outputSize; j++) {
            for (int i = 0; i < inputSize; i++) {
                mw[j][i] = momentum*mw[j][i] + (1-momentum)*dw[j][i]/ gradients.length;
                vw[j][i] = rms*vw[j][i] + (1-rms)*(dw[j][i]*dw[j][i])/ gradients.length;
                float Mw = mw[j][i]/(1- (float)Math.pow(momentum,epoch));
                float Vw = vw[j][i]/(1- (float)Math.pow(rms,epoch));
                w[j][i] -=learningRate * Mw/ ( (float)Math.sqrt(Vw)+EPSILON);
            }
            mb[j] = momentum*mb[j] + (1-momentum)*db[j]/ gradients.length;
            vb[j] = rms*vb[j] + (1-rms)*(db[j]*db[j])/ gradients.length;
            float Mb = mb[j]/(1- (float)Math.pow(momentum,epoch));
            float Vb = vb[j]/(1- (float)Math.pow(rms,epoch));
            b[j] -=learningRate * Mb/( (float)Math.sqrt(Vb)+EPSILON);
        }

        return dx;
    }

    @Override
    public void save(String filename){
        try {
            FileWriter writer = new FileWriter(filename+".layer");

            writer.write(inputSize+"\n");
            writer.write(outputSize+"\n");
            writer.write(name+"\n");
            writer.write(learningRate+"\n");
            writer.write(momentum+"\n");
            writer.write(rms+"\n");

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
            momentum = reader.nextFloat();
            rms = reader.nextFloat();

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
