/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that analyses (e.g. havana) and their associated xref types (e.g. OTTT)
 * exist, and vice versa.
 */
public class AnalysisXrefs extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AnalysisXrefs
	 */
	public AnalysisXrefs() {

		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that analyses (e.g. havana) and their associated xref types (e.g. OTTT) exist, and vice versa");
		setPriority(Priority.AMBER);
		setEffect("Will cause prblems/miscoloring on web display.");
		setFix("Possibly indicates a problem with the Havana/Ensembl merge pipeline");
		setTeamResponsible("genebuilders");

	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// --------------------------------
		// havana/OTTT

		result &= checkAnalysisAndSource(dbre, "Transcript", "havana", "OTTT");

		// --------------------------------
		// other pairs here

		// --------------------------------

		return result;

	} // run

	// --------------------------------------------------------------------------

	private boolean checkAnalysisAndSource(DatabaseRegistryEntry dbre, String objectType, String analysis, String source) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String table = objectType.toLowerCase();

		// find objects with the analysis logic name that don't have any xrefs of a
		// particular type (e.g. transcripts with an analysis of 'havana' should
		// have at least one xref from 'OTTT')
		String sql = "SELECT COUNT(*) FROM "
				+ table
				+ " t, analysis a WHERE t.analysis_id=a.analysis_id AND a.logic_name=? AND t."
				+ table
				+ "_id NOT IN (SELECT DISTINCT(ox.ensembl_id) FROM xref x, object_xref ox, external_db e WHERE x.xref_id=ox.xref_id AND x.external_db_id=e.external_db_id AND e.db_name=? AND ox.ensembl_object_type=?)";

		try {

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, analysis);
			stmt.setString(2, source);
			stmt.setString(3, objectType);

			ResultSet rs = stmt.executeQuery();

			rs.first();
			int rows = rs.getInt(1);

			if (rows > 0) {
				result = false;
				ReportManager.problem(this, con, rows + " " + table + "s with analysis " + analysis
						+ " do not have any associated xrefs of type " + source);
			} else {
				ReportManager.correct(this, con, "All " + table + "s with analysis " + analysis + " have associated " + source + " xrefs");
			}

			rs.close();

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		// and vice-versa - check for objects that have particular types of xref but
		// are the wrong analysis
		// e.g. all transcripts that have an OTTT xref should be of analysis type
		// 'havana'

		sql = "SELECT COUNT(DISTINCT(t." + table + "_id)) FROM xref x, object_xref ox, external_db e, " + table
				+ " t, analysis a WHERE x.xref_id=ox.xref_id AND x.external_db_id=e.external_db_id AND ox.ensembl_id=t." + table
				+ "_id AND a.logic_name != ? AND e.db_name=? AND t.analysis_id=a.analysis_id  AND ox.ensembl_object_type=?";

		try {

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setString(1, analysis);
			stmt.setString(2, source);
			stmt.setString(3, objectType);

			ResultSet rs = stmt.executeQuery();

			rs.first();
			int rows = rs.getInt(1);

			if (rows > 0) {
				result = false;
				ReportManager.problem(this, con, rows + " " + table + "s with " + source
						+ " xrefs do not have an analysis named " + analysis);
			} else {
				ReportManager.correct(this, con, "All " + table + "s with " + source + " xrefs have " + analysis + " analyses");
			}

			rs.close();

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing:\n" + sql);
			e.printStackTrace();

		}

		return result;

	}

	// --------------------------------------------------------------------------

} // AnalysisXrefs
