/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.collector.cross.platform.app;

/**
 *
 * @author JIMMY
 */
public class GlobalVariables {
    private String DB_SERVERNAME;
    private String DB_NAME;
    private String DB_UID;
    private String DB_PWD;
    private String SQLConStr;
    private String S_SanghanHahan;

    public void setDB_SERVERNAME(String DB_SERVERNAME) {
        this.DB_SERVERNAME = DB_SERVERNAME;
    }

    public void setDB_NAME(String DB_NAME) {
        this.DB_NAME = DB_NAME;
    }

    public void setDB_UID(String DB_UID) {
        this.DB_UID = DB_UID;
    }

    public void setDB_PWD(String DB_PWD) {
        this.DB_PWD = DB_PWD;
    }

    public void setSQLConStr(String SQLConStr) {
        this.SQLConStr = SQLConStr;
    }

    public void setS_SanghanHahan(String S_SanghanHahan) {
        this.S_SanghanHahan = S_SanghanHahan;
    }

    public String getDB_SERVERNAME() {
        return DB_SERVERNAME;
    }

    public String getDB_NAME() {
        return DB_NAME;
    }

    public String getDB_UID() {
        return DB_UID;
    }

    public String getDB_PWD() {
        return DB_PWD;
    }

    public String getSQLConStr() {
        return SQLConStr;
    }

    public String getS_SanghanHahan() {
        return S_SanghanHahan;
    }
    
    
}
