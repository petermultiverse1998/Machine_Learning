package neat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Specie {
    private final List<Genome> genomes;
    private float fitness;
    private static Random random;

    public Specie(){
        random = new Random();
        genomes = new ArrayList<>();
        fitness = 0;
    }

    /**
     * Add the genome to specie
     * @param genome    :   (reference) genome
     */
    public void add(Genome genome){
        genomes.add(genome);
    }

    /**
     * Arrange Genomes In Descending Order With Fitness
     */
    public void arrange(){
        genomes.sort((o1, o2) -> Float.compare(o2.getFitness(),o1.getFitness()));
    }

    /**
     * Recalculate the fitness
     */
    public void calculateFitness(){
        fitness = 0;
        for(Genome genome:genomes){
            fitness += genome.getFitness();
        }
    }

    /**
     * Calculate total fitness and divide each genome fitness by total fitness
     */
    public void adjustFitness(){
        calculateFitness();
        for(Genome genome:genomes){
            genome.setFitness(genome.getFitness()/fitness);
        }
    }

    /**
     * @return : total fitness of species
     */
    public float getFitness(){
        return fitness;
    }

    /**
     * Kill All last species
     * @param percentage : percentage e.g 0.2 for 20%
     */
    public void killLast(float percentage){
        int survived = (int) ((1.0f-percentage) * genomes.size());
        if (survived==0)
            survived = 1;
        while (genomes.size()>survived){
            genomes.remove(genomes.size()-1);
        }
    }

    /**
     * return : (reference) random genome based on their probability
     */
    public Genome randomGenome(){
        calculateFitness();
        float prob = random.nextFloat();
        float cumulative_prob = 0;
        for (Genome genome:genomes){
            cumulative_prob+=genome.getFitness()/fitness;
            if(prob<=cumulative_prob){
                return genome;
            }
        }
        return genomes.get(genomes.size()-1);
    }

    /**
     * Kill all genomes
     */
    public void killAllGenomes(){
        fitness = 0;
        genomes.clear();
    }

    /**
     * @return  :   number of genomes
     */
    public int size(){
        return genomes.size();
    }

    /**
     * Check compatibility
     * @param genome    : Genome
     * @param c1        : Excess gene coefficient
     * @param c2        : Disjoint gene coefficient
     * @param c3        : Weight Difference coefficient
     * @return          : Distance
     */
    public float compatibilityDistance(Genome genome,float c1,float c2,float c3){
        if(genomes.isEmpty())
            return 0;
        return Genome.compatibility(genomes.get(0),genome,c1,c2,c3);
    }

    /**
     * @return : (reference) list of genomes
     */
    public List<Genome> getGenomes(){
        return genomes;
    }
}
