package com.assignment;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

public class Main {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("booksData")
                .getOrCreate();

        // Load JSONL dataset from GCS
        Dataset<Row> df = spark.read().json("gs://finalbucketassignment/books.jsonl/");

        // Print schema
        df.printSchema();

        // Filter for books with good ratings
        Dataset<Row> highRated = df.filter(col("average_rating").gt(4.0));

        // Select title, author, and main category
        Dataset<Row> selected = highRated.select(
                col("title"),
                col("author.name").alias("author_name"),
                col("main_category"),
                col("details.Language").alias("language"),
                col("categories")
        );

        // Explode categories for analysis
        Dataset<Row> exploded = selected.withColumn("category", explode(col("categories")));

        // Show top results
        exploded.show(10, false);

        // Write transformed data back to GCS (optional)
        exploded.write()
                .mode("overwrite")
                .parquet("gs://finalbucketassignment/results.txt/");

        spark.stop();
    }
}