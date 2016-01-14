# UniversalDatabaseService

This is a universal cross-platform database sychronization service. The service suports MySQL, MSSQL, DB2, Oracle. The main targer is to synchronize tables from a local database to remote. The programm works as a service.

YAJSW was used in this project to implement services. Check it [here](http://yajsw.sourceforge.net/).

Installation
-------

1. Download UniversalDatabaseService.zip
2. Unzip it to any directory. There will be three files (yajsw, sync.properties, UniversalDatabaseService.jar)
3. Go to Windows/Unix installation instruction

Windows installation
-------

1. Run UniversalDatabaseService.jar
2. Find the PID of the running UniversalDatabaseService process
3. Open Windows Command Prompt with admin privileges and navigate to \UniversalDatabaseService\yajsw\bat
4. Type `genConfig <pid>` where the PID is your UniversalDatabaseService process id. This will configure your \UniversalDatabaseService\yajsw\conf\wrapper.conf file
5. Now you can terminate UniversalDatabaseService process
6. See p.3
7. To install UniversalDatabaseService.jar as a Windows service type `installService`. 
8. To start the service type `startService`. The service will be automatically started after rebooting PC.
9. You can optionally stop it by typing `stopService` or unistall `uninstallService`







