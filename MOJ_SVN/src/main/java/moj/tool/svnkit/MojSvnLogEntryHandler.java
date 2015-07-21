package moj.tool.svnkit;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

/**
 * SubversionのCommit Logを処理するHandlerクラス<br>
 * logに関連するfileに対し、下記処理を行う：<br>
 * <ul>
 * <li>ADDまたはUPDATEの場合、該当ファイルをexportする</li>
 * <li>DELETEの場合、該当ファイルがexport先存在する場合削除する</li>
 * <li>ログの対象ファイルがDirectoryの場合、該当Directoryのみがexportする。（Directory下のfileはexportしない）</li>
 * </ul>
 *
 * @author shearer
 */
public class MojSvnLogEntryHandler implements ISVNLogEntryHandler {
    /*
     private String svnRoot = "/svn/repos/Design_Document_Hanreki/branches/MOJ_BRANCH_ICHIBUYUYO_START_20150226";
     private String destRoot = "C:/00_WorkFolder/03_sandbox/svnKitTA/exp";
     private SVNClientManager ourClientManager = null;
     private String repositoryAddress = "file://192.168.101.7/svn/repos/Design_Document_Hanreki";

     private String pjRoot = "/myrepo/trunk/moj"; // pj root
     private String destRoot = "/Users/shearer/Documents/sdbx/SubversionTest/EXPORT_DIR";
     private SVNClientManager ourClientManager = null;
     private String repoRootURL = "svn://52.69.229.71/myrepo";
     */

    private String pjRoot = ""; // pj root
    private String destRoot = "";
    private SVNClientManager ourClientManager = null;
    private String repoRootURL = "";

    @Override
    public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {

        // LogEntryのRevision取得
        long revision = logEntry.getRevision();

        // Logから関連するファイルのPathを取得し、そのファイルをExportまたは削除する
        try {
            Collection<SVNLogEntryPath> logEntryPaths = logEntry.getChangedPaths().values();

            System.out.println("starting export -------");
            for (SVNLogEntryPath entryPath : logEntryPaths) {
                String url = repoRootURL + entryPath.getPath();
                char type = entryPath.getType();
                String destPath = getDestPath(url, pjRoot, destRoot);

                System.out.println("changedPath=" + entryPath.getPath());
                System.out.println("remote url=" + url);
                System.out.println("local path=" + destPath);

                // フォルダーの場合、フォルダー作成のみ //TODO
                if (type == 'A') {
                    // 新規の場合はexport
                    export(url, revision, destPath, SVNDepth.EMPTY);
                    System.out.println("svn export " + url + "@" + revision + " " + destPath);
                } else if (type == 'M') {
                    // 更新の場合、更新対象となるファイルが存在しなければエラー
                    if (!Files.exists(Paths.get(destPath))) {
                        throw new RuntimeException("ファイルが更新されるですが、更新元のファイルが存在しません。"
                                + "Revesion番号に間違いはないかご確認ください。¥ndestPath=" + destPath);
                    }
                    // 
                    export(url, revision, destPath, SVNDepth.EMPTY);
                } else if (type == 'D') {
                    // 削除の場合はファイル削除
                    //Files.deleteIfExists(Paths.get(destPath));
                    // 無条件で削除ため、destpathをdouble-check
                    if (!destPath.startsWith(destRoot)) {
                        throw new RuntimeException("削除先のパスに異常があるため、処理が中止します。¥ndestPath=" + destPath);
                    }
                    FileUtils.forceDelete(Paths.get(destPath).toFile());
                    System.out.println("del " + destPath);
                }
            }
            System.out.println("ending export -------");

        } catch (Exception e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
            throw new RuntimeException("エラーが発生しました。管理者にご連絡ください。", e);
        }
    }

    /**
     * 指定したRevisionのファイルを指定したPath (root) にExport
     *
     * @param url RepositoryのURL
     * @param revision Export対象のリビジョン番号
     * @param destPath Export先のPath
     * @param svnDepth
     */
    public void export(String url, long revision, String destPath, SVNDepth svnDepth) {
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);//TODO

        try {
            updateClient.doExport(SVNURL.parseURIEncoded(url), new File(destPath),
                    SVNRevision.create(revision), SVNRevision.create(revision),
                    null, true, svnDepth);

        } catch (SVNException e) {
            throw new RuntimeException("exportが失敗しました。", e);
        }
    }

    // 
    private String getDestPath(String url, String pjRoot, String destRoot) {
        Path relativePath = Paths.get(pjRoot).getParent().relativize(Paths.get(URI.create(url).getPath()));
        Path destPath = Paths.get(destRoot).resolve(relativePath);

        return destPath.toString();
    }

    public String getPjRoot() {
        return pjRoot;
    }

    public void setPjRoot(String pjRoot) {
        this.pjRoot = pjRoot;
    }

    public String getDestRoot() {
        return destRoot;
    }

    public void setDestRoot(String destRoot) {
        this.destRoot = destRoot;
    }

    public SVNClientManager getOurClientManager() {
        return ourClientManager;
    }

    public void setOurClientManager(SVNClientManager ourClientManager) {
        this.ourClientManager = ourClientManager;
    }

    public String getRepoRootURL() {
        return repoRootURL;
    }

    public void setRepoRootURL(String repoRootURL) {
        this.repoRootURL = repoRootURL;
    }
}
