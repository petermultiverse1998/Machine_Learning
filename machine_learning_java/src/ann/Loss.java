package ann;

public class Loss {
    protected String name;
    protected float loss;

    public Loss(){
        name = "LOSS";
        loss = 0;
    }

    public String getName() {
        return name;
    }

    public float getLoss() {
        return loss;
    }

    public float[][] gradient(float[][]inputs, float[][]reference){
        loss = 0;
        float[][] g =new float[inputs.length][];
        for (int t = 0; t < inputs.length; t++) {
            g[t] = new float[inputs[t].length];
            for (int i = 0; i < inputs[t].length; i++) {
                g[t][i] = inputs[t][i]-reference[t][i];
                loss += 0.5*Math.pow(reference[t][i]-inputs[t][i],2);
            }
        }
        loss = loss/g.length;
        return g;
    }

    @Override
    public String toString() {
        return name;
    }

}
