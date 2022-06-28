package ann.loss;

import ann.Loss;

public class MeanSquare extends Loss {

    public MeanSquare(){
        name = "MEAN_SQUARE";
        loss = 0;
    }

    @Override
    public float[][] gradient(float[][] inputs, float[][] reference) {
        //Loss = 0.5 * (reference-inputs)^2
        loss = 0;
        float[][] dy =new float[inputs.length][];
        for (int t = 0; t < inputs.length; t++) {
            dy[t] = new float[inputs[t].length];
            for (int i = 0; i < inputs[t].length; i++) {
                dy[t][i] = inputs[t][i]-reference[t][i];
                loss += 0.5*Math.pow(reference[t][i]-inputs[t][i],2);
            }
        }
        loss = loss/dy.length;
        return dy;
    }
}
