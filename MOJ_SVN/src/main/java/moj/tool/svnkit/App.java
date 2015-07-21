package moj.tool.svnkit;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // パラメータチェック
        if(!checkParam("repository_url")) return;
        String repository_url = System.getProperty("repository_url");
        
        if(!checkParam("pj_root")) return;        
        String pj_root = System.getProperty("pj_root");

        if(!checkParam("revision")) return;        
        String revision = System.getProperty("revision");

        if(!checkParam("local_root")) return;
        String local_root = System.getProperty("local_root");
        
        String svn_user = System.getenv("SVN_USER");
        String svn_password = System.getenv("SVN_PASSWORD");
        
        // Repository差分Export
        SVNHelper helper = new SVNHelper(repository_url, svn_user, svn_password);
        helper.exportRevChangedFiles(pj_root, Long.parseLong(revision), local_root);
    }
    
    private static void usage(){
        System.out.println("[usage]: moj.tool.svnkit.App {repository_url} {pj_root} {local_root} {svn_user} {svn_password}");
    }
    
    private static boolean checkParam(String p){
        String pValue = System.getProperty(p);
        if(pValue == null || pValue.equals("")){
            System.out.println(p + "を設定してください");
            return false;
        }
        return true;
    }
}
