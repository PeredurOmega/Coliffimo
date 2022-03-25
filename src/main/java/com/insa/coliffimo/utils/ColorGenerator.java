package com.insa.coliffimo.utils;

import java.awt.*;
import java.util.ArrayList;

public class ColorGenerator {

    public ArrayList<Color> generateColorList(int size) {
        ArrayList<Color> colors = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            colors.add(new Color(Color.HSBtoRGB(((float) (i)) / size, 1.0f, 1.0f)));
        }
        return colors;
    }
}
