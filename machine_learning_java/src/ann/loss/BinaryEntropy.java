package ann.loss;

import ann.Loss;

public class BinaryEntropy extends Loss {
    public BinaryEntropy() {
        super();
        name = "BINARY_CROSS_ENTROPY";
    }

    @Override
    public float[][] gradient(float[][] inputs, float[][] reference) {
        //Loss -= ref*ln(y)+(1-ref)*ln(1-y)
        //dy = -(ref/y)+(1-ref)/(1-y)
        float[][] dy =new float[inputs.length][];
        loss = 0;
        for (int t = 0; t < inputs.length; t++) {
            dy[t] = new float[inputs[t].length];
            for (int i = 0; i < inputs[t].length; i++) {
                //System.out.println("*"+inputs[t][i]);
                if(reference[t][i]>0.5f) {
                    dy[t][i] = -1/inputs[t][i];
                    loss += Math.log(inputs[t][i]);
                }else{
                    dy[t][i] =1/(1-inputs[t][i]);
                    loss += Math.log(1-inputs[t][i]);
                }
            }
        }
        loss = -loss/dy.length;
        return dy;
    }
}
