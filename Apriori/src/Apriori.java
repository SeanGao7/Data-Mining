/*
  A Java implementation of the Apriori Algorithm
  Source dataSet should be in the following format:
  1. Each line is considered as a transaction
  2. Each item in transaction is separated by a space
  3. Assume each transaction is sorted
*/
import java.io.*;
import java.util.*;

public class Apriori {
    private static final int SWITCH = 20;

    /* Minimum support for frequent item set */
    private int mThreshold;

    /* The cached item set from last computation (last call of generateFIS) */
    private List<String> mItemSet;

    /* The Frequent Item Set of Size 1 from constructor */
    private List<int[]> oneDimensionFIS;

    /* The database read from file in the form of List */
    private List<List<Integer>> mDataList;

    /* The database read from file in the form of Set */
    //private List<Set<Integer>> mDataSet;

    /* The lines to be skipped */
    private Set<Integer> skipLines;


    /* Which validate method to use */
    private boolean useList;


    /**
     * Constructor for the class
     * Also prepares frequent item set of size 1 and counts the number of transactions
     * @param dataPath the path to the .dat file
     * @param supportThreshold the threshold for support frequency
     */
    public Apriori(String dataPath, int supportThreshold){
        mThreshold = supportThreshold;
        mItemSet = new LinkedList<>();
        mDataList = new ArrayList<>();
        skipLines = new HashSet<>();
       // mDataSet = new ArrayList<>();

        // Construct a list of one item sets
        Map<Integer, Integer> atomicFIS = new HashMap<>();
        Map<Integer, Integer> sizeFrequency = new HashMap<>();

        try {
            BufferedReader dataBase
                    = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataPath))));
            int lineCount = 0;
            int wordCount = 0;
            while (dataBase.ready()){
                String transaction = dataBase.readLine();
                StringTokenizer tokenizer = new StringTokenizer(transaction, " ");
                List<Integer> line = new ArrayList<>();
                Set<Integer> lineSet = new HashSet<>();

                // Count of the number of items in this transaction
                int count = 0;

                while(tokenizer.hasMoreElements()){
                    int item = Integer.parseInt(tokenizer.nextToken());
                    line.add(item);
                    lineSet.add(item);
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
                mDataList.add(line);
                //mDataSet.add(lineSet);
                wordCount+=count;
                lineCount++;
            }
            dataBase.close();
            useList = wordCount / lineCount <= SWITCH;
        } catch (IOException e) {
            e.printStackTrace();
        }

        oneDimensionFIS = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : atomicFIS.entrySet()){
            if (entry.getValue() >= mThreshold){
                int[] temp = new int[1];
                temp[0] = entry.getKey();
                oneDimensionFIS.add(temp);
                // Put the Set - frequency entry in result
                mItemSet.add(entry.getKey() + "(" + entry.getValue() + ")\n");
            }
        }
    }

    /**
     * Merge two set of size k - 1 to produce a super set of size k
     * @param firstSet the first set to be merged
     * @param secondSet the second set to be merged
     * @return null if merged set > k, otherwise the merged set of size k
     */
    private int[] mergeTwoSet(int[] firstSet, int[] secondSet){
        int threshold = firstSet.length + 1;

        int[] candidate = new int[threshold];

        int firstIndex = 0;
        int secondIndex = 0;
        int index = 0;

        while ((firstIndex < firstSet.length || secondIndex < secondSet.length) && index < threshold){
            if (firstIndex == firstSet.length){
                // The end of first set is reached
                candidate[index] = secondSet[secondIndex++];

            } else if (secondIndex == secondSet.length){
                // The end of second set is reached
                candidate[index] = firstSet[firstIndex++];
            } else {
                // Both sets still have candidates
                if (firstSet[firstIndex] == secondSet[secondIndex]){
                    candidate[index] = firstSet[firstIndex++];
                    secondIndex++;
                } else if (firstSet[firstIndex] < secondSet[secondIndex]){
                    candidate[index] = firstSet[firstIndex++];
                } else{
                    candidate[index] = secondSet[secondIndex++];
                }
            }
            index++;
        }

        if (firstIndex == firstSet.length && secondIndex == secondSet.length){
            // This is a valid candidate (contains all elements from first / second set)
            return candidate;
        }

        return null;
    }

    /**
     * Generates new candidate for frequent item set of size k based on the frequent item set of size k - 1
     * by merging each pair of FIS of size k - 1 and selects those have appeared k times (have all subsets frequent)
     * The sorted ordering of items in each set is maintained
     * @param lastCandidates the frequent item set of size k - 1
     * @return the new list of candidates
     */
    private List<int[]> buildNewCandidates(List<int[]> lastCandidates){
        Map<String, SetWithFrequency> frequency = new HashMap<>();

        // Set the threshold to be k
        int threshold = lastCandidates.get(0).length + 1;

        // Creates new candidates by merging the previous sets
        for (int i = 0; i < lastCandidates.size(); i++){
            for (int j = i + 1; j < lastCandidates.size(); j++){
                int[] firstSet = lastCandidates.get(i);
                int[] secondSet = lastCandidates.get(j);

                int[] candidate = mergeTwoSet(firstSet, secondSet);

                if (candidate != null){

                    // This is a valid candidate (contains all elements from first / second set)
                    String key = arrayToString(candidate);

                    if (frequency.containsKey(key)){
                        frequency.get(key).frequency++;
                    } else{
                        frequency.put(key, new SetWithFrequency(key, candidate, 1));
                    }
                }
            }
        }

        List<int[]> res = new ArrayList<>();
        threshold = threshold == 2 ? 1 : threshold;
        for (SetWithFrequency entry: frequency.values()){
            // Prune the candidates which does not have all subsets being frequent
            if (entry.frequency == threshold){
                res.add(entry.set);
            }
        }

        return res;
    }

    private void combinationUtil(List<Integer> data, int k, List<Integer> temp, int index, List<List<Integer>> res){
        if (temp.size() == k){
            res.add(new ArrayList<>(temp));
            return;
        }
        for(int i = index; i < data.size(); i++){
            temp.add(data.get(i));
            combinationUtil(data, k, temp, i + 1, res);
            temp.remove(temp.size() - 1);
        }
    }

    private List<List<Integer>> subsetOfSizeK(List<Integer> originalSet, int k){
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> temp = new ArrayList<>(k);
        combinationUtil(originalSet, k, temp, 0, res);
        return res;
    }

    // This method is good when the data base has long entries
    private List<int[]> validateCandidatesWithSet(List<int[]> candidates) {
        List<SetWithFrequency> frequency = new LinkedList<>();

        for (int[] candidate : candidates){
            String key = arrayToString(candidate);
            frequency.add(new SetWithFrequency(key, candidate, 0));
        }

        for (int i = 0; i < mDataList.size(); i++){
            if (skipLines.contains(i)){
                continue;
            }
            boolean empty = true;
            Set<Integer> line = new HashSet<>();
            for (int num : mDataList.get(i)){
                line.add(num);
            }
            for (SetWithFrequency candidate : frequency){
                // Check if candidate is subset
                boolean isSubset = true;
                for (int n : candidate.set){
                    if (!line.contains(n)){
                        isSubset = false;
                        break;
                    }
                }
                if (isSubset){
                    empty = false;
                    candidate.frequency++;
                }
            }
            if (empty){
                skipLines.add(i);
            }
        }

        List<int[]> res = new ArrayList<>();
        for (SetWithFrequency entry : frequency){
            if (entry.frequency >= mThreshold){
                res.add(entry.set);

                // Put the Set - frequency entry in result
                mItemSet.add(entry.key + "(" + entry.frequency + ")\n");
            }
        }

        return res;
    }

    // This method is good when the data base has relatively short
    private List<int[]> validateCandidatesWithList(List<int[]> candidates){
        int size = candidates.get(0).length;

        Map<String, SetWithFrequency> frequency = new HashMap<>();
        for (int[] candidate : candidates){
            String key = arrayToString(candidate);
            frequency.put(key, new SetWithFrequency(key, candidate, 0));
        }


        // Filtering the database using the items appeared frequent in FIS of size k - 1

        Set<Integer> dict = new HashSet<>();
        for (int[] list : candidates){
            for (int num : list){
                dict.add(num);
            }
        }

        for (int i = 0; i < mDataList.size(); i++) {
            if (skipLines.contains(i)){
                continue;
            }

            List<Integer> transaction = mDataList.get(i);

            // Remove the item not in dictionary (still frequent set)
            transaction.removeIf(p -> !dict.contains(p));

            if (transaction.size() < size){
                skipLines.add(i);
                continue;
            }

            // Finds all subset of size k for this transaction

            List<List<Integer>> subsets = subsetOfSizeK(transaction, size);

            boolean empty = true;

            for (List<Integer> subset : subsets){
                String key = listToString(subset);
                if (frequency.containsKey(key)){
                    empty = false;
                    frequency.get(key).frequency++;
                }
            }

            if (empty){
                skipLines.add(i);
            }
        }


        List<int[]> res = new ArrayList<>();
        for (SetWithFrequency entry : frequency.values()){
            if (entry.frequency >= mThreshold){
                res.add(entry.set);

                // Put the Set - frequency entry in result
                mItemSet.add(entry.key + "(" + entry.frequency + ")\n");
            }
        }

        return res;
    }

    /**
     * Run the Apriori algorithm to compute the frequent item set
     */
    public void generateFIS(){
        List<int[]> lastFIS = oneDimensionFIS;
        int n = 2;
        while (true){
            long t = System.nanoTime();
            List<int[]> candidates = buildNewCandidates(lastFIS);
            //System.out.println("Build new candidates: " + (System.nanoTime() - t) / 1000000000.0);
            //System.out.println("candidates: " + candidates.size());
            if (candidates.size() == 0){
                break;
            }
            t = System.nanoTime();
            List<int[]> FIS;
            if (useList){
                FIS = validateCandidatesWithList(candidates);
            } else{
                FIS = validateCandidatesWithSet(candidates);
            }
            //System.out.println("Validate: " + (System.nanoTime() - t) / 1000000000.0);
            //System.out.println("New FIS: " + FIS.size());
            if (FIS.size() == 0){
                break;
            }
            lastFIS = FIS;
            n++;
        }
    }

    private String arrayToString(int[] list){
        StringBuilder res = new StringBuilder();
        for (int i : list){
            res.append(i).append(" ");
        }
        return res.toString();
    }

    private String listToString(List<Integer> list){
        StringBuilder res = new StringBuilder();
        for (int i : list){
            res.append(i).append(" ");
        }
        return res.toString();
    }

    /**
     * Write the frequent item data sets to the specified output path
     * @param path The path to output file
     * @param apriori The Apriori instance constructed
     */
    private static void writeToOutput(String path, Apriori apriori){
        try{
            BufferedWriter output = new BufferedWriter(new FileWriter(path, false));
            for (String s : apriori.mItemSet){
                output.write(s);
            }
            output.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Since using List as key is expensive, To better utilize the efficiency of HashMap and the flexibility of array
     * This data structure is designed as a container both for easy hashing.
     */
    private class SetWithFrequency {
        String key;
        int[] set;
        int frequency;

        SetWithFrequency(String key, int[] set, int frequency){
            this.key = key;
            this.set = set;
            this.frequency = frequency;
        }
    }

    /**
     * Main function to be executed
     * @param args 1st argument as the path to data file
     *             2nd argument as the support frequency thershold
     *             3rd argument as the path to output file
     */
    public static void main(String[] args){
        if (args.length < 3){
            System.out.println("Please specify data path, support frequency and output path");
            return;
        }
        int supportThreshold = Integer.parseInt(args[1]);
        long startTime = System.nanoTime();

        Apriori apriori = new Apriori(args[0], supportThreshold);

        apriori.generateFIS();
        writeToOutput(args[2], apriori);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Run Time: " + duration / 1000000000.0);
    }
}

