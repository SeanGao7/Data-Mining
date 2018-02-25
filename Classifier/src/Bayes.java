import java.io.*;
import java.util.*;

/**
 * A Java implementation of the Naive Bayesian Classifier
 * Independence is assumed among all features
 */
public class Bayes {
    /*
        The first map in mFrequencies maps between the class and the count of the class
        The second map in mFrequencies maps between the conditional probability and the class
    */
    private Map<String, Integer>[] mFrequencies;
    private static int CLASS = 0;
    private static int DATA = 1;
    private int N;


    /**
     * Import the data from file and build the probability dictionary
     * @param dataPath the path to the data file
     */
    public Bayes(String dataPath){
        mFrequencies = new Map[2];
        mFrequencies[CLASS] = new HashMap<>();
        mFrequencies[DATA] = new HashMap<>();
        N = 0;
        importDatabase(dataPath);
    }

    /**
     * Assume each token is separated by tab
     * Counts the frequency of each conditional probabilities and prior
     * @param dataPath the path to mDataBase File
     */
    private void importDatabase(String dataPath){
        try {
            BufferedReader dataBase
                    = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataPath))));

            while (dataBase.ready()){
                String line = dataBase.readLine();
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");

                int feature = 0;
                String classLabel = "";
                while (tokenizer.hasMoreElements()){
                    String token = tokenizer.nextToken();
                    if (feature == 0){
                        addToMap(token, CLASS);
                        classLabel = token;
                    } else {
                        addToMap(hashProbabilities(classLabel, feature, token), DATA);
                    }

                    feature++;
                }
                N++;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Test the test data set and write the output
     * @param testPath the path to the test data file
     * @param outputPath the path to the output file
     */
    private void test(String testPath, String outputPath){
        try {
            BufferedReader testData
                    = new BufferedReader(new InputStreamReader(new FileInputStream(new File(testPath))));
            PrintStream out = new PrintStream(new File(outputPath));
            System.setOut(out);
            int correctCount = 0;
            int falseCount = 0;
            while (testData.ready()){
                String line = testData.readLine();
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");

                List<String> features = new ArrayList<>();
                while (tokenizer.hasMoreElements()){
                    features.add(tokenizer.nextToken());
                }

                String prediction = predict(features);
                System.out.println(prediction);

                if (prediction.equals(features.get(CLASS))){
                    correctCount++;
                } else {
                    falseCount++;
                }
            }
            System.out.println("Number of data tested: " + (correctCount + falseCount));
            System.out.println("Correct Labels generated: " + correctCount);
            System.out.println("False labels generated: " + falseCount);
            System.out.println("Accuracy: " + correctCount * 1.0 / (correctCount + falseCount));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Predict the class based on features using Bayes Theorem
     * @param features Features of the entity to be predicted
     * @return A string represents the prediction
     */
    private String predict(List<String> features){
        String res = "";
        double probability = 0;

        for (Map.Entry<String, Integer> entry: mFrequencies[CLASS].entrySet()){
            int counter = 1;
            double prob = 1;
            double prior = (double) entry.getValue() / N;

            while (counter < features.size()){
                String feature = features.get(counter);
                String hash = hashProbabilities(entry.getKey(), counter, feature);
                if (mFrequencies[DATA].containsKey(hash)){
                    prob *= (double) mFrequencies[DATA].get(hash) / (double) entry.getValue();
                } else{
                    prob *= 0;
                }
                counter++;
            }

            if (prob * prior > probability){
                res = entry.getKey();
                probability = prob * prior;
            }
        }
        return res;
    }

    /**
     * Hash a conditional probability into String
     * @param labelValue The value of the class
     * @param featureName The name of the feature
     * @param featureValue The value of the feature
     * @return A single string encapsulating all the information
     */
    private String hashProbabilities(String labelValue, int featureName, String featureValue){
        return featureName + "=" + featureValue + "|" + labelValue;
    }

    /**
     * Utility function to register an appearance of a conditional probabilities
     * @param key the conditional probability hashed in String
     */
    private void addToMap(String key, int index){
        if (mFrequencies[index].containsKey(key)){
            mFrequencies[index].put(key, mFrequencies[index].get(key) + 1);
        } else{
            mFrequencies[index].put(key, 1);
        }
    }

    /**
     * Main Function to be executed
     * @param args Has the following structure:
     *             1st argument: path to training data
     *             2nd argument: path to test data
     *             3rd argument: path to output
     */
    public static void main(String[] args){
        Bayes classifier = new Bayes(args[0]);
        classifier.test(args[1], args[2]);
    }
}