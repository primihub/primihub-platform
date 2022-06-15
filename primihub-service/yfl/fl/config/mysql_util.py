import pymysql

MYSQL_HOST_NAME = '192.168.0.109'
MYSQL_PORT = 3306
MYSQL_USER = 'root'
MYSQL_PASSWD = '1qazmko0'


class MySqlConnection:
    def __init__(self, db):
        self.db = db
        self.conn = pymysql.connect(host=MYSQL_HOST_NAME, port=MYSQL_PORT, user=MYSQL_USER, passwd=MYSQL_PASSWD, db=self.db)

    def __get_connect(self):
        if not self.db:
            raise (NameError, "no database")
        cur = self.conn.cursor()
        if not cur:
            raise (NameError, "connection failed")
        else:
            return cur

    def exec_query(self, sql):
        cur = self.__get_connect()
        cur.execute(sql)
        result = cur.fetchall()
        return result

    def exec_non_query(self, sql):
        cur = self.__get_connect()
        effect_rows = cur.execute(sql)
        self.conn.commit()
        return effect_rows

    def _get_cur(self):
        return self.__get_connect()

    def _get_con(self):
        return self.conn

    def close(self):
        self.conn.close()
