package map;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

public class LayerPanel extends JPanel implements ActionListener, Observer
{
   private static final long serialVersionUID = 1L;

   private Map map;
   private java.util.Map<Integer, JRadioButton> buttons = new HashMap<>();
   private ButtonGroup group = new ButtonGroup();

   public LayerPanel(final Map map)
   {
      this.map = map;

      setLayout(new GridLayout(0, 1));

      Border up = BorderFactory.createRaisedBevelBorder();
      Border down = BorderFactory.createLoweredBevelBorder();
      Border border = BorderFactory.createCompoundBorder(up, down);
      setBorder(border);

      makeButtons();
      map.addObserver(this);
   }

   @Override
   public void update(final Observable o, final Object action)
   {
      if (action.equals(Map.MAP_LOADED))
         makeButtons();
      else if (action.equals(Map.LAYER_ADDED))
         addButton(buttons.size());
      else if (action.toString().startsWith(Map.DRAW_LAYER_SET))
      {
         JRadioButton btn = buttons.get(map.getDrawLayerIndex());
         if (!btn.isSelected()) // avoid infinite loop
            btn.setSelected(true);
      }
   }

   private void makeButtons()
   {
      removeAll();
      buttons.clear();
      group = new ButtonGroup();

      for (int i = 0; i < map.getLayers().size(); i++)
         addButton(i);
      buttons.get(0).setSelected(true); // select the terrain button
   }

   private JRadioButton addButton(final int layer)
   {
      JRadioButton btn = new JRadioButton(layer + "");
      btn.setToolTipText("Select to make layer " + layer + " the target when drawing");
      btn.addActionListener(this);

      group.clearSelection();
      btn.setSelected(true); // select the button by default when created

      buttons.put(layer, btn);
      group.add(btn);
      add(btn, 0); // add buttons in reverse order, so the bottom layer is on the bottom
      revalidate();

      return btn;
   }

   @Override
   public void actionPerformed(final ActionEvent e)
   {
      int layer = Integer.parseInt(((JRadioButton) e.getSource()).getText());
      map.setDrawLayerIndex(layer);
   }
}
