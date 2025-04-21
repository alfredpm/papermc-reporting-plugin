# PaperMC - Reporting

**Version**: Prototype

## Description
Simple reporting PaperMC/Bukkit plugin for Minecraft 1.20.4.

## Current features
 - Storage of player role and reports:
    - In memory only, no persistence
 - Interfaces:
    - Command line only
 - Actions:
    - All: 
        - Report players:    ```/report <playerName> <motive> <opt comment>```
    - Moderator:
        - Read reports:      ```/read <reportId>``` & ```/readall <opt pageNb>```
        - Close reports:     ```/solve <reportId> <status> <opt comment>```  

    *By default, connecting players not found in the role storage are set to Player on login. Use terminal to access Moderator commands.*

## To do
 - Implement persistent storage adapters (sqlite...)
 - Implement better input interface (inventory interface...)
 - Enrich report with game data to make the plugin actually usable and useful
    - Consult with business for relevant data
 - Make configurable rather than requiring change to PluginController source code