/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.collector.cross.platform.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JIMMY
 */
public class DataCollectorCrossPlatformApp {

    private static long high_value, low_value, particle;
    private static GlobalVariables g = new GlobalVariables();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        ModbusClient client = new ModbusClient();
        SpcData spcData = null;
        SpcData oldData = new SpcData();
        String ip = "192.168.12.140";
        int port = 15002;
        client.setUnitIdentifier((byte) 1);
        g.setDB_SERVERNAME("localhost");
        g.setDB_NAME("SENSORDATA");
        g.setDB_UID("dlitdb");
        g.setDB_PWD("dlitdb");
        g.setS_SanghanHahan((g.getDB_NAME().charAt(0) + "_SanghanHahan"));
        String sqlConStr = String.format("jdbc:sqlserver://%s;user=%s;password=%s", g.getDB_SERVERNAME(), g.getDB_UID(), g.getDB_PWD());
        g.setSQLConStr(sqlConStr);
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        } catch (SQLException ex) {
            Logger.getLogger(DataCollectorCrossPlatformApp.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage() + Arrays.toString(ex.getStackTrace()));
        }

        //String dbURL = "jdbc:sqlserver://localhost\\sqlexpress;user=sa;password=secret";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(g.getSQLConStr());
        } catch (SQLException ex) {
            Logger.getLogger(DataCollectorCrossPlatformApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (conn != null) {
            System.out.println("Connected");
        }
        else
        {
            System.out.println("Not connected");
        }

        
        
        
        
        
        
        
        
        
        
        
        
        int counter = 0;
        int second_counter = 0;

        while (counter < Integer.MAX_VALUE) {
            spcData = new SpcData();
            counter += 1;
            boolean connected = connect(client, ip, port);
            try {
                int[] d = null;
                if (connected) {
                    //System.out.format("Counter: %d, Client connected: %s. ", counter, client.getipAddress());                    
                    d = collect(client);
                    if (d != null && d.length > 39 && d[22] != 0) {
                        //System.out.print("Size of data received: " + d.length + ". ");
                        String someTempoString = "";
                        for (int index = 0; index < 12;) {
                            //System.out.println("data: " + d[index]);
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
                        //spcData = SetData(spcData, "particle");
                        spcData.setTime(someTempoString);
                        if (oldData.getTime() == null) {
                            System.out.print("It is Initial Data!!! ");
                            System.out.println(spcData.toString());
                            second_counter += 1;
                            oldData = spcData;
                        } else if (oldData.getTime().equals(spcData.getTime())) {
                            //System.out.print(" It is Old Data!! ");
                            System.out.print(".");
                            second_counter += 1;
                        } else {
                            System.out.println("Seconds past since last update: " + second_counter + "s. \nIt is New Data!!! ");
                            System.out.println(spcData.toString());
                            oldData = spcData;
                            second_counter = 0;
                        }

                    }

                    client.Disconnect();
                    //System.out.print(" Client disconnected. ");
                }

                Thread.sleep(1000);

            } catch (IOException | InterruptedException e) {
                System.out.println("Error disconnecting the client: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            }
        }

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

        high_value = d[startIndex];
        low_value = d[startIndex + 1];
        if ((high_value == -1 && low_value == -1) || (high_value == 65535 && low_value == 65535)) {
            particle = -1;
        } else {
            particle = high_value * 65536 + low_value;
        }
        return String.valueOf(particle);
    }

    private static SpcData SetData(SpcData spcData, String sensorCategory) {
        String[] tbColumns = GetTableColumnNames();

        /**
         *
         * List<string> tbColumns = GetTableColumnNames(g.s_SanghanHahan);
         * //Console.WriteLine("\ntbColumn len = " + tbColumns.Count); string
         * sql_select_dataType = $"SELECT DISTINCT {tbColumns[2]} FROM
         * [{g.dbName}].[dbo].[{g.s_SanghanHahan}] WHERE {tbColumns[0]} =
         * 'particle'"; List<string> sensorTypes = GetColumnDataAsList("string",
         * sql_select_dataType, tbColumns[2]);
         *
         * //Data data = new Data(); if (tbColumns.Count == 0) {
         * Console.WriteLine($"{g.s_SanghanHahan} table 존재하지 않거나 다른 에러가
         * 발생했습니다."); return data; } string sql_select = $"SELECT
         * {tbColumns[2]}, {tbColumns[7]} FROM
         * [{g.dbName}].[dbo].[{g.s_SanghanHahan}] WHERE {tbColumns[0]} =
         * '{sensorCategory}' AND {tbColumns[1]} = {sensorId}";
         *
         * using (SqlConnection con = new SqlConnection(g.sqlConStr)) {
         * con.Open(); using (SqlCommand cmd = new SqlCommand(sql_select, con))
         * { using (SqlDataReader r = cmd.ExecuteReader()) { while (r.Read()) {
         * if (r[tbColumns[2]].Equals("temperature"))
         * data.SetTemperature(r.GetString(1).Equals("Yes")); else if
         * (r[tbColumns[2]].Equals("humidity"))
         * data.SetHumidity(r.GetString(1).Equals("Yes")); else if
         * (r[tbColumns[2]].Equals("particle03"))
         * data.SetParticle03(r.GetString(1).Equals("Yes")); else if
         * (r[tbColumns[2]].Equals("particle05"))
         * data.SetParticle05(r.GetString(1).Equals("Yes")); else if
         * (r[tbColumns[2]].Equals("particle10"))
         * data.SetParticle10(r.GetString(1).Equals("Yes")); else if
         * (r[tbColumns[2]].Equals("particle50"))
         * data.SetParticle50(r.GetString(1).Equals("Yes")); else if
         * (r[tbColumns[2]].Equals("particle100"))
         * data.SetParticle100(r.GetString(1).Equals("Yes")); else if
         * (r[tbColumns[2]].Equals("particle250"))
         * data.SetParticle250(r.GetString(1).Equals("Yes"));
         *
         * }
         * }
         * }
         * }
         *
         *
         *
         *
         * return data;
         */
        return spcData;
    }

    private static String[] GetTableColumnNames() {
        String[] someStr = null;
        
        return someStr;
    }

}
