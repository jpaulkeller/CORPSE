package lotro.raid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import lotro.models.Character;
import lotro.models.Klass;
import lotro.models.Skill;
import lotro.models.Stats;
import lotro.my.reports.Report;
import model.NumericMap;
import web.HTMLUtils;

public class CharacterHTML
{
   private JTextPane htmlPane;
   private JTextPane rawPane;
   private StatPanel statPanel;
   
   private List<Character> sorted = new ArrayList<Character>();
   private int raidLevel;
   private int raidSize;
   private final StringBuilder html = new StringBuilder();

   private NumericMap<Skill, Integer> skillTotals = new NumericMap<Skill, Integer> (Integer.class);
   private NumericMap<Stats, Integer> statTotals  = new NumericMap<Stats, Integer> (Integer.class);

   public CharacterHTML (final StatPanel statPanel)
   {
      this.statPanel = statPanel;
      
      htmlPane = new JTextPane();
      htmlPane.setBackground (null);
      htmlPane.setEditable (false);
      htmlPane.setEditorKit (new HTMLEditorKit());
      
      rawPane = new JTextPane();
      rawPane.setBackground (null);
      rawPane.setEditable (false);
   }
   
   public JTextPane getHtmlPane()
   {
      return htmlPane;
   }
   
   public JTextPane getRawPane()
   {
      return rawPane;
   }
   
   public void updateReport (final Collection<Character> characters,
                             final String title,
                             final int theRaidLevel, final int theRaidSize)
   {
      clear();
      sorted.addAll (characters);
      Collections.sort (sorted); // by name
      
      this.raidLevel = theRaidLevel;
      this.raidSize = theRaidSize;
      
      html.append ("");
      
      html.append ("<meta http-equiv=\"content-type\" content=\"text-html; charset=utf-8\">\n");
      html.append ("<table border=\"1\" cellpadding=\"3\" cellspacing=\"1\">\n");
      html.append ("<tbody>\n");

      addHeader (title);
      showSkills();
      // showGroups();

      html.append ("</tbody>\n");
      html.append ("</table>\n");
      
      try
      {
         htmlPane.setText (html.toString());
         rawPane.setText (html.toString());
      }
      catch (Exception x) 
      {
         System.err.println (x.getMessage());
      }
   }
   
   private void clear()
   {
      sorted.clear();
      statTotals.clear();
      skillTotals.clear();
      html.setLength (0);
   }
   
   private void addHeader (final String title)
   {
      StringBuilder sb = new StringBuilder();
      
      sb.append ("  <tr bgcolor=yellow>\n");
      sb.append ("    <td align=center>Character</td>\n");
      sb.append ("    <td align=center>Level</td>\n");
      sb.append ("    <td align=center>Class</td>\n");
      sb.append ("    <td align=center>B/P/E</td>\n");
      int span = 4; 
      
      for (Stats stat : statPanel.getSelected())
      {
         sb.append ("    <td align=center>" + stat + "</td>\n");
         span++;
      }

      for (Skill skill : Skill.values())
      {
         String tag = HTMLUtils.getTip (skill.getAbbrev(), skill.toString());
         sb.append ("    <td align=center>" + tag + "</td>\n");
         span++;
      }
      sb.append ("  </tr>\n\n");
      
      html.append ("  <tr bgcolor=yellow><td colspan=" + span + " align=center><b>" +
                   title + "</b></td></tr>\n");
      html.append (sb);
   }
   
   private void showSkills()
   {
      for (Character ch : sorted)
      {
         html.append ("  <tr bgcolor=white>\n");
         html.append ("    <td>" + Report.getLink (ch) + "</td>\n");
         html.append ("    <td align=center>" + ch.getLevel() + "</td>\n");
         html.append ("    <td>" + ch.getKlass() + "</td>\n");
         
         addStat (ch, Stats.Block, Stats.Parry, Stats.Evade);
         
         for (Stats stat : statPanel.getSelected())
            addStat (ch, stat);
            
         if (ch.getKlass() != Klass.Unknown)
            for (Skill skill : Skill.values())
            {
               int score = ch.getScore (skill, raidLevel, raidSize);
               skillTotals.plus (skill, score);
               html.append ("    <td align=center>" + score + "</td>\n");
            }
         html.append ("  </tr>\n\n");
      }
      
      html.append ("  <tr>\n");
      html.append ("    <td bgcolor=white align=center colspan=3>");
      html.append ("<b><font color=blue>AVERAGE</font></b> or <b>TOTAL</b></td>\n");

      addStatTotal (4000, 3500, 3000, Stats.Block, Stats.Parry, Stats.Evade);
      for (Stats stat : statPanel.getSelected())
         addStatTotal (0, 0, 0, stat);
      /*
      addStatTotal (4500, 3500, 2500, Stats.Morale);
      addStatTotal (3500, 3000, 2500, Stats.Armour);
      addStatTotal (2500, 2000, 1500, Stats.Acid);
      */
      
      for (Skill skill : Skill.values())
      {
         int total = skillTotals.getInt (skill);
         String color = getSkillColor (total);
         html.append ("    <td bgcolor=" + color + " align=center><b>" +
                      total + "</b></td>\n");
      }
      html.append ("  </tr>\n\n");
   }
   
   private void addStat (final Character ch, final Stats... stats)
   {
      int total = 0;
      for (Stats stat : stats)
      {
         total += ch.getStat (stat);
         statTotals.plus (stat, ch.getStat (stat));
      }
      html.append ("    <td align=center>" + total + "</td>\n");
   }
   
   private void addStatTotal (final int green, final int yellow, final int orange,
                              final Stats... stats)
   {
      int total = 0;
      for (Stats stat : stats)
         total += statTotals.getInt (stat);
      int avg = sorted.isEmpty() ? 0 : total / sorted.size();
      float level = raidLevel / 60f;
      String color = getColor (avg, Math.round (green * level),
                               Math.round (yellow * level),
                               Math.round (orange * level));
      html.append ("    <td bgcolor=" + color + " align=center><font color=blue><b>" +
                   avg + "</b></font></td>\n");
   }
   
   // TBD: depends on target instance
   
   private String getSkillColor (final int groupTotal)
   {
      return getColor (groupTotal, 125, 100, 75);
   }

   private String getColor (final int score, 
                            final int green, final int yellow, final int orange)
   {
      if      (score >= green)  return "#CCFFCC"; // light green
      else if (score >= yellow) return "#FFFFBB"; // light yellow
      else if (score >= orange) return "#FFCC88"; // light orange
      return "#FFBBBB"; // pink
   }
}
