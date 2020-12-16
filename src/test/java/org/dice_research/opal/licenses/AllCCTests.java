package org.dice_research.opal.licenses;

import org.dice_research.opal.licenses.cc.CcBackMappingTest;
import org.dice_research.opal.licenses.cc.CcDataTest;
import org.dice_research.opal.licenses.cc.CcEvaluationSingleTest;
import org.dice_research.opal.licenses.cc.CcEvaluationTest;
import org.dice_research.opal.licenses.cc.ExecutionTest;
import org.dice_research.opal.licenses.cc.OdrlTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

		CcDataTest.class,

		CcBackMappingTest.class,

		CcEvaluationSingleTest.class,

		CcEvaluationTest.class,

		ExecutionTest.class,

		OdrlTest.class,

})

public class AllCCTests {
}