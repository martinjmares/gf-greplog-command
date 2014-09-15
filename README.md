gf-greplog-command
==================

Grep-log admin command for GlassFish application server. Filter logs based on provided parameters.

*This is example command implementation for JavaOne 2014*

Usage
-----

`asadmin grep-log [options]`

### Options

+ **--target**: Where logs should be filtered
+ **--limit**: Maximum number of returned log messages per one log file. If there is more then limit filtered messages then last limit number will be returned.
+ **--min-level**: Minimal logging level. It can be number or Logger Level name.
+ **--package**: Filter of the Logger. It is usually fully qualified class name. If defined then only log messages which Logger name starts with this value will be returned.
+ **--fixed-strings={true|false}**: If true then argument will be considered as substring and not as regexp.
+ **primary parameter**: Regular expression to filter logged message. If --fixed-strings is true then this is not regexp but just substring.

Build and install
-----------------

**Build:** Use maven and Java 7
       `mvn clean install`

**Deployment:** Copy created jar file to _module_ directory in your GlassFish 4.1 installation