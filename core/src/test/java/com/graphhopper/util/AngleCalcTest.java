/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import org.junit.jupiter.api.Test;

import static com.graphhopper.util.GHUtility.getCommonNode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Johannes Pelzer
 * @author Peter Karich
 */
public class AngleCalcTest {
    private final AngleCalc AC = AngleCalc.ANGLE_CALC;

    @Test
    public void testOrientationExact() {
        assertEquals(90.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 0)), 0.01);
        assertEquals(45.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 1)), 0.01);
        assertEquals(0.0, Math.toDegrees(AC.calcOrientation(0, 0, 0, 1)), 0.01);
        assertEquals(-45.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, 1)), 0.01);
        assertEquals(-135.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, -1)), 0.01);

        // is symmetric?
        assertEquals(90 - 32.76, Math.toDegrees(AC.calcOrientation(49.942, 11.580, 49.944, 11.582)), 0.01);
        assertEquals(-90 - 32.76, Math.toDegrees(AC.calcOrientation(49.944, 11.582, 49.942, 11.580)), 0.01);
    }

    @Test
    public void testOrientationFast() {
        assertEquals(90.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 0, false)), 0.01);
        assertEquals(45.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 1, false)), 0.01);
        assertEquals(0.0, Math.toDegrees(AC.calcOrientation(0, 0, 0, 1, false)), 0.01);
        assertEquals(-45.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, 1, false)), 0.01);
        assertEquals(-135.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, -1, false)), 0.01);

        // is symmetric?
        assertEquals(90 - 32.92, Math.toDegrees(AC.calcOrientation(49.942, 11.580, 49.944, 11.582, false)), 0.01);
        assertEquals(-90 - 32.92, Math.toDegrees(AC.calcOrientation(49.944, 11.582, 49.942, 11.580, false)), 0.01);
    }

    @Test
    public void testAlignOrientation() {
        assertEquals(90.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(90), Math.toRadians(90))), 0.001);
        assertEquals(225.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(90), Math.toRadians(-135))), 0.001);
        assertEquals(-45.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(-135), Math.toRadians(-45))), 0.001);
        assertEquals(-270.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(-135), Math.toRadians(90))), 0.001);
    }

    @Test
    public void testCombined() {
        double orientation = AC.calcOrientation(52.414918, 13.244221, 52.415333, 13.243595);
        assertEquals(132.7, Math.toDegrees(AC.alignOrientation(0, orientation)), 1);

        orientation = AC.calcOrientation(52.414918, 13.244221, 52.414573, 13.243627);
        assertEquals(-136.38, Math.toDegrees(AC.alignOrientation(0, orientation)), 1);
    }

    @Test
    public void testCalcAzimuth() {
        assertEquals(45.0, AC.calcAzimuth(0, 0, 1, 1), 0.001);
        assertEquals(90.0, AC.calcAzimuth(0, 0, 0, 1), 0.001);
        assertEquals(180.0, AC.calcAzimuth(0, 0, -1, 0), 0.001);
        assertEquals(270.0, AC.calcAzimuth(0, 0, 0, -1), 0.001);
        assertEquals(0.0, AC.calcAzimuth(49.942, 11.580, 49.944, 11.580), 0.001);
    }

    @Test
    // ajout de test unitaire pour tester toutes les conditions
    public void testAzimuthCompassPoint() {
        assertEquals("N", AC.azimuth2compassPoint(10));
        assertEquals("NE", AC.azimuth2compassPoint(25));
        assertEquals("E", AC.azimuth2compassPoint(70));
        assertEquals("SE", AC.azimuth2compassPoint(150));
        assertEquals("S", AC.azimuth2compassPoint(160));
        assertEquals("SW", AC.azimuth2compassPoint(205));
        assertEquals("W", AC.azimuth2compassPoint(250));
        assertEquals("NW", AC.azimuth2compassPoint(300));
        assertEquals("N", AC.azimuth2compassPoint(350));
    }

    @Test
    public void testAtan2() {
        // assertEquals(0, AngleCalc.atan2(0, 0), 1e-4);
        // assertEquals(0, AngleCalc.atan2(-0.002, 0), 1e-4);
        assertEquals(45, AngleCalc.atan2(5, 5) * 180 / Math.PI, 1e-2);
        assertEquals(-45, AngleCalc.atan2(-5, 5) * 180 / Math.PI, 1e-2);
        assertEquals(11.14, AngleCalc.atan2(1, 5) * 180 / Math.PI, 1);
        assertEquals(180, AngleCalc.atan2(0, -5) * 180 / Math.PI, 1e-2);
        assertEquals(-90, AngleCalc.atan2(-5, 0) * 180 / Math.PI, 1e-2);

        assertEquals(90, Math.atan2(1, 0) * 180 / Math.PI, 1e-2);
        assertEquals(90, AngleCalc.atan2(1, 0) * 180 / Math.PI, 1e-2);
    }

    @Test
    //ajout 2 test pour couvrir toutes les branches de test if et 1 exception handler
    public void testConvertAzimuth2xAxisAngle() {
        assertEquals(Math.PI / 2, AC.convertAzimuth2xaxisAngle(0), 1E-6);
        assertEquals(Math.PI / 2, Math.abs(AC.convertAzimuth2xaxisAngle(360)), 1E-6);
        assertEquals(0, AC.convertAzimuth2xaxisAngle(90), 1E-6);
        assertEquals(-Math.PI / 2, AC.convertAzimuth2xaxisAngle(180), 1E-6);
        assertEquals(Math.PI, Math.abs(AC.convertAzimuth2xaxisAngle(270)), 1E-6);
        assertEquals(-3 * Math.PI / 4, AC.convertAzimuth2xaxisAngle(225), 1E-6);
        assertEquals(3 * Math.PI / 4, AC.convertAzimuth2xaxisAngle(315), 1E-6);
        assertEquals(Math.PI / 4, AC.convertAzimuth2xaxisAngle(45), 1E-6);
        try {
            AC.convertAzimuth2xaxisAngle(400);
            fail("expected exception was not occured.");
        } catch(IllegalArgumentException e) {
            System.out.println("Excepted exception is handled");
        }
    }

    @Test
    public void checkAzimuthConsistency() {
        double azimuthDegree = AC.calcAzimuth(0, 0, 1, 1);
        double radianXY = AC.calcOrientation(0, 0, 1, 1);
        double radian2 = AC.convertAzimuth2xaxisAngle(azimuthDegree);
        assertEquals(radianXY, radian2, 1E-3);

        azimuthDegree = AC.calcAzimuth(0, 4, 1, 3);
        radianXY = AC.calcOrientation(0, 4, 1, 3);
        radian2 = AC.convertAzimuth2xaxisAngle(azimuthDegree);
        assertEquals(radianXY, radian2, 1E-3);
    }

    @Test
    public void testIsClockwise() {
        Coordinate a = new Coordinate(0.1, 1);
        Coordinate b = new Coordinate(0.2, 0.8);
        Coordinate c = new Coordinate(0.6, 0.3);
        assertTrue(isClockwise(a, b, c));
        assertTrue(isClockwise(b, c, a));
        assertTrue(isClockwise(c, a, b));
        assertFalse(isClockwise(c, b, a));
        assertFalse(isClockwise(a, c, b));
        assertFalse(isClockwise(b, a, c));
    }

    private boolean isClockwise(Coordinate a, Coordinate b, Coordinate c) {
        return AC.isClockwise(a.x, a.y, b.x, b.y, c.x, c.y);
    }

    private static class Coordinate {
        final double x;
        final double y;

        Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
