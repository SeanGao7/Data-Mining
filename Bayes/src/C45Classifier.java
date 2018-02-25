import java.io.*;
import java.util.*;

/**
 *  An implementation of the C4.5 Classifier
 *  The first column in database is assumed to be the class label
 */

public class C45Classifier {
    private List<List<String>> mDataBase;
    private Node root;

    public C45Classifier(String dataBase){
        importDatabase(dataBase);

        List<Node> queue = new LinkedList<>();

        // Build the first node
        List<Integer> initialIndex = new ArrayList<>();
        List<Integer> initialAttribute = new ArrayList<>();
        for (int i = 0; i < mDataBase.size(); i++){
            initialIndex.add(i);
        }
        for (int i = 1; i < mDataBase.get(0).size(); i++){
            initialAttribute.add(i);
        }
        System.out.println(initialAttribute);
        root = new Node(initialAttribute, initialIndex);
        queue.add(root);

        // Build the tree iteratively
        while (!queue.isEmpty()){
            List<Node> newQueue = new LinkedList<>();
            for (Node node: queue){
                // Check for termination

                if (isHomogeneous(node.mDataIndex)){

                    // Check if all data are in same class
                    node.registerDecision(mDataBase.get(node.mDataIndex.get(0)).get(0));
                    System.out.print("Reached Leaf with decision: " + node.mDecision + ";");
                } else if (node.mAttributes.isEmpty()){

                    // Running out of attributes, majority voting
                    Map<String, Integer> map = new HashMap<>();
                    for (int index : node.mDataIndex){
                        String label = mDataBase.get(index).get(0);
                        if (map.containsKey(label)){
                            map.put(label, map.get(label) + 1);
                        } else{
                            map.put(label, 1);
                        }
                    }

                    Map.Entry<String, Integer> max = null;
                    for (Map.Entry<String, Integer> entry : map.entrySet()){
                        if (max == null){
                            max = entry;
                        } else if (entry.getValue() > max.getValue()){
                           max = entry;
                       }
                    }
                    node.registerDecision(max.getKey());
                    System.out.print("Majority voted and chose: " + max.getKey() + ";");
                } else{

                    // Continue building the tree
                    double maxGainRatio = -1;
                    int attribute = -1;
                    for (int feature : node.mAttributes){
                        double ratio = node.getGainRatio(feature);
                        if (ratio > maxGainRatio){
                            attribute = feature;
                            maxGainRatio = ratio;
                        }
                    }

                    newQueue.addAll(node.partitionBasedOnFeature(attribute));
                    System.out.print("Partitioned based on " + attribute + ";");
                }
            }
            queue = newQueue;
            System.out.println("");
        }
    }

    private boolean isHomogeneous(List<Integer> dataIndex){
        String label = mDataBase.get(dataIndex.get(0)).get(0);
        for (int i : dataIndex){
            if (!mDataBase.get(i).get(0).equals(label)){
                return false;
            }
        }
        return true;
    }

    /**
     * Parse Database into a 2 dimensional array
     * Assume each token is separated by space
     * @param dataPath the path to mDataBase File
     */
    private void importDatabase(String dataPath){
        mDataBase = new ArrayList<>();
        try {
            BufferedReader dataBase
                    = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataPath))));
            while (dataBase.ready()){
                List<String> dataLine = new ArrayList<>();
                String line = dataBase.readLine();
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                while (tokenizer.hasMoreElements()){
                    dataLine.add(tokenizer.nextToken());
                }
                this.mDataBase.add(dataLine);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void testData(String testPath, String outputPath){
        try {
            BufferedReader test
                    = new BufferedReader(new InputStreamReader(new FileInputStream(new File(testPath))));
            PrintStream out = new PrintStream(new File(outputPath));
            System.setOut(out);
            int correctCount = 0;
            int falseCount = 0;
            while (test.ready()){
                List<String> dataLine = new ArrayList<>();
                String line = test.readLine();
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                while (tokenizer.hasMoreElements()){
                    dataLine.add(tokenizer.nextToken());
                }
                String res = test(dataLine);
                System.out.println(res);
                if (res.equals(dataLine.get(0))){
                    correctCount++;
                } else {
                    falseCount++;
                }
            }
            System.out.println("Number of data tested: " + correctCount + falseCount);
            System.out.println("Accuracy: " + correctCount * 1.0 / (correctCount + falseCount));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String test(List<String> data){
        Node pointer = root;
        while (pointer.mDecision == null){
            pointer = pointer.children.get(data.get(pointer.mFeature));
        }
        // Should not reach this line
        return pointer.mDecision;
    }

    public static void main(String[] args){
        C45Classifier classifier = new C45Classifier(args[0]);
        classifier.testData(args[1], args[2]);
    }

    /**
     * A utility container class represent each node in Decision Tree
     */
    class Node{
        Map<String, Node> children;
        List<Integer> mDataIndex;
        int mFeature;
        String mDecision = null;
        List<Integer> mAttributes;

        Node(List<Integer> attributes, List<Integer> dataIndex){
            this.children = new HashMap<>();
            this.mDataIndex = new ArrayList<>(dataIndex);
            this.mAttributes = new ArrayList<>(attributes);
        }

        /**
         * Compute the gain ratio based on the following definition:
         * Gain Ratio(A, P) = (Info(A) - Info(A, P)) / SplitInfo(A, P)
         * @param feature The index of feature to be partition based upon
         * @return the Gain Ratio of such partition
         */
        double getGainRatio(int feature){
            // The entropy of original data
            double entropy = computeEntropy(mDataIndex);

            int N = mDataIndex.size();

            double partitionEntropy = 0;
            double splitInfo = 0;

            for (List<Integer> partition : getPartitions(feature).values()){
                double weight = partition.size() * 1.0 / N;
                partitionEntropy += weight * computeEntropy(partition);
                splitInfo += weight * logOf2(weight);
            }

            splitInfo *= -1;
            return (entropy - partitionEntropy) / splitInfo;
        }

        /**
         * Compute the Information Entropy of selected data based on the following definition
         * info(A) = -1 * sigma(pi * log2(pi))
         * @param dataIndex The list of index of data entries in the {@code mDataBase}
         * @return the Entropy of partitioned data
         */
        double computeEntropy(List<Integer> dataIndex){
            int N = dataIndex.size();
            Map<String, Integer> map = new HashMap<>();

            // Count the frequency of each class
            for (int i : dataIndex){
                String label = mDataBase.get(i).get(0);
                if (map.containsKey(label)){
                    map.put(label, map.get(label) + 1);
                } else{
                    map.put(label, 1);
                }
            }

            // Compute the data entropy
            double entropy = 0;
            for (Map.Entry<String, Integer> entry : map.entrySet()){
                double probability = entry.getValue() * 1.0 / N;
                entropy += probability * logOf2(probability);
            }

            return entropy * -1;
        }

        double logOf2(double a){
            return Math.log(a) / Math.log(2);
        }

        /**
         * Build the children nodes based on {@code feature}
         * Also register such decision in mFeature
         * @param feature The index of feature to be partition based upon
         * @return A list of children for the current Node
         */
        List<Node> partitionBasedOnFeature(int feature){
            this.mFeature = feature;
            Map<String, List<Integer>> partitions = getPartitions(feature);

            // Build the list of attributes
            List<Integer> newAttributes = new ArrayList<>(mAttributes);
            newAttributes.remove(new Integer(feature));

            // Build children
            for (Map.Entry<String, List<Integer>> entry : partitions.entrySet()){
                children.put(entry.getKey(), new Node(newAttributes, entry.getValue()));
            }

            return new ArrayList<>(children.values());
        }

        /**
         * Partition the current data indexes based on the feature
         * @param feature The index of feature to be partition based upon
         * @return a map indicating the partitions made
         */
        Map<String, List<Integer>> getPartitions(int feature){
            Map<String, List<Integer>> map = new HashMap<>();

            for (int index : mDataIndex){
                String featureLabel = mDataBase.get(index).get(feature);
                if (map.containsKey(featureLabel)){
                    map.get(featureLabel).add(index);
                } else{
                    map.put(featureLabel, new ArrayList<>());
                    map.get(featureLabel).add(index);
                }
            }
            return map;
        }

        void registerDecision(String decision){
            mDecision = decision;
        }
    }

}


