package neat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Genome or neural network architecture
 * nodeGenes            : hashmap of all node genes
 * connection           : hashmap of all connection genes
 * fitness              : fitness of genome
 * age                  : age of genome (or count of generation it is survived)
 * isActivationInOutput : is activation allowed in output layer or not
 * isRelu               : is Relu allowed to use
 * isRecurrent          : is recurrent enable
 */
public class Genome {
    private final HashMap<Integer,NodeGene> nodeGenes;
    private final HashMap<Integer,ConnectionGene> connectionGenes;

    private int numberOfInputs;
    private int numberOfOutputs;
    private float fitness;
    private int age;
    public boolean isActivationInOutput;
    public boolean isRecurrent;
    public boolean isRelu;

    public static int UPPER_BOUND                   = 2;            //upper bound for random generation
    public static int LOWER_ORIGIN                  = -2;           //lower bound for random generation
    public static final Random random               = new Random(); //random generator
    public static int MUTATE_CONNECTION_MAX_LOOP    = 10;           //maximum loop that is allowed
    private static int mutate_connection_loop       = 0;           //loop records loop in mutation

    ////////////////////////////////ACTIVATION///////////////////////////////
    private float activation(float x){
        if (isRelu)
            return x>=0?x:0;
        return (float) (1f/(1f+Math.exp(-4.9*x)));
    }

    private float generateRandom(){
        //return random.nextFloat(LOWER_ORIGIN,UPPER_BOUND);
        return (float) random.nextGaussian(1,1);
    }

    ////////////////////////////////CONSTRUCTORS//////////////////////////////////
    public Genome(String filename){
        nodeGenes       = new HashMap<>();
        connectionGenes = new HashMap<>();
        read(filename);
    }

    public Genome(){
        nodeGenes               = new HashMap<>();
        connectionGenes         = new HashMap<>();

        fitness                 = 0;
        age                     = 0;
        isRelu                  = false;
        isActivationInOutput    = true;
    }

    public Genome(int numberOfInputs, int numberOfOutputs, GlobalInnovationNumbers globalInnovationNumbers,boolean isActivationInOutput,boolean isRecurrent,boolean isRelu){
        nodeGenes                   = new HashMap<>();
        connectionGenes             = new HashMap<>();
        init(numberOfInputs,numberOfOutputs,globalInnovationNumbers,isActivationInOutput,isRecurrent,isRelu);
    }

    public void init(int numberOfInputs, int numberOfOutputs, GlobalInnovationNumbers globalInnovationNumbers,boolean isActivationInOutput,boolean isRecurrent,boolean isRelu){
        this.numberOfInputs         = numberOfInputs;
        this.numberOfOutputs        = numberOfOutputs;
        this.fitness                = 0;
        this.age                    = 0;
        this.isActivationInOutput   = isActivationInOutput;
        this.isRecurrent            = isRecurrent;
        this.isRelu                 = isRelu;

        //for input node genes
        for (int i = 1; i <= numberOfInputs; i++)
            nodeGenes.put(i,new NodeGene(NodeGene.INPUT).setLayer(1));

        //for output node genes
        for (int i = 1+numberOfInputs; i <= numberOfInputs+numberOfOutputs; i++)
            nodeGenes.put(i, new NodeGene(NodeGene.OUTPUT).setLayer(2));

        //for connection genes
        int from = random.nextInt(1,numberOfInputs+1);
        int to = random.nextInt(numberOfInputs+1,numberOfInputs+numberOfOutputs+1);
        int innovationNumber = globalInnovationNumbers.getInnovationNumber(from,to);
        connectionGenes.put(innovationNumber,
                new ConnectionGene(from,to,generateRandom(),
                        true,false));
    }

    /////////////////////////////COPY/////////////////////////////////////////
    /**
     * @return copy of genome
     */
    public Genome copy(){
        Genome genome = new Genome();
        for(int node:nodeGenes.keySet())
            genome.nodeGenes.put(node,nodeGenes.get(node).copy());
        for (int innovationNumber: connectionGenes.keySet())
            genome.connectionGenes.put(innovationNumber,connectionGenes.get(innovationNumber).copy());
        genome.numberOfInputs       = numberOfInputs;
        genome.numberOfOutputs      = numberOfOutputs;
        genome.fitness              = fitness;
        genome.age                  = age;
        genome.isActivationInOutput = isActivationInOutput;
        genome.isRecurrent          = isRecurrent;
        genome.isRelu               = isRelu;
        return genome;
    }

    ////////////////////////////CROSSOVER AND MUTATION////////////////////////////
    /**
     * @param genome1   :   parent1
     * @param genome2   :   parent2
     * @return          : child
     */
    public static Genome crossover(Genome genome1, Genome genome2){
        Genome child = new Genome();
        int genome1LargestInnovationNumber  = genome1.getLargestInnovationNumber();
        int genome2LargestInnovationNumber  = genome2.getLargestInnovationNumber();
        int largestInnovationNumber = Math.max(genome1LargestInnovationNumber, genome2LargestInnovationNumber);

        //connection genes and corresponding nodes
        for (int i = 1; i <= largestInnovationNumber; i++) {
            ConnectionGene connectionGene = null;
            int parent = 0;

            //In case of matching connection genes
            if(genome1.connectionGenes.containsKey(i) && genome2.connectionGenes.containsKey(i)){
                //Choose randomly connection gene from either of one
                if(random.nextFloat()<=0.5){
                    connectionGene = genome1.connectionGenes.get(i).copy();
                    parent = 1;
                }else {
                    connectionGene = genome2.connectionGenes.get(i).copy();
                    parent = 2;
                }
                //75% chances inherited gene will be disabled if both parent has disabled genes
                if(genome1.connectionGenes.get(i).getState()
                    && genome2.connectionGenes.get(i).getState()){
                    if(random.nextFloat()<=0.25)
                        connectionGene.setState(true);
                }
            }else if(genome1.connectionGenes.containsKey(i)){
                if(i<genome2LargestInnovationNumber){
                    //In case of disjoint connection genes for genome1
                    if (genome1.fitness>=genome2.fitness){
                        connectionGene = genome1.connectionGenes.get(i).copy();
                        parent = 1;
                    }
                }else {
                    //In case of excess connection genes for genome1
                    if (genome1.fitness>=genome2.fitness){
                        connectionGene = genome1.connectionGenes.get(i).copy();
                        parent = 1;
                    }
                }
            }else if(genome2.connectionGenes.containsKey(i)){
                if(i<genome1LargestInnovationNumber){
                    //In case of disjoint connection genes for genome2
                    if (genome1.fitness<=genome2.fitness){
                        connectionGene = genome2.connectionGenes.get(i).copy();
                        parent = 2;
                    }
                }else {
                    //In case of excess connection genes for genome2
                    if (genome1.fitness<=genome2.fitness){
                        connectionGene = genome2.connectionGenes.get(i).copy();
                        parent = 2;
                    }
                }
            }
            if(connectionGene!=null){
                int from = connectionGene.getFromNode();
                int to   = connectionGene.getToNode();
                child.connectionGenes.put(i,connectionGene);

                if(parent==1){
                    if(!child.nodeGenes.containsKey(from))
                        child.nodeGenes.put(from,genome1.nodeGenes.get(from).copy());
                    if(!child.nodeGenes.containsKey(connectionGene.getToNode()))
                        child.nodeGenes.put(to,genome1.nodeGenes.get(to).copy());
                }else {
                    if(!child.nodeGenes.containsKey(from))
                        child.nodeGenes.put(from,genome2.nodeGenes.get(from).copy());
                    if(!child.nodeGenes.containsKey(to))
                        child.nodeGenes.put(to,genome2.nodeGenes.get(to).copy());
                }
            }

        }

        //If inputs or outputs are missing
        for(int i=1;i<=genome1.numberOfInputs;i++){
            if(!child.nodeGenes.containsKey(i))
                child.nodeGenes.put(i,genome1.nodeGenes.get(i).copy());
        }
        for(int i=1;i<=genome1.numberOfOutputs;i++){
            if(!child.nodeGenes.containsKey(i+genome1.numberOfInputs))
                child.nodeGenes.put(i+genome1.numberOfInputs,genome1.nodeGenes.get(i+genome1.numberOfInputs).copy());
        }

        child.numberOfInputs        = genome1.numberOfInputs;
        child.numberOfOutputs       = genome1.numberOfOutputs;
        child.fitness               = 0;
        child.age                   = 0;
        child.isActivationInOutput  = genome1.isActivationInOutput;
        child.isRecurrent           = genome1.isRecurrent;
        child.isRelu                = genome1.isRelu;

        for(int i=1;i<=child.numberOfInputs;i++){
            child.adjustLayerOfNodesFrom(i);
        }
        child.resetLayerOfInputsAndOutputs();
        child.checkWeightRecurrent();

        return child;
    }

    /**
     * @param genome                    :   parent
     * @param globalInnovationNumbers   :   Historical Marking
     * @return                          :   mutated child
     */
    public static Genome mutateConnection(Genome genome, GlobalInnovationNumbers globalInnovationNumbers){
        Genome child = genome.copy();

        //Randomly choose from node
        Set<Integer> fromSet = genome.nodeGenes.keySet();
        float prob = random.nextFloat();
        float cumulative_prob = 0;
        int from = 0;
        for (int node:fromSet){
            cumulative_prob += 1/(float)fromSet.size();
            if(prob<=cumulative_prob){
                from = node;
                break;
            }
        }

        //Selections of to sets
        Set<Integer> toSet = new HashSet<>();
        for (int node:fromSet){
            if(toSet.contains(node)){
                //if node already exist
                continue;
            }
//            if(genome.nodeGenes.get(node).getNodeType()==NodeGene.INPUT)
//                continue;

            if(genome.nodeGenes.get(node).getLayer()==genome.nodeGenes.get(from).getLayer()){
                //both from same layer
//                if(node!=from)
//                    continue;
                if(!genome.isRecurrent())
                    continue;
            } else if (genome.nodeGenes.get(node).getLayer()<genome.nodeGenes.get(from).getLayer()) {
                //backward
                if(!genome.isRecurrent())
                    continue;
            }

            boolean connection_exist = false;
            for (ConnectionGene connectionGene:genome.connectionGenes.values()){
                if (from == connectionGene.getFromNode() && node == connectionGene.getToNode()) {
                    connection_exist = true;
                    break;
                }
            }
            if(!connection_exist){
                //if connection doesn't exist
                toSet.add(node);
            }
        }


        //if to node is empty repeat everything again
        mutate_connection_loop++;
        if(toSet.isEmpty() && mutate_connection_loop<=MUTATE_CONNECTION_MAX_LOOP)
            return mutateConnection(genome,globalInnovationNumbers);
        mutate_connection_loop = 0;
        if(toSet.isEmpty())
            return null;

        //Randomly choose to node
        prob = random.nextFloat();
        cumulative_prob = 0;
        int to = 0;
        for (int node:toSet){
            cumulative_prob+=1/(float) toSet.size();
            if(prob<=cumulative_prob){
                to = node;
                break;
            }
        }
        boolean recurrent = child.nodeGenes.get(from).getLayer()>=child.nodeGenes.get(to).getLayer();
        child.connectionGenes.put(globalInnovationNumbers
                .getInnovationNumber(from,to),new ConnectionGene(from,to,
                child.generateRandom(), true,recurrent) );
        child.age = 0;
        return child;
    }

    /**
     * Add a node in random connection gene
     * @param genome                    :   parent
     * @param globalInnovationNumbers   :   Historical Marking
     * @return                          :   mutated child
     */
    public static Genome mutateNode(Genome genome,GlobalInnovationNumbers globalInnovationNumbers){
        Genome child = genome.copy();

        //Enabled innovation numbers
        Set<Integer> enabledInnovationNumbers = new HashSet<>();
        for (int _innovationNumber:child.connectionGenes.keySet()){
            int[] n = globalInnovationNumbers.getInnovationNumbers().get(_innovationNumber);
            if(n[0]==n[1])
                continue;//self loop connection
            if(!child.connectionGenes.get(_innovationNumber).getState())
                continue;//disabled
            if(child.connectionGenes.get(_innovationNumber).isRecurrent())
                continue;//recurrent
            enabledInnovationNumbers.add(_innovationNumber);
        }

        //Randomly choose connection genes
        float prob = random.nextFloat();
        float cumulative_prob = 0;
        int innovationNumber = 0;
        for (int _innovationNumber:enabledInnovationNumbers){
            cumulative_prob += 1/(float) enabledInnovationNumbers.size();
            if(prob<=cumulative_prob){
                innovationNumber = _innovationNumber;
                break;
            }
        }
        ConnectionGene connectionGene = child.connectionGenes.get(innovationNumber);
        connectionGene.setState(false);//disabled

        int from = connectionGene.getFromNode();
        int to = connectionGene.getToNode();
        float weight = connectionGene.getWeight();
        int newNode = child.getLargestNodeIndex()+1;
        int fromLayer = child.nodeGenes.get(from).getLayer();
        int toLayer = child.nodeGenes.get(to).getLayer();
        boolean recurrent = connectionGene.isRecurrent();

        //from_node->new_node
        child.connectionGenes.put(globalInnovationNumbers.
                getInnovationNumber(from,newNode),
                new ConnectionGene(from,newNode,1,true,recurrent));
        //new_node->from_node
        child.connectionGenes.put(globalInnovationNumbers.
                        getInnovationNumber(newNode,to),
                new ConnectionGene(newNode,to,weight,true,recurrent));

        //Always from_layer<to_layer
        //Add node
        int layer = fromLayer+1;
        NodeGene nodeGene = new NodeGene(NodeGene.HIDDEN)
                .setLayer(layer);
        child.nodeGenes.put(newNode,nodeGene);

        //Adjust layer
        if(layer>=toLayer){
            //if destination layer is equal to new node layer
            child.adjustLayerOfNodesFrom(newNode);
            child.resetLayerOfInputsAndOutputs();
            child.checkWeightRecurrent();
        }

        child.age = 0;
        return child;
    }

    //Adjust all the layer
    private void adjustLayerOfNodesFrom(int fromNode){
        if(nodeGenes.get(fromNode).getNodeType()==NodeGene.OUTPUT)
            return;
        for(ConnectionGene gene:connectionGenes.values()){
            if(!gene.getState())
                continue;//disabled gene
            if(gene.isRecurrent())
                continue;//recurrent
            if(gene.getFromNode()!=fromNode)
                continue;//gene has destination different node
            int fromLayer   = nodeGenes.get(fromNode).getLayer();
            if(fromLayer<nodeGenes.get(gene.getToNode()).getLayer())
                continue;//source layer is less than destination layer
            nodeGenes.get(gene.getToNode()).setLayer(fromLayer+1);
            adjustLayerOfNodesFrom(gene.getToNode());
        }
    }

    private void resetLayerOfInputsAndOutputs(){
        //for output node genes
        int largestLayer = getLargestLayer();
        for (int i = 1+numberOfInputs; i <= numberOfInputs+numberOfOutputs; i++)
            nodeGenes.get(i).setLayer(largestLayer);
    }

    private void checkWeightRecurrent(){
        for(int innovationNumber:connectionGenes.keySet()){
            int fromLayer    = nodeGenes.get(connectionGenes.get(innovationNumber).getFromNode()).getLayer();
            int toLayer      = nodeGenes.get(connectionGenes.get(innovationNumber).getToNode()).getLayer();

            if(toLayer>fromLayer)
                continue;//if not recurrent
            connectionGenes.get(innovationNumber).setRecurrent(true);
            if(isRecurrent)
                continue;//if network recurrent is enabled
            // recurrent is off but connection become recurrent than disable connection
            connectionGenes.get(innovationNumber).setState(false);
        }
    }

    /**
     * change the state of random connection gene (ENABLED->DISABLED and DISABLED->ENABLED)
     * @param genome    :   parent
     * @return          :   mutated child
     */
    public static Genome mutateState(Genome genome){
        Genome child = genome.copy();

        //Randomly choose connection genes
        int prob_index = random.nextInt(genome.connectionGenes.size());
        int i=0;
        int innovationNumber = 0;
        for (int _innovationNumber:child.connectionGenes.keySet()){
            if(i==prob_index){
                innovationNumber = _innovationNumber;
                break;
            }
            i++;
        }
        ConnectionGene connectionGene = child.connectionGenes.get(innovationNumber);
        connectionGene.setState(!connectionGene.getState());
        child.age = 0;
        return child;
    }

    /**
     * shift the weight of random connection gene (random(0,2)*weight)
     * @param genome    :   parent
     * @return          :   mutated child
     */
    public static Genome mutateWeightShift(Genome genome){
        Genome child = genome.copy();

        //Randomly choose connection genes
        int prob_index = random.nextInt(genome.connectionGenes.size());
        int i=0;
        int innovationNumber = 0;
        for (int _innovationNumber:child.connectionGenes.keySet()){
            if(i==prob_index){
                innovationNumber = _innovationNumber;
                break;
            }
            i++;
        }
        ConnectionGene connectionGene = child.connectionGenes.get(innovationNumber);
        connectionGene.setWeight(connectionGene.getWeight()+
                random.nextFloat(-0.03f,0.03f)* connectionGene.getWeight());
        child.age = 0;
        return child;
    }

    /**
     * assign random weight for a random connection gene
     * @param genome    :   parent
     * @return          :   mutated child
     */
    public static Genome mutateWeightRandom(Genome genome){
        Genome child = genome.copy();

        //Randomly choose connection genes
        int prob_index = random.nextInt(genome.connectionGenes.size());
        int i=0;
        int innovationNumber = 0;
        for (int _innovationNumber:child.connectionGenes.keySet()){
            if(i==prob_index){
                innovationNumber = _innovationNumber;
                break;
            }
            i++;
        }
        ConnectionGene connectionGene = child.connectionGenes.get(innovationNumber);
        connectionGene.setWeight(child.generateRandom());
        child.age = 0;
        return child;
    }

    //////////////////////////////COMPATIBILITY/////////////////////////////
    public static float compatibility(Genome genome1,Genome genome2,float c1,float c2,float c3){
        float M = 0;    //umber of matching genes
        float W = 0;    //Average weight of matching genes
        float E = 0;    //Number of excess genes
        float D = 0;    //Number of disjoint genes
        float N;        //Total number of genes

        int genome1LargestInnovationNumber  = genome1.getLargestInnovationNumber();
        int genome2LargestInnovationNumber  = genome2.getLargestInnovationNumber();
        int largestInnovationNumber = Math.max(genome1LargestInnovationNumber, genome2LargestInnovationNumber);
        for (int i = 1; i <= largestInnovationNumber; i++) {
            if(genome1.connectionGenes.containsKey(i) && genome2.connectionGenes.containsKey(i)){
                //In case of matching connection genes
                W += genome1.connectionGenes.get(i).getWeight()-genome2.connectionGenes.get(i).getWeight();
                M++;
            }else if(genome1.connectionGenes.containsKey(i)){
                if(genome1LargestInnovationNumber<=largestInnovationNumber){
                    //In case of disjoint connection genes for genome1
                    D++;
                }else{
                    //In case of excess connection genes for genome1
                    E++;
                }
            }else if(genome2.connectionGenes.containsKey(i)){
                if(genome2LargestInnovationNumber<=largestInnovationNumber){
                    //In case of disjoint connection genes for genome2
                    D++;
                }else{
                    //In case of excess connection genes for genome2
                    E++;
                }
            }
        }

        W = Math.abs(W/M);     //Averaging of wight difference
        N = Math.max(genome1.connectionGenes.size(),genome2.connectionGenes.size());
        //if both genome contains genes fewer than 20 then N can be kept 1
        if(N<20)
            N = 1;
        return (c1*E/N)+(c2*D/N)+(c3*W);
    }

    ///////////////////////////////CALCULATE/////////////////////////////////
    /**
     * @param inputs    :   inputs e.g: forward(1,2,3)
     * @return          :   outputs float[]
     */
    public float[] forward(float...inputs){
        int lastLayer = nodeGenes.get(numberOfInputs+numberOfOutputs).getLayer();

        if(isRecurrent) {
            //copy all current values to prev values
            for (NodeGene nodeGene : nodeGenes.values()) {
                nodeGene.setPrevValue(nodeGene.getCurrentValue());
            }
        }

        for (int layer = 1; layer <= lastLayer; layer++) {
            for(int node:getNodes(layer)){
                NodeGene nodeGene = nodeGenes.get(node);
                if(nodeGene.getNodeType()==NodeGene.INPUT){
                    //Input layer
                    nodeGene.setCurrentValue(calculate(node)+inputs[node-1]);
                }else if(nodeGene.getNodeType()==NodeGene.OUTPUT){
                    //Output layer
                    nodeGene.setCurrentValue(calculate(node));
                }else {
                    //Hidden layers
                    nodeGene.setCurrentValue(calculate(node));
                }
            }
        }

        float[] outputs = new float[numberOfOutputs];
        for (int i = 1; i <= numberOfOutputs; i++) {
            outputs[i-1] = nodeGenes.get(i+numberOfInputs).getCurrentValue();
        }
        return outputs;
    }

    //Calculate all the value in that node and layer
    private float calculate(int node){
        float layer = nodeGenes.get(node).getLayer();
        float sum = 0f;
        for (ConnectionGene connectionGene: connectionGenes.values()){
            if(connectionGene.getToNode() == node){
                int from = connectionGene.getFromNode();
                int fromLayer = nodeGenes.get(from).getLayer();
                if(fromLayer<layer){
                    //no recurrent
                    if(connectionGene.getState())
                        sum += nodeGenes.get(from).getCurrentValue()*connectionGene.getWeight();
                }else{
                    //recurrent
                    if(connectionGene.getState())
                        sum += nodeGenes.get(from).getPrevValue()*connectionGene.getWeight();
                }
            }
        }
        if(nodeGenes.get(node).getNodeType()==NodeGene.OUTPUT)
            return isActivationInOutput?activation(sum):sum;
        if(nodeGenes.get(node).getNodeType()==NodeGene.INPUT)
            return sum;
        return activation(sum);
    }

    //Return all the nodes in that layer
    private Set<Integer> getNodes(int layer){
        Set<Integer> nodes = new HashSet<>();
        for(int node:nodeGenes.keySet())
            if(nodeGenes.get(node).getLayer()==layer)
                nodes.add(node);
        return nodes;
    }

    ///////////////////////////////SETTERS///////////////////////////////////
    public Genome setNumberOfInputs(int numberOfInputs) {this.numberOfInputs =  numberOfInputs;return this;}
    public Genome setNumberOfOutputs(int numberOfOutputs) {this.numberOfOutputs =  numberOfOutputs;return this;}
    public Genome setFitness(float fitness){this.fitness = fitness;return this;}
    public Genome setAge(int age) {this.age =  age;return this;}
    public Genome increaseAge(){this.age++;return this;}
    public Genome decreaseAge(){this.age--;return this;}
    public Genome resetAge(){this.age=0;return this;}
    public Genome setActivationInOutput(boolean isActivationInOutput) {this.isActivationInOutput = isActivationInOutput;return this;}
    public Genome setRecurrent(boolean isRecurrent) {this.isRecurrent = isRecurrent;return this;}
    public Genome setRelu(boolean relu) {this.isRelu = relu;return this;}

    public Genome penalized(GlobalInnovationNumbers globalInnovationNumbers){
        nodeGenes.clear();
        connectionGenes.clear();
        init(numberOfInputs,numberOfOutputs,globalInnovationNumbers,isActivationInOutput,isRecurrent,isRelu);
        return this;
    }
    public Genome resetNodeValues(){
        for (NodeGene nodeGene: nodeGenes.values()){
            nodeGene.resetValue();
        }
        return this;
    }

    ///////////////////////////////GETTERS///////////////////////////////////
    public HashMap<Integer,NodeGene> getNodeGenes(){return nodeGenes;}
    public HashMap<Integer,ConnectionGene> getConnectionGenes(){return connectionGenes;}
    public int getNumberOfInputs() {return numberOfInputs;}
    public int getNumberOfOutputs() {return numberOfOutputs;}
    public float getFitness(){return fitness;}
    public int getAge(){return age;}
    public boolean isActivationInOutput() {return isActivationInOutput;}
    public boolean isRecurrent(){return isRecurrent;}
    public boolean isRelu() {return isRelu;}
    public int getLargestInnovationNumber(){
        int largestInnovationNumber = 1;
        for (int innovationNumber:connectionGenes.keySet()) {
            if(largestInnovationNumber<innovationNumber)
                largestInnovationNumber = innovationNumber;
        }
        return largestInnovationNumber;
    }

    public int getLargestNodeIndex(){
        int largestNodeIndex = 1;
        for (int node:nodeGenes.keySet()) {
            if(largestNodeIndex<node)
                largestNodeIndex = node;
        }
        return largestNodeIndex;
    }
    public int getLargestLayer(){
        int largestNodeLayer = 1;
        for (NodeGene nodeGene:nodeGenes.values()) {
            if(largestNodeLayer<nodeGene.getLayer())
                largestNodeLayer = nodeGene.getLayer();
        }
        return largestNodeLayer;
    }

    ////////////////////////////////TO STRING////////////////////////////////
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("--------------------NODE GENES----------------------\n");
        for(int node:nodeGenes.keySet()){
            str.append("[").append(node).append("]\t");
            NodeGene nodeGene = nodeGenes.get(node);
            switch (nodeGene.getNodeType()) {
                case 0 -> str.append("INPUT");
                case 1 -> str.append("OUTPUT");
                case 2 -> str.append("HIDDEN");
            }
            str.append(", ").append(nodeGene.getPrevValue());
            str.append(", ").append(nodeGene.getCurrentValue());
            str.append(", ").append(nodeGene.getLayer());
            str.append("\n");
        }
        str.append("--------------------CONNECTION GENES----------------\n");
        for (int innovationNumber:connectionGenes.keySet()){
            str.append("[").append(innovationNumber).append("]\t");
            ConnectionGene connectionGene = connectionGenes.get(innovationNumber);
            str.append(connectionGene.getFromNode()).append("->")
                    .append(connectionGene.getToNode()).append(",")
                    .append(connectionGene.getWeight()).append(",");
            if (connectionGene.getState())
                str.append("ENABLED");
            else
                str.append("DISABLED");
            if(connectionGene.isRecurrent())
                str.append(",RECURRENT");
            str.append("\n");
        }

        return str.toString();
    }

    public void save(String filename){
        try {
            FileWriter writer = new FileWriter(filename+".genome");

            writer.write(numberOfInputs+"\n");
            writer.write(numberOfOutputs+"\n");
            writer.write(fitness+"\n");
            writer.write(age+"\n");
            writer.write(isActivationInOutput+"\n");
            writer.write(isRecurrent+"\n");
            writer.write(isRelu+"\n");

            //Node genes
            writer.write(nodeGenes.size()+"\n");
            for(int node:nodeGenes.keySet()){
                NodeGene nodeGene = nodeGenes.get(node);
                writer.write(node+"\n");
                writer.write(nodeGene.getNodeType()+"\n");
                writer.write(nodeGene.getLayer()+"\n");
                writer.write(nodeGene.getPrevValue()+"\n");
                writer.write(nodeGene.getCurrentValue()+"\n");
            }

            //Connection Gene
            writer.write(connectionGenes.size()+"\n");
            for (int innovation:connectionGenes.keySet()){
                ConnectionGene connectionGene = connectionGenes.get(innovation);
                writer.write(innovation+"\n");
                writer.write(connectionGene.getFromNode()+"\n");
                writer.write(connectionGene.getToNode()+"\n");
                writer.write(connectionGene.getWeight()+"\n");
                writer.write(connectionGene.getState()+"\n");
                writer.write(connectionGene.isRecurrent()+"\n");
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read(String filename){
        try {
            Scanner reader = new Scanner(new File(filename+".genome"));

            //variables
            numberOfInputs = reader.nextInt();
            numberOfOutputs = reader.nextInt();
            fitness = reader.nextFloat();
            age = reader.nextInt();
            isActivationInOutput = reader.nextBoolean();
            isRecurrent = reader.nextBoolean();
            isRelu = reader.nextBoolean();

            //Node genes
            int nodeGenesSize = reader.nextInt();
            for (int j = 0; j < nodeGenesSize; j++) {
                int node = reader.nextInt();
                int nodeType = reader.nextInt();
                NodeGene nodeGene = new NodeGene(nodeType);
                nodeGene.setLayer(reader.nextInt());
                nodeGene.setPrevValue(reader.nextFloat());
                nodeGene.setCurrentValue(reader.nextFloat());
                nodeGenes.put(node,nodeGene);
            }

            //Connection Gene
            int connectionSize = reader.nextInt();
            for (int j = 0; j < connectionSize; j++) {
                int innovation = reader.nextInt();
                int fromNode = reader.nextInt();
                int toNode = reader.nextInt();
                float weight = reader.nextFloat();
                boolean state = reader.nextBoolean();
                boolean recurrent = reader.nextBoolean();
                ConnectionGene connectionGene = new ConnectionGene(fromNode,toNode,weight,state,recurrent);
                connectionGenes.put(innovation,connectionGene);
            }

            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
