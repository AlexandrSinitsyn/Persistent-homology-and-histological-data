package activity;

import entropy.CalculateEntropy;
import entropy.Entropy;

import java.awt.*;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.*;

import static activity.Time.time;

public class StartActivity extends JFrame {

    private static Font font = new Font("Verdana", Font.PLAIN, 24);

    private static JComboBox inPixels, inPercents, splitterator, entropyChooser, startChooser, endChooser, splitChooser, scaleChooser, nameChooser;
    private static JLabel picturesCount, selectedSize, selectSplitterator, line1, normLabel, tumLabel, selectedEntropy, graphLength, graphScale, name, line2, chosenPicture, Entropy_0, Entropy_1, Entropy_Combined, entropyInfo;
    private static JTextArea pathNorm, pathTum;

    private static JButton normDirectoryChooser, tumDirectoryChooser, start, pictureChooser, exitButton;
    private static Container picturesCountContainer, sizeContainer, normContainer, tumContainer, lengthContainer, scaleContainer, choosePictureContainer, entropyResultsContainer;
    private static JToggleButton onePicture;
    private static JPanel content;

    public static double normProgress, tumProgress,
            normTime, tumTime,
            normLeft, tumLeft;

    private static int WIDTH = 850;
    private static int maxWidth = WIDTH;
    private static int HEIGHT = 750;

    private static File bat = new File("startPython.bat");
    private static String absolutePath = bat.getAbsolutePath().substring(0,
            bat.getAbsolutePath().length() - ("startPython.bat").length());

    private static int splitter = 64 - 2;


    private static class params {
        int size, split;
        String path;

        params(int size, int split, String path) {
            this.size = size;
            this.split = split;
            this.path = path;
        }

        boolean equals(params params) {
            return this.size == params.size &&
                    this.split== params.split &&
                    this.path.equals(params.path);
        }
    }

    private static params previousParamsNorm = new params(1, splitter, absolutePath + "FULL\\");
    private static params previousParamsTum = new params(1, splitter, absolutePath + "FULL\\");


    private static boolean onlyOneImage = false;


    public static void main(String[] args) throws IOException {
        new StartActivity();
    }



    private static void START(int chosenSize,
                              String pathToNorm, String pathToTum) throws IOException, InterruptedException {
        // todo    Java
        {
            {
                EGEngine e = new EGEngine();
                e.addDrawableObject((graphics2D, sec) -> {
                    graphics2D.setStroke(new BasicStroke(5));
                    graphics2D.setFont(font);
                    graphics2D.setColor(Color.blue);

                    int width = 500;

                    graphics2D.drawRect(50, 50, width, 25);
                    graphics2D.fillRect(50, 50, (int) (width * normProgress / 100d), 25);
                    graphics2D.setColor(Color.red);
                    graphics2D.drawString("progress in normal pictures: " + (int) normProgress + "%", 80, 70);
                    graphics2D.setColor(Color.blue);
                    graphics2D.drawString("it took: " + time(normTime), 50, 100);
                    graphics2D.drawString("presumably left: " + time(normLeft), 50, 130);

                    graphics2D.drawRect(50, 200, width, 25);
                    graphics2D.fillRect(50, 200, (int) (width * tumProgress / 100d), 25);
                    graphics2D.setColor(Color.red);
                    graphics2D.drawString("progress in tumor pictures: " + (int) tumProgress + "%", 80, 220);
                    graphics2D.setColor(Color.blue);
                    graphics2D.drawString("it took: " + time(tumTime), 50, 250);
                    graphics2D.drawString("presumably left: " + time(tumLeft), 50, 280);


                    graphics2D.drawString("You can not close this program," +
                            "before it will finish calculating.", 50, 350);
                });

                e.startDrawingThread();
            }

            // todo    Norm
            params currentParamsNorm = new params(chosenSize, splitter, pathToNorm);
            if (!previousParamsNorm.equals(currentParamsNorm)) {
                new CalculateEntropy().start(pathToNorm, "NormResults.csv", chosenSize, splitter, true);

                previousParamsNorm = currentParamsNorm;
            }
            normProgress = 100;


            Thread.sleep(1000);

            // todo    Tum
            params currentParamsTum = new params(chosenSize, splitter, pathToTum);
            if (!previousParamsTum.equals(currentParamsTum)) {
                new CalculateEntropy().start(pathToTum, "TumResults.csv", chosenSize, splitter, false);

                previousParamsTum = currentParamsTum;
            }
            tumProgress = 100;
        }

        JOptionPane.showMessageDialog(null,
                "\tIt took\n" +
                        "for norm: " + time(normTime) + "\n" +
                        "for tum: " + time(tumTime) + "\n" +
                        "in total: " + time(normTime + tumTime, true), "The hole time", JOptionPane.INFORMATION_MESSAGE);

        // todo    Python
        {
            assert startChooser.getSelectedItem() != null && endChooser.getSelectedItem() != null;

            if (Integer.parseInt(startChooser.getSelectedItem().toString()) >
                    Integer.parseInt(endChooser.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(null, "Start point can not be greater than the end point!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                assert entropyChooser.getSelectedItem() != null;

                int entropy;
                if (entropyChooser.getSelectedItem().equals("0-entropy"))
                    entropy = 0;
                else if (entropyChooser.getSelectedItem().equals("1-entropy"))
                    entropy = 1;
                else
                    entropy = 2;

                PrintWriter entropyWriter = new PrintWriter(new File("entropy.txt"));

                entropyWriter.println(entropy);
                entropyWriter.println(startChooser.getSelectedItem());
                entropyWriter.println(endChooser.getSelectedItem());
                entropyWriter.println(splitChooser.getSelectedItem());
                entropyWriter.println(scaleChooser.getSelectedItem());
                entropyWriter.println(nameChooser.getSelectedItem());

                entropyWriter.close();



                if (System.getProperty("os.name").equals("Linux")) {
                    var dirs = new File("drawGraph.py").getAbsolutePath().split("/");

                    var path = "";
                    for (String s : dirs)
                        path += "\"" + s + "\"";

                    Runtime.getRuntime().exec("python3 " + path);
                } else {
                    PrintWriter batStarter = new PrintWriter(bat);

                    batStarter.println("python " + new File("drawGraph.py").getAbsolutePath());

                    batStarter.close();


                    Runtime.getRuntime().exec("cmd /c start startPython.bat");
                }
            }
        }
    }



    private StartActivity() throws IOException {
        super("Persistent homology and topological analysis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(ImageIO.read(new File("icon.png")));


        setContainers();
        setLabels();
        setButtons();
        setGroupsOfButtons();
        setToggleButtons();



        content = (JPanel) getContentPane();

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));



        picturesCountContainer.add(picturesCount);
        picturesCountContainer.add(onePicture);
        content.add(picturesCountContainer);


        {
            content.add(selectedSize);

            sizeContainer.add(inPixels);
            sizeContainer.add(inPercents);
            content.add(sizeContainer);



            content.add(selectSplitterator);
            content.add(splitterator);




            content.add(normDirectoryChooser);
            normContainer.add(normLabel);
            normContainer.add(pathNorm);
            content.add(normContainer);


            content.add(tumDirectoryChooser);
            tumContainer.add(tumLabel);
            tumContainer.add(pathTum);
            content.add(tumContainer);


            content.add(line1);


            content.add(selectedEntropy);
            content.add(entropyChooser);


            content.add(graphLength);
            lengthContainer.add(startChooser);
            lengthContainer.add(endChooser);
            content.add(lengthContainer);


            content.add(graphScale);
            scaleContainer.add(splitChooser);
            scaleContainer.add(scaleChooser);
            content.add(scaleContainer);


            content.add(name);
            content.add(nameChooser);


            content.add(line2);


            content.add(start);
        }

        {
            content.add(pictureChooser);

            choosePictureContainer.add(chosenPicture);
            entropyResultsContainer.add(Entropy_0);
            entropyResultsContainer.add(Entropy_1);
            entropyResultsContainer.add(Entropy_Combined);
            choosePictureContainer.add(entropyResultsContainer);
            content.add(choosePictureContainer);

            content.add(entropyInfo);
        }



        setSize(WIDTH, HEIGHT);
        setVisible(true);
    }



    private int getImageSize() {
        switch (selectedSize.getText().split(" ")[1]) {
            case "50x50": return 50;
            case "100x100": return 100;
            case "150x150": return 150;
            case "200x200": return 200;
            case "224x224": return 1;
            case "250x250": return 250;
            case "300x300": return 300;
            case "400x400": return 400;
            case "500x500": return 500;
            case "800x800": return 800;
            case "1000x1000": return 1000;

            default: return 1;
        }
    }

    private int getSplitter() {
        var arr = selectSplitterator.getText().split(" ");

        return Integer.parseInt(arr[arr.length - 1]);
    }

    private String getPathToNorm() {
        return pathNorm.getText();
    }

    private String getPathToTum() {
        return pathTum.getText();
    }



    private void setGroupsOfButtons() {
        ActionListener actionListener = e -> {
            JComboBox box = (JComboBox) e.getSource();

            selectedSize.setText("size: " + box.getSelectedItem());
        };

        String[] sizesInPercents = {
                "10%",
                "20%",
                "30%",
                "50%",
                "70%",
                "80%",
                "100%",
                "200%"};
        inPercents = new JComboBox(sizesInPercents);
        inPercents.setAlignmentX(CENTER_ALIGNMENT);
        inPercents.setFont(font);
        inPercents.addActionListener(actionListener);
        inPercents.setSelectedIndex(6);

        String[] sizesInPixels = {
                "50x50",
                "100x100",
                "150x150",
                "200x200",
                "250x250",
                "300x300",
                "400x400",
                "500x500",
                "800x800",
                "1000x1000"};
        inPixels = new JComboBox(sizesInPixels);
        inPixels.setFont(font);
        inPixels.setAlignmentX(CENTER_ALIGNMENT);
        inPixels.addActionListener(actionListener);
        inPixels.setSelectedIndex(0);



        String[] countOfSplits = {
                "64",
                "128",
                "256",
                "512",
                "1024",
                "50",
                "100",
                "150"};
        splitterator = new JComboBox(countOfSplits);
        splitterator.setEditable(true);
        splitterator.setFont(font);
        splitterator.setAlignmentX(CENTER_ALIGNMENT);
        splitterator.addActionListener((e) -> {
            JComboBox box = (JComboBox) e.getSource();

            selectSplitterator.setText("count of splits: " + box.getSelectedItem().toString());

            splitter = getSplitter() - 2;
        });
        splitterator.setSelectedIndex(0);




        String[] entropy = {
                "0-entropy",
                "1-entropy",
                "combined entropy"};
        entropyChooser = new JComboBox(entropy);
        entropyChooser.setAlignmentX(CENTER_ALIGNMENT);
        entropyChooser.setFont(font);
        entropyChooser.setSelectedIndex(1);

        String[] start = {
                "7",
                "8",
                "9",
                "10",
                "11",
                "12"};
        startChooser = new JComboBox(start);
        startChooser.setAlignmentX(CENTER_ALIGNMENT);
        startChooser.setFont(font);
        startChooser.setEditable(true);
        startChooser.setSelectedIndex(1);

        String[] end = {
                "11",
                "12",
                "13",
                "14",
                "15",
                "16"};
        endChooser = new JComboBox(end);
        endChooser.setAlignmentX(CENTER_ALIGNMENT);
        endChooser.setFont(font);
        endChooser.setEditable(true);
        endChooser.setSelectedIndex(1);

        String[] split = {
                "25",
                "50",
                "75",
                "100",
                "150",
                "200",
                "250",
                "300",
                "250",
                "400"};
        splitChooser = new JComboBox(split);
        splitChooser.setAlignmentX(CENTER_ALIGNMENT);
        splitChooser.setFont(font);
        splitChooser.setSelectedIndex(1);

        String[] scale = {
                "200",
                "300",
                "400",
                "500",
                "600",
                "700",
                "800"};
        scaleChooser = new JComboBox(scale);
        scaleChooser.setAlignmentX(CENTER_ALIGNMENT);
        scaleChooser.setFont(font);
        scaleChooser.setSelectedIndex(0);



        String[] name = {
                "0_Entropy",
                "1_Entropy",
                "Combined_Entropy",
                "THE_GRAPH",
                "Entropy",
                "Persistent Entropy"};
        nameChooser = new JComboBox(name);
        nameChooser.setAlignmentX(CENTER_ALIGNMENT);
        nameChooser.setFont(font);
        nameChooser.setEditable(true);
        nameChooser.setSelectedIndex(0);
    }

    private void setButtons() {
        JFileChooser fileChooser = new JFileChooser();

        normDirectoryChooser = new JButton("Choose path to \"norm\" images");
        normDirectoryChooser.addActionListener((e) -> {
            fileChooser.setDialogTitle("Choose the directory with images");

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(StartActivity.this);


            if (result == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();

                int curWidth = path.length() * 20;
                if (curWidth > maxWidth) {
                    this.setSize(curWidth, HEIGHT);

                    maxWidth = curWidth;
                }

                pathNorm.setText(path);
            }
        });
        normDirectoryChooser.setAlignmentX(CENTER_ALIGNMENT);
        normDirectoryChooser.setFont(font);

        tumDirectoryChooser = new JButton("Choose path to \"tum\" images");
        tumDirectoryChooser.addActionListener((e) -> {
            fileChooser.setDialogTitle("Choose the directory with images");

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(StartActivity.this);


            if (result == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();

                int curWidth = path.length() * 20;
                if (curWidth > maxWidth) {
                    this.setSize(curWidth, HEIGHT);

                    maxWidth = curWidth;
                }

                pathTum.setText(path);
            }
        });
        tumDirectoryChooser.setAlignmentX(CENTER_ALIGNMENT);
        tumDirectoryChooser.setFont(font);


        start = new JButton("Start");
        start.addActionListener((e) -> {
            try {
                START(getImageSize(), getPathToNorm(), getPathToTum());
            } catch (IOException | InterruptedException ignored) {
                System.exit(0);
            }
        });
        start.setAlignmentX(CENTER_ALIGNMENT);
        start.setFont(font);



        pictureChooser = new JButton("Choose path to your image");
        pictureChooser.addActionListener((e) -> {
            fileChooser.setDialogTitle("Choose your image");

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(StartActivity.this);


            if (result == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();

                chosenPicture.setIcon(new ImageIcon(path));


                var entropies = new Entropy().transitions(path, getImageSize(), splitter);
                Entropy_0.setText("0-Entropy: " + entropies.entropy);
                Entropy_1.setText("1-Entropy: " + entropies.negative);
                Entropy_Combined.setText("Combined-Entropy: " + entropies.combined);

                var statistics = getVariance(entropies.negative);

                entropyInfo.setText("<html>" +
                        "The probability of this patch to be normal is: " +
                        statistics + "%</html>");
            }
        });
        pictureChooser.setAlignmentX(CENTER_ALIGNMENT);
        pictureChooser.setFont(font);
        pictureChooser.setVisible(false);

        exitButton = new JButton(new ImageIcon("exit.png"));
        exitButton.addActionListener((e) -> System.exit(0));
        exitButton.setAlignmentX(CENTER_ALIGNMENT);
        exitButton.setFont(font);
        exitButton.setVisible(false);
    }

    private void setLabels() {
        picturesCount = new JLabel(
                "<html>" +
                "Do you want to draw a graph or" +
                        "<br>" +
                "calculate entropy for only one picture</html>");
        picturesCount.setAlignmentX(CENTER_ALIGNMENT);
        picturesCount.setFont(font);



        selectedSize = new JLabel("size: 50x50");
        selectedSize.setAlignmentX(CENTER_ALIGNMENT);
        selectedSize.setFont(font);



        selectSplitterator = new JLabel("count of splits: 64");
        selectSplitterator.setAlignmentX(CENTER_ALIGNMENT);
        selectSplitterator.setFont(font);



        normLabel = new JLabel("path to \"norm\": ");
        normLabel.setAlignmentX(CENTER_ALIGNMENT);
        normLabel.setFont(font);



        pathNorm = new JTextArea(absolutePath + "NORM\\");
        pathNorm.setAlignmentX(CENTER_ALIGNMENT);
        pathNorm.setFont(font);


        tumLabel = new JLabel("path to \"tum\": ");
        tumLabel.setAlignmentX(CENTER_ALIGNMENT);
        tumLabel.setFont(font);


        pathTum = new JTextArea(absolutePath + "TUM\\");
        pathTum.setAlignmentX(CENTER_ALIGNMENT);
        pathTum.setFont(font);


        line1 = new JLabel(" ");
        line1.setAlignmentX(CENTER_ALIGNMENT);
        line1.setFont(font);

        selectedEntropy = new JLabel("select an entropy for drawing, please");
        selectedEntropy.setAlignmentX(CENTER_ALIGNMENT);
        selectedEntropy.setFont(font);


        graphLength = new JLabel("select the minimum and the maximum entropy on the graphic");
        graphLength.setAlignmentX(CENTER_ALIGNMENT);
        graphLength.setFont(font);

        graphScale = new JLabel("select how your graph will be zoomed");
        graphScale.setAlignmentX(CENTER_ALIGNMENT);
        graphScale.setFont(font);


        name = new JLabel("Choose a name for the file (your graph is going to be saved)");
        name.setAlignmentX(CENTER_ALIGNMENT);
        name.setFont(font);


        line2 = new JLabel(" ");
        line2.setAlignmentX(CENTER_ALIGNMENT);
        line2.setFont(font);


        chosenPicture = new JLabel();
        chosenPicture.setAlignmentX(CENTER_ALIGNMENT);
        chosenPicture.setFont(font);

        Entropy_0 = new JLabel();
        Entropy_0.setAlignmentX(CENTER_ALIGNMENT);
        Entropy_0.setFont(font);

        Entropy_1 = new JLabel();
        Entropy_1.setAlignmentX(CENTER_ALIGNMENT);
        Entropy_1.setFont(font);

        Entropy_Combined = new JLabel();
        Entropy_Combined.setAlignmentX(CENTER_ALIGNMENT);
        Entropy_Combined.setFont(font);

        entropyInfo = new JLabel();
        entropyInfo.setAlignmentX(CENTER_ALIGNMENT);
        entropyInfo.setFont(font);
        entropyInfo.setVisible(false);
    }

    private void setContainers() {
        picturesCountContainer = new Container();
        picturesCountContainer.setLayout(new BoxLayout(picturesCountContainer, BoxLayout.X_AXIS));


        sizeContainer = new Container();
        sizeContainer.setLayout(new BoxLayout(sizeContainer, BoxLayout.X_AXIS));


        normContainer = new Container();
        normContainer.setLayout(new BoxLayout(normContainer, BoxLayout.X_AXIS));

        tumContainer = new Container();
        tumContainer.setLayout(new BoxLayout(tumContainer, BoxLayout.X_AXIS));


        lengthContainer = new Container();
        lengthContainer.setLayout(new BoxLayout(lengthContainer, BoxLayout.X_AXIS));

        scaleContainer = new Container();
        scaleContainer.setLayout(new BoxLayout(scaleContainer, BoxLayout.X_AXIS));



        choosePictureContainer = new Container();
        choosePictureContainer.setLayout(new BoxLayout(choosePictureContainer, BoxLayout.X_AXIS));
        choosePictureContainer.setVisible(false);

        entropyResultsContainer = new Container();
        entropyResultsContainer.setLayout(new BoxLayout(entropyResultsContainer, BoxLayout.Y_AXIS));
        entropyResultsContainer.setVisible(false);
    }

    private void setToggleButtons() {
        onePicture = new JToggleButton("draw graph");
        onePicture.addActionListener((e) -> {
            sizeContainer.setVisible(onlyOneImage);
            normContainer.setVisible(onlyOneImage);
            tumContainer.setVisible(onlyOneImage);
            lengthContainer.setVisible(onlyOneImage);
            scaleContainer.setVisible(onlyOneImage);


            entropyChooser.setVisible(onlyOneImage);
            nameChooser.setVisible(onlyOneImage);

            normDirectoryChooser.setVisible(onlyOneImage);
            tumDirectoryChooser.setVisible(onlyOneImage);
            start.setVisible(onlyOneImage);


            selectedSize.setVisible(onlyOneImage);
            selectedEntropy.setVisible(onlyOneImage);

            graphLength.setVisible(onlyOneImage);
            graphScale.setVisible(onlyOneImage);
            name.setVisible(onlyOneImage);

            line1.setVisible(onlyOneImage);
            line2.setVisible(onlyOneImage);




            pictureChooser.setVisible(!onlyOneImage);
            choosePictureContainer.setVisible(!onlyOneImage);
            entropyResultsContainer.setVisible(!onlyOneImage);
            entropyInfo.setVisible(!onlyOneImage);
            exitButton.setVisible(!onlyOneImage);



            content.updateUI();

            onlyOneImage = !onlyOneImage;
        });
        onePicture.setAlignmentX(CENTER_ALIGNMENT);
        onePicture.setFont(font);
        onePicture.setSelected(true);
    }




    private double[] getArray(String path) {
        try {
            Scanner n = new Scanner(new File(path));
            n.nextLine();

            LinkedList<Double> res = new LinkedList<>();
            while (n.hasNextLine())
                res.add(Double.parseDouble(n.nextLine().split(";")[2]));

            var r = new double[res.size()];
            for (int i = 0; i < r.length; i++) r[i] = res.get(i);

            res.sort(Comparator.comparingDouble(a -> a));
            return r;
        } catch (FileNotFoundException ignored) {}

        return new double[]{};
    }

    private NormalDistribution normDistribution = null;
    private NormalDistribution tumDistribution = null;
    private double getVariance(double x) {
        if (normDistribution == null) {
            normDistribution = new NormalDistribution(
                    getArray("NormResults.csv"));
            tumDistribution =new NormalDistribution(
                    getArray("TumResults.csv"));
        }

        var f = normDistribution.normal(x);
        var g = tumDistribution.normal(x);

        var r = f / (f + g);

        return r * 100;
    }
}