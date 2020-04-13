package activity;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JFrame;

public class EGEngine extends JFrame {
    private long lastFrameEnd;
    private long startTime;
    private Map<Integer, List<DrawableObject>> drawableObjectsDepthMap;

    EGEngine() {
        super("progress");

        this.setExtendedState(6);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            public void windowClosing(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {
                open = false;
            }
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
        });
        this.setVisible(true);
        this.setSize(700, 400);
        this.createBufferStrategy(2);
        this.lastFrameEnd = System.currentTimeMillis();
        this.startTime = System.currentTimeMillis();
        this.drawableObjectsDepthMap = new ConcurrentSkipListMap();
    }

    private boolean open = true;

    private int fps_limit = 60;
    void startDrawingThread() {
        Thread drawingThread = new Thread(() -> {
            while(open) {
                long dt = System.currentTimeMillis() - this.lastFrameEnd;
                this.lastFrameEnd = System.currentTimeMillis();
                try {
                    this.draw(dt);
                } catch (IllegalStateException ignored) {
                    System.err.println("EGEngine failed! Again(((");
                }
                long frameLength = System.currentTimeMillis() - this.lastFrameEnd;
                if (frameLength < (long)(1000 / fps_limit)) {
                    try {
                        Thread.sleep((long)(1000 / fps_limit) - frameLength);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
        drawingThread.start();
    }

    private void removeDrawableObject(DrawableObject drawableObject) {

        for (List<DrawableObject> objects : this.drawableObjectsDepthMap.values())
            objects.remove(drawableObject);
    }

    void addDrawableObject(DrawableObject drawableObject) {
        this.addDrawableObject(drawableObject, 0);
    }

    private void addDrawableObject(DrawableObject drawableObject, int depth) {
        List<DrawableObject> drawableObjects = this.drawableObjectsDepthMap.get(depth);
        if (drawableObjects == null) {
            drawableObjects = new CopyOnWriteArrayList<>();
            this.drawableObjectsDepthMap.put(depth, drawableObjects);
        }

        drawableObjects.add(drawableObject);
    }

    public void changeDepth(DrawableObject drawableObject, int newDepth) {
        this.removeDrawableObject(drawableObject);
        this.addDrawableObject(drawableObject, newDepth);
    }

    public List<DrawableObject> getDrawableObjects() {
        List<DrawableObject> result = new ArrayList<>();
        this.drawableObjectsDepthMap.forEach(result::addAll);
        return result;
    }

    private void draw(long dt) {
        BufferStrategy bs = this.getBufferStrategy();
        Graphics2D g2d = (Graphics2D)bs.getDrawGraphics();
        g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

        try {
            this.drawAndUpdate(g2d, (int)dt);
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        this.drawAndUpdateDrawableObjects(g2d, (int)dt);
        g2d.dispose();
        bs.show();
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawAndUpdateDrawableObjects(Graphics2D g2d, int dt) {
        this.drawableObjectsDepthMap.values().forEach((e) -> {
            e.forEach((d) -> {
                this.drawDrawableObject(d, g2d, dt);
            });
        });
    }

    private void drawDrawableObject(DrawableObject drawableObject, Graphics2D g2d, int dt) {
        try {
            drawableObject.drawAndUpdate(g2d, (double)dt / 1000.0D);
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    public int getTimeFromStartMillis() {
        return (int)(System.currentTimeMillis() - this.startTime);
    }

    private void drawAndUpdate(Graphics2D graphics, int dt) {}
}

interface DrawableObject {
    void drawAndUpdate(Graphics2D graphics2D, double sec);
}
