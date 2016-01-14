# UniversalDatabaseService

This is a universal cross-platform database sychronization service. The service suports MySQL, MSSQL, DB2, Oracle. The main targer is to synchronize tables from a local database to remote. The programm works as a service.

YAJSW was used in this project to implement services. Check it [here](http://yajsw.sourceforge.net/).

Installation
-------

1. Download [UniversalDatabaseService.zip](UniversalDatabaseService.zip)
2. Unzip it to any directory. There will be three files (yajsw, sync.properties, UniversalDatabaseService.jar)
3. Go to Windows/Unix installation instruction

Windows installation
-------

1. Open Windows Command Prompt with **admin privileges** and navigate to \UniversalDatabaseService folder.
2. Type `java -jar universaldatabaseservice.jar` 
3. Find the PID(process id) of the running UniversalDatabaseService.jar process
4. Navigate to \UniversalDatabaseService\yajsw\bat
5. Type `genConfig <pid>` (<pid> from p.3). This will configure your \UniversalDatabaseService\yajsw\conf\wrapper.conf file
6. Now you can terminate UniversalDatabaseService.jar process
7. To install UniversalDatabaseService.jar as a Windows service type `installService`. 
8. To start the service type `startService`. The service will be also automatically started after rebooting PC.
9. You can optionally stop it by typing `stopService` or unistall `uninstallService`

Unix installation
-------

1. Open Terminal with **admin privileges** and navigate to /UniversalDatabaseService folder
2. Type `nohup java -jar UniversalDatabaseService.jar &` 
3. Find the PID of the running UniversalDatabaseService.jar process. To show all running processes type `ps aux | grep java`
4. Navigate to /UniversalDatabaseService/yajsw/bin
5. Apply the permission to all the files under a directory /bin/. Type `chmod -R 755 bin/`
6. Type `./bin/genConfig.sh <pid>` (<pid> from p.3). This will configure your /UniversalDatabaseService/yajsw/conf/wrapper.conf file
7. Now you can terminate UniversalDatabaseService.jar. Type `kill -9 <pid>` (<pid> from p.3)
8. To install UniversalDatabaseService.jar as UNIX Daemon type `./bin/installDaemon.sh`
9. To see installed daemon in daemons list type `ls /etc/init.d`. Find "yajsw" in the output.
10. To start the service type `./bin/startDaemon.sh`. The service will be also automatically started after rebooting PC.
11. You can optionally stop it by typing `./bin/stopDaemon` or unistall `./bin/uninstallDaemon`
