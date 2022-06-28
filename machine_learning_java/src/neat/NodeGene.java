package neat;

/**
 * Node Gene or neuron
 * node type    : INPUT/OUTPUT/HIDDEN
 * layer        : 0=INPUT,1,2,3...so on
 * prevValue    : Contain previous value for recurrent
 * currentValue : Contains current value after evaluation
 */
public class NodeGene {
    private final int nodeType;
    private int layer;
    private float prevValue;
    private float currentValue;

    public static int INPUT     = 0;
    public static int OUTPUT    = 1;
    public static int HIDDEN    = 2;

    /////////////////////////////CONSTRUCTORS//////////////////////////////////
    public NodeGene(int nodeType){
        this.nodeType       = nodeType;
        this.layer          = 0;
        this.prevValue      = 0;
        this.currentValue   = 0;
    }

    /////////////////////////////COPY/////////////////////////////////////////
    /**
     * @return  :   copy of this object
     */
    public NodeGene copy(){
        return new NodeGene(nodeType)
                .setLayer(layer)
                .setCurrentValue(currentValue)
                .setPrevValue(prevValue);
    }

    /////////////////////////////SETTERS////////////////////////////////////
    public NodeGene setLayer(int layer) {this.layer = layer;return this;}
    public NodeGene rightShiftLayer(){this.layer++;return this;}
    public NodeGene leftShiftLayer(){this.layer--;return this;}
    public NodeGene setPrevValue(float prevValue) {this.prevValue = prevValue;return this;}
    public NodeGene setCurrentValue(float currentValue) {this.currentValue = currentValue;return this;}
    public NodeGene resetValue(){prevValue=0;currentValue=0;return this;}

    /////////////////////////////GETTERS////////////////////////////////////
    public int getNodeType(){return nodeType;}
    public int getLayer() {return layer;}
    public float getPrevValue() {return prevValue;}
    public float getCurrentValue() {return currentValue;}

}
