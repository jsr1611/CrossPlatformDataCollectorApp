/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.collector.cross.platform.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JIMMY
 */
public class DataCollectorCrossPlatformApp {

    private static long high_value, low_value, particle;
    private static GlobalVariables g = new GlobalVariables();
    private static Connection myCon = null;
    private static Statement myStatement = null;
    private static ResultSet myResultSet = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        ModbusClient client = new ModbusClient();
        client.setConnectionTimeout(5000);
        SpcData spcData = null;
        String ip = "192.168.12.143";
        int port = 15004;

        g.setDB_SERVERNAME("192.168.11.209");
        g.setDB_NAME("SENSORDATA");
        g.setDB_UID("dlitdb");
        g.setDB_PWD("dlitdb");
        g.setS_DeviceTable(g.getDB_NAME().charAt(0) + "_DEVICES");
        g.setDB_DATATABLE(g.getDB_NAME().charAt(0) + "_DATATABLE");
        g.setS_SanghanHahan((g.getDB_NAME().charAt(0) + "_SanghanHahan"));
        String sqlConStr = String.format("jdbc:sqlserver://%s;user=%s;password=%s", g.getDB_SERVERNAME(), g.getDB_UID(), g.getDB_PWD());
        g.setSQLConStr(sqlConStr);
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            myCon = DriverManager.getConnection(g.getSQLConStr());
        } catch (SQLException ex) {
            Logger.getLogger(DataCollectorCrossPlatformApp.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage() + Arrays.toString(ex.getStackTrace()));
        }
        g.setS_DTColumns(GetTableColumnNames(g.getDB_DATATABLE()));
        ArrayList<Integer> ids = GetColumnDataAsListOfInt(g.getS_DeviceTable(), g.getS_DTColumns(2));
        ids.remove(0);
        g.setS_IDs(ids);

        // "10.1.55.195",  15005, 
        g.setIP_LIST(new ArrayList<>(Arrays.asList("192.168.12.141", "192.168.12.142", "192.168.12.143")));
        g.setPORT_LIST(new ArrayList<>(Arrays.asList(15002, 15006, 15007)));

        int counter = 0;
        boolean dbInsert_OK;
        int sensorId;
        LocalDateTime timenow = LocalDateTime.now();
        LocalDateTime timenow_eachSensor = LocalDateTime.now();
        //timeNows.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        while (true) {
            try {
                timenow = LocalDateTime.now();
                for (int c = 0; c < g.getS_IDs().size(); c++) {
                    timenow_eachSensor = LocalDateTime.now();
                    sensorId = g.getS_IDs(c);
                    client.setUnitIdentifier((byte) sensorId);
                    spcData = new SpcData();
                    dbInsert_OK = false;
                    counter += 1;
                    boolean connected = connect(client, g.getIP_LIST(c), g.getPORT_LIST(c));

                    int[] d = null;
                    if (connected) {
                        //System.out.format("Counter: %d, Client connected: %s. ", counter, client.getipAddress());                    
                        d = collect(client);
                        while (d == null || d.length < 40 || d[22] == 0) {
                            d = collect(client);
                        }
                        if (d != null && d.length > 39 && d[22] != 0) {
                            spcData = prepareDataForStoring(d, spcData);

                            dbInsert_OK = InsertDataIntoDB(spcData);

                            System.out.println("" + spcData.toString() + "dbInsert_OK: " + dbInsert_OK + ". Time elapsed: " +Duration.between(timenow_eachSensor, LocalDateTime.now()).getSeconds() + " seconds. ");

                        } else {
                            if (d == null) {
                                System.out.println("No data. d = null. sensorId: " + sensorId + ", PC Time: " + spcData.getPc_time() + ", Time elapsed: " + Duration.between(timenow, LocalDateTime.now()).getSeconds() + " seconds. ");
                            } else {
                                System.out.print(" sensorId: " + sensorId + ", PC Time: " + spcData.getPc_time() + ", ");
                                for (int i = 0; i < d.length; i++) {
                                    System.out.print("" + d[i] + " ");
                                }
                                System.out.println(", Time elapsed: " + Duration.between(timenow, LocalDateTime.now()).getSeconds() + " seconds. ");
                            }
                        }

                        //System.out.print(" Client disconnected. ");
                        client.Disconnect();
                    } else {
                        System.out.println(" ????????? ????????????. sensorId: " + g.getS_IDs(c) + ", PC Time: " + spcData.getPc_time() + " , Time elapsed: " + Duration.between(timenow, LocalDateTime.now()).getSeconds() + " seconds. ");
                    }

                }
                System.out.println("---------------------//------------------------\nTime elapsed: " +Duration.between(timenow, LocalDateTime.now()).getSeconds() + " seconds. ");
                Thread.sleep(1000);
                while (LocalDateTime.now().getSecond() != 0 || Duration.between(timenow, LocalDateTime.now()).getSeconds() < 59) {
                    Thread.sleep(100);
                }

            } catch (IOException e) {
                System.out.println("Error disconnecting the client: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                spcData = null;
            }

        }
    }

    private static SpcData prepareDataForStoring(int[] d, SpcData spcData) {
        spcData.setPc_time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        //System.out.print("Size of data received: " + d.length + ". ");
        String someTempoString = "";
        for (int index = 0; index < 12;) {
            someTempoString += (Character.toString((char) d[index]) + Character.toString((char) d[index + 1]));
            index += 2;
            if (index < 12) {
                someTempoString += "-";
            }
        }
        spcData.setID_long(someTempoString);

        someTempoString = "20" + String.format("%02d", d[12]) + "-" + String.format("%02d", d[13]) + "-" + String.format("%02d", d[14]) + " " + String.format("%02d", d[15]) + ":" + String.format("%02d", d[16]) + ":" + String.format("%02d", d[17]) + ".000";
        spcData.setTemperature(String.format("%02d", d[18]) + "." + String.format("%02d", d[19]));
        spcData.setHumidity(String.format("%02d", d[20]) + "." + String.format("%02d", d[21]));
        spcData.setParticle03(extractParticleData(d, 24));
        spcData.setParticle05(extractParticleData(d, 26));
        spcData.setParticle10(extractParticleData(d, 28));
        spcData.setParticle50(extractParticleData(d, 32));
        spcData.setParticle100(extractParticleData(d, 34));
        spcData.setParticle250(extractParticleData(d, 36));

        spcData = SetData(spcData, "particle");

        spcData.setTime(someTempoString);

        return spcData;
    }

    /**
     * Return column data as a list of int values from an SQL table
     *
     * @param tableName - table name
     * @param columnName - column name
     * @return - a list of int values
     */
    private static ArrayList<Integer> GetColumnDataAsListOfInt(String tableName, String columnName) {
        ArrayList<Integer> result = null;
        System.out.println("Getting column from table: " + tableName + " " + columnName);
        try {
            result = new ArrayList<>();
            if (myCon.isClosed()) {
                myCon = DriverManager.getConnection(g.getSQLConStr());
            }
            myStatement = myCon.createStatement();
            String sqlSelect = String.format("SELECT %s FROM [%s].[dbo].[%s]", columnName, g.getDB_NAME(), tableName);
            ResultSet rs = myStatement.executeQuery(sqlSelect);

            while (rs.next()) {
                result.add(rs.getInt(1));
                System.out.println(" " + rs.getRow() + ": " + rs.getInt(1));
            }

        } catch (Exception e) {
        }

        return result;
    }

    /**
     * Return column data as a list of string values from an SQL table
     *
     * @param tableName - table name
     * @param columnName - column name
     * @return - a list of string values
     */
    private static ArrayList<String> GetColumnDataAsListOfString(String tableName, String columnName) {
        ArrayList<String> result = new ArrayList<>();

        return result;
    }

    /**
     * Return true if connected to the modbus client.
     *
     * @param client
     * @return
     */
    private static boolean connect(ModbusClient client, String ip, Integer port) {
        boolean result = false;
        try {
            if (!client.isConnected()) {

                client.Connect(ip, port);
                result = true;
            }

        } catch (IOException e) {
            System.out.println("Error " + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }

        return result;
    }

    /**
     * Return data read from input registers of the Modbus client
     *
     * @param client
     * @return
     */
    private static int[] collect(ModbusClient client) {
        int[] d = null;
        try {
            if (!client.isConnected()) {
                client.Connect();
            }
            d = client.ReadInputRegisters(0, 40);
        } catch (ModbusException | IOException e) {
            System.out.println("Error while collecting data " + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return d;
    }

    /**
     * Return particle data extracted from int16 array received from sensor
     * device.
     *
     * @param d - is an array of int16 data
     * @param startIndex - is starting index
     * @return particle data in the form of a String
     */
    private static String extractParticleData(int[] d, int startIndex) {
        high_value = 0;
        low_value = 0;
        particle = 0;

        high_value = d[startIndex] >= 0 ? d[startIndex] : 65536 + d[startIndex];
        low_value = d[startIndex + 1] >= 0 ? d[startIndex + 1] : 65536 + d[startIndex + 1];
//        if ((high_value == -1 && low_value == -1) || (high_value == 65535 && low_value == 65535)) {
//            particle = -1;
//        } else {
//            particle = high_value * 65536 + low_value;
//        }
        particle = (high_value == 65535 && low_value == 65535) ? -1 : (high_value * 65536 + low_value);
        return String.valueOf(particle);
    }

    /**
     * ????????? ?????? ?????? ?????? ??? spcData object??? ??????.
     *
     * @param spcData - sensor data object
     * @param sensorCategory - "particle" ?????? "pressure"
     * @return SpcData object ??????
     */
    private static SpcData SetData(SpcData spcData, String sensorCategory) {
        // String[] tbColumns = GetTableColumnNames();

        String sensorId_suffix = spcData.getID_long().substring(spcData.getID_long().length() - 5);
        int sensorId;
        switch (sensorId_suffix) {
            case "BE-CA":
                sensorId = 1;
                break;
            case "BE-C7":
                sensorId = 2;
                break;
            case "BE-CB":
                sensorId = 3;
                break;
            case "BE-C4":
                sensorId = 4;
                break;
            default:
                sensorId = 0;
        }
        spcData.setID(sensorId);

        ArrayList<String> tbColumnNames = GetTableColumnNames(g.getS_SanghanHahan());
        ResultSet resultSet = null;
        Statement statement = null;
        String sqlSelect = "";

        try {
            sqlSelect = String.format("SELECT %s, %s "
                    + "FROM [%s].[dbo].[%s] "
                    + "WHERE %s = '%s' AND %s = %d",
                    tbColumnNames.get(2), tbColumnNames.get(7),
                    g.getDB_NAME(), g.getS_SanghanHahan(),
                    tbColumnNames.get(0), sensorCategory, tbColumnNames.get(1), sensorId);
            statement = myCon.createStatement();
            resultSet = statement.executeQuery(sqlSelect);

            while (resultSet.next()) {
                switch (resultSet.getString(1)) {
                    case "temperature":
                        spcData.setTemperature_on(resultSet.getString(2).equals("Yes"));
                        break;
                    case "humidity":
                        spcData.setHumidity_on(resultSet.getString(2).equals("Yes"));
                        break;
                    case "particle03":
                        spcData.setParticle03_on(resultSet.getString(2).equals("Yes"));
                        break;
                    case "particle05":
                        spcData.setParticle05_on(resultSet.getString(2).equals("Yes"));
                        break;
                    case "particle10":
                        spcData.setParticle10_on(resultSet.getString(2).equals("Yes"));
                        break;
                    case "particle50":
                        spcData.setParticle50_on(resultSet.getString(2).equals("Yes"));
                        break;
                    case "particle100":
                        spcData.setParticle100_on(resultSet.getString(2).equals("Yes"));
                        break;
                    case "particle250":
                        spcData.setParticle250_on(resultSet.getString(2).equals("Yes"));
                        break;
                    default:
                        break;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataCollectorCrossPlatformApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return spcData;
    }

    /**
     * Store sensor data into DB
     *
     * @param data - sensor data
     * @return - "true" if data is stored successfully, else "false"
     */
    private static boolean InsertDataIntoDB(SpcData data) {
        boolean result = false;
        String sqlInsert = "";
        try {
            if (myCon.isClosed()) {
                myCon = DriverManager.getConnection(g.getSQLConStr());
            }

            // -------------?????? ?????? ---------------->
            if (data.isTemperature_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'temperature'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getTemperature());
            }

            //---------?????? ??????-------------------->
            if (data.isHumidity_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'humidity'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getHumidity());
            }
            //---------????????? 0.3um ??????-------------------->
            if (data.isParticle03_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'particle03'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getParticle03());
            }

            //---------????????? 0.5um ??????-------------------->
            if (data.isParticle05_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'particle05'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getParticle05());
            }

            //---------????????? 1.0um ??????-------------------->
            if (data.isParticle10_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'particle10'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getParticle10());
            }

            //---------????????? 5.0um ??????-------------------->
            if (data.isParticle50_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'particle50'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getParticle50());
            }

            //---------????????? 10.0um ??????-------------------->
            if (data.isParticle100_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'particle100'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getParticle100());
            }

            //---------????????? 25.0um ??????-------------------->
            if (data.isParticle250_on()) {
                sqlInsert += String.format("INSERT INTO [%s].dbo.[%s] VALUES"
                        + "('%s', '%s', %d, 'particle250'" // sensorCode
                        + ", '%s', '');", g.getDB_NAME(), g.getDB_DATATABLE(),
                        data.getPc_time(), data.getTime(), data.getID(), data.getParticle250());
            }

            //---------DB Insert cmd ?????? ??????-------------------->
            PreparedStatement ppdStatement = myCon.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            if (!DataAlreadyExists(data.getID(), data.getTime())) {
                ppdStatement.execute();
                result = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataCollectorCrossPlatformApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    /**
     * Get all the "column names" in the given table
     *
     * @param tableName - given table name
     * @return ArrayList of column names
     */
    private static ArrayList<String> GetTableColumnNames(String tableName) {

        ArrayList<String> columnNames = new ArrayList<String>();
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            if (myCon.isClosed()) {
                myCon = DriverManager.getConnection(g.getSQLConStr());
            }
            statement = myCon.createStatement();
            resultSet = statement.executeQuery(String.format("SELECT DISTINCT * FROM %s.dbo.%s;", g.getDB_NAME(), tableName));
            ResultSetMetaData resultSetMdata = resultSet.getMetaData();

            int index = 0;
            while (resultSetMdata.getColumnCount() > index) {
                index += 1;
                columnNames.add(resultSetMdata.getColumnName(index));
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataCollectorCrossPlatformApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return columnNames;
    }

    /**
     * Data??? ??????ID ??? ???????????? ???????????? ???????????? ????????? ??????????????? ??????
     *
     * @param sensorId - ??????ID
     * @param timestamp - ?????? (?????? ????????? ??????) ??????
     * @return boolean true (?????? ?????????) or false (?????? ?????????)
     */
    private static boolean DataAlreadyExists(Integer sensorId, String timestamp) {
        boolean result = false;
        try {
            String sqlSelect = String.format("SELECT TOP 1 1 FROM [%s].[dbo].[%s] "
                    + "WHERE %s = %d AND %s LIKE '%s%%';",
                    g.getDB_NAME(), g.getDB_DATATABLE(),
                    g.getS_DTColumns(2), sensorId, g.getS_DTColumns(1), timestamp.substring(0, timestamp.length() - 7));
            if (myCon.isClosed()) {
                myCon = DriverManager.getConnection(g.getSQLConStr());
            }
            myStatement = myCon.createStatement();
            myResultSet = myStatement.executeQuery(sqlSelect);
            if (myResultSet.next()) {
                System.out.print(" ?????? ????????? ");
                result = true;
            } else {
                System.out.print(" ?????? ????????? ");
            }
            myResultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}
