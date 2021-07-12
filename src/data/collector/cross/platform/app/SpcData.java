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
public class SpcData {

    private Integer sID;
    private String s_id_long;
    private String pc_time;
    private String sTime;
    private String sTemperature;
    private String sHumidity;
    private String sParticle03;
    private String sParticle05;
    private String sParticle10;
    private String sParticle50;
    private String sParticle100;
    private String sParticle250;
    
    private boolean temperature_on;
    private boolean humidity_on;
    private boolean sParticle03_on;
    private boolean sParticle05_on;
    private boolean sParticle10_on;
    private boolean sParticle50_on;
    private boolean sParticle100_on;
    private boolean sParticle250_on;
    

    public SpcData() {
    }

    public SpcData(Integer s_id, String s_id_hex, String pc_time, String s_time, String s_temperature, String s_humidity, String s_p03, String s_p05, String s_p10, String s_p50, String s_p100, String s_p250) {
        this.sID = s_id;
        this.s_id_long = s_id_hex;
        this.pc_time = pc_time;
        this.sTime = s_time;
        this.sTemperature = s_temperature;
        this.sHumidity = s_humidity;
        this.sParticle03 = s_p03;
        this.sParticle05 = s_p05;
        this.sParticle10 = s_p10;
        this.sParticle50 = s_p50;
        this.sParticle100 = s_p100;
        this.sParticle250 = s_p250;
    }

    public Integer get_id() {
        return sID;
    }

    public String getID_long() {
        return s_id_long;
    }

    public String getPc_time() {
        return pc_time;
    }

    public String getTime() {
        return sTime;
    }

    public String getTemperature() {
        return sTemperature;
    }

    public String getHumidity() {
        return sHumidity;
    }

    public String getParticle03() {
        return sParticle03;
    }

    public String getParticle05() {
        return sParticle05;
    }

    public String getParticle10() {
        return sParticle10;
    }

    public String getParticle50() {
        return sParticle50;
    }

    public String getParticle100() {
        return sParticle100;
    }

    public String getParticle250() {
        return sParticle250;
    }

    public void setID(Integer s_id) {
        this.sID = s_id;
    }

    public void setID_long(String s_id_hex) {
        this.s_id_long = s_id_hex;
    }

    public void setPc_time(String pc_time) {
        this.pc_time = pc_time;
    }

    public void setTime(String s_time) {
        this.sTime = s_time;
    }

    public void setTemperature(String s_temperature) {
        this.sTemperature = s_temperature;
    }

    public void setHumidity(String s_humidity) {
        this.sHumidity = s_humidity;
    }

    public void setParticle03(String s_p03) {
        this.sParticle03 = s_p03;
    }

    public void setParticle05(String s_p05) {
        this.sParticle05 = s_p05;
    }

    public void setParticle10(String s_p10) {
        this.sParticle10 = s_p10;
    }

    public void setParticle50(String s_p50) {
        this.sParticle50 = s_p50;
    }

    public void setParticle100(String s_p100) {
        this.sParticle100 = s_p100;
    }

    public void setParticle250(String s_p250) {
        this.sParticle250 = s_p250;
    }

    public void setTemperature_on(boolean s_temperature_on) {
        this.temperature_on = s_temperature_on;
    }

    public void setHumidity_on(boolean s_humidity_on) {
        this.humidity_on = s_humidity_on;
    }

    public void setParticle03_on(boolean s_p03_on) {
        this.sParticle03_on = s_p03_on;
    }

    public void setParticle05_on(boolean s_p05_on) {
        this.sParticle05_on = s_p05_on;
    }

    public void setParticle10_on(boolean s_p10_on) {
        this.sParticle10_on = s_p10_on;
    }

    public void setParticle50_on(boolean s_p50_on) {
        this.sParticle50_on = s_p50_on;
    }

    public void setParticle100_on(boolean s_p100_on) {
        this.sParticle100_on = s_p100_on;
    }

    public void setParticle250_on(boolean s_p250_on) {
        this.sParticle250_on = s_p250_on;
    }

    public boolean isTemperature_on() {
        return temperature_on;
    }

    public boolean isHumidity_on() {
        return humidity_on;
    }

    public boolean isParticle03_on() {
        return sParticle03_on;
    }

    public boolean isParticle05_on() {
        return sParticle05_on;
    }

    public boolean isParticle10_on() {
        return sParticle10_on;
    }

    public boolean isParticle50_on() {
        return sParticle50_on;
    }

    public boolean isParticle100_on() {
        return sParticle100_on;
    }

    public boolean isParticle250_on() {
        return sParticle250_on;
    }

    @Override
    public String toString() {
        return "sID: " + sID +  ", sTime: " + sTime + ", s_id_hex: " + s_id_long + ", 온도: " + sTemperature + ", 습도: "+ sHumidity+", p0.3: " + sParticle03 + ", p0.5: " + sParticle05 + ", p1.0: " + sParticle10 + ", p5.0: " + sParticle50 + ", p10.0: " + sParticle100 + ", p25.0: " + sParticle250 + " "; //To change body of generated methods, choose Tools | Templates.
    }

}
