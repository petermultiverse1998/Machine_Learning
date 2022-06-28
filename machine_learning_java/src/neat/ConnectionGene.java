package neat;

/**
 * Connection Gene or Axon
 * fromNode     : node index of source
 * toNode       : node index of destination
 * weight       : weight of connection
 * state        : state of connection
 * recurrent    : is connection recurrent or not
 */
public class ConnectionGene{
    private final int fromNode;
    private final int toNode;
    private float weight;
    private boolean state;
    private boolean recurrent;

    /////////////////////////////CONSTRUCTORS//////////////////////////////////
    public ConnectionGene(int fromNode,int toNode,float weight,boolean state,boolean recurrent){
        this.fromNode   = fromNode;
        this.toNode     = toNode;
        this.weight     = weight;
        this.state      = state;
        this.recurrent  = recurrent;
    }

    /////////////////////////////COPY/////////////////////////////////////////
    /**
     * @return  :   copy of this object
     */
    public ConnectionGene copy(){
        return new ConnectionGene(fromNode,toNode,weight,state,recurrent);
    }

    /////////////////////////////SETTERS///////////////////////////////
    public ConnectionGene setWeight(float weight){this.weight=weight;return this;}
    public ConnectionGene setState(boolean state){this.state=state;return this;}
    public ConnectionGene setRecurrent(boolean recurrent){this.recurrent=recurrent;return this;}

    /////////////////////////////GETTERS///////////////////////////////
    public int getFromNode() {return fromNode;}
    public int getToNode() {return toNode;}
    public float getWeight() {return weight;}
    public boolean getState(){return state;}
    public boolean isRecurrent(){return recurrent;}

}
