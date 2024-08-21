package neat;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Neat neat = new Neat();
        NeatGraphics graphics = new NeatGraphics();
        Genome fittest = null;
        for (int i = 0; i < 200; i++) {
            neat.evolve();
            fittest = neat.fittestGenome();
            graphics.draw(fittest);
            Thread.sleep(10);
            System.out.println(i);
        }

//        for (int i = 0; i < 10; i++) {
//            System.out.println(i+" : "+fittest.forward(1,i)[0]);
//        }

        System.out.println(" : "+fittest.forward(0,0)[0]);
        System.out.println(" : "+fittest.forward(1,0)[0]);
        System.out.println(" : "+fittest.forward(0,1)[0]);
        System.out.println(" : "+fittest.forward(1,1)[0]);

    }

    static class Neat extends NEAT {
        public Neat() {
            super(1000, 2, 1, 10, true, false, true);
        }

        @Override
        public float fitness(Genome genome) {
            float error = 0;
//            for (int i = 0; i < 10; i++) {
//                float f = genome.forward(1, i)[0];
//                error += (3 * i*i + 1 - f) * (3 * i*i + 1 - f);
//            }
            float f = genome.forward(0,0)[0];
            error += (0- f) * (0 - f);
            f = genome.forward(1,0)[0];
            error += (1- f) * (1 - f);
            f = genome.forward(0,1)[0];
            error += (1- f) * (1 - f);
            f = genome.forward(1,1)[0];
            error += (0- f) * (0 - f);
            return -error;
        }
    }
}
