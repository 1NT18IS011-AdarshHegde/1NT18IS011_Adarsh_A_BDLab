

//import statements
import java.io.IOException;//this will handle I/o operations
import java.util.StringTokenizer;//string should be converted to tokens
//we are importing hadoop packages because java cannot be executed in distributed manner 
import org.apache.hadoop.conf.Configuration;//program has to run in distributed manner so we have to configure it
import org.apache.hadoop.fs.Path;//file's path is specified to covert java code to jar file
import org.apache.hadoop.io.IntWritable;//convert primitive to object oriented(these 2 box classes are used because our ip is (k,v) pairs  
import org.apache.hadoop.io.Text;//covert primitive to object oriented
import org.apache.hadoop.mapreduce.Job;//work we do in hadoop it will be in terms of job
import org.apache.hadoop.mapreduce.Mapper;//this is used to write the mapper function logic
import org.apache.hadoop.mapreduce.Reducer;//this is used for reducer function logic
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;//type of ip file
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;//type of op file

public class WordCount {//main class

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{//it is a user defined class it will extend the mapper and object is used combination of string values or it can be anything 

    private final static IntWritable one = new IntWritable(1);//we are creating instance of the object and passing value 1.
    private Text word = new Text();//one more instance is used for text

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	//Get the text and tokenize the word using space as separator.
      StringTokenizer itr = new StringTokenizer(value.toString());// create instance
      
      //For each token aka word, write a key value pair with 
      //word and 1 as value to context
      while (itr.hasMoreTokens()) {//check if tokens are present
          word.set(itr.nextToken());//pointer will point to input file
          context.write(word, one);//write to file
        
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {//we are not using object here as we are getting <k,v> from mapper
    private IntWritable result = new IntWritable();// instcance for result

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {//mapper op is intermediate op so duplicates will be present. reducer cannot accept duplicates so iteratively we need to check for duplicates
      int sum = 0;//aggregation of values
    //For each key value pair, get the value and adds to the sum
      //to get the total occurances of a word
      for (IntWritable val : values) {//for each loop it will check for duplicates
        sum += val.get();//add duplicates
      }
      result.set(sum);//passing value of sum
      context.write(key, result);//this contains aggregated value
    }
  }

  public static void main(String[] args) throws Exception {
	//Create a new Jar and set the driver class(this class) as the main class of jar
	Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);

    //Set the map and reduce classes in the job
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
  //Set the input and the output path from the arguments
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
  //Run the job and wait for its completion
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
