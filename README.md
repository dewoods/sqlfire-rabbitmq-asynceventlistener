sqlfire-rabbitmq-asynceventlistener
===================================

Asynchronously adds events in SQLFire to a RabbitMQ exchange

Installation
------------

Generate JAR using included Makefile:
    
    $ make

Add JAR to -classpath parameter when starting SQLFire locators and servers:

    $ sqlf [locator|server] start \
        . . . \
        -classpath=/path/to/jar/RabbitAsyncEventListener.jar \
        . . . \

###Dependencies

- Java > 1.6
- SQLFire and RabbitMQ installed
- sqlfire.jar available in $CLASSPATH
- SQLFire [Enterprise license](http://www.vmware.com/products/application-platform/vfabric-sqlfire/buy.html) with WAN capabilities
- [RabbitMQ Java Client](http://www.rabbitmq.com/java-client.html) library installed

Usage
-----

Add RabbitAsyncEventListener to SQLFire, passing RabbitMQ URI to INITPARAMS:

    CREATE ASYNCEVENTLISTENER RabbitAsyncEventListener (
        listenerclass 'sqlfire.callbacks.RabbitAsyncEventListener'
        INITPARAMS 'amqp://guest:guest@localhost:5672'
    ) SERVER GROUPS( SG1 );


Start RabbitAsyncEventListener:

    call SYS.START_ASYNC_EVENT_LISTENER( 'RabbitAsyncEventListener' );

Add RabbitAsyncEventListener to one or more tables:

    CREATE TABLE new_table(
        ... columns ...
    ) SERVER GROUPS(
        ...
    ) AsyncEventListener(
        RabbitAsyncEventListener
    );

Once installed, any rows added to the specified table will be added to RabbitMQ in the exchange *sqlfire* with the routing key *schema.table*.  RabbitMQ message bodies will contain the new row in CSV format.

Limitations
-----------

The following known limitations will be addressed in a future release:
- No support for updates/deletes
- Limited support for set operations
- Incomplete CSV support - no escaping, quoting, etc.
