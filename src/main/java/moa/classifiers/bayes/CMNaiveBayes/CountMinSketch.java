
/**
 *
 * @author maroua
 */


package moa.classifiers.bayes.CMNaiveBayes;
 

import moa.classifiers.bayes.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CountMinSketch {
  // 1% estimation error with 1% probability (99% confidence) that the estimation breaks this limit
  private static final float DEFAULT_DELTA = 0.1f;
  private static final float DEFAULT_EPSILON = 0.01f;
  private final int w;
  private final int d;
  private final int[][] multiset;

  public CountMinSketch() {
    this(DEFAULT_DELTA, DEFAULT_EPSILON);
 
  }

  public CountMinSketch(float delta, float epsilon) {
    this.w = (int) Math.ceil(Math.exp(1.0) / epsilon);
    this.d = (int) Math.ceil(Math.log(1.0 / delta));
   // System.out.println(w);
   // System.out.println(d);
    this.multiset = new int[d][w];
   
   /* for (int i = 1; i <= d; i++) {
        for(int j=1;j<=w;j++){
            multiset[i - 1][j-1] = 1;
        }
    }*/ //smoothing

  }

  public CountMinSketch(int width, int depth) {
    this.w = width;
    this.d = depth;
    this.multiset = new int[d][w];
  }

  private CountMinSketch(int width, int depth, int[][] ms) {
    this.w = width;
    this.d = depth;
    this.multiset = ms;
  
  }

  public int getWidth() {
    return w;
  }

  public int getDepth() {
    return d;
  }
   public int [][] getMultiset() {
    return multiset;
  }
  /**
   * Returns the size in bytes after serialization.
   *
   * @return serialized size in bytes
   */
  public long getSizeInBytes() {
    return ((w * d) + 2) * (Integer.SIZE / 8);
  }

  public void set(byte[] key) {
    // We use the trick mentioned in "Less Hashing, Same Performance: Building a Better Bloom Filter"
    // by Kirsch et.al. From abstract 'only two hash functions are necessary to effectively
    // implement a Bloom filter without any loss in the asymptotic false positive probability'
    // The paper also proves that the same technique (using just 2 pairwise independent hash functions)
    // can be used for Count-Min sketch.

    // Lets split up 64-bit hashcode into two 32-bit hashcodes and employ the technique mentioned
    // in the above paper
    long hash64 = Murmur3.hash64(key);
    int hash1 = (int) hash64;
    int hash2 = (int) (hash64 >>> 32);
    for (int i = 1; i <= d; i++) {
      int combinedHash = hash1 + (i * hash2);
      // hashcode should be positive, flip all the bits if it's negative
      if (combinedHash < 0) {
        combinedHash = ~combinedHash;
      }
      int pos = combinedHash % w;
      multiset[i - 1][pos] += 1;
    }
  }
  
 

  public void setString(String val) {
    set(val.getBytes());
  }

  public void setByte(byte val) {
    set(new byte[]{val});
  }

  public void setInt(int val) {
    // puts int in little endian order
    set(intToByteArrayLE(val));
  }


  public void setLong(long val) {
    // puts long in little endian order
    set(longToByteArrayLE(val));
  }

  public void setFloat(float val) {
    setInt(Float.floatToIntBits(val));
  }

  public void setDouble(double val) {
    setLong(Double.doubleToLongBits(val));
  }

  private static byte[] intToByteArrayLE(int val) {
    return new byte[]{(byte) (val >> 0),
        (byte) (val >> 8),
        (byte) (val >> 16),
        (byte) (val >> 24)};
  }

  private static byte[] longToByteArrayLE(long val) {
    return new byte[]{(byte) (val >> 0),
        (byte) (val >> 8),
        (byte) (val >> 16),
        (byte) (val >> 24),
        (byte) (val >> 32),
        (byte) (val >> 40),
        (byte) (val >> 48),
        (byte) (val >> 56),};
  }

  public int getEstimatedCount(byte[] key) {
    long hash64 = Murmur3.hash64(key);
    int hash1 = (int) hash64;
    int hash2 = (int) (hash64 >>> 32);
    int min = Integer.MAX_VALUE;
    for (int i = 1; i <= d; i++) {
      int combinedHash = hash1 + (i * hash2);
      // hashcode should be positive, flip all the bits if it's negative
      if (combinedHash < 0) {
        combinedHash = ~combinedHash;
      }
      int pos = combinedHash % w;
      min = Math.min(min, multiset[i - 1][pos]);
    }

    return min;
  }

  public int getEstimatedCount(String val) {
    return getEstimatedCount(val.getBytes());
  }

  public int getEstimatedCount(byte val) {
    return getEstimatedCount(new byte[]{val});
  }

  public int getEstimatedCount(int val) {
    return getEstimatedCount(intToByteArrayLE(val));
  }

  public int getEstimatedCount(long val) {
    return getEstimatedCount(longToByteArrayLE(val));
  }

  public int getEstimatedCount(float val) {
    return getEstimatedCount(Float.floatToIntBits(val));
  }

  public int getEstimatedCount(double val) {
    return getEstimatedCount(Double.doubleToLongBits(val));
  }

  /**
   * Merge the give count min sketch with current one. Merge will throw RuntimeException if the
 provided CountMinSketch is not compatible with current one.
   *
   * @param that - the one to be merged
   */
  public void merge(CountMinSketch that) {
    if (that == null) {
      return;
    }

    if (this.w != that.w) {
      throw new RuntimeException("Merge failed! Width of count min sketch do not match!" +
          "this.width: " + this.getWidth() + " that.width: " + that.getWidth());
    }

    if (this.d != that.d) {
      throw new RuntimeException("Merge failed! Depth of count min sketch do not match!" +
          "this.depth: " + this.getDepth() + " that.depth: " + that.getDepth());
    }

    for (int i = 0; i < d; i++) {
      for (int j = 0; j < w; j++) {
        this.multiset[i][j] += that.multiset[i][j];
      }
    }
  }

  /**
   * Serialize the count min sketch to byte array. The format of serialization is width followed by
   * depth followed by integers in multiset from row1, row2 and so on..
   *
   * @return serialized byte array
   */
  public static byte[] serialize(CountMinSketch cms) {
    long serializedSize = cms.getSizeInBytes();
    ByteBuffer bb = ByteBuffer.allocate((int) serializedSize);
    bb.putInt(cms.getWidth());
    bb.putInt(cms.getDepth());
    for (int i = 0; i < cms.getDepth(); i++) {
      for (int j = 0; j < cms.getWidth(); j++) {
        bb.putInt(cms.multiset[i][j]);
      }
    }
    bb.flip();
    return bb.array();
  }

  /**
   * Deserialize the serialized count min sketch.
   *
   * @param serialized - serialized count min sketch
   * @return deserialized count min sketch object
   */
  public static CountMinSketch deserialize(byte[] serialized) {
    ByteBuffer bb = ByteBuffer.allocate(serialized.length);
    bb.put(serialized);
    bb.flip();
    int width = bb.getInt();
    int depth = bb.getInt();
    int[][] multiset = new int[depth][width];
    for (int i = 0; i < depth; i++) {
      for (int j = 0; j < width; j++) {
        multiset[i][j] = bb.getInt();
      }
    }
    CountMinSketch cms = new CountMinSketch(width, depth, multiset);
    return cms;
  }
}
