/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2013-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.serializable.SerializableTestUtility;
import android.icu.impl.Pair;
import android.icu.impl.Utility;
import android.icu.math.BigDecimal;
import android.icu.text.MeasureFormat;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.text.NumberFormat;
import android.icu.util.Currency;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.icu.util.TimeUnit;
import android.icu.util.TimeUnitAmount;
import android.icu.util.ULocale;

/**
 * See https://sites.google.com/site/icusite/processes/release/tasks/standards?pli=1
 * for information on how to update with each new release.
 * @author markdavis
 */
public class MeasureUnitTest extends TestFmwk {

    static class OrderedPair<F extends Comparable, S extends Comparable> extends Pair<F, S> implements Comparable<OrderedPair<F, S>> {

        OrderedPair(F first, S second) {
            super(first, second);
        }

        public static <F extends Comparable, S extends Comparable> OrderedPair<F, S> of(F first, S second) {
            if (first == null || second == null) {
                throw new IllegalArgumentException("OrderedPair.of requires non null values.");
            }
            return new OrderedPair<F, S>(first, second);
        }

        @Override
        public int compareTo(OrderedPair<F, S> other) {
            int result = first.compareTo(other.first);
            if (result != 0) {
                return result;
            }
            return second.compareTo(other.second);
        }
    }

    private static final String[] DRAFT_VERSIONS = {"57", "58"};

    private static final HashSet<String> DRAFT_VERSION_SET = new HashSet<String>();

    private static final HashSet<String> TIME_CODES = new HashSet<String>();

    private static final String[][] JAVA_VERSIONS = {
        {"G_FORCE", "53"},
        {"DEGREE", "53"},
        {"ARC_MINUTE", "53"},
        {"ARC_SECOND", "53"},
        {"ACRE", "53"},
        {"HECTARE", "53"},
        {"SQUARE_FOOT", "53"},
        {"SQUARE_KILOMETER", "53"},
        {"SQUARE_METER", "53"},
        {"SQUARE_MILE", "53"},
        {"MILLISECOND", "53"},
        {"CENTIMETER", "53"},
        {"FOOT", "53"},
        {"INCH", "53"},
        {"KILOMETER", "53"},
        {"LIGHT_YEAR", "53"},
        {"METER", "53"},
        {"MILE", "53"},
        {"MILLIMETER", "53"},
        {"PICOMETER", "53"},
        {"YARD", "53"},
        {"GRAM", "53"},
        {"KILOGRAM", "53"},
        {"OUNCE", "53"},
        {"POUND", "53"},
        {"HORSEPOWER", "53"},
        {"KILOWATT", "53"},
        {"WATT", "53"},
        {"HECTOPASCAL", "53"},
        {"INCH_HG", "53"},
        {"MILLIBAR", "53"},
        {"KILOMETER_PER_HOUR", "53"},
        {"METER_PER_SECOND", "53"},
        {"MILE_PER_HOUR", "53"},
        {"CELSIUS", "53"},
        {"FAHRENHEIT", "53"},
        {"CUBIC_KILOMETER", "53"},
        {"CUBIC_MILE", "53"},
        {"LITER", "53"},
        {"YEAR", "53"},
        {"MONTH", "53"},
        {"WEEK", "53"},
        {"DAY", "53"},
        {"HOUR", "53"},
        {"MINUTE", "53"},
        {"SECOND", "53"},
        {"METER_PER_SECOND_SQUARED", "54"},
        {"RADIAN", "54"},
        {"SQUARE_CENTIMETER", "54"},
        {"SQUARE_INCH", "54"},
        {"SQUARE_YARD", "54"},
        {"LITER_PER_KILOMETER", "54"},
        {"MILE_PER_GALLON", "54"},
        {"BIT", "54"},
        {"BYTE", "54"},
        {"GIGABIT", "54"},
        {"GIGABYTE", "54"},
        {"KILOBIT", "54"},
        {"KILOBYTE", "54"},
        {"MEGABIT", "54"},
        {"MEGABYTE", "54"},
        {"TERABIT", "54"},
        {"TERABYTE", "54"},
        {"MICROSECOND", "54"},
        {"NANOSECOND", "54"},
        {"AMPERE", "54"},
        {"MILLIAMPERE", "54"},
        {"OHM", "54"},
        {"VOLT", "54"},
        {"CALORIE", "54"},
        {"FOODCALORIE", "54"},
        {"JOULE", "54"},
        {"KILOCALORIE", "54"},
        {"KILOJOULE", "54"},
        {"KILOWATT_HOUR", "54"},
        {"GIGAHERTZ", "54"},
        {"HERTZ", "54"},
        {"KILOHERTZ", "54"},
        {"MEGAHERTZ", "54"},
        {"ASTRONOMICAL_UNIT", "54"},
        {"DECIMETER", "54"},
        {"FATHOM", "54"},
        {"FURLONG", "54"},
        {"MICROMETER", "54"},
        {"NANOMETER", "54"},
        {"NAUTICAL_MILE", "54"},
        {"PARSEC", "54"},
        {"LUX", "54"},
        {"CARAT", "54"},
        {"METRIC_TON", "54"},
        {"MICROGRAM", "54"},
        {"MILLIGRAM", "54"},
        {"OUNCE_TROY", "54"},
        {"STONE", "54"},
        {"TON", "54"},
        {"GIGAWATT", "54"},
        {"MEGAWATT", "54"},
        {"MILLIWATT", "54"},
        {"MILLIMETER_OF_MERCURY", "54"},
        {"POUND_PER_SQUARE_INCH", "54"},
        {"KARAT", "54"},
        {"KELVIN", "54"},
        {"ACRE_FOOT", "54"},
        {"BUSHEL", "54"},
        {"CENTILITER", "54"},
        {"CUBIC_CENTIMETER", "54"},
        {"CUBIC_FOOT", "54"},
        {"CUBIC_INCH", "54"},
        {"CUBIC_METER", "54"},
        {"CUBIC_YARD", "54"},
        {"CUP", "54"},
        {"DECILITER", "54"},
        {"FLUID_OUNCE", "54"},
        {"GALLON", "54"},
        {"HECTOLITER", "54"},
        {"MEGALITER", "54"},
        {"MILLILITER", "54"},
        {"PINT", "54"},
        {"QUART", "54"},
        {"TABLESPOON", "54"},
        {"TEASPOON", "54"},
        {"GENERIC_TEMPERATURE", "56"},
        {"REVOLUTION_ANGLE", "56"},
        {"LITER_PER_100KILOMETERS", "56"},
        {"CENTURY", "56"},
        {"MILE_SCANDINAVIAN", "56"},
        {"KNOT", "56"},
        {"CUP_METRIC", "56"},
        {"PINT_METRIC", "56"},
        {"MILLIGRAM_PER_DECILITER", "57"},
        {"MILLIMOLE_PER_LITER", "57"},
        {"PART_PER_MILLION", "57"},
        {"MILE_PER_GALLON_IMPERIAL", "57"},
        {"GALLON_IMPERIAL", "57"},
        {"EAST", "58"},
        {"NORTH", "58"},
        {"SOUTH", "58"},
        {"WEST", "58"},
    };

    private static final HashMap<String, String> JAVA_VERSION_MAP = new HashMap<String, String>();

    static {
        TIME_CODES.add("year");
        TIME_CODES.add("month");
        TIME_CODES.add("week");
        TIME_CODES.add("day");
        TIME_CODES.add("hour");
        TIME_CODES.add("minute");
        TIME_CODES.add("second");
        for (String verNum : DRAFT_VERSIONS) {
            DRAFT_VERSION_SET.add(verNum);
        }
        for (String[] funcNameAndVersion : JAVA_VERSIONS) {
            JAVA_VERSION_MAP.put(funcNameAndVersion[0], funcNameAndVersion[1]);
        }
    }

    /**
     * @author markdavis
     *
     */
    // TODO(junit): resolve
//    public static void main(String[] args) {
//        //generateConstants(); if (true) return;
//
//        // Ticket #12034 deadlock on multi-threaded static init of MeasureUnit.
//        // The code below reliably deadlocks with ICU 56.
//        // The test is here in main() rather than in a test function so it can be made to run
//        // before anything else.
//        Thread thread = new Thread()  {
//            @Override
//            public void run() {
//                @SuppressWarnings("unused")
//                Set<String> measureUnitTypes = MeasureUnit.getAvailableTypes();
//            }
//        };
//        thread.start();
//        @SuppressWarnings("unused")
//        Currency cur = Currency.getInstance(ULocale.ENGLISH);
//        try {thread.join();} catch(InterruptedException e) {};
//        // System.out.println("Done with MeasureUnit thread test.");
//
//        new MeasureUnitTest().run(args);
//    }

/*
    @Test
    public void testZZZ() {
        // various generateXXX calls go here, see
        // http://site.icu-project.org/design/formatting/measureformat/updating-measure-unit
        // use this test to run each of the ollowing in succession
        //generateConstants("58"); // for MeasureUnit.java, update generated MeasureUnit constants
        //generateBackwardCompatibilityTest("58.1"); // for MeasureUnitTest.java, create TestCompatible58_1
        //generateCXXHConstants("58"); // for measunit.h, update generated createXXX methods
        //generateCXXConstants(); // for measunit.cpp, update generated code
        //generateCXXBackwardCompatibilityTest("58.1"); // for measfmttest.cpp, create TestCompatible58_1
        updateJAVAVersions("58"); // for MeasureUnitTest.java, JAVA_VERSIONS
    }
*/

    @Test
    public void TestCompatible53_1() {
        MeasureUnit[] units = {
                MeasureUnit.G_FORCE,
                MeasureUnit.DEGREE,
                MeasureUnit.ARC_MINUTE,
                MeasureUnit.ARC_SECOND,
                MeasureUnit.ACRE,
                MeasureUnit.HECTARE,
                MeasureUnit.SQUARE_FOOT,
                MeasureUnit.SQUARE_KILOMETER,
                MeasureUnit.SQUARE_METER,
                MeasureUnit.SQUARE_MILE,
                MeasureUnit.MILLISECOND,
                MeasureUnit.CENTIMETER,
                MeasureUnit.FOOT,
                MeasureUnit.INCH,
                MeasureUnit.KILOMETER,
                MeasureUnit.LIGHT_YEAR,
                MeasureUnit.METER,
                MeasureUnit.MILE,
                MeasureUnit.MILLIMETER,
                MeasureUnit.PICOMETER,
                MeasureUnit.YARD,
                MeasureUnit.GRAM,
                MeasureUnit.KILOGRAM,
                MeasureUnit.OUNCE,
                MeasureUnit.POUND,
                MeasureUnit.HORSEPOWER,
                MeasureUnit.KILOWATT,
                MeasureUnit.WATT,
                MeasureUnit.HECTOPASCAL,
                MeasureUnit.INCH_HG,
                MeasureUnit.MILLIBAR,
                MeasureUnit.KILOMETER_PER_HOUR,
                MeasureUnit.METER_PER_SECOND,
                MeasureUnit.MILE_PER_HOUR,
                MeasureUnit.CELSIUS,
                MeasureUnit.FAHRENHEIT,
                MeasureUnit.CUBIC_KILOMETER,
                MeasureUnit.CUBIC_MILE,
                MeasureUnit.LITER,
                MeasureUnit.YEAR,
                MeasureUnit.MONTH,
                MeasureUnit.WEEK,
                MeasureUnit.DAY,
                MeasureUnit.HOUR,
                MeasureUnit.MINUTE,
                MeasureUnit.SECOND,
        };
        assertEquals("", 46, units.length);
    }

    @Test
    public void TestCompatible54_1() {
        MeasureUnit[] units = {
                MeasureUnit.G_FORCE,
                MeasureUnit.METER_PER_SECOND_SQUARED,
                MeasureUnit.ARC_MINUTE,
                MeasureUnit.ARC_SECOND,
                MeasureUnit.DEGREE,
                MeasureUnit.RADIAN,
                MeasureUnit.ACRE,
                MeasureUnit.HECTARE,
                MeasureUnit.SQUARE_CENTIMETER,
                MeasureUnit.SQUARE_FOOT,
                MeasureUnit.SQUARE_INCH,
                MeasureUnit.SQUARE_KILOMETER,
                MeasureUnit.SQUARE_METER,
                MeasureUnit.SQUARE_MILE,
                MeasureUnit.SQUARE_YARD,
                MeasureUnit.LITER_PER_KILOMETER,
                MeasureUnit.MILE_PER_GALLON,
                MeasureUnit.BIT,
                MeasureUnit.BYTE,
                MeasureUnit.GIGABIT,
                MeasureUnit.GIGABYTE,
                MeasureUnit.KILOBIT,
                MeasureUnit.KILOBYTE,
                MeasureUnit.MEGABIT,
                MeasureUnit.MEGABYTE,
                MeasureUnit.TERABIT,
                MeasureUnit.TERABYTE,
                MeasureUnit.DAY,
                MeasureUnit.HOUR,
                MeasureUnit.MICROSECOND,
                MeasureUnit.MILLISECOND,
                MeasureUnit.MINUTE,
                MeasureUnit.MONTH,
                MeasureUnit.NANOSECOND,
                MeasureUnit.SECOND,
                MeasureUnit.WEEK,
                MeasureUnit.YEAR,
                MeasureUnit.AMPERE,
                MeasureUnit.MILLIAMPERE,
                MeasureUnit.OHM,
                MeasureUnit.VOLT,
                MeasureUnit.CALORIE,
                MeasureUnit.FOODCALORIE,
                MeasureUnit.JOULE,
                MeasureUnit.KILOCALORIE,
                MeasureUnit.KILOJOULE,
                MeasureUnit.KILOWATT_HOUR,
                MeasureUnit.GIGAHERTZ,
                MeasureUnit.HERTZ,
                MeasureUnit.KILOHERTZ,
                MeasureUnit.MEGAHERTZ,
                MeasureUnit.ASTRONOMICAL_UNIT,
                MeasureUnit.CENTIMETER,
                MeasureUnit.DECIMETER,
                MeasureUnit.FATHOM,
                MeasureUnit.FOOT,
                MeasureUnit.FURLONG,
                MeasureUnit.INCH,
                MeasureUnit.KILOMETER,
                MeasureUnit.LIGHT_YEAR,
                MeasureUnit.METER,
                MeasureUnit.MICROMETER,
                MeasureUnit.MILE,
                MeasureUnit.MILLIMETER,
                MeasureUnit.NANOMETER,
                MeasureUnit.NAUTICAL_MILE,
                MeasureUnit.PARSEC,
                MeasureUnit.PICOMETER,
                MeasureUnit.YARD,
                MeasureUnit.LUX,
                MeasureUnit.CARAT,
                MeasureUnit.GRAM,
                MeasureUnit.KILOGRAM,
                MeasureUnit.METRIC_TON,
                MeasureUnit.MICROGRAM,
                MeasureUnit.MILLIGRAM,
                MeasureUnit.OUNCE,
                MeasureUnit.OUNCE_TROY,
                MeasureUnit.POUND,
                MeasureUnit.STONE,
                MeasureUnit.TON,
                MeasureUnit.GIGAWATT,
                MeasureUnit.HORSEPOWER,
                MeasureUnit.KILOWATT,
                MeasureUnit.MEGAWATT,
                MeasureUnit.MILLIWATT,
                MeasureUnit.WATT,
                MeasureUnit.HECTOPASCAL,
                MeasureUnit.INCH_HG,
                MeasureUnit.MILLIBAR,
                MeasureUnit.MILLIMETER_OF_MERCURY,
                MeasureUnit.POUND_PER_SQUARE_INCH,
                MeasureUnit.KARAT,
                MeasureUnit.KILOMETER_PER_HOUR,
                MeasureUnit.METER_PER_SECOND,
                MeasureUnit.MILE_PER_HOUR,
                MeasureUnit.CELSIUS,
                MeasureUnit.FAHRENHEIT,
                MeasureUnit.KELVIN,
                MeasureUnit.ACRE_FOOT,
                MeasureUnit.BUSHEL,
                MeasureUnit.CENTILITER,
                MeasureUnit.CUBIC_CENTIMETER,
                MeasureUnit.CUBIC_FOOT,
                MeasureUnit.CUBIC_INCH,
                MeasureUnit.CUBIC_KILOMETER,
                MeasureUnit.CUBIC_METER,
                MeasureUnit.CUBIC_MILE,
                MeasureUnit.CUBIC_YARD,
                MeasureUnit.CUP,
                MeasureUnit.DECILITER,
                MeasureUnit.FLUID_OUNCE,
                MeasureUnit.GALLON,
                MeasureUnit.HECTOLITER,
                MeasureUnit.LITER,
                MeasureUnit.MEGALITER,
                MeasureUnit.MILLILITER,
                MeasureUnit.PINT,
                MeasureUnit.QUART,
                MeasureUnit.TABLESPOON,
                MeasureUnit.TEASPOON,
        };
        assertEquals("",  121, units.length);
    }

    @Test
    public void TestCompatible55_1() {
        MeasureUnit[] units = {
                MeasureUnit.G_FORCE,
                MeasureUnit.METER_PER_SECOND_SQUARED,
                MeasureUnit.ARC_MINUTE,
                MeasureUnit.ARC_SECOND,
                MeasureUnit.DEGREE,
                MeasureUnit.RADIAN,
                MeasureUnit.ACRE,
                MeasureUnit.HECTARE,
                MeasureUnit.SQUARE_CENTIMETER,
                MeasureUnit.SQUARE_FOOT,
                MeasureUnit.SQUARE_INCH,
                MeasureUnit.SQUARE_KILOMETER,
                MeasureUnit.SQUARE_METER,
                MeasureUnit.SQUARE_MILE,
                MeasureUnit.SQUARE_YARD,
                MeasureUnit.LITER_PER_KILOMETER,
                MeasureUnit.MILE_PER_GALLON,
                MeasureUnit.BIT,
                MeasureUnit.BYTE,
                MeasureUnit.GIGABIT,
                MeasureUnit.GIGABYTE,
                MeasureUnit.KILOBIT,
                MeasureUnit.KILOBYTE,
                MeasureUnit.MEGABIT,
                MeasureUnit.MEGABYTE,
                MeasureUnit.TERABIT,
                MeasureUnit.TERABYTE,
                MeasureUnit.DAY,
                MeasureUnit.HOUR,
                MeasureUnit.MICROSECOND,
                MeasureUnit.MILLISECOND,
                MeasureUnit.MINUTE,
                MeasureUnit.MONTH,
                MeasureUnit.NANOSECOND,
                MeasureUnit.SECOND,
                MeasureUnit.WEEK,
                MeasureUnit.YEAR,
                MeasureUnit.AMPERE,
                MeasureUnit.MILLIAMPERE,
                MeasureUnit.OHM,
                MeasureUnit.VOLT,
                MeasureUnit.CALORIE,
                MeasureUnit.FOODCALORIE,
                MeasureUnit.JOULE,
                MeasureUnit.KILOCALORIE,
                MeasureUnit.KILOJOULE,
                MeasureUnit.KILOWATT_HOUR,
                MeasureUnit.GIGAHERTZ,
                MeasureUnit.HERTZ,
                MeasureUnit.KILOHERTZ,
                MeasureUnit.MEGAHERTZ,
                MeasureUnit.ASTRONOMICAL_UNIT,
                MeasureUnit.CENTIMETER,
                MeasureUnit.DECIMETER,
                MeasureUnit.FATHOM,
                MeasureUnit.FOOT,
                MeasureUnit.FURLONG,
                MeasureUnit.INCH,
                MeasureUnit.KILOMETER,
                MeasureUnit.LIGHT_YEAR,
                MeasureUnit.METER,
                MeasureUnit.MICROMETER,
                MeasureUnit.MILE,
                MeasureUnit.MILLIMETER,
                MeasureUnit.NANOMETER,
                MeasureUnit.NAUTICAL_MILE,
                MeasureUnit.PARSEC,
                MeasureUnit.PICOMETER,
                MeasureUnit.YARD,
                MeasureUnit.LUX,
                MeasureUnit.CARAT,
                MeasureUnit.GRAM,
                MeasureUnit.KILOGRAM,
                MeasureUnit.METRIC_TON,
                MeasureUnit.MICROGRAM,
                MeasureUnit.MILLIGRAM,
                MeasureUnit.OUNCE,
                MeasureUnit.OUNCE_TROY,
                MeasureUnit.POUND,
                MeasureUnit.STONE,
                MeasureUnit.TON,
                MeasureUnit.GIGAWATT,
                MeasureUnit.HORSEPOWER,
                MeasureUnit.KILOWATT,
                MeasureUnit.MEGAWATT,
                MeasureUnit.MILLIWATT,
                MeasureUnit.WATT,
                MeasureUnit.HECTOPASCAL,
                MeasureUnit.INCH_HG,
                MeasureUnit.MILLIBAR,
                MeasureUnit.MILLIMETER_OF_MERCURY,
                MeasureUnit.POUND_PER_SQUARE_INCH,
                MeasureUnit.KARAT,
                MeasureUnit.KILOMETER_PER_HOUR,
                MeasureUnit.METER_PER_SECOND,
                MeasureUnit.MILE_PER_HOUR,
                MeasureUnit.CELSIUS,
                MeasureUnit.FAHRENHEIT,
                MeasureUnit.GENERIC_TEMPERATURE,
                MeasureUnit.KELVIN,
                MeasureUnit.ACRE_FOOT,
                MeasureUnit.BUSHEL,
                MeasureUnit.CENTILITER,
                MeasureUnit.CUBIC_CENTIMETER,
                MeasureUnit.CUBIC_FOOT,
                MeasureUnit.CUBIC_INCH,
                MeasureUnit.CUBIC_KILOMETER,
                MeasureUnit.CUBIC_METER,
                MeasureUnit.CUBIC_MILE,
                MeasureUnit.CUBIC_YARD,
                MeasureUnit.CUP,
                MeasureUnit.DECILITER,
                MeasureUnit.FLUID_OUNCE,
                MeasureUnit.GALLON,
                MeasureUnit.HECTOLITER,
                MeasureUnit.LITER,
                MeasureUnit.MEGALITER,
                MeasureUnit.MILLILITER,
                MeasureUnit.PINT,
                MeasureUnit.QUART,
                MeasureUnit.TABLESPOON,
                MeasureUnit.TEASPOON,
        };
        assertEquals("",  122, units.length);
    }

    @Test
    public void TestCompatible56_1() {
        MeasureUnit[] units = {
                MeasureUnit.G_FORCE,
                MeasureUnit.METER_PER_SECOND_SQUARED,
                MeasureUnit.ARC_MINUTE,
                MeasureUnit.ARC_SECOND,
                MeasureUnit.DEGREE,
                MeasureUnit.RADIAN,
                MeasureUnit.REVOLUTION_ANGLE,
                MeasureUnit.ACRE,
                MeasureUnit.HECTARE,
                MeasureUnit.SQUARE_CENTIMETER,
                MeasureUnit.SQUARE_FOOT,
                MeasureUnit.SQUARE_INCH,
                MeasureUnit.SQUARE_KILOMETER,
                MeasureUnit.SQUARE_METER,
                MeasureUnit.SQUARE_MILE,
                MeasureUnit.SQUARE_YARD,
                MeasureUnit.LITER_PER_100KILOMETERS,
                MeasureUnit.LITER_PER_KILOMETER,
                MeasureUnit.MILE_PER_GALLON,
                MeasureUnit.BIT,
                MeasureUnit.BYTE,
                MeasureUnit.GIGABIT,
                MeasureUnit.GIGABYTE,
                MeasureUnit.KILOBIT,
                MeasureUnit.KILOBYTE,
                MeasureUnit.MEGABIT,
                MeasureUnit.MEGABYTE,
                MeasureUnit.TERABIT,
                MeasureUnit.TERABYTE,
                MeasureUnit.CENTURY,
                MeasureUnit.DAY,
                MeasureUnit.HOUR,
                MeasureUnit.MICROSECOND,
                MeasureUnit.MILLISECOND,
                MeasureUnit.MINUTE,
                MeasureUnit.MONTH,
                MeasureUnit.NANOSECOND,
                MeasureUnit.SECOND,
                MeasureUnit.WEEK,
                MeasureUnit.YEAR,
                MeasureUnit.AMPERE,
                MeasureUnit.MILLIAMPERE,
                MeasureUnit.OHM,
                MeasureUnit.VOLT,
                MeasureUnit.CALORIE,
                MeasureUnit.FOODCALORIE,
                MeasureUnit.JOULE,
                MeasureUnit.KILOCALORIE,
                MeasureUnit.KILOJOULE,
                MeasureUnit.KILOWATT_HOUR,
                MeasureUnit.GIGAHERTZ,
                MeasureUnit.HERTZ,
                MeasureUnit.KILOHERTZ,
                MeasureUnit.MEGAHERTZ,
                MeasureUnit.ASTRONOMICAL_UNIT,
                MeasureUnit.CENTIMETER,
                MeasureUnit.DECIMETER,
                MeasureUnit.FATHOM,
                MeasureUnit.FOOT,
                MeasureUnit.FURLONG,
                MeasureUnit.INCH,
                MeasureUnit.KILOMETER,
                MeasureUnit.LIGHT_YEAR,
                MeasureUnit.METER,
                MeasureUnit.MICROMETER,
                MeasureUnit.MILE,
                MeasureUnit.MILE_SCANDINAVIAN,
                MeasureUnit.MILLIMETER,
                MeasureUnit.NANOMETER,
                MeasureUnit.NAUTICAL_MILE,
                MeasureUnit.PARSEC,
                MeasureUnit.PICOMETER,
                MeasureUnit.YARD,
                MeasureUnit.LUX,
                MeasureUnit.CARAT,
                MeasureUnit.GRAM,
                MeasureUnit.KILOGRAM,
                MeasureUnit.METRIC_TON,
                MeasureUnit.MICROGRAM,
                MeasureUnit.MILLIGRAM,
                MeasureUnit.OUNCE,
                MeasureUnit.OUNCE_TROY,
                MeasureUnit.POUND,
                MeasureUnit.STONE,
                MeasureUnit.TON,
                MeasureUnit.GIGAWATT,
                MeasureUnit.HORSEPOWER,
                MeasureUnit.KILOWATT,
                MeasureUnit.MEGAWATT,
                MeasureUnit.MILLIWATT,
                MeasureUnit.WATT,
                MeasureUnit.HECTOPASCAL,
                MeasureUnit.INCH_HG,
                MeasureUnit.MILLIBAR,
                MeasureUnit.MILLIMETER_OF_MERCURY,
                MeasureUnit.POUND_PER_SQUARE_INCH,
                MeasureUnit.KARAT,
                MeasureUnit.KILOMETER_PER_HOUR,
                MeasureUnit.KNOT,
                MeasureUnit.METER_PER_SECOND,
                MeasureUnit.MILE_PER_HOUR,
                MeasureUnit.CELSIUS,
                MeasureUnit.FAHRENHEIT,
                MeasureUnit.GENERIC_TEMPERATURE,
                MeasureUnit.KELVIN,
                MeasureUnit.ACRE_FOOT,
                MeasureUnit.BUSHEL,
                MeasureUnit.CENTILITER,
                MeasureUnit.CUBIC_CENTIMETER,
                MeasureUnit.CUBIC_FOOT,
                MeasureUnit.CUBIC_INCH,
                MeasureUnit.CUBIC_KILOMETER,
                MeasureUnit.CUBIC_METER,
                MeasureUnit.CUBIC_MILE,
                MeasureUnit.CUBIC_YARD,
                MeasureUnit.CUP,
                MeasureUnit.CUP_METRIC,
                MeasureUnit.DECILITER,
                MeasureUnit.FLUID_OUNCE,
                MeasureUnit.GALLON,
                MeasureUnit.HECTOLITER,
                MeasureUnit.LITER,
                MeasureUnit.MEGALITER,
                MeasureUnit.MILLILITER,
                MeasureUnit.PINT,
                MeasureUnit.PINT_METRIC,
                MeasureUnit.QUART,
                MeasureUnit.TABLESPOON,
                MeasureUnit.TEASPOON,
        };
        assertEquals("",  129, units.length);
    }

    @Test
    public void TestCompatible57_1() {
        MeasureUnit[] units = {
                MeasureUnit.G_FORCE,
                MeasureUnit.METER_PER_SECOND_SQUARED,
                MeasureUnit.ARC_MINUTE,
                MeasureUnit.ARC_SECOND,
                MeasureUnit.DEGREE,
                MeasureUnit.RADIAN,
                MeasureUnit.REVOLUTION_ANGLE,
                MeasureUnit.ACRE,
                MeasureUnit.HECTARE,
                MeasureUnit.SQUARE_CENTIMETER,
                MeasureUnit.SQUARE_FOOT,
                MeasureUnit.SQUARE_INCH,
                MeasureUnit.SQUARE_KILOMETER,
                MeasureUnit.SQUARE_METER,
                MeasureUnit.SQUARE_MILE,
                MeasureUnit.SQUARE_YARD,
                MeasureUnit.KARAT,
                MeasureUnit.MILLIGRAM_PER_DECILITER,
                MeasureUnit.MILLIMOLE_PER_LITER,
                MeasureUnit.PART_PER_MILLION,
                MeasureUnit.LITER_PER_100KILOMETERS,
                MeasureUnit.LITER_PER_KILOMETER,
                MeasureUnit.MILE_PER_GALLON,
                MeasureUnit.MILE_PER_GALLON_IMPERIAL,
                MeasureUnit.BIT,
                MeasureUnit.BYTE,
                MeasureUnit.GIGABIT,
                MeasureUnit.GIGABYTE,
                MeasureUnit.KILOBIT,
                MeasureUnit.KILOBYTE,
                MeasureUnit.MEGABIT,
                MeasureUnit.MEGABYTE,
                MeasureUnit.TERABIT,
                MeasureUnit.TERABYTE,
                MeasureUnit.CENTURY,
                MeasureUnit.DAY,
                MeasureUnit.HOUR,
                MeasureUnit.MICROSECOND,
                MeasureUnit.MILLISECOND,
                MeasureUnit.MINUTE,
                MeasureUnit.MONTH,
                MeasureUnit.NANOSECOND,
                MeasureUnit.SECOND,
                MeasureUnit.WEEK,
                MeasureUnit.YEAR,
                MeasureUnit.AMPERE,
                MeasureUnit.MILLIAMPERE,
                MeasureUnit.OHM,
                MeasureUnit.VOLT,
                MeasureUnit.CALORIE,
                MeasureUnit.FOODCALORIE,
                MeasureUnit.JOULE,
                MeasureUnit.KILOCALORIE,
                MeasureUnit.KILOJOULE,
                MeasureUnit.KILOWATT_HOUR,
                MeasureUnit.GIGAHERTZ,
                MeasureUnit.HERTZ,
                MeasureUnit.KILOHERTZ,
                MeasureUnit.MEGAHERTZ,
                MeasureUnit.ASTRONOMICAL_UNIT,
                MeasureUnit.CENTIMETER,
                MeasureUnit.DECIMETER,
                MeasureUnit.FATHOM,
                MeasureUnit.FOOT,
                MeasureUnit.FURLONG,
                MeasureUnit.INCH,
                MeasureUnit.KILOMETER,
                MeasureUnit.LIGHT_YEAR,
                MeasureUnit.METER,
                MeasureUnit.MICROMETER,
                MeasureUnit.MILE,
                MeasureUnit.MILE_SCANDINAVIAN,
                MeasureUnit.MILLIMETER,
                MeasureUnit.NANOMETER,
                MeasureUnit.NAUTICAL_MILE,
                MeasureUnit.PARSEC,
                MeasureUnit.PICOMETER,
                MeasureUnit.YARD,
                MeasureUnit.LUX,
                MeasureUnit.CARAT,
                MeasureUnit.GRAM,
                MeasureUnit.KILOGRAM,
                MeasureUnit.METRIC_TON,
                MeasureUnit.MICROGRAM,
                MeasureUnit.MILLIGRAM,
                MeasureUnit.OUNCE,
                MeasureUnit.OUNCE_TROY,
                MeasureUnit.POUND,
                MeasureUnit.STONE,
                MeasureUnit.TON,
                MeasureUnit.GIGAWATT,
                MeasureUnit.HORSEPOWER,
                MeasureUnit.KILOWATT,
                MeasureUnit.MEGAWATT,
                MeasureUnit.MILLIWATT,
                MeasureUnit.WATT,
                MeasureUnit.HECTOPASCAL,
                MeasureUnit.INCH_HG,
                MeasureUnit.MILLIBAR,
                MeasureUnit.MILLIMETER_OF_MERCURY,
                MeasureUnit.POUND_PER_SQUARE_INCH,
                MeasureUnit.KILOMETER_PER_HOUR,
                MeasureUnit.KNOT,
                MeasureUnit.METER_PER_SECOND,
                MeasureUnit.MILE_PER_HOUR,
                MeasureUnit.CELSIUS,
                MeasureUnit.FAHRENHEIT,
                MeasureUnit.GENERIC_TEMPERATURE,
                MeasureUnit.KELVIN,
                MeasureUnit.ACRE_FOOT,
                MeasureUnit.BUSHEL,
                MeasureUnit.CENTILITER,
                MeasureUnit.CUBIC_CENTIMETER,
                MeasureUnit.CUBIC_FOOT,
                MeasureUnit.CUBIC_INCH,
                MeasureUnit.CUBIC_KILOMETER,
                MeasureUnit.CUBIC_METER,
                MeasureUnit.CUBIC_MILE,
                MeasureUnit.CUBIC_YARD,
                MeasureUnit.CUP,
                MeasureUnit.CUP_METRIC,
                MeasureUnit.DECILITER,
                MeasureUnit.FLUID_OUNCE,
                MeasureUnit.GALLON,
                MeasureUnit.GALLON_IMPERIAL,
                MeasureUnit.HECTOLITER,
                MeasureUnit.LITER,
                MeasureUnit.MEGALITER,
                MeasureUnit.MILLILITER,
                MeasureUnit.PINT,
                MeasureUnit.PINT_METRIC,
                MeasureUnit.QUART,
                MeasureUnit.TABLESPOON,
                MeasureUnit.TEASPOON,
        };
        assertEquals("",  134, units.length);
    }

    @Test
    public void TestCompatible58_1() {
        MeasureUnit[] units = {
                MeasureUnit.G_FORCE,
                MeasureUnit.METER_PER_SECOND_SQUARED,
                MeasureUnit.ARC_MINUTE,
                MeasureUnit.ARC_SECOND,
                MeasureUnit.DEGREE,
                MeasureUnit.RADIAN,
                MeasureUnit.REVOLUTION_ANGLE,
                MeasureUnit.ACRE,
                MeasureUnit.HECTARE,
                MeasureUnit.SQUARE_CENTIMETER,
                MeasureUnit.SQUARE_FOOT,
                MeasureUnit.SQUARE_INCH,
                MeasureUnit.SQUARE_KILOMETER,
                MeasureUnit.SQUARE_METER,
                MeasureUnit.SQUARE_MILE,
                MeasureUnit.SQUARE_YARD,
                MeasureUnit.KARAT,
                MeasureUnit.MILLIGRAM_PER_DECILITER,
                MeasureUnit.MILLIMOLE_PER_LITER,
                MeasureUnit.PART_PER_MILLION,
                MeasureUnit.LITER_PER_100KILOMETERS,
                MeasureUnit.LITER_PER_KILOMETER,
                MeasureUnit.MILE_PER_GALLON,
                MeasureUnit.MILE_PER_GALLON_IMPERIAL,
                MeasureUnit.EAST,
                MeasureUnit.NORTH,
                MeasureUnit.SOUTH,
                MeasureUnit.WEST,
                MeasureUnit.BIT,
                MeasureUnit.BYTE,
                MeasureUnit.GIGABIT,
                MeasureUnit.GIGABYTE,
                MeasureUnit.KILOBIT,
                MeasureUnit.KILOBYTE,
                MeasureUnit.MEGABIT,
                MeasureUnit.MEGABYTE,
                MeasureUnit.TERABIT,
                MeasureUnit.TERABYTE,
                MeasureUnit.CENTURY,
                MeasureUnit.DAY,
                MeasureUnit.HOUR,
                MeasureUnit.MICROSECOND,
                MeasureUnit.MILLISECOND,
                MeasureUnit.MINUTE,
                MeasureUnit.MONTH,
                MeasureUnit.NANOSECOND,
                MeasureUnit.SECOND,
                MeasureUnit.WEEK,
                MeasureUnit.YEAR,
                MeasureUnit.AMPERE,
                MeasureUnit.MILLIAMPERE,
                MeasureUnit.OHM,
                MeasureUnit.VOLT,
                MeasureUnit.CALORIE,
                MeasureUnit.FOODCALORIE,
                MeasureUnit.JOULE,
                MeasureUnit.KILOCALORIE,
                MeasureUnit.KILOJOULE,
                MeasureUnit.KILOWATT_HOUR,
                MeasureUnit.GIGAHERTZ,
                MeasureUnit.HERTZ,
                MeasureUnit.KILOHERTZ,
                MeasureUnit.MEGAHERTZ,
                MeasureUnit.ASTRONOMICAL_UNIT,
                MeasureUnit.CENTIMETER,
                MeasureUnit.DECIMETER,
                MeasureUnit.FATHOM,
                MeasureUnit.FOOT,
                MeasureUnit.FURLONG,
                MeasureUnit.INCH,
                MeasureUnit.KILOMETER,
                MeasureUnit.LIGHT_YEAR,
                MeasureUnit.METER,
                MeasureUnit.MICROMETER,
                MeasureUnit.MILE,
                MeasureUnit.MILE_SCANDINAVIAN,
                MeasureUnit.MILLIMETER,
                MeasureUnit.NANOMETER,
                MeasureUnit.NAUTICAL_MILE,
                MeasureUnit.PARSEC,
                MeasureUnit.PICOMETER,
                MeasureUnit.YARD,
                MeasureUnit.LUX,
                MeasureUnit.CARAT,
                MeasureUnit.GRAM,
                MeasureUnit.KILOGRAM,
                MeasureUnit.METRIC_TON,
                MeasureUnit.MICROGRAM,
                MeasureUnit.MILLIGRAM,
                MeasureUnit.OUNCE,
                MeasureUnit.OUNCE_TROY,
                MeasureUnit.POUND,
                MeasureUnit.STONE,
                MeasureUnit.TON,
                MeasureUnit.GIGAWATT,
                MeasureUnit.HORSEPOWER,
                MeasureUnit.KILOWATT,
                MeasureUnit.MEGAWATT,
                MeasureUnit.MILLIWATT,
                MeasureUnit.WATT,
                MeasureUnit.HECTOPASCAL,
                MeasureUnit.INCH_HG,
                MeasureUnit.MILLIBAR,
                MeasureUnit.MILLIMETER_OF_MERCURY,
                MeasureUnit.POUND_PER_SQUARE_INCH,
                MeasureUnit.KILOMETER_PER_HOUR,
                MeasureUnit.KNOT,
                MeasureUnit.METER_PER_SECOND,
                MeasureUnit.MILE_PER_HOUR,
                MeasureUnit.CELSIUS,
                MeasureUnit.FAHRENHEIT,
                MeasureUnit.GENERIC_TEMPERATURE,
                MeasureUnit.KELVIN,
                MeasureUnit.ACRE_FOOT,
                MeasureUnit.BUSHEL,
                MeasureUnit.CENTILITER,
                MeasureUnit.CUBIC_CENTIMETER,
                MeasureUnit.CUBIC_FOOT,
                MeasureUnit.CUBIC_INCH,
                MeasureUnit.CUBIC_KILOMETER,
                MeasureUnit.CUBIC_METER,
                MeasureUnit.CUBIC_MILE,
                MeasureUnit.CUBIC_YARD,
                MeasureUnit.CUP,
                MeasureUnit.CUP_METRIC,
                MeasureUnit.DECILITER,
                MeasureUnit.FLUID_OUNCE,
                MeasureUnit.GALLON,
                MeasureUnit.GALLON_IMPERIAL,
                MeasureUnit.HECTOLITER,
                MeasureUnit.LITER,
                MeasureUnit.MEGALITER,
                MeasureUnit.MILLILITER,
                MeasureUnit.PINT,
                MeasureUnit.PINT_METRIC,
                MeasureUnit.QUART,
                MeasureUnit.TABLESPOON,
                MeasureUnit.TEASPOON,
        };
        assertEquals("",  138, units.length);
    }

    @Test
    public void TestExamplesInDocs() {
        MeasureFormat fmtFr = MeasureFormat.getInstance(
                ULocale.FRENCH, FormatWidth.SHORT);
        Measure measure = new Measure(23, MeasureUnit.CELSIUS);
        assertEquals("23 °C", "23 °C", fmtFr.format(measure));
        Measure measureF = new Measure(70, MeasureUnit.FAHRENHEIT);
        assertEquals("70 °F", "70 °F", fmtFr.format(measureF));
        MeasureFormat fmtFrFull = MeasureFormat.getInstance(
                ULocale.FRENCH, FormatWidth.WIDE);
        assertEquals(
                "70 pied et 5,3 pouces",
                "70 pieds et 5,3 pouces",
                fmtFrFull.formatMeasures(
                        new Measure(70, MeasureUnit.FOOT),
                        new Measure(5.3, MeasureUnit.INCH)));
        assertEquals(
                "1 pied et 1 pouce",
                "1 pied et 1 pouce",
                fmtFrFull.formatMeasures(
                        new Measure(1, MeasureUnit.FOOT),
                        new Measure(1, MeasureUnit.INCH)));
        MeasureFormat fmtFrNarrow = MeasureFormat.getInstance(
                ULocale.FRENCH, FormatWidth.NARROW);
        assertEquals(
                "1′ 1″",
                "1′ 1″",
                fmtFrNarrow.formatMeasures(
                        new Measure(1, MeasureUnit.FOOT),
                        new Measure(1, MeasureUnit.INCH)));
        MeasureFormat fmtEn = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals(
                "1 inch, 2 feet",
                "1 inch, 2 feet",
                fmtEn.formatMeasures(
                        new Measure(1, MeasureUnit.INCH),
                        new Measure(2, MeasureUnit.FOOT)));
    }

    @Test
    public void TestFormatPeriodEn() {
        TimeUnitAmount[] _19m = {new TimeUnitAmount(19.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _1h_23_5s = {
                new TimeUnitAmount(1.0, TimeUnit.HOUR),
                new TimeUnitAmount(23.5, TimeUnit.SECOND)};
        TimeUnitAmount[] _1h_23_5m = {
                new TimeUnitAmount(1.0, TimeUnit.HOUR),
                new TimeUnitAmount(23.5, TimeUnit.MINUTE)};
        TimeUnitAmount[] _1h_0m_23s = {
                new TimeUnitAmount(1.0, TimeUnit.HOUR),
                new TimeUnitAmount(0.0, TimeUnit.MINUTE),
                new TimeUnitAmount(23.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _2y_5M_3w_4d = {
                new TimeUnitAmount(2.0, TimeUnit.YEAR),
                new TimeUnitAmount(5.0, TimeUnit.MONTH),
                new TimeUnitAmount(3.0, TimeUnit.WEEK),
                new TimeUnitAmount(4.0, TimeUnit.DAY)};
        TimeUnitAmount[] _1m_59_9996s = {
                new TimeUnitAmount(1.0, TimeUnit.MINUTE),
                new TimeUnitAmount(59.9996, TimeUnit.SECOND)};
        TimeUnitAmount[] _5h_17m = {
                new TimeUnitAmount(5.0, TimeUnit.HOUR),
                new TimeUnitAmount(17.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _neg5h_17m = {
                new TimeUnitAmount(-5.0, TimeUnit.HOUR),
                new TimeUnitAmount(17.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _19m_28s = {
                new TimeUnitAmount(19.0, TimeUnit.MINUTE),
                new TimeUnitAmount(28.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _0h_0m_9s = {
                new TimeUnitAmount(0.0, TimeUnit.HOUR),
                new TimeUnitAmount(0.0, TimeUnit.MINUTE),
                new TimeUnitAmount(9.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _0h_0m_17s = {
                new TimeUnitAmount(0.0, TimeUnit.HOUR),
                new TimeUnitAmount(0.0, TimeUnit.MINUTE),
                new TimeUnitAmount(17.0, TimeUnit.SECOND)};
        TimeUnitAmount[] _6h_56_92m = {
                new TimeUnitAmount(6.0, TimeUnit.HOUR),
                new TimeUnitAmount(56.92, TimeUnit.MINUTE)};
        TimeUnitAmount[] _3h_4s_5m = {
                new TimeUnitAmount(3.0, TimeUnit.HOUR),
                new TimeUnitAmount(4.0, TimeUnit.SECOND),
                new TimeUnitAmount(5.0, TimeUnit.MINUTE)};
        TimeUnitAmount[] _6_7h_56_92m = {
                new TimeUnitAmount(6.7, TimeUnit.HOUR),
                new TimeUnitAmount(56.92, TimeUnit.MINUTE)};
        TimeUnitAmount[] _3h_5h = {
                new TimeUnitAmount(3.0, TimeUnit.HOUR),
                new TimeUnitAmount(5.0, TimeUnit.HOUR)};

        Object[][] fullData = {
                {_1m_59_9996s, "1 minute, 59.9996 seconds"},
                {_19m, "19 minutes"},
                {_1h_23_5s, "1 hour, 23.5 seconds"},
                {_1h_23_5m, "1 hour, 23.5 minutes"},
                {_1h_0m_23s, "1 hour, 0 minutes, 23 seconds"},
                {_2y_5M_3w_4d, "2 years, 5 months, 3 weeks, 4 days"}};
        Object[][] abbrevData = {
                {_1m_59_9996s, "1 min, 59.9996 sec"},
                {_19m, "19 min"},
                {_1h_23_5s, "1 hr, 23.5 sec"},
                {_1h_23_5m, "1 hr, 23.5 min"},
                {_1h_0m_23s, "1 hr, 0 min, 23 sec"},
                {_2y_5M_3w_4d, "2 yrs, 5 mths, 3 wks, 4 days"}};
        Object[][] narrowData = {
                {_1m_59_9996s, "1m 59.9996s"},
                {_19m, "19m"},
                {_1h_23_5s, "1h 23.5s"},
                {_1h_23_5m, "1h 23.5m"},
                {_1h_0m_23s, "1h 0m 23s"},
                {_2y_5M_3w_4d, "2y 5m 3w 4d"}};


        Object[][] numericData = {
                {_1m_59_9996s, "1:59.9996"},
                {_19m, "19m"},
                {_1h_23_5s, "1:00:23.5"},
                {_1h_0m_23s, "1:00:23"},
                {_1h_23_5m, "1:23.5"},
                {_5h_17m, "5:17"},
                {_neg5h_17m, "-5h 17m"},
                {_19m_28s, "19:28"},
                {_2y_5M_3w_4d, "2y 5m 3w 4d"},
                {_0h_0m_9s, "0:00:09"},
                {_6h_56_92m, "6:56.92"},
                {_6_7h_56_92m, "6:56.92"},
                {_3h_4s_5m, "3h 4s 5m"},
                {_3h_5h, "3h 5h"}};
        Object[][] fullDataDe = {
                {_1m_59_9996s, "1 Minute, 59,9996 Sekunden"},
                {_19m, "19 Minuten"},
                {_1h_23_5s, "1 Stunde, 23,5 Sekunden"},
                {_1h_23_5m, "1 Stunde, 23,5 Minuten"},
                {_1h_0m_23s, "1 Stunde, 0 Minuten und 23 Sekunden"},
                {_2y_5M_3w_4d, "2 Jahre, 5 Monate, 3 Wochen und 4 Tage"}};
        Object[][] numericDataDe = {
                {_1m_59_9996s, "1:59,9996"},
                {_19m, "19 Min."},
                {_1h_23_5s, "1:00:23,5"},
                {_1h_0m_23s, "1:00:23"},
                {_1h_23_5m, "1:23,5"},
                {_5h_17m, "5:17"},
                {_19m_28s, "19:28"},
                {_2y_5M_3w_4d, "2 J, 5 M, 3 W und 4 T"},
                {_0h_0m_17s, "0:00:17"},
                {_6h_56_92m, "6:56,92"},
                {_3h_5h, "3 Std., 5 Std."}};

        NumberFormat nf = NumberFormat.getNumberInstance(ULocale.ENGLISH);
        nf.setMaximumFractionDigits(4);
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE, nf);
        verifyFormatPeriod("en FULL", mf, fullData);
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT, nf);
        verifyFormatPeriod("en SHORT", mf, abbrevData);
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NARROW, nf);
        verifyFormatPeriod("en NARROW", mf, narrowData);
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NUMERIC, nf);
        verifyFormatPeriod("en NUMERIC", mf, numericData);

        nf = NumberFormat.getNumberInstance(ULocale.GERMAN);
        nf.setMaximumFractionDigits(4);
        mf = MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.WIDE, nf);
        verifyFormatPeriod("de FULL", mf, fullDataDe);
        mf = MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.NUMERIC, nf);
        verifyFormatPeriod("de NUMERIC", mf, numericDataDe);

        // Same tests, with Java Locale
        nf = NumberFormat.getNumberInstance(Locale.GERMAN);
        nf.setMaximumFractionDigits(4);
        mf = MeasureFormat.getInstance(Locale.GERMAN, FormatWidth.WIDE, nf);
        verifyFormatPeriod("de FULL(Java Locale)", mf, fullDataDe);
        mf = MeasureFormat.getInstance(Locale.GERMAN, FormatWidth.NUMERIC, nf);
        verifyFormatPeriod("de NUMERIC(Java Locale)", mf, numericDataDe);

    }

    private void verifyFormatPeriod(String desc, MeasureFormat mf, Object[][] testData) {
        StringBuilder builder = new StringBuilder();
        boolean failure = false;
        for (Object[] testCase : testData) {
            String actual = mf.format(testCase[0]);
            if (!testCase[1].equals(actual)) {
                builder.append(String.format("%s: Expected: '%s', got: '%s'\n", desc, testCase[1], actual));
                failure = true;
            }
        }
        if (failure) {
            errln(builder.toString());
        }
    }

    @Test
    public void Test10219FractionalPlurals() {
        double[] values = {1.588, 1.011};
        String[][] expected = {
                {"1 minute", "1.5 minutes", "1.58 minutes"},
                {"1 minute", "1.0 minutes", "1.01 minutes"}
        };
        for (int j = 0; j < values.length; j++) {
            for (int i = 0; i < expected[j].length; i++) {
                NumberFormat nf = NumberFormat.getNumberInstance(ULocale.ENGLISH);
                nf.setRoundingMode(BigDecimal.ROUND_DOWN);
                nf.setMinimumFractionDigits(i);
                nf.setMaximumFractionDigits(i);
                MeasureFormat mf = MeasureFormat.getInstance(
                        ULocale.ENGLISH, FormatWidth.WIDE, nf);
                assertEquals("Test10219", expected[j][i], mf.format(new Measure(values[j], MeasureUnit.MINUTE)));
            }
        }
    }

    @Test
    public void TestGreek() {
        String[] locales = {"el_GR", "el"};
        final MeasureUnit[] units = new MeasureUnit[]{
                MeasureUnit.SECOND,
                MeasureUnit.MINUTE,
                MeasureUnit.HOUR,
                MeasureUnit.DAY,
                MeasureUnit.WEEK,
                MeasureUnit.MONTH,
                MeasureUnit.YEAR};
        FormatWidth[] styles = new FormatWidth[] {FormatWidth.WIDE, FormatWidth.SHORT};
        int[] numbers = new int[] {1, 7};
        String[] expected = {
                // "el_GR" 1 wide
                "1 δευτερόλεπτο",
                "1 λεπτό",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδομάδα",
                "1 μήνας",
                "1 έτος",
                // "el_GR" 1 short
                "1 δευτ.",
                "1 λεπ.",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδ.",
                "1 μήν.",
                "1 έτ.",	        // year (one)
                // "el_GR" 7 wide
                "7 δευτερόλεπτα",
                "7 λεπτά",
                "7 ώρες",
                "7 ημέρες",
                "7 εβδομάδες",
                "7 μήνες",
                "7 έτη",
                // "el_GR" 7 short
                "7 δευτ.",
                "7 λεπ.",
                "7 ώρ.",		    // hour (other)
                "7 ημέρες",
                "7 εβδ.",
                "7 μήν.",
                "7 έτ.",            // year (other)
                // "el" 1 wide
                "1 δευτερόλεπτο",
                "1 λεπτό",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδομάδα",
                "1 μήνας",
                "1 έτος",
                // "el" 1 short
                "1 δευτ.",
                "1 λεπ.",
                "1 ώρα",
                "1 ημέρα",
                "1 εβδ.",
                "1 μήν.",
                "1 έτ.",	        // year (one)
                // "el" 7 wide
                "7 δευτερόλεπτα",
                "7 λεπτά",
                "7 ώρες",
                "7 ημέρες",
                "7 εβδομάδες",
                "7 μήνες",
                "7 έτη",
                // "el" 7 short
                "7 δευτ.",
                "7 λεπ.",
                "7 ώρ.",		    // hour (other)
                "7 ημέρες",
                "7 εβδ.",
                "7 μήν.",
                "7 έτ."};           // year (other
        int counter = 0;
        String formatted;
        for ( int locIndex = 0; locIndex < locales.length; ++locIndex ) {
            for( int numIndex = 0; numIndex < numbers.length; ++numIndex ) {
                for ( int styleIndex = 0; styleIndex < styles.length; ++styleIndex ) {
                    for ( int unitIndex = 0; unitIndex < units.length; ++unitIndex ) {
                        Measure m = new Measure(numbers[numIndex], units[unitIndex]);
                        MeasureFormat fmt = MeasureFormat.getInstance(new ULocale(locales[locIndex]), styles[styleIndex]);
                        formatted = fmt.format(m);
                        assertEquals(
                                "locale: " + locales[locIndex]
                                        + ", style: " + styles[styleIndex]
                                                + ", units: " + units[unitIndex]
                                                        + ", value: " + numbers[numIndex],
                                                expected[counter], formatted);
                        ++counter;
                    }
                }
            }
        }
    }

    @Test
    public void testAUnit() {
        String lastType = null;
        for (MeasureUnit expected : MeasureUnit.getAvailable()) {
            String type = expected.getType();
            String code = expected.getSubtype();
            if (!type.equals(lastType)) {
                logln(type);
                lastType = type;
            }
            MeasureUnit actual = MeasureUnit.internalGetInstance(type, code);
            assertSame("Identity check", expected, actual);
        }
    }

    @Test
    public void testFormatSingleArg() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 meters", mf.format(new Measure(5, MeasureUnit.METER)));
    }

    @Test
    public void testFormatMeasuresZeroArg() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "", mf.formatMeasures());
    }

    @Test
    public void testFormatMeasuresOneArg() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 meters", mf.formatMeasures(new Measure(5, MeasureUnit.METER)));
    }



    @Test
    public void testMultiples() {
        ULocale russia = new ULocale("ru");
        Object[][] data = new Object[][] {
                {ULocale.ENGLISH, FormatWidth.WIDE, "2 miles, 1 foot, 2.3 inches"},
                {ULocale.ENGLISH, FormatWidth.SHORT, "2 mi, 1 ft, 2.3 in"},
                {ULocale.ENGLISH, FormatWidth.NARROW, "2mi 1\u2032 2.3\u2033"},
                {russia, FormatWidth.WIDE,   "2 \u043C\u0438\u043B\u0438 1 \u0444\u0443\u0442 \u0438 2,3 \u0434\u044E\u0439\u043C\u0430"},
                {russia, FormatWidth.SHORT,  "2 \u043C\u0438\u043B\u0438 1 \u0444\u0443\u0442 \u0438 2,3 \u0434\u044E\u0439\u043C."},
                {russia, FormatWidth.NARROW, "2 \u043C\u0438\u043B\u044C 1 \u0444\u0443\u0442 2,3 \u0434\u044E\u0439\u043C\u0430"},
        };
        for (Object[] row : data) {
            MeasureFormat mf = MeasureFormat.getInstance(
                    (ULocale) row[0], (FormatWidth) row[1]);
            assertEquals(
                    "testMultiples",
                    row[2],
                    mf.formatMeasures(
                            new Measure(2, MeasureUnit.MILE),
                            new Measure(1, MeasureUnit.FOOT),
                            new Measure(2.3, MeasureUnit.INCH)));
        }
    }

    @Test
    public void testManyLocaleDurations() {
        Measure hours   = new Measure(5, MeasureUnit.HOUR);
        Measure minutes = new Measure(37, MeasureUnit.MINUTE);
        ULocale ulocDanish       = new ULocale("da");
        ULocale ulocSpanish      = new ULocale("es");
        ULocale ulocFinnish      = new ULocale("fi");
        ULocale ulocIcelandic    = new ULocale("is");
        ULocale ulocNorwegianBok = new ULocale("nb");
        ULocale ulocNorwegianNyn = new ULocale("nn");
        ULocale ulocDutch        = new ULocale("nl");
        ULocale ulocSwedish      = new ULocale("sv");
        Object[][] data = new Object[][] {
            { ulocDanish,       FormatWidth.NARROW,  "5 t og 37 min" },
            { ulocDanish,       FormatWidth.NUMERIC, "5.37" },
            { ULocale.GERMAN,   FormatWidth.NARROW,  "5 Std., 37 Min." },
            { ULocale.GERMAN,   FormatWidth.NUMERIC, "5:37" },
            { ULocale.ENGLISH,  FormatWidth.NARROW,  "5h 37m" },
            { ULocale.ENGLISH,  FormatWidth.NUMERIC, "5:37" },
            { ulocSpanish,      FormatWidth.NARROW,  "5h 37min" },
            { ulocSpanish,      FormatWidth.NUMERIC, "5:37" },
            { ulocFinnish,      FormatWidth.NARROW,  "5t 37min" },
            { ulocFinnish,      FormatWidth.NUMERIC, "5.37" },
            { ULocale.FRENCH,   FormatWidth.NARROW,  "5h 37m" },
            { ULocale.FRENCH,   FormatWidth.NUMERIC, "05:37" },
            { ulocIcelandic,    FormatWidth.NARROW,  "5 klst. og 37 m\u00EDn." },
            { ulocIcelandic,    FormatWidth.NUMERIC, "5:37" },
            { ULocale.JAPANESE, FormatWidth.NARROW,  "5h37m" },
            { ULocale.JAPANESE, FormatWidth.NUMERIC, "5:37" },
            { ulocNorwegianBok, FormatWidth.NARROW,  "5t, 37m" },
            { ulocNorwegianBok, FormatWidth.NUMERIC, "5:37" },
            { ulocDutch,        FormatWidth.NARROW,  "5 u, 37 m" },
            { ulocDutch,        FormatWidth.NUMERIC, "5:37" },
            { ulocNorwegianNyn, FormatWidth.NARROW,  "5 h og 37 min" },
            { ulocNorwegianNyn, FormatWidth.NUMERIC, "5:37" },
            { ulocSwedish,      FormatWidth.NARROW,  "5h 37m" },
            { ulocSwedish,      FormatWidth.NUMERIC, "5:37" },
            { ULocale.CHINESE,  FormatWidth.NARROW,  "5\u5C0F\u65F637\u5206\u949F" },
            { ULocale.CHINESE,  FormatWidth.NUMERIC, "5:37" },
        };
        for (Object[] row : data) {
            MeasureFormat mf = null;
            try{
                mf = MeasureFormat.getInstance( (ULocale)row[0], (FormatWidth)row[1] );
            } catch(Exception e) {
                errln("Exception creating MeasureFormat for locale " + row[0] + ", width " +
                        row[1] + ": " + e);
                continue;
            }
            String result = mf.formatMeasures(hours, minutes);
            if (!result.equals(row[2])) {
                errln("MeasureFormat.formatMeasures for locale " + row[0] + ", width " +
                        row[1] + ", expected \"" + (String)row[2] + "\", got \"" + result + "\"" );
            }
        }
    }

    @Test
    public void testSimplePer() {
        Object DONT_CARE = null;
        Object[][] data = new Object[][] {
                // per unit pattern
                {FormatWidth.WIDE, 1.0, MeasureUnit.SECOND, "1 pound per second", DONT_CARE, 0, 0},
                {FormatWidth.WIDE, 2.0, MeasureUnit.SECOND, "2 pounds per second", DONT_CARE, 0, 0},
                // compound pattern
                {FormatWidth.WIDE, 1.0, MeasureUnit.MINUTE, "1 pound per minute", DONT_CARE, 0, 0},
                {FormatWidth.WIDE, 2.0, MeasureUnit.MINUTE, "2 pounds per minute", DONT_CARE, 0, 0},
                // per unit
                {FormatWidth.SHORT, 1.0, MeasureUnit.SECOND, "1 lb/s", DONT_CARE, 0, 0},
                {FormatWidth.SHORT, 2.0, MeasureUnit.SECOND, "2 lb/s", DONT_CARE, 0, 0},
                // compound
                {FormatWidth.SHORT, 1.0, MeasureUnit.MINUTE, "1 lb/min", DONT_CARE, 0, 0},
                {FormatWidth.SHORT, 2.0, MeasureUnit.MINUTE, "2 lb/min", DONT_CARE, 0, 0},
                // per unit
                {FormatWidth.NARROW, 1.0, MeasureUnit.SECOND, "1#/s", DONT_CARE, 0, 0},
                {FormatWidth.NARROW, 2.0, MeasureUnit.SECOND, "2#/s", DONT_CARE, 0, 0},
                // compound
                {FormatWidth.NARROW, 1.0, MeasureUnit.MINUTE, "1#/min", DONT_CARE, 0, 0},
                {FormatWidth.NARROW, 2.0, MeasureUnit.MINUTE, "2#/min", DONT_CARE, 0, 0},
                // field positions
                {FormatWidth.SHORT, 23.3, MeasureUnit.SECOND, "23.3 lb/s", NumberFormat.Field.DECIMAL_SEPARATOR, 2, 3},
                {FormatWidth.SHORT, 23.3, MeasureUnit.SECOND, "23.3 lb/s", NumberFormat.Field.INTEGER, 0, 2},
                {FormatWidth.SHORT, 23.3, MeasureUnit.MINUTE, "23.3 lb/min", NumberFormat.Field.DECIMAL_SEPARATOR, 2, 3},
                {FormatWidth.SHORT, 23.3, MeasureUnit.MINUTE, "23.3 lb/min", NumberFormat.Field.INTEGER, 0, 2},

        };

        for (Object[] row : data) {
            FormatWidth formatWidth = (FormatWidth) row[0];
            Number amount = (Number) row[1];
            MeasureUnit perUnit = (MeasureUnit) row[2];
            String expected = row[3].toString();
            NumberFormat.Field field = (NumberFormat.Field) row[4];
            int startOffset = ((Integer) row[5]).intValue();
            int endOffset = ((Integer) row[6]).intValue();
            MeasureFormat mf = MeasureFormat.getInstance(
                    ULocale.ENGLISH, formatWidth);
            FieldPosition pos = field != null ? new FieldPosition(field) : new FieldPosition(0);
            String prefix = "Prefix: ";
            assertEquals(
                    "",
                    prefix + expected,
                    mf.formatMeasurePerUnit(
                            new Measure(amount, MeasureUnit.POUND),
                            perUnit,
                            new StringBuilder(prefix),
                            pos).toString());
            if (field != DONT_CARE) {
                assertEquals("startOffset", startOffset, pos.getBeginIndex() - prefix.length());
                assertEquals("endOffset", endOffset, pos.getEndIndex() - prefix.length());
            }
        }
    }

    @Test
    public void testNumeratorPlurals() {
        ULocale polish = new ULocale("pl");
        Object[][] data = new Object[][] {
                {1, "1 stopa na sekundę"},
                {2, "2 stopy na sekundę"},
                {5, "5 stóp na sekundę"},
                {1.5, "1,5 stopy na sekundę"}};

        for (Object[] row : data) {
            MeasureFormat mf = MeasureFormat.getInstance(polish, FormatWidth.WIDE);
            assertEquals(
                    "",
                    row[1],
                    mf.formatMeasurePerUnit(
                            new Measure((Number) row[0], MeasureUnit.FOOT),
                            MeasureUnit.SECOND,
                            new StringBuilder(),
                            new FieldPosition(0)).toString());
        }
    }

    @Test
    public void testGram() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT);
        assertEquals(
                "testGram",
                "1 g",
                mf.format(new Measure(1, MeasureUnit.GRAM)));
        assertEquals(
                "testGram",
                "1 G",
                mf.format(new Measure(1, MeasureUnit.G_FORCE)));
    }

    @Test
    public void testCurrencies() {
        Measure USD_1 = new Measure(1.0, Currency.getInstance("USD"));
        Measure USD_2 = new Measure(2.0, Currency.getInstance("USD"));
        Measure USD_NEG_1 = new Measure(-1.0, Currency.getInstance("USD"));
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("Wide currency", "-1.00 US dollars", mf.format(USD_NEG_1));
        assertEquals("Wide currency", "1.00 US dollars", mf.format(USD_1));
        assertEquals("Wide currency", "2.00 US dollars", mf.format(USD_2));
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT);
        assertEquals("short currency", "-USD1.00", mf.format(USD_NEG_1));
        assertEquals("short currency", "USD1.00", mf.format(USD_1));
        assertEquals("short currency", "USD2.00", mf.format(USD_2));
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NARROW);
        assertEquals("narrow currency", "-$1.00", mf.format(USD_NEG_1));
        assertEquals("narrow currency", "$1.00", mf.format(USD_1));
        assertEquals("narrow currency", "$2.00", mf.format(USD_2));
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.NUMERIC);
        assertEquals("numeric currency", "-$1.00", mf.format(USD_NEG_1));
        assertEquals("numeric currency", "$1.00", mf.format(USD_1));
        assertEquals("numeric currency", "$2.00", mf.format(USD_2));

        mf = MeasureFormat.getInstance(ULocale.JAPAN, FormatWidth.WIDE);
        assertEquals("Wide currency", "-1.00\u7C73\u30C9\u30EB", mf.format(USD_NEG_1));
        assertEquals("Wide currency", "1.00\u7C73\u30C9\u30EB", mf.format(USD_1));
        assertEquals("Wide currency", "2.00\u7C73\u30C9\u30EB", mf.format(USD_2));

        Measure CAD_1 = new Measure(1.0, Currency.getInstance("CAD"));
        mf = MeasureFormat.getInstance(ULocale.CANADA, FormatWidth.SHORT);
        assertEquals("short currency", "CAD1.00", mf.format(CAD_1));
    }

    @Test
    public void testDisplayNames() {
        Object[][] data = new Object[][] {
            // Unit, locale, width, expected result
            { MeasureUnit.YEAR, "en", FormatWidth.WIDE, "years" },
            { MeasureUnit.YEAR, "ja", FormatWidth.WIDE, "年" },
            { MeasureUnit.YEAR, "es", FormatWidth.WIDE, "años" },
            { MeasureUnit.YEAR, "pt", FormatWidth.WIDE, "anos" },
            { MeasureUnit.YEAR, "pt-PT", FormatWidth.WIDE, "anos" },
            { MeasureUnit.AMPERE, "en", FormatWidth.WIDE, "amperes" },
            { MeasureUnit.AMPERE, "ja", FormatWidth.WIDE, "アンペア" },
            { MeasureUnit.AMPERE, "es", FormatWidth.WIDE, "amperios" },
            { MeasureUnit.AMPERE, "pt", FormatWidth.WIDE, "amperes" },
            { MeasureUnit.AMPERE, "pt-PT", FormatWidth.WIDE, "amperes" },
            { MeasureUnit.METER_PER_SECOND_SQUARED, "pt", FormatWidth.WIDE, "metros por segundo ao quadrado" },
            { MeasureUnit.METER_PER_SECOND_SQUARED, "pt-PT", FormatWidth.WIDE, "metros por segundo quadrado" },
            { MeasureUnit.SQUARE_KILOMETER, "pt", FormatWidth.NARROW, "km²" },
            { MeasureUnit.SQUARE_KILOMETER, "pt", FormatWidth.SHORT, "km²" },
            { MeasureUnit.SQUARE_KILOMETER, "pt", FormatWidth.WIDE, "quilômetros quadrados" },
            { MeasureUnit.SECOND, "pt-PT", FormatWidth.NARROW, "s" },
            { MeasureUnit.SECOND, "pt-PT", FormatWidth.SHORT, "s" },
            { MeasureUnit.SECOND, "pt-PT", FormatWidth.WIDE, "segundos" },
            { MeasureUnit.SECOND, "pt", FormatWidth.NARROW, "seg" },
            { MeasureUnit.SECOND, "pt", FormatWidth.SHORT, "segs" },
            { MeasureUnit.SECOND, "pt", FormatWidth.WIDE, "segundos" },
        };

        for (Object[] test : data) {
            MeasureUnit unit = (MeasureUnit) test[0];
            ULocale locale = ULocale.forLanguageTag((String) test[1]);
            FormatWidth formatWidth = (FormatWidth) test[2];
            String expected = (String) test[3];

            MeasureFormat mf = MeasureFormat.getInstance(locale, formatWidth);
            String actual = mf.getUnitDisplayName(unit);
            assertEquals(String.format("Unit Display Name for %s, %s, %s", unit, locale, formatWidth),
                    expected, actual);
        }
    }

    @Test
    public void testFieldPosition() {
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.SHORT);
        FieldPosition pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        fmt.format(new Measure(43.5, MeasureUnit.FOOT), new StringBuffer("123456: "), pos);
        assertEquals("beginIndex", 10, pos.getBeginIndex());
        assertEquals("endIndex", 11, pos.getEndIndex());

        pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        fmt.format(new Measure(43, MeasureUnit.FOOT), new StringBuffer(), pos);
        assertEquals("beginIndex", 0, pos.getBeginIndex());
        assertEquals("endIndex", 0, pos.getEndIndex());
    }

    @Test
    public void testFieldPositionMultiple() {
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.SHORT);
        FieldPosition pos = new FieldPosition(NumberFormat.Field.INTEGER);
        String result = fmt.formatMeasures(
                new StringBuilder(),
                pos,
                new Measure(354, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER)).toString();
        assertEquals("result", "354 m, 23 cm", result);

        // According to javadocs for {@link Format#format} FieldPosition is set to
        // beginning and end of first such field encountered instead of the last
        // such field encountered.
        assertEquals("beginIndex", 0, pos.getBeginIndex());
        assertEquals("endIndex", 3, pos.getEndIndex());

        pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        result = fmt.formatMeasures(
                new StringBuilder("123456: "),
                pos,
                new Measure(354, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER),
                new Measure(5.4, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "123456: 354 m, 23 cm, 5.4 mm", result);
        assertEquals("beginIndex", 23, pos.getBeginIndex());
        assertEquals("endIndex", 24, pos.getEndIndex());

        result = fmt.formatMeasures(
                new StringBuilder(),
                pos,
                new Measure(3, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER),
                new Measure(5.4, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "3 m, 23 cm, 5.4 mm", result);
        assertEquals("beginIndex", 13, pos.getBeginIndex());
        assertEquals("endIndex", 14, pos.getEndIndex());

        pos = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        result = fmt.formatMeasures(
                new StringBuilder("123456: "),
                pos,
                new Measure(3, MeasureUnit.METER),
                new Measure(23, MeasureUnit.CENTIMETER),
                new Measure(5, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "123456: 3 m, 23 cm, 5 mm", result);
        assertEquals("beginIndex", 0, pos.getBeginIndex());
        assertEquals("endIndex", 0, pos.getEndIndex());

        pos = new FieldPosition(NumberFormat.Field.INTEGER);
        result = fmt.formatMeasures(
                new StringBuilder("123456: "),
                pos,
                new Measure(57, MeasureUnit.MILLIMETER)).toString();
        assertEquals("result", "123456: 57 mm", result);
        assertEquals("beginIndex", 8, pos.getBeginIndex());
        assertEquals("endIndex", 10, pos.getEndIndex());

    }

    @Test
    public void testOldFormatWithList() {
        List<Measure> measures = new ArrayList<Measure>(2);
        measures.add(new Measure(5, MeasureUnit.ACRE));
        measures.add(new Measure(3000, MeasureUnit.SQUARE_FOOT));
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 acres, 3,000 square feet", fmt.format(measures));
        assertEquals("", "5 acres", fmt.format(measures.subList(0, 1)));
        List<String> badList = new ArrayList<String>();
        badList.add("be");
        badList.add("you");
        try {
            fmt.format(badList);
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expected) {
           // Expected
        }
    }

    @Test
    public void testOldFormatWithArray() {
        Measure[] measures = new Measure[] {
                new Measure(5, MeasureUnit.ACRE),
                new Measure(3000, MeasureUnit.SQUARE_FOOT),
        };
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "5 acres, 3,000 square feet", fmt.format(measures));
    }

    @Test
    public void testOldFormatBadArg() {
        MeasureFormat fmt = MeasureFormat.getInstance(
                ULocale.ENGLISH, FormatWidth.WIDE);
        try {
            fmt.format("be");
            fail("Expected IllegalArgumentExceptino.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testUnitPerUnitResolution() {
        // Ticket 11274
        MeasureFormat fmt = MeasureFormat.getInstance(Locale.ENGLISH, FormatWidth.SHORT);

        // This fails unless we resolve to MeasureUnit.POUND_PER_SQUARE_INCH
        assertEquals("", "50 psi",
                fmt.formatMeasurePerUnit(
                        new Measure(50, MeasureUnit.POUND),
                        MeasureUnit.SQUARE_INCH,
                        new StringBuilder(),
                        new FieldPosition(0)).toString());
    }

    @Test
    public void testEqHashCode() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.CANADA, FormatWidth.SHORT);
        MeasureFormat mfeq = MeasureFormat.getInstance(ULocale.CANADA, FormatWidth.SHORT);
        MeasureFormat mfne = MeasureFormat.getInstance(ULocale.CANADA, FormatWidth.WIDE);
        MeasureFormat mfne2 = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT);
        verifyEqualsHashCode(mf, mfeq, mfne);
        verifyEqualsHashCode(mf, mfeq, mfne2);
    }

    @Test
    public void testEqHashCodeOfMeasure() {
        Measure _3feetDouble = new Measure(3.0, MeasureUnit.FOOT);
        Measure _3feetInt = new Measure(3, MeasureUnit.FOOT);
        Measure _4feetInt = new Measure(4, MeasureUnit.FOOT);
        verifyEqualsHashCode(_3feetDouble, _3feetInt, _4feetInt);
    }

    @Test
    public void testGetLocale() {
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.SHORT);
        assertEquals("", ULocale.GERMAN, mf.getLocale(ULocale.VALID_LOCALE));
    }

    @Test
    public void TestSerial() {
        checkStreamingEquality(MeasureUnit.CELSIUS);
        checkStreamingEquality(MeasureFormat.getInstance(ULocale.FRANCE, FormatWidth.NARROW));
        checkStreamingEquality(Currency.getInstance("EUR"));
        checkStreamingEquality(MeasureFormat.getInstance(ULocale.GERMAN, FormatWidth.SHORT));
        checkStreamingEquality(MeasureFormat.getCurrencyFormat(ULocale.ITALIAN));
    }

    @Test
    public void TestSerialFormatWidthEnum() {
        // FormatWidth enum values must map to the same ordinal values for all time in order for
        // serialization to work.
        assertEquals("FormatWidth.WIDE", 0, FormatWidth.WIDE.ordinal());
        assertEquals("FormatWidth.SHORT", 1, FormatWidth.SHORT.ordinal());
        assertEquals("FormatWidth.NARROW", 2, FormatWidth.NARROW.ordinal());
        assertEquals("FormatWidth.NUMERIC", 3, FormatWidth.NUMERIC.ordinal());
    }

    @Test
    public void testCurrencyFormatStandInForMeasureFormat() {
        MeasureFormat mf = MeasureFormat.getCurrencyFormat(ULocale.ENGLISH);
        assertEquals(
                "70 feet, 5.3 inches",
                "70 feet, 5.3 inches",
                mf.formatMeasures(
                        new Measure(70, MeasureUnit.FOOT),
                        new Measure(5.3, MeasureUnit.INCH)));
        assertEquals("getLocale", ULocale.ENGLISH, mf.getLocale());
        assertEquals("getNumberFormat", ULocale.ENGLISH, mf.getNumberFormat().getLocale(ULocale.VALID_LOCALE));
        assertEquals("getWidth", MeasureFormat.FormatWidth.WIDE, mf.getWidth());
    }

    @Test
    public void testCurrencyFormatLocale() {
        MeasureFormat mfu = MeasureFormat.getCurrencyFormat(ULocale.FRANCE);
        MeasureFormat mfj = MeasureFormat.getCurrencyFormat(Locale.FRANCE);

        assertEquals("getCurrencyFormat ULocale/Locale", mfu, mfj);
    }

    @Test
    public void testDoubleZero() {
        ULocale en = new ULocale("en");
        NumberFormat nf = NumberFormat.getInstance(en);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        MeasureFormat mf = MeasureFormat.getInstance(en, FormatWidth.WIDE, nf);
        assertEquals(
                "Positive Rounding",
                "4 hours, 23 minutes, 16.00 seconds",
                mf.formatMeasures(
                        new Measure(4.7, MeasureUnit.HOUR),
                        new Measure(23, MeasureUnit.MINUTE),
                        new Measure(16, MeasureUnit.SECOND)));
        assertEquals(
                "Negative Rounding",
                "-4 hours, 23 minutes, 16.00 seconds",
                mf.formatMeasures(
                        new Measure(-4.7, MeasureUnit.HOUR),
                        new Measure(23, MeasureUnit.MINUTE),
                        new Measure(16, MeasureUnit.SECOND)));

    }

    @Test
    public void testIndividualPluralFallback() {
        // See ticket #11986 "incomplete fallback in MeasureFormat".
        // In CLDR 28, fr_CA temperature-generic/short has only the "one" form,
        // and falls back to fr for the "other" form.
        MeasureFormat mf = MeasureFormat.getInstance(new ULocale("fr_CA"), FormatWidth.SHORT);
        Measure twoDeg = new Measure(2, MeasureUnit.GENERIC_TEMPERATURE);
        assertEquals("2 deg temp in fr_CA", "2°", mf.format(twoDeg));
    }

    @Test
    public void testPopulateCache() {
        // Quick check that the lazily added additions to the MeasureUnit cache are present.
        assertTrue("MeasureUnit: unexpectedly few currencies defined", MeasureUnit.getAvailable("currency").size() > 50);
    }

    @Test
    public void testParseObject() {
        MeasureFormat mf = MeasureFormat.getInstance(Locale.GERMAN, FormatWidth.NARROW);
        try {
            mf.parseObject("3m", null);
            fail("MeasureFormat.parseObject(String, ParsePosition) " +
                    "should throw an UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    public void testCLDRUnitAvailability() {
        Set<MeasureUnit> knownUnits = new HashSet<MeasureUnit>();
        Class cMeasureUnit, cTimeUnit;
        try {
            cMeasureUnit = Class.forName("android.icu.util.MeasureUnit");
            cTimeUnit = Class.forName("android.icu.util.TimeUnit");
        } catch (ClassNotFoundException e) {
            fail("Count not load MeasureUnit or TimeUnit class: " + e.getMessage());
            return;
        }
        for (Field field : cMeasureUnit.getFields()) {
            if (field.getGenericType() == cMeasureUnit || field.getGenericType() == cTimeUnit) {
                try {
                    MeasureUnit unit = (MeasureUnit) field.get(cMeasureUnit);
                    knownUnits.add(unit);
                } catch (IllegalArgumentException e) {
                    fail(e.getMessage());
                    return;
                } catch (IllegalAccessException e) {
                    fail(e.getMessage());
                    return;
                }
            }
        }
        for (String type : MeasureUnit.getAvailableTypes()) {
            if (type.equals("currency") || type.equals("compound")) {
                continue;
            }
            for (MeasureUnit unit : MeasureUnit.getAvailable(type)) {
                if (!knownUnits.contains(unit)) {
                    fail("Unit present in CLDR but not available via constant in MeasureUnit: " + unit);
                }
            }
        }
    }

    // DO NOT DELETE THIS FUNCTION! It may appear as dead code, but we use this to generate code
    // for MeasureFormat during the release process.
    static Map<MeasureUnit, Pair<MeasureUnit, MeasureUnit>> getUnitsToPerParts() {
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        Map<MeasureUnit, Pair<String, String>> unitsToPerStrings =
                new HashMap<MeasureUnit, Pair<String, String>>();
        Map<String, MeasureUnit> namesToUnits = new HashMap<String, MeasureUnit>();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            String type = entry.getKey();
            // Currency types are always atomic units, so we can skip these
            if (type.equals("currency")) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String javaName = toJAVAName(unit);
                String[] nameParts = javaName.split("_PER_");
                if (nameParts.length == 1) {
                    namesToUnits.put(nameParts[0], unit);
                } else if (nameParts.length == 2) {
                    unitsToPerStrings.put(unit, Pair.of(nameParts[0], nameParts[1]));
                }
            }
        }
        Map<MeasureUnit, Pair<MeasureUnit, MeasureUnit>> unitsToPerUnits =
                new HashMap<MeasureUnit, Pair<MeasureUnit, MeasureUnit>>();
        for (Map.Entry<MeasureUnit, Pair<String, String>> entry : unitsToPerStrings.entrySet()) {
            Pair<String, String> perStrings = entry.getValue();
            MeasureUnit unit = namesToUnits.get(perStrings.first);
            MeasureUnit perUnit = namesToUnits.get(perStrings.second);
            if (unit != null && perUnit != null) {
                unitsToPerUnits.put(entry.getKey(), Pair.of(unit, perUnit));
            }
        }
        return unitsToPerUnits;
    }

    // DO NOT DELETE THIS FUNCTION! It may appear as dead code, but we use this to generate code
    // for MeasureFormat during the release process.
    static void generateCXXHConstants(String thisVersion) {
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        System.out.println();
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            String type = entry.getKey();
            if (type.equals("currency")) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String code = unit.getSubtype();
                String name = toCamelCase(unit);
                String javaName = toJAVAName(unit);
                checkForDup(seen, name, unit);
                if (isDraft(javaName)) {
                    System.out.println("#ifndef U_HIDE_DRAFT_API");
                }
                System.out.println("    /**");
                System.out.println("     * Returns unit of " + type + ": " + code + ".");
                System.out.println("     * Caller owns returned value and must free it.");
                System.out.println("     * @param status ICU error code.");
                if (isDraft(javaName)) {
                    System.out.println("     * @draft ICU " + getVersion(javaName, thisVersion));
                } else {
                    System.out.println("     * @stable ICU " + getVersion(javaName, thisVersion));
                }
                System.out.println("     */");
                System.out.printf("    static MeasureUnit *create%s(UErrorCode &status);\n\n", name);
                if (isDraft(javaName)) {
                    System.out.println("#endif /* U_HIDE_DRAFT_API */");
                }
            }
        }
    }

    private static void checkForDup(
            Map<String, MeasureUnit> seen, String name, MeasureUnit unit) {
        if (seen.containsKey(name)) {
            throw new RuntimeException("\nCollision!!" + unit + ", " + seen.get(name));
        } else {
            seen.put(name, unit);
        }
    }

    // DO NOT DELETE THIS FUNCTION! It may appear as dead code, but we use this to generate code
    // for MeasureFormat during the release process.
    static void updateJAVAVersions(String thisVersion) {
        System.out.println();
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            String type = entry.getKey();
            if (type.equals("currency")) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String javaName = toJAVAName(unit);
                checkForDup(seen, javaName, unit);
                if (!JAVA_VERSION_MAP.containsKey(javaName)) {
                    System.out.printf("        {\"%s\", \"%s\"},\n", javaName, thisVersion);
                }
            }
        }
    }

    static TreeMap<String, List<MeasureUnit>> getAllUnits() {
        TreeMap<String, List<MeasureUnit>> allUnits = new TreeMap<String, List<MeasureUnit>>();
        for (String type : MeasureUnit.getAvailableTypes()) {
            ArrayList<MeasureUnit> units = new ArrayList<MeasureUnit>(MeasureUnit.getAvailable(type));
            Collections.sort(
                    units,
                    new Comparator<MeasureUnit>() {

                        @Override
                        public int compare(MeasureUnit o1, MeasureUnit o2) {
                            return o1.getSubtype().compareTo(o2.getSubtype());
                        }

                    });
            allUnits.put(type, units);
        }
        return allUnits;
    }

    // DO NOT DELETE THIS FUNCTION! It may appear as dead code, but we use this to generate code
    // for MeasureFormat during the release process.
    static void generateCXXConstants() {
        System.out.println("");
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();

        System.out.println("static const int32_t gOffsets[] = {");
        int index = 0;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            System.out.printf("    %d,\n", index);
            index += entry.getValue().size();
        }
        System.out.printf("    %d\n", index);
        System.out.println("};");
        System.out.println();
        System.out.println("static const int32_t gIndexes[] = {");
        index = 0;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            System.out.printf("    %d,\n", index);
            if (!entry.getKey().equals("currency")) {
                index += entry.getValue().size();
            }
        }
        System.out.printf("    %d\n", index);
        System.out.println("};");
        System.out.println();
        System.out.println("// Must be sorted alphabetically.");
        System.out.println("static const char * const gTypes[] = {");
        boolean first = true;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            if (!first) {
                System.out.println(",");
            }
            System.out.print("    \"" + entry.getKey() + "\"");
            first = false;
        }
        System.out.println();
        System.out.println("};");
        System.out.println();
        System.out.println("// Must be grouped by type and sorted alphabetically within each type.");
        System.out.println("static const char * const gSubTypes[] = {");
        first = true;
        int offset = 0;
        int typeIdx = 0;
        Map<MeasureUnit, Integer> measureUnitToOffset = new HashMap<MeasureUnit, Integer>();
        Map<MeasureUnit, Pair<Integer, Integer>> measureUnitToTypeSubType =
                new HashMap<MeasureUnit, Pair<Integer, Integer>>();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            int subTypeIdx = 0;
            for (MeasureUnit unit : entry.getValue()) {
                if (!first) {
                    System.out.println(",");
                }
                System.out.print("    \"" + unit.getSubtype() + "\"");
                first = false;
                measureUnitToOffset.put(unit, offset);
                measureUnitToTypeSubType.put(unit, Pair.of(typeIdx, subTypeIdx));
                offset++;
                subTypeIdx++;
            }
            typeIdx++;
        }
        System.out.println();
        System.out.println("};");
        System.out.println();

        // Build unit per unit offsets to corresponding type sub types sorted by
        // unit first and then per unit.
        TreeMap<OrderedPair<Integer, Integer>, Pair<Integer, Integer>> unitPerUnitOffsetsToTypeSubType
                = new TreeMap<OrderedPair<Integer, Integer>, Pair<Integer, Integer>>();
        for (Map.Entry<MeasureUnit, Pair<MeasureUnit, MeasureUnit>> entry
                : getUnitsToPerParts().entrySet()) {
            Pair<MeasureUnit, MeasureUnit> unitPerUnit = entry.getValue();
            unitPerUnitOffsetsToTypeSubType.put(
                    OrderedPair.of(
                            measureUnitToOffset.get(unitPerUnit.first),
                            measureUnitToOffset.get(unitPerUnit.second)),
                    measureUnitToTypeSubType.get(entry.getKey()));
        }

        System.out.println("// Must be sorted by first value and then second value.");
        System.out.println("static int32_t unitPerUnitToSingleUnit[][4] = {");
        first = true;
        for (Map.Entry<OrderedPair<Integer, Integer>, Pair<Integer, Integer>> entry
                : unitPerUnitOffsetsToTypeSubType.entrySet()) {
            if (!first) {
                System.out.println(",");
            }
            first = false;
            OrderedPair<Integer, Integer> unitPerUnitOffsets = entry.getKey();
            Pair<Integer, Integer> typeSubType = entry.getValue();
            System.out.printf("        {%d, %d, %d, %d}",
                    unitPerUnitOffsets.first,
                    unitPerUnitOffsets.second,
                    typeSubType.first,
                    typeSubType.second);
        }
        System.out.println();
        System.out.println("};");
        System.out.println();

        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {

            String type = entry.getKey();
            if (type.equals("currency")) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String name = toCamelCase(unit);
                Pair<Integer, Integer> typeSubType = measureUnitToTypeSubType.get(unit);
                if (typeSubType == null) {
                    throw new IllegalStateException();
                }
                checkForDup(seen, name, unit);
                System.out.printf("MeasureUnit *MeasureUnit::create%s(UErrorCode &status) {\n", name);
                System.out.printf("    return MeasureUnit::create(%d, %d, status);\n",
                        typeSubType.first, typeSubType.second);
                System.out.println("}");
                System.out.println();
            }
        }
    }

    private static String toCamelCase(MeasureUnit unit) {
        StringBuilder result = new StringBuilder();
        boolean caps = true;
        String code = unit.getSubtype();
        int len = code.length();
        for (int i = 0; i < len; i++) {
            char ch = code.charAt(i);
            if (ch == '-') {
                caps = true;
            } else if (caps) {
                result.append(Character.toUpperCase(ch));
                caps = false;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    static boolean isTypeHidden(String type) {
        return "currency".equals(type);
    }

    // DO NOT DELETE THIS FUNCTION! It may appear as dead code, but we use this to generate code
    // for MeasureFormat during the release process.
    static void generateBackwardCompatibilityTest(String version) {
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        System.out.println();
        System.out.printf("    public void TestCompatible%s() {\n", version.replace(".", "_"));
        System.out.println("        MeasureUnit[] units = {");
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        int count = 0;
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            if (isTypeHidden(entry.getKey())) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String javaName = toJAVAName(unit);
                checkForDup(seen, javaName, unit);
                System.out.printf("                MeasureUnit.%s,\n", javaName);
                count++;
            }
        }
        System.out.println("        };");
        System.out.printf("        assertEquals(\"\",  %d, units.length);\n", count);
        System.out.println("    }");
    }

    // DO NOT DELETE THIS FUNCTION! It may appear as dead code, but we use this to generate code
    // for MeasureFormat during the release process.
    static void generateCXXBackwardCompatibilityTest(String version) {
        System.out.println();
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        System.out.printf("void MeasureFormatTest::TestCompatible%s() {\n", version.replace(".", "_"));
        System.out.println("    UErrorCode status = U_ZERO_ERROR;");
        System.out.println("    LocalPointer<MeasureUnit> measureUnit;");
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            if (isTypeHidden(entry.getKey())) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String camelCase = toCamelCase(unit);
                checkForDup(seen, camelCase, unit);
                System.out.printf("    measureUnit.adoptInstead(MeasureUnit::create%s(status));\n", camelCase);
            }
        }
        System.out.println("    assertSuccess(\"\", status);");
        System.out.println("}");
    }

    static String toJAVAName(MeasureUnit unit) {
        String code = unit.getSubtype();
        String type = unit.getType();
        String name = code.toUpperCase(Locale.ENGLISH).replace("-", "_");
        if (type.equals("angle")) {
            if (code.equals("minute") || code.equals("second")) {
                name = "ARC_" + name;
            }
        }
        return name;
    }

    // DO NOT DELETE THIS FUNCTION! It may appear as dead code, but we use this to generate code
    // for MeasureFormat during the release process.
    static void generateConstants(String thisVersion) {
        System.out.println();
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        TreeMap<String, List<MeasureUnit>> allUnits = getAllUnits();
        for (Map.Entry<String, List<MeasureUnit>> entry : allUnits.entrySet()) {
            String type = entry.getKey();
            if (isTypeHidden(type)) {
                continue;
            }
            for (MeasureUnit unit : entry.getValue()) {
                String name = toJAVAName(unit);
                String code = unit.getSubtype();
                checkForDup(seen, name, unit);
                System.out.println("    /**");
                System.out.println("     * Constant for unit of " + type +
                        ": " +
                        code);
                // Special case JAVA had old constants for time from before.
                if ("duration".equals(type) && TIME_CODES.contains(code)) {
                    System.out.println("     * @stable ICU 4.0");
                }
                else if (isDraft(name)) {
                    System.out.println("     * @draft ICU " + getVersion(name, thisVersion));
                    System.out.println("     * @provisional This API might change or be removed in a future release.");
                } else {
                    System.out.println("     * @stable ICU " + getVersion(name, thisVersion));
                }
                System.out.println("    */");
                if ("duration".equals(type) && TIME_CODES.contains(code)) {
                    System.out.println("    public static final TimeUnit " + name + " = (TimeUnit) MeasureUnit.internalGetInstance(\"" +
                            type +
                            "\", \"" +
                            code +
                            "\");");
                } else {
                    System.out.println("    public static final MeasureUnit " + name + " = MeasureUnit.internalGetInstance(\"" +
                            type +
                            "\", \"" +
                            code +
                            "\");");
                }
                System.out.println();
            }
        }
        System.out.println("    private static HashMap<Pair<MeasureUnit, MeasureUnit>, MeasureUnit>unitPerUnitToSingleUnit =");
        System.out.println("            new HashMap<Pair<MeasureUnit, MeasureUnit>, MeasureUnit>();");
        System.out.println();
        System.out.println("    static {");
        for (Map.Entry<MeasureUnit, Pair<MeasureUnit, MeasureUnit>> unitPerUnitEntry
                : getUnitsToPerParts().entrySet()) {
            Pair<MeasureUnit, MeasureUnit> unitPerUnit = unitPerUnitEntry.getValue();
            System.out.println("        unitPerUnitToSingleUnit.put(Pair.<MeasureUnit, MeasureUnit>of(MeasureUnit." + toJAVAName(unitPerUnit.first) + ", MeasureUnit." + toJAVAName(unitPerUnit.second) + "), MeasureUnit." + toJAVAName(unitPerUnitEntry.getKey()) + ");");
        }
        System.out.println("    }");
    }

    private static String getVersion(String javaName, String thisVersion) {
        String version = JAVA_VERSION_MAP.get(javaName);
        if (version == null) {
            return thisVersion;
        }
        return version;
    }

    private static boolean isDraft(String javaName) {
        String version = JAVA_VERSION_MAP.get(javaName);
        if (version == null) {
            return true;
        }
        return DRAFT_VERSION_SET.contains(version);
    }

    public <T extends Serializable> void checkStreamingEquality(T item) {
        try {
          ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOut);
          objectOutputStream.writeObject(item);
          objectOutputStream.close();
          byte[] contents = byteOut.toByteArray();
          logln("bytes: " + contents.length + "; " + item.getClass() + ": " + showBytes(contents));
          ByteArrayInputStream byteIn = new ByteArrayInputStream(contents);
          ObjectInputStream objectInputStream = new ObjectInputStream(byteIn);
          Object obj = objectInputStream.readObject();
          assertEquals("Streamed Object equals ", item, obj);
        } catch (IOException e) {
          e.printStackTrace();
          assertNull("Test Serialization " + item.getClass(), e);
        } catch (ClassNotFoundException e) {
          assertNull("Test Serialization " + item.getClass(), e);
        }
      }

    /**
     * @param contents
     * @return
     */
    private String showBytes(byte[] contents) {
      StringBuilder b = new StringBuilder('[');
      for (int i = 0; i < contents.length; ++i) {
        int item = contents[i] & 0xFF;
        if (item >= 0x20 && item <= 0x7F) {
          b.append((char) item);
        } else {
          b.append('(').append(Utility.hex(item, 2)).append(')');
        }
      }
      return b.append(']').toString();
    }

    private void verifyEqualsHashCode(Object o, Object eq, Object ne) {
        assertEquals("verifyEqualsHashCodeSame", o, o);
        assertEquals("verifyEqualsHashCodeEq", o, eq);
        assertNotEquals("verifyEqualsHashCodeNe", o, ne);
        assertNotEquals("verifyEqualsHashCodeEqTrans", eq, ne);
        assertEquals("verifyEqualsHashCodeHashEq", o.hashCode(), eq.hashCode());

        // May be a flaky test, but generally should be true.
        // May need to comment this out later.
        assertNotEquals("verifyEqualsHashCodeHashNe", o.hashCode(), ne.hashCode());
    }

    public static class MeasureUnitHandler implements SerializableTestUtility.Handler
    {
        @Override
        public Object[] getTestObjects()
        {
            MeasureUnit items[] = {
                    MeasureUnit.CELSIUS,
                    Currency.getInstance("EUR")
            };
            return items;
        }
        @Override
        public boolean hasSameBehavior(Object a, Object b)
        {
            MeasureUnit a1 = (MeasureUnit) a;
            MeasureUnit b1 = (MeasureUnit) b;
            return a1.getType().equals(b1.getType())
                    && a1.getSubtype().equals(b1.getSubtype());
        }
    }

    public static class MeasureFormatHandler  implements SerializableTestUtility.Handler
    {
        @Override
        public Object[] getTestObjects()
        {
            MeasureFormat items[] = {
                    MeasureFormat.getInstance(ULocale.FRANCE, FormatWidth.SHORT),
                    MeasureFormat.getInstance(
                            ULocale.FRANCE,
                            FormatWidth.WIDE,
                            NumberFormat.getIntegerInstance(ULocale.CANADA_FRENCH)),
            };
            return items;
        }
        @Override
        public boolean hasSameBehavior(Object a, Object b)
        {
            MeasureFormat a1 = (MeasureFormat) a;
            MeasureFormat b1 = (MeasureFormat) b;
            return a1.getLocale().equals(b1.getLocale())
                    && a1.getWidth().equals(b1.getWidth())
                    && a1.getNumberFormat().equals(b1.getNumberFormat())
                    ;
        }
    }
}
