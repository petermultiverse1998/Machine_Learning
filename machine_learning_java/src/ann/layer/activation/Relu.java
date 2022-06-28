package ann.layer.activation;

import ann.layer.Activation;

public class Relu extends Activation {
    public Relu() {
        super();
        name = "RELU";
    }

    //f(x) = max(x,0)
    @Override
    protected float f(float x) {
        return Math.max(x,0);
    }

    //f'(x) = 1 for x>0
    //f'(x) = 0 for x<0
    //f'(x) = undefined for x=0
    @Override
    protected float df(float x) {
        if(x>0)
            return 1;
        else
            return 0;
    }
}
