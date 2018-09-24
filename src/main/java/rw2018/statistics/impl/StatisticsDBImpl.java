package rw2018.statistics.impl;

import java.io.*;

import rw2018.statistics.StatisticsDB;
import rw2018.statistics.TriplePosition;

/**
 * This is the class that will be executed during the evaluation!!
 * 
 * TODO implement this class
 */
public class StatisticsDBImpl extends StatisticsDBBaseImpl {


  private RandomAccessFile[] positional = {};
  private int positionalBytes[] = {1, 1, 1};
  private long lastId[] = {0, 0, 0};
  private File dir;

  @Override
  public void setUp(File statisticsDir, int numberOfChunks) {
    dir = statisticsDir;
    statisticsDir.mkdirs();
    try {
      positional = new RandomAccessFile[]{null, null, null};
      positional[0] = new RandomAccessFile(new File(statisticsDir, "0.1"), "rw");
      positional[1] = new RandomAccessFile(new File(statisticsDir, "1.1"), "rw");
      positional[2] = new RandomAccessFile(new File(statisticsDir, "2.1"), "rw");

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void incrementFrequency(long resourceId, int chunkNumber, TriplePosition triplePosition) {
    try {
      int ordinal = triplePosition.ordinal();
      RandomAccessFile file = seekToPosition(resourceId, chunkNumber, triplePosition);
      if (file == null) return;
      long value = 0;
      try {
        value = file.readLong();
      } catch (EOFException e) { }
      file = seekToPosition(resourceId, chunkNumber, triplePosition);
      file.writeLong(++value);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public long getFrequency(long resourceId, int chunkNumber, TriplePosition triplePosition) {
    RandomAccessFile file = null;
    try {
      file = seekToPosition(resourceId, chunkNumber, triplePosition);
      if (file == null) return 0;
      long value = 0;
      try {
        value = file.readLong();
      } catch (EOFException e) { }
      return value;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (file != null) file.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return 0;
  }

  private RandomAccessFile seekToPosition(long resourceId, int chunkNumber, TriplePosition triplePosition) throws IOException {
    int ordinal = triplePosition.ordinal();
    RandomAccessFile file = null;
    for (int i = 0; i < 8; i++) {
      if (resourceId < (1<<i)) {
        if (positionalBytes[ordinal] != i) {
          positional[ordinal].close();
          positionalBytes[ordinal] = i;
          positional[ordinal] = file = new RandomAccessFile(new File(dir, String.format("%d.%d", ordinal, i)), "rw");
        } else {
          file = positional[ordinal];
        }
      }
    }
    if (file == null)
      return null;
    long sizeOfRow = positionalBytes[ordinal] * getNumberOfChunks() * getTriplePositions().length;
    file.seek(sizeOfRow*resourceId + positionalBytes[ordinal] * chunkNumber);
    return file;
  }

  @Override
  public void close() {
    for (int i = 0; i < 3; i++) {
      try {
        positional[i].close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
