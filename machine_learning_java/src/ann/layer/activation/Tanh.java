package ann.layer.activation;

import ann.layer.Activation;

public class Tanh extends Activation {
    public Tanh() {
        super();
        name = "TANH";
    }

    //f(x) = 1/(1+exp(-x))
    @Override
    protected float f(float x){
        return (float) (Math.tanh(x));
    }

    @Override
    //f'(x) = f(x)*(1-f(x))
    protected float df(float x){
        return (float) (1-Math.pow(f(x),2));
    }
}
