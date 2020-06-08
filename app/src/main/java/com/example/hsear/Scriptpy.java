package com.example.hsear;

import com.google.ar.core.AugmentedImage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;

class Scriptpy extends MainActivity {
    public static boolean main(AugmentedImage img1) {
        int abc = 0;
    try {
        String prg= "import numpy as np\n" +
                "import cv2\n" +
                "import sys\n" +
                "\n" +
                "img1 = cv2.imread('box.png',0)          # queryImage\n" +
                "img2 = cv2.imread(sys.argv[1],0) # trainImage\n" +
                "\n" +
                "\n" +
                "orb = cv2.ORB()\n" +
                "\n" +
                "\n" +
                "kp1, des1 = orb.detectAndCompute(img1,None)\n" +
                "kp2, des2 = orb.detectAndCompute(img2,None)\n" +
                "\n" +
                "\n" +
                "bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=True)\n" +
                "\n" +
                "\n" +
                "matches = bf.match(des1,des2)\n" +
                "\n" +
                "\n" +
                "matches = sorted(matches, key = lambda x:x.distance)\n" +
                "if(matches.distance < 20):\n" +
                "    return 1\n" +
                "else:\n" +
                "    return 0";

        BufferedWriter out = new BufferedWriter(new FileWriter("test1.py"));
        out.write(prg);
        out.close();
        Process p = Runtime.getRuntime().exec("python test1.py"+img1.getName());
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        abc = new Integer(in.readLine()).intValue();
        } catch(Exception e){}
    if (abc == 1)
        return true;
    else
        return false;

    }
}