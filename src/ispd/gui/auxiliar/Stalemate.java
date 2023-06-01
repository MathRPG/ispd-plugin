/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <phk@FreeBSD.ORG> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Poul-Henning Kamp
 * ----------------------------------------------------------------------------
 */
package ispd.gui.auxiliar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

import javax.swing.JPanel;

public class Stalemate extends JPanel
        implements MouseMotionListener, MouseListener, Runnable {

    private static final int                 ENEMY_DEFAULT_POSITION = -50;
    private static final int                 OPTION_WIDTH           = 60;
    private static final int                 OPTION_HEIGHT          = 20;
    private static final int                 START_MONEY            = 500;
    private static final byte[]              GRASS                  = {
            -119, 80, 78, 71, 13, 10, 26, 10, 0
            , 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 20, 0, 0, 0, 20, 8, 6, 0, 0,
            0, -115, -119, 29, 13, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8,
            124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0
            , 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 4, -71, 73, 68, 65, 84,
            56, -115, 61, -108, 93, 110, 26, 105, 2, 69, 15, 95, 85, 81, 85,
            -90, -88, 2, 23, -104, -30, 39, 54, -79, 77, 66, 108, 37, -18,
            -106, -94, 25, 37, -99, 121, 106, -51, 30, 102, 3, -77, -113, -52
            , 10, 122, 1, -77, -123, 89, 68, 30, 58, -119, -14, 35, -59, 113,
            100, 59, 14, 113, 8, -104, 24, -16, 0, 6, 92, -123, 113, -3, -50,
            -125, 123, -6, -15, -22, 62, 92, -23, -24, -22, -92, -2, -11, -17
            , -65, 36, -87, 84, -118, 56, -114, 73, -89, -45, -68, 122, -11,
            -118, -99, -99, 29, 26, -115, 6, -121, -121, -121, 52, -101, 77,
            -50, -50, -50, 40, 22, -117, -100, -100, -76, -88, 84, 42, -124,
            97, -120, -82, -21, -8, -66, 79, 46, -105, -93, -37, -3, -114, 97
            , 24, 4, -95, -113, -12, -16, -87, -6, 60, 12, 67, -110, 36, 97,
            50, -103, 112, -1, -2, 125, 20, 69, -95, -43, 106, -79, -79, -79,
            -63, 112, 56, -92, -37, -19, 34, 73, 18, -86, -70, -62, 116, 58,
            67, -120, 20, 66, 8, 126, -1, -3, 53, -110, 36, -120, -94, 16, 89
            , -106, 25, 94, 12, 17, -74, 109, 83, -85, -43, -48, 52, 13, 85,
            85, -55, -25, -13, -12, -5, 125, -98, 62, 125, -54, -15, -15, 49,
            -105, -105, -105, 60, 120, -16, -128, -31, 112, -120, 16, 2, 89,
            -106, 1, 65, -95, -80, 70, -87, 84, -96, 80, 40, -32, -5, 62, -98
            , -25, 113, -25, -50, 29, -124, -17, -5, -124, 97, 72, 28, -57,
            -72, -82, -53, 112, 56, -60, -74, 109, -34, -67, 123, -57, -67,
            123, -9, -120, -29, -104, 31, 63, 126, -16, -16, -31, 30, 23, 23,
            23, 40, -118, 66, -81, -41, -93, -41, -21, -47, 108, 54, 49, 77,
            -109, -23, 116, 74, 20, 69, 76, -89, 83, -60, -1, -53, -47, 104,
            -124, 16, -126, 126, -65, 15, 64, 58, -99, -26, -28, -28, 4, 93,
            -41, 1, 16, 66, -112, 78, -89, 41, -83, -107, 41, 20, 10, 76, 38,
            19, -94, 40, -95, -35, 110, -29, 56, 14, -49, -98, 61, 67, -110,
            36, 68, -83, 86, -93, 94, -81, 99, 24, 6, -102, -90, 81, -85, -43
            , -120, -29, -104, 92, 46, 71, 16, 68, -20, -17, -73, -8, -11,
            -41, -65, 115, 116, 116, -124, 127, 19, 114, 126, 126, -114, 44,
            -53, 104, -102, -122, 101, 89, 120, -98, -57, -54, -54, 10, 95,
            79, 91, 120, -98, -121, 48, 77, -109, -45, -45, 54, 81, -108, -80
            , 88, 44, -55, -25, 109, -30, 24, 92, 119, -63, -6, 122, 29, -37,
            94, -31, -61, -121, -113, -20, -19, -3, -116, 44, -53, -92, 82,
            41, -106, -53, 37, -118, -94, -96, 105, 105, 76, -45, 96, 60, 25,
            -95, -86, -73, 89, -6, -23, 111, -103, -25, -71, 92, 30, 33, 4,
            -82, -21, -94, -21, 58, -118, -94, 34, -53, 10, -85, -85, -85,
            100, -77, 38, 66, 8, 0, 86, 86, -78, 44, 22, 30, -106, -103, 35,
            99, 104, -116, -57, 99, 42, 85, 7, 77, -45, -16, -3, 27, 36, 41,
            -123, -112, 101, -103, -43, -43, 85, 54, 55, 55, -15, 60, 15, -41
            , 117, 57, 62, 62, 38, -118, 34, 84, 85, -59, -13, 60, -62, 48,
            -92, -45, -23, -96, -86, 42, -118, -94, -16, -23, -45, 39, 76,
            -45, 36, 12, 67, 14, 14, 14, 80, 85, -107, 32, 8, 88, 46, -105, 8
            , 69, 81, -24, -9, -5, -4, -10, -37, 127, 48, 12, -125, -47, 104,
            68, 62, -97, 103, 56, 28, 50, 24, 12, -40, -34, -34, -58, -9, 125
            , 44, -53, -62, -78, -78, -68, 126, -3, -127, -57, -113, 31, -13,
            -30, -59, 11, 36, 73, 66, -106, 101, 94, -66, 124, 73, -79, 88,
            100, 52, 26, 33, -3, -29, -97, -113, -97, -33, -82, -115, 41,
            -107, 74, 52, -101, 77, 46, 47, 39, 64, -62, 114, 121, 77, 28, 71
            , -56, -78, -60, -39, 89, -113, 32, 8, -7, -27, -105, -65, -14,
            -26, -51, 27, -42, -41, 107, 116, -69, 93, -18, -35, 111, -80,
            -74, -74, -58, 96, -48, 39, 8, 124, -60, -73, 111, -33, 120, -5,
            -10, 45, -90, 105, 82, -85, -43, 104, -75, 90, -24, -70, -114,
            105, -102, 127, -34, -27, -29, -57, -113, -40, -74, -115, -82,
            -21, 116, 58, 29, 118, 119, 119, -23, -11, 122, 100, -77, 89, 114
            , -71, 28, -11, 122, -99, 126, -65, -49, -109, 39, 79, 16, 97, 24
            , -77, -67, -67, -51, -42, -42, 22, -77, -39, -116, -13, -13, 115
            , 14, 15, 63, -111, -53, -103, 84, -85, 101, 100, 89, 112, -9,
            -18, 6, 59, 59, 77, -126, 32, -32, -12, -12, -108, 124, 62, -113,
            -82, -21, 72, -110, -124, -82, -21, -20, -17, -17, 83, -83, 86,
            25, 14, -121, -56, -103, 76, -122, -59, 98, -63, -51, -51, 13, 0,
            -66, -17, 35, -124, -32, -53, -105, 47, 68, 81, -60, -18, -18, 46
            , -13, -7, -100, 110, -73, -53, -55, -55, 23, -10, -10, -10, 24,
            -115, 70, -108, -53, -27, -37, -1, 125, -3, -54, 114, -71, -92,
            -47, -40, -94, 90, 45, 35, -124, 16, 124, -1, -2, -99, -59, 98,
            65, 38, -109, -63, -78, -84, 63, 56, 94, -78, -35, -40, -30, 114,
            58, 65, -43, -46, 92, 95, 123, 24, -122, -127, -21, -70, 116, 58,
            29, -110, 36, 33, 73, 18, -82, -82, -82, 112, 28, -121, -125,
            -125, 3, -38, -19, 54, -14, -83, 69, 116, -126, 32, 96, 54, -101,
            -95, -21, 42, -114, -29, 112, -27, -50, 73, -110, -124, 48, 12,
            -79, 44, -117, -51, -69, 91, -56, -46, 15, 44, -53, 66, 81, 20,
            110, 124, -113, -9, -17, -33, -13, -45, -49, 15, -103, -49, -25,
            24, -122, 65, 20, 69, -120, -85, -85, 43, 20, 69, -95, 82, -87,
            32, 73, 18, -114, -29, 48, 30, -113, 113, 28, -121, 98, -79, 72,
            24, -6, -88, -86, -62, -15, -25, 35, -58, -109, -1, 50, -103, 76,
            -104, 78, -89, -88, -86, 74, 58, -99, 102, 48, 24, -112, 78, -89,
            -79, 109, -5, 22, -105, -103, -51, -95, -86, 42, -77, -39, 12,
            -53, -78, 16, 66, 16, -124, 62, -66, -17, 51, -103, -116, 0, -16,
            -68, 43, -126, 32, 32, -101, -51, 18, -57, 49, -21, -21, -21, 56,
            -114, -61, -58, -58, 6, -115, 70, -125, 110, -73, 75, 38, -109,
            97, 48, 24, 32, 52, 77, -93, 94, -81, 19, 4, 1, -13, -7, -100,
            -61, -61, 67, -106, -53, 37, -45, -23, -12, 15, -8, 37, 46, 46,
            46, 72, -91, 82, -116, -57, 99, 30, 61, 122, -60, -25, -49, -97,
            -71, -66, -66, 70, -110, 36, 92, -41, -59, 48, 12, -38, -19, 54,
            -90, 105, -34, -6, -48, -13, 60, 58, -99, 14, 81, 20, -47, 104,
            52, -112, 36, -119, 74, -59, -31, -6, -38, -29, -20, -20, -116,
            114, -71, 76, -87, 84, 98, 111, 111, -113, 94, -81, 71, -75, 90,
            -27, -24, -24, 8, -37, -74, 73, -110, -28, 86, 107, -91, 18, -101
            , -101, -101, -4, 15, 106, -56, 51, 107, 98, 34, -27, 53, 0, 0, 0
            , 0, 73, 69, 78, 68, -82, 66, 96, -126,
            };
    private static final byte[]              STONE                  = {
            -119, 80, 78, 71, 13, 10, 26, 10, 0
            , 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 20, 0, 0, 0, 20, 8, 6, 0, 0,
            0, -115, -119, 29, 13, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8,
            124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0
            , 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 4, -81, 73, 68, 65, 84,
            56, -115, 61, -108, -35, -114, -45, 6, 26, 64, -113, 29, 59, -74,
            99, 123, -30, 16, 59, -98, -4, 76, 96, -121, 10, -119, -120, 84,
            72, -128, 68, 85, -72, -25, -126, 27, -82, 65, 60, 75, 47, 118,
            -9, 21, -112, 120, -104, 74, -83, -96, -102, -108, 45, -123, 89,
            -40, 97, -104, 97, 126, 26, -51, -28, 111, 112, -100, 56, 118,
            -20, -40, 73, -10, 2, -54, 11, 124, -6, 62, 125, -25, 28, -31,
            -97, -1, -2, -41, 58, 77, 83, -110, 36, 33, 8, 2, -78, 44, 35, 8,
            2, 20, 69, 97, 58, -99, 18, -57, 49, -114, -29, -16, -24, -47, 35
            , 60, -49, -61, 117, 93, 52, 77, 99, 60, 30, 99, 109, 20, -23,
            -11, 122, 28, 29, 29, 49, -99, 78, 57, 61, 61, 69, -44, 117, -99,
            -43, 106, -59, -42, -42, 22, -90, 105, -94, 105, 26, -86, -86,
            -94, -21, 58, -75, 90, 13, -57, 113, -8, -8, -15, 35, -17, -33,
            -65, -89, 94, -81, 115, -7, -14, 101, 46, 46, 46, -24, -11, 122,
            24, -122, -127, -21, -70, -104, -90, -55, -10, -10, 54, -19, 118,
            27, -55, 52, 77, -46, 52, 69, 81, 20, 100, 89, 102, 52, 26, -95,
            40, 10, -123, 66, -127, -31, 112, 72, 16, 4, -56, -78, 76, -89,
            -45, -63, 52, 77, 114, -71, 28, -118, -94, 112, -27, -54, 21, -94
            , 40, 66, 85, 85, 90, -83, 22, -109, -55, -124, 126, -65, -113,
            -76, 94, -81, -47, 52, -115, 48, 12, 49, 12, -125, 36, 73, -66,
            13, -82, 86, -85, 124, -8, -16, -127, 123, -9, -18, -31, 121, 30,
            7, 7, 7, -8, -66, 79, -91, 82, -31, -8, -8, -104, 52, 89, 96, 89,
            22, -115, 70, 3, -33, -9, -103, 78, -89, 72, -78, 44, 83, 40, 20,
            -16, 125, 31, -37, -74, -87, 86, -85, 36, 73, 66, -65, -33, 39,
            12, 67, -118, -59, 34, -83, 86, -117, -109, -109, 19, -54, -27,
            50, -59, 98, -111, 44, -53, 56, 60, 60, 100, -107, 45, 49, 12,
            -125, 44, -53, -80, 109, -101, -37, -73, 111, 35, 25, -122, -127,
            101, 89, 100, 89, -122, -25, 121, 88, -106, -123, -25, 121, -40,
            -74, 77, 16, 4, 60, 126, -4, -104, 122, -67, -50, 120, 60, 102,
            -75, 90, 49, -97, -49, 9, -126, 0, -128, -63, 96, -64, -7, -7, 57
            , -61, -31, -112, -85, 87, -81, -30, 56, 14, -46, -97, 127, -2,
            -127, -29, 56, 28, 30, 30, -15, -30, -59, 11, -54, -27, 50, -77,
            -39, 12, -57, 113, 112, 93, -105, 40, -118, 120, -3, -6, 53, -11,
            122, -99, -55, 100, -126, 36, -119, 8, -62, -102, -63, -96, -57,
            -17, -65, -17, -112, -90, 75, 52, 77, -95, -37, 61, 37, 77, 83,
            -92, 95, 127, -7, -103, 52, -51, -80, -118, -105, -48, -75, 2,
            -109, -79, -49, -91, 75, 54, 87, -1, -15, 29, -19, 118, -101, 74,
            -91, 66, 102, 47, -24, -11, 122, -56, 114, -114, 69, 50, 71, -106
            , 4, 54, 93, 27, -124, 37, 27, 69, -115, 102, -77, 73, -83, 86,
            -59, -13, 60, -92, 31, 127, -4, -127, -109, -109, 46, -115, 70,
            -125, 116, -79, -2, 122, -78, -49, -26, -26, 38, -90, 105, 18,
            -122, 33, 105, -102, 80, -87, -40, 24, -122, -63, -1, -10, -2, 75
            , 20, 69, 108, 109, -43, 105, 108, 109, 114, -1, -2, 125, -18,
            -36, -2, 1, -33, -9, 57, 62, 62, 65, -6, -4, -7, 51, -70, -82,
            -93, 105, 26, -33, -73, -81, -45, -21, -11, 88, -83, 64, -106,
            101, -78, 44, 35, -105, -53, -111, 36, 9, -110, 36, 82, 40, 20,
            80, 20, -123, 40, -118, -48, 117, -99, -89, 79, -97, -78, 92, 46,
            -7, 91, -116, -69, 119, -17, 34, 30, 31, 31, 51, -99, 78, 121, -8
            , -16, 33, -43, 106, -107, 90, -83, -58, 98, -79, -64, 48, 12, 4,
            65, 64, 20, 69, -126, 32, 96, 52, 26, -111, 101, 25, -118, -94,
            -112, -53, -27, 16, 4, 1, -128, 56, -114, 41, 20, 10, -104, -90,
            -55, 104, 52, 66, 76, -110, 4, -37, -74, -15, 125, -97, -115,
            -115, 13, -30, 56, -26, -26, -51, -101, 108, 109, 109, -31, 56,
            14, -94, 40, 50, -97, -49, -55, -78, 12, 65, 16, 88, -81, -41, 8,
            -126, -128, -86, -86, 44, 22, 11, 68, 81, 100, 48, 24, -112, -53,
            -27, -80, 44, 11, -15, -42, -83, 91, -52, 102, 51, -126, 32, 96,
            119, 119, -105, 74, -91, -126, 36, 73, 72, -110, -124, 44, -53,
            -40, -74, 77, -83, 86, 67, -106, 101, -94, 40, 34, -97, -49, -77,
            -79, -79, 65, -106, 101, -8, -66, -113, -29, 56, 68, 81, -60, 114
            , -71, -60, 52, 77, 36, -57, 113, 48, 77, -117, 48, 12, 105, 54,
            -101, 95, 117, 11, -119, -94, -120, 82, -87, 68, -75, 90, 101,
            123, 123, -101, -65, -2, 58, 97, 50, -103, 16, 69, 17, -94, 40,
            18, -122, 33, -110, 36, 17, -57, 49, -13, -7, -100, -27, 114, 73,
            28, -57, -120, 127, 59, 59, 24, 12, 56, 58, 58, 98, 127, 127, -97
            , 119, -17, -34, 81, 42, -107, 72, -45, 20, -33, -9, -111, 101,
            -103, 122, -67, -114, 40, -118, -100, -98, -98, 50, 28, 14, -55,
            -27, 114, -84, 86, 43, -122, -61, 33, -82, -21, -46, 108, 54, 25,
            12, 6, -120, -117, -59, -126, 107, -41, -82, -79, -77, -77, 67,
            28, -57, 88, -106, -123, -82, -21, 100, 89, -122, -86, -86, 92,
            92, 92, -32, 121, 30, -123, 66, -127, 79, -97, 62, 33, 73, 18,
            -69, -69, -69, -12, -5, 125, -118, -59, 34, -27, 114, -103, 44,
            -53, 56, 59, 59, -93, 90, -83, 34, 41, -118, 66, -89, -13, 31, 94
            , -66, 124, -119, -128, -52, -115, 27, 55, -72, 115, -25, 14,
            -118, -94, -80, 88, 44, -120, -29, -104, -39, 108, 74, 20, -51,
            56, 59, 59, 67, -50, -117, -88, -86, -54, 116, 58, -91, 104, -103
            , 40, -118, 2, -21, 47, -104, 57, -114, -125, -44, -19, 118, 121,
            -13, -26, 13, -49, -98, 61, 99, 56, -16, -24, 118, -69, -68, 122,
            -11, 10, -41, 117, -47, 117, 29, 0, 65, 88, 127, 75, -100, 86,
            -56, 35, -118, 34, -29, -15, 103, -62, 48, -60, 117, 93, 86, 75,
            -111, 82, -87, -12, -27, -21, -77, -39, -116, -67, -67, -125, 111
            , -39, 122, -16, -32, 1, -83, 86, -117, 78, -89, -61, -13, -25,
            -49, 121, -5, -10, 45, -7, 124, -98, 124, 62, -113, -86, -86, -12
            , -5, 125, -10, -10, -10, 88, -81, -41, -76, -37, 109, 26, -115,
            -58, 55, 46, -25, -13, 57, 57, -45, -44, 126, 26, 14, -121, 60,
            121, -14, -124, 90, -75, -63, -2, -2, 62, -11, 122, -125, -21,
            -41, -81, -93, -86, 42, -98, -25, -15, -37, 111, 47, -23, 116,
            118, -66, 34, 99, 112, 126, 126, -50, -26, -90, -117, -86, 125,
            -119, -14, 122, 45, 112, 112, -16, 101, -87, -1, 3, -90, 108, 68,
            38, -127, -110, -70, 123, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96
            , -126,
            };
    private static final byte[]              WOOD                   = {
            -119, 80, 78, 71, 13, 10, 26, 10,
            0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 20, 0, 0, 0, 20, 8, 6, 0, 0
            , 0, -115, -119, 29, 13, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8,
            124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0
            , 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, 117, 73, 68, 65, 84,
            56, -115, -107, -108, 65, -114, 34, 49, 12, 69, -97, 93, -111, 40
            , 88, -80, -30, -92, 125, -80, 57, -52, 28, -123, 30, 9, 4, -119,
            127, 47, -86, 2, 9, 21, -48, 116, -92, -110, 74, 113, -4, -14,
            -65, -29, -60, -2, -2, -7, -110, -103, -47, 14, 73, -92, -108, 72
            , 41, 33, -119, 26, 23, 96, -126, -80, 37, 46, -77, 46, 14, -112,
            -114, -57, 35, 35, -96, -69, 51, 77, -45, 0, 40, -28, 70, 68, 16,
            18, 50, -93, -51, 78, 102, -122, 53, 59, -43, -1, 105, -102, 112,
            -9, 49, -48, 12, -103, 22, 80, 3, -108, 68, -86, 73, -18, -2,
            -128, -71, 59, 17, 65, -50, -71, 83, 47, -64, -128, 96, -79, -20,
            102, -53, -36, -70, 38, 34, 72, 57, 103, 104, 38, -19, -91, 46,
            -110, 58, 96, -91, -106, 82, 8, -127, -116, -34, -14, -27, 114,
            97, 100, -5, 126, -65, -113, 21, -82, 53, 92, -128, -125, 26, 50,
            24, -110, -40, -19, 118, -20, -9, -5, 78, -3, 34, 78, 4, 44, 7,
            54, 58, -27, -61, -31, 48, 84, 40, -119, -120, 24, 40, 4, 55,
            -127, -39, -14, 53, 27, 2, -92, 121, -98, 59, -48, -21, -126, -25
            , 81, 52, 51, -113, 26, -10, -106, 37, -111, 114, 46, -85, -94,
            -86, 70, -104, 57, 57, -33, 41, -91, 108, 122, -76, 38, -6, -28,
            -124, -39, -93, -115, -84, 2, -65, -65, -1, -11, -74, 86, -75,
            -110, 54, -11, 25, -59, 121, -119, -89, 121, -98, 31, -54, -86,
            -75, -38, -40, -11, -90, -116, -128, 32, 66, -32, 47, 6, -46, -77
            , 118, 79, 88, 109, -20, 82, -54, -48, -18, 39, 7, -23, 124, 62,
            127, -76, 12, -4, -86, 36, 111, 31, -121, -42, -14, -81, -128, 27
            , 79, -85, -94, -120, -40, -44, -17, -65, -128, -41, -21, -11,
            109, 107, -116, -58, -21, 61, -33, 0, -21, 93, 110, 71, 68, -112,
            82, -6, 104, -71, 58, -40, 0, 79, -89, -45, 80, 97, 59, 55, 2,
            -106, 82, -120, -120, -18, -39, 51, 51, -46, -19, 118, -5, -104,
            -16, -10, -90, -72, 119, 27, -42, -68, 31, -18, -40, 78, -57, -6,
            -83, 123, 82, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126,
            };
    private static final int                 SIZE                   = 300;
    private static final int                 MENU                   = 60;
    private static final Image[]             FIELD                  = {
            Toolkit.getDefaultToolkit().createImage(Stalemate.GRASS),
            Toolkit.getDefaultToolkit().createImage(Stalemate.STONE),
            Toolkit.getDefaultToolkit().createImage(Stalemate.WOOD),
            };
    private final        Collection<Element> items                  = new ArrayList<>(0);
    private final        Collection<Element> map                    = new ArrayList<>(0);
    private final        Dimension           preferredSize          = new Dimension(
            Stalemate.SIZE, Stalemate.SIZE);
    private final        Font                font                   = new Font("Tahoma", Font.BOLD, 12);
    private final        List<Element>       options                = this.makeOptions();
    private final        Stroke              stroke                 = new BasicStroke(3);
    private final        RandomGenerator     random                 = new Random();
    private final        List<Element>       enemies                = this.makeEnemies();
    private              int                 cursorX                = 0;
    private              int                 cursorY                = 0;
    private              int                 money                  = Stalemate.START_MONEY;
    private              int                 life                   = 100;
    private              int                 level                  = 1;
    private              int                 levelT                 = 0;
    private              int                 currentMap             = 1;
    private              boolean             isWaiting              = true;
    private              boolean             isRunning              = true;
    private              int                 enemyTypes             = 0;
    private              Element             selectedElement        = null;

    public Stalemate () {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        this.startGame();
    }

    private void startGame () {
        new Thread(this).start();
        new Thread(this::animationLogic).start();
    }

    private void animationLogic () {
        try {
            while (this.life > 0) {
                Thread.sleep(150);
                this.updateAllItems();
            }
        } catch (final InterruptedException ex) {
            Logger.getLogger(Stalemate.class.getName())
                  .log(Level.SEVERE, null, ex);
        }
    }

    private void updateAllItems () {
        for (final var e : this.items) {
            e.updateDrawing();
        }
    }

    private List<Element> makeEnemies () {
        final var enemies = new ArrayList<Element>(0);
        for (int i = 0; i < 10; i++) {
            enemies.add(new Element(
                    Stalemate.ENEMY_DEFAULT_POSITION,
                    Stalemate.ENEMY_DEFAULT_POSITION,
                    Element.OPTIONS
            ));
        }
        return enemies;
    }

    private List<Element> makeOptions () {
        final var options = new ArrayList<Element>(0);
        for (
                int i = 0, x = 5; i < Element.OPTIONS;
                i++, x += Stalemate.OPTION_WIDTH
        ) {
            options.add(new Element(x, Stalemate.OPTION_HEIGHT, i));
        }
        return options;
    }

    @Override
    protected void paintComponent (final Graphics g) {
        ((Graphics2D) g).setStroke(this.stroke);
        g.setFont(this.font);
        //Desenha tela do menu
        for (int x = 0; x < Stalemate.SIZE; x += 20) {
            g.drawImage(Stalemate.FIELD[2], x, 0, null);
            g.drawImage(Stalemate.FIELD[2], x, 20, null);
            g.drawImage(Stalemate.FIELD[2], x, 40, null);
        }
        // Desenha opções
        if (this.life >= 100) {
            g.setColor(Color.GREEN);
            g.drawString(" :D " + this.life, 130, 12);
        } else if (this.life > 50) {
            g.setColor(Color.BLUE);
            g.drawString(" :) " + this.life, 130, 12);
        } else if (this.life > 10) {
            g.setColor(Color.RED);
            g.drawString(" :( " + this.life, 130, 12);
        } else {
            g.setColor(Color.RED);
            g.drawString(" X( " + this.life, 130, 12);
        }
        g.setColor(Color.BLACK);
        g.drawString(" $ " + this.money + "     Nivel " + this.levelT, 10,
                     12
        );
        for (final Element item : this.options) {
            item.draw(g);
            g.drawString("$:" + item.value, item.x + 20, item.y + 10);
        }
        //Desenha mapa
        this.drawMap(g);
        synchronized (this.items) {
            // Desenhamos todos itens
            for (final Element item : this.items) {
                item.draw(g);
            }
        }
        // Desenha inimigos
        for (final Element item : this.enemies) {
            item.draw(g);
        }
        //Opções fora do jogo
        if (this.isWaiting) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(Stalemate.SIZE / 2 - 50, Stalemate.SIZE - 100,
                       100, 50
            );
            g.setColor(Color.BLACK);
            g.drawRect(Stalemate.SIZE / 2 - 50, Stalemate.SIZE - 100,
                       100, 50
            );
            g.drawString("Iniciar", Stalemate.SIZE / 2 - 20,
                         Stalemate.SIZE - 70
            );
        }
        if (this.selectedElement != null
            && this.cursorY > Stalemate.MENU
            && this.money >= this.selectedElement.value) {
            this.selectedElement.drawCursor(
                    this.cursorX - this.selectedElement.width / 2,
                    this.cursorY - this.selectedElement.height / 2,
                    g
            );
        }
        if (this.life > 0) {
            this.repaint();
        } else {
            g.setColor(Color.RED);
            g.drawString(" Fim de jogo! ",
                         Stalemate.SIZE / 2 - 20, Stalemate.SIZE - 70
            );
        }
    }

    @Override
    public Dimension getPreferredSize () {
        return this.preferredSize;
    }

    @Override
    public Dimension getMaximumSize () {
        return this.preferredSize;
    }

    @Override
    public Dimension getMinimumSize () {
        return this.preferredSize;
    }

    private void drawMap (final Graphics g) {
        g.setColor(Color.GREEN);
        //Desenha grama
        for (int y = Stalemate.MENU; y < Stalemate.SIZE; y += 20) {
            for (int x = 0; x < Stalemate.SIZE; x += 20) {
                g.drawImage(Stalemate.FIELD[0], x, y, null);
            }
        }
        switch (this.currentMap) {
            case 1:
                for (int y = 70; y < 170; y += 20) {
                    for (int x = 0; x < Stalemate.SIZE; x += 20) {
                        g.drawImage(Stalemate.FIELD[1], x, y, null);
                    }
                }
                break;
            case 2:
                for (int y = 70; y < 170; y += 20) {
                    for (int x = 0; x < Stalemate.SIZE / 2 + 10; x += 20) {
                        g.drawImage(Stalemate.FIELD[1], x, y, null);
                    }
                }
                for (int y = 160; y < Stalemate.SIZE; y += 20) {
                    g.drawImage(Stalemate.FIELD[1],
                                Stalemate.SIZE / 2 - 10, y, null
                    );
                }
                break;
            case 3:
                for (int y = 70; y < 170; y += 20) {
                    for (int x = 0; x < Stalemate.SIZE / 2 + 20; x += 20) {
                        g.drawImage(Stalemate.FIELD[1], x, y, null);
                    }
                }
                for (int y = 160; y < Stalemate.SIZE; y += 20) {
                    g.drawImage(Stalemate.FIELD[1],
                                Stalemate.SIZE / 2 - 10, y, null
                    );
                    g.drawImage(Stalemate.FIELD[1],
                                Stalemate.SIZE / 2 + 10, y, null
                    );
                }
                for (int x = Stalemate.SIZE / 2 + 30; x < Stalemate.SIZE; x += 20) {
                    g.drawImage(Stalemate.FIELD[1], x, Stalemate.SIZE - 40
                            , null);
                    g.drawImage(Stalemate.FIELD[1], x, Stalemate.SIZE - 20
                            , null);
                }
                break;
            case 4:
                for (int y = 80; y < Stalemate.SIZE; y += 20) {
                    g.drawImage(Stalemate.FIELD[1], 0, y, null);
                    g.drawImage(Stalemate.FIELD[1], 20, y, null);
                }
                for (int x = 40; x < 240; x += 20) {
                    g.drawImage(Stalemate.FIELD[1], x, Stalemate.SIZE - 20
                            , null);
                    g.drawImage(Stalemate.FIELD[1], x, Stalemate.SIZE - 40
                            , null);
                }
                for (int y = 120; y < Stalemate.SIZE - 40; y += 20) {
                    g.drawImage(Stalemate.FIELD[1], 180, y, null);
                    g.drawImage(Stalemate.FIELD[1], 200, y, null);
                    g.drawImage(Stalemate.FIELD[1], 220, y, null);
                }
                for (int x = 180; x < Stalemate.SIZE; x += 20) {
                    g.drawImage(Stalemate.FIELD[1], x, 80, null);
                    g.drawImage(Stalemate.FIELD[1], x, 100, null);
                }
                break;
        }
    }

    @Override
    public void mouseClicked (final MouseEvent me) {
        if (me.getY() < Stalemate.MENU) {
            for (final Element item : this.options) {
                item.isSelected = false;
            }
            this.selectedElement = null;
            for (final Element item : this.options) {
                if (item.contains(me.getX(), me.getY())) {
                    item.isSelected      = true;
                    this.selectedElement = item;
                }
            }
        } else if (me.getY() > Stalemate.MENU) {
            if (this.selectedElement != null && this.money >= this.selectedElement.value) {
                boolean colisao = false;
                for (final Element element : this.items) {
                    if (element.contains(me.getX(), me.getY())) {
                        colisao = true;
                    }
                }
                if (!colisao) {
                    final Element el =
                            new Element(
                                    me.getX() - this.selectedElement.width / 2,
                                    me.getY() - this.selectedElement.height / 2,
                                    this.selectedElement.type
                            );
                    el.value = el.getPower();
                    this.items.add(el);
                    this.money -= this.selectedElement.value;
                    this.selectedElement.isSelected = false;
                    this.selectedElement            = null;
                }
            }
        }
    }

    @Override
    public void mousePressed (final MouseEvent me) {
        if (me.getButton() == MouseEvent.BUTTON2) {
            this.money += 100;
        }
        if (me.getButton() == MouseEvent.BUTTON1 && this.isWaiting) {
            if (me.getX() > Stalemate.SIZE / 2 - 50
                && me.getX() < Stalemate.SIZE / 2 + 50
                && me.getY() > Stalemate.SIZE - 100
                && me.getY() < Stalemate.SIZE - 50) {
                this.isWaiting = false;
            }
        }
    }

    @Override
    public void mouseReleased (final MouseEvent me) {
    }

    @Override
    public void mouseEntered (final MouseEvent me) {
        this.isRunning = true;
    }

    @Override
    public void mouseExited (final MouseEvent me) {
        this.isRunning = false;
    }

    @Override
    public void mouseDragged (final MouseEvent me) {
    }

    @Override
    public void mouseMoved (final MouseEvent me) {
        this.cursorX = me.getX();
        this.cursorY = me.getY();
    }

    @Override
    public void run () {
        try {
            this.runGame();
        } catch (final InterruptedException ex) {
            Logger.getLogger(Stalemate.class.getName()).log(Level.SEVERE,
                                                            null, ex
            );
        }
    }

    private void runGame () throws InterruptedException {
        int levelChange = 100;
        while (this.life > 0) {
            Thread.sleep(200);
            levelChange = this.runOneLevel(levelChange);
        }
    }

    private int runOneLevel (int levelChange) throws InterruptedException {
        while (this.isRunning && this.life > 0) {
            Thread.sleep(40);
            //Teste para mudança de nível
            levelChange = this.changeLevelIfNeeded(levelChange);
            //Movimentação dos inimigos
            levelChange = this.processEnemyMovement(levelChange);
            this.processCannonShots();
            this.changeEnemyTrajectories();
        }
        return levelChange;
    }

    private int changeLevelIfNeeded (int levelChange) throws InterruptedException {
        if (levelChange >= 100 && this.level < Element.TOTAL_ENEMIES) {
            levelChange = 0;
            this.startNew();
        }
        return levelChange;
    }

    private int processEnemyMovement (int levelChange) {
        boolean novoDisparo = true;
        for (final Element item : this.enemies) {
            if (item.isSelected) {
                item.x += (item.getSpeed() / 2) * item.shotX;
                item.y += (item.getSpeed() / 2) * item.shotY;
                novoDisparo = false;
            } else if (item.shotX != 0 || item.shotY != 0) {
                item.x += item.getSpeed() * item.shotX;
                item.y += item.getSpeed() * item.shotY;
                novoDisparo = false;
            }
            if (item.value <= 0) {
                this.money++;
                levelChange++;
                item.stop();
            } else if (item.x > Stalemate.SIZE || item.y > Stalemate.SIZE) {
                this.life -= 5;
                levelChange++;
                item.stop();
            }
        }
        if (novoDisparo) {
            this.restartEnemies();
        }
        return levelChange;
    }

    private void processCannonShots () {
        for (final var item : this.items) {
            final var e = this.enemies.stream()
                                      .filter(item::isTargetInRange)
                                      .findFirst()
                                      .orElse(null);

            if (e == null) {
                item.shotX = this.getShotX(item);
                item.shotY = this.getShotY(item);
                return;
            }

            item.shotX = this.getShotX(e);
            item.shotY = this.getShotY(e);

            e.value -= item.value;
            if (item.type == Element.ICE_CANNON) {
                e.isSelected = true;
            }
        }
    }

    private void changeEnemyTrajectories () {
        for (final var item : this.map) {
            item.change(this.enemies);
        }
    }

    private void startNew () throws InterruptedException {
        this.isWaiting = true;
        this.levelT++;
        this.currentMap = (this.currentMap + 1) % 4;

        this.money += 200;
        for (final var e : this.items) {
            this.money += e.getValor();
            this.life++;
        }

        synchronized (this.items) {
            this.items.clear();
        }

        while (this.isWaiting) {
            Thread.sleep(200);
        }

        this.map.clear();
        this.enemyTypes = 0;
        this.restartEnemies();

        switch (this.currentMap) {
            case 1 -> this.level++;
            case 2 -> this.map.add(
                    new Element(Stalemate.SIZE / 2, 80, 20, 100, 0, 1));
            case 3 -> {
                this.map.add(
                        new Element(Stalemate.SIZE / 2, 80, 20, 100, 0, 1));
                this.map.add(
                        new Element(10, Stalemate.SIZE - 20,
                                    Stalemate.SIZE - 20, 20, 1, 0
                        ));
            }
            case 4 -> {
                this.map.add(new Element(10, 80, 30, 100, 0, 1));
                this.map.add(new Element(10, Stalemate.SIZE - 30, 50, 30
                        , 1, 0));
                this.map.add(new Element(200, Stalemate.SIZE - 30, 50,
                                         30, 0, -1
                ));
                this.map.add(new Element(190, 80, 50, 30, 1, 0));
            }
        }
    }

    private void restartEnemies () {
        int dec = 0;
        for (final Element item : this.enemies) {
            if (this.currentMap <= 3) {
                item.x = dec -= 10;
                item.y = 80 + this.random.nextInt(80);
                item.setType(Element.OPTIONS + this.enemyTypes);
            }
            if (this.currentMap == 4) {
                item.x = dec -= 10;
                item.y = 80 + this.random.nextInt(40);
                item.setType(Element.OPTIONS + this.enemyTypes);
            }
        }
        this.enemyTypes++;
        if (this.enemyTypes >= this.level) {
            this.enemyTypes = 0;
        }
    }

    private int getShotX (final Element item) {
        return item.x + item.width / 2;
    }

    private int getShotY (final Element item) {
        return item.y + item.height / 2;
    }

    private class Element extends Rectangle {

        static final         int        VIOLET_CANNON = 0;
        static final         int        RED_CANNON    = 1;
        static final         int        GREEN_CANNON  = 2;
        static final         int        GRAY_CANNON   = 3;
        static final         int        ICE_CANNON    = 4;
        private static final Object[][] VALUES        = {
                { 25, 28, Color.MAGENTA, 100, 20, 2 },
                { 33, 33, Color.RED, 125, 15, 4 },
                { 33, 33, Color.GREEN, 200, 50, 2 },
                { 33, 33, Color.DARK_GRAY, Stalemate.START_MONEY, 10, 20 },
                { 25, 28, Color.BLUE, 100, 10, 1 },
                { 5, 5, Color.ORANGE, 100, 2 },
                { 3, 3, Color.BLACK, 150, 4 },
                { 8, 8, Color.PINK, 200, 10 },
                { 20, 20, Color.YELLOW, 2000, 4 }
        };
        private static final int        OPTIONS       = 5;
        private static final int        TOTAL_ENEMIES = 4;
        private static final byte[]     BLUE_0        = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8,
                6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0,
                -47, 73, 68, 65, 84, 72, -119, -27, -43, -53, 21, -125, 64, 8
                , 5, 80, -56, 73, 97, -42, 98, 37, 86, 98, 45, -23, 108, 92,
                36, -26, 0, -14, 121, -104, -104, 77, -40, -87, -64, 21, 71,
                29, -94, 127, -114, -47, 45, -32, 15, 0, -72, -74, -125, 120,
                19, 64, -11, -73, 22, 48, -65, -100, 121, -24, -13, 95, 64,
                52, -80, 71, 3, -86, 16, 31, 104, 66, -39, 51, 61, 2, -85, 72
                , -9, -49, -69, -3, -94, 73, 18, 96, 100, -96, 59, -111, 39,
                -41, -128, 44, 5, 38, -78, 8, 8, -12, 32, -119, 36, -117,
                -105, -83, 107, -6, -87, -80, -105, 113, -7, 36, 77, -24, -4,
                -102, -128, 16, 14, 100, 72, 1, -31, 64, -123, -60, 80, 3, 32
                , -86, 127, 43, 108, 26, -23, 0, 0, 4, -119, 33, 16, -128, 18
                , 68, -100, -34, 79, 126, -66, 51, -38, 59, -51, 94, -17, -4,
                -43, 55, 61, -44, 87, -74, 76, -49, -125, -27, 81, 66, 33,
                -32, -11, 96, 123, 113, 15, -111, 116, -72, 51, -39, 84, -43,
                4, 61, -104, -120, -122, -67, -24, -123, -121, -94, 117, -9,
                58, 13, 111, 24, -59, -5, 113, 93, 25, 27, 43, -30, 97, -100,
                64, 39, 72, 52, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        };
        private static final byte[]     BLUE_1        = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8,
                6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0,
                -28, 73, 68, 65, 84, 72, -119, -51, -107, 81, 18, -125, 48, 8
                , 68, -105, 78, 15, -42, -77, -28, 36, -98, -60, -77, -12,
                102, -12, -93, -38, 38, 27, 52, -128, -83, -29, -2, 48, 26,
                -32, 65, 48, 6, -72, -88, 52, 26, 32, 7, 0, -18, -40, 8, -60,
                -22, -64, 21, 127, 11, 1, -54, -62, 41, -38, -66, -1, 1, -92,
                5, 0, -64, 44, 33, -48, 8, -46, 39, 40, 102, -50, 93, -48, 30
                , 68, 27, 51, -45, -10, -81, -49, 54, -44, 13, -7, 2, -72,
                -48, 126, 54, -122, -109, 27, 66, -30, 110, -100, -118, 65,
                24, -26, -36, -78, -67, -46, 52, 113, -10, 76, -89, 120, 39,
                92, 117, -47, 97, 39, -71, -19, -30, -63, 15, 102, 21, -125,
                56, 62, -41, 4, 68, -56, 46, -30, -63, -101, 78, 62, -120, 52
                , -58, -102, 69, 15, 10, 67, 42, 66, 37, 59, 105, -18, 0,
                -111, 20, -128, -94, 104, 107, -99, 127, -31, 83, -18, -109,
                83, 110, -58, -116, 14, -35, -15, 28, -100, -87, -44, -52, 33
                , -11, -30, -12, 120, 63, 76, -49, 20, 104, 51, -121, -16,
                -30, -86, -54, -87, -85, -84, 78, -38, -60, 108, -28, 16, 0,
                -54, -117, -106, 44, -88, 55, -18, 62, 118, -13, 39, -36, -46
                , 103, -69, -2, -87, 23, 2, -67, 79, -31, 30, -67, 32, 126, 0
                , 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        };
        private static final byte[]     BLUE_2        = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8,
                6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0,
                -58, 73, 68, 65, 84, 72, -119, -27, -107, -39, 17, -61, 32,
                12, 68, -91, 76, 10, 75, 45, -82, -60, -107, -92, -106, 116,
                70, 62, -16, 1, 88, 98, 87, -15, 49, -98, -55, -2, 120, 108,
                29, 79, 8, 97, 68, -2, 89, 41, 26, -96, 59, 0, 116, 108, 4,
                98, -83, -128, -118, 127, -124, 0, -61, -60, 25, 82, -3, -3,
                0, 72, 13, -104, 21, 0, 33, 8, -69, -55, 93, 63, 98, 37, -120
                , -125, -21, 96, -9, 100, -105, 110, 8, 121, 107, -3, 36,
                -123, -68, 83, -18, 121, -49, 109, -79, -69, 78, -4, 74, -4,
                17, -122, -118, -75, 107, 123, 24, 79, -128, -4, -88, -69, 64
                , -48, 108, -32, 73, 67, 16, 118, 86, -69, 126, 76, -69, 114,
                -126, -10, 108, -84, -17, -80, -112, 75, -18, -109, -53, 111,
                -58, -74, 82, 47, 73, -17, 23, 96, -26, -48, -46, 56, -66,
                -14, -53, -8, -127, 32, 23, 96, -27, -48, -42, 56, -85, 112,
                -38, 84, 86, 38, -83, 98, -100, 28, 42, 34, -87, 53, 90, -78,
                -96, 108, -36, 19, -69, -15, 9, 61, 45, -19, 58, 83, 95, 11,
                31, 51, 105, 42, 126, 107, -14, 0, 0, 0, 0, 73, 69, 78, 68,
                -82, 66, 96, -126
        };
        private static final byte[]     RED_0         = {
                -119, 80, 78, 71, 13, 10, 26, 10
                , 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6
                , 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8,
                8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0,
                0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, 28,
                73, 68, 65, 84, 88, -123, -19, -104, 81, 14, 2, 33, 12, 68,
                11, 39, -13, 104, 123, 52, 111, -122, 63, -82, 97, -53, 76,
                -95, 45, 26, 77, 108, 98, 12, -124, -19, -68, -76, 118, 68,
                69, -14, -47, -78, 9, -54, 70, -128, 112, -82, 12, 4, -86, 64
                , 40, 95, 20, -62, 106, -127, 59, 103, -51, 0, 52, -67, 1,
                -105, -5, 33, 6, 0, -78, 118, -127, 120, 32, 40, 64, 22, 100,
                -75, 127, 16, -96, -96, 3, 99, -46, -87, -122, -85, 29, 72,
                -88, -87, -75, 62, -73, 18, 46, -120, 66, -60, 52, -100, 119,
                60, 66, -107, 96, 34, -70, 58, 111, -127, -24, 5, -40, 116,
                68, 60, 60, -30, 19, -37, -29, 15, 113, -58, 111, 66, 20, -11
                , 62, -37, -33, 1, 113, -7, -80, -49, 70, -48, 24, 97, 115,
                104, 44, -120, -31, -63, 38, -40, 53, -75, -127, 17, 69, 10,
                -62, 32, -32, 3, -56, 45, 81, 117, -116, -106, -64, -68, 8,
                -94, -119, -120, 28, 55, -84, -65, 98, -37, 104, -39, -27, 27
                , 64, 52, 4, 2, 48, 65, -32, 1, 114, -116, -127, -12, 16, 22,
                -64, 20, 100, -11, -21, 27, -127, -44, 126, 99, 2, 64, 65,
                -68, -9, 7, 13, 82, -99, 0, 51, -95, 101, -101, -24, 65, 106,
                0, -128, 9, -70, 125, -22, -44, -115, -38, -74, -98, -48, -24
                , 85, 34, 5, -127, 64, -62, 63, 7, -61, 16, -49, 82, -66, -84
                , 34, -40, -46, 101, -120, 6, 94, 23, 16, 5, 64, -49, -77, 40
                , 32, -55, 71, -29, -72, 127, -55, 125, -94, -56, -122, -1,
                23, -78, -15, 0, 89, -87, 89, 32, 112, 104, 89, -45, 0, 0, 0,
                0, 73, 69, 78, 68, -82, 66, 96, -126
        };
        private static final byte[]     RED_1         = {
                -119, 80, 78, 71, 13, 10, 26, 10
                , 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6
                , 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8,
                8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0,
                0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, 17,
                73, 68, 65, 84, 88, -123, -19, -42, 81, 14, -61, 32, 8, 6, 96
                , -12, 100, 59, 90, -113, -74, -101, -79, -105, -51, 88, -4,
                65, 64, 99, -74, 100, 36, 77, 99, 87, -31, 91, 55, -87, 68,
                -7, -32, -123, -71, -73, 40, -101, 0, -39, 60, 68, 68, 84, 87
                , 0, 12, -82, -99, 66, -36, 42, 10, 72, 10, 19, 69, -64, 34,
                -20, -72, 103, 39, 66, -83, -80, 2, -119, 32, -90, -119, -77,
                16, 47, -126, -5, -84, 69, -100, 103, -13, 118, 33, 6, -128,
                28, -93, -49, -68, -31, 70, -12, -59, 88, -100, 87, 0, 33, 4,
                -109, -2, -8, 87, 0, 33, 68, -113, 65, -29, -107, 110, -107,
                106, 86, -69, -29, -113, -8, -60, 111, 34, -28, 10, 9, 52,
                -82, 52, -94, -3, -23, 11, -51, -33, 27, 6, -60, 92, 60, 22,
                -126, -75, 1, -6, -10, 43, 16, 13, 49, 76, -48, -38, 53, -38,
                91, 68, 33, 8, -63, 68, 68, -41, 99, 52, -56, -62, -114, 6,
                -43, 60, 93, -66, 97, -102, 68, 32, 0, -124, -104, 21, -31,
                80, -121, -12, 8, 11, -96, 38, -10, 2, 44, 72, -19, 47, 76, 0
                , 106, 5, 47, 64, -125, -44, 40, -96, 47, 82, 18, 0, 4, -87,
                65, 0, 42, 86, 8, 122, -4, -112, 108, -37, -42, 86, -29, -111
                , 45, -65, 21, -23, 45, 69, 26, -15, 126, -108, -83, 101, 36,
                126, -46, 16, -126, -63, 113, -125, 8, -128, 122, -65, 22, 5,
                36, 57, 26, -41, -13, 75, -10, 19, -42, 27, -6, 88, -68, 0,
                69, 86, 89, 30, -39, -114, 40, 78, 0, 0, 0, 0, 73, 69, 78, 68
                , -82, 66, 96, -126
        };
        private static final byte[]     GREEN_0       = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8,
                6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0,
                -11, 73, 68, 65, 84, 88, -123, -59, -41, 81, 14, -125, 32, 12
                , 6, -32, -42, -20, 76, -37, -85, 87, -40, 78, -60, -119, -12
                , 10, 123, -43, 67, -55, 30, 22, 26, 10, -44, -96, -108, -14,
                63, 17, -125, -12, 19, 77, -92, 8, 60, 30, -20, -126, -39,
                -64, 24, -64, -22, 7, 4, 1, -90, -49, -85, 125, -27, -9, 51,
                -69, -26, -41, -99, -58, -57, -78, -79, -23, 104, 13, 40, 65,
                8, -111, 2, 18, -83, 74, -92, 26, -113, -46, -28, 99, -39,
                -40, 13, -91, -89, -85, 73, -70, 3, -23, -70, -124, -77, 2, 0
                , -4, 119, -94, -76, -61, 25, 34, -114, 38, -32, 44, 34, -62,
                10, 32, 34, 44, 1, 34, -62, 18, -96, -122, 104, 1, -88, 32,
                90, 1, -51, 8, 13, 64, 19, 66, 11, 112, 27, -95, 9, -72, -123
                , -48, 6, 92, 70, -12, 0, 92, 66, -12, 2, 84, 35, 122, 2, -86
                , 16, -67, 1, 25, 34, -3, 103, 88, 0, 24, 98, 20, -128, 33,
                70, 1, 68, -124, 117, 24, -62, -81, -69, 120, 4, -45, -120,
                116, -58, -92, -125, 110, -4, 10, 122, 65, -92, -106, 66, -67
                , -17, -88, 77, -38, 119, -128, 53, -92, -44, -127, -123, 12,
                -21, 69, 39, 55, 83, 113, 60, -103, -36, 13, -32, 102, -16,
                -31, -61, -12, 0, -128, 110, -74, 19, -72, 47, -43, 5, -116,
                118, 98, 88, 126, -9, -70, 118, -106, 72, -78, -62, -113, 0,
                0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        };
        private static final byte[]     GREEN_1       = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8,
                6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1,
                15, 73, 68, 65, 84, 88, -123, -59, -41, -63, 17, -125, 32, 16
                , 5, -48, 69, 83, 83, 114, -91, -123, -28, 110, 9, -10, 64,
                15, -106, -32, 93, 91, -56, 85, 123, 82, 114, 72, -40, 17, 4,
                -126, -78, -84, -1, -60, 40, 51, -5, 100, 24, 103, 87, -128,
                29, 13, 124, 17, -69, 5, 51, -64, -86, 111, 16, 8, -88, 94,
                -113, 50, -43, -98, 119, 92, 47, 77, 103, -67, 18, -36, 0, 31
                , 4, 17, 46, 96, 29, 38, 114, 76, -35, -73, -72, -42, -29,
                -116, 53, 110, -66, -51, -21, 48, 37, -99, -118, -17, 11, 99,
                89, -102, 14, -22, -66, 5, 61, -50, -42, -13, -118, 11, 0,
                -16, 61, 9, -25, 62, -8, 17, 41, 57, 3, -120, -27, 48, -126,
                26, 112, 24, -111, 11, 112, -17, -62, 97, 68, 41, 64, 50, -94
                , 36, 32, 9, 81, 26, -16, 23, -63, 1, -120, 34, -72, 0, 65, 4
                , 39, 32, -120, -32, 4, -112, 35, -50, 0, 72, 17, 103, 1, 100
                , -120, 28, 0, 9, 34, 23, -112, -115, -96, 0, 100, 33, -88, 0
                , 59, 68, -22, -1, -127, 18, 96, 33, -82, 2, 88, 8, -109, 80,
                11, 70, 1, 8, -75, -114, -34, 70, 55, 6, -55, 73, -88, 119,
                -75, -26, 14, -45, -110, -105, 56, 114, 55, -37, -111, -126,
                109, 2, 11, 1, -32, 55, -127, -103, 92, 54, -117, 86, 74, 98,
                113, 17, -39, 92, 12, -96, 36, 104, 115, 49, 53, 0, 8, 37, -7
                , 4, -22, -115, 117, 65, 108, 78, -30, -78, 124, 0, -37, 81,
                114, 94, 49, 105, 108, 112, 0, 0, 0, 0, 73, 69, 78, 68, -82,
                66, 96, -126
        };
        private static final byte[]     GREEN_2       = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8,
                6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0,
                -4, 73, 68, 65, 84, 88, -123, -59, -41, 65, 18, -125, 32, 12,
                5, -48, 31, -89, 103, 106, -73, 94, -63, -98, -120, 19, -47,
                43, 116, 107, 15, 5, 93, -88, -116, 82, 64, -127, 36, -3, 43,
                -57, 97, -56, 51, 58, 78, 32, 28, -29, -95, 23, -6, -71, 80,
                6, 28, -22, 111, -120, 0, 24, -98, 15, -15, -54, -50, -50, 7,
                8, 105, 3, 82, -112, -128, -120, 1, -111, -106, 37, -71, 26,
                -73, -44, 98, 103, 103, -111, -82, -20, -9, -91, -23, 14, -84
                , -120, 65, 11, 0, 44, -99, 112, 118, 94, 0, -5, -5, 34, -43,
                42, -93, -118, -120, 59, -96, -114, -56, 1, -44, 16, 41, -128
                , 127, 125, -12, 16, 103, 0, 113, 68, -23, 21, -88, 32, -82,
                2, -60, 16, 53, 0, 17, 68, 45, -128, 29, -47, 2, 96, 69, -76,
                2, -40, 16, 61, 0, 22, 68, 47, -96, 27, -63, 1, -24, 66, 112,
                1, -102, 17, -83, -128, -8, 119, -35, -116, -32, 6, 84, 35,
                36, 0, 73, -60, 54, -126, 73, 0, 114, -93, 99, 114, -48, 77,
                66, 24, -90, -17, -36, -20, -102, 61, 119, -108, -98, -4, -84
                , -67, 87, 18, -97, 59, 16, 67, 52, 1, 88, 79, 96, 97, -1,
                -18, -35, -21, 67, 0, 48, -104, 49, 20, -89, -62, 98, 49,
                -128, 25, -31, -73, 15, -45, 3, 32, 51, -22, 9, -52, 59, -44,
                5, -19, 58, -15, -73, 124, 1, -120, 79, 91, 100, 51, -68, 114
                , 65, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        };
        private static final byte[]     GRAY_0        = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8,
                6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1,
                -91, 73, 68, 65, 84, 88, -123, -19, 87, 81, -110, -61, 32, 8,
                125, -20, -12, 94, -15, 104, 28, -115, -100, -52, -3, 104,
                -80, -120, -104, -112, 100, -37, -23, -57, -66, 25, -89, 70,
                17, 30, 8, 106, -127, 127, 124, 17, -24, -124, 108, 125, -105
                , -2, -97, 3, -93, -74, -95, -42, -102, 106, 34, 50, -43, 113
                , -122, 105, -73, -96, -42, -13, 65, 88, -41, 21, 0, 80, 74,
                -55, -38, 28, 8, 84, 5, -116, 39, 89, -120, 72, -73, 78, 68,
                -4, -40, 46, -85, 22, 118, -21, 81, -32, 77, 24, 29, -94, -47
                , 73, 102, 6, -16, -118, -120, -47, -43, -124, 109, 78, 28,
                18, -48, 61, 87, -125, -66, -87, 81, -109, 19, -115, -124,
                -114, -71, 124, -23, -40, -92, 8, -40, 57, 69, 41, -91, 25,
                114, -98, 118, 125, 102, -98, -51, 17, 33, 81, 122, 81, -24,
                109, -30, -7, -112, 91, 68, 99, 30, 15, 60, -93, -47, 34, 17,
                -19, 107, 52, 102, 67, -82, -58, 50, 6, -127, 103, 84, 76,
                -12, 58, -27, 67, 5, -8, 44, -65, -45, -104, 121, 90, 37, 54,
                49, 41, -14, -38, 122, 108, 19, 115, 6, 17, 105, -51, 122,
                -82, -120, -86, -29, -31, 116, 16, -128, 74, 68, -45, 18, 84,
                34, 71, 57, -95, 125, 59, 23, 17, -120, 72, -124, 68, 68, 36,
                -67, -33, 22, 25, 2, -64, -4, -18, 24, -74, 70, -61, -85, 99,
                -53, -78, 116, 11, 102, -91, 121, 68, 96, -113, 68, 71, 68,
                13, 122, 34, 17, -20, 54, 100, 8, 0, -15, 118, 120, 34, 97,
                -114, 16, 81, 88, -90, 103, 9, -100, 65, 120, -87, 105, -97,
                -103, -121, 82, -124, 43, -61, -65, -62, -12, 118, -3, 20,
                -127, 67, 34, -97, 34, 48, 37, 114, -105, -64, 94, 117, -52,
                16, -98, -84, 119, -110, -16, 78, -42, 70, 30, 95, -46, 23,
                45, -86, 59, 115, 123, 68, -50, -56, -17, -54, 86, -9, -101,
                49, 62, -21, -89, -11, 15, -49, -69, 13, 87, -68, -54, 36,
                -92, -43, 59, 92, -27, -107, 11, -64, 37, -83, -52, -53, -42,
                -19, 59, -125, -63, 86, -117, 4, -53, -96, -36, -106, 91, -8,
                39, 70, 101, 29, -127, 72, -10, 117, -98, -108, -34, 30, 16,
                60, -7, -67, 71, 44, -51, -40, 37, 68, -21, 55, 2, -31, -109,
                31, 0, -56, 50, 124, 7, 60, 1, -3, 120, -5, 17, 123, -124, 95
                , 102, -54, -57, -21, -58, -110, 94, 109, 0, 0, 0, 0, 73, 69,
                78, 68, -82, 66, 96, -126
        };
        private static final byte[]     GRAY_1        = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8,
                6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1,
                -66, 73, 68, 65, 84, 88, -123, -19, -105, 81, -110, -61, 32,
                8, -122, 127, 59, -67, 87, 114, -77, 112, 52, 114, 50, -10,
                -95, -59, 5, 2, -58, 100, -89, 59, -5, -80, -1, -116, -109,
                104, 20, 62, 81, -87, 5, -2, -11, -121, -44, 38, -5, -55, 7,
                109, -105, 29, 15, 78, -119, 104, 104, 104, 93, 87, 0, -64,
                -78, 44, 104, 45, 53, 91, 66, 101, 31, 28, -64, -103, -13, 10
                , 4, 64, 6, 51, 69, 39, 0, -64, -52, -50, -24, 12, 76, 21, 9,
                17, -119, 64, 67, 50, 7, -96, -17, -103, -13, -39, 54, -75,
                -107, 68, 38, -115, -120, 0, 16, 102, -18, -123, -120, 68,
                -37, -19, 55, -37, 22, 11, 17, -119, -68, -90, 126, 24, -89,
                50, -33, -70, -98, 25, -47, 72, -52, -20, -94, 69, 68, -82,
                14, -8, 37, -48, 101, 98, 102, 44, -53, 98, -65, 41, 72, 107,
                -111, -86, 114, 92, -75, 89, 8, 13, -69, 85, 113, 82, -100,
                -98, 120, -83, -113, -24, 12, -78, 65, 118, -125, -86, -78,
                61, 48, -29, 80, -57, -102, -15, -19, -42, -58, -68, 35, 93,
                -114, -105, 87, -65, 65, 31, -90, 95, 3, -114, -77, -74, 16,
                113, 63, 100, 18, -111, 94, -20, -72, -18, 36, 57, 33, -61,
                -116, 105, -99, 70, 24, -5, -84, -10, -123, 58, -76, 16, 102,
                -110, -35, -73, -115, -124, 85, -113, -118, -106, -69, -53,
                114, 6, 48, -126, 112, 32, 42, 5, -47, -74, -72, 116, 89, 20,
                -76, -67, 2, 56, -125, 112, 32, -37, -74, -91, 32, -23, -96,
                55, 0, 17, -59, 52, -98, 46, -1, 25, 68, 31, -88, -119, -57,
                58, -49, 64, 44, -64, -74, 109, -89, 0, 87, -107, -90, 111,
                77, -59, -74, 45, 73, -33, 67, 93, 73, -37, -70, 52, 46, -97,
                -24, 76, 99, 84, -10, 125, -65, 96, -6, -98, -54, 31, 53, 34,
                -118, 109, -65, 11, 114, 23, 96, 102, 99, 86, 74, 51, -20,
                -24, 40, 14, 13, -3, 80, -39, -116, 47, -39, 29, -35, 49, 103
                , 12, 93, 5, -104, -78, 45, -31, 57, -29, -68, 122, -97, -74,
                -1, 72, 58, 0, -41, -94, -48, 66, 125, -92, -61, -43, -63, 66
                , 8, -83, 0, -83, -45, -58, 98, 95, 121, -41, 103, 116, -16,
                -43, 35, 65, 124, 48, 110, -113, 89, -68, -44, -70, -66, 1,
                32, -21, -5, -99, 71, 86, -17, 15, 72, -2, 119, -60, 25, 17,
                119, 103, -73, -108, -115, 127, 3, -108, -9, -119, 102, 9, 63
                , -95, 8, -96, -107, -113, -89, -42, 51, 125, 1, -12, 83, 73,
                96, 76, -110, 92, -110, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66,
                96, -126
        };
        private static final byte[]     VIOLET_0      = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8,
                6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0,
                -21, 73, 68, 65, 84, 72, -119, -19, -106, 73, 14, -61, 48, 8,
                69, -79, -43, -125, -11, 102, 118, 110, -106, -101, -47, 77,
                -79, 108, -14, 25, 28, 85, 93, 5, 41, 82, 68, -32, 63, -16,
                -104, 66, 121, 99, -32, 43, -103, -60, 84, -112, 0, 58, -11,
                -31, -104, -34, 67, -115, 12, -28, 2, -40, 5, -67, -18, 0, 26
                , 53, 13, 98, 15, 84, 3, -120, 11, 104, -44, 96, -121, 59,
                -112, -53, 68, -49, 0, -32, 67, 11, 35, -124, -92, -86, -52,
                -60, 89, 16, -77, -86, -64, 96, -98, -39, 73, -74, -117, 76,
                124, 56, -15, -65, -80, 7, -14, 64, 86, -5, -57, 18, -122,
                -121, -35, 65, 71, -28, -125, 121, -107, -100, -35, -115, -86
                , -101, 69, -27, 61, -24, -102, -27, -88, 71, 71, 117, -79,
                10, 64, 29, -127, 124, -47, 93, -122, 11, 10, 70, 115, -29,
                124, 31, 122, -107, -120, -88, -65, 77, 80, -15, -124, -100,
                -101, -111, 103, -35, -47, -55, 46, 40, 11, -112, 0, -98, 29,
                -3, 92, -59, 117, 50, 42, -64, 3, -12, 19, 44, -31, -96, 35,
                -3, -72, 0, 49, -72, 79, 20, 104, 126, -76, -24, -14, 13, 1,
                -120, -100, -65, 21, -99, -16, 29, 70, -10, 98, 44, 27, 123,
                33, -101, -80, 99, 50, -65, 50, -82, 119, -17, -12, -116,
                -107, 15, -45, 30, 82, -22, 101, -5, 59, 120, 0, 0, 0, 0, 73,
                69, 78, 68, -82, 66, 96, -126
        };
        private static final byte[]     VIOLET_1      = {
                -119, 80, 78, 71, 13, 10, 26,
                10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8,
                6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8
                , 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0
                , 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0,
                -23, 73, 68, 65, 84, 72, -119, -19, -106, 75, 14, -61, 32, 12
                , 68, -121, -88, 7, -21, -51, -22, -93, -27, 102, -18, -94,
                -72, 34, -32, 111, -23, -90, 82, 29, 69, 34, 48, -10, -77, 33
                , -124, 52, -28, -115, -107, -66, -106, 113, 76, -119, 4, 64,
                -96, 119, -57, -48, 14, 99, 100, 32, 11, -96, 10, -118, 32,
                38, -96, 2, 58, 2, -120, 11, -56, -116, 71, 16, 109, -95, 61,
                51, -11, 110, 37, -103, 44, 51, 58, 11, 98, 102, -11, -24, 87
                , -59, -49, -84, 68, -53, 110, 12, 62, -125, -68, 106, -62,
                -123, -73, -126, 90, 125, 91, -112, 29, -5, 67, 126, 8, -110,
                -35, -120, 25, -67, 5, -55, 30, 1, 41, -65, 3, -50, -18, -2,
                -46, 103, -123, -91, -110, -113, 79, -67, 64, -49, 99, -125,
                -95, -125, 24, 0, 19, -120, 9, -60, 12, -66, -36, 4, 10, 125,
                101, -116, -23, 94, 7, 101, 0, 18, -73, -11, 7, 0, 0, -99,
                102, -23, -107, 51, -2, -91, 29, 98, 94, 32, 25, -48, 100, 46
                , 64, -30, 45, -81, -16, 32, -104, -125, 54, -27, 118, 1, 98,
                -22, 62, -103, 64, -42, -36, 47, 99, 26, 0, 0, 110, 122, -9,
                -22, -48, -89, -111, 61, -115, 101, 45, -54, 98, -57, 100,
                125, 101, 94, -85, 127, 38, 21, 107, 79, 77, -7, 124, 40, -83
                , 34, 110, -15, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
        };
        private static final Image[]    DRAWINGS      = {
                Toolkit.getDefaultToolkit().createImage(Element.BLUE_0),
                Toolkit.getDefaultToolkit().createImage(Element.BLUE_1),
                Toolkit.getDefaultToolkit().createImage(Element.BLUE_2),
                Toolkit.getDefaultToolkit().createImage(Element.RED_0),
                Toolkit.getDefaultToolkit().createImage(Element.RED_1),
                Toolkit.getDefaultToolkit().createImage(Element.GREEN_0),
                Toolkit.getDefaultToolkit().createImage(Element.GREEN_1),
                Toolkit.getDefaultToolkit().createImage(Element.GREEN_2),
                Toolkit.getDefaultToolkit().createImage(Element.GRAY_0),
                Toolkit.getDefaultToolkit().createImage(Element.GRAY_1),
                Toolkit.getDefaultToolkit().createImage(Element.VIOLET_0),
                Toolkit.getDefaultToolkit().createImage(Element.VIOLET_1)
        };

        protected int type  = 0;
        protected int value = 0;
        boolean isSelected = false;
        int     shotX;
        int     shotY;
        private int     drawing      = 0;
        private boolean shouldInvert = false;
        private int     direction    = 1;

        Element (final int x, final int y, final int type) {
            super(x, y,
                  (Integer) Element.VALUES[type][0],
                  (Integer) Element.VALUES[type][1]
            );
            this.setType(type);
            this.shotX      = x + this.width / 2;
            this.shotY      = y + this.height / 2;
            this.isSelected = false;
            this.drawing    = Element.newDrawingFromType(type);
        }

        private void setType (final int type) {
            this.type   = type;
            this.width  = (Integer) Element.VALUES[type][0];
            this.height = (Integer) Element.VALUES[type][1];
            this.shotX  = 1;
            this.shotY  = 0;
            this.value  = this.getVida();
        }

        private static int newDrawingFromType (final int type) {
            return switch (type) {
                case Element.RED_CANNON -> 3;
                case Element.GREEN_CANNON -> 5;
                case Element.GRAY_CANNON -> 8;
                case Element.VIOLET_CANNON -> 10;
                default -> 0;
            };
        }

        private int getVida () {
            return (Integer) Element.VALUES[this.type][3];
        }

        private Element (
                final int x, final int y,
                final int w, final int h,
                final int shotX, final int shotY
        ) {
            super(x, y, w, h);
            this.shotX = shotX;
            this.shotY = shotY;
        }

        private void drawCursor (
                final int locX, final int locY,
                final Graphics g
        ) {
            if (locX < Stalemate.SIZE && locY < Stalemate.SIZE) {
                g.setColor(this.getColor());
                g.drawRect(
                        locX - this.getRange(),
                        locY - this.getRange(),
                        this.width + this.getRange() * 2,
                        this.height + this.getRange() * 2
                );
                g.drawImage(this.getImage(), locX, locY, null);
            }
        }

        private Color getColor () {
            return (Color) Element.VALUES[this.type][2];
        }

        private int getRange () {
            return (Integer) Element.VALUES[this.type][4];
        }

        private Image getImage () {
            return Element.DRAWINGS[this.drawing];
        }

        private void draw (final Graphics g) {
            if (this.x >= Stalemate.SIZE || this.y >= Stalemate.SIZE) {
                return;
            }

            if (this.type < Element.OPTIONS) {
                g.setColor(this.getColor());
                g.drawLine(
                        Stalemate.this.getShotX(this),
                        Stalemate.this.getShotY(this),
                        this.shotX, this.shotY
                );
            }

            if (this.type >= Element.OPTIONS) {
                g.setColor(Color.BLACK);
                g.drawRect(this.x, this.y, this.width, this.height);
                g.setColor(this.getColor());
                g.fillRect(this.x, this.y, this.width, this.height);
            } else {
                g.setColor(this.getColor());
                if (this.shouldInvert) {
                    g.drawImage(
                            this.getImage(),
                            this.x, this.y,
                            this.width + this.x, this.height + this.y,
                            this.width, 0, 0, this.height,
                            null
                    );
                } else {
                    g.drawImage(this.getImage(), this.x, this.y, null);
                }
            }

            if (!this.isSelected) {
                return;
            }

            if (this.type >= Element.OPTIONS) {
                g.setColor(Color.BLUE);
            }

            g.drawRect(
                    this.x - 2, this.y - 2,
                    this.width + 4, this.height + 4
            );
        }

        private boolean isTargetInRange (final Rectangle target) {
            return this.isTargetInCardinalRange(this.x, target.x, this.width)
                   && this.isTargetInCardinalRange(this.y, target.y,
                                                   this.height
            );
        }

        private boolean isTargetInCardinalRange (
                final int z, final int targetZ, final int size
        ) {
            final int range = targetZ + size / 2;
            return (z - this.getRange()) < range && (z + size + this.getRange() * 2) > range;
        }

        private int getValor () {
            return (Integer) Element.VALUES[this.type][3];
        }

        private int getPower () {
            return (Integer) Element.VALUES[this.type][5];
        }

        private int getSpeed () {
            return (Integer) Element.VALUES[this.type][4];
        }

        private void change (final Iterable<? extends Element> enemies) {
            for (final var target : enemies) {
                if (this.contains(target)) {
                    target.shotX = this.shotX;
                    target.shotY = this.shotY;
                }
            }
        }

        private void stop () {
            this.isSelected = false;
            this.value      = 1;
            this.shotX      = 0;
            this.shotY      = 0;
            this.x          = Stalemate.ENEMY_DEFAULT_POSITION;
            this.y          = Stalemate.ENEMY_DEFAULT_POSITION;
        }

        private void updateDrawing () {
            switch (this.type) {
                case Element.ICE_CANNON -> this.updateGenericCannon(3, 0);
                case Element.RED_CANNON -> this.updateRedCannon();
                case Element.GREEN_CANNON -> this.updateGreenCannon();
                case Element.GRAY_CANNON -> this.updateGenericCannon(10, 8);
                case Element.VIOLET_CANNON -> this.updateGenericCannon(12, 10);
            }
        }

        private void updateGenericCannon (
                final int targetX,
                final int newDrawing
        ) {
            if (this.drawing + 1 == targetX) {
                this.drawing      = newDrawing;
                this.shouldInvert = !this.shouldInvert;
            } else {
                this.drawing++;
            }
        }

        private void updateRedCannon () {
            if (this.drawing + 1 == 5) {
                this.drawing = 3;
            } else {
                this.drawing++;
                this.shouldInvert = !this.shouldInvert;
            }
        }

        private void updateGreenCannon () {
            if (this.direction == -1 && this.drawing - 1 == 4) {
                this.direction = 1;
            }
            if (this.direction == 1 && this.drawing + 1 == 8) {
                this.direction = -1;
            } else {
                this.drawing += this.direction;
            }
        }
    }
}