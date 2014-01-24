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

package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * These are the tests that register themselves as compara_homology. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMemberId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyHomologyId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeySequenceId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.SingleDBCollations </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckHomology </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.DuplicateGenomeDb </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckFlatProteinTrees </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSequenceTable </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyFamilyId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId </li> 
 * </ul>
 *
 * @author Autogenerated
 *
 */
public class ComparaHomology extends GroupOfTests {
	
	public ComparaHomology() {

		addTest(
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMemberId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyHomologyId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeySequenceId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class,
			org.ensembl.healthcheck.testcase.compara.CheckHomology.class,
			org.ensembl.healthcheck.testcase.compara.DuplicateGenomeDb.class,
			org.ensembl.healthcheck.testcase.compara.CheckFlatProteinTrees.class,
			org.ensembl.healthcheck.testcase.compara.CheckSequenceTable.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyFamilyId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId.class
		);
	}
}
