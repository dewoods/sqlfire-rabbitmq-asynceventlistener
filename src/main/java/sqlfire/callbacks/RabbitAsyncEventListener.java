/*
 * Copyright (c) 2013 Dillon Woods <dewoods@gmail.com>
 *
 * sqlfire-rabbitmq-asynceventlistener is free software; you can redistribute it and/or modify
 * it under the terms of the MIT license. See LICENSE for details.
 */

package sqlfire.callbacks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import com.vmware.sqlfire.callbacks.AsyncEventListener;
import com.vmware.sqlfire.callbacks.Event;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class RabbitAsyncEventListener implements AsyncEventListener {

    private final static String EXCHANGE_NAME = "sqlfire";

    private Connection connection;
    private Channel channel;

    /**
     * Initialization handled in init()
     */
    public void start() {
        return;
    }

    public void close() {
        try {
            this.channel.close();
            this.connection.close();
        } catch( IOException e ) {
            //shutting down after close, ignore RabbitMQ shutdown exception
        }

        return;
    }

    /**
     * Creates a connection RabbitMQ that will be used to queue modified rows
     *
     * @param   rabbitURI   will take the form of "amqp://guest:guest@localhost:5672"
     */
    public void init( String rabbitURI ) {
        ConnectionFactory factory = new ConnectionFactory();

        /**
         * Throw a RunTimeException if connecting to RabbitMQ fails for any reason
         */
        try {
            factory.setUri( rabbitURI );
        } catch ( java.net.URISyntaxException e ) {
            throw new RuntimeException( e );
        } catch( java.security.NoSuchAlgorithmException e ) {
            throw new RuntimeException( e );
        } catch( java.security.KeyManagementException e ) {
            throw new RuntimeException( e );
        }

        try {
            this.connection = factory.newConnection();
            this.channel = this.connection.createChannel();
            this.channel.exchangeDeclare( EXCHANGE_NAME, "topic" );
        } catch( java.io.IOException e ) {
            throw new RuntimeException( e );
        }

        return;
    }

    public boolean processEvents( List<Event> events ) {
        try {
            Iterator<Event> itr = events.iterator();
            Event.Type et = null;

            for( Event event : events ) {
                ResultSetMetaData rsmd = event.getResultSetMetaData();

                int numcols = rsmd.getColumnCount();
                String routingKey = rsmd.getSchemaName( 1 ) + "." + rsmd.getTableName( 1 );

                et = event.getType();
                switch( et ) {
                    case AFTER_INSERT:
                        ResultSet rs = event.getNewRowsAsResultSet();
                        String out = "";
                        
                        //TODO: handle potential batches instead of single next()
                        rs.next();

                        /**
                         * Build a comma seperated list of column values
                         * TODO: handle escaping and quoting of values
                         */
                        out = "";
                        for( int i=0; i < numcols; i++ ) {
                            try {
                                out += rs.getString( i+1 );
                            } catch( SQLException e ) {
                                out += "<NULL>";
                            }

                            if( i != numcols-1 ) {
                                out += ",";
                            }

                        }

                        /**
                         * Each table will have a different routingKey, consisting of schemaname.tablename
                         * This way consumers can subscribe to specific tables
                         */
                        this.channel.basicPublish( EXCHANGE_NAME, routingKey, null, out.getBytes() );

                        break;
                    case AFTER_UPDATE:
                        //TODO: handle updates
                        break;
                    case AFTER_DELETE:
                        //TODO: handle deletes?
                        break;
                }
            }
        } catch( Exception e ) {
            //we were not able to handle this row, it will not be deleted from the internal queue
            return false;
        }

        return true;
    }

}
