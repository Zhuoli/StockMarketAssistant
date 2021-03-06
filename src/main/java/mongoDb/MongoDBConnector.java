package mongoDb;

import com.google.gson.Gson;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import dataEngineer.data.FinancialData;
import dataEngineer.data.SharesQuote;
import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by zhuol on 2017/7/4.
 */
public class MongoDBConnector {
    public static final String DB_NAME = "stockdb";
    private static String con = "mongodb://zhuoli:tGVT}77mU)xHqB@stockmarket-shard-00-00-bcpbx.mongodb.net:27017,stockmarket-shard-00-01-bcpbx.mongodb.net:27017,stockmarket-shard-00-02-bcpbx.mongodb.net:27017/stockdb?ssl=true&replicaSet=stockmarket-shard-0&authSource=admin";
    public static final String CHINESE_TABLE = "chinesestockmarket";
    public static final String US_TABLE = "usstockcompany";
    MongoClient mongoClient = null;
    private MongoDatabase mongoDatabase;

    public void connect(){
        this.mongoClient = new MongoClient(new MongoClientURI(MongoDBConnector.con));
        this.mongoDatabase = this.mongoClient.getDatabase(DB_NAME);
        Assert.assertNotNull("Could not find the giving database: " + DB_NAME, this.mongoDatabase);
    }

    public void test(){
        this.connect();
        SharesQuote stockCompany = SharesQuote
                .builder()
                    ._id("sz000001")
                .companyname("平安银行")
                .currentPrice("123.5")
                .lastUpdatedTime(new Date())
                .financialData(FinancialData.builder().grossMargin(31).reporturl("www.qq.com").roe(10).build())
                .build();
        SharesQuote usCompany = SharesQuote.builder()._id("amzn").companyname("Amazon").currentPrice("985").lastUpdatedTime(new Date()).build();
        this.insertDocument(MongoDBConnector.CHINESE_TABLE ,stockCompany);
        this.insertDocument(MongoDBConnector.US_TABLE, usCompany);
        List<SharesQuote> stockMarkets = this.retrieveDcouments(MongoDBConnector.CHINESE_TABLE);
    }

    public void close(){
        this.mongoClient.close();
    }

    public synchronized void insertDocument( String tableName, SharesQuote... records){
        MongoCollection chineseStockTable = this.mongoDatabase.getCollection(tableName);
        Gson gson = new Gson();
        for(SharesQuote record : records){
            Assert.assertNotNull("stock Id should not be null", record.get_id());
            Assert.assertNotNull("company name should not be null", record.getCompanyname());

            String json = gson.toJson(record);
            Document doc = Document.parse(json);
            try {
                chineseStockTable.insertOne(doc);
            }catch (MongoWriteException exception){
                chineseStockTable.updateOne(eq("_id", record.get_id()), new Document("$set",doc));
            }
        }
    }

    public  List<SharesQuote> retrieveDcouments(String tablename){
        ArrayList<SharesQuote> result = new ArrayList<>();
        Gson gson = new Gson();
        MongoCollection chineseStockTable = this.mongoDatabase.getCollection(tablename);
        FindIterable<Document> queryResult = chineseStockTable.find();
        for (Document document : queryResult) {
            SharesQuote sharesQuote = gson.fromJson(document.toJson(), SharesQuote.class);
            result.add(sharesQuote);
        }
        return result;
    }
}
