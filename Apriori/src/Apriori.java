/**
 * A Java implementation of the Apriori Aglorithm
 * Source dataSet shoudl be in the following format:
 * 1. Each line is considered as a transaction
 * 2. Each item in transaction is seperated by a space
 * 3. Assume each transaction is sorted
 */
import java.io.*;
import java.util.*;

public class Apriori {
    /* Minimum support for frequent item set */
    private int mThreshold;

    /* The cached item set from last computation (last call of generateFIS) */
    private Map<List<Integer>, Integer> mItemSet;

    /* Path for the source data file */
    private String mDataFile;

    /* Number of transactions */
    private int numOfTransactions;

    /* The max number of size of transaction that met min support*/
    private int maxSize = 0;

    /* Number of transactions */
    private List<List<Integer>> oneDimensionFIS;

    /**
     * Constructor for the class
     * Also prepares frequent item set of size 1 and counts the number of transactions
     * @param dataPath the path to the .dat file
     * @param supportThreshold the threshold for support count
     */
    public Apriori(String dataPath, int supportThreshold){
        mThreshold = supportThreshold;
        mDataFile = dataPath;
        mItemSet = new HashMap<>();

        // Construct a list of one item sets
        Map<Integer, Integer> atomicFIS = new HashMap<>();
        Map<Integer, Integer> sizeFrequency = new HashMap<>();
        numOfTransactions = 0;

        try {
            BufferedReader dataBase
                    = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mDataFile))));
            while (dataBase.ready()){
                String transaction = dataBase.readLine();
                StringTokenizer tokenizer = new StringTokenizer(transaction, " ");

                // Count of the number of items in this transaction
                int count = 0;

                while(tokenizer.hasMoreElements()){
                    int item = Integer.parseInt(tokenizer.nextToken());

                    if (atomicFIS.containsKey(item)){
                        atomicFIS.put(item, atomicFIS.get(item) + 1);
                    } else{
                        atomicFIS.put(item, 1);
                    }

                    count ++;
                }

                if (sizeFrequency.containsKey(count)){
                    sizeFrequency.put(count, sizeFrequency.get(count) + 1);
                } else{
                    sizeFrequency.put(count, 1);
                }

                numOfTransactions++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        oneDimensionFIS = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : atomicFIS.entrySet()){
            if (entry.getValue() >= mThreshold){
                List<Integer> temp = new ArrayList<>();
                temp.add(entry.getKey());
                oneDimensionFIS.add(temp);

                // Put the Set - frequency entry in result
                mItemSet.put(temp, entry.getValue());
            }
        }

        for (Map.Entry<Integer, Integer> entry : sizeFrequency.entrySet()){
            if (entry.getValue() >= mThreshold){
                maxSize = maxSize > entry.getKey() ? maxSize : entry.getKey();
            }
        }
    }

    /**
     * Generates new candidate for frequent item set of size k based on the frequent item set of size k - 1
     * by merging each pair of FIS of size k - 1 and selects those have appeared k times (have all subsets frequent)
     * The sorted ordering of items in each set is maintained
     * @param lastCandidates the frequent item set of size k - 1
     * @return the new list of candidates
     */
    private List<List<Integer>> buildNewCandidates(List<List<Integer>> lastCandidates){
        Map<List<Integer>, Integer> frequency = new HashMap<>();

        // Set the threshold to be k
        int threshold = lastCandidates.get(0).size() + 1;

        // Creates new candidates by merging the previous sets
        for (int i = 0; i < lastCandidates.size(); i++){
            for (int j = i + 1; j < lastCandidates.size(); j++){
                List<Integer> candidate = new ArrayList<>();

                int firstIndex = 0;
                List<Integer> firstSet = lastCandidates.get(i);
                int secondIndex = 0;
                List<Integer> secondSet = lastCandidates.get(j);

                while ((firstIndex < firstSet.size() || secondIndex < secondSet.size())
                        && candidate.size() < threshold){
                    if (firstIndex == firstSet.size()){
                        // The end of first set is reached
                        candidate.add(secondSet.get(secondIndex++));

                    } else if (secondIndex == secondSet.size()){
                        // The end of second set is reached
                        candidate.add(firstSet.get(firstIndex++));

                    } else {
                        // Both sets still have candidates
                        if (firstSet.get(firstIndex).equals(secondSet.get(secondIndex))){
                            candidate.add(firstSet.get(firstIndex++));
                            secondIndex++;
                        } else if (firstSet.get(firstIndex) < secondSet.get(secondIndex)){
                            candidate.add(firstSet.get(firstIndex++));
                        } else{
                            candidate.add(secondSet.get(secondIndex++));
                        }
                    }
                }

                if (firstIndex == firstSet.size() || secondIndex == secondSet.size()){
                    // This is a valid candidate (contains all elements from first / second set)
                    if (frequency.containsKey(candidate)){
                        frequency.put(candidate, frequency.get(candidate) + 1);
                    } else{
                        frequency.put(candidate, 1);
                    }
                }
            }
        }

        List<List<Integer>> res = new ArrayList<>();
        threshold = threshold == 2 ? 1 : threshold;
        for (Map.Entry<List<Integer>, Integer> entry: frequency.entrySet()){
            // Prune the candidates which does not have all subsets being frequent
            if (entry.getValue() >= threshold){
                res.add(entry.getKey());
            }
        }

        return res;
    }

    private void combinationUtil(List<Integer> data, int k, List<Integer> temp, int index, List<List<Integer>> res){
        if (temp.size() == k){
            res.add(new ArrayList<>(temp));
            return;
        }
        if (index >= data.size()){
            return;
        }
        temp.add(data.get(index));
        combinationUtil(data, k, temp, index + 1, res);
        temp.remove(temp.size() - 1);
        combinationUtil(data, k, temp, index + 1, res);
    }

    private List<List<Integer>> subsetOfSizeK(List<Integer> originalSet, int k){
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        combinationUtil(originalSet, k, temp, 0, res);
        return res;
    }

    private List<List<Integer>> validateCandidates(List<List<Integer>> candidates){
        int k = candidates.get(0).size();

        Map<List<Integer>, Integer> frequency = new HashMap<>();
        for (List<Integer> i : candidates){
            frequency.put(i, 0);
        }

        try {
            BufferedReader dataBase
                    = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mDataFile))));
            while (dataBase.ready()) {
                String line = dataBase.readLine();
                StringTokenizer tokenizer = new StringTokenizer(line, " ");
                List<Integer> transaction = new ArrayList<Integer>();
                while(tokenizer.hasMoreElements()){
                    transaction.add(Integer.parseInt(tokenizer.nextToken()));
                }

                if (transaction.size() < k){
                    // Skip since this transaction is too short
                    continue;
                }

                // Finds all subset of size k for this transaction
                List<List<Integer>> subsets = subsetOfSizeK(transaction, k);
                for (List<Integer> set : subsets){
                    if (frequency.containsKey(set)){
                        frequency.put(set, frequency.get(set) + 1);
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        List<List<Integer>> res = new ArrayList<>();
        for (Map.Entry<List<Integer>, Integer> entry : frequency.entrySet()){
            if (entry.getValue() >= mThreshold){
                res.add(entry.getKey());

                // Put the Set - frequency entry in result
                mItemSet.put(entry.getKey(), entry.getValue());
            }
        }

        return res;
    }

    /**
     * Run the Apriori algorithm to compute the frequent item set
     */
    public void generateFIS(){
        List<List<Integer>> lastFIS = oneDimensionFIS;

        for (int k = 2; k <= maxSize; k++){
            List<List<Integer>> candidates = buildNewCandidates(lastFIS);
            if (candidates.size() == 0){
                break;
            }
            List<List<Integer>> FIS = validateCandidates(candidates);
            if (FIS.size() == 0){
                break;
            }
            lastFIS = FIS;
        }

        for (Map.Entry<List<Integer>,Integer> entry : mItemSet.entrySet()){
            for (int i : entry.getKey()){
                System.out.print(i + " ");
            }
            System.out.print("(" + entry.getValue() + ")\n");
        }
    }

    /**
     * Write the frequent item data sets to the specified output path
     */
    private static void writeToOutput(String path){

    }

    /**
     * Main function to be executed
     * @param args 1st argument as the path to data file
     *             2nd argument as the support count thershold
     *             3rd argument as the path to output file
     */
    public static void main(String[] args){
        if (args.length < 3){
            System.out.println("Please specify data path, support count and output path");
            return;
        }
        int supportThreshold = Integer.parseInt(args[1]);
        Apriori apriori = new Apriori(args[0], supportThreshold);


        //Unit test for mergeSets
        List<List<Integer>> input = new ArrayList<>();

        List<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);

        List<Integer> b = new ArrayList<>();
        b.add(2);
        b.add(3);
        b.add(4);

        List<Integer> c = new ArrayList<>();
        c.add(1);
        c.add(2);
        c.add(4);

        List<Integer> d = new ArrayList<>();
        d.add(1);
        d.add(4);
        d.add(5);

        List<Integer> e = new ArrayList<>();
        e.add(1);
        e.add(3);
        e.add(4);

        input.add(a);
        input.add(b);
        input.add(c);
        input.add(d);
        input.add(e);

        List<List<Integer>> output = apriori.buildNewCandidates(input);


        /*
        List<Integer> input = new ArrayList<>();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add(5);

        List<List<Integer>> output = apriori.subsetOfSizeK(input, 3);
        */
        for (List<Integer> l : output){
            for (int i : l){
                System.out.println(i);
            }
            System.out.println();
        }

        apriori.generateFIS();
        writeToOutput(args[2]);
    }
}
