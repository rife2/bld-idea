package rife.bld.idea;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.PsiErrorElementUtil;

@TestDataPath("\\$CONTENT_ROOT/src/test/testData")
public class BldPluginTest extends BasePlatformTestCase {
    public void testXMLFile() {
        var psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>");
        var xmlFile = assertInstanceOf(psiFile, XmlFile.class);

        assertFalse(PsiErrorElementUtil.hasErrors(getProject(), xmlFile.getVirtualFile()));

        assertNotNull(xmlFile.getRootTag());

        var root = xmlFile.getRootTag();
        assertEquals("foo", root.getName());
        assertEquals("bar", root.getValue().getText());
    }
}