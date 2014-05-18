/*
 * (C) Copyright 2014 Mikaël Castellani
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package NLP.MRscoring;

import com.mongodb.BasicDBList;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import common.DBHelper;
import common.Event;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.bson.types.ObjectId;

public class MR {

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, DoubleWritable> {

        
        private static final String[] positive = new String[] {":-D", "=D", "xD", "<3", "(L)", "^^", "x)", ":-)" ,":)" ,":o)" ,":]" ,":3" ,":c)" ,":D" ,"C:", "=)"};
        private static final String[] negative = new String[] {"D8" ,"D;", "D=", "DX", "v.v", ":'(" , "='(" , ":\\", "x(", ":-(", "=("  ,":(" ,":c",":[" , "</3","- -", "-.-", "(> <)", ":|"};

        public void map(LongWritable key, Text comment, OutputCollector<Text, DoubleWritable> output, Reporter reporter) throws IOException {
            String line = comment.toString();
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            
            StringTokenizer tokenizer = new StringTokenizer(line, "#|#");
            Text realKey = new Text(tokenizer.nextToken());
            System.out.println("TREATING new : " + realKey);
            while (tokenizer.hasMoreTokens()) {
                DoubleWritable commentScore = evaluateComment(new Text(tokenizer.nextToken()), pipeline);
                output.collect(realKey, commentScore);
            }

        }

        private DoubleWritable evaluateComment(Text comment, StanfordCoreNLP pipeline) {
          String message = comment.toString();
          System.out.println("phrase is " + message);
          int mainSentiment = 0;
          if (message != null && message.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(message);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
              Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
              String partText = sentence.toString();
              int sentiment = 2;
              
              for (String item : positive) {
                if (partText.contains(item)) {
                  sentiment = 4;
                  break;
                }
              }
              
              if (sentiment == 2) {
                for (String item : negative) {
                  if (partText.contains(item)) {
                    sentiment = 0;
                    break;
                  }
                }
              }
              
              if (sentiment == 2) {
                sentiment = RNNCoreAnnotations.getPredictedClass(tree);
              }
              
              if (partText.length() > longest) {
                mainSentiment = sentiment;
                longest = partText.length();
              }

            }
          }
          // 0 = negative
          // 2 = neutral
          // 4 = positive
          
          // Remap negative = 0, neutral = 0.5, positive = 1
          if (mainSentiment == 4) {
            return new DoubleWritable(1);
          }
          else if (mainSentiment == 0) {
            return new DoubleWritable(0);
          }
          else {
            return new DoubleWritable(0.5);
          }
        }
    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        public void reduce(Text key, Iterator<DoubleWritable> values, OutputCollector<Text, DoubleWritable> output, Reporter reporter) throws IOException {
            int sum = 0;
            int count = 0;
            while (values.hasNext()) {
                sum += values.next().get();
                count++;
            }
            
            double result = sum / (double) count;
            output.collect(key, new DoubleWritable(result));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: App path ");
            System.exit(2);
        }
        if (!"/".equals(args[0].substring(args[0].length() - 1, args[0].length()))) {
            args[0] += "/";
        }

        //Prepare data
        String path = args[0] + "temp/";
        writeToFile(path);

        //First job
        JobConf jobconf1 = new JobConf(MR.class);
        jobconf1.setJobName("CommentsRatingJob");

        jobconf1.setOutputKeyClass(Text.class);
        jobconf1.setOutputValueClass(DoubleWritable.class);

        jobconf1.setMapperClass(Map.class);
        jobconf1.setReducerClass(Reduce.class);

        jobconf1.setNumMapTasks(2);
        jobconf1.setNumReduceTasks(2);

        jobconf1.setInputFormat(TextInputFormat.class);
        jobconf1.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(jobconf1, new Path(path + "comments.txt"));
        FileOutputFormat.setOutputPath(jobconf1, new Path(args[0] + "output/"));

        JobClient.runJob(jobconf1);
    }

    public static void writeToFile(String path) throws IOException, URISyntaxException {

        DBHelper db = DBHelper.getInstance();

        DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        String text = "";
        while (cursor.hasNext()) {

          DBObject item = cursor.next();
            for (Event.TYPE everyType : Event.TYPE.values()) {
                if (item.get("post_id_facebook_" + everyType.name().toLowerCase()) != null) {
                  ObjectId Id = (ObjectId) item.get("post_id_facebook_" + everyType.name().toLowerCase());
                  System.out.println("FOUND ONE, ID IS : " + Id);
                  DBObject post = db.findFBPostsById(Id);
                  System.out.println("POST IS : " + post.get("comments"));
                  BasicDBList commentsList = (BasicDBList) post.get("comments");
                  boolean first = true;
                  for (Object comment : commentsList) {
                    if (first) {
                      text += Id.toString() + "_" + everyType.name().toLowerCase();
                      first = false;
                    }
                    String mess = (String) ((DBObject) comment).get("message");
                    text += "#|#" + mess.replace("\n", ".");
                  }
                  if (!first) {
                    text += "\n";
                  }
                }
            }
            
        }

        Configuration configuration = new Configuration();
        FileSystem hdfs = FileSystem.get(configuration);
        Path file = new Path(path + "comments.txt");
        if (hdfs.exists(file)) {
            hdfs.delete(file, true);
        }
        OutputStream os = hdfs.create(file);
        BufferedWriter br = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        br.write(text);
        br.close();
        hdfs.close();
    }
}
