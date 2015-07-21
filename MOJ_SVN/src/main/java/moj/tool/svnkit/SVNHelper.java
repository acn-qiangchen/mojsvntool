package moj.tool.svnkit;

import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SVNHelper {

    // SVNRepositoryを表すObject
    private SVNRepository repository = null;
    // 認証を管理するManager
    private ISVNAuthenticationManager authManager = null;
    // 各ClientManagerを管理するManager
    private SVNClientManager ourClientManager = null;
    // 初期化済みフラグ
    private boolean initialized = false;
    // リモート接続先のURL
    private String url = null;
    // ユーザー
    private String userName = null;
    // パスワード
    private String password = null;
    // ログ出力の対象ディレクトリ
    private String[] includedDirectories = new String[]{};;

    public SVNHelper(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        //eager init
        init();
    }

    // 初期化する
    private void init() {

        if (initialized) {
            return;
        }

        // Library初期化
        setupLibrary();

        try {
            /*
             * Creates an instance of SVNRepository to work with the repository.
             * All user's requests to the repository are relative to the
             * repository location used to create this SVNRepository.
             * SVNURL is a wrapper for URL strings that refer to repository locations.
             */
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        } catch (SVNException svne) {
            throw new RuntimeException("SVNKitのライブラリ初期化が失敗しました。url=" + url
                    + ", userName=" + userName + ", password=" + password, svne);
        }

        // 認証を通す
        authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
        repository.setAuthenticationManager(authManager);

        // ClientManagerを作成
        ourClientManager = SVNClientManager.newInstance();
        ourClientManager.setAuthenticationManager(authManager);
    }

    /**
     * 指定したRevisionに変化があるファイルをExportする (差分)
     * @param pjRoot
     * @param revision
     * @param destPath 
     */
    public void exportRevChangedFiles(String pjRoot, long revision, String destPath){
        this.exportRevChangedFiles(pjRoot, revision, revision, destPath);
    }
    
    /**
     * 指定したRevision範囲に変化があるファイルをExportする（差分）
     *
     * @param pjRoot export操作対象PJのROOTパス
     * @param startRevision 開始リビジョン
     * @param endRevision 終了リビジョン
     * @param destPath export先のパス
     * @return
     */
    public void exportRevChangedFiles(String pjRoot, long startRevision,
            long endRevision, String destPath) {

        // 初期化する
        init();
        
        // RepositoryよりLogEntryのCollectionを取得し、Loopする
        // LogEntryからファイルのPathとRevisionをListに格納し、返却する
        try {
            MojSvnLogEntryHandler handler = new MojSvnLogEntryHandler();
            handler.setRepoRootURL(repository.getRepositoryRoot(true).toDecodedString());
            handler.setPjRoot(pjRoot);
            handler.setDestRoot(destPath);
            handler.setOurClientManager(ourClientManager);

            System.out.println("repository location .. " + repository.getLocation().toDecodedString());
            System.out.println("repository root .. " + repository.getRepositoryRoot(true));
            System.out.println("traversing log entries from " + startRevision + " to " + endRevision);

            // 対象RevisionのログをWalkthrough
            // 各ログのEntryに対し、MojSvnLogEntryHandlerが呼び出されてよりする
            repository.log(this.includedDirectories,
                    startRevision, endRevision, true, true, handler);

        } catch (SVNException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "getChangedPathsOfRevisionに実行は失敗しました。 startRevision="
                    + startRevision + ", endRevision=" + endRevision
                    + "url=" + url, e);
        }
    }

    /**
     * BaseとなるファイルをExportする<br>
     * svn export url@revision destPathと同じ操作
     * @param fromURL
     * @param revision
     * @param destPath 
     */
    public void exportBase(String fromURL, long revision, String destPath) {
        init();
        
        try {
            SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
            
            System.out.println("svn export " + fromURL + "@" + revision + " " + destPath);  
            updateClient.doExport(SVNURL.parseURIEncoded(fromURL), Paths.get(destPath).toFile(),
                    SVNRevision.HEAD, SVNRevision.create(revision),
                    null, true, SVNDepth.INFINITY);
        } catch (SVNException ex) {
            //Logger.getLogger(SVNHelper.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            throw new RuntimeException("svn export処理が失敗しました。"
                    + "¥nfromURL=" + fromURL 
                    + "¥nrevision=" + revision
                    + "¥ndestPath=" + destPath, ex);
        }
    }
    /*
     * Initializes the library to work with a repository via 
     * different protocols.
     */
    private void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }
}
