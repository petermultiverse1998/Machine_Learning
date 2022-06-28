package ann.layer.activation;

import ann.layer.Activation;

public class Sigmoid extends Activation {
    public Sigmoid() {
        super();
        name = "SIGMOID";
    }

    //f(x) = 1/(1+exp(-x))
    @Override
    protected float f(float x){
        return (float) (1/(1+Math.exp(-x)));
    }

    @Override
    //f'(x) = f(x)*(1-f(x))
    protected float df(float x){
        return f(x)*(1-f(x));
    }
}
