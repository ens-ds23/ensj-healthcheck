/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check compara genome_db table against core meta one.
 */

public class CheckGenomeDB extends MultiDatabaseTestCase {

    /**
     * Create a new instance of MetaCrossSpecies
     */
    public CheckGenomeDB() {

        addToGroup("compara_external_foreign_keys");
        setDescription("Check that the properties of the genome_db table (taxon_id, assembly" +
            " and genebuild) correspond to the meta data in the core DB and vice versa.");
        setTeamResponsible(Team.COMPARA);

    }
 
    /**
     * Check that the properties of the genome_db table (taxon_id, assembly and genebuild)
     * correspond to the meta data in the core DB and vice versa.
     * NB: A warning message is displayed if some dnafrags cannot be checked because
     * there is not any connection to the corresponding core database.
     * 
     * @param dbr
     *          The database registry containing all the specified databases.
     * @return true if the all the dnafrags are top_level seq_regions in their corresponding
     *    core database.
     */
    public boolean run(DatabaseRegistry dbr) {

        boolean result = true;

        // Get compara DB connection
        DatabaseRegistryEntry[] allComparaDBs = dbr.getAll(DatabaseType.COMPARA);
        if (allComparaDBs.length == 0) {
          result = false;
          ReportManager.problem(this, "", "Cannot find compara database");
          usage();
          return false;
        }

        Map speciesDbrs = getSpeciesDatabaseMap(dbr, true);

        for (int i = 0; i < allComparaDBs.length; i++) {
            result &= checkAssemblies(allComparaDBs[i]);
            result &= checkGenomeDB(allComparaDBs[i], speciesDbrs);
        }
        return result;
    }


    public boolean checkAssemblies(DatabaseRegistryEntry comparaDbre) {

        boolean result = true;
        Connection comparaCon = comparaDbre.getConnection();
        String comparaDbName = (comparaCon == null) ? "no_database" : DBUtils.getShortDatabaseName(comparaCon);

        // Get list of species with more than 1 default assembly
        String sql = "SELECT DISTINCT genome_db.name FROM genome_db WHERE assembly_default = 1"
            + " GROUP BY name HAVING count(*) <> 1";
        try {
          Statement stmt = comparaCon.createStatement();
          ResultSet rs = stmt.executeQuery(sql);
          while (rs.next()) {
            ReportManager.problem(this, comparaCon, "There are more than 1 default assembly for "
              + rs.getString(1));
            result = false;
          }
          rs.close();
          stmt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }

        // Get list of species with a non-default assembly
        sql = "SELECT DISTINCT name FROM genome_db WHERE assembly_default = 0";
        if (!Pattern.matches(".*master.*", comparaDbName)) {
            try {
              Statement stmt = comparaCon.createStatement();
              ResultSet rs = stmt.executeQuery(sql);
              while (rs.next()) {
                ReportManager.problem(this, comparaCon, comparaDbName + " There is at least one non-default assembly for "
                  + rs.getString(1) + " (this should not happen in the release DB)");
              }
              rs.close();
              stmt.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
        }

        // Get list of species with no default assembly
        sql = "SELECT DISTINCT gdb1.name FROM genome_db gdb1 LEFT JOIN genome_db gdb2"
            + " ON (gdb1.name = gdb2.name and gdb1.assembly_default = 1 - gdb2.assembly_default)"
            + " WHERE gdb1.assembly_default = 0 AND gdb2.name is null";
        try {
          Statement stmt = comparaCon.createStatement();
          ResultSet rs = stmt.executeQuery(sql);
          while (rs.next()) {
            ReportManager.problem(this, comparaCon, "There is no default assembly for "
              + rs.getString(1));
            result = false;
          }
          rs.close();
          stmt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }

        return result;
    }


    public boolean checkGenomeDB(DatabaseRegistryEntry comparaDbre, Map speciesDbrs) {

        boolean result = true;
        Connection comparaCon = comparaDbre.getConnection();

        // Get list of species in compara
        Vector comparaSpecies = new Vector();
        String sql = "SELECT DISTINCT genome_db.name FROM genome_db WHERE assembly_default = 1"
            + " AND name <> 'ancestral_sequences'";
        try {
          Statement stmt = comparaCon.createStatement();
          ResultSet rs = stmt.executeQuery(sql);
          while (rs.next()) {
            Species species = Species.resolveAlias(rs.getString(1).toLowerCase().replace(' ', '_'));
	     if (species.toString().equals("unknown")) {
              ReportManager.problem(this, comparaCon, "No species defined for " + rs.getString(1) +
                  " in org.ensembl.healthcheck.Species");
            } else {
              comparaSpecies.add(species);
            }
          }
          rs.close();
          stmt.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        
        boolean allSpeciesFound = true;
        for (int i = 0; i < comparaSpecies.size(); i++) {
          Species species = (Species) comparaSpecies.get(i);
          DatabaseRegistryEntry[] speciesDbr = (DatabaseRegistryEntry[]) speciesDbrs.get(species);
          if (speciesDbr != null) {
            Connection speciesCon = speciesDbr[0].getConnection();
            /* Check taxon_id */
            String sql1, sql2;

            sql1 = "SELECT \"" + species + "\", \"taxon_id\", taxon_id FROM genome_db" +
                " WHERE genome_db.name = \"" + species + "\" AND  assembly_default = 1";
            sql2 = "SELECT \"" + species + "\", \"taxon_id\", meta_value FROM meta" +
                " WHERE meta_key = \"species.taxonomy_id\"";
            result &= compareQueries(comparaCon, sql1, speciesCon, sql2);
            /* Check assembly */
            sql1 = "SELECT \"" + species + "\", \"assembly\", assembly FROM genome_db" +
                " WHERE genome_db.name = \"" + species + "\" AND  assembly_default = 1";
            sql2 = "SELECT \"" + species + "\", \"assembly\", meta_value FROM meta" +
                " WHERE meta_key = \"assembly.default\"";
            result &= compareQueries(comparaCon, sql1, speciesCon, sql2);
            /* Check genebuild */
            sql1 = "SELECT \"" + species + "\", \"genebuild\", genebuild FROM genome_db" +
                " WHERE genome_db.name = \"" + species + "\" AND  assembly_default = 1";
            sql2 = "SELECT \"" + species + "\", \"genebuild\", meta_value FROM meta" +
                " WHERE meta_key = \"genebuild.start_date\"";
            result &= compareQueries(comparaCon, sql1, speciesCon, sql2);
          } else {
            ReportManager.problem(this, comparaCon, "No connection for " + species);
            allSpeciesFound = false;
          }
        }
        if (!allSpeciesFound) {
          usage();
        }

        return result;
    }
    
    /**
     * Prints the usage through the ReportManager
     * 
     * @param
     *          The database registry containing all the specified databases.
     * @return true if the all the dnafrags are top_level seq_regions in their corresponding
     *    core database.
     */
    private void usage() {

      ReportManager.problem(this, "USAGE", "run-healthcheck.sh -d ensembl_compara_.+ " + 
          " -d2 .+_core_.+ CheckGenomeDB");
    }
    
} // CheckTopLevelDnaFrag
