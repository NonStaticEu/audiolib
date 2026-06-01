/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ui;

import edu.princeton.cs.algs4.Complex;
import eu.nonstatic.audio.ComplexUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.io.Serial;
import javax.swing.JPanel;
import lombok.Getter;

/**
 * X axis is time
 * Y axis is frequency
 * Color axis is magnitude
 */
public class SpectrumPanel extends JPanel {

  @Serial
  private static final long serialVersionUID = 1L;
  private final Complex[][] fftBuffer;
  @Getter
  private final boolean logMode;

  public SpectrumPanel(Complex[][] fftBuffer, boolean logMode) {
    this.fftBuffer = fftBuffer;
    this.logMode = logMode;
  }

  @Override
  public void paint(Graphics g) {
    final double maxMag = ComplexUtils.getMaxMagnitude(fftBuffer);
    final double maxMag2 = maxMag * maxMag;
    final int width = getWidth();
    final int height = getHeight();
    
    //draw the graph
    for(int x = 0; x < width; x++) {
      int slice = (x * fftBuffer.length) / width; //slice in time
      Complex[] timeSlice = fftBuffer[slice]; //set of freqs for that slice of time
      
      int freqsCount = timeSlice.length;
      double freqsCountLog = Math.log1p(freqsCount);
      for(int y = 0; y < height-1; y++) { 
        int ry = (height-1-y); //reversed y: y axis increases from the top whereas we want the lower freqs on the bottom
        int f; //freq index
        if(logMode) {
          f = (int)Math.expm1((ry * freqsCountLog) / height);
        }
        else {
          f = (ry * freqsCount) / height;
        }
        
        Complex k = timeSlice[f];
        double normMag = (k.re() * k.re() + k.im() * k.im()) / maxMag2; //basic spectrums work with squares of magnitudes //XXX maybe the magnitude should be taken as (complex.abs()/fftBuffer.length)^2 (have seen it on some example)
        Color c = getColor(normMag);
        
        g.setColor(c);
        g.drawLine(x, y, x, y);
      }
    }
  }

  
  public static Color getColor(double mag) {
    double r0 = 0.0001, r1 = 0.001, r2 = 0.01, r3 = 0.1, r4 = 1.0;
    if(mag < r0) {
      return Color.black;
    }
    
    mag = Math.log(Math.min(mag, 1.0));
    
    double t0 = Math.log(r0), t1 = Math.log(r1), t2 = Math.log(r2), t3 = Math.log(r3), t4 = Math.log(r4);
    
    int r, g, b;
    if(mag < t1) {
      r = 0;
      g = 0;
      b = (int)(255.0 * (mag - t0) / (t1 - t0)); //up
    }
    else if(mag < t2) {
      r = 0;
      g = (int)(255.0 * (mag - t1) / (t2 - t1)); //up
      b = 255;
    }
    else if(mag < t3) {
      r = 0;
      g = 255;
      b = (int)(255.0 * (t3 - mag) / (t3 - t2)); //down
    }
    else {
      r = (int)(255.0 * (mag - t3) / (t4 - t3)); //up
      g = 255;
      b = 0;
    }
    
    if(r < 0 || g < 0 || b < 0 || r > 255 || g > 255 || b > 255) {
      System.out.println("no good");
    }
    
    return new Color(r, g, b);
  }

}
