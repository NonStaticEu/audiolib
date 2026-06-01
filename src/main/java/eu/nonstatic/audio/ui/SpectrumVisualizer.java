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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serial;
import javax.swing.JFrame;

public class SpectrumVisualizer extends JFrame {

  @Serial
  private static final long serialVersionUID = 1L;
  
  public SpectrumVisualizer(Complex[][] fftBuffer) {
    this(fftBuffer, true);
  }
  
  public SpectrumVisualizer(Complex[][] fftBuffer, boolean logMode) {
    this(fftBuffer, 800, 600, logMode);
  }
  
  public SpectrumVisualizer(Complex[][] fftBuffer, int width, int height, boolean logMode) {

    add( new SpectrumPanel(fftBuffer, logMode));
    
    addKeyListener( new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          dispose();
        }
      }
    });
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(width, height);
    setVisible(true);
  }
}
