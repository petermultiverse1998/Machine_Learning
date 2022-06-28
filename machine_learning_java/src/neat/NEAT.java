package neat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Main neat algorithm
 * population               : number of genomes initially
 * numberOfInputs           : input size
 * numberOfOutputs          : output size
 * speciesSizeTarget        : record species size
 * isActivationInOutput     : activation in last layer
 * recurrent                : is recurrent required
 * isRelu                   : is relu
 */
public abstract class NEAT {
    private int population;
    private int numberOfInputs;
    private int numberOfOutputs;
    private int speciesSizeTarget;
    private boolean isActivationInOutput;
    private boolean recurrent;
    private boolean isRelu;

    protected final GlobalInnovationNumbers globalInnovationNumbers;
    protected final Random random;
    protected final List<Genome> genomes;
    protected final List<Specie> species;

    //Hyper parameters
    protected float c1;
    protected float c2;
    protected float c3;
    protected float compatibilityThreshold;
    protected int dropOfAge;
    protected float fitnessLimit;
    protected float compatibilityModifier;
    protected float survivalThreshold;
    protected float penalizingFitness;
    protected float mutationPercentage;       //mutation percentage
    protected float mutationWeightProbability;//weight mutation probability
    protected float mutationShiftProbability; //inside weight mutation, weight shift probability
    protected float mutationNodeProbability;  //Node mutation probability inside other mutation
    protected float interSpeciesCrossover;    //cross over inside same species probability

    protected Genome fittest;

    ///////////////////////////////////CONSTRUCTOR/////////////////////////////////////
    public NEAT(int population,int numberOfInputs, int numberOfOutputs,
                int speciesSizeTarget,boolean isActivationInOutput, boolean recurrent,boolean isRelu){
        this.numberOfInputs     = numberOfInputs;
        this.numberOfOutputs    = numberOfOutputs;
        this.population         = population;
        this.speciesSizeTarget  = speciesSizeTarget;
        this.isActivationInOutput = isActivationInOutput;
        this.recurrent          = recurrent;
        this.isRelu             = isRelu;

        globalInnovationNumbers = new GlobalInnovationNumbers();
        random                  = new Random();
        genomes                 = new ArrayList<>();
        species                 = new ArrayList<>();

        initParameters();

        //generate random population
        generateRandomPopulation();
        evaluation();
    }

    public NEAT(String filename){
        random = new Random();
        species = new ArrayList<>();
        genomes = new ArrayList<>();
        globalInnovationNumbers = new GlobalInnovationNumbers();
        read(filename);
        fittest = genomes.get(0).copy();
        initParameters();
    }

    /**
     * Initiate to change parameters
     */
    protected void initParameters(){
        c1 = 2.0f;
        c2 = 2.0f;
        c3 = 1.0f;
        compatibilityThreshold      = 6.0f;
        compatibilityModifier       = 0.3f;
        survivalThreshold           = 0.2f;
        dropOfAge                   = 15;
        fitnessLimit                = 1e9f;
        penalizingFitness           = 0;
        mutationPercentage          = 0.25f;
        mutationWeightProbability   = 0.8f;
        mutationShiftProbability    = 0.9f;
        mutationNodeProbability     = 0.3f;
        interSpeciesCrossover       = 0.001f;
    }

    ///////////////////////////////////////PROCESS////////////////////////////////////
    /**
     * Generate random population
     */
    private void generateRandomPopulation(){
        for(int i=0;i<population;i++){
            genomes.add(new Genome(numberOfInputs,numberOfOutputs,globalInnovationNumbers,isActivationInOutput,recurrent,isRelu));
        }
        fittest = genomes.get(0).copy();
    }

    /**
     * Speciation
     */
    private void speciation(){
        //Speciation
        int i=0;
        while (!genomes.isEmpty()){
            if(species.isEmpty())
                species.add(new Specie());

            Genome genome = genomes.get(0).copy();
            boolean similarSpecieFound = false;
            for(Specie specie:species){
                if(specie.compatibilityDistance(genome,c1,c2,c3)<=compatibilityThreshold){
                    specie.add(genome);
                    genomes.remove(0);
                    similarSpecieFound = true;
                    break;
                }
            }

            //In case of no similar species found
            if(!similarSpecieFound){
                species.add(new Specie());
                species.get(species.size()-1).add(genome);
                genomes.remove(0);
            }

//            if(species.size()==speciesSizeTarget)
//                return;
        }

        if(species.size()<speciesSizeTarget){
            compatibilityThreshold -=compatibilityModifier*compatibilityThreshold;
        }
        else if(species.size()>speciesSizeTarget){
            compatibilityThreshold +=compatibilityModifier*compatibilityThreshold;
        }
    }

    /**
     * Kill all weaker species
     */
    private void killingInsideSpecies(){
        for (Specie specie:species){
            //In a specie
            specie.arrange();                               //arrange all the genomes in specie in descending order
            specie.calculateFitness();                      //calculate total fitness
            specie.killLast(1-survivalThreshold); //Kill all the weaker genomes
            specie.calculateFitness();                      //again calculate total fitness
            specie.adjustFitness();                         //adjust fitness
            specie.calculateFitness();                      //again calculate total fitness
        }
    }

    /**
     * Copy all the genomes of species to next generation
     */
    private void copyToNextGeneration(){
        for (int i = 0; i < species.size(); i++) {
            for(Specie specie:species){
                if(i<specie.getGenomes().size()) {
                    if(genomes.size()>=survivalThreshold*population)
                        break;
                    genomes.add(specie.getGenomes().get(i).copy());
                }
            }
        }
//        for(Specie specie:species){
//            for (Genome genome:specie.getGenomes()){
//                genomes.add(genome.copy());
//            }
//        }
    }

    /**
     * @return : calculate and return total fitness
     */
    private float totalFitness(){
        float sum = 0;
        for (Specie specie:species){
            sum += specie.getFitness();
        }
        return sum;
    }

    /**
     * @return :  (reference) random specie
     */
    private Specie randomSpecies(){
        float prob              = random.nextFloat();
        float cumulativeProb    = 0;
        float totalFitness      = totalFitness();
        for(Specie specie:species){
            cumulativeProb += specie.getFitness()/totalFitness;
            if(prob <= cumulativeProb){
                return specie;
            }
        }
        return species.get(species.size()-1);
    }

    /**
     * Mutation
     */
    private void mutation(){
        int requiredSize = (int) (genomes.size()+population*mutationPercentage);
        while (genomes.size()<requiredSize) {
            if(random.nextFloat()<=mutationWeightProbability){
                //Weight mutation
                if(random.nextFloat()<=mutationShiftProbability){
                    //Weight shift mutation
                    genomes.add(Genome.mutateWeightShift(randomSpecies().randomGenome().copy()));
                }else{
                    //Random weight mutation
                    genomes.add(Genome.mutateWeightRandom(randomSpecies().randomGenome().copy()));
                }
            }else{
                //Node and connection mutation
                if(random.nextFloat()<=mutationNodeProbability){
                    //Node mutation
                    genomes.add(Genome.mutateNode(randomSpecies().randomGenome().copy(),globalInnovationNumbers));
                }else {
                    //connection mutation
                    Genome genome = null;
                    for (int i = 0; i < 100; i++) {
                        genome = Genome.mutateConnection(randomSpecies().randomGenome().copy(),globalInnovationNumbers);
                        if(genome!=null)
                            break;
                    }
                    if(genome!=null)
                        genomes.add(genome);
                }

            }
        }
    }

    /**
     * crossover
     */
    private void crossOver(){
        while (genomes.size()<population){
            if(random.nextFloat()>interSpeciesCrossover){
                //cross over among genomes of same species
                Specie specie = randomSpecies();
                Genome genome1 = specie.randomGenome();
                Genome genome2 = specie.randomGenome();
                genomes.add(Genome.crossover(genome1,genome2));
            }else{
                //cross over among genomes of different species
                Specie specie1 = randomSpecies();
                Specie specie2 = randomSpecies();
                if(specie1!=specie2)
                    break;
                genomes.add(Genome.crossover(specie1.randomGenome(),specie2.randomGenome()));
            }
        }
    }

    /**
     * Evaluation of fitness of genome
     */
    private void evaluation(){
        for(Genome genome:genomes){
            if(genome.getAge()>=dropOfAge)
                genome.penalized(globalInnovationNumbers);
            genome.resetNodeValues();
            float prevFitness = genome.getFitness();
            float newFitness  = fitness(genome);
            genome.setFitness(newFitness);

            //if fitness remain constant then increment in age
            if(Math.max(newFitness,prevFitness)<=fitnessLimit){
                if((newFitness-prevFitness)<=penalizingFitness)
                    genome.increaseAge();
                else
                    genome.resetAge();
            }

        }
    }

    /**
     * Organize genomes in descending order with respect to fitness
     */
    private void reorderGenomes(){
        genomes.sort((o1, o2) -> Float.compare(o2.getFitness(), o1.getFitness()));
    }

    /**
     * One generation
     */
    public void evolve(){
        speciation();
        killingInsideSpecies();
        genomes.clear();
        copyToNextGeneration();
        mutation();
        crossOver();
        species.clear();
        evaluation();
        reorderGenomes();
        fittest = genomes.get(0).copy();
    }

    public Genome fittestGenome() {
        return fittest;
    }

    ///////////////////////////////////////FITNESS/////////////////////////////////////
    public abstract float fitness(Genome genome);

    //////////////////////////////////////GETTER///////////////////////////////////////
    public List<Genome> getGenomes(){
        return genomes;
    }

    public void save(String filename){
        try {
            FileWriter writer = new FileWriter(filename+".neat");

            //variables
            writer.write(population+"\n");
            writer.write(numberOfInputs+"\n");
            writer.write(numberOfOutputs+"\n");
            writer.write(speciesSizeTarget+"\n");
            writer.write(isActivationInOutput+"\n");
            writer.write(recurrent+"\n");
            writer.write(isRelu+"\n");

            //Hyper parameters
            writer.write(c1+"\n");
            writer.write(c2+"\n");
            writer.write(c3+"\n");
            writer.write(compatibilityThreshold+"\n");
            writer.write(dropOfAge+"\n");
            writer.write(fitnessLimit+"\n");
            writer.write(compatibilityModifier+"\n");
            writer.write(survivalThreshold+"\n");
            writer.write(penalizingFitness+"\n");
            writer.write(mutationPercentage+"\n");
            writer.write(mutationWeightProbability+"\n");
            writer.write(mutationShiftProbability+"\n");
            writer.write(mutationNodeProbability+"\n");
            writer.write(interSpeciesCrossover+"\n");

            //Global Innovation Numbers
            writer.write(globalInnovationNumbers.getInnovationNumbers().size()+"\n");
            for (int innovation_number:globalInnovationNumbers.getInnovationNumbers().keySet()){
                writer.write(innovation_number+"\n");
                int[] node = globalInnovationNumbers.getInnovationNumbers().get(innovation_number);
                writer.write(node[0]+"\n");
                writer.write(node[1]+"\n");
            }

            //Genomes
            writer.write(genomes.size()+"\n");
            for (Genome genome:genomes){
                //variables
                writer.write(numberOfInputs+"\n");
                writer.write(numberOfOutputs+"\n");
                writer.write(genome.getFitness()+"\n");
                writer.write(genome.getAge()+"\n");
                writer.write(genome.isActivationInOutput()+"\n");
                writer.write(genome.isRecurrent()+"\n");
                writer.write(genome.isRelu()+"\n");

                //Node genes
                writer.write(genome.getNodeGenes().size()+"\n");
                for(int node:genome.getNodeGenes().keySet()){
                    NodeGene nodeGene = genome.getNodeGenes().get(node);
                    writer.write(node+"\n");
                    writer.write(nodeGene.getNodeType()+"\n");
                    writer.write(nodeGene.getLayer()+"\n");
                    writer.write(nodeGene.getPrevValue()+"\n");
                    writer.write(nodeGene.getCurrentValue()+"\n");
                }

                //Connection Gene
                writer.write(genome.getConnectionGenes().size()+"\n");
                for (int innovation:genome.getConnectionGenes().keySet()){
                    ConnectionGene connectionGene = genome.getConnectionGenes().get(innovation);
                    writer.write(innovation+"\n");
                    writer.write(connectionGene.getFromNode()+"\n");
                    writer.write(connectionGene.getToNode()+"\n");
                    writer.write(connectionGene.getWeight()+"\n");
                    writer.write(connectionGene.getState()+"\n");
                    writer.write(connectionGene.isRecurrent()+"\n");
                }
            }

            genomes.get(0).save(filename+"_fittest");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read(String filename){
        try {
            Scanner reader = new Scanner(new File(filename+".neat"));

            //variables
            population = reader.nextInt();
            numberOfInputs = reader.nextInt();
            numberOfOutputs = reader.nextInt();
            speciesSizeTarget = reader.nextInt();
            isActivationInOutput = reader.nextBoolean();
            recurrent = reader.nextBoolean();
            isRelu    = reader.nextBoolean();

            //Hyper parameters
            c1=reader.nextFloat();
            c2=reader.nextFloat();
            c3=reader.nextFloat();
            compatibilityThreshold=reader.nextFloat();
            dropOfAge=reader.nextInt();
            fitnessLimit = reader.nextFloat();
            compatibilityModifier=reader.nextFloat();
            survivalThreshold=reader.nextFloat();
            penalizingFitness=reader.nextFloat();
            mutationPercentage=reader.nextFloat();
            mutationWeightProbability=reader.nextFloat();
            mutationShiftProbability=reader.nextFloat();
            mutationNodeProbability=reader.nextFloat();
            interSpeciesCrossover=reader.nextFloat();

            //Global Innovation Numbers
            int n = reader.nextInt();
            for (int i = 0; i < n; i++) {
                int innovation_number = reader.nextInt();
                int from = reader.nextInt();
                int to = reader.nextInt();
                globalInnovationNumbers.getInnovationNumbers().put(innovation_number,new int[]{from,to});
            }

            //Genomes
            int genomes_size = reader.nextInt();
            for (int i=0;i<genomes_size;i++){
                //variables
                Genome genome = new Genome();
                genome.setNumberOfInputs(reader.nextInt());
                genome.setNumberOfOutputs(reader.nextInt());
                genome.setFitness(reader.nextFloat());
                genome.setAge(reader.nextInt());
                genome.setActivationInOutput(reader.nextBoolean());
                genome.setRecurrent(reader.nextBoolean());
                genome.setRelu(reader.nextBoolean());

                //Node genes
                int nodeGenesSize = reader.nextInt();
                for (int j = 0; j < nodeGenesSize; j++) {
                    int node = reader.nextInt();
                    int nodeType = reader.nextInt();
                    NodeGene nodeGene = new NodeGene(nodeType);
                    nodeGene.setLayer(reader.nextInt());
                    nodeGene.setPrevValue(reader.nextFloat());
                    nodeGene.setCurrentValue(reader.nextFloat());
                    genome.getNodeGenes().put(node,nodeGene);
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
                    genome.getConnectionGenes().put(innovation,connectionGene);
                }
                genomes.add(genome);
            }

            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
