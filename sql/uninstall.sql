call SYS.STOP_ASYNC_EVENT_LISTENER( 'RabbitAsyncEventListener' );

DROP TABLE sqlf_rabbit_test;

DROP ASYNCEVENTLISTENER RabbitAsyncEventListener;
