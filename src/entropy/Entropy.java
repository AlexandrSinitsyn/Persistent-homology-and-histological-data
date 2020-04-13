/*
package entropy;

import scala.Int;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Entropy {

    private LinkedList<pair<int[][], LinkedList<pair<Integer, Integer>>>> components;
    private int[][] matrix;
    public int[][] s;

    private LinkedList<pair<int[][], LinkedList<pair<Integer, Integer>>>> componentsInverted;
    private int[][] matrixInverted;
    public int[][] sInverted;


    private int count = 1 - 1;


    private double holeLength = 0;
    private double holeLengthInverted = 0;


    private int[][] imgPixels = null;



    private static BufferedImage scale(BufferedImage img, int width, int height) {
        BufferedImage scaledImage =
                new BufferedImage((width > 0) ? width : 1, (height > 0) ? height : 1, img.getType());
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.drawImage(img, 0, 0, width, height, null);
        graphics2D.dispose();

        return scaledImage;
    }

    private int[][][] modifyImage(BufferedImage image, int color) {
        int w = image.getWidth();
        int h = image.getHeight();

        var pixels = new int[w][h];
        var pixelsInverted = new int[w][h];

        for (var i = 0; i < imgPixels.length; i++) {
            for (var j = 0; j < imgPixels[i].length; j++) {
                if (imgPixels[i][j] > color) {
                    pixels[i][j] = 255;
                    pixelsInverted[i][j] = 0;
                } else {
                    pixels[i][j] = 0;
                    pixelsInverted[i][j] = 255;
                }
            }
        }

        return new int[][][] {pixels, pixelsInverted};
    }


    private pair<int[][], LinkedList<pair<Integer, Integer>>> getComponent(int[][] pixels) {
        count = 1 - 1;


        int[][] r = new int[pixels.length][pixels[0].length];
        boolean[][] was = new boolean[pixels.length][pixels[0].length];
        var component = new LinkedList<pair<Integer, Integer>>();


        int x = 0;
        int y = 0;
        while (y < pixels.length) {
            if (!was[y][x]) {
                int color = pixels[y][x];

                was[y][x] = true;
                if (color == 0)
                    acrossComponent(x, y, r, was, component, pixels);
            }

            x++;
            if (x >= pixels[y].length) {
                x = 0;

                y++;
            }
        }


        return new pair(r, component);
    }

    private void acrossComponent(int startX, int startY, int[][] map, boolean[][] was,
                                 LinkedList<pair<Integer, Integer>> component, int[][] imgPixels) {
        boolean first = true;

        var toVisit = new LinkedList<pair<Integer, Integer>>();
        toVisit.add(new pair(startX, startY));

        while (!toVisit.isEmpty()) {
            pair<Integer, Integer> current = toVisit.removeFirst();
            int x = current.i;
            int y = current.j;

            if (imgPixels[y][x] == 0) {
                if (first) {
                    first = false;

                    count++;
                    component.add(new pair<>(x, y));
                }

                map[y][x] = count;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (y + i >= 0 && y + i < imgPixels.length &&
                                x + j >= 0 && x + j < imgPixels[y].length &&
                                !was[y + i][x + j]) {
                            toVisit.push(new pair<>(x + j, y + i));

                            was[y + i][x + j] = true;
                        }
                    }
                }
            }
        }
    }


    private void getMatrix(String path, int size, int splitter) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));

            int scale = (size == 1) ? image.getWidth() : size;

            image = scale(image, scale, scale);
        } catch (IOException e) {
            System.err.println(path + "\tF");
            System.exit(0);
        }


        imgPixels = new int[image.getWidth()][image.getHeight()];

        var listOfColors = new int[256];
        for (int i = 0; i < imgPixels.length; i++) {
            for (int j = 0; j < imgPixels[i].length; j++) {
                Color c = new Color(image.getRGB(i, j));
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                int color = (r + g + b) / 3;

                imgPixels[i][j] = color;
                listOfColors[color]++;
            }
        }


        Map<Integer, Integer> setOfColors = new ConcurrentHashMap<>();

        for (int color = 0; color < listOfColors.length; color++) {
            if (listOfColors[color] != 0)
                setOfColors.put(color, listOfColors[color]);
        }


        var colors = getListOfColors(setOfColors, splitter);


        final var img = image;
        colors.forEach((color, count) -> {
            var images = modifyImage(img, color);
            var comp = getComponent(images[0]);
            var compInv = getComponent(images[1]);

            for (int i = 0; i < count; i++) {
                components.add(comp);
                componentsInverted.addFirst(compInv);
            }
        });
    }

    private int currentLine = 0;
    private int fullLine = 0;
    private Map<Integer, Integer> getListOfColors(
            Map<Integer, Integer> setOfColors, int numberOfSpaces) {
        setOfColors.forEach((color, count) -> fullLine += count);

        if (numberOfSpaces <= 50) System.exit(1);
        final var splitter = fullLine / numberOfSpaces;
        if (splitter == 0) System.exit(1);

        var result = new ConcurrentHashMap<Integer, Integer>();
        result.put(0, 1);

        setOfColors.forEach((color, count) -> {
            currentLine += count;

            int current = currentLine / splitter;
            currentLine -= current * splitter;

            if (current != 0)
                result.put(color, current);
        });
        result.put(255, 1);

        return result;
    }



    private int countOfSpaces;
    public Four transitions(String path, int size, int splitter) {
        countOfSpaces = splitter + 2 - 1;
        reset(countOfSpaces);


        getMatrix(path, size, splitter);


        for (int n = 0; n <= countOfSpaces; n++) {
            for (int m = n; m <= countOfSpaces; m++) {
                // todo     0-Entropy
                {
                    var first = components.get(n).j;

                    var arrival = new LinkedList<Integer>();
                    for (entropy.pair<Integer, Integer> e : first)
                        arrival.add(components.get(m).i[e.j][e.i]);


                    matrix[m][n] = (new HashSet<>(arrival)).size();
                }

                // todo     1-Entropy
                {
                    var first = componentsInverted.get(n).j;

                    var arrival = new LinkedList<Integer>();
                    for (entropy.pair<Integer, Integer> e : first)
                        arrival.add(componentsInverted.get(m).i[e.j][e.i]);


                    matrixInverted[m][n] = (new HashSet<>(arrival)).size();
                }

                if (m != n) {
                    // todo     0-Entropy
                    {
                        s[m - 1][n] = getSnm(n, m - 1, matrix);

                        holeLength += ((m - 1) - n) * s[m - 1][n];
                    }

                    // todo     1-Entropy
                    {
                        sInverted[m - 1][n] = getSnm(n, m - 1, matrixInverted);

                        holeLengthInverted += ((m - 1) - n) * sInverted[m - 1][n];
                    }
                }
            }
        }

        for (int n = 0; n <= countOfSpaces; n++) {
            // todo     0-Entropy
            {
                s[countOfSpaces][n] = getSnm(n, countOfSpaces, matrix);

                holeLength += ((countOfSpaces - n) * s[countOfSpaces][n]);
            }

            // todo     1-Entropy
            {
                sInverted[countOfSpaces][n] = getSnm(n, countOfSpaces, matrixInverted);

                holeLengthInverted += ((countOfSpaces - n) * sInverted[countOfSpaces][n]);
            }
        }


        String[] nameOfTheFile = path.split("/");
        String name = nameOfTheFile[nameOfTheFile.length - 1];
        System.out.print(name + ": ");

        double result = H(s, holeLength);
        double resultInverted = H(sInverted, holeLengthInverted);
        double resultCombined = getCombinedEntropy();

        System.out.println(result + "\n" + resultInverted + "\n" + resultCombined);


        return new Four(name, result, resultInverted, resultCombined);
    }




    private int getSnm(int n, int m, int[][] r) {
        int snm = r[m][n];

        if (n - 1 >= 0)
            snm += -r[m][n - 1] + ((m + 1 < r.length) ? r[m + 1][n - 1] : 0);

        snm += -((m + 1 < r.length) ? r[m + 1][n] : 0);

        return snm;
    }

    public double H(int[][] s, double L) {
        var r = 0d;

        for (int n = 0; n <= countOfSpaces; n++) {
            for (int m = n + 1; m <= countOfSpaces; m++) {
                if (s[m][n] == 0) continue;
                //System.out.println(r + " (" + n + ", " + m + ")");

                double argument = (m - n) * s[m][n] / L;
                //System.out.println(argument + " " + (m - n) + " " + s[m][n] + " " + L);
                r += (argument * (Math.log(argument) / Math.log(2)));
            }
        }

        return -r;
    }



    private static final boolean zeroes = true;

    public static String toString(int[][] mas) {
        String R = "";

        for (int m = 0; m < mas.length; m++) {
            String r = "";

            for (int n = 0; n < mas[m].length; n++)
                r += (m < n && !zeroes) ? " " : mas[m][n] + " ";

            R += r + "\n";
        }

        return R;
    }

    public static void printList(int[][] mas) {
        System.out.println(toString(mas));
    }


    private static int[][] addEdge(int[][] array) {
        int[][] r = new int[array.length + 1 + 1][array[0].length + 1 + 1];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                r[i + 1][j + 1] = array[i][j];
            }
        }

        return r;
    }

    private void reset(int splitter) {
        ++splitter;

        components = new LinkedList<>();
        matrix = new int[splitter][splitter];
        s = new int[splitter][splitter];

        componentsInverted = new LinkedList<>();
        matrixInverted = new int[splitter][splitter];
        sInverted = new int[splitter][splitter];

        count = 1 - 1;

        holeLength = 0;
        holeLengthInverted = 0;


        fullLine = 0;
        currentLine = 0;
    }


    public double getCombinedEntropy() {
        var combined = new int[s.length][s[0].length];

        var len = 0;
        for (int n = 0; n <= countOfSpaces; n++) {
            for (int m = n; m <= countOfSpaces; m++) {
                combined[m][n] = s[m][n] + sInverted[m][n];

                len += (m - n) * combined[m][n];
            }
        }

        return H(combined, len);
    }
}
*/