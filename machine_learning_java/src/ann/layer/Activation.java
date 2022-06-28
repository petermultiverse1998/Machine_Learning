package ann.layer;

import ann.Layer;

public class Activation extends Layer {
    public Activation(){
        super(0,0);
        name = "ACTIVATION";
    }

    @Override
    public float[][] output(float[]... inputs) {
        inputSize = inputs[0].length;
        outputSize = inputSize;
        //y_i = f(x_i,0)
        x = new float[inputs.length][inputSize];
        float[][] y = new float[inputs.length][outputSize];
        for (int t = 0; t < inputs.length; t++) {
            for (int i = 0; i < inputs[t].length; i++) {
                x[t][i] = inputs[t][i];
                y[t][i] = f(x[t][i]);
            }
        }
        return y;
    }

    //f(x) = 1/(1+exp(-x))
    protected float f(float x){
        return (float) (1/(1+Math.exp(-x)));
    }

    //f'(x) = f(x)*(1-f(x))
    protected float df(float x){
        return f(x)*(1-f(x));
    }

    @Override
    public float[][] gradient(float[]... gradients) {
        //dx_i = dy_i * f'(x)
        float[][] dx = new float[gradients.length][inputSize];
        for (int t = 0; t < gradients.length; t++) {
            for (int i = 0; i < gradients[t].length; i++) {
                dx[t][i] = gradients[t][i]*df(x[t][i]);
            }
        }
        return dx;
    }

    @Override
    public float[] predict(float... inputs) {
        float[] y = new float[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            y[i] = f(inputs[i]);
        }
        return y;
    }


    @Override
    public String toString() {
        return name;
    }
}
