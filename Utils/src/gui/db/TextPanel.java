package gui.db;

import file.FileUtils;
import gui.form.TextAreaItem;
import gui.form.ValueChangeEvent;
import gui.form.ValueChangeListener;
import gui.wizard.Wizard;
import gui.wizard.WizardPanel;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

public class TextPanel extends WizardPanel implements ItemListener
{
   private Wizard wiz;
   private Object callback;
   private Method onNextMethod;

   private TextAreaItem text;
   private JCheckBox wrap;

   public TextPanel (final Wizard wiz, 
                     final Object callback,
                     final Method onNextMethod)
   {
      this.wiz = wiz;
      this.callback = callback;
      this.onNextMethod = onNextMethod;

      text = new TextAreaItem (null);
      text.setLineWrap (false);
      text.addValueChangeListener (new TextListener());
      add (text.getComponent(), BorderLayout.CENTER);

      wrap = new JCheckBox ("Wrap Long Lines", false);
      wrap.addItemListener (this);
      add (wrap, BorderLayout.SOUTH);
   }
   
   void update (final File file)
   {
      if (file != null && file.exists() && file.isFile())
      {
         setBorder (BorderFactory.createTitledBorder ("File Contents"));
         text.setInitialValue (FileUtils.getText (file));
      }
      else
         setBorder (BorderFactory.createTitledBorder ("Enter Data"));
   }

   // implement ItemListener
   public void itemStateChanged (final ItemEvent e)
   {
      text.setLineWrap (wrap.isSelected());
   }

   @Override
   public void onEntry()
   {
      wiz.enablePrev (true);
      wiz.enableNext (text.getValue() != null);
   }

   @Override
   public void onNext()
   {
      if (onNextMethod != null)
         try
         {
            onNextMethod.invoke (callback, text.getValue());
         }
         catch (Exception x)
         {
            x.printStackTrace();
         }
   }

   class TextListener implements ValueChangeListener
   {
      public void valueChanged (final ValueChangeEvent e)
      {
         wiz.enableNext (text.getValue() != null);
      }
   }
}
