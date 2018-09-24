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


  private RandomAccessFile subj;
  private RandomAccessFile pred;
  private RandomAccessFile obj;

  private RandomAccessFile[] positional = {};

  @Override
  public void setUp(File statisticsDir, int numberOfChunks) {
    statisticsDir.mkdirs();
    try {
      subj = new RandomAccessFile(new File(statisticsDir, "subj"), "rw");
      pred = new RandomAccessFile(new File(statisticsDir, "pred"), "rw");
      obj = new RandomAccessFile(new File(statisticsDir, "obj"), "rw");
      positional = new RandomAccessFile[]{subj, pred, obj};
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void incrementFrequency(long resourceId, int chunkNumber, TriplePosition triplePosition) {
    try {
      RandomAccessFile file = seekToPosition(resourceId, chunkNumber, triplePosition);
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
      long value = 0;
      try {
        value = file.readLong();
      } catch (EOFException e) { }
      return value;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private RandomAccessFile seekToPosition(long resourceId, int chunkNumber, TriplePosition triplePosition) throws IOException {
    RandomAccessFile file = positional[triplePosition.ordinal()];
    long sizeOfRow = Long.BYTES * getNumberOfChunks() * getTriplePositions().length;
    file.seek(sizeOfRow*resourceId + Long.BYTES * chunkNumber);
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
