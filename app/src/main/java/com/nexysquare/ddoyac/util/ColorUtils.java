package com.nexysquare.ddoyac.util;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ColorUtils {

    public static String[] COLORS_KOR = {"분홍", "노랑", "하양", "연두", "갈색", "파랑", "주황", "초록", "투명", "빨강", "회색", "자주", "보라", "청록", "진한", "검정", "남색"};

    private static ColorUtils _instance;
    private ColorUtils(){}
    public static ColorUtils getInstance(){
        if(_instance == null){
            _instance = new ColorUtils();
        }
        return _instance;
    }

    /**
     * Initialize the color list that we have.
     */

    //List of colors in app database
    private ArrayList<ColorName> initColorList() {
        ArrayList<ColorName> colorList = new ArrayList<ColorName>();

        colorList.add(new ColorName("White", 0xF0, 0xF8, 0xFF)); //AliceBlue
        colorList.add(new ColorName("White, yellow", 0xFA, 0xEB, 0xD7));//AntiqueWhite
        colorList.add(new ColorName("Blue, white", 0x00, 0xFF, 0xFF)); //Aqua
        colorList.add(new ColorName("Green, white", 0x7F, 0xFF, 0xD4));//Aquamarine
        colorList.add(new ColorName("White", 0xF0, 0xFF, 0xFF));//Azure
        colorList.add(new ColorName("White, yellow", 0xF5, 0xF5, 0xDC));//Beige
        colorList.add(new ColorName("white, yellow", 0xFF, 0xE4, 0xC4)); //Bisque
        colorList.add(new ColorName("Black", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("white, yellow", 0xFF, 0xEB, 0xCD));//BlanchedAlmond
        colorList.add(new ColorName("Blue", 0x00, 0x00, 0xFF));
        colorList.add(new ColorName("Purple, pink", 0x8A, 0x2B, 0xE2));//BlueViolet
        colorList.add(new ColorName("Brown", 0xA5, 0x2A, 0x2A));
        colorList.add(new ColorName("Yellow, Brown", 0xDE, 0xB8, 0x87)); //BurlyWood
        colorList.add(new ColorName("Blue", 0x5F, 0x9E, 0xA0)); //CadetBlue
        colorList.add(new ColorName("Green", 0x7F, 0xFF, 0x00)); //Chartreuse
        colorList.add(new ColorName("Brown, red", 0xD2, 0x69, 0x1E)); //Chocolate
        colorList.add(new ColorName("Red", 0xFF, 0x7F, 0x50)); //Coral
        colorList.add(new ColorName("Blue", 0x64, 0x95, 0xED));//CornflowerBlue
        colorList.add(new ColorName("white, yellow", 0xFF, 0xF8, 0xDC));//Cornsilk
        colorList.add(new ColorName("red", 0xDC, 0x14, 0x3C)); //Crimson
        colorList.add(new ColorName("blue", 0x00, 0xFF, 0xFF));//Cyan
        colorList.add(new ColorName("Blue", 0x00, 0x00, 0x8B));//DarkBlue
        colorList.add(new ColorName("Blue", 0x00, 0x8B, 0x8B));//DarkCyan
        colorList.add(new ColorName("Yellow, brown", 0xB8, 0x86, 0x0B));//DarkGoldenRod
        colorList.add(new ColorName("Gray, white", 0xA9, 0xA9, 0xA9)); //DarkGray
        colorList.add(new ColorName("Green", 0x00, 0x64, 0x00)); //Green
        colorList.add(new ColorName("green, yellow", 0xBD, 0xB7, 0x6B)); //DarkKhaki
        colorList.add(new ColorName("purple, pink", 0x8B, 0x00, 0x8B));//DarkMagenta
        colorList.add(new ColorName("Green, yellow", 0x55, 0x6B, 0x2F));//DarkOliveGreen
        colorList.add(new ColorName("yellow, brown, red", 0xFF, 0x8C, 0x00));//DarkOrange
        colorList.add(new ColorName("purple, pink", 0x99, 0x32, 0xCC));//DarkOrchid
        colorList.add(new ColorName("red", 0x8B, 0x00, 0x00));//DarkRed
        colorList.add(new ColorName("red", 0xE9, 0x96, 0x7A));//DarkSalmon
        colorList.add(new ColorName("Green", 0x8F, 0xBC, 0x8F));//DarkSeaGreen
        colorList.add(new ColorName("Blue, Purple", 0x48, 0x3D, 0x8B));//DarkSlateBlue
        colorList.add(new ColorName("Blue, Green", 0x2F, 0x4F, 0x4F));//DarkSlateGray
        colorList.add(new ColorName("blue", 0x00, 0xCE, 0xD1));//DarkTurquoise
        colorList.add(new ColorName("Purple, pink", 0x94, 0x00, 0xD3));//DarkViolet
        colorList.add(new ColorName("Pink, red", 0xFF, 0x14, 0x93));//DeepPink
        colorList.add(new ColorName("Blue", 0x00, 0xBF, 0xFF));//DeepSkyBlue

        colorList.add(new ColorName("White, Gray", 0x69, 0x69, 0x69));//DimGray

        colorList.add(new ColorName("Blue", 0x1E, 0x90, 0xFF));//DodgerBlue

        colorList.add(new ColorName("Red", 0xB2, 0x22, 0x22));//FireBrick

        colorList.add(new ColorName("White", 0xFF, 0xFA, 0xF0));//FloralWhite

        colorList.add(new ColorName("Green", 0x22, 0x8B, 0x22));//ForestGreen

        colorList.add(new ColorName("Pink", 0xFF, 0x00, 0xFF));//Fuchsia

        colorList.add(new ColorName("White", 0xDC, 0xDC, 0xDC));//Gainsboro

        colorList.add(new ColorName("White", 0xF8, 0xF8, 0xFF));//GhostWhite

        colorList.add(new ColorName("yellow", 0xFF, 0xD7, 0x00));//Gold

        colorList.add(new ColorName("yellow, brown", 0xDA, 0xA5, 0x20));//GoldenRod

        colorList.add(new ColorName("Gray, white", 0x80, 0x80, 0x80));//Gray

        colorList.add(new ColorName("Green", 0x00, 0x80, 0x00));

        colorList.add(new ColorName("Green, Yellow", 0xAD, 0xFF, 0x2F)); //GreenYellow

        colorList.add(new ColorName("Green, white", 0xF0, 0xFF, 0xF0));//HoneyDew

        colorList.add(new ColorName("Pink, red", 0xFF, 0x69, 0xB4));//HotPink

        colorList.add(new ColorName("Red", 0xCD, 0x5C, 0x5C));//IndianRed

        colorList.add(new ColorName("Purple, Pink", 0x4B, 0x00, 0x82));//Indigo

        colorList.add(new ColorName("yellow, white", 0xFF, 0xFF, 0xF0));//Ivory

        colorList.add(new ColorName("yellow, white", 0xF0, 0xE6, 0x8C));//Khaki

        colorList.add(new ColorName("white", 0xE6, 0xE6, 0xFA));//Lavender

        colorList.add(new ColorName("white", 0xFF, 0xF0, 0xF5));//LavenderBlush

        colorList.add(new ColorName("Green", 0x7C, 0xFC, 0x00));//LawnGreen

        colorList.add(new ColorName("white, yellow", 0xFF, 0xFA, 0xCD));//LemonChiffon

        colorList.add(new ColorName("white, blue", 0xAD, 0xD8, 0xE6));//LightBlue

        colorList.add(new ColorName("red", 0xF0, 0x80, 0x80));//LightCoral

        colorList.add(new ColorName("white, blue", 0xE0, 0xFF, 0xFF));//LightCyan

        colorList.add(new ColorName("white, yellow", 0xFA, 0xFA, 0xD2));//LightGoldenRodYellow

        colorList.add(new ColorName("white", 0xD3, 0xD3, 0xD3)); //LightGray

        colorList.add(new ColorName("Green, white", 0x90, 0xEE, 0x90));//LightGreen

        colorList.add(new ColorName("white, Pink", 0xFF, 0xB6, 0xC1));//LightPink

        colorList.add(new ColorName("red, white", 0xFF, 0xA0, 0x7A));//LightSalmon

        colorList.add(new ColorName("Green, blue", 0x20, 0xB2, 0xAA));//LightSeaGreen

        colorList.add(new ColorName("white, Blue", 0x87, 0xCE, 0xFA)); //LightSkyBlue

        colorList.add(new ColorName("blue, white", 0x77, 0x88, 0x99)); //LightSlateGray

        colorList.add(new ColorName("Blue, wHITE", 0xB0, 0xC4, 0xDE));//LightSteelBlue

        colorList.add(new ColorName("WHITE, Yellow", 0xFF, 0xFF, 0xE0));//LightYellow

        colorList.add(new ColorName("Green", 0x00, 0xFF, 0x00));//Lime

        colorList.add(new ColorName("Green", 0x32, 0xCD, 0x32));//LimeGreen

        colorList.add(new ColorName("White", 0xFA, 0xF0, 0xE6));//Linen

        colorList.add(new ColorName("Pink, Purple", 0xFF, 0x00, 0xFF)); //Magenta

        colorList.add(new ColorName("red", 0x80, 0x00, 0x00)); //Maroon

        colorList.add(new ColorName("Green", 0x66, 0xCD, 0xAA)); //MediumAquaMarine

        colorList.add(new ColorName("Blue", 0x00, 0x00, 0xCD)); //MediumBlue

        colorList.add(new ColorName("Purple, pink", 0xBA, 0x55, 0xD3)); //MediumOrchid

        colorList.add(new ColorName("Purple, blue", 0x93, 0x70, 0xDB));//MediumPurple

        colorList.add(new ColorName("Green", 0x3C, 0xB3, 0x71)); //MediumSeaGreen

        colorList.add(new ColorName("Blue", 0x7B, 0x68, 0xEE)); //MediumSlateBlue

        colorList.add(new ColorName("Green", 0x00, 0xFA, 0x9A)); //MediumSpringGreen

        colorList.add(new ColorName("blue, white", 0x48, 0xD1, 0xCC)); //MediumTurquoise

        colorList.add(new ColorName("Red, pink", 0xC7, 0x15, 0x85));//MediumVioletRed

        colorList.add(new ColorName("Blue", 0x19, 0x19, 0x70));//MidnightBlue

        colorList.add(new ColorName("white", 0xF5, 0xFF, 0xFA)); //MintCream

        colorList.add(new ColorName("white, red", 0xFF, 0xE4, 0xE1));//MistyRose

        colorList.add(new ColorName("white, yellow", 0xFF, 0xE4, 0xB5)); //Moccasin

        colorList.add(new ColorName("white, yellow", 0xFF, 0xDE, 0xAD)); //NavajoWhite

        colorList.add(new ColorName("blue", 0x00, 0x00, 0x80));//Navy

        colorList.add(new ColorName("white", 0xFD, 0xF5, 0xE6)); //OldLace

        colorList.add(new ColorName("green", 0x80, 0x80, 0x00));//Olive

        colorList.add(new ColorName("green", 0x6B, 0x8E, 0x23)); //OliveDrab

        colorList.add(new ColorName("Orange", 0xFF, 0xA5, 0x00));//Orange

        colorList.add(new ColorName("red", 0xFF, 0x45, 0x00)); //OrangeRed

        colorList.add(new ColorName("pink", 0xDA, 0x70, 0xD6)); //Orchid

        colorList.add(new ColorName("Yellow, white", 0xEE, 0xE8, 0xAA)); //PaleGoldenRod

        colorList.add(new ColorName("Green, white", 0x98, 0xFB, 0x98)); //PaleGreen

        colorList.add(new ColorName("blue, white", 0xAF, 0xEE, 0xEE)); //PaleTurquoise

        colorList.add(new ColorName("red, pink", 0xDB, 0x70, 0x93)); //PaleVioletRed

        colorList.add(new ColorName("white, yellow", 0xFF, 0xEF, 0xD5)); //PapayaWhip

        colorList.add(new ColorName("white, yellow", 0xFF, 0xDA, 0xB9)); //PeachPuff

        colorList.add(new ColorName("Brown, red", 0xCD, 0x85, 0x3F)); //Peru

        colorList.add(new ColorName("Pink", 0xFF, 0xC0, 0xCB));

        colorList.add(new ColorName("Pink", 0xDD, 0xA0, 0xDD)); //Plum

        colorList.add(new ColorName("Blue, white", 0xB0, 0xE0, 0xE6)); //PowderBlue

        colorList.add(new ColorName("Purple", 0x80, 0x00, 0x80));

        colorList.add(new ColorName("Red", 0xFF, 0x00, 0x00));

        colorList.add(new ColorName("Red, white", 0xBC, 0x8F, 0x8F)); //RosyBrown

        colorList.add(new ColorName("Blue", 0x41, 0x69, 0xE1)); //RoyalBlue

        colorList.add(new ColorName("Brown, red", 0x8B, 0x45, 0x13)); //SaddleBrown

        colorList.add(new ColorName("Red", 0xFA, 0x80, 0x72)); //Salmon

        colorList.add(new ColorName("Brown, yellow", 0xF4, 0xA4, 0x60)); //SandyBrown

        colorList.add(new ColorName("Green", 0x2E, 0x8B, 0x57)); //SeaGreen

        colorList.add(new ColorName("White", 0xFF, 0xF5, 0xEE));//SeaShell

        colorList.add(new ColorName("RED, Brown", 0xA0, 0x52, 0x2D)); //Sienna

        colorList.add(new ColorName("White", 0xC0, 0xC0, 0xC0)); //Silver

        colorList.add(new ColorName("Blue, white", 0x87, 0xCE, 0xEB)); //SkyBlue

        colorList.add(new ColorName("Blue", 0x6A, 0x5A, 0xCD)); //SlateBlue

        colorList.add(new ColorName("BLUE, Gray", 0x70, 0x80, 0x90));//SlateGray

        colorList.add(new ColorName("WHITE", 0xFF, 0xFA, 0xFA));//Snow

        colorList.add(new ColorName("Green", 0x00, 0xFF, 0x7F)); //SpringGreen

        colorList.add(new ColorName("Blue", 0x46, 0x82, 0xB4));//SteelBlue

        colorList.add(new ColorName("yellow, brown, WHITE", 0xD2, 0xB4, 0x8C)); //Tan

        colorList.add(new ColorName("GREEN, blue", 0x00, 0x80, 0x80)); //Teal

        colorList.add(new ColorName("WHITE, pink", 0xD8, 0xBF, 0xD8)); //Thistle

        colorList.add(new ColorName("Red", 0xFF, 0x63, 0x47)); //Tomato

        colorList.add(new ColorName("BLUE", 0x40, 0xE0, 0xD0)); //Turquoise

        colorList.add(new ColorName("Pink", 0xEE, 0x82, 0xEE));//Violet

        colorList.add(new ColorName("White, yellow", 0xF5, 0xDE, 0xB3));//Wheat

        colorList.add(new ColorName("White", 0xFF, 0xFF, 0xFF));

        colorList.add(new ColorName("White", 0xF5, 0xF5, 0xF5));//WhiteSmoke

        colorList.add(new ColorName("Yellow", 0xFF, 0xFF, 0x00));

        colorList.add(new ColorName("Green", 0x9A, 0xCD, 0x32)); //YellowGreen


        //extra list
        colorList.add(new ColorName("blue", 0x8F, 0xB4, 0xC7));
        colorList.add(new ColorName("red", 0xd6, 0xB3, 0xB7));



        return colorList;
    }


    //List of colors in app database
    private ArrayList<ColorName> initColorListWithOriginName() {
        ArrayList<ColorName> colorList = new ArrayList<ColorName>();


        colorList.add(new ColorName("AliceBlue, White", 0xF0, 0xF8, 0xFF)); //AliceBlue

        colorList.add(new ColorName("AntiqueWhite, White, yellow", 0xFA, 0xEB, 0xD7));//AntiqueWhite

        colorList.add(new ColorName("Aqua, Blue, white", 0x00, 0xFF, 0xFF)); //Aqua

        colorList.add(new ColorName("Aquamarine, Green, white", 0x7F, 0xFF, 0xD4));//Aquamarine

        colorList.add(new ColorName("Azure,White", 0xF0, 0xFF, 0xFF));//Azure

        colorList.add(new ColorName("Beige, White, yellow", 0xF5, 0xF5, 0xDC));//Beige

        colorList.add(new ColorName("Bisque, white, yellow", 0xFF, 0xE4, 0xC4)); //Bisque

        colorList.add(new ColorName("Black", 0x00, 0x00, 0x00));

        colorList.add(new ColorName("BlanchedAlmond, white, yellow", 0xFF, 0xEB, 0xCD));//BlanchedAlmond

        colorList.add(new ColorName("Blue", 0x00, 0x00, 0xFF));

        colorList.add(new ColorName("BlueViolet, Purple, pink", 0x8A, 0x2B, 0xE2));//BlueViolet

        colorList.add(new ColorName("Brown", 0xA5, 0x2A, 0x2A));

        colorList.add(new ColorName("BurlyWood, Yellow, Brown", 0xDE, 0xB8, 0x87)); //BurlyWood

        colorList.add(new ColorName("CadetBlue, Blue", 0x5F, 0x9E, 0xA0)); //CadetBlue

        colorList.add(new ColorName("Chartreuse, Green", 0x7F, 0xFF, 0x00)); //Chartreuse

        colorList.add(new ColorName("Chocolate, Brown, red", 0xD2, 0x69, 0x1E)); //Chocolate

        colorList.add(new ColorName("Coral, Red", 0xFF, 0x7F, 0x50)); //Coral

        colorList.add(new ColorName("CornflowerBlue, Blue", 0x64, 0x95, 0xED));//CornflowerBlue

        colorList.add(new ColorName("Cornsilk, white, yellow", 0xFF, 0xF8, 0xDC));//Cornsilk

        colorList.add(new ColorName("Crimson, red", 0xDC, 0x14, 0x3C)); //Crimson

        colorList.add(new ColorName("Cyan, blue", 0x00, 0xFF, 0xFF));//Cyan

        colorList.add(new ColorName("DarkBlue, Blue", 0x00, 0x00, 0x8B));//DarkBlue

        colorList.add(new ColorName("DarkCyan, Blue", 0x00, 0x8B, 0x8B));//DarkCyan

        colorList.add(new ColorName("DarkGoldenRod, Yellow, brown", 0xB8, 0x86, 0x0B));//DarkGoldenRod

        colorList.add(new ColorName("DarkGray, Gray, white", 0xA9, 0xA9, 0xA9)); //DarkGray

        colorList.add(new ColorName("Green", 0x00, 0x64, 0x00)); //Green

        colorList.add(new ColorName("DarkKhaki, green, yellow", 0xBD, 0xB7, 0x6B)); //DarkKhaki

        colorList.add(new ColorName("DarkMagenta, purple, pink", 0x8B, 0x00, 0x8B));//DarkMagenta

        colorList.add(new ColorName("DarkOliveGreen, Green, yellow", 0x55, 0x6B, 0x2F));//DarkOliveGreen

        colorList.add(new ColorName("DarkOrange, yellow, brown, red", 0xFF, 0x8C, 0x00));//DarkOrange

        colorList.add(new ColorName("DarkOrchid, purple, pink", 0x99, 0x32, 0xCC));//DarkOrchid

        colorList.add(new ColorName("DarkRed, red", 0x8B, 0x00, 0x00));//DarkRed

        colorList.add(new ColorName("DarkSalmon, red", 0xE9, 0x96, 0x7A));//DarkSalmon

        colorList.add(new ColorName("DarkSeaGreen, Green", 0x8F, 0xBC, 0x8F));//DarkSeaGreen

        colorList.add(new ColorName("DarkSlateBlue, Blue, Purple", 0x48, 0x3D, 0x8B));//DarkSlateBlue

        colorList.add(new ColorName("DarkSlateGray, Blue, Green", 0x2F, 0x4F, 0x4F));//DarkSlateGray

        colorList.add(new ColorName("DarkTurquoise, blue", 0x00, 0xCE, 0xD1));//DarkTurquoise

        colorList.add(new ColorName("DarkViolet, Purple, pink", 0x94, 0x00, 0xD3));//DarkViolet

        colorList.add(new ColorName("DeepPink, Pink, red", 0xFF, 0x14, 0x93));//DeepPink

        colorList.add(new ColorName("DeepSkyBlue, Blue", 0x00, 0xBF, 0xFF));//DeepSkyBlue

        colorList.add(new ColorName("DimGray, White, Gray", 0x69, 0x69, 0x69));//DimGray

        colorList.add(new ColorName("DodgerBlue, Blue", 0x1E, 0x90, 0xFF));//DodgerBlue

        colorList.add(new ColorName("FireBrick, Red", 0xB2, 0x22, 0x22));//FireBrick

        colorList.add(new ColorName("FloralWhite, White", 0xFF, 0xFA, 0xF0));//FloralWhite

        colorList.add(new ColorName("ForestGreen, Green", 0x22, 0x8B, 0x22));//ForestGreen

        colorList.add(new ColorName("Fuchsia, Pink", 0xFF, 0x00, 0xFF));//Fuchsia

        colorList.add(new ColorName("Gainsboro, White", 0xDC, 0xDC, 0xDC));//Gainsboro

        colorList.add(new ColorName("GhostWhite, White", 0xF8, 0xF8, 0xFF));//GhostWhite

        colorList.add(new ColorName("Gold, yellow", 0xFF, 0xD7, 0x00));//Gold

        colorList.add(new ColorName("GoldenRod, yellow, brown", 0xDA, 0xA5, 0x20));//GoldenRod

        colorList.add(new ColorName("Gray, Gray, white", 0x80, 0x80, 0x80));//Gray

        colorList.add(new ColorName("Green", 0x00, 0x80, 0x00));

        colorList.add(new ColorName("GreenYellow, Green, Yellow", 0xAD, 0xFF, 0x2F)); //GreenYellow

        colorList.add(new ColorName("HoneyDew, Green, white", 0xF0, 0xFF, 0xF0));//HoneyDew

        colorList.add(new ColorName("HotPink, Pink, red", 0xFF, 0x69, 0xB4));//HotPink

        colorList.add(new ColorName("IndianRed, Red", 0xCD, 0x5C, 0x5C));//IndianRed

        colorList.add(new ColorName("Indigo, Purple, Pink", 0x4B, 0x00, 0x82));//Indigo

        colorList.add(new ColorName("Ivory, yellow, white", 0xFF, 0xFF, 0xF0));//Ivory

        colorList.add(new ColorName("Khaki, yellow, white", 0xF0, 0xE6, 0x8C));//Khaki

        colorList.add(new ColorName("Lavender, white", 0xE6, 0xE6, 0xFA));//Lavender

        colorList.add(new ColorName("LavenderBlush, white", 0xFF, 0xF0, 0xF5));//LavenderBlush

        colorList.add(new ColorName("LawnGreen, Green", 0x7C, 0xFC, 0x00));//LawnGreen

        colorList.add(new ColorName("LemonChiffon, white, yellow", 0xFF, 0xFA, 0xCD));//LemonChiffon

        colorList.add(new ColorName("LightBlue, white, blue", 0xAD, 0xD8, 0xE6));//LightBlue

        colorList.add(new ColorName("LightCoral, red", 0xF0, 0x80, 0x80));//LightCoral

        colorList.add(new ColorName("LightCyan, white, blue", 0xE0, 0xFF, 0xFF));//LightCyan

        colorList.add(new ColorName("LightGoldenRodYellow, white, yellow", 0xFA, 0xFA, 0xD2));//LightGoldenRodYellow

        colorList.add(new ColorName("LightGray, white", 0xD3, 0xD3, 0xD3)); //LightGray

        colorList.add(new ColorName("LightGreen, Green, white", 0x90, 0xEE, 0x90));//LightGreen

        colorList.add(new ColorName("LightPink, white, Pink", 0xFF, 0xB6, 0xC1));//LightPink

        colorList.add(new ColorName("LightSalmon, red, white", 0xFF, 0xA0, 0x7A));//LightSalmon

        colorList.add(new ColorName("LightSeaGreen, Green, blue", 0x20, 0xB2, 0xAA));//LightSeaGreen

        colorList.add(new ColorName("LightSkyBlue, white, Blue", 0x87, 0xCE, 0xFA)); //LightSkyBlue

        colorList.add(new ColorName("LightSlateGray, blue, white", 0x77, 0x88, 0x99)); //LightSlateGray

        colorList.add(new ColorName("LightSteelBlue, Blue, wHITE", 0xB0, 0xC4, 0xDE));//LightSteelBlue

        colorList.add(new ColorName("LightYellow, WHITE, Yellow", 0xFF, 0xFF, 0xE0));//LightYellow

        colorList.add(new ColorName("Lime, Green", 0x00, 0xFF, 0x00));//Lime

        colorList.add(new ColorName("LimeGreen, Green", 0x32, 0xCD, 0x32));//LimeGreen

        colorList.add(new ColorName("Linen, White", 0xFA, 0xF0, 0xE6));//Linen

        colorList.add(new ColorName("Magenta, Pink, Purple", 0xFF, 0x00, 0xFF)); //Magenta

        colorList.add(new ColorName("Maroon, red", 0x80, 0x00, 0x00)); //Maroon

        colorList.add(new ColorName("MediumAquaMarine, Green", 0x66, 0xCD, 0xAA)); //MediumAquaMarine

        colorList.add(new ColorName("MediumBlue, Blue", 0x00, 0x00, 0xCD)); //MediumBlue

        colorList.add(new ColorName("MediumOrchid, Purple, pink", 0xBA, 0x55, 0xD3)); //MediumOrchid

        colorList.add(new ColorName("MediumPurple, Purple, blue", 0x93, 0x70, 0xDB));//MediumPurple

        colorList.add(new ColorName("MediumSeaGreen, Green", 0x3C, 0xB3, 0x71)); //MediumSeaGreen

        colorList.add(new ColorName("MediumSlateBlue, Blue", 0x7B, 0x68, 0xEE)); //MediumSlateBlue

        colorList.add(new ColorName("MediumSpringGreen, Green", 0x00, 0xFA, 0x9A)); //MediumSpringGreen

        colorList.add(new ColorName("MediumTurquoise, blue, white", 0x48, 0xD1, 0xCC)); //MediumTurquoise

        colorList.add(new ColorName("MediumVioletRed, Red, pink", 0xC7, 0x15, 0x85));//MediumVioletRed

        colorList.add(new ColorName("MidnightBlue, Blue", 0x19, 0x19, 0x70));//MidnightBlue

        colorList.add(new ColorName("MintCream, white", 0xF5, 0xFF, 0xFA)); //MintCream

        colorList.add(new ColorName("MistyRose, white, red", 0xFF, 0xE4, 0xE1));//MistyRose

        colorList.add(new ColorName("Moccasin, white, yellow", 0xFF, 0xE4, 0xB5)); //Moccasin

        colorList.add(new ColorName("NavajoWhite, white, yellow", 0xFF, 0xDE, 0xAD)); //NavajoWhite

        colorList.add(new ColorName("Navy, blue", 0x00, 0x00, 0x80));//Navy

        colorList.add(new ColorName("OldLace, white", 0xFD, 0xF5, 0xE6)); //OldLace

        colorList.add(new ColorName("Olive, green", 0x80, 0x80, 0x00));//Olive

        colorList.add(new ColorName("OliveDrab, green", 0x6B, 0x8E, 0x23)); //OliveDrab

        colorList.add(new ColorName("Orange, Orange", 0xFF, 0xA5, 0x00));//Orange

        colorList.add(new ColorName("OrangeRed, red", 0xFF, 0x45, 0x00)); //OrangeRed

        colorList.add(new ColorName("Orchid, pink", 0xDA, 0x70, 0xD6)); //Orchid

        colorList.add(new ColorName("PaleGoldenRod, Yellow, white", 0xEE, 0xE8, 0xAA)); //PaleGoldenRod

        colorList.add(new ColorName("PaleGreen, Green, white", 0x98, 0xFB, 0x98)); //PaleGreen

        colorList.add(new ColorName("PaleTurquoise, blue, white", 0xAF, 0xEE, 0xEE)); //PaleTurquoise

        colorList.add(new ColorName("PaleVioletRed, red, pink", 0xDB, 0x70, 0x93)); //PaleVioletRed

        colorList.add(new ColorName("PapayaWhip, white, yellow", 0xFF, 0xEF, 0xD5)); //PapayaWhip

        colorList.add(new ColorName("PeachPuff, white, yellow", 0xFF, 0xDA, 0xB9)); //PeachPuff

        colorList.add(new ColorName("Peru, Brown, red", 0xCD, 0x85, 0x3F)); //Peru

        colorList.add(new ColorName("Pink", 0xFF, 0xC0, 0xCB));

        colorList.add(new ColorName("Plum, Pink", 0xDD, 0xA0, 0xDD)); //Plum

        colorList.add(new ColorName("PowderBlue, Blue, white", 0xB0, 0xE0, 0xE6)); //PowderBlue

        colorList.add(new ColorName("Purple", 0x80, 0x00, 0x80));

        colorList.add(new ColorName("Red", 0xFF, 0x00, 0x00));

        colorList.add(new ColorName("RosyBrown, Red, white", 0xBC, 0x8F, 0x8F)); //RosyBrown

        colorList.add(new ColorName("RoyalBlue, Blue", 0x41, 0x69, 0xE1)); //RoyalBlue

        colorList.add(new ColorName("SaddleBrown, Brown, red", 0x8B, 0x45, 0x13)); //SaddleBrown

        colorList.add(new ColorName("Salmon, Red", 0xFA, 0x80, 0x72)); //Salmon

        colorList.add(new ColorName("SandyBrown, Brown, yellow", 0xF4, 0xA4, 0x60)); //SandyBrown

        colorList.add(new ColorName("SeaGreen, Green", 0x2E, 0x8B, 0x57)); //SeaGreen

        colorList.add(new ColorName("SeaShell, White", 0xFF, 0xF5, 0xEE));//SeaShell

        colorList.add(new ColorName("Sienna, RED, Brown", 0xA0, 0x52, 0x2D)); //Sienna

        colorList.add(new ColorName("Silver, White", 0xC0, 0xC0, 0xC0)); //Silver

        colorList.add(new ColorName("SkyBlue, Blue, white", 0x87, 0xCE, 0xEB)); //SkyBlue

        colorList.add(new ColorName("SlateBlue, Blue", 0x6A, 0x5A, 0xCD)); //SlateBlue

        colorList.add(new ColorName("SlateGray, BLUE, Gray", 0x70, 0x80, 0x90));//SlateGray

        colorList.add(new ColorName("Snow, WHITE", 0xFF, 0xFA, 0xFA));//Snow

        colorList.add(new ColorName("SpringGreen, Green", 0x00, 0xFF, 0x7F)); //SpringGreen

        colorList.add(new ColorName("SteelBlue, Blue", 0x46, 0x82, 0xB4));//SteelBlue

        colorList.add(new ColorName("Tan, yellow, brown, WHITE", 0xD2, 0xB4, 0x8C)); //Tan

        colorList.add(new ColorName("Teal, GREEN, blue", 0x00, 0x80, 0x80)); //Teal

        colorList.add(new ColorName("Thistle, WHITE, pink", 0xD8, 0xBF, 0xD8)); //Thistle

        colorList.add(new ColorName("Tomato, Red", 0xFF, 0x63, 0x47)); //Tomato

        colorList.add(new ColorName("Turquoise, BLUE", 0x40, 0xE0, 0xD0)); //Turquoise

        colorList.add(new ColorName("Violet, Pink", 0xEE, 0x82, 0xEE));//Violet

        colorList.add(new ColorName("Wheat, yellow", 0xF5, 0xDE, 0xB3));//Wheat

        colorList.add(new ColorName("White", 0xFF, 0xFF, 0xFF));

        colorList.add(new ColorName("WhiteSmoke, White", 0xF5, 0xF5, 0xF5));//WhiteSmoke

        colorList.add(new ColorName("Yellow", 0xFF, 0xFF, 0x00));

        colorList.add(new ColorName("YellowGreen, Green", 0x9A, 0xCD, 0x32)); //YellowGreen


        //extra list
        colorList.add(new ColorName("blue", 0x8F, 0xB4, 0xC7));
        colorList.add(new ColorName("red", 0xd6, 0xB3, 0xB7));



        return colorList;
    }


    public String[]  getListOfColorInKorean(String eng_color){
//        ArrayList<String> colors = new ArrayList<>();

        HashMap<String, String[]> map = new HashMap<>();
        map.put("white", new String[] {"하양"});
        map.put("red", new String[] {"빨강", "주황", "자주","갈색"});
        map.put("yellow", new String[] {"노랑", "주황"});
        map.put("brown", new String[] {"갈색", "노랑", "주황"});
        map.put("pink", new String[] {"분홍", "자주", "보라"});
        map.put("green", new String[] {"초록", "청록", "연두"});
        map.put("gray", new String[] {"진한", "회색"});
        map.put("purple", new String[] {"자주", "보라"});
        map.put("blue", new String[] {"파랑", "청록", "남색"});
        map.put("black", new String[] {"검정", "남색"});

//        String[] colors = map.get("eng_color");

        return map.get(eng_color.toLowerCase());
    }


    public ArrayList<String> getListOfColorInKoreanFromEngList(String colors_str){
        String trim = colors_str.replaceAll(" ", "");
        String[] split = trim.split(",");
        ArrayList<String> colors = new ArrayList<>();
        for(String s : split){

            Log.d("getListOfColorInKoreanFromEngList", s );
//            List<String> list = new ArrayList<String>();
            String[] ks = getListOfColorInKorean(s);
            if(ks!=null){
                colors.addAll(Arrays.asList(ks));
            }

        }

        return colors;
    }


    public Map imageColour(Bitmap image) throws Exception {

        int height = image.getHeight();
        int width = image.getWidth();

        Map m = new HashMap();

        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {

                int rgb = image.getPixel(i, j);
                int[] rgbArr = getRGBArr(rgb);

                //Checking if picture has color
//                if (!isGray(rgbArr)) {

                Integer counter = (Integer) m.get(rgb);
                if (counter == null)
                    counter = 0;
                counter++;
                m.put(rgb, counter);

//                }
            }
        }

        return m;
    }

    public static int[] getRGBArr(int pixel) {

        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        return new int[]{red, green, blue};

    }

    /**
     * Get the closest color name from our list
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
//Finding color name from RGB Value
    public String getNearestColorNameFromRgb(int r, int g, int b) {
        ArrayList<ColorName> colorList = initColorListWithOriginName();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
//            closestColorHex="#"+ To00Hex(closestMatch.getR())+ To00Hex(closestMatch.getG())+To00Hex(closestMatch.getB());
//            int colorInt = Color.parseColor(closestColorHex);
//            closestColorView.setBackgroundColor(colorInt);
//            closestColorHexText.setText("HEX: " + closestColorHex);
            return closestMatch.getName();
        } else {
            return "No matched color name.";
        }
    }

    public String getNearestColorHexFromRgb(int r, int g, int b) {
        ArrayList<ColorName> colorList = initColorListWithOriginName();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return "#"+ To00Hex(closestMatch.getR())+ To00Hex(closestMatch.getG())+To00Hex(closestMatch.getB());

        } else {
            return "#ffffff";
        }
    }

    public int getNearestColorIntFromRgb(int r, int g, int b) {
        ArrayList<ColorName> colorList = initColorListWithOriginName();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            String closestColorHex = "#"+ To00Hex(closestMatch.getR())+ To00Hex(closestMatch.getG())+To00Hex(closestMatch.getB());
            int colorInt = Color.parseColor(closestColorHex);

            return colorInt;
        } else {
            return 0;
        }
    }
    public static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length()-2, hex.length());
    }


    public String getColorNameFromHex(int hexColor) {
        int r = (hexColor & 0xFF0000) >> 16;
        int g = (hexColor & 0xFF00) >> 8;
        int b = (hexColor & 0xFF);
        return getNearestColorNameFromRgb(r, g, b);
    }

    public int colorToHex(Color c) {

        int RGB = getIntFromColor(c.RED, c.GREEN, c.BLUE);
        return Integer.decode("0x"
                + Integer.toHexString(RGB).substring(2));
    }
    public int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
    public String getColorNameFromColor(Color color) {
        return getNearestColorNameFromRgb(color.RED, color.GREEN,
                color.BLUE);
    }

    /**
     * SubClass of ColorUtils. In order to lookup color name
     *
     * @author Xiaoxiao Li
     *
     */
    public class ColorName {
        public int r, g, b;
        public String name;

        public ColorName(String name, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        public int computeMSE(int pixR, int pixG, int pixB) {
            return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3);
        }

        public String getName() {
            return name;
        }
        public int getR() {
            return r;
        }
        public int getG() {
            return g;
        }
        public int getB() {
            return b;
        }
    }



    public ArrayList<Integer> convertedColorListToIdx(ArrayList<String> colors){
        ArrayList<Integer> converted = new ArrayList<>();

        for(String color : colors){
            converted.add(getColorIdx(color));
        }
        return converted;
    }

    public static int getColorIdx(String _color){
//        StringBuilder sb = new StringBuilder();
//        sb.append(color);


        String color = normalizeNfc(_color);
//        Log.d("Object compare", "=====target color : " + color + " ================");
        for(int i = 0 ; i < ColorUtils.COLORS_KOR.length ; i++){

            String compared = normalizeNfc(ColorUtils.COLORS_KOR[i]);
//            Log.d("Object compare", compared + ":" +color+ ":::result:"+ " hashcode:" + compared.hashCode() +"/" + color.hashCode() + ":: " +compared.equals(color));

            if(compared.equals(color)){
                return i;
            }

//            if(Objects.equals(ColorUtils.COLORS_KOR[i], color)){
//
//            }
        }


        return -1;
    }
    public static String normalizeNfc(String unNormalMailBoxName) {
        if (!Normalizer.isNormalized(unNormalMailBoxName, Normalizer.Form.NFC)) {
            return Normalizer.normalize(unNormalMailBoxName, Normalizer.Form.NFC);
        }
        return unNormalMailBoxName;
    }
}