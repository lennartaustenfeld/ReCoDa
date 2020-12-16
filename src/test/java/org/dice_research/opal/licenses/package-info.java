/**
 * The tests are sub-divided in 3 groups:
 * 
 * <p>
 * (1) Production test: Default tests.
 * </p>
 * 
 * <p>
 * (2) CC tests: These tests require data of an additional repository. If the
 * data can not be found, the tests will be skipped. The directory to use can be
 * set by the system property 'cc.licenserdf' (VM arguments). For instance:
 * <code>-Dcc.licenserdf=../cc.licenserdf/cc/licenserdf/licenses/</code>
 * 
 * Repository used for development:
 * https://github.com/projekt-opal/cc.licenserdf
 * 
 * Official Creative Commons repository:
 * https://github.com/creativecommons/cc.licenserdf
 * </p>
 * 
 * <p>
 * (3) EDP evaluation tests.
 * </p>
 * 
 * <p>
 * See also the main README.md file.
 * </p>
 * 
 * @see org.dice_research.opal.licenses.utils.Cfg
 * 
 * @author Adrian Wilke
 */
package org.dice_research.opal.licenses;