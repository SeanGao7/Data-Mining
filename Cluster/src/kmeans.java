import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class kmeans {
    List<List<Double>> mDataBase;
    List<Partition> clusters;

    public kmeans(int k, String dataPath){
        mDataBase = importData(dataPath);

        // Main Algorithm for KMeans
        clusters = generateInitialPartitions(k);

        while (true){
            System.out.println("Centers");
            for (Partition lst: clusters){
                System.out.println(lst.center);
            }

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

                minPartition.addPoint(point, minDistance);
            }

            System.out.println("Points");
            for (Partition lst: clusters){
                System.out.println(lst.points);
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

    private int[] generateKRandomIndexes(int k){
        int n = mDataBase.size();

        int[] indexes = new int[n];
        int[] res= new int[k];

        for (int i = 0; i < n; i++){
            indexes[i] = i;
        }

        for (int i = 0; i < k; i++){
            int index = (int) (Math.random() * (n - i));
            res[i] = indexes[index];
            indexes[index] = indexes[n - 1 -i];
        }

        return res;

    }

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

        /*
        int dimension = mDataBase.get(0).size();

        double[] max = new double[dimension];
        double[] min = new double[dimension];

        for (int i = 0; i < dimension; i++){
            max[i] = Integer.MIN_VALUE;
            min[i] = Integer.MAX_VALUE;
        }

        for (List<Double> point: mDataBase){
            for (int i = 0; i < dimension; i++){
                max[i] = Math.max(max[i], point.get(i));
                min[i] = Math.min(min[i], point.get(i));
            }
        }

        Set<List<Double>> set = new HashSet<>();
        Random r = new Random();

        for (int i = 0; i < k; i++){
            List<Double> list;
            do{
                list = new ArrayList<>();
                for (int j = 0; j < dimension; j++){
                    list.add(min[j] + (max[j] - min[j]) * r.nextDouble());
                }
            } while (set.contains(list));
            set.add(list);
        }

        for (List<Double> lst: set){
            clusters.add(new Partition(lst));
        }

        //


        for (int i : generateKRandomIndexes(k)){
            clusters.add(new Partition(mDataBase.get(i)));
        }
        */

        return clusters;
    }

    private static List<Double> computeCenter(List<List<Double>> points){
        assert points.size() > 1;
        double weight = 1.0 / points.size();
        System.out.println("Weights");
        System.out.println(points.size());
        System.out.println(weight);
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

    private static double computeDistance(List<Double> pointA, List<Double> pointB){
        double squaredSum = 0;

        assert pointA.size() == pointB.size();

        for (int i = 0; i < pointA.size(); i++){
            squaredSum += Math.pow(pointA.get(i) - pointB.get(i), 2);
        }

        return Math.sqrt(squaredSum);
    }

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
        for (List<Double> list: res){
            System.out.println(list);
        }
        return res;
    }

    public void output(String outputPath){
        int counter = 0;
        for (List<Double> lst: mDataBase){

            for (int i = 0; i < clusters.size(); i++){
                if (clusters.get(i).points.contains(lst)){
                    System.out.println(i);
                }
            }

            counter++;
        }

    }

    public static void main(String[] args){
        kmeans cluster = new kmeans(Integer.parseInt(args[1]), args[0]);
        cluster.output(args[2]);
    }

    private static class Partition{
        List<Double> center;
        List<List<Double>> points;
        double SSD;

        Partition(List<Double> center){
            this.center = new ArrayList<>(center);
            points = new ArrayList<>();
            SSD = 0;
        }

        double getSSD(){
            return SSD;
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
