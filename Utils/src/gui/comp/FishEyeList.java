package gui.comp;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * FishEyeList is a list with the selected item displayed in the center and
 * larger than the other items. It's a list with a Fish Eye perspective.
 * 
 * @author Steve & Eric
 */
public final class FishEyeList extends JPanel
{
   private static final long serialVersionUID = 1L;
   
   public static final int VERTICAL = 0, HORIZONTAL = 1;
   
   private static final Color BLACKY = new Color (20, 20, 20);
   private static final Color SHADED = new Color (150, 150, 150);
   private static final Color CREAMY = new Color (247, 247, 225);
   private static final int MAX_VISIBLE = 11;
   private static final int X_PAD = 25, Y_PAD = 18;
   
   /*
   private static final String ARIEL_BLACK = "Ariel Black";
   private static final String ARIEL_ROUNDED = "Ariel Rounded MT Bold";
   private static final String BODONI = "Bodoni MT";
   private static final String BOOKMAN = "Bookman Old Style";
   private static final String COMIC_SANS = "Comic Sans MT";
   private static final String COPPERPLATE = "Copperplate Gothic Light";
   private static final String LINOTYPE = "Linotype";
   private static final String PALATINO = "Palatino";
   private static final String PERPETUA = "Perpetua";
   private static final String TIMES_ROMAN = "Times New Roman";
   private static final String TREBUCHET = "Trebuchet MS";
   private static final String VERDANA = "Verdana";
   */
   
   private static final String FONT_NAME = "Maiandra GD";
   private static final Font SMALL_FONT = new Font (FONT_NAME, Font.BOLD, 8);
   private static final Font LARGE_FONT = new Font (FONT_NAME, Font.BOLD, 36);
   
   private final AnimationThread animThread = new AnimationThread();
   private final LinkedBlockingQueue<Integer> nextSelected =
      new LinkedBlockingQueue<Integer>();

   private FontMetrics smallFontMetrics = null, largeFontMetrics = null;
   private List<Object> list = new ArrayList<Object>();
   private int w = 0, h = 0;
   private int selected = 0, numBeforeOrAfter = 0, layout = VERTICAL;

   public FishEyeList (final List<Object> inList)
   {
      this (inList, null, VERTICAL);
   }

   public FishEyeList (final List<Object> inList, 
                       final JComponent parent, 
                       final int layout)
   {
      super (new BorderLayout());
      this.layout = (layout == VERTICAL ? VERTICAL : HORIZONTAL);
      list = inList;
      Graphics g = null;
      if (parent != null)
         g = parent.getGraphics();
      if (g != null)
      {
         smallFontMetrics = g.getFontMetrics (SMALL_FONT);
         largeFontMetrics = g.getFontMetrics (LARGE_FONT);
      }
      animThread.start();

      setPreferredSize (new Sizer().calcDimension());
      setFocusable (true);
   }

   final class Sizer
   {
      private int smallRowHeight, largeRowHeight;
      private Object widest;
      private int totalWidth;

      private Sizer()
      {
         smallRowHeight = smallFontMetrics.getHeight() + 2;
         largeRowHeight = largeFontMetrics.getHeight() + 6;
      }
      
      private Dimension calcDimension()
      {
         return layout == HORIZONTAL ? calcHorizontal() : calcVertical();
      }

      private Dimension calcHorizontal()
      {
         getWidest(); // determine component width
         h = largeRowHeight + (Y_PAD * 2); // determine component height
         return new Dimension (totalWidth, h);
      }
      
      private Dimension calcVertical()
      {
         getWidest(); // determine component width
         w = largeFontMetrics.stringWidth (widest.toString()) + (X_PAD * 5);

         // determine component height
         numBeforeOrAfter = ((list.size() < MAX_VISIBLE ? list.size() : MAX_VISIBLE)) / 2;
         smallRowHeight = smallFontMetrics.getHeight() + 2;
         largeRowHeight = largeFontMetrics.getHeight() + 6;
         h = smallRowHeight * (numBeforeOrAfter * 2);
         h = h + largeRowHeight; // center line height
         h = h + (Y_PAD * 2); // insets above and below

         return new Dimension (w, h);
      }

      private void getWidest()
      {
         for (Object obj : list)
         {

            if (obj == null)
               continue;
            String val = obj.toString();
            if (val == null)
               continue;
            if (val.length() > w)
            {
               w = val.length();
               widest = val;
            }
            totalWidth += smallFontMetrics.stringWidth (val) + (X_PAD * 2);
         }
         totalWidth -= smallFontMetrics.stringWidth (widest.toString()) + (X_PAD * 2);
         totalWidth += largeFontMetrics.stringWidth (widest.toString()) + (X_PAD * 5);
      }
   }
   
   public void setSelected (final String val)
   {
      setSelected (list.indexOf (val) >= 0 ? list.indexOf (val) : 0);
   }

   public void setSelected (final int index)
   {
      synchronized (this)
      {
         int oldSelected = selected;
         selected = (index < list.size() ? index : 0);
         for (int i = oldSelected + 1; (i % list.size()) != selected; i++)
            nextSelected.add (i);
         nextSelected.add (selected + 1);
      }
   }

   public Object getSelected()
   {
      return list.get (selected);
   }

   public void advance()
   {
      setSelected (selected + 1);
   }

   public void retreat()
   {
      synchronized (this)
      {
         int index = selected - 1;
         selected = (index < 0 ? list.size() - 1 : index);
         nextSelected.add (-(selected + 1));
      }
   }

   class AnimationThread extends Thread
   {
      private static final int ANIM_SLEEP = 3;
      
      private volatile int animSelected = 0;
      private volatile double offset = 0.0;

      public AnimationThread()
      {
         setDaemon (true);
      }

      @Override
      public void run()
      {
         try
         {
            while (!isInterrupted())
            {
               int next;
               synchronized (this)
               {
                  next = nextSelected.take();
               }
               repaint();

               if (next >= 0)
               {
                  synchronized (this)
                  {
                     animSelected = Math.abs (next) - 1;
                     offset = 1.0;
                  }
                  while (!isInterrupted() && offset > 0)
                  {
                     sleep (ANIM_SLEEP);
                     offset = Math.max (0.0, offset - 0.01 * (nextSelected.size() + 1));
                     repaint();
                  }
               }
               else
               {
                  while (!isInterrupted() && offset < 1.0)
                  {
                     sleep (ANIM_SLEEP);
                     offset = Math.min (1.0, offset + 0.01 * (nextSelected.size() + 1));
                     repaint();
                  }
                  synchronized (this)
                  {
                     animSelected = Math.abs (next) - 1;
                     offset = 0.0;
                  }
               }
            }
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }
   }

   class BubbledText
   {
      private static final double FISH_PCT = 0.5;
      
      private int index;
      private Point center;
      private double offset;
      private double scale;
      private boolean isSelected;
      private boolean prevSected;

      public BubbledText (final int i, final Point c, final double o)
      {
         index = i;
         center = c;
         offset = o;
         scale = 1.0 - (offset * (1.0 - FISH_PCT));
         isSelected = true;
      }

      public BubbledText (final BubbledText bt, final boolean before)
      {
         isSelected = false;
         prevSected = before && bt.isSelected;
         offset = bt.offset;

         scale = prevSected ? FISH_PCT + (offset * (1.0 - FISH_PCT)) : 
            bt.scale * FISH_PCT;

         index = before ? (bt.index - 1 + list.size()) % list.size() :
            (bt.index + 1) % list.size();

         Rectangle btBounds = bt.getBounds();
         Dimension dim = getDimensions();

         if (layout == FishEyeList.HORIZONTAL)
         {
            int px = before ? btBounds.x - (dim.width / 2) - X_PAD :
               (int) btBounds.getMaxX() + (dim.width / 2) + X_PAD;
            center = new Point (px, bt.center.y);
         }
         else // VERTICTAL
         {
            int py = before ? btBounds.y - (dim.height / 2) - Y_PAD :
               (int) btBounds.getMaxY() + (dim.height / 2) + Y_PAD;
            center = new Point (bt.center.x, py);
         }
      }

      public Rectangle getBounds()
      {
         Rectangle bounds = getTextBounds();
         bounds.x -= X_PAD * scale;
         bounds.y -= Y_PAD * scale;
         bounds.setSize (getDimensions());
         return bounds;
      }

      public Dimension getDimensions()
      {
         Dimension dimensions = getTextDimensions();
         dimensions.width += (X_PAD * 2) * scale;
         dimensions.height += (Y_PAD * 2) * scale;
         return dimensions;
      }

      public Rectangle getTextBounds()
      {
         Dimension textDim = getTextDimensions();
         Point textPoint = new Point (center.x - (textDim.width / 2),
                                      center.y - (textDim.height / 2));
         return new Rectangle (textPoint, textDim);
      }

      public Dimension getTextDimensions()
      {
         String text = list.get (index).toString();
         FontMetrics fm = getGraphics().getFontMetrics (getFont());
         double sw = fm.stringWidth (text);
         double sh = fm.getAscent() - fm.getDescent();
         return new Dimension ((int) sw, (int) sh);
      }

      private double getFontScale()
      {
         return scale * ((LARGE_FONT.getSize() / (double) SMALL_FONT.getSize()) - 1.0);
      }

      private Font getFont()
      {
         return SMALL_FONT.deriveFont 
         ((float) (SMALL_FONT.getSize() + SMALL_FONT.getSize() * getFontScale()));
      }

      public void paint (final Graphics2D g2)
      {
         Rectangle bounds = getBounds();
         if (!g2.getClipBounds().intersects (bounds))
            return;

         String text = list.get (index).toString();
         Rectangle textBounds = getTextBounds();

         g2.translate (center.x, center.y);

         Composite comp = g2.getComposite();
         g2.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_ATOP, (float) scale));

         g2.setColor (SHADED);
         g2.fillRoundRect (-(bounds.width / 2), -(bounds.height / 2),
                  bounds.width, bounds.height, 50, 50);
         g2.setColor (Color.WHITE);
         g2.fillRoundRect (-(bounds.width / 2), -(bounds.height / 2),
                  bounds.width - 2, bounds.height - 2, 50, 50);
         g2.setColor (CREAMY);
         g2.fillRoundRect (-(bounds.width / 2) + 2, -(bounds.height / 2) + 2,
                  bounds.width - 4, bounds.height - 4, 50, 50);

         g2.setComposite (comp);
         g2.setFont (getFont());
         g2.setColor (BLACKY);
         g2.drawString (text, -(textBounds.width / 2), textBounds.height / 2);
         g2.translate (-center.x, -center.y);
      }
   }

   @Override
   public void paint (final Graphics g)
   {
      super.paint (g);

      // for a smoother font
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
               RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      int paintSelected = animThread.animSelected;
      double paintOffset = animThread.offset;
      Point center = new Point (getWidth() / 2, getHeight() / 2);
      int prevSelected = (paintSelected - 1 + list.size()) % list.size();
      BubbledText prevSelBT = new BubbledText (prevSelected, center, 0.0);
      BubbledText prevAfterBT = new BubbledText (prevSelBT, false);

      if (layout == HORIZONTAL)
         g2.translate ((prevAfterBT.center.x - prevSelBT.center.x) * paintOffset, 0.0);
      else // VERTICAL
         g2.translate (0.0, (prevAfterBT.center.y - prevSelBT.center.y) * paintOffset);

      BubbledText selBubble = new BubbledText (paintSelected, center,
               paintOffset);
      selBubble.paint (g2);

      BubbledText before = selBubble;
      BubbledText after = selBubble;
      for (int i = 0; i < list.size(); i++)
      {
         before = new BubbledText (before, true);
         before.paint (g2);
         after = new BubbledText (after, false);
         after.paint (g2);
      }
   }

   public static final class MyListener implements MouseListener
   {
      private SpinThread spinThread;

      public MyListener (final FishEyeList fel)
      {
         super();
         spinThread = new SpinThread (fel);
         spinThread.start();
      }

      @Override
      public synchronized void mouseClicked (final MouseEvent me)
      {
      }

      @Override
      public synchronized void mousePressed (final MouseEvent me)
      {
         accelerate (me);
      }

      @Override
      public synchronized void mouseEntered (final MouseEvent me)
      {
         accelerate (me);
      }

      @Override
      public synchronized void mouseReleased (final MouseEvent me)
      {
         if (me.getButton() == MouseEvent.BUTTON1)
         {
            spinThread.setButton (MouseEvent.BUTTON1);
            spinThread.decelerate();
         }
         if (me.getButton() == MouseEvent.BUTTON3)
         {
            spinThread.setButton (MouseEvent.BUTTON3);
            spinThread.decelerate();
         }
      }

      @Override
      public synchronized void mouseExited (final MouseEvent me)
      {
         if (me.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK)
         {
            spinThread.setButton (MouseEvent.BUTTON1);
            spinThread.decelerate();
         }
         if (me.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK)
         {
            spinThread.setButton (MouseEvent.BUTTON3);
            spinThread.decelerate();
         }
      }

      private void accelerate (final MouseEvent me)
      {
         if (me.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK)
         {
            spinThread.setButton (MouseEvent.BUTTON1);
            spinThread.accelerate();
         }
         if (me.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK)
         {
            spinThread.setButton (MouseEvent.BUTTON3);
            spinThread.accelerate();
         }
      }

      public static class SpinThread extends Thread
      {
         private static final int START_SPEED = 500;
         private static final int STOP_SPEED = 400;
         private static final int MAX_SPEED = 20;
         private static final double DECEL_PCT = 1.60;
         private static final double ACCEL_PCT = 0.80;
         
         private FishEyeList fel;
         private int button, speed;
         private boolean accelerating = false, decelerating = false, spinning = true;

         public SpinThread (final FishEyeList fel)
         {
            this.fel = fel;
         }

         public void setSpinning (final boolean b)
         {
            spinning = b;
         }

         public void setButton (final int b)
         {
            button = b;
         }

         public void decelerate()
         {
            accelerating = false;
            decelerating = true;
         }

         public void accelerate()
         {
            speed = START_SPEED;
            decelerating = false;
            accelerating = true;
            setSpinning (true);
         }

         @Override
         public void run()
         {
            speed = START_SPEED;
            while (!isInterrupted())
            {
               try
               {
                  sleep (speed);
               }
               catch (InterruptedException e)
               {
                  interrupt();
               }
               if (spinning)
               {
                  MyObject mo = (MyObject) fel.getSelected();
                  if (button == MouseEvent.BUTTON1)
                  {
                     mo.advance (0);
                     fel.advance();
                  }
                  else if (button == MouseEvent.BUTTON3)
                  {
                     mo.retreat (0);
                     fel.retreat();
                  }
                  if (accelerating)
                  {
                     if (speed > MAX_SPEED)
                        speed = (int) (speed * ACCEL_PCT);
                     else
                        accelerating = false;
                  }
                  if (decelerating)
                  {
                     if (speed < STOP_SPEED)
                        speed = (int) (speed * DECEL_PCT);
                     else
                     {
                        decelerating = false;
                        setSpinning (false);
                     }
                  }
               }
            }
         }
      }
   }

   // for testing. run main and mouse click in the list to advance
   public static class MyObject
   {
      private int score;
      private String name;

      public MyObject (final int s, final String n)
      {
         this.score = s;
         this.name = n;
      }

      @Override
      public String toString()
      {
         return name + " " + score;
      }

      public synchronized void advance (final int amt)
      {
         score += amt;
      }

      public synchronized void retreat (final int amt)
      {
         score -= amt;
      }
   }

   public static void main (final String[] args)
   {
      JPanel panel = new JPanel (new BorderLayout());
      panel.setPreferredSize (new Dimension (850, 600));

      JFrame frame = new JFrame ("Fish Eye List");
      frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
      frame.add (panel);
      frame.pack();
      frame.setLocation (50, 50);
      frame.setVisible (true);

      List<Object> list = new ArrayList<Object>();
      list.add (new MyObject (56, "Bill"));
      list.add (new MyObject (20, "Wendy"));
      list.add (new MyObject (59, "Doug"));
      list.add (new MyObject (20, "Debbie"));
      list.add (new MyObject (60, "Kim"));
      list.add (new MyObject (20, "Jim"));
      list.add (new MyObject (01, "Steve"));
      list.add (new MyObject (65, "Janice"));
      list.add (new MyObject (93, "Colleen"));
      list.add (new MyObject (96, "Ryan"));

      FishEyeList vFel = new FishEyeList (list, panel, FishEyeList.VERTICAL);
      vFel.setBackground (Color.YELLOW);
      vFel.addMouseListener (new MyListener (vFel));

      FishEyeList hFel = new FishEyeList (list, panel, FishEyeList.HORIZONTAL);
      hFel.setBackground (Color.YELLOW);
      hFel.addMouseListener (new MyListener (hFel));

      panel.add (vFel, BorderLayout.CENTER);
      panel.add (hFel, BorderLayout.SOUTH);
      panel.revalidate();
   }
}