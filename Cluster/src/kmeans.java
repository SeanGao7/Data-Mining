import java.io.*;
import java.util.*;

/**
 * A java implementation of the k-means Classifier
 */
public class kmeans {
    List<List<Double>> mDataBase;
    List<Partition> clusters;

    /**
     * A constructor for the class, read the data from file and ran kmeans clustering
     * @param k Number of clusters
     * @param dataPath the path to data file
     */
    public kmeans(int k, String dataPath){
        mDataBase = importData(dataPath);

        // Main Algorithm for KMeans
        clusters = generateInitialPartitions(k);

        while (true){

            for (List<Double> point: mDataBase){
                Partition minPartition = null;
                double minDistance = Integer.MAX_VALUE;

                for (Partition partition : clusters){
                    double distance = computeDistance(point, partition.center);
                    if (distance < minDistance){
                        minDistance = distance;
                        minPartition = partition;
                    }
                }

                minPartition.addPoint(point, Math.pow(minDistance, 2));
            }

            List<Partition> newPartitions = new ArrayList<>();
            boolean changed = false;
            for (int i = 0; i < k; i++){
                List<Double> newCenter = computeCenter(clusters.get(i).points);
                if (!newCenter.equals(clusters.get(i).center)){
                    changed = true;
                }
                newPartitions.add(new Partition(newCenter));
            }
            if (!changed){
                break;
            }
            clusters = newPartitions;
        }
    }

    /**
     * Generate k random initial centroids by selecting k indexes from data entry
     * @param k the number of partitions
     * @return a list of centroids
     */
    private List<Partition> generateInitialPartitions(int k){
        List<Partition> clusters = new ArrayList<>();

        assert mDataBase.size() > 1;

        List<Integer> indexes = new ArrayList<>();
        List<Integer> res = new ArrayList<>();
        Map<Integer, Double> map = new HashMap<>();

        for (int i = 0 ; i < mDataBase.size(); i++){
            indexes.add(i);
            map.put(i, 0.0);
        }

        // Add the first element
        res.add(indexes.remove((int) (Math.random() * mDataBase.size())));

        for (int i = 0; i < k - 1; i ++) {
            int last = res.get(res.size() - 1);
            map.remove(last);

            for (int index: map.keySet()){
                map.put(index, map.get(index) + computeDistance(mDataBase.get(index), mDataBase.get(last)));
            }

            int index = -1;
            double distance = Integer.MIN_VALUE;

            for (Map.Entry<Integer, Double> entry: map.entrySet()){
                if (entry.getValue() > distance){
                    distance = entry.getValue();
                    index = entry.getKey();
                }
            }

            res.add(index);
        }

        for (int index: res){
            clusters.add(new Partition(mDataBase.get(index)));
        }

        return clusters;
    }

    /**
     * Compute the center of the given points by averaging their coordiantes
     * @param points a list of points to compute the center
     * @return the center computed
     */
    private static List<Double> computeCenter(List<List<Double>> points){
        assert points.size() > 1;
        double weight = 1.0 / points.size();
        int dimension = points.get(0).size();

        List<Double> center = new ArrayList<>(dimension);

        for (int i = 0; i < dimension; i++){
            center.add(0.0);
        }
        for (List<Double> point : points){
            for (int i = 0; i < dimension; i++){
                center.set(i, center.get(i) + weight * point.get(i));
            }
        }

        return center;
    }

    /**
     * Compute the euclidean distance between given points
     * @param pointA the first point
     * @param pointB the second point
     * @return the euclidean distance computed
     */
    private static double computeDistance(List<Double> pointA, List<Double> pointB){
        double squaredSum = 0;

        assert pointA.size() == pointB.size();

        for (int i = 0; i < pointA.size(); i++){
            squaredSum += Math.pow(pointA.get(i) - pointB.get(i), 2);
        }

        return Math.sqrt(squaredSum);
    }

    /**
     * Parse data into a list od list of doubles
     * Assume data are separated by comma, with each line represents one point
     * @param dataPath the path to the data file
     * @return a list points represented in list of doubles
     */
    private List<List<Double>> importData(String dataPath){
        List<List<Double>> res = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataPath))));
            while (reader.ready()){
                String set = reader.readLine();
                List<Double> line = new ArrayList<>();
                StringTokenizer tokenizer = new StringTokenizer(set, ",");
                while (tokenizer.hasMoreElements()){
                    String element = tokenizer.nextToken();
                    double token = Double.NaN;
                    try{
                        token = Double.parseDouble(element);
                    } catch (Exception ignored){

                    }
                    if (!Double.isNaN(token)){
                        line.add(token);
                    }
                }
                res.add(line);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        // Normalize tha data
        assert res.size() > 1;

        double[] min = new double[res.get(0).size()];
        double[] max = new double[res.get(0).size()];

        for (int i = 0; i < min.length; i++){
            min[i] = Integer.MAX_VALUE;
            max[i] = Integer.MIN_VALUE;
        }

        for (List<Double> list: res){
            for (int i = 0; i < list.size(); i++){
                min[i] = Math.min(min[i], list.get(i));
                max[i] = Math.max(max[i], list.get(i));
            }
        }

        for (List<Double> list: res){
            for (int i = 0; i < list.size(); i++){
                list.set(i, (list.get(i) - min[i]) / (max[i] - min[i]));
            }
        }

        return res;
    }

    /**
     * Write the output into the path specified and report the SSE
     * @param outputPath the path to the output file
     */
    public void output(String outputPath){
        try {
            PrintStream out = new PrintStream(new File(outputPath));
            System.setOut(out);
        } catch (Exception e){
            e.printStackTrace();
        }
        for (List<Double> lst: mDataBase){

            for (int i = 0; i < clusters.size(); i++){
                if (clusters.get(i).points.contains(lst)){
                    System.out.println(i);
                }
            }

        }

        double sum = 0;
        for (Partition p: clusters){
            sum += p.SSD;
        }
        System.out.print("SSE: ");
        System.out.println(sum);
    }

    public static void main(String[] args) {
        kmeans cluster = new kmeans(Integer.parseInt(args[1]), args[0]);
        cluster.output(args[2]);
    }

    /**
     * A container class represent each cluster
     */
    private static class Partition{
        List<Double> center;
        List<List<Double>> points;
        double SSD;

        Partition(List<Double> center){
            this.center = new ArrayList<>(center);
            points = new ArrayList<>();
            SSD = 0;
        }

        void addPoint(List<Double> point, double distance){
            points.add(point);
            SSD += distance;
        }

        @Override
        public boolean equals(Object object){
            if (object != null && object instanceof Partition
                    && ((Partition) object).center.size() == this.center.size()){
                for (int i = 0; i < this.center.size(); i++){
                    if (!this.center.get(i).equals(((Partition) object).center.get(i))){
                        return false;
                    }
                }
            } else{
                return false;
            }
            return true;
        }
    }
}
