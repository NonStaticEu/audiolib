package eu.nonstatic.audio.formats.mpeg;

/**
 * <a href="https://phoxis.org/2010/05/08/synch-safe/">...</a>
 * <a href="http://en.wikipedia.org/wiki/Synchsafe">...</a>
 */
public class SynchSafe {

  public static int toInt(byte[] bytes) {
    return (((bytes[0] << 7 | (bytes[1]&0xff)) << 7) | (bytes[2]&0xff)) << 7 | (bytes[3]&0xff);
  }

  public static byte[] toBytes(int i) {
    byte b0 = (byte)(i >> 21);
    byte b1 = (byte)(i >> 14 & 0xff);
    byte b2 = (byte)(i >> 7 & 0xff);
    byte b3 = (byte)(i & 0xff);
    return new byte[]{b0, b1, b2, b3};
  }

}
