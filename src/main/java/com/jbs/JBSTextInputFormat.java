package com.jbs;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapred.*;

import java.io.IOException;


/**
 * @author lyl
 * @description: TODO
 * @date 2022/1/7 13:53
 * 自定义重写的 TextInputFormat 类
 * 此类是将org.apache.hadoop.mapred下的TextInputFormat源码拷贝进来进行改写。
 *  Hive将HDFS上的文件导入Hive会进行如下处理：
 *      调用InputFormat，将文件切成不同的文档。每篇文档即一行(Row)。
 *      调用SerDe的Deserializer，将一行(Row)，切分为各个字段。
 *
 *  可以查看hadoop-mapreduce-client-core-3.2.2.jar包org.apache.hadoop.mapred下的类TextInputFormat。
 *  建表前在hive的CLI界面上输入如下即可实现自定义多字符换行符
 *      set textinputformat.record.delimiter=<自定义换行字符串>;
 * Maven项目的 pom.xml文件中添加如下依赖。
 */
public class JBSTextInputFormat extends FileInputFormat<LongWritable, Text> implements JobConfigurable {

    private CompressionCodecFactory compressionCodecs = null;
    private final static String defaultEncoding = "UTF-8";
    private String encoding = null;

    public RecordReader<LongWritable, Text> getRecordReader(InputSplit inputSplit, JobConf jobConf, Reporter reporter) throws IOException {
        reporter.setStatus(inputSplit.toString());
        String delimiter = jobConf.get("jbs.record.line.seperator");
        this.encoding = jobConf.get("jbs.record.encoding",defaultEncoding);
        byte[] recordDelimiterBytes = null;
        if (null != delimiter) {
            recordDelimiterBytes = delimiter.getBytes(this.encoding);
        }
        return new JBSLineRecordReader(jobConf, (FileSplit)inputSplit, recordDelimiterBytes);
    }

    @Override
    public void configure(JobConf jobConf) {
        this.compressionCodecs = new CompressionCodecFactory(jobConf);
    }

    @Override
    protected boolean isSplitable(FileSystem fs, Path filename) {
        CompressionCodec codec = this.compressionCodecs.getCodec(filename);
        if (null == codec) {
            return true;
        }
        return codec instanceof SplittableCompressionCodec;
    }


}























