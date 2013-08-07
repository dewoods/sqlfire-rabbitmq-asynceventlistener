CREATE ASYNCEVENTLISTENER RabbitAsyncEventListener (
    listenerclass 'sqlfire.callbacks.RabbitAsyncEventListener'
    INITPARAMS 'amqp://guest:guest@localhost:5672'
) SERVER GROUPS( SG1 );

call SYS.START_ASYNC_EVENT_LISTENER( 'RabbitAsyncEventListener' );

CREATE TABLE sqlf_rabbit_test(
    id int not null,
    kind varchar(64),
    color varchar(64)
) SERVER GROUPS(
    sg1
) AsyncEventListener( 
    RabbitAsyncEventListener
);
