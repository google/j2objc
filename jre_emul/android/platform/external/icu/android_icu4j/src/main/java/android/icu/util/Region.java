/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011-2016, International Business Machines Corporation
 * All Rights Reserved.
 *******************************************************************************
 */
package android.icu.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;

/**
 * <code>Region</code> is the class representing a Unicode Region Code, also known as a 
 * Unicode Region Subtag, which is defined based upon the BCP 47 standard. We often think of
 * "regions" as "countries" when defining the characteristics of a locale.  Region codes There are different
 * types of region codes that are important to distinguish.
 * <p>
 *  Macroregion - A code for a "macro geographical (continental) region, geographical sub-region, or 
 *  selected economic and other grouping" as defined in 
 *  UN M.49 (http://unstats.un.org/unsd/methods/m49/m49regin.htm). 
 *  These are typically 3-digit codes, but contain some 2-letter codes, such as the LDML code QO 
 *  added for Outlying Oceania.  Not all UNM.49 codes are defined in LDML, but most of them are.
 *  Macroregions are represented in ICU by one of three region types: WORLD ( region code 001 ),
 *  CONTINENTS ( regions contained directly by WORLD ), and SUBCONTINENTS ( things contained directly
 *  by a continent ).
 *  <p>
 *  TERRITORY - A Region that is not a Macroregion. These are typically codes for countries, but also
 *  include areas that are not separate countries, such as the code "AQ" for Antarctica or the code 
 *  "HK" for Hong Kong (SAR China). Overseas dependencies of countries may or may not have separate 
 *  codes. The codes are typically 2-letter codes aligned with the ISO 3166 standard, but BCP47 allows
 *  for the use of 3-digit codes in the future.
 *  <p>
 *  UNKNOWN - The code ZZ is defined by Unicode LDML for use to indicate that the Region is unknown,
 *  or that the value supplied as a region was invalid.
 *  <p>
 *  DEPRECATED - Region codes that have been defined in the past but are no longer in modern usage,
 *  usually due to a country splitting into multiple territories or changing its name.
 *  <p>
 *  GROUPING - A widely understood grouping of territories that has a well defined membership such
 *  that a region code has been assigned for it.  Some of these are UNM.49 codes that do't fall into 
 *  the world/continent/sub-continent hierarchy, while others are just well known groupings that have
 *  their own region code. Region "EU" (European Union) is one such region code that is a grouping.
 *  Groupings will never be returned by the getContainingRegion() API, since a different type of region
 *  ( WORLD, CONTINENT, or SUBCONTINENT ) will always be the containing region instead.
 *  
 * @author       John Emmons
 * @hide Only a subset of ICU is exposed in Android
 */

public class Region implements Comparable<Region> {

    /**
     * RegionType is an enumeration defining the different types of regions.  Current possible
     * values are WORLD, CONTINENT, SUBCONTINENT, TERRITORY, GROUPING, DEPRECATED, and UNKNOWN.
     */

    public enum RegionType {
        /**
         * Type representing the unknown region.
         */
        UNKNOWN,

        /**
         * Type representing a territory.
         */
        TERRITORY,

        /**
         * Type representing the whole world.
         */
        WORLD,
        /**
         * Type representing a continent.
         */
        CONTINENT,
        /**
         * Type representing a sub-continent.
         */
        SUBCONTINENT,
        /**
         * Type representing a grouping of territories that is not to be used in
         * the normal WORLD/CONTINENT/SUBCONTINENT/TERRITORY containment tree.
         */
        GROUPING,
        /**
         * Type representing a region whose code has been deprecated, usually
         * due to a country splitting into multiple territories or changing its name.
         */
        DEPRECATED,
    }

    private String id;
    private int code;
    private RegionType type;
    private Region containingRegion = null;
    private Set<Region> containedRegions = new TreeSet<Region>();
    private List<Region> preferredValues = null;

    private static boolean regionDataIsLoaded = false;

    private static Map<String,Region> regionIDMap = null;       // Map from ID the regions
    private static Map<Integer,Region> numericCodeMap = null;   // Map from numeric code to the regions
    private static Map<String,Region> regionAliases = null;     // Aliases

    private static ArrayList<Region> regions = null;            // This is the main data structure where the Regions are stored.
    private static ArrayList<Set<Region>> availableRegions = null;

    private static final String UNKNOWN_REGION_ID = "ZZ";
    private static final String OUTLYING_OCEANIA_REGION_ID = "QO";
    private static final String WORLD_ID = "001";

    /*
     * Private default constructor.  Use factory methods only.
     */
    private Region () {}

    /*
     * Initializes the region data from the ICU resource bundles.  The region data
     * contains the basic relationships such as which regions are known, what the numeric
     * codes are, any known aliases, and the territory containment data.
     * 
     * If the region data has already loaded, then this method simply returns without doing
     * anything meaningful.
     * 
     */
    private static synchronized void loadRegionData() {

        if ( regionDataIsLoaded ) {
            return;
        }

        regionAliases = new HashMap<String,Region>();
        regionIDMap = new HashMap<String,Region>();
        numericCodeMap = new HashMap<Integer,Region>();

        availableRegions = new ArrayList<Set<Region>>(RegionType.values().length);


        UResourceBundle metadataAlias = null;
        UResourceBundle territoryAlias = null;
        UResourceBundle codeMappings = null;
        UResourceBundle idValidity = null;
        UResourceBundle regionList = null;
        UResourceBundle regionRegular = null;
        UResourceBundle regionMacro = null;
        UResourceBundle regionUnknown = null;
        UResourceBundle worldContainment = null;
        UResourceBundle territoryContainment = null;
        UResourceBundle groupingContainment = null;

        UResourceBundle metadata = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,"metadata",ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        metadataAlias = metadata.get("alias");
        territoryAlias = metadataAlias.get("territory");

        UResourceBundle supplementalData = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME,"supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        codeMappings = supplementalData.get("codeMappings");
        idValidity = supplementalData.get("idValidity");
        regionList = idValidity.get("region");
        regionRegular = regionList.get("regular");
        regionMacro = regionList.get("macroregion");
        regionUnknown = regionList.get("unknown");

        territoryContainment = supplementalData.get("territoryContainment");
        worldContainment = territoryContainment.get("001");
        groupingContainment = territoryContainment.get("grouping");

        String[] continentsArr = worldContainment.getStringArray();
        List<String> continents = Arrays.asList(continentsArr);
        String[] groupingArr = groupingContainment.getStringArray();
        List<String> groupings = Arrays.asList(groupingArr);
        List<String> regionCodes = new ArrayList<String>();

        List<String> allRegions = new ArrayList<String>();
        allRegions.addAll(Arrays.asList(regionRegular.getStringArray()));
        allRegions.addAll(Arrays.asList(regionMacro.getStringArray()));
        allRegions.add(regionUnknown.getString());
        
        for ( String r : allRegions ) {
            int rangeMarkerLocation = r.indexOf("~");
            if ( rangeMarkerLocation > 0 ) {
                StringBuilder regionName = new StringBuilder(r);
                char endRange = regionName.charAt(rangeMarkerLocation+1);
                regionName.setLength(rangeMarkerLocation);
                char lastChar = regionName.charAt(rangeMarkerLocation-1);
                while ( lastChar <= endRange ) {
                    String newRegion = regionName.toString();
                    regionCodes.add(newRegion);
                    lastChar++;
                    regionName.setCharAt(rangeMarkerLocation-1,lastChar);
                }
            } else {
                regionCodes.add(r);
            }
        }

        regions = new ArrayList<Region>(regionCodes.size());

        // First process the region codes and create the master array of regions.
        for ( String id : regionCodes) {
            Region r = new Region();
            r.id = id;
            r.type = RegionType.TERRITORY; // Only temporary - figure out the real type later once the aliases are known.
            regionIDMap.put(id, r);
            if ( id.matches("[0-9]{3}")) {
                r.code = Integer.valueOf(id).intValue();
                numericCodeMap.put(r.code, r);
                r.type = RegionType.SUBCONTINENT;
            } else {
                r.code = -1;
            }
            regions.add(r);
        }


        // Process the territory aliases
        for ( int i = 0 ; i < territoryAlias.getSize(); i++ ) {
            UResourceBundle res = territoryAlias.get(i);
            String aliasFrom = res.getKey();
            String aliasTo = res.get("replacement").getString();

            if ( regionIDMap.containsKey(aliasTo) && !regionIDMap.containsKey(aliasFrom) ) { // This is just an alias from some string to a region
                regionAliases.put(aliasFrom, regionIDMap.get(aliasTo));
            } else {
                Region r;
                if ( regionIDMap.containsKey(aliasFrom) ) {  // This is a deprecated region
                    r = regionIDMap.get(aliasFrom);
                } else { // Deprecated region code not in the master codes list - so need to create a deprecated region for it.
                    r = new Region();
                    r.id = aliasFrom;
                    regionIDMap.put(aliasFrom, r);
                    if ( aliasFrom.matches("[0-9]{3}")) {
                        r.code = Integer.valueOf(aliasFrom).intValue();
                        numericCodeMap.put(r.code, r);
                    } else {
                        r.code = -1;
                    }
                    regions.add(r);
                }
                r.type = RegionType.DEPRECATED;
                List<String> aliasToRegionStrings = Arrays.asList(aliasTo.split(" "));
                r.preferredValues = new ArrayList<Region>();
                for ( String s : aliasToRegionStrings ) {
                    if (regionIDMap.containsKey(s)) {
                        r.preferredValues.add(regionIDMap.get(s));
                    }
                }
            }
        }

        // Process the code mappings - This will allow us to assign numeric codes to most of the territories.
        for ( int i = 0 ; i < codeMappings.getSize(); i++ ) {
            UResourceBundle mapping = codeMappings.get(i);
            if ( mapping.getType() == UResourceBundle.ARRAY ) {
                String [] codeMappingStrings = mapping.getStringArray();
                String codeMappingID = codeMappingStrings[0];
                Integer codeMappingNumber = Integer.valueOf(codeMappingStrings[1]);
                String codeMapping3Letter = codeMappingStrings[2];

                if ( regionIDMap.containsKey(codeMappingID)) {
                    Region r = regionIDMap.get(codeMappingID);
                    r.code = codeMappingNumber.intValue();
                    numericCodeMap.put(r.code, r);
                    regionAliases.put(codeMapping3Letter, r);
                }                    
            }
        }

        // Now fill in the special cases for WORLD, UNKNOWN, CONTINENTS, and GROUPINGS
        Region r;
        if ( regionIDMap.containsKey(WORLD_ID)) {
            r = regionIDMap.get(WORLD_ID);
            r.type = RegionType.WORLD;
        }

        if ( regionIDMap.containsKey(UNKNOWN_REGION_ID)) {
            r = regionIDMap.get(UNKNOWN_REGION_ID);
            r.type = RegionType.UNKNOWN;
        }

        for ( String continent : continents ) {
            if (regionIDMap.containsKey(continent)) {
                r = regionIDMap.get(continent);
                r.type = RegionType.CONTINENT;
            }
        }

        for ( String grouping : groupings ) {
            if (regionIDMap.containsKey(grouping)) {
                r = regionIDMap.get(grouping);
                r.type = RegionType.GROUPING;
            }
        }

        // Special case: The region code "QO" (Outlying Oceania) is a subcontinent code added by CLDR
        // even though it looks like a territory code.  Need to handle it here.

        if ( regionIDMap.containsKey(OUTLYING_OCEANIA_REGION_ID)) {
            r = regionIDMap.get(OUTLYING_OCEANIA_REGION_ID);
            r.type = RegionType.SUBCONTINENT;
        }

        // Load territory containment info from the supplemental data.
        for ( int i = 0 ; i < territoryContainment.getSize(); i++ ) {
            UResourceBundle mapping = territoryContainment.get(i);
            String parent = mapping.getKey();
            if (parent.equals("containedGroupings") || parent.equals("deprecated")) {
                continue; // handle new pseudo-parent types added in ICU data per cldrbug 7808; for now just skip.
                // #11232 is to do something useful with these.
            }
            Region parentRegion = regionIDMap.get(parent);
            for ( int j = 0 ; j < mapping.getSize(); j++ ) {
                String child = mapping.getString(j);
                Region childRegion = regionIDMap.get(child);
                if ( parentRegion != null && childRegion != null ) {                    

                    // Add the child region to the set of regions contained by the parent
                    parentRegion.containedRegions.add(childRegion);

                    // Set the parent region to be the containing region of the child.
                    // Regions of type GROUPING can't be set as the parent, since another region
                    // such as a SUBCONTINENT, CONTINENT, or WORLD must always be the parent.
                    if ( parentRegion.getType() != RegionType.GROUPING) {
                        childRegion.containingRegion = parentRegion;
                    }
                }
            }
        }     

        // Create the availableRegions lists

        for (int i = 0 ; i < RegionType.values().length ; i++) {
            availableRegions.add(new TreeSet<Region>());
        }

        for ( Region ar : regions ) {
            Set<Region> currentSet = availableRegions.get(ar.type.ordinal());
            currentSet.add(ar);
            availableRegions.set(ar.type.ordinal(),currentSet);
        }

        regionDataIsLoaded = true;
    }    

    /** Returns a Region using the given region ID.  The region ID can be either a 2-letter ISO code,
     * 3-letter ISO code,  UNM.49 numeric code, or other valid Unicode Region Code as defined by the CLDR.
     * @param id The id of the region to be retrieved.
     * @return The corresponding region.
     * @throws NullPointerException if the supplied id is null.
     * @throws IllegalArgumentException if the supplied ID cannot be canonicalized to a Region ID that is known by ICU.
     */

    public static Region getInstance(String id) {

        if ( id == null ) {
            throw new NullPointerException();
        }

        loadRegionData();

        Region r = regionIDMap.get(id);

        if ( r == null ) {
            r = regionAliases.get(id);
        }

        if ( r == null ) {
            throw new IllegalArgumentException("Unknown region id: " + id);         
        }

        if ( r.type == RegionType.DEPRECATED && r.preferredValues.size() == 1) {
            r = r.preferredValues.get(0);
        }

        return r;
    }


    /** Returns a Region using the given numeric code as defined by UNM.49
     * @param code The numeric code of the region to be retrieved.
     * @return The corresponding region.
     * @throws IllegalArgumentException if the supplied numeric code is not recognized.
     */

    public static Region getInstance(int code) {

        loadRegionData();

        Region r = numericCodeMap.get(code);

        if ( r == null ) { // Just in case there's an alias that's numeric, try to find it.
            String pad = "";
            if ( code < 10 ) {
                pad = "00";
            } else if ( code < 100 ) {
                pad = "0";
            }
            String id = pad + Integer.toString(code);
            r = regionAliases.get(id);
        }

        if ( r == null ) {
            throw new IllegalArgumentException("Unknown region code: " + code);
        }

        if ( r.type == RegionType.DEPRECATED && r.preferredValues.size() == 1) {
            r = r.preferredValues.get(0);
        }

        return r;
    }


    /** Used to retrieve all available regions of a specific type.
     * 
     * @param type The type of regions to be returned ( TERRITORY, MACROREGION, etc. )
     * @return An unmodifiable set of all known regions that match the given type.
     */

    public static Set<Region> getAvailable(RegionType type) {

        loadRegionData();
        return Collections.unmodifiableSet(availableRegions.get(type.ordinal()));
    }


    /** Used to determine the macroregion that geographically contains this region.
     * 
     * @return The region that geographically contains this region.  Returns NULL if this region is
     *  code "001" (World) or "ZZ" (Unknown region).  For example, calling this method with region "IT" (Italy)
     *  returns the region "039" (Southern Europe).    
     */

    public Region getContainingRegion() {
        loadRegionData();        
        return containingRegion;
    }

    /** Used to determine the macroregion that geographically contains this region and that matches the given type.
     * 
     * @return The region that geographically contains this region and matches the given type.  May return NULL if 
     *  no containing region can be found that matches the given type.  For example, calling this method with region "IT" (Italy)
     *  and type CONTINENT returns the region "150" (Europe).    
     */

    public Region getContainingRegion(RegionType type) {
        loadRegionData();
        if ( containingRegion == null ) {
            return null;
        }
        if ( containingRegion.type.equals(type)) {
            return containingRegion;
        } else {
            return containingRegion.getContainingRegion(type);
        }
    }

    /** Used to determine the sub-regions that are contained within this region.
     * 
     * @return An unmodifiable set containing all the regions that are immediate children
     * of this region in the region hierarchy.  These returned regions could be either macro
     * regions, territories, or a mixture of the two, depending on the containment data as defined
     * in CLDR.  This API may return an empty set if this region doesn't have any sub-regions.
     * For example, calling this method with region "150" (Europe) returns a set containing
     * the various sub regions of Europe - "039" (Southern Europe) - "151" (Eastern Europe) 
     * - "154" (Northern Europe) and "155" (Western Europe).
     */

    public Set<Region> getContainedRegions() {
        loadRegionData();
        return Collections.unmodifiableSet(containedRegions);
    }

    /** Used to determine all the regions that are contained within this region and that match the given type
     * 
     * @return An unmodifiable set containing all the regions that are children of this region
     * anywhere in the region hierarchy and match the given type.  This API may return an empty set
     * if this region doesn't have any sub-regions that match the given type.
     * For example, calling this method with region "150" (Europe) and type "TERRITORY" returns a set
     *  containing all the territories in Europe ( "FR" (France) - "IT" (Italy) - "DE" (Germany) etc. )
     */

    public Set<Region> getContainedRegions(RegionType type) {

        loadRegionData();

        Set<Region> result = new TreeSet<Region>();
        Set<Region> cr = getContainedRegions();

        for ( Region r : cr ) {
            if ( r.getType() == type ) {
                result.add(r);
            } else {
                result.addAll(r.getContainedRegions(type));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /** 
     * @return For deprecated regions, return an unmodifiable list of the regions that are the preferred replacement regions for this region.  
     * Returns null for a non-deprecated region.  For example, calling this method with region "SU" (Soviet Union) would 
     * return a list of the regions containing "RU" (Russia), "AM" (Armenia), "AZ" (Azerbaijan), etc... 
     */
    public List<Region> getPreferredValues() {

        loadRegionData();

        if ( type == RegionType.DEPRECATED) {
            return Collections.unmodifiableList(preferredValues);
        } else {
            return null;
        }
    }

    /**
     * @return Returns true if this region contains the supplied other region anywhere in the region hierarchy.
     */
    public boolean contains(Region other) {

        loadRegionData();

        if (containedRegions.contains(other)) {
            return true;
        } else {
            for (Region cr : containedRegions) {
                if (cr.contains(other)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Returns the string representation of this region
     * 
     * @return The string representation of this region, which is its ID.
     */

    public String toString() {
        return id;
    }

    /**
     * Returns the numeric code for this region
     * 
     * @return The numeric code for this region. Returns a negative value if the given region does not have a numeric
     *         code assigned to it. This is a very rare case and only occurs for a few very small territories.
     */

    public int getNumericCode() {
        return code;
    }

    /** Returns this region's type.
     * 
     * @return This region's type classification, such as MACROREGION or TERRITORY.
     */

    public RegionType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Region other) {
        return id.compareTo(other.id);
    }
}
