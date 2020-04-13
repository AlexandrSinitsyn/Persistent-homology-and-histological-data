package entropy;

import activity.StartActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class CalculateEntropy {

    private static LinkedList<String> getAllFiles(File file) {
        LinkedList<String> r = new LinkedList<>();

        if (!file.isDirectory())
            if (file.getName().endsWith(".png"))
                return new LinkedList<>(List.of(file.getAbsolutePath()));

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                System.out.println(f.getName());
                r.addAll(getAllFiles(f));
            }
            else
                if (f.getName().endsWith(".png"))
                    r.add(f.getAbsolutePath());
        }

        System.out.println(r.size());
        return r;
    }

    private static String[] toMas(LinkedList<String> list) {
        String[] r = new String[list.size()];

        for (int i = 0; i < list.size(); i++) r[i] = list.get(i);

        return r;
    }



    private int queueSize = 0;
    private int completedSize = 0;
    private double timePassed = 0;
    private double presumablyLeft = 0;


    public void start(String in, String out, int size, int splitter, boolean isNormal)
            throws InterruptedException, FileNotFoundException {
        final File folder = new File(in);

        String[] files = toMas(getAllFiles(folder));
        if (files == null || files.length == 0) {
            System.err.println("F");
            System.exit(0);
        }


        CopyOnWriteArrayList<Four> images = new CopyOnWriteArrayList<>();


        CopyOnWriteArrayList<String> queue = new CopyOnWriteArrayList<>();
        Collections.addAll(queue, files);
        Thread.sleep(100);


        final long START = System.currentTimeMillis();


        queueSize = queue.size();
        new Thread(() -> {
            boolean hasNotFinished = true;

            while (hasNotFinished) {
                if (queue.size() == 0)
                    hasNotFinished = false;
                completedSize = queueSize - queue.size();

                timePassed = (System.currentTimeMillis() - START) / 1000d;
                presumablyLeft = timePassed * queue.size() / completedSize;



                if (isNormal) {
                    StartActivity.normProgress = completedSize * 100d / queueSize;
                    StartActivity.normTime = timePassed;
                    StartActivity.normLeft = presumablyLeft;
                } else {
                    StartActivity.tumProgress = completedSize * 100d / queueSize;
                    StartActivity.tumTime = timePassed;
                    StartActivity.tumLeft = presumablyLeft;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("F");
                }
            }
        }).start();



        int count = 10;
        CountDownLatch cd = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                while (!queue.isEmpty()) {
                    String file = queue.remove(0);


                    images.add(new Entropy().transitions(file, size, splitter));
                }

                cd.countDown();
            }).start();
        }

        cd.await();
        System.err.println("It worked for " + (System.currentTimeMillis() - START) / 1000d + " seconds");

        Thread.sleep(1000);




        var pw = new PrintWriter(new File(out));
        pw.println("FileName;Entropy0;Entropy1;FullEntropy");

        for (Four e : images)
            pw.println(e);

        pw.close();


        System.out.println("\n\n");
        for (Four e : images) System.out.println(e);
    }
}
