package mongoDb;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;
import mongoDb.domains.ChineseStockMarket;
import mongoDb.domains.ICollection;
import mongoDb.domains.USStockMarket;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.Date;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by zhuol on 2017/7/4.
 */
public class MongoDBConnector {
    public static final String DB_NAME = "stockdb";
    private static String con = "mongodb://stockdbzhuoli:QrLUUzcspLOK2pjdEvVlevms5zCfvhQlChWOtrLVRI1r5HF1mKwAKwwFm296SBSWLoOPnAQ8apN8zaPPYA3inQ==@stockdbzhuoli.documents.azure.com:10255/?ssl=true&replicaSet=globaldb";
    MongoClient mongoClient = null;
    private MongoDatabase mongoDatabase;

    public void connect(){
        this.mongoClient = new MongoClient(new MongoClientURI(MongoDBConnector.con));
        this.mongoDatabase = this.mongoClient.getDatabase(DB_NAME);
        ChineseStockMarket stockCompany = ChineseStockMarket.builder()._id("sz00001").companyName("平安银行").currentPrice(123.5).lastUpdatedTime(new Date()).build();
        USStockMarket usCompany = USStockMarket.builder()._id("amzn").companyName("Amazon").currentPrice(985).lastUpdatedTime(new Date()).build();
        this.insertDocument(ChineseStockMarket.TABLE_NAME, stockCompany);
        this.insertDocument(USStockMarket.TABLE_NAME, usCompany);
    }

    public synchronized <T extends ICollection> void insertDocument(String collectionname, T... records){
        MongoCollection chineseStockTable = this.mongoDatabase.getCollection(collectionname);
        Gson gson = new Gson();
        for(T record : records){
            String json = gson.toJson(record);
            Document doc = Document.parse(json);
            try {
                chineseStockTable.insertOne(doc);
            }catch (MongoWriteException exception){
                chineseStockTable.updateOne(eq("_id", record._id), new Document("$set",doc));
            }
        }
    }
}
