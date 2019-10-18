package com.company.lab5;

import java.io.Serializable;

public class Picture implements Serializable {
    private int height, width;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Picture(int height, int width) {
        this.width = width;
        this.height = height;
    }
    public String getHeightWidth() {
        return height + ", " + width;
    }
    public String say() {
        return "//Тут могла бы быть ваша картинка//";
    }
}
