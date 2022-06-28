package neat;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class NeatGraphics extends JFrame {
    private Genome genome;
    private boolean isRunning = false;

    public NeatGraphics(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int WIDTH = 1000;
        int HEIGHT = 500;
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    public void draw(Genome genome){
        if(!isRunning) {
            this.genome = genome.copy();
            repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        if(genome!=null) {
            isRunning = true;
            g.setColor(Color.BLACK);
            g.fillRect(0,0,getWidth(),getHeight());
            drawGenome(g);
            isRunning = false;
        }
    }

    private void drawGenome(Graphics g){
        int width   = getWidth();
        int height  = getHeight();

        //Layer->Number_of_nodes
        Map<Integer,List<Integer>> layers = new HashMap<>();
        for (int node: genome.getNodeGenes().keySet()){
            NodeGene nodeGene = genome.getNodeGenes().get(node);
            if(!layers.containsKey(nodeGene.getLayer())){
                layers.put(nodeGene.getLayer(),new ArrayList<>());
                layers.get(nodeGene.getLayer()).add(node);
            }else{
                layers.get(nodeGene.getLayer()).add(node);
            }
        }

        List<Integer> layersPos = new ArrayList<>(layers.keySet().stream().toList());
        Collections.sort(layersPos);

        //draw all connections
        for(ConnectionGene connectionGene:genome.getConnectionGenes().values()){
            int fromNode        = connectionGene.getFromNode();
            int toNode          = connectionGene.getToNode();
            boolean state       = connectionGene.getState();

            int from_layer    = genome.getNodeGenes().get(fromNode).getLayer();
            int from_layer_pos  = layersPos.indexOf(from_layer)+1;
            int from_node_pos   = layers.get(from_layer).indexOf(fromNode)+1;
            float from_x_ratio  = (float) width/(float)(layers.size()+1);
            float from_y_ratio  = (float) height/(float)(layers.get(from_layer).size()+1);
            int from_x          = (int)(from_layer_pos * from_x_ratio);
            int from_y          = (int)(from_node_pos * from_y_ratio);

            int to_layer      = genome.getNodeGenes().get(toNode).getLayer();
            int to_layer_pos    = layersPos.indexOf(to_layer)+1;
            int to_node_pos     = layers.get(to_layer).indexOf(toNode)+1;
            float to_x_ratio    = (float) width/(float)(layers.size()+1);
            float to_y_ratio    = (float) height/(float)(layers.get(to_layer).size()+1);
            int to_x            = (int)(to_layer_pos * to_x_ratio);
            int to_y            = (int)(to_node_pos * to_y_ratio);

            int offset = 5;
            if(from_layer<to_layer){
                //forward
                if(state)
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.RED);
                if(from_y!=to_y){
                    g.drawLine(from_x,from_y-offset,to_x,to_y-offset);
                }else{
                    int a = to_x-from_x;
                    int b = 70;
                    g.drawArc(from_x,from_y-b/2,a,b,0,180);
                }
            }else if(from_layer>to_layer){
                //backward
                if(state)
                    g.setColor(Color.BLUE);
                else
                    g.setColor(Color.RED);
                if(from_y!=to_y){
                    g.drawLine(from_x,from_y+offset,to_x,to_y+offset);
                }else{
                    int a = from_x-to_x;
                    int b = 70;
                    g.drawArc(to_x,to_y-b/2,a,b,180,180);
                }
            }else {
                //same layer
                if(fromNode==toNode){
                    //same node
                    int a = 30;
                    int b = 35;
                    if(state)
                        g.setColor(Color.YELLOW);
                    else
                        g.setColor(Color.RED);
                    g.drawOval(to_x-a/2,to_y-b,a,b);
                }else if(fromNode<toNode){
                    //up to down
                    int a = 70;
                    int b = to_y-from_y;
                    if(state)
                        g.setColor(Color.GREEN);
                    else
                        g.setColor(Color.RED);
                    g.drawArc(from_x-a/2,from_y,a,b,90,-180);
                }else{
                    //up to down
                    int a = 70;
                    int b = from_y-to_y;
                    if(state)
                        g.setColor(Color.BLUE);
                    else
                        g.setColor(Color.RED);
                    g.drawArc(from_x-a/2,to_y,a,b,90,180);
                }
            }

        }

        //draw all nodes
        for(int node:genome.getNodeGenes().keySet()){
            int layer         = genome.getNodeGenes().get(node).getLayer();
            int layer_pos       = layersPos.indexOf(layer)+1;
            int node_pos        = layers.get(layer).indexOf(node)+1;
            float x_ratio       = (float) width/(float)(layers.size()+1);
            float y_ratio       = (float) height/(float)(layers.get(layer).size()+1);
            int x               = (int)(layer_pos * x_ratio);
            int y               = (int)(node_pos * y_ratio);

            int RADIUS          = 20;

            g.setColor(Color.WHITE);
            g.fillOval(x-RADIUS/2,y-RADIUS/2,RADIUS,RADIUS);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Courier", Font.BOLD,14));
            g.drawString(String.valueOf(node), x-RADIUS/3,y+RADIUS/4);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Courier", Font.PLAIN,14));
        g.drawString("fitness = "+String.valueOf(genome.getFitness()),width/10,height/10);
    }


}
