
import java.util.*;

import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import java.net.URI;

public class KMeans {

  static void printUsage() {
    System.out.println ("KMeans <input> <clusterFileDirectory> <numIters>");
    System.exit(-1);
  }

  public static int main (String [] args) throws Exception {

    // if we have the wrong number of args, then exit
    if (args.length != 3) {
      printUsage ();
      return 1;
    }

    // repeate the whole thing the correct number of times
    for (int i = 0; i < Integer.parseInt (args[2]); i++) {

      // Get the default configuration object
      Configuration conf = new Configuration ();

      // look at all of the files in the cluster file directory... start by getting the directory name
      String dirName;

      // if this is the very first iter, use the directory name supplied by the user
      if (i == 0)
        dirName = args[1];

      // otherwise, use the last one that we wrote to
      else
        dirName = args[1] + i;

      // now, list the files in that directory
      FileSystem fs = FileSystem.get (URI.create("s3://ds-ibanez-vidiri"), conf);
      Path path = new Path (dirName);
      FileStatus fstatus[] = fs.listStatus (path);

      // find if there any files in the directory... count them at the same time
      int count = 0;
      for (FileStatus f: fstatus) {

        // ignore files that start with an underscore, since they just describe Hadoop output
        if (f.getPath().toUri().getPath().contains ("/_"))
          continue;

        count++;
        conf.set ("clusterInput", f.getPath().toUri().getPath());
      }

      // make sure there was not more than one cluster file
      if (count != 1) {
        throw new RuntimeException ("Found more than a single file in the clusters directory!");
      }

      // get the new job
      Job job = new Job(conf);
      job.setJobName ("K-Means clustering");

      // all of the inputs and outputs are text
      job.setMapOutputKeyClass (Text.class);
      job.setMapOutputValueClass (Text.class);
      job.setOutputKeyClass (Text.class);
      job.setOutputValueClass (Text.class);

      // tell Hadoop what mapper and reducer to use
      job.setMapperClass (KMeansMapper.class);
      job.setReducerClass (KMeansReducer.class);

      // set the input and output format class... these tell Haoop how to read/write to HDFS
      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      // set the input and output files
      TextInputFormat.setInputPaths (job, args[0]);
      TextOutputFormat.setOutputPath (job, new Path (args[1] + (i + 1)));

      // force the split size to 4 megs (this is small!)
      TextInputFormat.setMinInputSplitSize (job, 4 * 1024 * 1024);
      TextInputFormat.setMaxInputSplitSize (job, 4 * 1024 * 1024);

      // set the jar file to run
      job.setJarByClass (KMeans.class);

      //set the number of reducers to one
      job.setNumReduceTasks (1);

      // submit the job
      System.out.println ("Starting iteration " + i);
      int exitCode = job.waitForCompletion(true) ? 0 : 1;
      if (exitCode != 0) {
        System.out.println("Job Failed!!!");
        return exitCode;
      }
    }
    return 0;
  }
}

