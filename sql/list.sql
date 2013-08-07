select t.*, m.ID DSID from SYS.SYSTABLES t, SYS.MEMBERS m, SYS.ASYNCEVENTLISTENERS a
       where t.tablename='SQLF_RABBIT_TEST' and groupsintersect(a.SERVER_GROUP, m.SERVERGROUPS)
           and groupsintersect(t.ASYNCLISTENERS, a.ID);
