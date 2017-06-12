package dataEngineer.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;

import java.util.Set;

/**
 * Created by zhuolil on 6/12/17.
 */
@AllArgsConstructor
@Data
public class WebParserData {

    private String stockId;

    public static int moveUnsearchedDataAhead(Set<String> updatedStockIdSet, WebParserData[] array){

        Assert.assertNotNull(updatedStockIdSet);
        Assert.assertNotNull(array);

        int nextSeenIdx = array.length - 1;

        // Sort company array so that those unsearched company moved to head of array and those
        // companies already in databaes moved to tail.
        for (int idx = 0; idx <= nextSeenIdx; idx++) {
            // If current stock is seen in records
            if (updatedStockIdSet.contains(array[idx].stockId)) {
                // Move this stock to tail
                WebParserData tmp = array[nextSeenIdx];
                array[nextSeenIdx] = array[idx];
                array[idx] = tmp;
                nextSeenIdx--;
                idx--;
            }
        }

        return nextSeenIdx;
    }


    public static int moveUnsearchedItemAhead(Set<String> searchedIds, String[] toSearchStockIdArray){

        Assert.assertNotNull(searchedIds);
        Assert.assertNotNull(toSearchStockIdArray);

        int nextSeenIdx = toSearchStockIdArray.length - 1;

        // Sort company array so that those unsearched company moved to head of array and those
        // companies already in databaes moved to tail.
        for (int idx = 0; idx <= nextSeenIdx; idx++) {
            // If current stock is seen in records
            if (searchedIds.contains(toSearchStockIdArray[idx])) {
                // Move this stock to tail
                String tmp = toSearchStockIdArray[nextSeenIdx];
                toSearchStockIdArray[nextSeenIdx] = toSearchStockIdArray[idx];
                toSearchStockIdArray[idx] = tmp;
                nextSeenIdx--;
                idx--;
            }
        }
        return nextSeenIdx;
    }
}
