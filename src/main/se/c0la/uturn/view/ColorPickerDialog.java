package se.c0la.uturn.view;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Window;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import static javax.swing.BorderFactory.*;

public class ColorPickerDialog extends JDialog implements ActionListener
{
    private Color currentColor = null;

    public ColorPickerDialog(Window parent)
    {
        super(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createEmptyBorder(3,3,3,3));

        List<Color> colors = new ArrayList<Color>();
        int steps = 4;
        for (int r = 0; r < steps; r++) {
            for (int g = 0; g < steps; g++) {
                for (int b = 0; b < steps; b++) {
                    colors.add(new Color(r*0xFF/(steps-1),
                                         g*0xFF/(steps-1),
                                         b*0xFF/(steps-1)));
                }
            }
        }

        int side = (int)Math.round(Math.sqrt(steps*steps*steps));
        for (int x = 0; x < side; x++) {
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));

            for (int y = 0; y < side; y++) {
                JButton button = new JButton();
                button.setContentAreaFilled(false);
                button.setOpaque(true);
                button.setMaximumSize(new Dimension(25, 25));
                button.setMinimumSize(new Dimension(25, 25));
                button.setPreferredSize(new Dimension(25, 25));
                button.setBackground(colors.get(x*side + y));
                button.addActionListener(this);
                rowPanel.add(button);
            }

            panel.add(rowPanel);
        }

        add(panel);

        setTitle("Color picker");
        setModal(true);
        setSize(new Dimension(400, 300));
        pack();
    }

    public Color getColor()
    {
        return currentColor;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (!(e.getSource() instanceof JButton)) {
            return;
        }

        JButton source = (JButton)e.getSource();
        this.currentColor = source.getBackground();

        setVisible(false);
    }
}
