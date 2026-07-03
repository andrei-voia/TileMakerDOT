package io;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import data.NoteColor;

public class NoteConfigPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JTextField inputField;
	private JComboBox<NoteColor> colorDropdown;
    private JPanel colorPreview;

    public NoteConfigPanel(String defaultText, NoteColor existingColor) {
        //vertical stacked layout configuration
        setLayout(new GridLayout(4, 1, 4, 4));

        add(new JLabel("Enter Map Annotation Note:"));
        inputField = new JTextField(defaultText, 20);
        add(inputField);
        
        //auto focus the field as soon as the JOptionPane opens
        inputField.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                //run later ensures the Swing thread has finished drawing the component
                SwingUtilities.invokeLater(() -> {
                    inputField.requestFocusInWindow();
                });
            }

            @Override 
            public void ancestorRemoved(AncestorEvent event) {}
            
            @Override 
            public void ancestorMoved(AncestorEvent event) {}
        });

        add(new JLabel("Select Note Label Color:"));

        //row alignment wrapper for dropdown + Square Preview block
        JPanel selectionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        //populate the dropdown directly with your values from the defined enum
        colorDropdown = new JComboBox<>(NoteColor.values());
        colorDropdown.setSelectedIndex(0);
        
        //build the color preview square component
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(22, 22));
        colorPreview.setOpaque(true); 
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        selectionRow.add(colorDropdown);
        selectionRow.add(colorPreview);
        add(selectionRow);

        colorDropdown.addActionListener(e -> updateSquarePreview());

        //sync initial view states
        if (existingColor != null) {
            colorDropdown.setSelectedItem(existingColor);
        } else {
            updateSquarePreview();
        }
    }

    private void updateSquarePreview() {
    	NoteColor selected = (NoteColor) colorDropdown.getSelectedItem();
        if (selected != null) {
            colorPreview.setBackground(selected.getColorRGB());
        }
    }
    
    public String getNoteText() {
        return inputField.getText().trim();
    }

    public NoteColor getSelectedColor() {
        return (NoteColor) colorDropdown.getSelectedItem();
    }
}
