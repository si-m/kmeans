
import java.io.IOException;
import java.util.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class KMeansReducer extends Reducer <Text, Text, Text, Text> {
  
  // this is the list of clusters we are creating
  private ArrayList <VectorizedObject> newClusters = new ArrayList <VectorizedObject> (0);
  
  public void reduce (Text key, Iterable <Text> values, Context context) throws IOException, InterruptedException {
    
    // combine all of the info on each cluster 
    VectorizedObject curCluster = null;
    for (Text i : values) {
      
      if (curCluster == null) {
        curCluster = new VectorizedObject (i.toString ());
      } else {
        VectorizedObject oneIJustRead = new VectorizedObject (i.toString ());
        oneIJustRead.getLocation ().addMyselfToHim (curCluster.getLocation ());
        curCluster.addToValueAsInt (oneIJustRead.getValueAsInt ());
      } 
    }
    
    newClusters.add (curCluster);
  }
  
  protected void cleanup (Context context) throws IOException, InterruptedException {
    
    // this is all of the stuff to spit a cluster that is too big
    //
    // loop through all of the clusters, finding the one with the most points, and the one with the fewest
    int bigIndex = -1, big = 0;
    int smallIndex = -1, small = 999999999;
    int curIndex = 0;
    for (VectorizedObject i : newClusters) {
      if (i.getValueAsInt () < small) {
        small = i.getValueAsInt ();
        smallIndex = curIndex;
      }
      if (i.getValueAsInt () > big) {
        big = i.getValueAsInt ();
        bigIndex = curIndex;
      }
      curIndex++;
    }
    
    // if the bg one is less than 1/20 the size of the small one, then split the big one and use
    // it to replace the small one
    if (small < big / 20) {
      String temp = newClusters.get (bigIndex).writeOut ();
      VectorizedObject newObj = new VectorizedObject (temp);
      newObj.setKey (newClusters.get (smallIndex).getKey ());
      newObj.getLocation ().multiplyMyselfByHim (1.00000001);
      newClusters.set (smallIndex, newObj);
    }
    
    // write the clusters to the output
    for (VectorizedObject i : newClusters) {
      Text key = new Text ();
      Text value = new Text ();
      key.set ("");
      i.getLocation ().multiplyMyselfByHim (1.0 / i.getValueAsInt ());
      value.set (i.writeOut ());
      context.write (key, value);
    } 
  }
}

