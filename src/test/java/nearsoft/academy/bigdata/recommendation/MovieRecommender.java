package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.FileWriter;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.zip.GZIPInputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

class MovieRecommender {
    private ArrayList<Double> reviews = new ArrayList<Double>();
    private ArrayList<String> users = new ArrayList<String>();
    private ArrayList<String> products = new ArrayList<String>();

    private Map<String,Integer> usersMap = new HashMap<String, Integer>();
    private Map<String, Integer> productsMap = new HashMap<String, Integer>();


    MovieRecommender(String path){
        this.readTxtFile(path);
    }


    private void readTxtFile(String path){
        String userKey = "review/userId: ";
        String productKey = "product/productId: ";
        String reviewKey = "review/score: ";

        InputStream fileStream;

        try{
            fileStream = new FileInputStream(path);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
            BufferedReader buffered = new BufferedReader(decoder);

            int usersCounter = 1;
            int productsCounter = 1;
            String content;

            while ((content = buffered.readLine()) != null){
                if (content.contains(userKey)){
                    String userId = content.split(userKey)[1];
                    users.add(userId);
                    if(!this.usersMap.containsKey(userId)){
                        this.usersMap.put(userId, usersCounter);
                        usersCounter++;
                    }
                }
                else if(content.contains(productKey)){
                    String productId = content.split(productKey)[1];
                    products.add(productId);
                    if(!this.productsMap.containsKey(productId)){
                        this.productsMap.put(productId, productsCounter);
                        productsCounter++;
                    }
                }
                else if(content.contains(reviewKey)){
                    String scoreValue = content.split(reviewKey)[1];
                    reviews.add(Double.valueOf(scoreValue));
                }
            }

            System.out.println("users: " + usersMap.size() + " products: " + productsMap.size() + " reviews: " + reviews.size());
            this.writeCsv();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    int getTotalReviews(){
        return reviews.size();
    }

    int getTotalProducts(){
        return productsMap.size();
    }

    int getTotalUsers(){
        return usersMap.size();
    }


    private void writeCsv(){
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("movies.csv");

            for(int i = 0; i < this.users.size()-1; i++){
                int userId = this.usersMap.get(this.users.get(i));
                fileWriter.append(String.valueOf(userId));
                fileWriter.append(",");

                int productId = this.productsMap.get(this.products.get(i));
                fileWriter.append(String.valueOf(productId));
                fileWriter.append(",");

                fileWriter.append(String.valueOf(this.reviews.get(i)));

                fileWriter.append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert fileWriter != null;
                fileWriter.flush();
                fileWriter.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    List<String> getRecommendationsForUser(String userId) {
        List<String> recommended = new ArrayList<String>();
        List<RecommendedItem> recommendations = null;

        try {
            DataModel model = new FileDataModel(new File("movies.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            recommendations = recommender.recommend(usersMap.get(userId), 3);

        } catch (TasteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert recommendations != null;
        for (RecommendedItem recommendation : recommendations) {
            String value = getKey((int)recommendation.getItemID());
            recommended.add(value);
        }

        return recommended;

    }

    private String getKey(int value) {
        for (String key : productsMap.keySet()){
            if(productsMap.get(key) == value){
                return key;
            }
        }
        return null;
    }
}

