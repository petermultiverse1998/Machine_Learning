package ann.layer.activation;

import ann.layer.Activation;

public class Softmax extends Activation {
    private float[][] y;

    public Softmax() {
        super();
        name = "SOFTMAX";
    }

    @Override
    public float[][] output(float[]... inputs) {
        inputSize = inputs[0].length;
        outputSize = inputSize;
        y = new float[inputs.length][inputSize];
        //y_i = exp(x_i)/sum_j(x_j)
        for (int t = 0; t < inputs.length; t++) {
            float sum = 0;
            for (int i = 0; i < inputSize; i++)
                sum += Math.exp(inputs[t][i]);
            for (int i = 0; i < inputSize; i++) {
                y[t][i]= (float) (Math.exp(inputs[t][i])/sum);
            }
        }
        return y;
    }
    @Override
    public float[][] gradient(float[]... gradients) {
        //dx_i = sum_j(dy_j*y_j*(del_ji-y_i))
        //del_ji  = 1 for i==j
        //del_ji  = 0 for i!=j
        float[][] dx = new float[gradients.length][inputSize];
        for (int t = 0; t < gradients.length; t++) {
            for (int i = 0; i < inputSize; i++) {
                dx[t][i] = 0;
                for (int j = 0; j < inputSize; j++) {
                    dx[t][i] += gradients[t][j]*y[t][j]*(del(j,i)-y[t][i]);
                }
            }
        }
        return dx;
    }

    private float del(int i,int j){
        return i==j?1:0;
    }

    @Override
    public float[] predict(float... inputs) {
        float[] y = new float[inputs.length];
        //y_i = exp(x_i)/sum_j(x_j)
        float sum = 0;
        for (int i = 0; i < inputs.length; i++)
            sum += Math.exp(inputs[i]);
        for (int i = 0; i < inputs.length; i++) {
            y[i]= (float) (Math.exp(inputs[i])/sum);
        }
        return y;
    }
}
