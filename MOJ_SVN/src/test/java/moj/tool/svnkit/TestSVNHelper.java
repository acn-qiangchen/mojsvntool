package moj.tool.svnkit;

import java.io.IOException;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestSVNHelper {
    
    private static SVNHelper helper = null;
//	private static final String URL = "file://192.168.101.7/svn/repos/Design_Document_Hanreki/branches/MOJ_BRANCH_ICHIBUYUYO_START_20150226/";
//	private static final String TARGET = "/branches/MOJ_BRANCH_ICHIBUYUYO_START_20150226/20.アプリケーション設計/02.プログラム構造設計書/90.内部資料";
    private static final String REPO_URL = "svn://52.69.229.71/myrepo/trunk";
    private static final String LOCAL_ROOT = "/Users/shearer/Documents/sdbx/SubversionTest/EXPORT_DIR";
    private static final String PJ_ROOT = "/myrepo/trunk/moj";
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        helper = new SVNHelper(REPO_URL, "tink", "tink");
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testGetChangedFileList() throws IOException {
        //Files.deleteIfExists(Paths.get(LOCAL_ROOT));
        //helper.exportRevChangedFiles(1, 1, true);
        helper.exportRevChangedFiles(PJ_ROOT, 1, LOCAL_ROOT);
        helper.exportRevChangedFiles(PJ_ROOT, 4, LOCAL_ROOT);
        //helper.exportRevChangedFiles(PJ_ROOT, -1, LOCAL_ROOT);
        //helper.exportRevChangedFiles(9, 9, true);
    }
    
    @Test
    public void testExportBase(){
        URI uri = URI.create(REPO_URL);
        String frm = uri.getScheme() + "://" + uri.getAuthority() + PJ_ROOT;
        helper.exportBase(frm, 6, Paths.get(LOCAL_ROOT).resolve("moj").toString());
        helper.exportRevChangedFiles(PJ_ROOT, 10, LOCAL_ROOT);
    }
    
    @Test
    @Ignore
    public void testGetDestPath() {
        String url = "file://192.168.101.7/svn/repos/Design_Document_Hanreki/"
                + "branches/MOJ_BRANCH_ICHIBUYUYO_START_20150226/20.アプリケーション設計/02.プログラム構造設計書/90.内部資料/work/sub3/file3.txt";
        
        String svnRoot = "/svn/repos/Design_Document_Hanreki/branches/MOJ_BRANCH_ICHIBUYUYO_START_20150226";
        String destRoot = "C:/00_WorkFolder/03_sandbox/svnKitTA/exp";
        
        String exp = "C:\\00_WorkFolder\\03_sandbox\\svnKitTA\\exp\\20.アプリケーション設計\\02.プログラム構造設計書\\90.内部資料\\work\\sub3\\file3.txt";
        
        String actual = getDestPath(url, svnRoot, destRoot);
        assertEquals(exp, actual);
        
    }
    
    @Test
    public void test1() {
        String url = "file://192.168.101.7:1234/svn/repos/Design_Document_Hanreki/";
        String svnbase = "/svn/repos/Design_Document_Hanreki/";
        
        URI uri = URI.create(url);
        String schema = uri.getScheme();
        String authority = uri.getAuthority();
        
    }
    
    private String getDestPath(String url, String svnRoot, String destRoot) {
        Path relativePath = Paths.get(svnRoot).relativize(Paths.get(URI.create(url).getPath()));
        Path destPath = Paths.get(destRoot).resolve(relativePath);
        
        return destPath.toString();
    }
}
