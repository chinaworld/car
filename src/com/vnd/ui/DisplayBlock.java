package com.vnd.ui;

import java.awt.BorderLayout;
import java.awt.Label;

import javax.swing.*;

public class DisplayBlock extends JPanel {
    private static final long serialVersionUID = 1L;

    Display display;
    String name;

    public DisplayBlock(String name, Display display) {
        this.display = display;
        this.name = name;

        setLayout(new BorderLayout(0, 0));
        add(display);
        Label lbl = new Label(name);
        lbl.setAlignment(Label.CENTER);
        lbl.setSize(display.getWidth(), 16);
        add(lbl, BorderLayout.SOUTH);
    }
}
