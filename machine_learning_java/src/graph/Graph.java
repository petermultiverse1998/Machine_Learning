package graph;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Graph extends JFrame {
    private final List<float[]> xy;
    private volatile boolean isRunning;
    private boolean isReady;

    public Graph(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int WIDTH = 1000;
        int HEIGHT = 500;
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
        isRunning = false;
        isReady   = true;
        xy = new ArrayList<>();
    }

    @Override
    public void paint(Graphics g) {
        if(!xy.isEmpty() && isReady){
            Graphics2D g2D = (Graphics2D) g;

            g2D.setColor(Color.BLACK);
            g2D.fillRect(0,0,getWidth(),getHeight());

            isRunning = true;
            int width       = getWidth();
            int height      = getHeight();

            int size = xy.size();
            float xPrev     = xy.get(0)[0];
            float yPrev     = xy.get(0)[1];
            float xMin      = xPrev;
            float xMax      = xPrev;
            float yMin      = yPrev;
            float yMax      = yPrev;

            for (int i = 1; i < size; i++) {
                float x = xy.get(i)[0];
                float y = xy.get(i)[1];
                xMin = Math.min(xMin,x);
                xMax = Math.max(xMax,x);
                yMin = Math.min(yMin,y);
                yMax = Math.max(yMax,y);
            }

            int step = size<=100?1:(size/100);

            int xPrevPlot = 0,yPrevPlot = 0;
            int marginX = width/20;
            int marginY = height/15;

            for (int i = 0; i < size; i+=step) {
                float x         = xy.get(i)[0];
                float y         = xy.get(i)[1];

                float scaleX    = (width-2*marginX)/(xMax-xMin);
                float scaleY    = (height-2*marginY)/(yMax-yMin);

                if(xMax-xMin==0 || yMax-yMin==0)
                    continue;

                int xPlot       = marginX+(int) ((x-xMin) * scaleX);
                int yPlot       = height-(marginY+(int) ((y-yMin) * scaleY));

                if(i>0) {
                    g2D.setColor(Color.CYAN);
                    g2D.setStroke(new BasicStroke(3));
                    g2D.drawLine(xPrevPlot,yPrevPlot,xPlot,yPlot);
                }
                xPrevPlot = xPlot;
                yPrevPlot = yPlot;
            }


            g.setFont(new Font("Courier", Font.PLAIN,14));
            g2D.setStroke(new BasicStroke(1));
            int separation = 18;
            g.setColor(Color.RED);
            g.drawString("Max : "+yMax,120,60);
            g.drawString("Min  : "+yMin,120,60+separation);
            g.drawString("Avg  : "+average(xy),120,60+2*separation);
//            g.drawString("Xmax : "+xMax,120,60+2*separation);
//            g.drawString("Xmin  : "+xMin,120,60+3*separation);

            int fix = 1;

            g.setColor(Color.GREEN);
            int sizeX = 9;
            step = (width-2*marginX)/sizeX;
            for (int i=0;i<sizeX+1;i++){
                float value = xMin+(xMax-xMin)*i/sizeX;
                int x = marginX+i*step;
                int y = height-marginY+marginY/2;
                g.drawString(String.valueOf((int)(value*Math.pow(10,fix))/Math.pow(10,fix)),x,y);
            }
            g.drawLine(marginX,height-marginY,width-marginX,height-marginY);

            g.setColor(Color.RED);
            int sizeY = 9;
            step = (height-2*marginY)/sizeY;
            for (int i=0;i<sizeY+1;i++){
                float value = yMin+(yMax-yMin)*i/sizeY;
                int x = marginX-marginX/2;
                int y = height-(marginY+i*step);
                g.drawString(String.valueOf((int)(value*Math.pow(10,fix))/Math.pow(10,fix)),x,y);
            }
            g.drawLine(marginX,height-marginY,marginX,marginY);
            isRunning = false;
        }
    }

    private float average(List<float[]>xy){
        float sumY = 0;
        float sumX = 0;
        for (float[] x_y:xy){
            sumX+=x_y[0];
            sumY+=x_y[1]*x_y[0];
        }
        return sumY/sumX;
    }

    public Graph clear(){
        while (isRunning) Thread.onSpinWait();
        isReady = false;
        xy.clear();
        isReady = true;
        repaint();
        return this;
    }

    public Graph plotXY(float x,float y){
        while (isRunning) Thread.onSpinWait();
        isReady = false;
        xy.add(new float[]{x,y});
        isReady = true;
        repaint();
        return this;
    }

    public Graph plotXY(List<float[]>xy){
        while (isRunning) Thread.onSpinWait();
        isReady = false;
        for (float[] x_y:xy)
            this.xy.add(new float[]{x_y[0],x_y[1]});
        isReady = true;
        repaint();
        return this;
    }

    public Graph plotY(float y){
        while (isRunning) Thread.onSpinWait();
        isReady = false;
        xy.add(new float[]{xy.size(),y});
        isReady = true;
        repaint();
        return this;
    }

    public Graph plotY(List<Float> y){
        while (isRunning) Thread.onSpinWait();
        isReady = false;
        for (float y_:y)
            this.xy.add(new float[]{xy.size(),y_});
        isReady = true;
        repaint();
        return this;
    }

}
