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

/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all of certain types of objects have analysis_descriptions. Also
 * check that displayable field is set.
 */

public class AnalysisDescription extends SingleDatabaseTestCase {

	String[] types = { "probe_feature", "feature_set", "result_set" }; // annotated_feature?

	/**
	 * Create a new AnalysisDescription testcase.
	 */
	public AnalysisDescription() {

		addToGroup("funcgen");
		addToGroup("funcgen-release");
		setDescription("Check that all of certain types of objects have analysis_descriptions; also check that displayable field is set.");
		setTeamResponsible(Team.FUNCGEN);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		result &= checkDescriptions(dbre);
		result &= checkDisplayable(dbre);

		return result;

	} // run

	// ------------------------------------------------------------------------------

	private boolean checkDescriptions(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// cache logic_names by analysis_id
		Map logicNamesByAnalID = getLogicNamesFromAnalysisTable(con);

		for (int i = 0; i < types.length; i++) {

			// get analyses that are used
			// special case for transcripts - need to link to gene table and get
			// analysis from there

			String sql = "SELECT DISTINCT(analysis_id) FROM " + types[i];
			// System.out.println(sql);
			// if (types[i].equals("transcript")) {
			// sql =
			// "SELECT DISTINCT(g.analysis_id) FROM gene g, transcript t WHERE t.gene_id=g.gene_id";
			// }
			String[] analyses = DBUtils.getColumnValues(con, sql);

			// check each one has an analysis_description
			for (int j = 0; j < analyses.length; j++) {
				int count = DBUtils.getRowCount(con,
						"SELECT COUNT(*) FROM analysis_description WHERE analysis_id="
								+ analyses[j]);
				if (count == 0) {
					ReportManager.problem(this, con, "Analysis "
							+ logicNamesByAnalID.get(analyses[j])
							+ " is used in " + types[i]
							+ " but has no entry in analysis_description");
					result = false;
				} else {
					ReportManager.correct(this, con, "Analysis "
							+ logicNamesByAnalID.get(analyses[j])
							+ " is used in " + types[i]
							+ " and has an entry in analysis_description");
				}

			}
		}

		return result;

	}

	// ------------------------------------------------------------------------------

	private boolean checkDisplayable(DatabaseRegistryEntry dbre) {

		return checkNoNulls(dbre.getConnection(), "analysis_description",
				"displayable");

	}

	// ------------------------------------------------------------------------------

} // AnalysisDescription
