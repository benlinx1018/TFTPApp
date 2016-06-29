1. 簡介
本文件主要在敘述【行動 APP─軟體更新應用】應用軟體之軟體需求、規格、
目的與範圍，進而使DVW32E能在離線狀態下利用手機ＡＰＰ透過SNMP功能對Ｍodem控制跟命令,ＡＰＰ需內建Tftp 供Modem將軟體上傳跟下載功能,完成此專案。
1.1 規格目的
本軟體需求規格在說明【行動 APP─軟體更新應用】之軟體建構項目的需
求，使開發人員可以透過此規格需求書了解系統目標、架構、系統各項功能
及相關人員之權責，以便於往後系統設計、系統維護等作業之進行，並作為
日後軟體設計與軟體測試之依據。
1.2 規格範圍
【行動 APP─軟體更新應用】分為兩大項：
(1) 需內建TFTP Server 
(2) 需可以透過Java軟體對Modem下達SNMP 指令
本軟體需求手冊規格涵蓋範圍為【行動 APP─軟體更新應用】應用程式，
Android 應用程式【行動 APP─軟體更新應用】中軟體建構項目之各功能建構項目
的架構、功能需求、作業程式需求及維護需求等，並提供給此系統的設計者、
開發者、測試者，在進行系統開發期間的依據，及日後此套系統的維護人員
參考依據。
1.3 軟體規格使用套件
(1) exception, file, file, io, ioexception, ioexception, net, network, printstream, runtimeexception, server, tftp, tftp, tftpdatapacket, tftptransfer, unexpected, unexpected, util
org.apache.commons.net.io.FromNetASCIIOutputStream;
org.apache.commons.net.io.ToNetASCIIInputStream;
java.io.BufferedInputStream;
java.io.BufferedOutputStream;
java.io.File;
java.io.FileInputStream;
java.io.FileNotFoundException;
java.io.FileOutputStream;
java.io.IOException;
java.io.InputStream;
java.io.OutputStream;
java.io.PrintStream;
java.net.SocketTimeoutException;
java.util.HashSet;
java.util.Iterator;
(2) SNMP4J-AGENT
 
SET OID_BCM_cdPvtMibEnableKeyValue OCTETSTRING !@#$*&^
Example : 使用的OID 1.3.6.1.4.1.4413.2.99.1.1.1.2.1.2.1 OCTETSTRING  “!@#$*&^” 

SET OID_v2FwControlImageNumber INTERGER 2
Example : 使用的OID 1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.1.0 INTERGER “2”

SET OID_v2FwDloadTftpServer IPADDRESS 192.168.100.3
Example : 使用的OID 1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.2.0 IPADDRESS “192.168.100.3”

SET OID_v2FwDloadTftpPath OCTETSTRING UBC1302-U10C111-VCM-B0-4MB-EUTDC-PC15.cpr
Example : 使用的OID 1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.3.0 OCTETSTRING “UBC1302-U10C111-VCM-B0-4MB-EUTDC-PC15.cpr” 檔案須為動態可透過上傳下載後選擇所需檔案

SET OID_v2FwDloadNow INTERGER 1
Example : 使用的OID 1.3.6.1.4.1.4413.2.99.1.1.2.4.2.2.2.6.0 INTERGER “1” 
