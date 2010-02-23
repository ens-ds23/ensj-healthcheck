/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock extends SingleDatabaseTestCase {

    /**
     * Create an ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock that applies to a specific set of databases.
     */
    public ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock() {

        addToGroup("compara_genomic");
        setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
        setTeamResponsible("compara");

    }

    /**
     * Run the test.
     * 
     * @param dbre
     *          The database to use.
     * @return true if the test passed.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();

        if (tableHasRows(con, "method_link_species_set")) {

            result &= checkForOrphansWithConstraint(con,
                "method_link_species_set", "method_link_species_set_id",
                "genomic_align_block", "method_link_species_set_id",
                "method_link_id in (SELECT method_link_id FROM method_link WHERE class like 'GenomicAlign%')");
            result &= checkForOrphans(con,
                "genomic_align_block", "method_link_species_set_id",
                "method_link_species_set", "method_link_species_set_id");
/*              This is now checked in the ForeignKeyGenomicAlignId healthcheck
              result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id",
                  "genomic_align", "method_link_species_set_id", "method_link_id < 100");
              result &= checkForOrphans(con, "genomic_align", "method_link_species_set_id", "method_link_species_set",
                  "method_link_species_set_id");
*/

        } else {
            ReportManager.correct(this, con, "NO ENTRIES in method_link_species_set table, so nothing to test IGNORED");
        }

        return result;

    }

} // ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock
